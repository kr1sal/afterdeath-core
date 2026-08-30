package com.afterdeath.core.playermode;

import com.afterdeath.core.AfterdeathCore;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.Team;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

// Central place for the "active" afterdeath skills backed by tag rewards.
// Cooldowns live in memory only — a server restart resets them, which is fine.
public final class AfterdeathAbilities {

    public static final String TAG_DASH = "afterdeath.dash";
    public static final String TAG_SUMMON_VEX = "afterdeath.soul_summon_vex";
    public static final String TAG_SUMMON_SKELETON = "afterdeath.skeleton_summon_swordsman";
    public static final String TAG_DAMAGE_BURST = "afterdeath.damage_burst";

    // Entity tag applied to summoned allies so we can find and clean them up.
    public static final String ENTITY_TAG_ALLY = "afterdeath_ally";
    public static final String ALLY_TEAM = "afterdeath_allies";

    private static final ResourceLocation BURST_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath(AfterdeathCore.MODID, "damage_burst");

    private static final int DASH_COOLDOWN_TICKS = 100;      // 5s
    private static final double DASH_HORIZONTAL_IMPULSE = 1.0;
    private static final double DASH_VERTICAL_LIFT = 0.15;

    private static final int SUMMON_COOLDOWN_TICKS = 1200;   // 60s

    private static final int BURST_COOLDOWN_TICKS = 600;     // 30s
    private static final int BURST_DURATION_TICKS = 100;     // 5s
    private static final double BURST_DAMAGE_PER_MAX_HP = 0.5;

    // key = "ability|playerUuid" → gameTime when the ability becomes ready again
    private static final Map<String, Long> COOLDOWNS = new HashMap<>();
    // key = playerUuid → owned summon UUID (only one active summon per player)
    private static final Map<UUID, UUID> ACTIVE_SUMMON = new HashMap<>();
    // key = playerUuid → gameTime when the damage burst modifier expires
    private static final Map<UUID, Long> BURST_EXPIRES = new HashMap<>();

    private AfterdeathAbilities() {}

    /* -------------------- Cooldown helpers -------------------- */

    // Ticks remaining until `key` is ready for this player. 0 = ready now.
    private static int cooldownRemaining(ServerPlayer player, String key) {
        long now = player.serverLevel().getGameTime();
        Long readyAt = COOLDOWNS.get(key + "|" + player.getUUID());
        if (readyAt == null) return 0;
        long diff = readyAt - now;
        return diff > 0 ? (int) diff : 0;
    }

    private static void setCooldown(ServerPlayer player, String key, int cooldownTicks) {
        long now = player.serverLevel().getGameTime();
        COOLDOWNS.put(key + "|" + player.getUUID(), now + cooldownTicks);
    }

    /* -------------------- Dash (Soul + Skeleton) -------------------- */

    // Fires from a sprint-jump.
    public static AbilityResult tryDash(ServerPlayer player) {
        if (!player.getTags().contains(TAG_DASH)) return AbilityResult.NO_ABILITY;
        PlayerMode mode = player.getData(PlayerModeAttachments.PLAYER_MODE);
        if (mode == PlayerMode.HUMAN) return AbilityResult.WRONG_MODE; // dash lives in soul/skeleton phases
        if (!player.isSprinting()) return AbilityResult.NOT_SPRINTING;
        int rem = cooldownRemaining(player, "dash");
        if (rem > 0) return new AbilityResult.OnCooldown(rem);

        Vec3 look = player.getLookAngle();
        Vec3 flatLook = new Vec3(look.x, 0, look.z).normalize();
        Vec3 impulse = flatLook.scale(DASH_HORIZONTAL_IMPULSE).add(0, DASH_VERTICAL_LIFT, 0);

        Vec3 v = player.getDeltaMovement();
        player.setDeltaMovement(v.x + impulse.x, DASH_VERTICAL_LIFT, v.z + impulse.z);
        player.hurtMarked = true;
        player.hasImpulse = true;

        ServerLevel lvl = player.serverLevel();
        lvl.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENDER_DRAGON_FLAP, SoundSource.PLAYERS, 0.5F, 1.6F);
        setCooldown(player, "dash", DASH_COOLDOWN_TICKS);
        return AbilityResult.SUCCESS;
    }

    /* -------------------- Summons -------------------- */

    public static AbilityResult trySummonVex(ServerPlayer player) {
        if (!player.getTags().contains(TAG_SUMMON_VEX)) return AbilityResult.NO_ABILITY;
        if (player.getData(PlayerModeAttachments.PLAYER_MODE) != PlayerMode.SOUL) return AbilityResult.WRONG_MODE;
        int rem = cooldownRemaining(player, "summon");
        if (rem > 0) return new AbilityResult.OnCooldown(rem);
        return doSummon(player, EntityType.VEX);
    }

    public static AbilityResult trySummonSkeleton(ServerPlayer player) {
        if (!player.getTags().contains(TAG_SUMMON_SKELETON)) return AbilityResult.NO_ABILITY;
        if (player.getData(PlayerModeAttachments.PLAYER_MODE) != PlayerMode.SKELETON) return AbilityResult.WRONG_MODE;
        int rem = cooldownRemaining(player, "summon");
        if (rem > 0) return new AbilityResult.OnCooldown(rem);
        return doSummon(player, EntityType.SKELETON);
    }

    private static <T extends Mob> AbilityResult doSummon(ServerPlayer player, EntityType<T> type) {
        ServerLevel lvl = player.serverLevel();
        ensureAllyTeam(lvl.getServer());
        addPlayerToAllyTeam(lvl, player);

        Vec3 look = player.getLookAngle();
        double sx = player.getX() + look.x * 1.5;
        double sy = player.getY();
        double sz = player.getZ() + look.z * 1.5;

        T summon = type.create(lvl, entity -> {}, net.minecraft.core.BlockPos.containing(sx, sy, sz),
                MobSpawnType.SPAWNER, false, false);
        if (summon == null) return AbilityResult.SPAWN_FAILED;
        summon.moveTo(sx, sy, sz, player.getYRot(), 0);
        if (summon instanceof Skeleton s) {
            s.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND,
                    new net.minecraft.world.item.ItemStack(Items.IRON_SWORD));
        }
        if (summon instanceof Vex v) {
            v.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND,
                    new net.minecraft.world.item.ItemStack(Items.IRON_SWORD));
        }
        summon.setPersistenceRequired();
        summon.addTag(ENTITY_TAG_ALLY);
        summon.addTag("owner:" + player.getUUID());
        lvl.getScoreboard().addPlayerToTeam(summon.getScoreboardName(),
                lvl.getScoreboard().getPlayerTeam(ALLY_TEAM));
        if (!lvl.addFreshEntity(summon)) {
            return AbilityResult.SPAWN_FAILED;
        }

        removePreviousSummon(lvl, player);
        ACTIVE_SUMMON.put(player.getUUID(), summon.getUUID());
        lvl.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.EVOKER_PREPARE_SUMMON, SoundSource.PLAYERS, 1.0F, 1.0F);
        setCooldown(player, "summon", SUMMON_COOLDOWN_TICKS);
        return AbilityResult.SUCCESS;
    }

    private static void ensureAllyTeam(MinecraftServer server) {
        Scoreboard sb = server.getScoreboard();
        if (sb.getPlayerTeam(ALLY_TEAM) != null) return;
        PlayerTeam team = sb.addPlayerTeam(ALLY_TEAM);
        team.setAllowFriendlyFire(false);
        team.setSeeFriendlyInvisibles(true);
        team.setCollisionRule(Team.CollisionRule.NEVER);
    }

    private static void addPlayerToAllyTeam(ServerLevel lvl, ServerPlayer player) {
        Scoreboard sb = lvl.getScoreboard();
        PlayerTeam team = sb.getPlayerTeam(ALLY_TEAM);
        if (team == null) return;
        if (sb.getPlayersTeam(player.getScoreboardName()) != team) {
            sb.addPlayerToTeam(player.getScoreboardName(), team);
        }
    }

    private static void removePreviousSummon(ServerLevel lvl, ServerPlayer player) {
        UUID prev = ACTIVE_SUMMON.remove(player.getUUID());
        if (prev == null) return;
        Entity old = lvl.getEntity(prev);
        if (old != null && old.isAlive()) {
            old.discard();
        }
    }

    /* -------------------- Damage burst (Skeleton) -------------------- */

    public static AbilityResult tryDamageBurst(ServerPlayer player) {
        if (!player.getTags().contains(TAG_DAMAGE_BURST)) return AbilityResult.NO_ABILITY;
        if (player.getData(PlayerModeAttachments.PLAYER_MODE) != PlayerMode.SKELETON) return AbilityResult.WRONG_MODE;
        int rem = cooldownRemaining(player, "burst");
        if (rem > 0) return new AbilityResult.OnCooldown(rem);

        AttributeInstance attr = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attr == null) return AbilityResult.SPAWN_FAILED;

        double bonus = player.getMaxHealth() * BURST_DAMAGE_PER_MAX_HP;
        attr.removeModifier(BURST_MODIFIER_ID);
        attr.addTransientModifier(new AttributeModifier(
                BURST_MODIFIER_ID, bonus, AttributeModifier.Operation.ADD_VALUE));
        BURST_EXPIRES.put(player.getUUID(),
                player.serverLevel().getGameTime() + BURST_DURATION_TICKS);

        // Visual/audio cue.
        player.addEffect(new MobEffectInstance(MobEffects.GLOWING, BURST_DURATION_TICKS, 0, false, false));
        ServerLevel lvl = player.serverLevel();
        lvl.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.SKELETON_HURT, SoundSource.PLAYERS, 1.5F, 0.6F);
        setCooldown(player, "burst", BURST_COOLDOWN_TICKS);
        return AbilityResult.SUCCESS;
    }

    // Called every server tick from AbilityTickEvents to expire the burst.
    public static void tickBurstExpiry(ServerPlayer player) {
        UUID id = player.getUUID();
        Long expires = BURST_EXPIRES.get(id);
        if (expires == null) return;
        if (player.serverLevel().getGameTime() < expires) return;
        AttributeInstance attr = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attr != null) attr.removeModifier(BURST_MODIFIER_ID);
        BURST_EXPIRES.remove(id);
    }

    // Called when a player leaves so we don't leak per-player state.
    public static void clearPlayer(UUID playerId) {
        BURST_EXPIRES.remove(playerId);
        ACTIVE_SUMMON.remove(playerId);
        COOLDOWNS.keySet().removeIf(key -> key.endsWith("|" + playerId));
    }
}

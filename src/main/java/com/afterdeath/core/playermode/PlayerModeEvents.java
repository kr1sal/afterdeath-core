package com.afterdeath.core.playermode;

import com.afterdeath.core.Config;
import com.afterdeath.core.AfterdeathCore;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@EventBusSubscriber(modid = AfterdeathCore.MODID)
public final class PlayerModeEvents {

    private PlayerModeEvents() {}

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        PlayerMode mode = player.getData(PlayerModeAttachments.PLAYER_MODE);
        PlayerModeTransform.syncSkillCategories(player, mode);
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        PlayerMode mode = player.getData(PlayerModeAttachments.PLAYER_MODE);
        PlayerModeTransform.syncSkillCategories(player, mode);
    }

    @SubscribeEvent
    public static void onPlayerAttack(AttackEntityEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;

        PlayerInfo playerInfo = Objects.requireNonNull(Minecraft.getInstance().getConnection()).getPlayerInfo(player.getUUID());
        if (playerInfo != null) {
            GameType gameMode = playerInfo.getGameMode();
            if (gameMode != GameType.SURVIVAL) return;
        }

        PlayerMode mode = player.getData(PlayerModeAttachments.PLAYER_MODE);
        if (mode.isHostileTarget()) return;

        if (!(event.getTarget() instanceof Mob attacked) || !(attacked instanceof Enemy)) return;

        broadcastAnger(attacked, player);
    }

    @SubscribeEvent
    public static void onMobShotByArrow(LivingIncomingDamageEvent event) {
        if (event.getEntity().level().isClientSide) return;
        if (!(event.getEntity() instanceof Mob attacked) || !(attacked instanceof Enemy)) return;

        if (!(event.getSource().getDirectEntity() instanceof AbstractArrow)) return;
        if (!(event.getSource().getEntity() instanceof Player player)) return;

        PlayerMode mode = player.getData(PlayerModeAttachments.PLAYER_MODE);
        if (mode.isHostileTarget()) return;

        broadcastAnger(attacked, player);
    }

    private static void broadcastAnger(Mob attacked, Player player) {
        angerMob(attacked, player);

        double radius = Config.PLAYERMODE_BROADCAST_RADIUS.get();
        if (radius <= 0.0D) return;

        AABB area = attacked.getBoundingBox().inflate(radius);
        for (Entity nearby : attacked.level().getEntities(attacked, area,
                m -> m != attacked && m instanceof Enemy)) {
            if (nearby instanceof Mob mob) {
                angerMob(mob, player);
            }
        }
    }

    @SubscribeEvent
    public static void onChangeTarget(LivingChangeTargetEvent event) {
        if (!(event.getEntity() instanceof Enemy)) return;

        if (shouldIgnorePlayer(event.getEntity(), event.getNewAboutToBeSetTarget())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onMobTick(EntityTickEvent.Post event) {
        if (event.getEntity().level().isClientSide) return;
        if (!(event.getEntity() instanceof Mob mob) || !(mob instanceof Enemy)) return;

        LivingEntity target = mob.getTarget();
        if (shouldIgnorePlayer(mob, target)) {
            mob.setTarget(null);
        }
    }

    private static void angerMob(Mob mob, Player player) {
        long untilTick = mob.level().getGameTime() + Config.PLAYERMODE_ANGER_TICKS.get();
        mob.setData(PlayerModeAttachments.ANGER, new AngerData(player.getUUID(), untilTick));
        mob.setTarget(player);
    }

    private static boolean shouldIgnorePlayer(LivingEntity mob, @Nullable LivingEntity target) {
        if (!(target instanceof Player player)) return false;

        PlayerMode mode = player.getData(PlayerModeAttachments.PLAYER_MODE);
        if (mode.isHostileTarget()) return false;

        AngerData anger = mob.getData(PlayerModeAttachments.ANGER);
        return !anger.isAngryAt(player.getUUID(), mob.level().getGameTime());
    }
}

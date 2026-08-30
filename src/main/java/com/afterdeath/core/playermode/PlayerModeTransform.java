package com.afterdeath.core.playermode;

import com.mojang.logging.LogUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import org.slf4j.Logger;

public final class PlayerModeTransform {

    private static final Logger LOGGER = LogUtils.getLogger();

    private PlayerModeTransform() {}

    public static boolean apply(ServerPlayer player, PlayerMode target) {
        PlayerMode current = player.getData(PlayerModeAttachments.PLAYER_MODE);
        if (current == target) return false;

        player.setData(PlayerModeAttachments.PLAYER_MODE, target);

        if (target == PlayerMode.SKELETON) {
            player.clearFire();
        }
        if (target == PlayerMode.HUMAN) {
            player.level().broadcastEntityEvent(player, (byte) 35);
        }

        // Sync categories first so tag-granting rewards (flight, stamina bonus)
        // are already applied before we compute the stamina cap for this session.
        syncSkillCategories(player, target);

        if (target == PlayerMode.SOUL) {
            player.setData(PlayerModeAttachments.SPIRIT_FLIGHT_TICKS,
                    SpiritFlightEvents.maxFlightTicks(player));
        }

        playMorphEffect(player, target);
        return true;
    }

    // Toggle afterdeath_skills categories so each phase exposes the trees it
    // is allowed to see. Cumulative: SOUL → soul; SKELETON → soul + skeleton;
    // HUMAN → all three. Locking preserves purchased skills; puffish_skills
    // strips the reward buffs while locked and re-applies them on unlock.
    public static void syncSkillCategories(ServerPlayer player, PlayerMode target) {
        MinecraftServer server = player.getServer();
        if (server == null) {
            return;
        }

        boolean soul = false;
        boolean skeleton = false;
        boolean human = false;

        switch (target) {
            case PlayerMode.HUMAN -> human = true;
            case PlayerMode.SKELETON -> skeleton = true;
            case PlayerMode.SOUL -> soul = true;
        }

        runCategoryCommand(server, player, "soul", soul);
        runCategoryCommand(server, player, "skeleton", skeleton);
        runCategoryCommand(server, player, "human", human);
    }

    private static void runCategoryCommand(MinecraftServer server, ServerPlayer player, String category, boolean unlock) {
        String verb = unlock ? "unlock" : "lock";
        String command = "puffish_skills category " + verb + " " + player.getGameProfile().getName()
                + " afterdeath_skills:" + category;
        int result;
        try {
            result = server.getCommands().getDispatcher()
                    .execute(command, server.createCommandSourceStack());
        } catch (com.mojang.brigadier.exceptions.CommandSyntaxException e) {
            LOGGER.warn("[playermode] `{}` failed to parse/execute: {}", command, e.getMessage());
            player.sendSystemMessage(Component.literal("[playermode] parse fail: " + e.getMessage())
                    .withStyle(ChatFormatting.RED));
            return;
        }
        if (result == 0) {
            player.sendSystemMessage(Component.literal("[playermode] returned 0: " + command)
                    .withStyle(ChatFormatting.RED));
        }
    }

    private static void playMorphEffect(ServerPlayer player, PlayerMode mode) {
        ServerLevel lvl = player.serverLevel();
        double cx = player.getX();
        double cy = player.getY() + player.getBbHeight() * 0.5D;
        double cz = player.getZ();
        double width = player.getBbWidth();

        switch (mode) {
            case HUMAN -> {
                ring(lvl, ParticleTypes.TOTEM_OF_UNDYING, cx, cy, cz, width, 60, 0.35D);
                ring(lvl, ParticleTypes.END_ROD,          cx, cy, cz, width, 20, 0.05D);
                play(lvl, cx, cy, cz, SoundEvents.TOTEM_USE,     1.0F, 1.0F);
                play(lvl, cx, cy, cz, SoundEvents.PLAYER_LEVELUP, 0.9F, 1.1F);
            }
            case SOUL -> {
                ring(lvl, ParticleTypes.SOUL,            cx, cy, cz, width, 40, 0.10D);
                ring(lvl, ParticleTypes.SOUL_FIRE_FLAME, cx, cy, cz, width, 30, 0.02D);
                play(lvl, cx, cy, cz, SoundEvents.PORTAL_TRIGGER,    0.9F, 1.6F);
                play(lvl, cx, cy, cz, SoundEvents.ENDERMAN_TELEPORT, 1.0F, 0.6F);
            }
            case SKELETON -> {
                ring(lvl, ParticleTypes.POOF,            cx, cy, cz, width, 50, 0.06D);
                ring(lvl, ParticleTypes.SOUL_FIRE_FLAME, cx, cy, cz, width, 20, 0.02D);
                play(lvl, cx, cy, cz, SoundEvents.SKELETON_HURT, 1.2F, 0.7F);
                play(lvl, cx, cy, cz, SoundEvents.SKELETON_STEP, 1.0F, 0.6F);
            }
        }
    }

    private static void ring(ServerLevel lvl, ParticleOptions particle,
                             double cx, double cy, double cz, double width,
                             int count, double speed) {
        double spread = 0.4D + width * 0.5D;
        lvl.sendParticles(particle, cx, cy, cz, count, spread, 0.9D, spread, speed);
    }

    private static void play(ServerLevel lvl, double x, double y, double z,
                             SoundEvent sound, float volume, float pitch) {
        lvl.playSound(null, x, y, z, sound, SoundSource.PLAYERS, volume, pitch);
    }
}

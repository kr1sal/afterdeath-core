package com.afterdeath.core.playermode;

import com.afterdeath.core.AfterdeathCore;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = AfterdeathCore.MODID)
public final class SkeletonSunburnEvents {

    private static final float BRIGHTNESS_THRESHOLD = 0.5F;
    private static final float IGNITE_CHANCE_SCALE = 30.0F;
    private static final float IGNITE_SECONDS = 8.0F;

    private SkeletonSunburnEvents() {}

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.getAbilities().invulnerable) return;
        if (player.getData(PlayerModeAttachments.PLAYER_MODE) != PlayerMode.SKELETON) return;

        ServerLevel level = player.serverLevel();
        if (!level.isDay()) return;
        if (player.isInWaterRainOrBubble() || player.isInPowderSnow) return;
        if (player.hasEffect(MobEffects.FIRE_RESISTANCE)) return;

        BlockPos eyePos = BlockPos.containing(player.getX(), player.getEyeY(), player.getZ());
        if (!level.canSeeSky(eyePos)) return;

        float brightness = player.getLightLevelDependentMagicValue();
        if (brightness <= BRIGHTNESS_THRESHOLD) return;
        if (player.getRandom().nextFloat() * IGNITE_CHANCE_SCALE >= (brightness - 0.4F) * 2.0F) return;

        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        if (!helmet.isEmpty()) {
            helmet.hurtAndBreak(1, player, EquipmentSlot.HEAD);
            return;
        }

        player.igniteForSeconds(IGNITE_SECONDS);
    }
}

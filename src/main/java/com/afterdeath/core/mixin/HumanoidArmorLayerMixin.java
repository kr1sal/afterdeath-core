package com.afterdeath.core.mixin;

import com.afterdeath.core.playermode.PlayerMode;
import com.afterdeath.core.playermode.PlayerModeAttachments;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Hides player armor whenever a skeleton-morph player is drawn through the
 * humanoid pipeline. That path is used by Better Combat's first-person swing
 * (BC pipes the full HumanoidArmorLayer into the 1p arm), so without this
 * suppression the morph shows a bare human arm plus vanilla armor while the
 * 3p view shows the boney skeleton.
 *
 * The 3p skeleton render goes through {@code skeletonRenderer.render(dummy)}
 * with a {@link net.minecraft.world.entity.monster.Skeleton} entity, so the
 * {@code entity instanceof Player} guard leaves that path untouched — the
 * skeleton keeps its armor.
 */
@Mixin(value = HumanoidArmorLayer.class, priority = 1500)
public abstract class HumanoidArmorLayerMixin {

    @Inject(
            method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V",
            at = @At("HEAD"),
            cancellable = true,
            require = 0
    )
    private void afterdeath_core$hideArmorForSkeletonPlayer(
            PoseStack pose, MultiBufferSource buffers, int light,
            LivingEntity entity, float limbSwing, float limbSwingAmount, float partialTick,
            float ageInTicks, float netHeadYaw, float headPitch,
            CallbackInfo ci
    ) {
        if (entity instanceof Player player
                && player.getData(PlayerModeAttachments.PLAYER_MODE) == PlayerMode.SKELETON) {
            ci.cancel();
        }
    }
}

package com.afterdeath.core.mixin;

import com.afterdeath.core.playermode.MorphCapture;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Snapshot / overwrite the PoseStack at the very entry of ItemRenderer.renderStatic.
 *
 * Better Combat pushes its sword-tilt onto the PoseStack somewhere inside the
 * item-in-hand pipeline. Hooking directly at renderStatic's HEAD is the last
 * shared choke-point before the actual item mesh gets drawn — anything BC does
 * before this call is baked into the pose here.
 *
 * priority = 1500 keeps our callback ordered AFTER default-priority mods, so we
 * see the pose with BC's tilt applied rather than before it.
 *
 * Gated by MorphCapture.isActive(), so unrelated renderStatic calls (ground
 * items, GUI, etc.) are cheap no-ops.
 */
@Mixin(value = ItemRenderer.class, priority = 1500)
public abstract class ItemRendererMixin {

    @Inject(
            method = "renderStatic(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/level/Level;III)V",
            at = @At("HEAD"),
            require = 0
    )
    private void afterdeath_core$onRenderStaticHead(LivingEntity entity, ItemStack stack,
                                               ItemDisplayContext displayContext,
                                               boolean leftHand,
                                               PoseStack pose, MultiBufferSource buffers,
                                               Level level, int light, int overlay, int seed,
                                               CallbackInfo ci) {
        if (!MorphCapture.isActive()) return;
        HumanoidArm arm = leftHand ? HumanoidArm.LEFT : HumanoidArm.RIGHT;
        MorphCapture.onItemPose(arm, pose);
    }
}

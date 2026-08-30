package com.afterdeath.core.mixin;

import com.afterdeath.core.playermode.MorphCapture;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Hooks ModelPart.render right AFTER the part applies its own
 * translateAndRotate. Bendy-lib style mods (Better Combat) inject their extra
 * PoseStack pushes at that same moment; this callback fires after theirs so we
 * see the fully-transformed pose.
 *
 * MorphCapture decides whether to snapshot the pose (for the discard warm-up)
 * or overwrite it (during the skeleton pass) — this mixin has no state.
 */
@Mixin(value = ModelPart.class, priority = 1500)
public abstract class ModelPartMixin {

    @Inject(
            method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/model/geom/ModelPart;translateAndRotate(Lcom/mojang/blaze3d/vertex/PoseStack;)V",
                    shift = At.Shift.AFTER
            )
    )
    private void afterdeath_core$onPartPosed(PoseStack pose, VertexConsumer buffer,
                                        int packedLight, int packedOverlay, int color,
                                        CallbackInfo ci) {
        if (!MorphCapture.isActive()) return;
        MorphCapture.onPartPosed((ModelPart) (Object) this, pose);
    }
}

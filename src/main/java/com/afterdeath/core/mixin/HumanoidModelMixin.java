package com.afterdeath.core.mixin;

import com.afterdeath.core.playermode.MorphCapture;
import net.minecraft.client.model.HumanoidModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * HumanoidArmorLayer calls {@code outerModel.copyPropertiesTo(armorModel)}
 * before rendering each armor piece. We piggy-back on that call to register
 * the armor HumanoidModel's own ModelParts against our role table, so when
 * armor's ModelPart.render fires the shared ModelPart mixin looks up the
 * matching snapshot and replays BC's PoseStack pushes on the armor too.
 */
@Mixin(value = HumanoidModel.class, priority = 1500)
public abstract class HumanoidModelMixin {

    @Inject(
            method = "copyPropertiesTo(Lnet/minecraft/client/model/HumanoidModel;)V",
            at = @At("RETURN"),
            require = 0
    )
    private void afterdeath_core$registerArmorTarget(HumanoidModel<?> other, CallbackInfo ci) {
        MorphCapture.registerArmorTarget(other);
    }
}

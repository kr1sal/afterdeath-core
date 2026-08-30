package com.afterdeath.core.playermode;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.SkeletonModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import org.jetbrains.annotations.Nullable;

public class MorphSkeletonModel extends SkeletonModel<AbstractSkeleton> {

    @Nullable private HumanoidModel<?> partSource;

    public MorphSkeletonModel(ModelPart root) {
        super(root);
    }

    public void setPartSource(@Nullable HumanoidModel<?> source) {
        this.partSource = source;
    }

    @Override
    public void setupAnim(AbstractSkeleton entity,
                          float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        HumanoidModel<?> src = this.partSource;
        if (src == null) return;

        copyRotations(src.head, this.head);
        copyRotations(src.hat, this.hat);
        copyRotations(src.body, this.body);
        copyRotations(src.rightArm, this.rightArm);
        copyRotations(src.leftArm, this.leftArm);
        copyRotations(src.rightLeg, this.rightLeg);
        copyRotations(src.leftLeg, this.leftLeg);
    }

    private static void copyRotations(ModelPart from, ModelPart to) {
        to.xRot = from.xRot;
        to.yRot = from.yRot;
        to.zRot = from.zRot;
        to.visible = from.visible;
    }
}

package com.afterdeath.core.playermode;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.HumanoidArm;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.EnumMap;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Role-keyed pose snapshots for the skeleton morph.
 *
 * ModelPart identities of the player, skeleton, and each armor HumanoidModel
 * are all different objects. But every HumanoidModel shares the same seven
 * named parts, so we key snapshots by a {@link Role} enum instead of by
 * ModelPart identity. Record-side and apply-side maps translate part identity
 * to role; the armor mixin fills apply-side roles for armor models on the fly.
 */
public final class MorphCapture {

    public enum Role { HEAD, HAT, BODY, RIGHT_ARM, LEFT_ARM, RIGHT_LEG, LEFT_LEG }

    private static boolean recording;
    private static boolean applying;

    private static Map<ModelPart, Role> recordRoles = new IdentityHashMap<>();
    private static Map<ModelPart, Role> applyRoles = new IdentityHashMap<>();

    private static final EnumMap<Role, Snapshot> byRole = new EnumMap<>(Role.class);
    private static final EnumMap<HumanoidArm, Snapshot> itemPoses = new EnumMap<>(HumanoidArm.class);

    private MorphCapture() {}

    public static void beginRecording(Map<ModelPart, Role> playerRoles) {
        recordRoles = playerRoles;
        byRole.clear();
        itemPoses.clear();
        recording = true;
    }

    public static void endRecording() {
        recording = false;
    }

    public static void beginApplying(Map<ModelPart, Role> skelRoles) {
        applyRoles = new IdentityHashMap<>(skelRoles);
        applying = true;
    }

    public static void endApplying() {
        applying = false;
        applyRoles = new IdentityHashMap<>();
        recordRoles = new IdentityHashMap<>();
        byRole.clear();
        itemPoses.clear();
    }

    public static boolean isActive() {
        return recording || applying;
    }

    public static void onPartPosed(ModelPart part, PoseStack pose) {
        if (recording) {
            Role role = recordRoles.get(part);
            if (role == null) return;
            PoseStack.Pose top = pose.last();
            byRole.put(role, new Snapshot(new Matrix4f(top.pose()), new Matrix3f(top.normal())));
            return;
        }
        if (applying) {
            Role role = applyRoles.get(part);
            if (role == null) return;
            Snapshot snap = byRole.get(role);
            if (snap == null) return;
            PoseStack.Pose top = pose.last();
            top.pose().set(snap.matrix);
            top.normal().set(snap.normal);
        }
    }

    /**
     * Called from a mixin on HumanoidModel.copyPropertiesTo. Once the skeleton
     * has copied its rotations into the armor's HumanoidModel, we register
     * that armor model's own parts under the same roles — so when armor's
     * ModelPart.render fires, it looks up the same snapshot and gets BC's
     * PoseStack pushes replayed on the armor too.
     */
    public static void registerArmorTarget(HumanoidModel<?> armor) {
        if (!applying) return;
        applyRoles.put(armor.head, Role.HEAD);
        applyRoles.put(armor.hat, Role.HAT);
        applyRoles.put(armor.body, Role.BODY);
        applyRoles.put(armor.rightArm, Role.RIGHT_ARM);
        applyRoles.put(armor.leftArm, Role.LEFT_ARM);
        applyRoles.put(armor.rightLeg, Role.RIGHT_LEG);
        applyRoles.put(armor.leftLeg, Role.LEFT_LEG);
    }

    public static void onItemPose(HumanoidArm arm, PoseStack pose) {
        if (recording) {
            PoseStack.Pose top = pose.last();
            itemPoses.put(arm, new Snapshot(new Matrix4f(top.pose()), new Matrix3f(top.normal())));
            return;
        }
        if (applying) {
            Snapshot snap = itemPoses.get(arm);
            if (snap == null) return;
            PoseStack.Pose top = pose.last();
            top.pose().set(snap.matrix);
            top.normal().set(snap.normal);
        }
    }

    private record Snapshot(Matrix4f matrix, Matrix3f normal) {}
}

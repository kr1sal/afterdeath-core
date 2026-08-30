package com.afterdeath.core.playermode;

import com.afterdeath.core.AfterdeathCore;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.SkeletonRenderer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.WalkAnimationState;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import net.minecraft.client.model.geom.ModelPart;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.util.IdentityHashMap;
import java.util.Map;

@EventBusSubscriber(modid = AfterdeathCore.MODID, value = Dist.CLIENT)
public final class MorphRenderer {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static Skeleton cachedDummy;
    private static ClientLevel cachedFor;
    private static MorphSkeletonModel cachedMorphModel;
    private static Field cachedModelField;
    private static boolean modelFieldMissing;

    private static boolean insideMorphRender;

    private static final VarHandle WALK_POSITION;
    private static final VarHandle WALK_SPEED;
    private static final VarHandle WALK_SPEED_OLD;

    static {
        VarHandle pos = null, spd = null, spdOld = null;
        try {
            MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(WalkAnimationState.class, MethodHandles.lookup());
            pos = lookup.findVarHandle(WalkAnimationState.class, "position", float.class);
            spd = lookup.findVarHandle(WalkAnimationState.class, "speed", float.class);
            spdOld = lookup.findVarHandle(WalkAnimationState.class, "speedOld", float.class);
        } catch (ReflectiveOperationException e) {
            LOGGER.error("Failed to bind WalkAnimationState fields; skeleton morph legs may look stiff", e);
        }
        WALK_POSITION = pos;
        WALK_SPEED = spd;
        WALK_SPEED_OLD = spdOld;
    }

    private MorphRenderer() {}

    private static Boolean savedHead;
    private static Boolean savedHat;

    @SubscribeEvent
    public static void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        if (insideMorphRender) return;
        Player player = event.getEntity();
        PlayerMode mode = player.getData(PlayerModeAttachments.PLAYER_MODE);
        if (mode != PlayerMode.SKELETON) return;
        if (!(player instanceof AbstractClientPlayer clientPlayer)) return;

        Minecraft mc = Minecraft.getInstance();
        // Better Combat runs PlayerRenderer.render even in first person to
        // drive the swing animation. Cancelling here would kill that anim;
        // letting it through unmodified pokes the head+hat (including the
        // helmet, since HumanoidArmorLayer inherits visibility) straight into
        // the camera. Compromise: hide head and hat for this call only, let
        // BC animate the rest, restore on Post.
        if (player == mc.player && mc.options.getCameraType().isFirstPerson()) {
            PlayerModel<AbstractClientPlayer> model = event.getRenderer().getModel();
            savedHead = model.head.visible;
            savedHat = model.hat.visible;
            model.head.visible = false;
            model.hat.visible = false;
            return;
        }

        event.setCanceled(true);
        renderSkeleton(clientPlayer, event);
    }

    @SubscribeEvent
    public static void onRenderPlayerPost(RenderPlayerEvent.Post event) {
        if (savedHead == null) return;
        Minecraft mc = Minecraft.getInstance();
        if (event.getEntity() != mc.player) return;
        PlayerModel<AbstractClientPlayer> model = event.getRenderer().getModel();
        model.head.visible = savedHead;
        model.hat.visible = savedHat;
        savedHead = null;
        savedHat = null;
    }

    private static void renderSkeleton(AbstractClientPlayer player, RenderPlayerEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null) return;

        Skeleton s = dummy(level);
        syncState(player, s);

        float partialTick = event.getPartialTick();
        PoseStack pose = event.getPoseStack();
        MultiBufferSource buffers = event.getMultiBufferSource();
        int light = event.getPackedLight();

        // Warm up the vanilla player renderer into a discard buffer so third-party
        // animation mods (e.g. Better Combat) get to run. The ModelPart mixin
        // snapshots the PoseStack transform per part; nothing hits real buffers.
        PlayerModel<AbstractClientPlayer> playerModel = event.getRenderer().getModel();
        MorphSkeletonModel morph = morphModel(mc);

        // Run the warm-up on a scratch PoseStack seeded from the real one, so
        // any unbalanced push/pop from other mods' PlayerRenderer mixins (Iris,
        // batched entity rendering, etc.) stays in the throwaway stack and never
        // trips LevelRenderer.checkPoseStack. Captures share absolute matrices
        // because scratch's top is copied from the real top.
        PoseStack scratch = new PoseStack();
        scratch.last().pose().set(pose.last().pose());
        scratch.last().normal().set(pose.last().normal());

        insideMorphRender = true;
        MorphCapture.beginRecording(roles(playerModel));
        try {
            event.getRenderer().render(player, player.getYRot(), partialTick,
                    scratch, DiscardBuffers.INSTANCE, light);
        } catch (Throwable t) {
            LOGGER.debug("Warm-up player render failed", t);
        } finally {
            MorphCapture.endRecording();
            insideMorphRender = false;
        }

        EntityRenderer<? super Skeleton> generic = mc.getEntityRenderDispatcher().getRenderer(s);
        if (generic instanceof SkeletonRenderer skeletonRenderer) {
            Field modelField = livingModelField();
            if (morph != null && modelField != null) {
                Object original = null;
                morph.setPartSource(playerModel);
                MorphCapture.beginApplying(roles(morph));
                try {
                    original = modelField.get(skeletonRenderer);
                    modelField.set(skeletonRenderer, morph);
                    skeletonRenderer.render(s, player.getYRot(), partialTick, pose, buffers, light);
                } catch (ReflectiveOperationException e) {
                    LOGGER.error("Failed to swap SkeletonRenderer model; falling back to vanilla skeleton", e);
                    generic.render(s, player.getYRot(), partialTick, pose, buffers, light);
                } finally {
                    MorphCapture.endApplying();
                    morph.setPartSource(null);
                    if (original != null) {
                        try {
                            modelField.set(skeletonRenderer, original);
                        } catch (IllegalAccessException e) {
                            LOGGER.error("Failed to restore SkeletonRenderer model", e);
                        }
                    }
                }
                return;
            }
        }
        generic.render(s, player.getYRot(), partialTick, pose, buffers, light);
    }

    private static Map<ModelPart, MorphCapture.Role> roles(net.minecraft.client.model.HumanoidModel<?> model) {
        Map<ModelPart, MorphCapture.Role> m = new IdentityHashMap<>(8);
        m.put(model.head, MorphCapture.Role.HEAD);
        m.put(model.hat, MorphCapture.Role.HAT);
        m.put(model.body, MorphCapture.Role.BODY);
        m.put(model.rightArm, MorphCapture.Role.RIGHT_ARM);
        m.put(model.leftArm, MorphCapture.Role.LEFT_ARM);
        m.put(model.rightLeg, MorphCapture.Role.RIGHT_LEG);
        m.put(model.leftLeg, MorphCapture.Role.LEFT_LEG);
        return m;
    }

    private static Skeleton dummy(ClientLevel level) {
        if (cachedDummy == null || cachedFor != level) {
            cachedDummy = new Skeleton(EntityType.SKELETON, level);
            cachedFor = level;
        }
        return cachedDummy;
    }

    private static MorphSkeletonModel morphModel(Minecraft mc) {
        if (cachedMorphModel == null) {
            try {
                cachedMorphModel = new MorphSkeletonModel(mc.getEntityModels().bakeLayer(ModelLayers.SKELETON));
            } catch (Throwable t) {
                LOGGER.error("Failed to bake morph skeleton model", t);
            }
        }
        return cachedMorphModel;
    }

    private static Field livingModelField() {
        if (modelFieldMissing) return null;
        if (cachedModelField != null) return cachedModelField;
        try {
            Field f = LivingEntityRenderer.class.getDeclaredField("model");
            f.setAccessible(true);
            cachedModelField = f;
            return f;
        } catch (NoSuchFieldException e) {
            modelFieldMissing = true;
            LOGGER.error("LivingEntityRenderer.model field not found; BC-anim skeleton disabled", e);
            return null;
        }
    }

    private static void syncState(Player player, Skeleton s) {
        s.setPos(player.getX(), player.getY(), player.getZ());
        s.xo = player.xo;
        s.yo = player.yo;
        s.zo = player.zo;

        s.setYRot(player.getYRot());
        s.yRotO = player.yRotO;
        s.setXRot(player.getXRot());
        s.xRotO = player.xRotO;
        s.yBodyRot = player.yBodyRot;
        s.yBodyRotO = player.yBodyRotO;
        s.yHeadRot = player.yHeadRot;
        s.yHeadRotO = player.yHeadRotO;
        s.setYHeadRot(player.getYHeadRot());

        s.attackAnim = player.attackAnim;
        s.oAttackAnim = player.oAttackAnim;
        s.hurtTime = player.hurtTime;
        s.hurtDuration = player.hurtDuration;
        s.deathTime = player.deathTime;
        s.tickCount = player.tickCount;

        s.setPose(player.getPose());
        s.setDeltaMovement(player.getDeltaMovement());
        s.setSprinting(player.isSprinting());
        s.setSharedFlagOnFire(player.isOnFire());
        s.setInvisible(player.isInvisible());
        s.setSwimming(player.isSwimming());

        copyWalkAnimation(player.walkAnimation, s.walkAnimation);

        s.setItemSlot(EquipmentSlot.MAINHAND, player.getMainHandItem());
        s.setItemSlot(EquipmentSlot.OFFHAND, player.getOffhandItem());
        s.setItemSlot(EquipmentSlot.HEAD, player.getItemBySlot(EquipmentSlot.HEAD));
        s.setItemSlot(EquipmentSlot.CHEST, player.getItemBySlot(EquipmentSlot.CHEST));
        s.setItemSlot(EquipmentSlot.LEGS, player.getItemBySlot(EquipmentSlot.LEGS));
        s.setItemSlot(EquipmentSlot.FEET, player.getItemBySlot(EquipmentSlot.FEET));
    }

    private static void copyWalkAnimation(WalkAnimationState from, WalkAnimationState to) {
        if (WALK_POSITION == null) {
            to.setSpeed(from.speed());
            return;
        }
        WALK_POSITION.set(to, (float) WALK_POSITION.get(from));
        WALK_SPEED.set(to, (float) WALK_SPEED.get(from));
        WALK_SPEED_OLD.set(to, (float) WALK_SPEED_OLD.get(from));
    }

    private static int soulTickCounter;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null || mc.isPaused()) return;

        soulTickCounter++;
        for (Player p : level.players()) {
            PlayerMode mode = p.getData(PlayerModeAttachments.PLAYER_MODE);
            if (mode == PlayerMode.SOUL) spawnSoulTrail(level, p);
        }
    }

    private static void spawnSoulTrail(ClientLevel level, Player player) {
        boolean flying = player.getAbilities().flying;
        int interval = flying ? 3 : 6;
        if (soulTickCounter % interval != 0) return;

        RandomSource rng = level.random;
        double w = player.getBbWidth() * 0.6D;
        double dx = (rng.nextDouble() - 0.5D) * w;
        double dy = rng.nextDouble() * (player.getBbHeight() * 0.7D);
        double dz = (rng.nextDouble() - 0.5D) * w;
        level.addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                player.getX() + dx,
                player.getY() + dy,
                player.getZ() + dz,
                0.0D, 0.015D, 0.0D);
    }
}

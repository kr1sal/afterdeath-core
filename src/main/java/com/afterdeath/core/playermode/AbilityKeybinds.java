package com.afterdeath.core.playermode;

import com.afterdeath.core.AfterdeathCore;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

// Client-side keybinds for the active afterdeath skills.
// Defaults: Z / X / C. Users can rebind in Controls; unbinding = key not set.
public final class AbilityKeybinds {

    public static final String CATEGORY = "key.categories.afterdeath_core";

    public static final KeyMapping SUMMON_VEX = new KeyMapping(
            "key.afterdeath_core.summon_vex",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_Z,
            CATEGORY);

    public static final KeyMapping SUMMON_SKELETON = new KeyMapping(
            "key.afterdeath_core.summon_skeleton",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_X,
            CATEGORY);

    public static final KeyMapping DAMAGE_BURST = new KeyMapping(
            "key.afterdeath_core.damage_burst",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_C,
            CATEGORY);

    public static final KeyMapping DASH = new KeyMapping(
            "key.afterdeath_core.dash",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            CATEGORY);

    private AbilityKeybinds() {}

    @EventBusSubscriber(modid = AfterdeathCore.MODID, value = Dist.CLIENT)
    public static final class ModBus {
        private ModBus() {}

        @SubscribeEvent
        public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
            event.register(SUMMON_VEX);
            event.register(SUMMON_SKELETON);
            event.register(DAMAGE_BURST);
            event.register(DASH);
        }
    }

    @EventBusSubscriber(modid = AfterdeathCore.MODID, value = Dist.CLIENT)
    public static final class GameBus {
        private GameBus() {}

        @SubscribeEvent
        public static void onClientTick(ClientTickEvent.Post event) {
            while (SUMMON_VEX.consumeClick()) {
                PacketDistributor.sendToServer(new UseAbilityPayload("summon_vex"));
            }
            while (SUMMON_SKELETON.consumeClick()) {
                PacketDistributor.sendToServer(new UseAbilityPayload("summon_skeleton"));
            }
            while (DAMAGE_BURST.consumeClick()) {
                PacketDistributor.sendToServer(new UseAbilityPayload("damage_burst"));
            }
            while (DASH.consumeClick()) {
                PacketDistributor.sendToServer(new UseAbilityPayload("dash"));
            }
        }
    }
}

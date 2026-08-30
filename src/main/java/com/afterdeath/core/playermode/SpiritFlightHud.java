package com.afterdeath.core.playermode;

import com.afterdeath.core.Config;
import com.afterdeath.core.AfterdeathCore;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(modid = AfterdeathCore.MODID, value = Dist.CLIENT)
public final class SpiritFlightHud {

    public static final ResourceLocation LAYER_ID =
            ResourceLocation.fromNamespaceAndPath(AfterdeathCore.MODID, "spirit_stamina");

    private static final int TICKS_PER_PORTION = 20;
    private static final int SLOT_SIZE = 11;
    private static final int SLOT_STEP = 10;

    public static final ResourceLocation STAMINA_YES_TEX =
            ResourceLocation.fromNamespaceAndPath(AfterdeathCore.MODID, "textures/gui/stamina_yes.png");
    public static final ResourceLocation STAMINA_NO_TEX =
            ResourceLocation.fromNamespaceAndPath(AfterdeathCore.MODID, "textures/gui/stamina_no.png");


    private SpiritFlightHud() {}

    @SubscribeEvent
    public static void register(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.FOOD_LEVEL, LAYER_ID, SpiritFlightHud::render);
    }

    @SubscribeEvent
    public static void hideFood(RenderGuiLayerEvent.Pre event) {
        if (!VanillaGuiLayers.FOOD_LEVEL.equals(event.getName())) return;
        if (shouldShowStamina()) event.setCanceled(true);
    }

    private static boolean shouldShowStamina() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return false;
        return player.getData(PlayerModeAttachments.PLAYER_MODE) == PlayerMode.SOUL
                && player.getData(PlayerModeAttachments.SOUL_FLIGHT_UNLOCKED);
    }

    private static void render(GuiGraphics graphics, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.options.hideGui) return;
        if (player.isSpectator() || player.getAbilities().instabuild) return;
        if (!shouldShowStamina()) return;

        int current = player.getData(PlayerModeAttachments.SPIRIT_FLIGHT_TICKS);
        int max = Math.max(1, Config.SPIRIT_FLIGHT_MAX_TICKS.get());
        int slotCount = Math.max(1, (max + TICKS_PER_PORTION - 1) / TICKS_PER_PORTION);
        int filled = Math.clamp(current / TICKS_PER_PORTION, 0, slotCount);

        int screenW = graphics.guiWidth();
        int screenH = graphics.guiHeight();
        int right = screenW / 2 + 91;
        int y = screenH - 40;
        if (player.getArmorValue() > 0) y -= 10;

        for (int m = 0; m < slotCount; m++) {
            int x = right - SLOT_SIZE - m * SLOT_STEP;
            drawSlot(graphics, x, y, m < filled);
        }
    }

    private static void drawSlot(GuiGraphics graphics, int x, int y, boolean filled) {
        graphics.blit(filled ? STAMINA_YES_TEX : STAMINA_NO_TEX, x, y, 11, 11, 11,11, 11, 11);
    }
}

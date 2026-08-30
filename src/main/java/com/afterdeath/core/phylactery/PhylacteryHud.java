package com.afterdeath.core.phylactery;

import com.afterdeath.core.AfterdeathCore;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(modid = AfterdeathCore.MODID, value = Dist.CLIENT)
public final class PhylacteryHud {

    public static final ResourceLocation LAYER_ID =
            ResourceLocation.fromNamespaceAndPath(AfterdeathCore.MODID, "phylactery_charge");

    public static final ResourceLocation GUI_TEXTURE_STAGE0 = ResourceLocation.fromNamespaceAndPath(AfterdeathCore.MODID, "textures/gui/phylactery_stage0.png");
    public static final ResourceLocation GUI_TEXTURE_STAGE1 = ResourceLocation.fromNamespaceAndPath(AfterdeathCore.MODID, "textures/gui/phylactery_stage1.png");
    public static final ResourceLocation GUI_TEXTURE_STAGE2 = ResourceLocation.fromNamespaceAndPath(AfterdeathCore.MODID, "textures/gui/phylactery_stage2.png");

    private PhylacteryHud() {}

    @SubscribeEvent
    public static void register(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.HOTBAR, LAYER_ID, PhylacteryHud::render);
    }

    private static void render(GuiGraphics graphics, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.options.hideGui) return;

        ItemStack phylactery = findPhylactery(player);
        if (phylactery.isEmpty()) return;

        final int winWidth = graphics.guiWidth();
        final int winHeight = graphics.guiHeight();


        final int stage = PhylacteryItem.getCharge(phylactery) / PhylacteryItem.chargePerUse();
        final ResourceLocation tex = switch (stage) {
            case 0 -> GUI_TEXTURE_STAGE0;
            case 1 -> GUI_TEXTURE_STAGE1;
            default -> GUI_TEXTURE_STAGE2;
        };
            graphics.blit(tex, winWidth / 2 - 8, winHeight - 55, 16, 16, 16, 16, 16, 16);
    }

    private static ItemStack findPhylactery(LocalPlayer player) {
        ItemStack main = player.getMainHandItem();
        if (main.getItem() instanceof PhylacteryItem) return main;
        ItemStack off = player.getOffhandItem();
        if (off.getItem() instanceof PhylacteryItem) return off;

        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.getItem() instanceof PhylacteryItem) return stack;
        }
        return ItemStack.EMPTY;
    }
}

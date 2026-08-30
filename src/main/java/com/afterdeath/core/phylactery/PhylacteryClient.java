package com.afterdeath.core.phylactery;

import com.afterdeath.core.AfterdeathCore;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = AfterdeathCore.MODID, value = Dist.CLIENT)
public final class PhylacteryClient {

    public static final ResourceLocation CHARGE_LEVEL =
            ResourceLocation.fromNamespaceAndPath(AfterdeathCore.MODID, "charge_level");

    private PhylacteryClient() {}

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> ItemProperties.register(
                AfterdeathCore.PHYLACTERY.get(),
                CHARGE_LEVEL,
                (stack, level, entity, seed) -> {
                    int charge = PhylacteryItem.getCharge(stack);
                    int max = PhylacteryItem.maxCharge();
                    if (charge <= 0) return 0.0F;
                    if (charge >= max) return 1.0F;
                    return 0.5F;
                }
        ));
    }
}

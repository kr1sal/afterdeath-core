package com.afterdeath.core.storycompass;

import com.afterdeath.core.AfterdeathCore;
import net.minecraft.client.renderer.item.CompassItemPropertyFunction;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.component.LodestoneTracker;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = AfterdeathCore.MODID, value = Dist.CLIENT)
public final class StoryCompassClient {

    private StoryCompassClient() {}

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> ItemProperties.register(
                AfterdeathCore.STORY_COMPASS_FIRST_BOSS.get(),
                ResourceLocation.withDefaultNamespace("angle"),
                new CompassItemPropertyFunction((level, stack, entity) -> {
                    LodestoneTracker tracker = stack.get(DataComponents.LODESTONE_TRACKER);
                    return tracker != null ? tracker.target().orElse(null) : null;
                })
        ));
        event.enqueueWork(() -> ItemProperties.register(
                AfterdeathCore.STORY_COMPASS_SECOND_BOSS.get(),
                ResourceLocation.withDefaultNamespace("angle"),
                new CompassItemPropertyFunction((level, stack, entity) -> {
                    LodestoneTracker tracker = stack.get(DataComponents.LODESTONE_TRACKER);
                    return tracker != null ? tracker.target().orElse(null) : null;
                })
        ));
        event.enqueueWork(() -> ItemProperties.register(
                AfterdeathCore.STORY_COMPASS_THIRD_BOSS.get(),
                ResourceLocation.withDefaultNamespace("angle"),
                new CompassItemPropertyFunction((level, stack, entity) -> {
                    LodestoneTracker tracker = stack.get(DataComponents.LODESTONE_TRACKER);
                    return tracker != null ? tracker.target().orElse(null) : null;
                })
        ));
        event.enqueueWork(() -> ItemProperties.register(
                AfterdeathCore.STORY_COMPASS_PLAYER_BASE.get(),
                ResourceLocation.withDefaultNamespace("angle"),
                new CompassItemPropertyFunction((level, stack, entity) -> {
                    LodestoneTracker tracker = stack.get(DataComponents.LODESTONE_TRACKER);
                    return tracker != null ? tracker.target().orElse(null) : null;
                })
        ));
        event.enqueueWork(() -> ItemProperties.register(
                AfterdeathCore.STORY_COMPASS_FINAL_BOSS.get(),
                ResourceLocation.withDefaultNamespace("angle"),
                new CompassItemPropertyFunction((level, stack, entity) -> {
                    LodestoneTracker tracker = stack.get(DataComponents.LODESTONE_TRACKER);
                    return tracker != null ? tracker.target().orElse(null) : null;
                })
        ));
    }
}

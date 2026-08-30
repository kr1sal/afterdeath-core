package com.afterdeath.core;

import com.afterdeath.core.phylactery.PhylacteryComponents;
import com.afterdeath.core.phylactery.PhylacteryItem;
import com.afterdeath.core.playermode.PlayerModeAttachments;
import com.afterdeath.core.storycompass.StoryCompassItem;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(AfterdeathCore.MODID)
public class AfterdeathCore {
    public static final String MODID = "afterdeath_core";

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final DeferredItem<PhylacteryItem> PHYLACTERY = ITEMS.register("phylactery",
            () -> new PhylacteryItem(new Item.Properties()));

    public static final DeferredItem<StoryCompassItem> STORY_COMPASS_FIRST_BOSS = ITEMS.register("story_compass_first_boss",
            () -> new StoryCompassItem(new Item.Properties(),
                    ResourceLocation.fromNamespaceAndPath("kubejs", "first_boss"),
                    "structure.afterdeath_core.first_boss"));

    public static final DeferredItem<StoryCompassItem> STORY_COMPASS_SECOND_BOSS = ITEMS.register("story_compass_second_boss",
            () -> new StoryCompassItem(new Item.Properties(),
                    ResourceLocation.fromNamespaceAndPath("kubejs", "second_boss"),
                    "structure.afterdeath_core.second_boss"));

    public static final DeferredItem<StoryCompassItem> STORY_COMPASS_THIRD_BOSS = ITEMS.register("story_compass_third_boss",
            () -> new StoryCompassItem(new Item.Properties(),
                    ResourceLocation.fromNamespaceAndPath("kubejs", "third_boss"),
                    "structure.afterdeath_core.third_boss"));

    public static final DeferredItem<StoryCompassItem> STORY_COMPASS_PLAYER_BASE = ITEMS.register("story_compass_player_base",
            () -> new StoryCompassItem(new Item.Properties(),
                    ResourceLocation.fromNamespaceAndPath("kubejs", "player_base"),
                    "structure.afterdeath_core.player_base"));

    public static final DeferredItem<StoryCompassItem> STORY_COMPASS_FINAL_BOSS = ITEMS.register("story_compass_final_boss",
            () -> new StoryCompassItem(new Item.Properties(),
                    ResourceLocation.fromNamespaceAndPath("kubejs", "final_boss"),
                    "structure.afterdeath_core.final_boss"));

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> EXAMPLE_TAB =
            CREATIVE_MODE_TABS.register("example_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.afterdeath_core"))
                    .withTabsBefore(CreativeModeTabs.COMBAT)
                    .icon(() -> PHYLACTERY.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(PHYLACTERY.get());
                        output.accept(STORY_COMPASS_FIRST_BOSS.get());
                        output.accept(STORY_COMPASS_SECOND_BOSS.get());
                        output.accept(STORY_COMPASS_THIRD_BOSS.get());
                        output.accept(STORY_COMPASS_PLAYER_BASE.get());
                        output.accept(STORY_COMPASS_FINAL_BOSS.get());
                    }).build());

    public AfterdeathCore(IEventBus modEventBus, ModContainer modContainer) {
        ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
        PlayerModeAttachments.register(modEventBus);
        PhylacteryComponents.register(modEventBus);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        }
    }
}

package com.afterdeath.core.storycompass;

import com.afterdeath.core.Config;
import com.mojang.datafixers.util.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.CompassItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.LodestoneTracker;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.Structure;

import java.util.List;
import java.util.Optional;

public class StoryCompassItem extends CompassItem {
    final ResourceLocation location;
    final String translatableKey;

    public StoryCompassItem(Properties props, ResourceLocation location, String translatableKey) {
        super(props.stacksTo(1).rarity(Rarity.RARE));
        this.location = location;
        this.translatableKey = translatableKey;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        int interval = Config.STORY_COMPASS_INTERVAL_TICKS.get();
        if (interval <= 0 || level.getGameTime() % interval != 0) return;

        BlockPos playerPos = entity.blockPosition();
        double refreshSq = Config.STORY_COMPASS_REFRESH_DIST.get();
        refreshSq *= refreshSq;

        LodestoneTracker current = stack.get(DataComponents.LODESTONE_TRACKER);
        if (current != null && current.target().isPresent()) {
            GlobalPos gp = current.target().get();
            if (gp.dimension().equals(level.dimension())
                    && gp.pos().distSqr(playerPos) < refreshSq * 4.0D) {
                return;
            }
        }

        BlockPos found = findNearestStructure(serverLevel, playerPos);
        if (found != null) {
            GlobalPos gp = GlobalPos.of(level.dimension(), found);
            stack.set(DataComponents.LODESTONE_TRACKER, new LodestoneTracker(Optional.of(gp), false));
        }
    }

    private BlockPos findNearestStructure(ServerLevel level, BlockPos origin) {
        Registry<Structure> registry = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
        ResourceKey<Structure> key = ResourceKey.create(Registries.STRUCTURE, this.location);
        Optional<Holder.Reference<Structure>> holder = registry.getHolder(key);
        if (holder.isEmpty()) return null;

        HolderSet<Structure> set = HolderSet.direct(holder.get());
        int radius = Config.STORY_COMPASS_RADIUS_CHUNKS.get();
        Pair<BlockPos, Holder<Structure>> result = level.getChunkSource().getGenerator()
                .findNearestMapStructure(level, set, origin, radius, false);
        return result != null ? result.getFirst() : null;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        LodestoneTracker t = stack.get(DataComponents.LODESTONE_TRACKER);
        return t != null && t.target().isPresent();
    }

    // Vanilla CompassItem.getDescriptionId(stack) swaps to
    // "item.minecraft.lodestone_compass" as soon as a LodestoneTracker is
    // attached. Override it so the per-target name (e.g. "Story Compass:
    // First Boss") stays after the structure is found.
    @Override
    public String getDescriptionId(ItemStack stack) {
        return this.getDescriptionId();
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tooltip, TooltipFlag flag) {
        Component targetName = Component.translatable(this.translatableKey);
        tooltip.add(Component.translatable("item.afterdeath_core.story_compass.target", targetName)
                .withStyle(ChatFormatting.GRAY));

        LodestoneTracker t = stack.get(DataComponents.LODESTONE_TRACKER);
        if (t != null && t.target().isPresent()) {
            BlockPos p = t.target().get().pos();
            tooltip.add(Component.translatable("item.afterdeath_core.story_compass.found",
                    p.getX(), p.getY(), p.getZ()).withStyle(ChatFormatting.DARK_GRAY));
        } else {
            tooltip.add(Component.translatable("item.afterdeath_core.story_compass.searching")
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}

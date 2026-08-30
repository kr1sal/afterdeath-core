package com.afterdeath.core.phylactery;

import com.afterdeath.core.Config;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.portal.PortalShape;

import java.util.List;
import java.util.Optional;

public class PhylacteryItem extends Item {

    public PhylacteryItem(Properties props) {
        super(props.stacksTo(1).rarity(Rarity.EPIC).fireResistant());
    }

    public static int maxCharge() {
        return Config.PHYLACTERY_MAX_CHARGE.get();
    }

    public static int ticksPerChargeUnit() {
        return Config.PHYLACTERY_TICKS_PER_UNIT.get();
    }

    public static int chargePerUse() {
        return Config.PHYLACTERY_CHARGE_PER_USE.get();
    }

    public static int portalChargeCost() {
        return Config.PHYLACTERY_PORTAL_CHARGE_COST.get();
    }

    public static int getCharge(ItemStack stack) {
        Integer c = stack.get(PhylacteryComponents.CHARGE.get());
        return c == null ? maxCharge() : c;
    }

    public static void setCharge(ItemStack stack, int charge) {
        int clamped = Math.clamp(charge, 0, maxCharge());
        stack.set(PhylacteryComponents.CHARGE.get(), clamped);
    }

    public static boolean isCharged(ItemStack stack) {
        return getCharge(stack) >= chargePerUse();
    }

    public static float chargeFraction(ItemStack stack) {
        int max = maxCharge();
        return max <= 0 ? 0f : (float) getCharge(stack) / (float) max;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return isCharged(stack);
    }

    // Right-click obsidian to ignite a Nether portal, at the cost of stored charge.
    // Vanilla ignition sources (flint & steel, fire charges, lightning-lit fire) are blocked
    // via PortalSpawnEvent in PhylacteryEvents, so this is the only way to light a portal.
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();
        BlockState clickedState = level.getBlockState(clickedPos);
        if (!clickedState.is(Blocks.OBSIDIAN)) {
            return InteractionResult.PASS;
        }

        BlockPos framePos = clickedPos.relative(context.getClickedFace());
        if (!level.getBlockState(framePos).isAir()) {
            return InteractionResult.PASS;
        }

        ItemStack stack = context.getItemInHand();
        int cost = portalChargeCost();
        if (getCharge(stack) < cost) {
            return InteractionResult.FAIL;
        }

        Optional<PortalShape> shape = PortalShape.findEmptyPortalShape(level, framePos, Direction.Axis.X);
        if (shape.isEmpty()) {
            shape = PortalShape.findEmptyPortalShape(level, framePos, Direction.Axis.Z);
        }
        if (shape.isEmpty() || !shape.get().isValid()) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide) {
            shape.get().createPortalBlocks();
            setCharge(stack, getCharge(stack) - cost);
            Player player = context.getPlayer();
            level.playSound(null, framePos,
                    SoundEvents.PORTAL_TRIGGER, SoundSource.BLOCKS,
                    1.0F, level.getRandom().nextFloat() * 0.4F + 0.8F);
            level.gameEvent(player, GameEvent.BLOCK_PLACE, framePos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        int c = getCharge(stack);
        int max = maxCharge();
        ChatFormatting color = c >= max ? ChatFormatting.GOLD : ChatFormatting.LIGHT_PURPLE;
        tooltip.add(Component.translatable("item.afterdeath_core.phylactery.tooltip", c, max).withStyle(color));
        tooltip.add(Component.translatable("item.afterdeath_core.phylactery.desc").withStyle(ChatFormatting.DARK_GRAY));
    }
}

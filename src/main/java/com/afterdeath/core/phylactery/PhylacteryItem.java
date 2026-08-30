package com.afterdeath.core.phylactery;

import com.afterdeath.core.Config;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

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

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        int c = getCharge(stack);
        int max = maxCharge();
        ChatFormatting color = c >= max ? ChatFormatting.GOLD : ChatFormatting.LIGHT_PURPLE;
        tooltip.add(Component.translatable("item.AfterdeathCore.phylactery.tooltip", c, max).withStyle(color));
        tooltip.add(Component.translatable("item.AfterdeathCore.phylactery.desc").withStyle(ChatFormatting.DARK_GRAY));
    }
}

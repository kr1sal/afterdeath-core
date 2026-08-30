package com.afterdeath.core.phylactery;

import com.afterdeath.core.Config;
import com.afterdeath.core.AfterdeathCore;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = AfterdeathCore.MODID)
public final class PhylacteryEvents {

    private PhylacteryEvents() {}

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;
        int ticksPer = PhylacteryItem.ticksPerChargeUnit();
        if (ticksPer <= 0 || player.tickCount % ticksPer != 0) return;

        int max = PhylacteryItem.maxCharge();
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.getItem() instanceof PhylacteryItem) {
                int cur = PhylacteryItem.getCharge(stack);
                if (cur < max) {
                    PhylacteryItem.setCharge(stack, cur + 1);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide) return;

        float amount = event.getAmount();
        if (amount < player.getHealth()) return;

        ItemStack charged = findChargedPhylactery(player);
        if (charged.isEmpty()) return;

        PhylacteryItem.setCharge(charged, PhylacteryItem.getCharge(charged) - PhylacteryItem.chargePerUse());

        player.setHealth((float) (double) Config.PHYLACTERY_HEAL_AMOUNT.get());
        player.removeAllEffects();
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, Config.PHYLACTERY_REGEN_TICKS.get(), 1));
        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, Config.PHYLACTERY_ABSORPTION_TICKS.get(), 1));
        player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, Config.PHYLACTERY_FIRE_RES_TICKS.get(), 0));

        player.level().broadcastEntityEvent(player, (byte) 35);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.0F, 1.0F);

        event.setCanceled(true);
    }

    // Ignition of a Nether portal is reserved for the phylactery. Every vanilla path
    // (flint & steel, fire charges, dispensers, lightning-set fires, dragon breath, mod
    // items that place fire) funnels through BaseFireBlock.onPlace -> PortalSpawnEvent.
    // The phylactery instead calls PortalShape.createPortalBlocks directly and does not
    // trigger this event, so a blanket cancel here leaves it as the sole ignition source.
    @SubscribeEvent
    public static void onPortalSpawn(BlockEvent.PortalSpawnEvent event) {
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onItemToss(ItemTossEvent event) {
        if (!Config.PHYLACTERY_EMPTY_ON_TOSS.get()) return;
        ItemEntity ent = event.getEntity();
        ItemStack stack = ent.getItem();
        if (stack.getItem() instanceof PhylacteryItem) {
            PhylacteryItem.setCharge(stack, 0);
        }
    }

    @SubscribeEvent
    public static void onPlayerDeathDrops(LivingDropsEvent event) {
        if (!Config.PHYLACTERY_EMPTY_ON_DEATH.get()) return;
        if (!(event.getEntity() instanceof Player)) return;
        event.getDrops().forEach(drop -> {
            ItemStack stack = drop.getItem();
            if (stack.getItem() instanceof PhylacteryItem) {
                PhylacteryItem.setCharge(stack, 0);
            }
        });
    }

    private static ItemStack findChargedPhylactery(Player player) {
        ItemStack main = player.getMainHandItem();
        if (main.getItem() instanceof PhylacteryItem && PhylacteryItem.isCharged(main)) return main;
        ItemStack off = player.getOffhandItem();
        if (off.getItem() instanceof PhylacteryItem && PhylacteryItem.isCharged(off)) return off;

        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.getItem() instanceof PhylacteryItem && PhylacteryItem.isCharged(stack)) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }
}

package com.afterdeath.core.playermode;

import com.afterdeath.core.Config;
import com.afterdeath.core.AfterdeathCore;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = AfterdeathCore.MODID)
public final class SpiritFlightEvents {

    private static final ResourceLocation FLIGHT_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath(AfterdeathCore.MODID, "spirit_flight");

    // Tags granted by afterdeath_skills soul-tree skills.
    private static final String TAG_SOUL_FLIGHT = "afterdeath.soul_flight";
    private static final String TAG_FLIGHT_STAMINA_PLUS = "afterdeath.flight_stamina_plus";

    // Extra flight ticks granted when the stamina skill is purchased.
    private static final int FLIGHT_STAMINA_BONUS_TICKS = 100;

    private static final float VANILLA_FLY_SPEED = 0.05F;
    private static final float EPS = 1.0E-6F;

    private SpiritFlightEvents() {}

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        Abilities abilities = player.getAbilities();
        if (abilities.instabuild || player.isSpectator()) return;

        PlayerMode mode = player.getData(PlayerModeAttachments.PLAYER_MODE);
        if (mode == PlayerMode.SOUL && serverPlayer.getTags().contains(TAG_SOUL_FLIGHT)) {
            tickSoul(serverPlayer, abilities);
        } else {
            tickMortal(serverPlayer, abilities);
        }
    }

    static int maxFlightTicks(Player player) {
        int max = Config.SPIRIT_FLIGHT_MAX_TICKS.get();
        if (player.getTags().contains(TAG_FLIGHT_STAMINA_PLUS)) {
            max += FLIGHT_STAMINA_BONUS_TICKS;
        }
        return max;
    }

    private static void tickSoul(ServerPlayer player, Abilities abilities) {
        int max = maxFlightTicks(player);
        int regenPer = Config.SPIRIT_FLIGHT_REGEN_TICKS_PER_UNIT.get();
        float flySpeed = Config.SPIRIT_FLIGHT_SPEED.get().floatValue();

        int remaining = player.getData(PlayerModeAttachments.SPIRIT_FLIGHT_TICKS);
        boolean abilitiesChanged = false;

        if (Math.abs(abilities.getFlyingSpeed() - flySpeed) > EPS) {
            abilities.setFlyingSpeed(flySpeed);
            abilitiesChanged = true;
        }

        if (abilities.flying) {
            if (remaining > 0) {
                remaining--;
                player.setData(PlayerModeAttachments.SPIRIT_FLIGHT_TICKS, remaining);
            }
            if (remaining <= 0) {
                abilities.flying = false;
                abilitiesChanged = true;
            }
        } else {
            boolean grounded = player.onGround() || player.isInWater() || player.onClimbable();
            if (grounded && remaining < max && regenPer > 0 && player.tickCount % regenPer == 0) {
                remaining = Math.min(max, remaining + 1);
                player.setData(PlayerModeAttachments.SPIRIT_FLIGHT_TICKS, remaining);
            }
        }

        setFlightAllowed(player, remaining > 0);

        if (abilitiesChanged) {
            player.onUpdateAbilities();
        }
    }

    private static void tickMortal(ServerPlayer player, Abilities abilities) {
        boolean abilitiesChanged = false;
        if (abilities.flying) {
            abilities.flying = false;
            abilitiesChanged = true;
        }
        if (Math.abs(abilities.getFlyingSpeed() - VANILLA_FLY_SPEED) > EPS) {
            abilities.setFlyingSpeed(VANILLA_FLY_SPEED);
            abilitiesChanged = true;
        }

        setFlightAllowed(player, false);

        if (abilitiesChanged) {
            player.onUpdateAbilities();
        }
    }

    private static void setFlightAllowed(Player player, boolean allowed) {
        AttributeInstance attr = player.getAttribute(NeoForgeMod.CREATIVE_FLIGHT);
        if (attr == null) return;

        if (allowed) {
            if (attr.getModifier(FLIGHT_MODIFIER_ID) == null) {
                attr.addOrUpdateTransientModifier(new AttributeModifier(
                        FLIGHT_MODIFIER_ID, 1.0D, AttributeModifier.Operation.ADD_VALUE));
            }
        } else if (attr.getModifier(FLIGHT_MODIFIER_ID) != null) {
            attr.removeModifier(FLIGHT_MODIFIER_ID);
        }
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        refill(event.getEntity());
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        refill(event.getEntity());
    }

    @SubscribeEvent
    public static void onChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        refill(event.getEntity());
    }

    private static void refill(Player player) {
        if (player.level().isClientSide) return;
        int max = maxFlightTicks(player);
        int cur = player.getData(PlayerModeAttachments.SPIRIT_FLIGHT_TICKS);
        if (cur < max) {
            player.setData(PlayerModeAttachments.SPIRIT_FLIGHT_TICKS, max);
        }
    }
}

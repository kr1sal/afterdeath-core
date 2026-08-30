package com.afterdeath.core.playermode;

import com.afterdeath.core.AfterdeathCore;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = AfterdeathCore.MODID)
public final class AbilityNetwork {

    private AbilityNetwork() {}

    @SubscribeEvent
    public static void onRegister(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(
                UseAbilityPayload.TYPE,
                UseAbilityPayload.STREAM_CODEC,
                AbilityNetwork::handleUseAbility);
    }

    private static void handleUseAbility(UseAbilityPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            AbilityResult result = switch (payload.name()) {
                case "summon_vex"      -> AfterdeathAbilities.trySummonVex(player);
                case "summon_skeleton" -> AfterdeathAbilities.trySummonSkeleton(player);
                case "damage_burst"    -> AfterdeathAbilities.tryDamageBurst(player);
                case "dash"            -> AfterdeathAbilities.tryDash(player);
                default -> null;
            };
            if (result != null) result.sendTo(player);
        });
    }
}

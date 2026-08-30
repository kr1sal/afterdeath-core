package com.afterdeath.core.playermode;

import com.afterdeath.core.AfterdeathCore;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = AfterdeathCore.MODID)
public final class AbilityEvents {

    private AbilityEvents() {}

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        AfterdeathAbilities.tickBurstExpiry(player);
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        AfterdeathAbilities.clearPlayer(event.getEntity().getUUID());
    }
}

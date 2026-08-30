package com.afterdeath.core.playermode;

import com.afterdeath.core.AfterdeathCore;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

// Client → server: the player pressed a keybind and wants to use the named ability.
public record UseAbilityPayload(String name) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<UseAbilityPayload> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath(AfterdeathCore.MODID, "use_ability"));

    public static final StreamCodec<FriendlyByteBuf, UseAbilityPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8, UseAbilityPayload::name,
                    UseAbilityPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

package com.afterdeath.core.playermode;

import com.afterdeath.core.AfterdeathCore;
import com.mojang.serialization.Codec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public final class PlayerModeAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, AfterdeathCore.MODID);

    private static final StreamCodec<io.netty.buffer.ByteBuf, PlayerMode> PLAYER_MODE_STREAM_CODEC =
            ByteBufCodecs.VAR_INT.map(i -> PlayerMode.values()[i], PlayerMode::ordinal);

    public static final Supplier<AttachmentType<PlayerMode>> PLAYER_MODE = ATTACHMENT_TYPES.register(
            "player_mode",
            () -> AttachmentType.builder(() -> PlayerMode.SOUL)
                    .serialize(PlayerMode.CODEC)
                    .sync(PLAYER_MODE_STREAM_CODEC)
                    .copyOnDeath()
                    .build()
    );

    public static final Supplier<AttachmentType<AngerData>> ANGER = ATTACHMENT_TYPES.register(
            "anger",
            () -> AttachmentType.builder(() -> AngerData.NONE)
                    .serialize(AngerData.CODEC)
                    .build()
    );

    public static final Supplier<AttachmentType<Integer>> SPIRIT_FLIGHT_TICKS = ATTACHMENT_TYPES.register(
            "spirit_flight_ticks",
            () -> AttachmentType.<Integer>builder(() -> 0)
                    .serialize(Codec.INT)
                    .sync(ByteBufCodecs.VAR_INT)
                    .copyOnDeath()
                    .build()
    );

    public static void register(IEventBus modBus) {
        ATTACHMENT_TYPES.register(modBus);
    }

    private PlayerModeAttachments() {}
}

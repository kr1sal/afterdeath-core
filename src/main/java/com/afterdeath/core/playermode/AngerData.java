package com.afterdeath.core.playermode;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public record AngerData(@Nullable UUID target, long untilTick) {

    public static final AngerData NONE = new AngerData(null, 0L);

    public static final Codec<AngerData> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            UUIDUtil.CODEC.optionalFieldOf("target").forGetter(a -> Optional.ofNullable(a.target())),
            Codec.LONG.fieldOf("until_tick").forGetter(AngerData::untilTick)
    ).apply(inst, (opt, tick) -> new AngerData(opt.orElse(null), tick)));

    public boolean isAngryAt(UUID player, long nowTick) {
        return target != null && target.equals(player) && untilTick > nowTick;
    }
}

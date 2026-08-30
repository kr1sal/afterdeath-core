package com.afterdeath.core.playermode;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum PlayerMode implements StringRepresentable {
    HUMAN("human"),
    SOUL("soul"),
    SKELETON("skeleton");

    public static final Codec<PlayerMode> CODEC = StringRepresentable.fromEnum(PlayerMode::values);

    private final String name;

    PlayerMode(String name) {
        this.name = name;
    }

    @Override
    public @NotNull String getSerializedName() {
        return name;
    }

    public boolean isHostileTarget() {
        return this == HUMAN;
    }
}

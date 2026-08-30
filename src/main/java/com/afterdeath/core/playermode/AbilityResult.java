package com.afterdeath.core.playermode;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

// Outcome of an ability attempt. Callers use sendTo() to tell the player
// exactly why nothing happened (or that it did).
public sealed interface AbilityResult {

    Success SUCCESS = new Success();
    NoAbility NO_ABILITY = new NoAbility();
    WrongMode WRONG_MODE = new WrongMode();
    SpawnFailed SPAWN_FAILED = new SpawnFailed();

    record Success() implements AbilityResult {}
    record NoAbility() implements AbilityResult {}
    record WrongMode() implements AbilityResult {}
    record OnCooldown(int remainingTicks) implements AbilityResult {}
    record SpawnFailed() implements AbilityResult {}

    default boolean ok() { return this instanceof Success; }

    default void sendTo(ServerPlayer player) {
        Component msg = switch (this) {
            case Success ignored -> null;
            case NoAbility ignored -> Component.translatable(
                    "ability.afterdeath_core.no_ability").withStyle(ChatFormatting.GRAY);
            case WrongMode ignored -> Component.translatable(
                    "ability.afterdeath_core.wrong_mode").withStyle(ChatFormatting.GRAY);
            case OnCooldown c -> Component.translatable(
                    "ability.afterdeath_core.cooldown",
                    Math.max(1, (c.remainingTicks() + 19) / 20)).withStyle(ChatFormatting.YELLOW);
            case SpawnFailed ignored -> Component.translatable(
                    "ability.afterdeath_core.spawn_failed").withStyle(ChatFormatting.RED);
        };
        if (msg != null) player.sendSystemMessage(msg);
    }
}

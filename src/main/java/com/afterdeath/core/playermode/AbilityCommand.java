package com.afterdeath.core.playermode;

import com.afterdeath.core.AfterdeathCore;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.List;

// /afterdeath ability <name>
// Player-runnable (perm 0) — meant to be bound to a keybind on the client, but
// works from chat too so it can be tested without any client wiring.
@EventBusSubscriber(modid = AfterdeathCore.MODID)
public final class AbilityCommand {

    private static final List<String> ABILITIES = List.of(
            "summon_vex",
            "summon_skeleton",
            "damage_burst",
            "dash"
    );

    private static final SuggestionProvider<CommandSourceStack> ABILITY_SUGGESTIONS =
            (ctx, builder) -> SharedSuggestionProvider.suggest(ABILITIES, builder);

    private AbilityCommand() {}

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("afterdeath")
                        .then(Commands.literal("ability")
                                .then(Commands.argument("name", StringArgumentType.word())
                                        .suggests(ABILITY_SUGGESTIONS)
                                        .executes(ctx -> {
                                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                                            String name = StringArgumentType.getString(ctx, "name");
                                            AbilityResult result = switch (name) {
                                                case "summon_vex" ->
                                                        AfterdeathAbilities.trySummonVex(player);
                                                case "summon_skeleton" ->
                                                        AfterdeathAbilities.trySummonSkeleton(player);
                                                case "damage_burst" ->
                                                        AfterdeathAbilities.tryDamageBurst(player);
                                                case "dash" ->
                                                        AfterdeathAbilities.tryDash(player);
                                                default -> null;
                                            };
                                            if (result == null) {
                                                ctx.getSource().sendFailure(Component.literal(
                                                        "Unknown ability: " + name));
                                                return 0;
                                            }
                                            result.sendTo(player);
                                            return result.ok() ? 1 : 0;
                                        })
                                )
                        )
        );
    }
}

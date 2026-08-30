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

import java.util.Arrays;

@EventBusSubscriber(modid = AfterdeathCore.MODID)
public final class PlayerModeCommand {

    private static final SuggestionProvider<CommandSourceStack> MODE_SUGGESTIONS =
            (ctx, builder) -> SharedSuggestionProvider.suggest(
                    Arrays.stream(PlayerMode.values()).map(PlayerMode::getSerializedName),
                    builder
            );

    private PlayerModeCommand() {}

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("playermode")
                        .requires(src -> src.hasPermission(2))
                        .then(Commands.argument("mode", StringArgumentType.word())
                                .suggests(MODE_SUGGESTIONS)
                                .executes(ctx -> {
                                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                                    String raw = StringArgumentType.getString(ctx, "mode");

                                    PlayerMode mode = Arrays.stream(PlayerMode.values())
                                            .filter(m -> m.getSerializedName().equalsIgnoreCase(raw))
                                            .findFirst()
                                            .orElse(null);

                                    if (mode == null) {
//                                        ctx.getSource().sendFailure(
//                                                Component.translatable("command.AfterdeathCore.playermode.unknown", raw));
                                        return 0;
                                    }
//
//                                    boolean changed = PlayerModeTransform.apply(player, mode);
//                                    Component name = Component.translatable(
//                                            "playermode.AfterdeathCore." + mode.getSerializedName());
//                                    if (changed) {
//                                        ctx.getSource().sendSuccess(
//                                                () -> Component.translatable("command.AfterdeathCore.playermode.changed", name),
//                                                true);
//                                    } else {
//                                        ctx.getSource().sendSuccess(
//                                                () -> Component.translatable("command.AfterdeathCore.playermode.same", name),
//                                                false);
//                                    }
                                    return 1;
                                })
                        )
        );
    }
}

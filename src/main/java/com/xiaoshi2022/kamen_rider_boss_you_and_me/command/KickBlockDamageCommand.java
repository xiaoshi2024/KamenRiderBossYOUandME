package com.xiaoshi2022.kamen_rider_boss_you_and_me.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.KRBVariables;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * 骑士踢方块破坏命令，允许管理员控制骑士踢是否允许破坏方块
 */
public class KickBlockDamageCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("kickblockdamage")
                        .requires(source -> source.hasPermission(2)) // 需要管理员权限
                        .then(
                                Commands.literal("enable")
                                        .then(
                                                Commands.argument("player", EntityArgument.player())
                                                        .executes(context -> setKickBlockDamageStatus(context, true))
                                        )
                                        .executes(context -> setGlobalKickBlockDamageStatus(context, true))
                        )
                        .then(
                                Commands.literal("disable")
                                        .then(
                                                Commands.argument("player", EntityArgument.player())
                                                        .executes(context -> setKickBlockDamageStatus(context, false))
                                        )
                                        .executes(context -> setGlobalKickBlockDamageStatus(context, false))
                        )
                        .then(
                                Commands.literal("check")
                                        .then(
                                                Commands.argument("player", EntityArgument.player())
                                                        .executes(context -> checkKickBlockDamageStatus(context))
                                        )
                                        .executes(context -> checkOwnKickBlockDamageStatus(context))
                        )
        );
    }

    /**
     * 设置单个玩家的骑士踢方块破坏状态
     */
    private static int setKickBlockDamageStatus(CommandContext<CommandSourceStack> context, boolean enable) {
        try {
            ServerPlayer player = EntityArgument.getPlayer(context, "player");
            CommandSourceStack source = context.getSource();

            // 获取玩家变量并设置状态
            KRBVariables.PlayerVariables variables = player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null)
                    .orElse(new KRBVariables.PlayerVariables());
            variables.allowKickBlockDamage = enable;
            variables.syncPlayerVariables(player);

            String action = enable ? "启用" : "禁用";
            source.sendSuccess(
                    () -> Component.literal(action + "了玩家 " + player.getGameProfile().getName() + " 的骑士踢方块破坏功能"),
                    true
            );

            player.sendSystemMessage(
                    Component.literal("管理员已" + action + "你的骑士踢方块破坏功能")
            );

            return Command.SINGLE_SUCCESS;
        } catch (CommandSyntaxException e) {
            context.getSource().sendFailure(Component.literal("设置骑士踢方块破坏状态失败: " + e.getMessage()));
            return 0;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("设置骑士踢方块破坏状态失败: " + e.getMessage()));
            return 0;
        }
    }

    /**
     * 设置全局的骑士踢方块破坏状态（对所有在线玩家）
     */
    private static int setGlobalKickBlockDamageStatus(CommandContext<CommandSourceStack> context, boolean enable) {
        try {
            CommandSourceStack source = context.getSource();
            for (ServerPlayer player : source.getServer().getPlayerList().getPlayers()) {
                // 获取玩家变量并设置状态
                KRBVariables.PlayerVariables variables = player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null)
                        .orElse(new KRBVariables.PlayerVariables());
                variables.allowKickBlockDamage = enable;
                variables.syncPlayerVariables(player);

                player.sendSystemMessage(
                        Component.literal("管理员已" + (enable ? "启用" : "禁用") + "骑士踢方块破坏功能")
                );
            }

            String action = enable ? "启用" : "禁用";
            source.sendSuccess(
                    () -> Component.literal(action + "了所有玩家的骑士踢方块破坏功能"),
                    true
            );

            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("设置全局骑士踢方块破坏状态失败: " + e.getMessage()));
            return 0;
        }
    }

    /**
     * 检查指定玩家的骑士踢方块破坏状态
     */
    private static int checkKickBlockDamageStatus(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer player = EntityArgument.getPlayer(context, "player");
            CommandSourceStack source = context.getSource();

            // 获取玩家变量
            KRBVariables.PlayerVariables variables = player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null)
                    .orElse(new KRBVariables.PlayerVariables());

            source.sendSuccess(
                    () -> Component.literal("玩家 " + player.getGameProfile().getName() + " 的骑士踢方块破坏功能状态: " + 
                            (variables.allowKickBlockDamage ? "已启用" : "已禁用")),
                    false
            );

            return Command.SINGLE_SUCCESS;
        } catch (CommandSyntaxException e) {
            context.getSource().sendFailure(Component.literal("检查骑士踢方块破坏状态失败: " + e.getMessage()));
            return 0;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("检查骑士踢方块破坏状态失败: " + e.getMessage()));
            return 0;
        }
    }

    /**
     * 检查自己的骑士踢方块破坏状态
     */
    private static int checkOwnKickBlockDamageStatus(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer player = context.getSource().getPlayerOrException();

            // 获取玩家变量
            KRBVariables.PlayerVariables variables = player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null)
                    .orElse(new KRBVariables.PlayerVariables());

            player.sendSystemMessage(
                    Component.literal("你的骑士踢方块破坏功能状态: " + 
                            (variables.allowKickBlockDamage ? "已启用" : "已禁用"))
            );

            return Command.SINGLE_SUCCESS;
        } catch (CommandSyntaxException e) {
            context.getSource().sendFailure(Component.literal("检查骑士踢方块破坏状态失败: " + e.getMessage()));
            return 0;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("检查骑士踢方块破坏状态失败: " + e.getMessage()));
            return 0;
        }
    }
}
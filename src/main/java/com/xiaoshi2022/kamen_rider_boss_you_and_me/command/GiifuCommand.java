package com.xiaoshi2022.kamen_rider_boss_you_and_me.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.KRBVariables;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * Giifu命令，允许管理员设置玩家的Giifu状态
 */
public class GiifuCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("giifu")
                        .requires(source -> source.hasPermission(2)) // 需要管理员权限
                        .then(
                                Commands.literal("set")
                                        .then(
                                                Commands.argument("player", EntityArgument.player())
                                                        .executes(context -> setGiifuStatus(context, true))
                                        )
                        )
                        .then(
                                Commands.literal("remove")
                                        .then(
                                                Commands.argument("player", EntityArgument.player())
                                                        .executes(context -> setGiifuStatus(context, false))
                                        )
                        )
                        .then(
                                Commands.literal("check")
                                        .then(
                                                Commands.argument("player", EntityArgument.player())
                                                        .executes(context -> checkGiifuStatus(context))
                                        )
                                        .executes(context -> checkOwnGiifuStatus(context))
                        )
        );
    }

    /**
     * 设置玩家的Giifu状态
     */
    private static int setGiifuStatus(CommandContext<CommandSourceStack> context, boolean isGiifu) {
        try {
            ServerPlayer player = EntityArgument.getPlayer(context, "player");
            
            // 先获取玩家当前的基夫状态
            boolean wasGiifu = player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null)
                    .map(variables -> variables.isGiifu)
                    .orElse(false);
            
            // 设置玩家的Giifu状态
            player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(variables -> {
                variables.isGiifu = isGiifu;
                variables.syncPlayerVariables(player);
            });
            
            // 如果是从非基夫变为基夫，则触发成就
            if (isGiifu && !wasGiifu) {
                // 触发成就：献上感谢，亡命徒
                kamen_rider_boss_you_and_me.GIIFU_TRIGGER.trigger(player);
            }
            
            String action = isGiifu ? "授予" : "移除";
            context.getSource().sendSuccess(
                    () -> Component.literal(action + "了玩家 " + player.getGameProfile().getName() + " 的基夫状态"),
                    true
            );
            
            player.sendSystemMessage(
                    Component.literal("管理员已" + action + "你的基夫状态")
            );
            
            return Command.SINGLE_SUCCESS;
        } catch (CommandSyntaxException e) {
            context.getSource().sendFailure(Component.literal("设置基夫状态失败: " + e.getMessage()));
            return 0;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("设置基夫状态失败: " + e.getMessage()));
            return 0;
        }
    }

    /**
     * 检查指定玩家的Giifu状态
     */
    private static int checkGiifuStatus(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer player = EntityArgument.getPlayer(context, "player");
            
            boolean isGiifu = player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null)
                    .map(variables -> variables.isGiifu)
                    .orElse(false);
            
            String status = isGiifu ? "是" : "不是";
            context.getSource().sendSuccess(
                    () -> Component.literal("玩家 " + player.getGameProfile().getName() + " " + status + " 基夫"),
                    false
            );
            
            return Command.SINGLE_SUCCESS;
        } catch (CommandSyntaxException e) {
            context.getSource().sendFailure(Component.literal("检查基夫状态失败: " + e.getMessage()));
            return 0;
        }
    }

    /**
     * 检查自己的Giifu状态
     */
    private static int checkOwnGiifuStatus(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer player = context.getSource().getPlayerOrException();
            
            boolean isGiifu = player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null)
                    .map(variables -> variables.isGiifu)
                    .orElse(false);
            
            String status = isGiifu ? "你现在是基夫！" : "你不是基夫。";
            context.getSource().sendSuccess(
                    () -> Component.literal(status),
                    false
            );
            
            return Command.SINGLE_SUCCESS;
        } catch (CommandSyntaxException e) {
            context.getSource().sendFailure(Component.literal("检查基夫状态失败: " + e.getMessage()));
            return 0;
        }
    }
}
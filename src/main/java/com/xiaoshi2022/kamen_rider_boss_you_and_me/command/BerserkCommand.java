package com.xiaoshi2022.kamen_rider_boss_you_and_me.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.BerserkSyncPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.BerserkModeManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * 暴走命令，允许玩家或管理员立即触发/停止暴走模式
 */
public class BerserkCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("berserk")
                        .requires(source -> source.hasPermission(2)) // 需要管理员权限
                        .then(
                                Commands.literal("start")
                                        .then(
                                                Commands.argument("player", EntityArgument.player())
                                                        .executes(context -> startBerserkMode(context))
                                        )
                                        .executes(context -> startOwnBerserkMode(context))
                        )
                        .then(
                                Commands.literal("stop")
                                        .then(
                                                Commands.argument("player", EntityArgument.player())
                                                        .executes(context -> stopBerserkMode(context))
                                        )
                                        .executes(context -> stopOwnBerserkMode(context))
                        )
                        .then(
                                Commands.literal("check")
                                        .then(
                                                Commands.argument("player", EntityArgument.player())
                                                        .executes(context -> checkBerserkStatus(context))
                                        )
                                        .executes(context -> checkOwnBerserkStatus(context))
                        )
        );
    }

    /**
     * 启动指定玩家的暴走模式
     */
    private static int startBerserkMode(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer player = EntityArgument.getPlayer(context, "player");
            
            BerserkModeManager.startBerserkModeImmediately(player);
            
            context.getSource().sendSuccess(
                    () -> Component.literal("已立即启动玩家 " + player.getGameProfile().getName() + " 的暴走模式"),
                    true
            );
            
            player.sendSystemMessage(
                    Component.literal("暴走模式已立即启动！你将失去对角色的控制权，角色将自动攻击附近的任何生物。")
            );
            
            return Command.SINGLE_SUCCESS;
        } catch (CommandSyntaxException e) {
            context.getSource().sendFailure(Component.literal("启动暴走模式失败: " + e.getMessage()));
            return 0;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("启动暴走模式失败: " + e.getMessage()));
            return 0;
        }
    }

    /**
     * 启动自己的暴走模式
     */
    private static int startOwnBerserkMode(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer player = context.getSource().getPlayerOrException();
            
            BerserkModeManager.startBerserkModeImmediately(player);
            
            context.getSource().sendSuccess(
                    () -> Component.literal("已立即启动暴走模式"),
                    false
            );
            
            player.sendSystemMessage(
                    Component.literal("暴走模式已立即启动！你将失去对角色的控制权，角色将自动攻击附近的任何生物。")
            );
            
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("启动暴走模式失败: " + e.getMessage()));
            return 0;
        }
    }

    /**
     * 停止指定玩家的暴走模式
     */
    private static int stopBerserkMode(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer player = EntityArgument.getPlayer(context, "player");
            
            BerserkModeManager.stopBerserkMode(player);
            
            context.getSource().sendSuccess(
                    () -> Component.literal("已停止玩家 " + player.getGameProfile().getName() + " 的暴走模式"),
                    true
            );
            
            player.sendSystemMessage(
                    Component.literal("暴走模式已停止，你重新获得了对角色的控制权。")
            );
            
            return Command.SINGLE_SUCCESS;
        } catch (CommandSyntaxException e) {
            context.getSource().sendFailure(Component.literal("停止暴走模式失败: " + e.getMessage()));
            return 0;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("停止暴走模式失败: " + e.getMessage()));
            return 0;
        }
    }

    /**
     * 停止自己的暴走模式
     */
    private static int stopOwnBerserkMode(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer player = context.getSource().getPlayerOrException();
            
            BerserkModeManager.stopBerserkMode(player);
            
            context.getSource().sendSuccess(
                    () -> Component.literal("已停止暴走模式"),
                    false
            );
            
            player.sendSystemMessage(
                    Component.literal("暴走模式已停止，你重新获得了对角色的控制权。")
            );
            
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("停止暴走模式失败: " + e.getMessage()));
            return 0;
        }
    }

    /**
     * 检查指定玩家的暴走状态
     */
    private static int checkBerserkStatus(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer player = EntityArgument.getPlayer(context, "player");
            
            boolean isBerserk = BerserkModeManager.isBerserk(player);
            
            String status = isBerserk ? "暴走中" : "正常";
            context.getSource().sendSuccess(
                    () -> Component.literal("玩家 " + player.getGameProfile().getName() + " " + status),
                    false
            );
            
            return Command.SINGLE_SUCCESS;
        } catch (CommandSyntaxException e) {
            context.getSource().sendFailure(Component.literal("检查暴走状态失败: " + e.getMessage()));
            return 0;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("检查暴走状态失败: " + e.getMessage()));
            return 0;
        }
    }

    /**
     * 检查自己的暴走状态
     */
    private static int checkOwnBerserkStatus(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer player = context.getSource().getPlayerOrException();
            
            boolean isBerserk = BerserkModeManager.isBerserk(player);
            
            String status = isBerserk ? "你正处于暴走模式！" : "你目前处于正常状态。";
            context.getSource().sendSuccess(
                    () -> Component.literal(status),
                    false
            );
            
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("检查暴走状态失败: " + e.getMessage()));
            return 0;
        }
    }
}
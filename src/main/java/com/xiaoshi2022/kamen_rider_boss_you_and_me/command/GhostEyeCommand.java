package com.xiaoshi2022.kamen_rider_boss_you_and_me.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.KRBVariables;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.GhostEyeManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * GhostEye命令，允许管理员设置玩家的眼魔状态
 */
public class GhostEyeCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("ghosteye")
                        .requires(source -> source.hasPermission(2)) // 需要管理员权限
                        .then(
                                Commands.literal("set")
                                        .then(
                                                Commands.argument("player", EntityArgument.player())
                                                        .executes(context -> setGhostEyeStatus(context, true))
                                        )
                        )
                        .then(
                                Commands.literal("remove")
                                        .then(
                                                Commands.argument("player", EntityArgument.player())
                                                        .executes(context -> setGhostEyeStatus(context, false))
                                        )
                        )
                        .then(
                                Commands.literal("check")
                                        .then(
                                                Commands.argument("player", EntityArgument.player())
                                                        .executes(context -> checkGhostEyeStatus(context))
                                        )
                                        .executes(context -> checkOwnGhostEyeStatus(context))
                        )
        );
    }

    /**
     * 设置玩家的眼魔状态
     */
    private static int setGhostEyeStatus(CommandContext<CommandSourceStack> context, boolean isGhostEye) {
        try {
            ServerPlayer player = EntityArgument.getPlayer(context, "player");
            
            if (isGhostEye) {
                // 检查是否是第一次设置眼魔状态
                boolean isFirstTime = !GhostEyeManager.isGhostEye(player);
                // 使用GhostEyeManager设置眼魔状态并添加buff效果
                GhostEyeManager.setGhostEyeState(player, isFirstTime);
            } else {
                // 使用GhostEyeManager移除眼魔状态和buff效果
                GhostEyeManager.removeGhostEyeState(player);
            }
            
            String action = isGhostEye ? "授予" : "移除";
            context.getSource().sendSuccess(
                    () -> Component.literal(action + "了玩家 " + player.getGameProfile().getName() + " 的眼魔状态"),
                    true
            );
            
            player.sendSystemMessage(
                    Component.literal("管理员已" + action + "你的眼魔状态")
            );
            
            return Command.SINGLE_SUCCESS;
        } catch (CommandSyntaxException e) {
            context.getSource().sendFailure(Component.literal("设置眼魔状态失败: " + e.getMessage()));
            return 0;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("设置眼魔状态失败: " + e.getMessage()));
            return 0;
        }
    }

    /**
     * 检查指定玩家的眼魔状态
     */
    private static int checkGhostEyeStatus(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer player = EntityArgument.getPlayer(context, "player");
            
            boolean isGhostEye = player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null)
                    .map(variables -> variables.isGhostEye)
                    .orElse(false);
            
            String status = isGhostEye ? "是" : "不是";
            context.getSource().sendSuccess(
                    () -> Component.literal("玩家 " + player.getGameProfile().getName() + " " + status + " 眼魔"),
                    false
            );
            
            return Command.SINGLE_SUCCESS;
        } catch (CommandSyntaxException e) {
            context.getSource().sendFailure(Component.literal("检查眼魔状态失败: " + e.getMessage()));
            return 0;
        }
    }

    /**
     * 检查自己的眼魔状态
     */
    private static int checkOwnGhostEyeStatus(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer player = context.getSource().getPlayerOrException();
            
            boolean isGhostEye = player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null)
                    .map(variables -> variables.isGhostEye)
                    .orElse(false);
            
            String status = isGhostEye ? "你现在是眼魔!" : "你不是眼魔。";
            context.getSource().sendSuccess(
                    () -> Component.literal(status),
                    false
            );
            
            return Command.SINGLE_SUCCESS;
        } catch (CommandSyntaxException e) {
            context.getSource().sendFailure(Component.literal("检查眼魔状态失败: " + e.getMessage()));
            return 0;
        }
    }
}
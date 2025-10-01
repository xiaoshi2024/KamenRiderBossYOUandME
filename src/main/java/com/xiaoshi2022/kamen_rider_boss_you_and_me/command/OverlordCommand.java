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
 * Overlord命令，允许管理员设置玩家的Overlord状态
 */
public class OverlordCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("overlord")
                        .requires(source -> source.hasPermission(2)) // 需要管理员权限
                        .then(
                                Commands.literal("set")
                                        .then(
                                                Commands.argument("player", EntityArgument.player())
                                                        .executes(context -> setOverlordStatus(context, true))
                                        )
                        )
                        .then(
                                Commands.literal("remove")
                                        .then(
                                                Commands.argument("player", EntityArgument.player())
                                                        .executes(context -> setOverlordStatus(context, false))
                                        )
                        )
                        .then(
                                Commands.literal("check")
                                        .then(
                                                Commands.argument("player", EntityArgument.player())
                                                        .executes(context -> checkOverlordStatus(context))
                                        )
                                        .executes(context -> checkOwnOverlordStatus(context))
                        )
        );
    }

    /**
     * 设置玩家的Overlord状态
     */
    private static int setOverlordStatus(CommandContext<CommandSourceStack> context, boolean isOverlord) {
        try {
            ServerPlayer player = EntityArgument.getPlayer(context, "player");
            
            // 设置玩家的Overlord状态
            player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(variables -> {
                variables.isOverlord = isOverlord;
                variables.syncPlayerVariables(player);
            });
            
            String action = isOverlord ? "授予" : "移除";
            context.getSource().sendSuccess(
                    () -> Component.literal(action + "了玩家 " + player.getGameProfile().getName() + " 的Overlord状态"),
                    true
            );
            
            player.sendSystemMessage(
                    Component.literal("管理员已" + action + "你的Overlord状态")
            );
            
            return Command.SINGLE_SUCCESS;
        } catch (CommandSyntaxException e) {
            context.getSource().sendFailure(Component.literal("设置Overlord状态失败: " + e.getMessage()));
            return 0;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("设置Overlord状态失败: " + e.getMessage()));
            return 0;
        }
    }

    /**
     * 检查指定玩家的Overlord状态
     */
    private static int checkOverlordStatus(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer player = EntityArgument.getPlayer(context, "player");
            
            boolean isOverlord = player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null)
                    .map(variables -> variables.isOverlord)
                    .orElse(false);
            
            String status = isOverlord ? "是" : "不是";
            context.getSource().sendSuccess(
                    () -> Component.literal("玩家 " + player.getGameProfile().getName() + " " + status + " Overlord"),
                    false
            );
            
            return Command.SINGLE_SUCCESS;
        } catch (CommandSyntaxException e) {
            context.getSource().sendFailure(Component.literal("检查Overlord状态失败: " + e.getMessage()));
            return 0;
        }
    }

    /**
     * 检查自己的Overlord状态
     */
    private static int checkOwnOverlordStatus(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer player = context.getSource().getPlayerOrException();
            
            boolean isOverlord = player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null)
                    .map(variables -> variables.isOverlord)
                    .orElse(false);
            
            String status = isOverlord ? "你现在是Overlord!" : "你不是Overlord。";
            context.getSource().sendSuccess(
                    () -> Component.literal(status),
                    false
            );
            
            return Command.SINGLE_SUCCESS;
        } catch (CommandSyntaxException e) {
            context.getSource().sendFailure(Component.literal("检查Overlord状态失败: " + e.getMessage()));
            return 0;
        }
    }
}
package com.xiaoshi2022.kamen_rider_boss_you_and_me.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.advancement.RoidmudeTrigger;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.varibales.KRBVariables;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * Roidmude命令，允许管理员设置玩家的Roidmude状态
 */
public class RoidmudeCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("roidmude")
                        .requires(source -> source.hasPermission(2)) // 需要管理员权限
                        .then(
                                Commands.literal("set")
                                        .then(
                                                Commands.argument("player", EntityArgument.player())
                                                        .executes(context -> setRoidmudeStatus(context, true, "plain", 0))
                                        )
                        )
                        .then(
                                Commands.literal("remove")
                                        .then(
                                                Commands.argument("player", EntityArgument.player())
                                                        .executes(context -> setRoidmudeStatus(context, false, "plain", 0))
                                        )
                        )
                        .then(
                                Commands.literal("evolve")
                                        .then(
                                                Commands.argument("player", EntityArgument.player())
                                                        .executes(context -> evolveRoidmude(context))
                                        )
                        )
                        .then(
                                Commands.literal("check")
                                        .then(
                                                Commands.argument("player", EntityArgument.player())
                                                        .executes(context -> checkRoidmudeStatus(context))
                                        )
                                        .executes(context -> checkOwnRoidmudeStatus(context))
                        )
        );
    }

    /**
     * 设置玩家的Roidmude状态
     */
    private static int setRoidmudeStatus(CommandContext<CommandSourceStack> context, boolean isRoidmude, String type, int number) {
        try {
            ServerPlayer player = EntityArgument.getPlayer(context, "player");
            
            // 设置玩家的Roidmude状态
            player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(variables -> {
                variables.isRoidmude = isRoidmude;
                
                if (isRoidmude) {
                    // 设置类型和编号
                    variables.roidmudeType = type;
                    variables.roidmudeNumber = number;
                    variables.isRoidmudeEvolved = false;
                    
                    // 触发Roidmude成就
                    RoidmudeTrigger.getInstance().trigger(player);
                } else {
                    // 重置Roidmude相关变量
                    variables.roidmudeType = "plain";
                    variables.roidmudeNumber = 0;
                    variables.isRoidmudeEvolved = false;
                }
                
                variables.syncPlayerVariables(player);
            });
            
            String action = isRoidmude ? "授予" : "移除";
            context.getSource().sendSuccess(
                    () -> Component.literal(action + "了玩家 " + player.getGameProfile().getName() + " 的Roidmude状态"),
                    true
            );
            
            player.sendSystemMessage(
                    Component.literal("管理员已" + action + "你的Roidmude状态")
            );
            
            return Command.SINGLE_SUCCESS;
        } catch (CommandSyntaxException e) {
            context.getSource().sendFailure(Component.literal("设置Roidmude状态失败: " + e.getMessage()));
            return 0;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("设置Roidmude状态失败: " + e.getMessage()));
            return 0;
        }
    }

    /**
     * 进化玩家的Roidmude状态
     */
    private static int evolveRoidmude(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer player = EntityArgument.getPlayer(context, "player");
            
            player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(variables -> {
                if (variables.isRoidmude) {
                    variables.isRoidmudeEvolved = true;
                    variables.syncPlayerVariables(player);
                    
                    context.getSource().sendSuccess(
                            () -> Component.literal("玩家 " + player.getGameProfile().getName() + " 的Roidmude已进化"),
                            true
                    );
                    
                    player.sendSystemMessage(
                            Component.literal("你的Roidmude已进化！")
                    );
                } else {
                    context.getSource().sendFailure(
                            Component.literal("玩家 " + player.getGameProfile().getName() + " 不是Roidmude")
                    );
                }
            });
            
            return Command.SINGLE_SUCCESS;
        } catch (CommandSyntaxException e) {
            context.getSource().sendFailure(Component.literal("进化Roidmude失败: " + e.getMessage()));
            return 0;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("进化Roidmude失败: " + e.getMessage()));
            return 0;
        }
    }

    /**
     * 检查指定玩家的Roidmude状态
     */
    private static int checkRoidmudeStatus(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer player = EntityArgument.getPlayer(context, "player");
            
            player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(variables -> {
                boolean isRoidmude = variables.isRoidmude;
                String status = isRoidmude ? "是" : "不是";
                
                if (isRoidmude) {
                    String type = variables.roidmudeType;
                    int number = variables.roidmudeNumber;
                    boolean evolved = variables.isRoidmudeEvolved;
                    String evolvedStatus = evolved ? "已进化" : "未进化";
                    
                    context.getSource().sendSuccess(
                            () -> Component.literal("玩家 " + player.getGameProfile().getName() + " " + status + " Roidmude"),
                            false
                    );
                    context.getSource().sendSuccess(
                            () -> Component.literal("类型: " + type + ", 编号: " + number + ", 状态: " + evolvedStatus),
                            false
                    );
                } else {
                    context.getSource().sendSuccess(
                            () -> Component.literal("玩家 " + player.getGameProfile().getName() + " " + status + " Roidmude"),
                            false
                    );
                }
            });
            
            return Command.SINGLE_SUCCESS;
        } catch (CommandSyntaxException e) {
            context.getSource().sendFailure(Component.literal("检查Roidmude状态失败: " + e.getMessage()));
            return 0;
        }
    }

    /**
     * 检查自己的Roidmude状态
     */
    private static int checkOwnRoidmudeStatus(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer player = context.getSource().getPlayerOrException();
            
            player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(variables -> {
                boolean isRoidmude = variables.isRoidmude;
                
                if (isRoidmude) {
                    String type = variables.roidmudeType;
                    int number = variables.roidmudeNumber;
                    boolean evolved = variables.isRoidmudeEvolved;
                    String evolvedStatus = evolved ? "已进化" : "未进化";
                    
                    context.getSource().sendSuccess(
                            () -> Component.literal("你是Roidmude！类型: " + type + ", 编号: " + number + ", 状态: " + evolvedStatus),
                            false
                    );
                } else {
                    context.getSource().sendSuccess(
                            () -> Component.literal("你不是Roidmude。"),
                            false
                    );
                }
            });
            
            return Command.SINGLE_SUCCESS;
        } catch (CommandSyntaxException e) {
            context.getSource().sendFailure(Component.literal("检查Roidmude状态失败: " + e.getMessage()));
            return 0;
        }
    }
}

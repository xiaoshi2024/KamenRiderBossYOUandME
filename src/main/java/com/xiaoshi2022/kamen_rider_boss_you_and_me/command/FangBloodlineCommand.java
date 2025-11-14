package com.xiaoshi2022.kamen_rider_boss_you_and_me.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.advancement.TamedKivatTrigger;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.bloodline.effects.ModEffects;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.varibales.KRBVariables;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.PlayerBloodlineHelper;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;

/**
 * 牙血鬼血脉命令，允许管理员设置玩家的牙血鬼血脉状态
 */
public class FangBloodlineCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("fang_bloodline")
                        .requires(source -> source.hasPermission(2)) // 需要管理员权限
                        .then(
                                Commands.literal("set")
                                        .then(
                                                Commands.argument("player", EntityArgument.player())
                                                        .executes(context -> setFangBloodlineStatus(context, true))
                                        )
                        )
                        .then(
                                Commands.literal("remove")
                                        .then(
                                                Commands.argument("player", EntityArgument.player())
                                                        .executes(context -> setFangBloodlineStatus(context, false))
                                        )
                        )
                        .then(
                                Commands.literal("check")
                                        .then(
                                                Commands.argument("player", EntityArgument.player())
                                                        .executes(context -> checkFangBloodlineStatus(context))
                                        )
                                        .executes(context -> checkOwnFangBloodlineStatus(context))
                        )
                        .then(
                                Commands.literal("set_purity")
                                        .then(
                                                Commands.argument("player", EntityArgument.player())
                                                        .then(
                                                                Commands.argument("purity", FloatArgumentType.floatArg(0.0f, 1.0f))
                                                                        .executes(context -> setFangPurity(context))
                                                        )
                                        )
                        )
        );
    }

    /**
     * 设置玩家的牙血鬼血脉状态
     */
    private static int setFangBloodlineStatus(CommandContext<CommandSourceStack> context, boolean isFangBloodline) {
        try {
            ServerPlayer player = EntityArgument.getPlayer(context, "player");
            
            // 获取当前状态
            boolean wasFangBloodline = player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null)
                    .map(variables -> variables.isFangBloodline)
                    .orElse(false);
            
            // 设置玩家的牙血鬼血脉状态
            player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(variables -> {
                variables.isFangBloodline = isFangBloodline;
                variables.syncPlayerVariables(player);
            });
            
            // 应用或移除牙血鬼之脉效果
            if (isFangBloodline) {
                player.addEffect(new MobEffectInstance(
                        ModEffects.FANG_BLOODLINE.get(), 
                        Integer.MAX_VALUE, 
                        0, 
                        false, 
                        false, 
                        true
                ));
                
                // 确保血脉纯度足够高
                PlayerBloodlineHelper.setFangPurity(player, 1.0f);
                
                // 当授予牙血鬼血脉状态时触发成就
                if (!wasFangBloodline) {
                    TamedKivatTrigger.INSTANCE.trigger(player);
                }
            } else {
                player.removeEffect(ModEffects.FANG_BLOODLINE.get());
            }
            
            String action = isFangBloodline ? "授予" : "移除";
            context.getSource().sendSuccess(
                    () -> Component.literal(action + "了玩家 " + player.getGameProfile().getName() + " 的牙血鬼血脉状态"),
                    true
            );
            
            player.sendSystemMessage(
                    Component.literal("管理员已" + action + "你的牙血鬼血脉状态")
            );
            
            return Command.SINGLE_SUCCESS;
        } catch (CommandSyntaxException e) {
            context.getSource().sendFailure(Component.literal("设置牙血鬼血脉状态失败: " + e.getMessage()));
            return 0;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("设置牙血鬼血脉状态失败: " + e.getMessage()));
            return 0;
        }
    }

    /**
     * 检查指定玩家的牙血鬼血脉状态
     */
    private static int checkFangBloodlineStatus(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer player = EntityArgument.getPlayer(context, "player");
            
            boolean isFangBloodline = player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null)
                    .map(variables -> variables.isFangBloodline)
                    .orElse(false);
            
            float purity = PlayerBloodlineHelper.getFangPurity(player);
            
            String status = isFangBloodline ? "是" : "不是";
            context.getSource().sendSuccess(
                    () -> Component.literal("玩家 " + player.getGameProfile().getName() + " " + status + " 牙血鬼血脉一族，血脉纯度: " + (int)(purity * 100) + "%"),
                    true
            );
            
            return Command.SINGLE_SUCCESS;
        } catch (CommandSyntaxException e) {
            context.getSource().sendFailure(Component.literal("检查牙血鬼血脉状态失败: " + e.getMessage()));
            return 0;
        }
    }
    
    /**
     * 检查自己的牙血鬼血脉状态
     */
    private static int checkOwnFangBloodlineStatus(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer player = context.getSource().getPlayerOrException();
            
            boolean isFangBloodline = player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null)
                    .map(variables -> variables.isFangBloodline)
                    .orElse(false);
            
            float purity = PlayerBloodlineHelper.getFangPurity(player);
            
            String status = isFangBloodline ? "是" : "不是";
            context.getSource().sendSuccess(
                    () -> Component.literal("你 " + status + " 牙血鬼血脉一族，血脉纯度: " + (int)(purity * 100) + "%"),
                    true
            );
            
            return Command.SINGLE_SUCCESS;
        } catch (CommandSyntaxException e) {
            context.getSource().sendFailure(Component.literal("检查牙血鬼血脉状态失败: " + e.getMessage()));
            return 0;
        }
    }
    
    /**
     * 设置玩家的牙血鬼血脉纯度
     */
    private static int setFangPurity(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer player = EntityArgument.getPlayer(context, "player");
            float purity = FloatArgumentType.getFloat(context, "purity");
            
            PlayerBloodlineHelper.setFangPurity(player, purity);
            
            context.getSource().sendSuccess(
                    () -> Component.literal("已将玩家 " + player.getGameProfile().getName() + " 的牙血鬼血脉纯度设置为: " + (int)(purity * 100) + "%"),
                    true
            );
            
            player.sendSystemMessage(
                    Component.literal("管理员已将你的牙血鬼血脉纯度设置为: " + (int)(purity * 100) + "%")
            );
            
            // 如果纯度达到阈值，自动授予牙血鬼血脉状态
            if (purity >= 0.7f) {
                boolean isFangBloodline = player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null)
                        .map(variables -> variables.isFangBloodline)
                        .orElse(false);
                
                if (!isFangBloodline) {
                    player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(variables -> {
                        variables.isFangBloodline = true;
                        variables.syncPlayerVariables(player);
                    });
                    
                    player.addEffect(new MobEffectInstance(
                            ModEffects.FANG_BLOODLINE.get(), 
                            Integer.MAX_VALUE, 
                            0, 
                            false, 
                            false, 
                            true
                    ));
                    
                    TamedKivatTrigger.INSTANCE.trigger(player);
                    
                    context.getSource().sendSuccess(
                            () -> Component.literal("由于血脉纯度达到阈值，已自动授予玩家 " + player.getGameProfile().getName() + " 牙血鬼血脉状态"),
                            true
                    );
                }
            }
            
            return Command.SINGLE_SUCCESS;
        } catch (CommandSyntaxException e) {
            context.getSource().sendFailure(Component.literal("设置牙血鬼血脉纯度失败: " + e.getMessage()));
            return 0;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("设置牙血鬼血脉纯度失败: " + e.getMessage()));
            return 0;
        }
    }
}
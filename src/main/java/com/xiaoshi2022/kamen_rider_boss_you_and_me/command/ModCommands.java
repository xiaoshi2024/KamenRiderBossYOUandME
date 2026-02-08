package com.xiaoshi2022.kamen_rider_boss_you_and_me.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class ModCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("kiva")
                        .then(
                                Commands.literal("showaltar")
                                        .executes(context -> showAltarStructureCommand(context))
                        )
                        .then(
                                Commands.literal("resetcooldown")
                                        .then(
                                                Commands.argument("player", EntityArgument.player())
                                                        .executes(context -> resetPlayerCooldownCommand(context))
                                        )
                                        .executes(context -> resetAllCooldownsCommand(context))
                        )
        );
        
        // 注册Overlord命令
        OverlordCommand.register(dispatcher);
        
        // 注册Giifu命令
        GiifuCommand.register(dispatcher);
        
        // 注册牙血鬼血脉命令
        FangBloodlineCommand.register(dispatcher);
        
        // 注册变身武器指令
        TransformationWeaponCommand.register(dispatcher);
        
        // 注册眼魔命令
        GhostEyeCommand.register(dispatcher);
        
        // 注册Roidmude命令
        RoidmudeCommand.register(dispatcher);
        
        // 注册骑士踢方块破坏命令
        KickBlockDamageCommand.register(dispatcher);
        
        // 注册暴走命令
        BerserkCommand.register(dispatcher);
    }

    private static int showAltarStructureCommand(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer player = context.getSource().getPlayerOrException();
            BlockPos pos = player.blockPosition();
            com.xiaoshi2022.kamen_rider_boss_you_and_me.block.kivas.ClownBatSummoner.showAltarStructure(player, pos);
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("显示祭坛结构失败: " + e.getMessage()));
            return 0;
        }
    }

    private static int resetPlayerCooldownCommand(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer player = EntityArgument.getPlayer(context, "player");
            com.xiaoshi2022.kamen_rider_boss_you_and_me.block.kivas.ClownBatSummoner.resetPlayerCooldown(player);
            context.getSource().sendSuccess(
                    () -> Component.literal("已重置玩家 " + player.getGameProfile().getName() + " 的冷却时间"),
                    true
            );
            return Command.SINGLE_SUCCESS;
        } catch (CommandSyntaxException e) {
            context.getSource().sendFailure(Component.literal("重置玩家冷却时间失败: " + e.getMessage()));
            return 0;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("重置玩家冷却时间失败: " + e.getMessage()));
            return 0;
        }
    }

    private static int resetAllCooldownsCommand(CommandContext<CommandSourceStack> context) {
        try {
            com.xiaoshi2022.kamen_rider_boss_you_and_me.block.kivas.ClownBatSummoner.resetAllCooldowns();
            context.getSource().sendSuccess(
                    () -> Component.literal("已重置所有玩家的冷却时间"),
                    true
            );
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("重置所有玩家冷却时间失败: " + e.getMessage()));
            return 0;
        }
    }
}
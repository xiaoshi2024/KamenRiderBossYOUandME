package com.xiaoshi2022.kamen_rider_boss_you_and_me.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.TransformationConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;

public class TransformationWeaponCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("transformationweapon")
                        .requires(source -> source.hasPermission(2)) // 需要管理员权限
                        .then(
                                Commands.literal("enable")
                                        .executes(context -> enableWeaponCommand(context))
                        )
                        .then(
                                Commands.literal("disable")
                                        .executes(context -> disableWeaponCommand(context))
                        )
                        .then(
                                Commands.literal("status")
                                        .executes(context -> checkStatusCommand(context))
                        )
        );
    }

    private static int enableWeaponCommand(CommandContext<CommandSourceStack> context) {
        try {
            CommandSourceStack source = context.getSource();
            MinecraftServer server = source.getServer();
            String serverId = server.getServerModName(); // 使用服务器名称作为唯一标识
            
            TransformationConfig.setGlobalGiveWeaponEnabled(true);
            // 也可以设置服务器特定的状态：TransformationConfig.setGiveWeaponEnabledForServer(serverId, true);
            
            source.sendSuccess(
                    () -> Component.literal("已启用变身后自动给予武器功能"),
                    true
            );
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("启用武器功能失败: " + e.getMessage()));
            return 0;
        }
    }

    private static int disableWeaponCommand(CommandContext<CommandSourceStack> context) {
        try {
            CommandSourceStack source = context.getSource();
            MinecraftServer server = source.getServer();
            String serverId = server.getServerModName(); // 使用服务器名称作为唯一标识
            
            TransformationConfig.setGlobalGiveWeaponEnabled(false);
            // 也可以设置服务器特定的状态：TransformationConfig.setGiveWeaponEnabledForServer(serverId, false);
            
            source.sendSuccess(
                    () -> Component.literal("已禁用变身后自动给予武器功能"),
                    true
            );
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("禁用武器功能失败: " + e.getMessage()));
            return 0;
        }
    }

    private static int checkStatusCommand(CommandContext<CommandSourceStack> context) {
        try {
            CommandSourceStack source = context.getSource();
            MinecraftServer server = source.getServer();
            String serverId = server.getServerModName(); // 使用服务器名称作为唯一标识
            
            boolean status = TransformationConfig.isGlobalGiveWeaponEnabled();
            // 也可以检查服务器特定的状态：TransformationConfig.isGiveWeaponEnabledForServer(serverId);
            
            source.sendSuccess(
                    () -> Component.literal("变身后自动给予武器功能状态: " + (status ? "已启用" : "已禁用")),
                    false
            );
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("检查状态失败: " + e.getMessage()));
            return 0;
        }
    }
}
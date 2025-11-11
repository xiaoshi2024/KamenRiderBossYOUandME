package com.xiaoshi2022.kamen_rider_boss_you_and_me.commands;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.anotherRiders.skill.aidcdskill.AnotherWorldSummon;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class AnotherDecadeCommand {
    
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("anotherdecade")
                .requires(source -> source.hasPermission(0) || source.isPlayer()) // 单人游戏中玩家不需要OP权限
                .then(Commands.literal("summon")
                        .then(Commands.literal("add")
                                .then(Commands.argument("entity", ResourceLocationArgument.id())
                                        .then(Commands.argument("weight", DoubleArgumentType.doubleArg(0.1D, 5.0D))
                                                .executes(context -> addSummonEntity(
                                                        context.getSource(),
                                                        ResourceLocationArgument.getId(context, "entity"),
                                                        DoubleArgumentType.getDouble(context, "weight"),
                                                        "normal"))))
                                .then(Commands.literal("low_health")
                                        .then(Commands.argument("entity", ResourceLocationArgument.id())
                                                .then(Commands.argument("weight", DoubleArgumentType.doubleArg(0.1D, 5.0D))
                                                        .executes(context -> addSummonEntity(
                                                                context.getSource(),
                                                                ResourceLocationArgument.getId(context, "entity"),
                                                                DoubleArgumentType.getDouble(context, "weight"),
                                                                "low_health")))))
                                .then(Commands.literal("enraged")
                                        .then(Commands.argument("entity", ResourceLocationArgument.id())
                                                .then(Commands.argument("weight", DoubleArgumentType.doubleArg(0.1D, 5.0D))
                                                        .executes(context -> addSummonEntity(
                                                                context.getSource(),
                                                                ResourceLocationArgument.getId(context, "entity"),
                                                                DoubleArgumentType.getDouble(context, "weight"),
                                                                "enraged"))))))
                        .then(Commands.literal("remove")
                                .then(Commands.argument("entity", ResourceLocationArgument.id())
                                        .executes(context -> removeSummonEntity(
                                                context.getSource(),
                                                ResourceLocationArgument.getId(context, "entity")))))
                        .then(Commands.literal("list")
                                .executes(context -> listSummonEntities(context.getSource())))
                        .then(Commands.literal("clear")
                                .executes(context -> clearSummonEntities(context.getSource()))))
                .then(Commands.literal("debug")
                        .then(Commands.literal("on")
                                .executes(context -> setDebugMode(context.getSource(), true)))
                        .then(Commands.literal("off")
                                .executes(context -> setDebugMode(context.getSource(), false))));
        
        dispatcher.register(command);
        
        // 为数据包支持添加一个更通用的命令，不需要OP权限
        LiteralArgumentBuilder<CommandSourceStack> dataPackCommand = Commands.literal("data")
                .then(Commands.literal("anotherdecade")
                        .then(Commands.literal("summon")
                                .then(Commands.literal("add")
                                        .then(Commands.argument("pool", StringArgumentType.string())
                                                .then(Commands.argument("entity", ResourceLocationArgument.id())
                                                        .then(Commands.argument("weight", DoubleArgumentType.doubleArg(0.1D, 5.0D))
                                                                .executes(context -> addSummonEntityDataPack(
                                                                        context.getSource(),
                                                                        StringArgumentType.getString(context, "pool"),
                                                                        ResourceLocationArgument.getId(context, "entity"),
                                                                        DoubleArgumentType.getDouble(context, "weight")))))))));
        
        dispatcher.register(dataPackCommand);
    }
    
    private static int addSummonEntity(CommandSourceStack source, ResourceLocation entityId, double weight, String poolType) {
        try {
            // 添加调试信息
            if (source.isPlayer()) {
                source.sendSystemMessage(Component.literal("[调试] 开始添加实体: " + entityId + ", 权重: " + weight + ", 池类型: " + poolType));
            }
            
            // 直接使用ResourceLocation对象，因为ResourceLocationArgument已经验证了格式
            String entityIdStr = entityId.toString();
            boolean added = false;
            
            switch (poolType) {
                case "normal":
                    // 使用AnotherWorldSummon的原始方法，直接传入ResourceLocation
                    AnotherWorldSummon.addSummonEntity(entityId, weight);
                    source.sendSuccess(() -> Component.literal("已添加实体到普通召唤池: " + entityId + " (权重: " + weight + ")"), true);
                    added = true;
                    break;
                case "low_health":
                    AnotherWorldSummon.addLowHealthEntity(entityId, weight);
                    source.sendSuccess(() -> Component.literal("已添加实体到低血量召唤池: " + entityId + " (权重: " + weight + ")"), true);
                    added = true;
                    break;
                case "enraged":
                    AnotherWorldSummon.addEnragedEntity(entityId, weight);
                    source.sendSuccess(() -> Component.literal("已添加实体到愤怒状态召唤池: " + entityId + " (权重: " + weight + ")"), true);
                    added = true;
                    break;
            }
            
            if (added && source.isPlayer()) {
                source.sendSystemMessage(Component.literal("[调试] 实体添加成功，建议使用 /anotherdecade summon list 验证"));
            }
            
            return 1;
        } catch (Exception e) {
            String errorMsg = "添加实体时出错: " + e.getMessage();
            source.sendFailure(Component.literal(errorMsg));
            if (source.isPlayer()) {
                source.sendSystemMessage(Component.literal("[调试] " + errorMsg));
                source.sendSystemMessage(Component.literal("[调试] 异常类型: " + e.getClass().getName()));
                source.sendSystemMessage(Component.literal("[调试] 可能是实体不存在，请检查实体ID是否正确"));
            }
            return 0;
        }
    }
    
    private static int addSummonEntityDataPack(CommandSourceStack source, String poolType, ResourceLocation entityId, double weight) {
        try {
            // 这里与addSummonEntity方法逻辑类似，但用于数据包执行
            switch (poolType) {
                case "normal":
                    AnotherWorldSummon.addSummonEntity(entityId, weight);
                    break;
                case "low_health":
                    AnotherWorldSummon.addLowHealthEntity(entityId, weight);
                    break;
                case "enraged":
                    AnotherWorldSummon.addEnragedEntity(entityId, weight);
                    break;
            }
            
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }
    
    private static int removeSummonEntity(CommandSourceStack source, ResourceLocation entityId) {
        try {
            // 从所有池中移除实体
            boolean removed = false;
            
            // 这里需要修改AnotherWorldSummon类，添加移除方法
            // 暂时只发送消息
            source.sendSuccess(() -> Component.literal("注意: 移除功能尚未实现，请修改AnotherWorldSummon类添加移除方法"), true);
            
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("移除实体时出错: " + e.getMessage()));
            return 0;
        }
    }
    
    private static int listSummonEntities(CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal("=== 异类Decade可召唤实体列表 ==="), false);
        
        // 显示普通召唤池
        source.sendSuccess(() -> Component.literal("\n[普通召唤池]"), false);
        AnotherWorldSummon.getSummonableEntities().forEach(entity -> {
            source.sendSuccess(() -> Component.literal("- " + entity), false);
        });
        
        // 显示低血量召唤池
        source.sendSuccess(() -> Component.literal("\n[低血量召唤池]"), false);
        AnotherWorldSummon.getLowHealthSummonableEntities().forEach(entity -> {
            source.sendSuccess(() -> Component.literal("- " + entity), false);
        });
        
        // 显示愤怒状态召唤池
        source.sendSuccess(() -> Component.literal("\n[愤怒状态召唤池]"), false);
        AnotherWorldSummon.getEnragedSummonableEntities().forEach(entity -> {
            source.sendSuccess(() -> Component.literal("- " + entity), false);
        });
        
        source.sendSuccess(() -> Component.literal("\n使用 /anotherdecade summon add <entity> <weight> [low_health|enraged] 添加实体"), false);
        source.sendSuccess(() -> Component.literal("使用 /anotherdecade debug on 开启调试模式查看更多信息"), false);
        
        return 1;
    }
    
    private static int clearSummonEntities(CommandSourceStack source) {
        AnotherWorldSummon.clearAllEntities();
        source.sendSuccess(() -> Component.literal("已清空所有召唤实体列表"), true);
        return 1;
    }
    
    private static int setDebugMode(CommandSourceStack source, boolean enabled) {
        AnotherWorldSummon.setDebugMode(enabled);
        source.sendSuccess(() -> Component.literal("调试模式已" + (enabled ? "开启" : "关闭")), true);
        return 1;
    }
}
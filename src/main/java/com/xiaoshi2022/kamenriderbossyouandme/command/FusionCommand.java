// command/FusionCommand.java
package com.xiaoshi2022.kamenriderbossyouandme.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.xiaoshi2022.kamenriderbossyouandme.entity.FusionEffectEntity;
import com.xiaoshi2022.kamenriderbossyouandme.handler.TransformationHandler;
import com.xiaoshi2022.kamenriderbossyouandme.manager.FusionTagManager;
import com.xiaoshi2022.kamenriderbossyouandme.registry.ModEntitys;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Set;
import java.util.UUID;

public class FusionCommand {

    private static final SuggestionProvider<CommandSourceStack> ONLINE_PLAYERS =
            (context, builder) -> {
                return SharedSuggestionProvider.suggest(
                        context.getSource().getOnlinePlayerNames(),
                        builder
                );
            };

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("fusion")
                .requires(source -> source.hasPermission(2)) // OP权限

                // ===== 测试指令: 直接召唤带皮肤的融合特效 =====
                .then(Commands.literal("summon")
                        .then(Commands.argument("player1", StringArgumentType.word())
                                .suggests(ONLINE_PLAYERS)
                                .then(Commands.argument("player2", StringArgumentType.word())
                                        .suggests(ONLINE_PLAYERS)
                                        .then(Commands.argument("player3", StringArgumentType.word())
                                                .suggests(ONLINE_PLAYERS)
                                                .executes(context -> {
                                                    ServerPlayer player = context.getSource().getPlayer();
                                                    if (player == null) {
                                                        context.getSource().sendFailure(
                                                                Component.literal("§c只能由玩家执行")
                                                        );
                                                        return 0;
                                                    }

                                                    String name1 = StringArgumentType.getString(context, "player1");
                                                    String name2 = StringArgumentType.getString(context, "player2");
                                                    String name3 = StringArgumentType.getString(context, "player3");

                                                    // 创建融合特效实体
                                                    FusionEffectEntity entity = new FusionEffectEntity(
                                                            ModEntitys.FUSION_EFFECT.get(),
                                                            player.level()
                                                    );
                                                    entity.setPos(
                                                            player.getX(),
                                                            player.getY() + 1.0,
                                                            player.getZ()
                                                    );
                                                    entity.setPlayerNames(name1, name2, name3);
                                                    player.level().addFreshEntity(entity);

                                                    context.getSource().sendSuccess(
                                                            () -> Component.literal(
                                                                    "§a✅ 融合特效已召唤！\n" +
                                                                            "§7玩家1: §f" + name1 + "\n" +
                                                                            "§7玩家2: §f" + name2 + "\n" +
                                                                            "§7玩家3: §f" + name3
                                                            ),
                                                            true
                                                    );
                                                    return 1;
                                                })
                                        )
                                )
                        )
                )

                // 原有的指令...
                // 添加标签: /fusion tag <玩家名>
                .then(Commands.literal("tag")
                        .then(Commands.argument("player", StringArgumentType.word())
                                .suggests(ONLINE_PLAYERS)
                                .executes(context -> {
                                    String playerName = StringArgumentType.getString(context, "player");
                                    ServerPlayer target = context.getSource().getServer().getPlayerList()
                                            .getPlayerByName(playerName);

                                    if (target == null) {
                                        context.getSource().sendFailure(
                                                Component.literal("§c玩家不存在: " + playerName)
                                        );
                                        return 0;
                                    }

                                    if (FusionTagManager.isFusionTarget(target)) {
                                        context.getSource().sendFailure(
                                                Component.literal("§e" + playerName + " 已经是融合者了")
                                        );
                                        return 0;
                                    }

                                    FusionTagManager.addFusionTarget(target);
                                    context.getSource().sendSuccess(
                                            () -> Component.literal("§a✅ " + playerName + " 已被标记为 §6融合者§f！"),
                                            true
                                    );
                                    target.sendSystemMessage(
                                            Component.literal("§6⚡ 你已被标记为 §e融合者§6！")
                                    );
                                    return 1;
                                })
                        )
                )
                // 移除标签: /fusion untag <玩家名>
                .then(Commands.literal("untag")
                        .then(Commands.argument("player", StringArgumentType.word())
                                .suggests(ONLINE_PLAYERS)
                                .executes(context -> {
                                    String playerName = StringArgumentType.getString(context, "player");
                                    ServerPlayer target = context.getSource().getServer().getPlayerList()
                                            .getPlayerByName(playerName);

                                    if (target == null) {
                                        context.getSource().sendFailure(
                                                Component.literal("§c玩家不存在: " + playerName)
                                        );
                                        return 0;
                                    }

                                    if (!FusionTagManager.isFusionTarget(target)) {
                                        context.getSource().sendFailure(
                                                Component.literal("§e" + playerName + " 不是融合者")
                                        );
                                        return 0;
                                    }

                                    FusionTagManager.removeFusionTarget(target);
                                    context.getSource().sendSuccess(
                                            () -> Component.literal("§c❌ " + playerName + " 已移除 §6融合者§c 标签"),
                                            true
                                    );
                                    target.sendSystemMessage(
                                            Component.literal("§c❌ 你已不再是融合者")
                                    );
                                    return 1;
                                })
                        )
                )
                // 查看列表: /fusion list
                .then(Commands.literal("list")
                        .executes(context -> {
                            Set<UUID> targets = FusionTagManager.getAllFusionTargets();
                            if (targets.isEmpty()) {
                                context.getSource().sendSuccess(
                                        () -> Component.literal("§7当前没有融合者"),
                                        true
                                );
                                return 1;
                            }

                            StringBuilder list = new StringBuilder("§6融合者列表 (§e" + targets.size() + "§6):\n");
                            for (UUID uuid : targets) {
                                ServerPlayer player = context.getSource().getServer().getPlayerList()
                                        .getPlayer(uuid);
                                if (player != null) {
                                    list.append("§7- §f").append(player.getName().getString()).append("\n");
                                }
                            }

                            context.getSource().sendSuccess(
                                    () -> Component.literal(list.toString()),
                                    true
                            );
                            return 1;
                        })
                )
                // 清除所有: /fusion clear
                .then(Commands.literal("clear")
                        .executes(context -> {
                            FusionTagManager.clearAll();
                            context.getSource().sendSuccess(
                                    () -> Component.literal("§c🧹 已清除所有 §6融合者§c 标签"),
                                    true
                            );
                            return 1;
                        })
                )
                // 变身: /fusion transform
                .then(Commands.literal("transform")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayer();
                            if (player == null) {
                                context.getSource().sendFailure(
                                        Component.literal("§c只能由玩家执行")
                                );
                                return 0;
                            }

                            boolean success = TransformationHandler.performTransformation(player);
                            return success ? 1 : 0;
                        })
                )
                // 取消变身: /fusion cancel
                .then(Commands.literal("cancel")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayer();
                            if (player == null) {
                                context.getSource().sendFailure(
                                        Component.literal("§c只能由玩家执行")
                                );
                                return 0;
                            }

                            TransformationHandler.cancelTransformation(player);
                            return 1;
                        })
                )
                // 查看状态: /fusion status
                .then(Commands.literal("status")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayer();
                            if (player == null) {
                                context.getSource().sendFailure(
                                        Component.literal("§c只能由玩家执行")
                                );
                                return 0;
                            }

                            int count = TransformationHandler.getAvailableFusionCount(player);
                            boolean canTransform = TransformationHandler.canTransform(player);

                            context.getSource().sendSuccess(
                                    () -> Component.literal(
                                            "§7=== §6融合状态 §7===\n" +
                                                    "§7附近融合者: §e" + count + "§7/§e3\n" +
                                                    "§7可变身: " + (canTransform ? "§a✅ 是" : "§c❌ 否")
                                    ),
                                    true
                            );
                            return 1;
                        })
                )
                // 帮助: /fusion help
                .then(Commands.literal("help")
                        .executes(context -> {
                            context.getSource().sendSuccess(
                                    () -> Component.literal(
                                            "§6=== 融合指令帮助 ===\n" +
                                                    "§7/fusion summon <玩家1> <玩家2> <玩家3> §f- 测试召唤\n" +
                                                    "§7/fusion tag <玩家> §f- 标记融合者\n" +
                                                    "§7/fusion untag <玩家> §f- 移除融合者\n" +
                                                    "§7/fusion list §f- 查看融合者列表\n" +
                                                    "§7/fusion clear §f- 清除所有融合者\n" +
                                                    "§7/fusion transform §f- 执行变身\n" +
                                                    "§7/fusion cancel §f- 取消变身\n" +
                                                    "§7/fusion status §f- 查看状态"
                                    ),
                                    true
                            );
                            return 1;
                        })
                )
        );
    }
}
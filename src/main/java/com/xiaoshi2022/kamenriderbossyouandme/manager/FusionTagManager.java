// manager/FusionTagManager.java
package com.xiaoshi2022.kamenriderbossyouandme.manager;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 融合者标签管理器
 * 管理"融合者"标签的添加、移除和查询
 */
public class FusionTagManager {

    // 存储所有被标记为"融合者"的玩家UUID
    private static final Set<UUID> FUSION_TARGETS = ConcurrentHashMap.newKeySet();

    // 存储变身者对应的3个融合伙伴 (变身者UUID -> 融合者UUID列表)
    private static final Map<UUID, List<UUID>> FUSION_PARTNERS = new ConcurrentHashMap<>();

    /**
     * 给玩家添加"融合者"标签
     */
    public static void addFusionTarget(Player player) {
        if (player == null) return;
        FUSION_TARGETS.add(player.getUUID());
    }

    /**
     * 移除玩家的"融合者"标签
     */
    public static void removeFusionTarget(Player player) {
        if (player == null) return;
        FUSION_TARGETS.remove(player.getUUID());
    }

    /**
     * 检查玩家是否有"融合者"标签
     */
    public static boolean isFusionTarget(Player player) {
        if (player == null) return false;
        return FUSION_TARGETS.contains(player.getUUID());
    }

    /**
     * 获取附近带有"融合者"标签的玩家列表
     * @param transformer 变身者
     * @param radius 检测半径
     * @return 带有标签的玩家列表
     */
    public static List<Player> getNearbyFusionTargets(Player transformer, double radius) {
        List<Player> targets = new ArrayList<>();

        if (transformer == null || transformer.level() == null) return targets;

        Level level = transformer.level();

        // 获取范围内所有玩家
        for (Player player : level.players()) {
            // 排除变身者自己
            if (player == transformer) continue;

            // 检查是否有"融合者"标签
            if (!isFusionTarget(player)) continue;

            // 检查距离
            double distance = player.distanceTo(transformer);
            if (distance <= radius) {
                targets.add(player);
            }
        }

        return targets;
    }

    /**
     * 设置变身者的融合伙伴（保存3个选中的融合者）
     */
    public static void setFusionPartners(Player transformer, List<Player> partners) {
        if (transformer == null) return;

        List<UUID> partnerIds = new ArrayList<>();
        for (Player partner : partners) {
            if (partner != null) {
                partnerIds.add(partner.getUUID());
            }
        }
        FUSION_PARTNERS.put(transformer.getUUID(), partnerIds);
    }

    /**
     * 获取变身者的融合伙伴
     */
    public static List<Player> getFusionPartners(Player transformer) {
        List<Player> partners = new ArrayList<>();

        if (transformer == null || transformer.level() == null) return partners;

        List<UUID> partnerIds = FUSION_PARTNERS.get(transformer.getUUID());
        if (partnerIds == null) return partners;

        Level level = transformer.level();
        for (UUID uuid : partnerIds) {
            // 尝试获取玩家
            Player player = level.getPlayerByUUID(uuid);
            if (player != null) {
                partners.add(player);
            }
        }

        return partners;
    }

    /**
     * 清除变身者的融合伙伴记录
     */
    public static void clearFusionPartners(Player transformer) {
        if (transformer == null) return;
        FUSION_PARTNERS.remove(transformer.getUUID());
    }

    /**
     * 获取所有融合者标签玩家
     */
    public static Set<UUID> getAllFusionTargets() {
        return new HashSet<>(FUSION_TARGETS);
    }

    /**
     * 清除所有标签和数据
     */
    public static void clearAll() {
        FUSION_TARGETS.clear();
        FUSION_PARTNERS.clear();
    }
}
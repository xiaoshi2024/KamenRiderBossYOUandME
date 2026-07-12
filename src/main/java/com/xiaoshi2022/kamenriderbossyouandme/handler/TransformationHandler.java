// handler/TransformationHandler.java
package com.xiaoshi2022.kamenriderbossyouandme.handler;

import com.xiaoshi2022.kamenriderbossyouandme.entity.FusionEffectEntity;
import com.xiaoshi2022.kamenriderbossyouandme.manager.FusionStateManager;
import com.xiaoshi2022.kamenriderbossyouandme.manager.FusionTagManager;
import com.xiaoshi2022.kamenriderbossyouandme.manager.FusionTeleportManager;
import com.xiaoshi2022.kamenriderbossyouandme.registry.ModEntitys;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public class TransformationHandler {

    private static final double DETECTION_RADIUS = 15.0;
    private static final int REQUIRED_PLAYERS = 3;

    public static boolean performTransformation(Player transformer) {
        if (transformer == null || transformer.level().isClientSide()) {
            return false;
        }

        // 1. 获取附近融合者
        List<Player> fusionTargets = FusionTagManager.getNearbyFusionTargets(transformer, DETECTION_RADIUS);

        if (fusionTargets.size() < REQUIRED_PLAYERS) {
            transformer.sendSystemMessage(
                    Component.literal("§c❌ 需要至少 " + REQUIRED_PLAYERS + " 个融合者在附近！"
                            + " (当前: " + fusionTargets.size() + "/" + REQUIRED_PLAYERS + ")")
            );
            return false;
        }

        // 2. 取前3个融合者
        List<Player> selectedPartners = fusionTargets.subList(0, REQUIRED_PLAYERS);

        // 3. 检查融合者是否在传送冷却中
        for (Player partner : selectedPartners) {
            if (FusionTeleportManager.isInCooldown(partner)) {
                transformer.sendSystemMessage(
                        Component.literal("§e⚠️ " + partner.getName().getString()
                                + " 还在传送冷却中！")
                );
                return false;
            }
        }

        // 4. 保存融合伙伴
        FusionStateManager.createBinding(transformer, selectedPartners.toArray(new Player[0]), 60);

        // 5. 将融合者传送到地狱
        for (Player partner : selectedPartners) {
            FusionTeleportManager.teleportToHell(partner, transformer);
        }

        // 6. 生成融合特效
        spawnFusionEffect(transformer, selectedPartners);

        // 7. 发送成功消息
        StringBuilder names = new StringBuilder();
        for (int i = 0; i < selectedPartners.size(); i++) {
            if (i > 0) names.append("§7, §f");
            names.append(selectedPartners.get(i).getName().getString());
        }

        transformer.sendSystemMessage(
                Component.literal("§a✅ 融合变身成功！§r\n")
                        .append(Component.literal("§7融合者已流放至地狱: §f" + names.toString()))
        );

        return true;
    }

    /**
     * 生成融合特效实体
     */
    private static void spawnFusionEffect(Player transformer, List<Player> partners) {
        if (!(transformer.level() instanceof ServerLevel serverLevel)) return;

        String name1 = partners.get(0).getName().getString();
        String name2 = partners.size() > 1 ? partners.get(1).getName().getString() : "";
        String name3 = partners.size() > 2 ? partners.get(2).getName().getString() : "";

        FusionEffectEntity entity = new FusionEffectEntity(
                ModEntitys.FUSION_EFFECT.get(),
                transformer.level()
        );

        entity.setPos(
                transformer.getX(),
                transformer.getY() + 0.5,
                transformer.getZ()
        );

        entity.setPlayerNames(name1, name2, name3);
        serverLevel.addFreshEntity(entity);

        // 发送网络包同步皮肤
        // 注意：需要为每个玩家发送皮肤更新
        // 这里逻辑由客户端自己加载，不需要发送
    }

    /**
     * 取消变身
     */
    public static void cancelTransformation(Player transformer) {
        if (transformer == null || transformer.level().isClientSide()) return;

        FusionStateManager.removeBinding(transformer);

        transformer.level().getEntitiesOfClass(
                FusionEffectEntity.class,
                transformer.getBoundingBox().inflate(10),
                entity -> entity.distanceTo(transformer) < 10
        ).forEach(entity -> entity.remove(Entity.RemovalReason.DISCARDED));

        transformer.sendSystemMessage(
                Component.literal("§c❌ 已取消融合变身")
        );
    }

    /**
     * 获取可用融合者数量
     */
    public static int getAvailableFusionCount(Player transformer) {
        if (transformer == null) return 0;
        return FusionTagManager.getNearbyFusionTargets(transformer, DETECTION_RADIUS).size();
    }

    /**
     * 检查是否可以变身
     */
    public static boolean canTransform(Player transformer) {
        if (transformer == null) return false;
        return getAvailableFusionCount(transformer) >= REQUIRED_PLAYERS;
    }
}
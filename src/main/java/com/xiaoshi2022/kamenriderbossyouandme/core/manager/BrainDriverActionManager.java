package com.xiaoshi2022.kamenriderbossyouandme.core.manager;

import com.jpigeon.ridebattlelib.server.system.helper.DriverActionManager;
import com.xiaoshi2022.kamenriderbossyouandme.Accessory.BrainDriver;
import com.xiaoshi2022.kamenriderbossyouandme.KamenRiderBossYOUandME;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/**
 * 自定义Brain变身管理器
 * 继承 DriverActionManager 来覆盖变身逻辑
 */
public class BrainDriverActionManager extends DriverActionManager {

    private static BrainDriverActionManager instance;

    public static BrainDriverActionManager getInstance() {
        if (instance == null) {
            instance = new BrainDriverActionManager();
        }
        return instance;
    }

    private BrainDriverActionManager() {
        super(); // 调用父类构造
    }

    /**
     * 重写完成变身方法 - 添加Brain特殊逻辑
     */
    @Override
    public void completeTransformation(Player player) {
        if (player == null || !(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        // 检查是否是Brain骑士
        if (!isBrainRider(serverPlayer)) {
            // 如果不是Brain，调用父类方法
            super.completeTransformation(player);
            return;
        }

        try {
            // Brain特殊变身逻辑
            KamenRiderBossYOUandME.LOGGER.debug("Brain完成变身: {}", player.getName().getString());

            // 1. 设置腰带状态（在BrainDriver中已经设置了）
            // 2. 直接调用父类的核心逻辑
            // 注意：这里不调用 super.completeTransformation，避免事件循环

            // 使用反射或受保护的方法来完成变身
            // 或者直接调用RideBattleAPI的完成方法
            com.jpigeon.ridebattlelib.common.api.RideBattleAPI.completeHenshin(player);

        } catch (Exception e) {
            KamenRiderBossYOUandME.LOGGER.error("Brain完成变身失败", e);
        }
    }

    /**
     * 判断是否是Brain骑士
     */
    private boolean isBrainRider(Player player) {
        // 检查是否装备了BrainDriver
        return com.xiaoshi2022.kamenriderbossyouandme.util.CurioUtils
                .findFirstCurio(player, stack -> stack.getItem() instanceof BrainDriver)
                .isPresent();
    }
}
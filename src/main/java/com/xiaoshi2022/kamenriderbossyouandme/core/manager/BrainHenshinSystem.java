package com.xiaoshi2022.kamenriderbossyouandme.core.manager;

import com.jpigeon.ridebattlelib.server.system.HenshinSystem;
import com.xiaoshi2022.kamenriderbossyouandme.Accessory.BrainDriver;
import com.xiaoshi2022.kamenriderbossyouandme.KamenRiderBossYOUandME;
import com.xiaoshi2022.kamenriderbossyouandme.core.handler.henshinHandler.BrainHandler;
import net.minecraft.world.entity.player.Player;

/**
 * 自定义Brain变身系统
 */
public class BrainHenshinSystem extends HenshinSystem {

    private static BrainHenshinSystem instance;

    public static BrainHenshinSystem getInstance() {
        if (instance == null) {
            instance = new BrainHenshinSystem();
        }
        return instance;
    }

    private BrainHenshinSystem() {
        super();
    }

    @Override
    public void driverAction(Player player) {
        if (player == null) return;

        // 检查是否是Brain骑士
        if (isBrainRider(player)) {
            // Brain特殊处理：不通过事件系统
            handleBrainDriverAction(player);
        } else {
            // 其他骑士使用默认逻辑
            super.driverAction(player);
        }
    }

    private void handleBrainDriverAction(Player player) {
        KamenRiderBossYOUandME.LOGGER.debug("Brain驱动动作: {}", player.getName().getString());

        // ✅ 直接调用 BrainHandler 的变身方法
        // 但 BrainHandler 中没有 startBrainHenshin，所以我们需要调用 handleBrainHenshin
        // 但是 handleBrainHenshin 是 private 的

        // 方案1: 将 handleBrainHenshin 改为 public static
        // BrainHandler.handleBrainHenshin(player);

        // 方案2: 在 BrainHandler 中添加公共方法
        BrainHandler.triggerBrainHenshin(player);
    }

    private boolean isBrainRider(Player player) {
        return com.xiaoshi2022.kamenriderbossyouandme.util.CurioUtils
                .findFirstCurio(player, stack -> stack.getItem() instanceof BrainDriver)
                .isPresent();
    }
}
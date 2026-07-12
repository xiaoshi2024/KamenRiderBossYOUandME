// manager/FusionStateManager.java
package com.xiaoshi2022.kamenriderbossyouandme.manager;

import net.minecraft.world.entity.player.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 融合者状态管理
 */
public class FusionStateManager {

    // 变身者与融合者的绑定
    private static final Map<UUID, FusionBinding> BINDINGS = new ConcurrentHashMap<>();

    /**
     * 融合绑定
     */
    public static class FusionBinding {
        public final UUID transformerId;
        public final UUID[] partners;
        public final long transformTime;
        public final int duration;

        public FusionBinding(UUID transformerId, UUID[] partners, int duration) {
            this.transformerId = transformerId;
            this.partners = partners.clone();
            this.transformTime = System.currentTimeMillis();
            this.duration = duration;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() - transformTime > duration * 1000L;
        }
    }

    /**
     * 创建融合绑定
     */
    public static void createBinding(Player transformer, Player[] partners, int duration) {
        UUID[] partnerIds = new UUID[3];
        for (int i = 0; i < 3 && i < partners.length; i++) {
            if (partners[i] != null) {
                partnerIds[i] = partners[i].getUUID();
            }
        }
        BINDINGS.put(transformer.getUUID(), new FusionBinding(
                transformer.getUUID(), partnerIds, duration
        ));
    }

    /**
     * 移除绑定
     */
    public static void removeBinding(Player transformer) {
        if (transformer != null) {
            BINDINGS.remove(transformer.getUUID());
        }
    }

    /**
     * 获取绑定
     */
    public static FusionBinding getBinding(Player transformer) {
        if (transformer == null) return null;
        return BINDINGS.get(transformer.getUUID());
    }
}
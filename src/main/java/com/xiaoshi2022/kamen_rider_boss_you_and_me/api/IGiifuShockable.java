package com.xiaoshi2022.kamen_rider_boss_you_and_me.api;

import net.minecraft.world.entity.player.Player;

/**
 * 被基夫冲击波命中时的回调。
 * 任何“变身骑士”实体或玩家实现此接口即可被强制解除变身。
 */
public interface IGiifuShockable {
    /**
     * @param player 受击玩家（或骑士实体）
     * @return true 表示“变身已被解除”，false 表示“没有变身或解除失败”
     */
    boolean onGiifuShockWave(Player player);
}
package com.xiaoshi2022.kamen_rider_boss_you_and_me.util;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import top.theillusivec4.curios.api.CuriosApi;

/**
 * 骑士盔甲工具类，提供通用的检查方法
 */
public class KamenBossArmorUtil {
    
    /**
     * 检查玩家是否装备了实现KamenBossArmor接口的盔甲并且处于变身状态
     * @param player 玩家对象
     * @return 是否装备并处于变身状态
     * @deprecated 该方法已被弃用，现在直接使用isKamenBossArmorEquipped方法来检测腰带和盔甲
     */
    @Deprecated
    public static boolean isKamenBossArmorEquippedAndTransformed(Player player) {
        // 现在直接调用isKamenBossArmorEquipped方法，该方法会检测腰带和盔甲
        return isKamenBossArmorEquipped(player);
    }
    
    /**
     * 检查玩家是否佩戴了任何骑士腰带
     * 注意：只检查腰带佩戴状态，不检查盔甲
     * @param player 玩家对象
     * @return 是否佩戴了骑士腰带
     */
    public static boolean isKamenBossArmorEquipped(Player player) {
        if (player == null) {
            return false;
        }
        
        // 服务端环境处理
        if (player instanceof ServerPlayer serverPlayer) {
            // 仅检查Curios饰品栏中的腰带
            // 如果玩家佩戴了任何一种骑士腰带，则返回true
            return hasRiderBeltInCurios(serverPlayer);
        }
        
        // 客户端环境处理
        // 在客户端也需要检查Curios饰品栏中的腰带
        try {
            // 使用Curios API查找玩家是否佩戴了指定的骑士腰带
            return CuriosApi.getCuriosInventory(player)
                .resolve()
                .flatMap(inv -> 
                    // 检查玩家是否佩戴了任何一种指定的骑士腰带
                    inv.findFirstCurio(stack -> 
                        stack.getItem() instanceof DrakKivaBelt ||
                        stack.getItem() instanceof Genesis_driver ||
                        stack.getItem() instanceof Mega_uiorder ||
                        stack.getItem() instanceof Two_sidriver ||
                                stack.getItem() instanceof GhostDriver||
                        stack.getItem() instanceof sengokudrivers_epmty
                    )
                )
                .isPresent(); // 如果找到任何一种腰带，返回true
        } catch (Exception e) {
            // 如果Curios API出现任何异常，返回false
            return false;
        }
    }
    
    /**
     * 检查玩家Curios饰品栏中是否佩戴了任何骑士腰带
     * @param player 玩家对象
     * @return 是否佩戴了骑士腰带
     */
    private static boolean hasRiderBeltInCurios(ServerPlayer player) {
        try {
            // 使用Curios API查找玩家是否佩戴了指定的骑士腰带
            return CuriosApi.getCuriosInventory(player)
                .resolve()
                .flatMap(inv -> 
                    // 检查玩家是否佩戴了任何一种指定的骑士腰带
                    inv.findFirstCurio(stack -> 
                        stack.getItem() instanceof DrakKivaBelt ||
                        stack.getItem() instanceof Genesis_driver ||
                        stack.getItem() instanceof Mega_uiorder ||
                        stack.getItem() instanceof Two_sidriver ||
                                stack.getItem() instanceof GhostDriver||
                        stack.getItem() instanceof sengokudrivers_epmty
                    )
                )
                .isPresent(); // 如果找到任何一种腰带，返回true
        } catch (Exception e) {
            // 如果Curios API出现任何异常，返回false
            return false;
        }
    }
}
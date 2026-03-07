package com.xiaoshi2022.kamenriderbossyouandme.util;//package com.xiaoxiaodong.dimensionalotherworld.util;
//
//import com.jpigeon.ridebattlelib.core.system.henshin.RiderConfig;
//import net.minecraft.resources.ResourceLocation;
//import net.minecraft.world.entity.player.Player;
//
//public class DebugRiderConfig extends CuriosRiderConfig {
//
//    public DebugRiderConfig(ResourceLocation riderId) {
//        super(riderId);
//    }
//
//    @Override
//    public boolean isEquippedByPlayer(Player player) {
//        boolean result = super.isEquippedByPlayer(player);
//
//        System.out.println("========== 骑士装备检查 ==========");
//        System.out.println("骑士ID: " + this.getRiderId());
//        System.out.println("玩家: " + player.getName().getString());
//        System.out.println("驱动器物品: " + this.getDriverItem().toString());  // 替代getRegistryName()
//        System.out.println("驱动器槽位: " + this.getDriverSlot());
//        System.out.println("辅助驱动器物品: " +
//                (this.getAuxDriverItem() != null ? this.getAuxDriverItem().toString() : "无"));
//        System.out.println("检查结果: " + result);
//        System.out.println("==================================");
//
//        return result;
//    }
//
//    @Override
//    public boolean isAuxDriverEquippedByPlayer(Player player) {
//        boolean result = super.isAuxDriverEquippedByPlayer(player);
//
//        if (this.getAuxDriverItem() != null) {
//            System.out.println("辅助驱动器检查 - 结果: " + result);
//        }
//
//        return result;
//    }
//}
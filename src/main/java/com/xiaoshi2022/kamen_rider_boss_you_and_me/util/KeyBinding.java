package com.xiaoshi2022.kamen_rider_boss_you_and_me.util;

import com.mojang.blaze3d.platform.InputConstants;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.Genesis_driver;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.darkKiva.DarkKivaItem;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.rider_barons.rider_baronsItem;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.Superpower.CherryEnergyArrowPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.PacketHandler;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.SummonDukeKnightPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.Superpower.DarkKivaBloodSuckPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.Superpower.DarkKivaSonicBlastPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.Superpower.DarkKivaFuuinKekkaiPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.Superpower.DarkKivaToggleFlightPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.Superpower.DarkKivaSealBarrierPullPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.Superpower.DukeCombatAnalysisPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.Superpower.BaronBananaEnergyPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.BaronLemonAbilityPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.baron_lemons.baron_lemonItem;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.dark_orangels.Dark_orangels;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.Superpower.DarkGaimKickEnhancePacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.Superpower.DarkGaimBlindnessFieldPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.Superpower.DarkGaimHelheimCrackPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.TempRemoveLockSeedPacket;
import com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.sonicarrow;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class KeyBinding {
    public static final String KEY_CATEGORY_kamen_rider_boss_you_and_me = "KEY.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me";
    public static final String KEY_CHANGE_BODY = "KEY.kamen_rider_weapon_craft.change_body";
    public static final String KEY_CHANGE_PLAYER = "KEY.kamen_rider_weapon_craft.change_player";
    public static final String KEY_RELIEVE_PLAYER = "KEY.kamen_rider_weapon_craft.relieve_player";

    public static final KeyMapping KEY_GUARD  = new KeyMapping("key.skill1",  InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_V, "key.categories.kamen_rider");
    public static final KeyMapping KEY_BLAST  = new KeyMapping("key.skill2",  InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_B, "key.categories.kamen_rider");
    public static final KeyMapping KEY_BOOST  = new KeyMapping("key.skill3",  InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_N, "key.categories.kamen_rider");
    public static final KeyMapping KEY_FLIGHT = new KeyMapping("key.flight_toggle", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_F, "key.categories.kamen_rider");
    public static final KeyMapping KEY_BARRIER_PULL = new KeyMapping("key.barrier_pull", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_Z, "key.categories.kamen_rider");

    public static final KeyMapping CHANGE_KEY = new KeyMapping(KEY_CHANGE_BODY, KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_X, KEY_CATEGORY_kamen_rider_boss_you_and_me);
    public static final KeyMapping CHANGES_KEY = new KeyMapping(KEY_CHANGE_PLAYER, KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, KEY_CATEGORY_kamen_rider_boss_you_and_me);
    public static final KeyMapping RELIEVE_KEY = new KeyMapping(KEY_RELIEVE_PLAYER, KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_C, KEY_CATEGORY_kamen_rider_boss_you_and_me);

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        
        // 直接在客户端检测玩家是否穿着全套黑暗Kiva盔甲
        boolean isDarkKiva = isDarkKivaArmorEquipped(mc.player);
        
        // 检测玩家是否穿着全套Duke盔甲
        boolean isDuke = isDukeArmorEquipped(mc.player);
        
        // 检测玩家是否穿着基础巴隆盔甲
        boolean isBaron = isBaronArmorEquipped(mc.player);
        
        // 检测玩家是否穿着巴隆柠檬形态盔甲
        boolean isBaronLemon = isBaronLemonArmorEquipped(mc.player);
        
        // 检测玩家是否穿着黑暗铠武阵羽柠檬盔甲
        boolean isDarkGaim = isDarkGaimArmorEquipped(mc.player);
        
        // 检测玩家是否手持音速弓柠檬模式
        boolean hasSonicArrowLemonMode = hasSonicArrowLemonMode(mc.player);
        // 检测玩家是否手持音速弓樱桃模式
        boolean hasSonicArrowCherryMode = hasSonicArrowCherryMode(mc.player);
        
    // Z键：优先检查创世纪驱动器临时取下锁种功能，然后是结界拉扯功能
        if (KEY_BARRIER_PULL.consumeClick()) {
            // 检查玩家是否装备了创世纪驱动器
            boolean hasGenesisDriver = hasGenesisDriver(mc.player);
            if (hasGenesisDriver) {
                // 发送临时取下锁种的数据包
                PacketHandler.sendToServer(new TempRemoveLockSeedPacket());
            } else {
                // 没有创世纪驱动器时，执行原来的结界拉扯功能
                PacketHandler.sendToServer(new DarkKivaSealBarrierPullPacket());
            }
        }
        
        // 优先检查特殊形态 - 樱桃能量箭矢（任何穿着骑士腰带的玩家都可以使用，只要手持音速弓樱桃模式）
        if (hasSonicArrowCherryMode) {
            // 手持音速弓樱桃模式时，使用V键触发樱桃能量箭矢技能
            // 在 KeyBinding 类中修改调用方式
            if (KEY_GUARD.consumeClick()) {
                PacketHandler.sendToServer(new CherryEnergyArrowPacket());
                return;
            }
        }
        // 其次检查巴隆柠檬形态
        else if (isBaronLemon && hasSonicArrowLemonMode) {
            // 巴隆柠檬形态且手持音速弓柠檬模式时，使用B键触发柠檬能量陷阱技能
            if (KEY_BLAST.consumeClick()) {
                PacketHandler.sendToServer(new BaronLemonAbilityPacket());
                return; // 处理完后直接返回，避免其他技能干扰
            }
        } 
        // 然后检查其他主要形态
        else if (isDarkGaim) {
            // 黑暗铠武阵羽柠檬形态的三个技能
            if (KEY_GUARD.consumeClick()) PacketHandler.sendToServer(new DarkGaimKickEnhancePacket());
            if (KEY_BLAST.consumeClick()) PacketHandler.sendToServer(new DarkGaimBlindnessFieldPacket());
            if (KEY_BOOST.consumeClick()) PacketHandler.sendToServer(new DarkGaimHelheimCrackPacket());
        }
        else if (isDarkKiva) {
            // 技能1键（V键）：封印结界
            if (KEY_GUARD.consumeClick()) PacketHandler.sendToServer(new DarkKivaFuuinKekkaiPacket());
            // 技能2键（B键）：吸血能力
            if (KEY_BLAST.consumeClick()) PacketHandler.sendToServer(new DarkKivaBloodSuckPacket());
            // 技能3键（N键）：声波爆破
            if (KEY_BOOST.consumeClick()) PacketHandler.sendToServer(new DarkKivaSonicBlastPacket());
            // F键：飞行开关
            if (KEY_FLIGHT.consumeClick()) PacketHandler.sendToServer(new DarkKivaToggleFlightPacket());
        } else if (isDuke) {
            // Duke盔甲使用N键召唤骑士
            if (KEY_BOOST.consumeClick()) {
                //发包生成
                PacketHandler.sendToServer(new SummonDukeKnightPacket());
            }
            // Duke盔甲使用B键激活战斗数据分析
            if (KEY_BLAST.consumeClick()) {
                PacketHandler.sendToServer(new DukeCombatAnalysisPacket());
            }
        } else if (isBaron) {
            // 基础巴隆盔甲使用V键触发香蕉能量技能
            if (KEY_GUARD.consumeClick()) {
                PacketHandler.sendToServer(new BaronBananaEnergyPacket());
            }
        } else {
            // 基础形态下的技能已被移除
        }
    }
    
    // 检测玩家是否穿着全套Duke盔甲
    private static boolean isDukeArmorEquipped(Player player) {
        return player.getInventory().armor.get(3).getItem() instanceof com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.duke.Duke &&
               player.getInventory().armor.get(2).getItem() instanceof com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.duke.Duke &&
               player.getInventory().armor.get(1).getItem() instanceof com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.duke.Duke;
    }
    
    // 客户端专用的黑暗Kiva盔甲检测方法
    private static boolean isDarkKivaArmorEquipped(LocalPlayer player) {
        // 检查是否穿着黑暗Kiva头盔、胸甲和护腿（不需要鞋子）
        return player.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof DarkKivaItem &&
               player.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof DarkKivaItem &&
               player.getItemBySlot(EquipmentSlot.LEGS).getItem() instanceof DarkKivaItem;
    }
    
    // 客户端专用的基础巴隆盔甲检测方法
    private static boolean isBaronArmorEquipped(LocalPlayer player) {
        // 检查胸甲是否为基础巴隆盔甲
        return player.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof rider_baronsItem;
    }
    
    // 客户端专用的巴隆柠檬形态盔甲检测方法
    private static boolean isBaronLemonArmorEquipped(LocalPlayer player) {
        // 检查是否穿着巴隆柠檬形态的全套盔甲
        return player.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof baron_lemonItem &&
               player.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof baron_lemonItem &&
               player.getItemBySlot(EquipmentSlot.LEGS).getItem() instanceof baron_lemonItem;
    }
    
    // 客户端专用的黑暗铠武阵羽柠檬盔甲检测方法
    private static boolean isDarkGaimArmorEquipped(LocalPlayer player) {
        // 检查是否穿着黑暗铠武阵羽柠檬的全套盔甲
        return player.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof Dark_orangels &&
               player.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof Dark_orangels &&
               player.getItemBySlot(EquipmentSlot.LEGS).getItem() instanceof Dark_orangels;
    }
    
    // 检查玩家是否手持音速弓柠檬模式
    private static boolean hasSonicArrowLemonMode(LocalPlayer player) {
        // 获取玩家主手物品
        net.minecraft.world.item.ItemStack mainHandItem = player.getMainHandItem();
        
        // 检查是否为音速弓且处于柠檬模式
        if (mainHandItem.getItem() instanceof sonicarrow) {
            sonicarrow sonicArrow = (sonicarrow) mainHandItem.getItem();
            return sonicArrow.getCurrentMode(mainHandItem) == sonicarrow.Mode.LEMON;
        }
        
        return false;
    }
    
    // 检查玩家是否手持音速弓樱桃模式
    private static boolean hasSonicArrowCherryMode(LocalPlayer player) {
        // 获取玩家主手物品
        net.minecraft.world.item.ItemStack mainHandItem = player.getMainHandItem();
        
        // 检查是否为音速弓且处于樱桃模式
        if (mainHandItem.getItem() instanceof sonicarrow) {
            sonicarrow sonicArrow = (sonicarrow) mainHandItem.getItem();
            return sonicArrow.getCurrentMode(mainHandItem) == sonicarrow.Mode.CHERRY;
        }
        
        return false;
    }
    
    // 检查玩家是否装备了创世纪驱动器
    private static boolean hasGenesisDriver(Player player) {
        return top.theillusivec4.curios.api.CuriosApi.getCuriosInventory(player)
                .resolve()
                .flatMap(inv -> inv.findFirstCurio(stack -> stack.getItem() instanceof Genesis_driver))
                .isPresent();
    }
}

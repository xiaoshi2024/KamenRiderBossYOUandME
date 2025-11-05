package com.xiaoshi2022.kamen_rider_boss_you_and_me.util;

import com.mojang.blaze3d.platform.InputConstants;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.BatStampItem;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.property.Necrom_eye;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.Genesis_driver;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.baron_lemons.baron_lemonItem;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.darkKiva.DarkKivaItem;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.dark_orangels.Dark_orangels;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.evilbats.EvilBatsArmor;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.marika.Marika;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.rider_barons.rider_baronsItem;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.tyrant.TyrantItem;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.*;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.Superpower.*;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.ghosteye.GhostEyeInvisibilityPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.ghosteye.GhostEyeRevertPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.ghosteye.GhostEyeTransformPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.henshin.DarkGhostTransformationRequestPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.henshin.DarkGhostLightningAttackPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.henshin.DarkGhostTeleportPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.GhostDriver;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.marika.MarikaSensoryEnhancementPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.tyrant.TyrantIntangibilityTogglePacket;
import com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.sonicarrow;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

import java.util.Optional;

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

    // 存储正在隐身的眼魔玩家
    private static final java.util.Set<String> invisibleGhostEyePlayers = new java.util.HashSet<>();
    
    // 存储玩家的维度传送冷却时间
    private static final java.util.Map<String, Integer> teleportCooldowns = new java.util.HashMap<>();
    private static final int TELEPORT_COOLDOWN_TICKS = 20 * 5; // 5秒冷却时间（20刻/秒）
    
    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        
        // 检测黑暗Ghost变身条件
        if (CHANGE_KEY.isDown()) {
            LocalPlayer player = mc.player;
            
            // 检查是否装备了魂灵驱动器（腰带如果有眼魂就不需要检测手持）
            if (isGhostDriverEquipped(player)) {
                // 发送黑暗Ghost变身请求
                PacketHandler.sendToServer(new DarkGhostTransformationRequestPacket());
            }
        }
        
        // 检测解除变身条件
        if (RELIEVE_KEY.isDown()) {
            LocalPlayer player = mc.player;
            
            // 检查是否装备了魂灵驱动器
            if (isGhostDriverEquipped(player)) {
                // 发送解除变身请求，设置isRelease=true
                PacketHandler.sendToServer(new DarkGhostTransformationRequestPacket(true));
            }
        }
        
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
        
        // 检测玩家是否穿着玛丽卡盔甲
        boolean isMarika = isMarikaArmorEquipped(mc.player);
        
        // 检测玩家是否穿着火龙果盔甲
        boolean isTyrant = isTyrantArmorEquipped(mc.player);
        
        // 检测玩家是否穿着全套EvilBats盔甲
        boolean isEvilBats = isEvilBatsArmorEquipped(mc.player);
        
        // 检查玩家是否拥有Overlord标签
        boolean isOverlord = false;
        try {
            isOverlord = mc.player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new KRBVariables.PlayerVariables()).isOverlord;
        } catch (Exception e) {
            // 处理可能的异常
        }
        
        // 检查玩家是否是眼魔
        boolean isGhostEye = isGhostEyePlayer(mc.player);
        
        // 同时从持久化变量检查眼魔状态，确保即使没有效果也能检测到
        try {
            boolean hasGhostEyeFlag = mc.player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new KRBVariables.PlayerVariables()).isGhostEye;
            if (hasGhostEyeFlag) {
                isGhostEye = true;
            }
        } catch (Exception e) {
            // 处理可能的异常
        }
        
        // 检查玩家是否变身为假面骑士幽冥
        boolean isNecromTransformed = false;
        try {
            isNecromTransformed = mc.player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new KRBVariables.PlayerVariables()).isMegaUiorderTransformed;
        } catch (Exception e) {
            // 处理可能的异常
        }
        
        // 检查玩家是否变身为DarkGhost形态
        boolean isDarkGhostTransformed = isDarkGhostTransformed(mc.player);
        
        // 检查玩家是否手持眼魂并按下Shift+V键（优先处理维度传送功能）
        if (hasShiftDown() && KEY_GUARD.isDown() && isHoldingGhostEye(mc.player)) {
            if (KEY_GUARD.consumeClick()) {
                String playerId = mc.player.getStringUUID();
                // 检查冷却时间
                if (!isTeleportOnCooldown(playerId)) {
                    // 发送数据包到服务器执行维度传送，isTeleport=true
                    PacketHandler.sendToServer(new GhostEyeDimensionTeleportPacket(true));
                    // 设置冷却时间
                    setTeleportCooldown(playerId, TELEPORT_COOLDOWN_TICKS);
                    return; // 优先处理维度传送功能，不继续执行其他按键检测
                } else {
                    // 显示冷却提示
                    int remainingSeconds = teleportCooldowns.getOrDefault(playerId, 0) / 20;
                    mc.player.displayClientMessage(net.minecraft.network.chat.Component.literal("传送冷却中，还剩 " + remainingSeconds + " 秒"), true);
                }
            }
        }
        
        // 检查玩家是否手持眼魂并按下V键，在主世界也能获得眼魂种族
        if (KEY_GUARD.isDown() && isHoldingGhostEye(mc.player)) {
            if (KEY_GUARD.consumeClick()) {
                // 发送数据包给服务器将玩家设置为眼魔种族，isTeleport=false
                PacketHandler.sendToServer(new GhostEyeDimensionTeleportPacket(false));
                mc.player.displayClientMessage(net.minecraft.network.chat.Component.literal("已获得眼魂种族！"), true);
                return;
            }
        }
        
        // 手持眼魂或变身为幽冥时按B键隐身功能 - 提高优先级，避免与其他技能冲突
        if ((isHoldingGhostEye(mc.player) || isNecromTransformed) && KEY_BLAST.isDown()) {
            if (KEY_BLAST.consumeClick()) {
                // 检查是否按下Shift键
                if (hasShiftDown() && mc.player.hasEffect(MobEffects.INVISIBILITY)) {
                    // Shift+B键：解除隐身效果
                    PacketHandler.sendToServer(new GhostEyeInvisibilityPacket(false));
                    mc.player.displayClientMessage(net.minecraft.network.chat.Component.literal("隐身效果已解除！"), true);
                } else if (!mc.player.hasEffect(MobEffects.INVISIBILITY)) {
                    // B键：添加隐身效果
                    PacketHandler.sendToServer(new GhostEyeInvisibilityPacket(true));
                    mc.player.displayClientMessage(net.minecraft.network.chat.Component.literal("获得隐身效果！"), true);
                }
                return; // 优先处理眼魂隐身功能，不继续执行其他按键检测
            }
        }
        
        // 眼魔玩家或幽冥玩家专属功能：按下B键变身为眼魂实体，按下Shift+B键变回到人形
        if ((isGhostEye || isNecromTransformed)) {
            // 执行变形功能，无论玩家是否手持眼魂
            if (KEY_BLAST.consumeClick()) {
                if (hasShiftDown()) {
                    // Shift+B键：变回到人形
                    PacketHandler.sendToServer(new GhostEyeRevertPacket());
                    mc.player.displayClientMessage(net.minecraft.network.chat.Component.literal("已变回人形"), true);
                } else {
                    // B键：变身为眼魂实体
                    PacketHandler.sendToServer(new GhostEyeTransformPacket());
                    mc.player.displayClientMessage(net.minecraft.network.chat.Component.literal("已变身为眼魂实体"), true);
                }
                return; // 优先处理眼魔变形功能，不继续执行其他按键检测
            }
        }
        
        // 检查DarkGhost形态的技能
        if (isDarkGhostTransformed) {
            // V键：闪电格斗攻击
            if (KEY_GUARD.consumeClick()) {
                PacketHandler.sendToServer(new DarkGhostLightningAttackPacket());
                return;
            }
            // N键：短距离瞬移
            if (KEY_BOOST.consumeClick()) {
                PacketHandler.sendToServer(new DarkGhostTeleportPacket());
                return;
            }
        }
        
        // 检查Shift+N组合键用于召回异域者（任何拥有Overlord标签的玩家都可以使用）
        if (isOverlord && hasShiftDown() && KEY_BOOST.consumeClick()) {
            PacketHandler.sendToServer(new BaronRecallInvesPacket());
            return; // 优先处理召回功能，不继续执行其他按键检测
        }
        
        // Z键：优先检查创世纪驱动器临时取下锁种功能，然后是EvilBats临时取下印章功能，再是临时取下眼魂功能，最后是结界拉扯功能
        if (KEY_BARRIER_PULL.consumeClick()) {
            // 检查玩家是否装备了创世纪驱动器
            boolean hasGenesisDriver = hasGenesisDriver(mc.player);
            if (hasGenesisDriver) {
                // 发送临时取下锁种的数据包
                PacketHandler.sendToServer(new TempRemoveLockSeedPacket());
            } else if (isEvilBats) {
                // 发送临时取下蝙蝠印章的数据包
                PacketHandler.sendToServer(new TempRemoveBatStampPacket());
            } else if (isHoldingGhostEye(mc.player) || hasMegaUiorderWithNecromEyeMode(mc.player)) {
                // 检查玩家是否手持眼魂或装备了处于NECROM_EYE模式的Mega_uiorder
                // 发送临时取下眼魂的数据包
                PacketHandler.sendToServer(new TempRemoveNecromEyePacket());
                mc.player.displayClientMessage(net.minecraft.network.chat.Component.literal("眼魂已临时取下！"), true);
            } else {
                // 没有创世纪驱动器时，执行原来的结界拉扯功能
                PacketHandler.sendToServer(new DarkKivaSealBarrierPullPacket());
            }
        }
        
        // 检测玩家是否手持音速弓柠檬模式
        boolean hasSonicArrowLemonMode = hasSonicArrowLemonMode(mc.player);
        // 检测玩家是否手持音速弓樱桃模式
        boolean hasSonicArrowCherryMode = hasSonicArrowCherryMode(mc.player);
        
        // 优先检查特殊形态 - 樱桃能量箭矢（任何穿着骑士腰带的玩家都可以使用，只要手持音速弓樱桃模式）
        if (hasSonicArrowCherryMode) {
            // 手持音速弓樱桃模式时，使用V键触发樱桃能量箭矢技能
            if (KEY_GUARD.consumeClick()) {
                PacketHandler.sendToServer(new CherryEnergyArrowPacket());
                return;
            }
        }
        // 其次检查是否有Overlord标签并按下B键（骑士2技能）
        else if (isOverlord && KEY_BLAST.isDown()) {
            // 只有巴隆形态和人类形态的Overlord可以使用B键触发藤蔓技能
            if (isBaron || isBaronLemon || (!isDarkKiva && !isDuke && !isDarkGaim && !isMarika && !isTyrant)) {
                KEY_BLAST.consumeClick(); // 只在确认要使用时才消耗按键事件
                PacketHandler.sendToServer(new OverlordVineSkillPacket());
                return;
            }
        }
        // 然后检查巴隆柠檬形态
        else if (isBaronLemon) {
            // 巴隆柠檬形态使用V键触发柠檬能量陷阱技能
            if (KEY_GUARD.consumeClick()) {
                PacketHandler.sendToServer(new BaronLemonAbilityPacket());
            }
            // 巴隆柠檬形态使用N键触发召唤异域者技能
            if (KEY_BOOST.consumeClick()) {
                PacketHandler.sendToServer(new BaronSummonInvesPacket());
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
            // 基础巴隆盔甲使用N键触发召唤异域者技能
            if (KEY_BOOST.consumeClick()) {
                PacketHandler.sendToServer(new BaronSummonInvesPacket());
            }
        } else if (isMarika) {
            // 玛丽卡盔甲使用V键触发感官加强技能
            if (KEY_GUARD.consumeClick()) {
                PacketHandler.sendToServer(new MarikaSensoryEnhancementPacket());
            }
        } else if (isTyrant) {
            // 火龙果盔甲使用B键触发虚化技能
            if (KEY_BLAST.consumeClick()) {
                PacketHandler.sendToServer(new TyrantIntangibilityTogglePacket());
            }
        } else {
            // 基础形态下的技能已被移除
            // 但是如果玩家拥有Overlord标签，允许按下技能3键召唤异域者
            if (isOverlord && KEY_BOOST.consumeClick()) {
                PacketHandler.sendToServer(new BaronSummonInvesPacket());
            }
        }
        
        // 最后检查玩家是否手持蝙蝠印章并按下V键（骑士1键）
        // 这个检查放在最后，这样当玩家穿着特定盔甲时，优先使用盔甲的技能
        if (KEY_GUARD.consumeClick()) {
            if (mc.player.getMainHandItem().getItem() instanceof BatStampItem) {
                PacketHandler.sendToServer(new BatUltrasonicAttackPacket());
            }
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
    
    // 客户端专用的玛丽卡盔甲检测方法
    private static boolean isMarikaArmorEquipped(LocalPlayer player) {
        // 检查是否穿着玛丽卡头盔、胸甲和护腿
        return player.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof Marika &&
               player.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof Marika &&
               player.getItemBySlot(EquipmentSlot.LEGS).getItem() instanceof Marika;
    }
    
    // 客户端专用的火龙果盔甲检测方法
    private static boolean isTyrantArmorEquipped(LocalPlayer player) {
        // 检查是否穿着火龙果头盔、胸甲和护腿
        return player.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof TyrantItem &&
               player.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof TyrantItem &&
               player.getItemBySlot(EquipmentSlot.LEGS).getItem() instanceof TyrantItem;
    }
    
    // 客户端专用的EvilBats盔甲检测方法
    private static boolean isEvilBatsArmorEquipped(LocalPlayer player) {
        // 检查是否穿着EvilBats头盔、胸甲和护腿
        return player.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof EvilBatsArmor &&
               player.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof EvilBatsArmor &&
               player.getItemBySlot(EquipmentSlot.LEGS).getItem() instanceof EvilBatsArmor;
    }
    
    // 检测玩家是否装备了魂灵驱动器
    private static boolean isGhostDriverEquipped(Player player) {
        Optional<SlotResult> opt = CuriosApi.getCuriosInventory(player)
                .resolve()
                .flatMap(inv -> inv.findFirstCurio(s -> s.getItem() instanceof GhostDriver));
        
        return opt.isPresent();
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
    
    // 检测玩家是否装备了处于NECROM_EYE模式的Mega_uiorder
    private static boolean hasMegaUiorderWithNecromEyeMode(Player player) {
        return CuriosApi.getCuriosInventory(player)
                .map(handler -> {
                    // 寻找第一个Mega_uiorder物品
                    Optional<SlotResult> result = handler.findFirstCurio(stack -> stack.getItem() instanceof com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.Mega_uiorder);
                    if (result.isPresent()) {
                        ItemStack stack = result.get().stack();
                        com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.Mega_uiorder belt = 
                                (com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.Mega_uiorder) stack.getItem();
                        return belt.getCurrentMode(stack) == com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.Mega_uiorder.Mode.NECROM_EYE;
                    }
                    return false;
                })
                .orElse(false);
    }
    
    // 检查玩家是否手持眼魂
    private static boolean isHoldingGhostEye(Player player) {
        return player.getMainHandItem().getItem() instanceof Necrom_eye ||
               player.getOffhandItem().getItem() instanceof Necrom_eye;
    }
    
    // 检查是否按下了Shift键
    private static boolean hasShiftDown() {
        Minecraft mc = Minecraft.getInstance();
        return mc.options.keyShift.isDown();
    }
    
    // 检查玩家是否处于传送冷却中
    private static boolean isTeleportOnCooldown(String playerId) {
        Integer cooldown = teleportCooldowns.get(playerId);
        return cooldown != null && cooldown > 0;
    }
    
    // 设置玩家的传送冷却时间
    private static void setTeleportCooldown(String playerId, int ticks) {
        teleportCooldowns.put(playerId, ticks);
    }
    
    // 处理冷却时间减少
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END && Minecraft.getInstance().player != null) {
            // 减少所有玩家的冷却时间
            java.util.Iterator<java.util.Map.Entry<String, Integer>> iterator = teleportCooldowns.entrySet().iterator();
            while (iterator.hasNext()) {
                java.util.Map.Entry<String, Integer> entry = iterator.next();
                int remainingCooldown = entry.getValue() - 1;
                if (remainingCooldown <= 0) {
                    iterator.remove();
                } else {
                    entry.setValue(remainingCooldown);
                }
            }
        }
    }
    
    // 检查玩家是否是眼魔
    private static boolean isGhostEyePlayer(Player player) {
        // 在客户端，我们可以通过检查玩家是否有眼魔特有的效果来判断
        // 注意：这只是一个近似的判断方法，因为服务器才真正存储玩家是否是眼魔
        return player.hasEffect(MobEffects.NIGHT_VISION) && 
               player.hasEffect(MobEffects.MOVEMENT_SPEED) && 
               player.hasEffect(MobEffects.JUMP);
    }
    
    // 检查玩家是否处于DarkGhost形态 - 现在使用盔甲检测代替字段检测
    private static boolean isDarkGhostTransformed(Player player) {
        // 直接调用DarkGhostAbilityHandler中的盔甲检测方法
        return com.xiaoshi2022.kamen_rider_boss_you_and_me.event.Superpower.DarkGhostAbilityHandler.isWearingDarkGhostArmor(player);
    }
    
    // 切换眼魔玩家的隐身状态
    private static void toggleGhostEyeInvisibility(LocalPlayer player) {
        String playerId = player.getStringUUID();
        boolean isCurrentlyInvisible = invisibleGhostEyePlayers.contains(playerId);
        
        if (isCurrentlyInvisible) {
            // 取消隐身
            invisibleGhostEyePlayers.remove(playerId);
            player.removeEffect(MobEffects.INVISIBILITY);
            player.displayClientMessage(net.minecraft.network.chat.Component.literal("已取消隐身状态"), true);
        } else {
            // 开启隐身
            invisibleGhostEyePlayers.add(playerId);
            player.addEffect(new net.minecraft.world.effect.MobEffectInstance(MobEffects.INVISIBILITY, Integer.MAX_VALUE, 0, false, false));
            player.displayClientMessage(net.minecraft.network.chat.Component.literal("已进入隐身状态"), true);
        }
    }
    
    // 处理玩家渲染事件，用于临时关闭幽冥的盔甲渲染
    @SubscribeEvent
    public static void onRenderPlayer(RenderPlayerEvent.Pre event) {
        Player player = event.getEntity();
        String playerId = player.getStringUUID();
        
        // 如果是隐身的眼魔玩家，临时关闭盔甲渲染
        if (invisibleGhostEyePlayers.contains(playerId)) {
            // 直接设置玩家的盔甲物品为空气，这样就不会渲染盔甲
            player.setItemSlot(EquipmentSlot.HEAD, net.minecraft.world.item.ItemStack.EMPTY);
            player.setItemSlot(EquipmentSlot.CHEST, net.minecraft.world.item.ItemStack.EMPTY);
            player.setItemSlot(EquipmentSlot.LEGS, net.minecraft.world.item.ItemStack.EMPTY);
        }
    }
    
    // 渲染事件后恢复盔甲渲染
    @SubscribeEvent
    public static void onRenderPlayerPost(RenderPlayerEvent.Post event) {
        Player player = event.getEntity();
        String playerId = player.getStringUUID();
        
        // 如果是隐身的眼魔玩家，恢复盔甲渲染
        if (invisibleGhostEyePlayers.contains(playerId)) {
            // 这里不需要恢复，因为我们只是临时修改，不会真正改变玩家的物品栏
            // Minecraft会在每一帧重新从玩家的实际物品栏获取盔甲信息
        }
    }
}

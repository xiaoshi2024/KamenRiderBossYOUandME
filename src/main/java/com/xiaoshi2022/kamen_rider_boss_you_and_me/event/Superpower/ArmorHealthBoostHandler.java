package com.xiaoshi2022.kamen_rider_boss_you_and_me.event.Superpower;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.darkKiva.DarkKivaItem;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.dark_orangels.Dark_orangels;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.rider_barons.rider_baronsItem;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.baron_lemons.baron_lemonItem;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.duke.Duke;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.rider_necrom.RidernecromItem;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.tyrant.TyrantItem;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.zangetsu_shin.ZangetsuShinItem;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.sigurd.Sigurd;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.marika.Marika;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.KRBVariables;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = "kamen_rider_boss_you_and_me")
public class ArmorHealthBoostHandler {
    // 定义UUID用于属性修改器（确保每次都是同一个修改器）
    private static final UUID HEALTH_BOOST_MODIFIER_ID = UUID.fromString("8a7e45f2-6b1c-4d93-9a3e-7f8b9c1d2e3f");
    private static final String HEALTH_BOOST_MODIFIER_NAME = "Armor Health Boost";

    // 每套盔甲提供的额外生命值（总共100点，按5件套分配）
    private static final double HEALTH_BOOST_PER_ARMOR = 25.0; // 每件盔甲提供25点额外生命，四件共100点

    /**
     * 每tick检查玩家装备的盔甲情况，并更新生命值上限
     */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Player player = event.player;
    
        // 只在服务器端处理
        if (player.level().isClientSide()) return;
    
        // 获取或创建玩家变量
        KRBVariables.PlayerVariables variables = player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null)
                .orElse(new KRBVariables.PlayerVariables());
    
        // 获取玩家的最大生命值属性
        AttributeInstance maxHealthAttribute = player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH);
        if (maxHealthAttribute == null) return;
    
        // 检查玩家是否有任何自定义盔甲
        int customArmorCount = getCustomArmorCount(player);
    
        // 获取上一次记录的盔甲数量
        int lastArmorCount = variables.lastCustomArmorCount;
    
        // 只有当盔甲数量发生变化时才进行更新
        if (customArmorCount != lastArmorCount) {
            // 移除旧的修改器
            maxHealthAttribute.removeModifier(HEALTH_BOOST_MODIFIER_ID);
    
            // 如果玩家装备了自定义盔甲，添加生命值加成
            if (customArmorCount > 0) {
                double healthBoost = customArmorCount * HEALTH_BOOST_PER_ARMOR;
    
                // 添加修改器来增加最大生命值
                maxHealthAttribute.addTransientModifier(
                        new AttributeModifier(
                                HEALTH_BOOST_MODIFIER_ID,
                                HEALTH_BOOST_MODIFIER_NAME,
                                healthBoost,
                                AttributeModifier.Operation.ADDITION
                        )
                );
    
                // 确保当前生命值也相应调整
                player.setHealth(player.getMaxHealth());
            } else {
                // 如果玩家没有装备任何自定义盔甲，恢复到基础生命值
                double baseHealth = variables.baseMaxHealth > 0 ? variables.baseMaxHealth : 20.0D; // 默认20点生命值
                
                // 保存当前生命值百分比
                float healthPercentage = player.getHealth() / player.getMaxHealth();
                
                // 恢复基础生命值
                maxHealthAttribute.setBaseValue(baseHealth);
                
                // 按比例调整当前生命值
                float newHealth = (float) (baseHealth * healthPercentage);
                player.setHealth(Math.max(newHealth, 1.0F)); // 确保至少保留1点生命值
            }
            
            // 记录当前盔甲数量
            variables.lastCustomArmorCount = customArmorCount;
            
            // 同步变量
            variables.syncPlayerVariables(player);
        }
    }

    /**
     * 计算玩家装备的自定义盔甲数量
     */
    private static int getCustomArmorCount(Player player) {
        int count = 0;

        // 检查所有盔甲槽位
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() == EquipmentSlot.Type.ARMOR) {
                ItemStack armorStack = player.getItemBySlot(slot);
                if (isCustomArmor(armorStack)) {
                    count++;
                }
            }
        }

        return count;
    }

    /**
     * 检查物品是否为自定义盔甲
     */
    private static boolean isCustomArmor(ItemStack stack) {
        if (stack.isEmpty()) return false;

        // 检查是否为任何一种自定义盔甲
        return stack.getItem() instanceof rider_baronsItem ||
                stack.getItem() instanceof baron_lemonItem ||
                stack.getItem() instanceof Duke ||
                stack.getItem() instanceof RidernecromItem ||
                stack.getItem() instanceof ZangetsuShinItem ||
                stack.getItem() instanceof Sigurd ||
                stack.getItem() instanceof TyrantItem ||
                stack.getItem() instanceof Dark_orangels ||
                stack.getItem() instanceof DarkKivaItem ||
                stack.getItem() instanceof Marika;
    }

    // 在类中添加一个新的事件订阅方法来处理玩家登录事件
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide()) {
            KRBVariables.PlayerVariables variables = player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null)
                    .orElse(new KRBVariables.PlayerVariables());
    
            // 初始化基础生命值为玩家当前的最大生命值
            AttributeInstance maxHealthAttribute = player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH);
            if (maxHealthAttribute != null) {
                variables.baseMaxHealth = maxHealthAttribute.getBaseValue();
            }
            
            // 同步变量，确保所有状态都被正确加载
            variables.syncPlayerVariables(player);
        }
    }
}
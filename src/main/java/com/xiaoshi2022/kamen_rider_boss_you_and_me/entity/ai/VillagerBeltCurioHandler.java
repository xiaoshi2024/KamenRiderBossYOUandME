package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.ai;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.curio.BeltCurioIntegration;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.Genesis_driver;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModBossSounds;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

// 说明：
// 1. Curio菜单可以通过玩家与村民交互打开：当玩家空手右击MCA村民时，会触发腰带取出逻辑
// 2. 模型恢复：当腰带从Curio槽位移除时，会自动移除村民的所有盔甲，恢复原始模型

@Mod.EventBusSubscriber(modid = "kamen_rider_boss_you_and_me")
public class VillagerBeltCurioHandler {
    
    // 处理村民装备变化事件
    @SubscribeEvent
    public static void onVillagerEquipmentChange(LivingEquipmentChangeEvent event) {
        LivingEntity entity = event.getEntity();
        
        // 检查是否是MCA村民
        if (isMCAVillager(entity)) {
            // 检查主手是否持有腰带
            ItemStack mainHandStack = entity.getMainHandItem();
            if (mainHandStack.getItem() instanceof Genesis_driver) {
                // 尝试将腰带到Curio槽位
                BeltCurioIntegration.tryEquipBeltToCurioSlot(entity, mainHandStack);
            }
        }
    }
    
    // 定期检查村民的腰带装备状态
    @SubscribeEvent
    public static void onVillagerTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        
        if (isMCAVillager(entity) && entity.tickCount % 20 == 0) { // 每1秒检查一次
            // 如果村民主手有腰带但Curio槽位没有，尝试装备到Curio槽位
            ItemStack mainHandStack = entity.getMainHandItem();
            if (mainHandStack.getItem() instanceof Genesis_driver && 
                !BeltCurioIntegration.hasBeltInCurioSlot(entity)) {
                BeltCurioIntegration.tryEquipBeltToCurioSlot(entity, mainHandStack);
            }
        }
    }
    
    // 处理玩家与村民的交互，允许玩家从Curio槽位取出腰带
    @SubscribeEvent
    public static void onPlayerInteractEntity(PlayerInteractEvent.EntityInteract event) {
        Player player = event.getEntity();
        Entity target = event.getTarget();
        
        // 检查目标是否是MCA村民且玩家空手
        if (isMCAVillager(target) && player.getItemInHand(event.getHand()).isEmpty()) {
            LivingEntity villager = (LivingEntity) target;
            
            // 尝试从村民的Curio槽位取出腰带
            BeltCurioIntegration.removeBeltFromCurioSlot(villager).ifPresent(beltStack -> {
                // 将腰带给予玩家
                if (!player.addItem(beltStack)) {
                    // 如果玩家物品栏满了，将腰带掉落
                    villager.spawnAtLocation(beltStack);
                }
                
                // 播放解除变身音效
                if (!villager.level().isClientSide) {
                    villager.level().playSound(
                        null,
                        villager.getX(),
                        villager.getY(),
                        villager.getZ(),
                        ModBossSounds.LOCKOFF.get(),
                        SoundSource.PLAYERS,
                        1.0F,
                        1.0F
                    );
                }
                
                // 移除村民的盔甲（解除变身）
                removeAllArmor(villager);
                
                // 取消事件，避免其他交互
                event.setCanceled(true);
            });
        }
    }
    
    // 检查实体是否是MCA村民
    private static boolean isMCAVillager(Entity entity) {
        if (!(entity instanceof LivingEntity)) return false;
        
        // 通过类名检查是否是MCA村民
        String className = entity.getClass().getName();
        return className.contains("VillagerEntityMCA") || 
               className.contains("mca") && className.contains("villager");
    }
    
    // 移除实体的所有盔甲
    private static void removeAllArmor(LivingEntity entity) {
        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS}) {
            if (!entity.getItemBySlot(slot).isEmpty()) {
                entity.setItemSlot(slot, ItemStack.EMPTY);
            }
        }
    }
}
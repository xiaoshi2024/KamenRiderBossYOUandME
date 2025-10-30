package com.xiaoshi2022.kamen_rider_boss_you_and_me.curio;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.Genesis_driver;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me.MODID;

@Mod.EventBusSubscriber(modid = MODID)
public class BeltAutoEquipHandler {

    // 实体装备冷却管理
    private static final Map<Integer, Integer> EQUIP_COOLDOWN = new HashMap<>();
    private static final Random RANDOM = new Random();
    private static final int COOLDOWN_TICKS = 20; // 1秒冷却

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();

        // 仅处理僵尸类型和MCA村民
        if (!BeltCurioIntegration.canEquipToCurioSlot(entity)) {
            return;
        }

        int entityId = entity.getId();

        // 检查冷却时间
        if (EQUIP_COOLDOWN.containsKey(entityId) && EQUIP_COOLDOWN.get(entityId) > 0) {
            EQUIP_COOLDOWN.put(entityId, EQUIP_COOLDOWN.get(entityId) - 1);
            return;
        }

        // 检查手中是否有腰带
        if (hasBeltInHand(entity)) {
            handleBeltEquipping(entity);
            // 设置冷却
            EQUIP_COOLDOWN.put(entityId, COOLDOWN_TICKS);
        }
    }

    /**
     * 检查实体手中是否有腰带
     */
    private static boolean hasBeltInHand(LivingEntity entity) {
        return isBelt(entity.getMainHandItem()) || isBelt(entity.getOffhandItem());
    }

    /**
     * 检查物品是否是腰带
     */
    private static boolean isBelt(ItemStack stack) {
        // 实现腰带物品的判断逻辑
        // 修复：使用正确的类路径
        return stack.getItem() instanceof Genesis_driver;
    }

    /**
     * 处理腰带装备逻辑
     */
    private static void handleBeltEquipping(LivingEntity entity) {
        // 检查是否已经装备了腰带
        if (BeltCurioIntegration.isCurioEquipped(entity, BeltCurioIntegration.BELT_SLOT_TYPE)) {
            return;
        }

        // 获取手中的腰带
        ItemStack beltStack = ItemStack.EMPTY;
        if (isBelt(entity.getMainHandItem())) {
            beltStack = entity.getMainHandItem();
        } else if (isBelt(entity.getOffhandItem())) {
            beltStack = entity.getOffhandItem();
        }

        if (!beltStack.isEmpty()) {
            // 尝试装备到Curio槽位
            if (BeltCurioIntegration.equipCurio(entity, beltStack.copy(), BeltCurioIntegration.BELT_SLOT_TYPE)) {
                // 播放变身音效
                playTransformationSound(entity, beltStack);

                // 清除手中的腰带
                if (entity.getMainHandItem() == beltStack) {
                    entity.setItemInHand(entity.getUsedItemHand(), ItemStack.EMPTY);
                } else if (entity.getOffhandItem() == beltStack) {
                    entity.getOffhandItem().isEmpty();
                }
            }
        }
    }

    /**
     * 播放变身音效
     */
    private static void playTransformationSound(LivingEntity entity, ItemStack beltStack) {
        // 获取腰带对应的音效
        net.minecraft.sounds.SoundEvent soundEvent = getTransformationSound(beltStack);

        if (soundEvent != null) {
            // 播放音效
            entity.level().playSound(null,
                    entity.blockPosition(),
                    soundEvent,
                    SoundSource.PLAYERS,
                    1.0F,
                    1.0F);
        }
    }

    /**
     * 获取腰带对应的变身音效
     */
    private static net.minecraft.sounds.SoundEvent getTransformationSound(ItemStack beltStack) {
        // 根据不同的腰带类型返回对应的音效
        // 这里需要根据实际的腰带类型和音效注册表进行实现
        // 示例：
        // if (beltStack.getItem() == ModItems.GENESIS_DRIVER.get()) {
        //     return ModSounds.TRANSFORMATION_SOUND.get();
        // }
        return null;
    }
}
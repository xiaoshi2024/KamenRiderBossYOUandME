package com.xiaoshi2022.kamen_rider_boss_you_and_me.curio;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.DrakKivaBelt;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.Genesis_driver;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.Two_sidriver;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.sengokudrivers_epmty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.Optional;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import static com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me.MODID;

@Mod.EventBusSubscriber(modid = MODID)
public class VillagerCurioHandler {

    // 村民状态管理 - 使用ConcurrentHashMap确保线程安全
    private static final Map<Integer, VillagerState> VILLAGER_STATES = new ConcurrentHashMap<>();
    private static final Random RANDOM = new Random();

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();

        // 处理所有村民类型
        if (!BeltCurioIntegration.isVillagerType(entity)) {
            return;
        }

        // 获取或创建村民状态
        int entityId = entity.getId();
        VillagerState state = VILLAGER_STATES.computeIfAbsent(entityId, id -> new VillagerState());

        // 更新状态
        state.tick();

        // 处理村民变身逻辑
        handleVillagerTransformation(entity, state);

        // 清理已死亡的村民状态
        if (!entity.isAlive()) {
            VILLAGER_STATES.remove(entityId);
        }
    }

    /**
     * 处理村民的变身逻辑
     */
    private static void handleVillagerTransformation(LivingEntity villager, VillagerState state) {
        // 检查村民是否携带腰带
        ItemStack beltStack = getBeltFromVillager(villager);
        if (beltStack.isEmpty()) {
            return;
        }

        // 检查是否处于危险中
        boolean isInDanger = isVillagerInDanger(villager);

        // 检查是否处于变身状态
        boolean isTransformed = BeltCurioIntegration.isCurioEquipped(villager, BeltCurioIntegration.BELT_SLOT_TYPE);

        // 逻辑处理
        if (isInDanger && !isTransformed && state.canTransform()) {
            // 遇到危险且未变身，且冷却时间已过 -> 变身
            transformVillager(villager, beltStack);
            state.onTransform();
        } else if (!isInDanger && isTransformed && state.canRevert()) {
            // 处于安全状态且已变身，且满足解除条件 -> 解除变身
            revertVillagerTransformation(villager);
            state.onRevert();
        }
    }

    /**
     * 检查村民是否处于危险中
     */
    private static boolean isVillagerInDanger(LivingEntity villager) {
        // 检查周围是否有怪物
        double dangerRange = 10.0D;
        return villager.level().getEntitiesOfClass(Monster.class,
                villager.getBoundingBox().inflate(dangerRange),
                monster -> monster.getTarget() == villager || monster.distanceToSqr(villager) < dangerRange * dangerRange).size() > 0;
    }

    /**
     * 获取村民手中或Curio槽位中的腰带
     */
    private static ItemStack getBeltFromVillager(LivingEntity villager) {
        // 首先检查Curio槽位是否已有腰带
        Optional<ItemStack> curioBelt = BeltCurioIntegration.getBeltFromCurioSlot(villager);
        if (curioBelt.isPresent()) {
            return curioBelt.get();
        }
        // 检查主手
        if (isBelt(villager.getMainHandItem())) {
            return villager.getMainHandItem();
        }
        // 检查副手
        if (isBelt(villager.getOffhandItem())) {
            return villager.getOffhandItem();
        }
        return ItemStack.EMPTY;
    }

    /**
     * 检查物品是否是腰带
     */
    private static boolean isBelt(ItemStack stack) {
        // 实现腰带物品的判断逻辑，支持所有腰带类型
        Item item = stack.getItem();
        return item instanceof Genesis_driver ||
               item instanceof sengokudrivers_epmty ||
               item instanceof DrakKivaBelt ||
               item instanceof Two_sidriver;
    }

    /**
     * 村民变身
     */
    private static void transformVillager(LivingEntity villager, ItemStack beltStack) {
        // 检查是否已经在Curio槽位中
        if (BeltCurioIntegration.hasBeltInCurioSlot(villager)) {
            // 已经装备，检查是否已经标记为变身状态
            if (!isVillagerAlreadyTransformed(villager)) {
                // 未标记为变身状态才播放音效
                playTransformationSound(villager);
                // 标记为已变身
                markVillagerAsTransformed(villager);
            }
        } else {
            // 将腰带装备到Curio槽位
            if (BeltCurioIntegration.equipCurio(villager, beltStack.copy(), BeltCurioIntegration.BELT_SLOT_TYPE)) {
                // 检查是否已经标记为变身状态
                if (!isVillagerAlreadyTransformed(villager)) {
                    // 未标记为变身状态才播放音效
                    playTransformationSound(villager);
                    // 标记为已变身
                    markVillagerAsTransformed(villager);
                }

                // 注意：不再清除手中的腰带，这样如果Curio装备失败，村民仍然保留腰带
                // 只有在Curio装备成功后，才可以考虑清除手中的腰带，但为了安全，我们暂时保留
                // 这样玩家给村民的腰带就不会丢失
            }
        }
    }
    
    /**
     * 检查村民是否已经标记为变身状态
     */
    private static boolean isVillagerAlreadyTransformed(LivingEntity villager) {
        return villager.getPersistentData().getBoolean("is_transformed_ridder");
    }
    
    /**
     * 标记村民为已变身状态
     */
    private static void markVillagerAsTransformed(LivingEntity villager) {
        villager.getPersistentData().putBoolean("is_transformed_ridder", true);
    }
    
    /**
     * 清除村民的变身状态标记
     */
    private static void clearVillagerTransformedMark(LivingEntity villager) {
        villager.getPersistentData().remove("is_transformed_ridder");
    }

    /**
     * 村民解除变身
     */
    private static void revertVillagerTransformation(LivingEntity villager) {
        // 保存腰带，不再移除Curio槽位中的腰带
        // 这样村民在解除变身后仍然保留腰带，以便在需要时再次变身
        // BeltCurioIntegration.removeCurio(villager, BeltCurioIntegration.BELT_SLOT_TYPE);

        // 播放解除变身音效
        playRevertSound(villager);
        
        // 清除变身状态标记
        clearVillagerTransformedMark(villager);
    }

    /**
     * 播放变身音效
     */
    private static void playTransformationSound(LivingEntity entity) {
        // 实现变身音效播放逻辑
        // 可以使用entity.level().playSound(null, entity.blockPosition(), soundEvent, SoundSource.PLAYERS, volume, pitch);
    }

    /**
     * 播放解除变身音效
     */
    private static void playRevertSound(LivingEntity entity) {
        // 实现解除变身音效播放逻辑
    }

    /**
     * 村民状态类
     */
    private static class VillagerState {
        private int transformCooldown = 0;
        private int revertCooldown = 0;
        private int dangerTime = 0;
        private static final int TRANSFORM_COOLDOWN = 60; // 3秒
        private static final int REVERT_COOLDOWN = 200; // 10秒
        private static final int DANGER_DETECTION_TIME = 10; // 检测到危险0.5秒后变身

        public void tick() {
            // 更新冷却时间
            if (transformCooldown > 0) {
                transformCooldown--;
            }
            if (revertCooldown > 0) {
                revertCooldown--;
            }
        }

        public boolean canTransform() {
            return transformCooldown <= 0;
        }

        public boolean canRevert() {
            return revertCooldown <= 0 && RANDOM.nextFloat() < 0.05F; // 5%的概率解除变身
        }

        public void onTransform() {
            transformCooldown = TRANSFORM_COOLDOWN;
            dangerTime = 0;
        }

        public void onRevert() {
            revertCooldown = REVERT_COOLDOWN;
        }
    }
}
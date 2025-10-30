package com.xiaoshi2022.kamen_rider_boss_you_and_me.event;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.baron_lemons.baron_lemonItem;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.darkKiva.DarkKivaItem;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.dark_orangels.Dark_orangels;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.duke.Duke;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.evilbats.EvilBatsArmor;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.marika.Marika;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.rider_barons.rider_baronsItem;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.rider_necrom.RidernecromItem;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.sigurd.Sigurd;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.tyrant.TyrantItem;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.zangetsu_shin.ZangetsuShinItem;
import net.minecraft.client.model.EntityModel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "kamen_rider_boss_you_and_me", value = Dist.CLIENT)
public class HideEntityModelWhenWearingTransformationArmor {

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static <T extends LivingEntity, M extends EntityModel<T>> void onRenderLivingEntityPre(RenderLivingEvent.Pre<T, M> event) {
        // 跳过玩家，玩家有专门的处理类
        if (event.getEntity() instanceof Player) {
            return;
        }

        LivingEntity entity = event.getEntity();
        M model = event.getRenderer().getModel();
        
        // 确保模型不为null
        if (model == null) {
            return;
        }

        // 更可靠的NBT标签检测，检查多个可能的标签位置
        boolean hasTransformation = hasTransformationNBT(entity);
        
        // 对于村民和僵尸类型实体，使用更严格的模型隐藏逻辑
        boolean isVillagerOrZombie = isVillagerOrZombieType(entity);
        
        if (hasTransformation || (isVillagerOrZombie && hasRiderArmorEquipped(entity))) {
            // 立即隐藏模型，不依赖EntityModelHider的列表机制
            if (model instanceof net.minecraft.client.model.HumanoidModel<?>) {
                net.minecraft.client.model.HumanoidModel<?> humanoidModel = (net.minecraft.client.model.HumanoidModel<?>) model;
                // 隐藏所有模型部分
                humanoidModel.head.visible = false;
                humanoidModel.body.visible = false;
                humanoidModel.leftArm.visible = false;
                humanoidModel.rightArm.visible = false;
                humanoidModel.leftLeg.visible = false;
                humanoidModel.rightLeg.visible = false;
                
                // 使用反射来隐藏可能的特殊部位，避免类型转换错误
                try {
                    // 尝试隐藏帽子部分
                    java.lang.reflect.Field hatField = humanoidModel.getClass().getDeclaredField("hat");
                    hatField.setAccessible(true);
                    Object hat = hatField.get(humanoidModel);
                    if (hat instanceof net.minecraft.client.model.geom.ModelPart) {
                        ((net.minecraft.client.model.geom.ModelPart)hat).visible = false;
                    }
                } catch (Exception e) {
                    // 忽略反射异常
                }
            }
        } else {
            // 如果没有变身NBT标签，确保模型可见
            if (model instanceof net.minecraft.client.model.HumanoidModel<?>) {
                net.minecraft.client.model.HumanoidModel<?> humanoidModel = (net.minecraft.client.model.HumanoidModel<?>) model;
                // 显示所有模型部分
                humanoidModel.head.visible = true;
                humanoidModel.body.visible = true;
                humanoidModel.leftArm.visible = true;
                humanoidModel.rightArm.visible = true;
                humanoidModel.leftLeg.visible = true;
                humanoidModel.rightLeg.visible = true;
                
                // 使用反射来显示可能的特殊部位
                try {
                    // 尝试显示帽子部分
                    java.lang.reflect.Field hatField = humanoidModel.getClass().getDeclaredField("hat");
                    hatField.setAccessible(true);
                    Object hat = hatField.get(humanoidModel);
                    if (hat instanceof net.minecraft.client.model.geom.ModelPart) {
                        ((net.minecraft.client.model.geom.ModelPart)hat).visible = true;
                    }
                } catch (Exception e) {
                    // 忽略反射异常
                }
            }
            
            // 确保清除隐藏标签
            entity.getPersistentData().remove("hide_original_model");
        }
    }

    /**
     * 检查实体是否带有变身NBT标签或装备了腰带
     * 增强检测逻辑，确保能正确识别各种情况下的腰带装备状态
     */
    private static boolean hasTransformationNBT(LivingEntity entity) {
        if (entity == null) {
            return false;
        }
        
        // 优先检查独立的隐藏模型NBT标签 - 这是最可靠的方式
        boolean hasHideModelTag = entity.getPersistentData().getBoolean("hide_original_model");
        if (hasHideModelTag) {
            return true;
        }
        
        // 检查是否是村民或僵尸类型
        boolean isVillagerOrZombie = isVillagerOrZombieType(entity);
        
        // 对于村民和僵尸，更严格地检查变身状态
        if (isVillagerOrZombie) {
            // 检查变身标记
            boolean isTransformed = entity.getPersistentData().getBoolean("is_transformed_ridder");
            // 检查是否装备了骑士盔甲
            boolean hasRiderArmor = hasRiderArmorEquipped(entity);
            
            // 如果村民或僵尸已变身且装备了盔甲，隐藏原模型
            if (isTransformed && hasRiderArmor) {
                // 同时更新hide_original_model标签，确保下次正确识别
                entity.getPersistentData().putBoolean("hide_original_model", true);
                return true;
            }
        }
        
        // 保留原有的变身状态标签检查作为后备
        boolean hasPersistentNBT = entity.getPersistentData().getBoolean("is_transformed_ridder");
        
        // 额外检查是否持有腰带（作为备用检测方式）
        boolean hasDriverInHand = false;
        ItemStack mainHandStack = entity.getMainHandItem();
        if (mainHandStack.getItem() instanceof com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.Genesis_driver) {
            hasDriverInHand = true;
        }
        
        // 检查Curio槽是否装备了腰带
        boolean hasDriverInCurio = false;
        try {
            // 使用反射或直接调用检查Curio槽，避免依赖BeltCurioIntegration类
            // 检查实体是否有Curio功能（通过检查特定的访问器方法）
            // 这里使用try-catch块确保即使Curios API不可用也不会崩溃
            hasDriverInCurio = checkCurioSlots(entity);
        } catch (Exception e) {
            // 忽略Curio检查的异常，仅依赖其他检测方法
        }
        
        // 返回任一条件满足
        return hasPersistentNBT || hasDriverInHand || hasDriverInCurio;
    }
    
    /**
     * 检查实体是否为村民或僵尸类型
     */
    private static boolean isVillagerOrZombieType(LivingEntity entity) {
        // 使用安全的方式检查实体类型
        String entityName = entity.getClass().getSimpleName().toLowerCase();
        return entityName.contains("villager") || 
               entityName.contains("zombie") ||
               // 直接检查是否为MCA村民
               entity.getClass().getName().contains("mca.entity.VillagerEntityMCA");
    }
    
    /**
     * 检查实体是否装备了骑士盔甲
     */
    private static boolean hasRiderArmorEquipped(LivingEntity entity) {
        // 检查头盔槽，通常骑士盔甲会先装备头盔
        ItemStack helmet = entity.getItemBySlot(EquipmentSlot.HEAD);
        if (!helmet.isEmpty()) {
            Item item = helmet.getItem();
            // 检查是否为特定的骑士盔甲
            return item instanceof ZangetsuShinItem ||
                   item instanceof Duke ||
                   item instanceof Sigurd ||
                   item instanceof Marika ||
                   item instanceof TyrantItem ||
                   item instanceof rider_baronsItem ||
                   item instanceof baron_lemonItem ||
                   item instanceof DarkKivaItem ||
                   item instanceof RidernecromItem ||
                   item instanceof Dark_orangels ||
                   item instanceof EvilBatsArmor;
        }
        return false;
    }
    
    /**
     * 检查实体的Curio槽是否装备了腰带
     */
    private static boolean checkCurioSlots(LivingEntity entity) {
        // 直接检查实体是否有腰带相关物品在Curio槽中
        // 这里使用更通用的方法，检查实体的所有装备槽
        // 除了主手外，也检查副手和可能的Curio槽
        
        // 检查副手
        ItemStack offHandStack = entity.getOffhandItem();
        if (offHandStack.getItem() instanceof com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.Genesis_driver) {
            return true;
        }
        
        // 检查所有装备槽
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack stack = entity.getItemBySlot(slot);
            if (!stack.isEmpty() && stack.getItem() instanceof com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.Genesis_driver) {
                return true;
            }
        }
        
        // 如果所有检查都失败，返回false
        return false;
    }
    
    // 计数器，用于定期清理不再有效的实体引用
    private static int cleanupCounter = 0;
    private static final int CLEANUP_INTERVAL = 60; // 每60个tick清理一次（约3秒）
    
    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            // 增加计数器并在达到间隔时清理
            cleanupCounter++;
            if (cleanupCounter >= CLEANUP_INTERVAL) {
                EntityModelHider.cleanup();
                cleanupCounter = 0;
            }
        }
    }
}
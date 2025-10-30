package com.xiaoshi2022.kamen_rider_boss_you_and_me.event;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import java.util.HashSet;
import java.util.Set;

/**
 * 实体模型隐藏处理器
 * 用于针对单个实体对象处理模型隐藏逻辑
 */
@OnlyIn(Dist.CLIENT)
public class EntityModelHider {
    // 存储需要隐藏模型的实体对象
    private static final Set<LivingEntity> entitiesToHideModel = new HashSet<>();
    
    /**
     * 添加实体到隐藏列表
     * @param entity 需要隐藏模型的实体
     */
    public static void addEntityToHideList(LivingEntity entity) {
        if (entity != null) {
            entitiesToHideModel.add(entity);
        }
    }
    
    /**
     * 从隐藏列表中移除实体
     * @param entity 需要显示模型的实体
     */
    public static void removeEntityFromHideList(LivingEntity entity) {
        if (entity != null) {
            entitiesToHideModel.remove(entity);
        }
    }
    
    /**
     * 检查实体是否在隐藏列表中
     * @param entity 要检查的实体
     * @return 是否需要隐藏模型
     */
    public static boolean shouldHideModel(LivingEntity entity) {
        return entity != null && entitiesToHideModel.contains(entity);
    }
    
    /**
     * 隐藏指定实体的模型
     * @param entity 实体
     * @param model 模型
     * @return 是否成功隐藏模型
     */
    public static boolean hideEntityModel(LivingEntity entity, EntityModel<? extends LivingEntity> model) {
        // 添加额外的安全检查
        if (entity == null || model == null) {
            return false;
        }
        
        // 只有在隐藏列表中的实体才应该被隐藏模型
        if (!shouldHideModel(entity)) {
            // 如果实体不在隐藏列表中，确保模型可见（恢复所有部位的可见性）
            if (model instanceof HumanoidModel<?>) {
                HumanoidModel<?> humanoidModel = (HumanoidModel<?>) model;
                showAllModelParts(humanoidModel);
            }
            return false;
        }
        
        if (model instanceof HumanoidModel<?>) {
            HumanoidModel<?> humanoidModel = (HumanoidModel<?>) model;
            
            // 隐藏所有模型部位
            hideAllModelParts(humanoidModel);
            return true;
        }
        
        return false;
    }
    
    /**
     * 隐藏人形模型的所有部位
     * @param model 人形模型
     */
    private static void hideAllModelParts(HumanoidModel<?> model) {
        // 隐藏基本部位
        model.head.visible = false;
        model.body.visible = false;
        model.rightArm.visible = false;
        model.leftArm.visible = false;
        model.rightLeg.visible = false;
        model.leftLeg.visible = false;
        
    }
    
    /**
     * 显示人形模型的所有部位
     * @param model 人形模型
     */
    private static void showAllModelParts(HumanoidModel<?> model) {
        // 显示基本部位
        model.head.visible = true;
        model.body.visible = true;
        model.rightArm.visible = true;
        model.leftArm.visible = true;
        model.rightLeg.visible = true;
        model.leftLeg.visible = true;
    }
    
    /**
     * 清理不再有效的实体引用
     * 防止内存泄漏
     */
    public static void cleanup() {
        entitiesToHideModel.removeIf(entity -> entity == null || entity.isRemoved());
    }
}
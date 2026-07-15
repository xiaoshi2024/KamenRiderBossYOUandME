package com.xiaoshi2022.kamenriderbossyouandme.riders.build;

import com.jpigeon.ridebattlelib.common.config.FormConfig;
import com.jpigeon.ridebattlelib.common.config.RiderConfig;
import com.jpigeon.ridebattlelib.common.config.TriggerType;
import com.jpigeon.ridebattlelib.common.registry.RiderRegistry;
import com.xiaoshi2022.kamenriderbossyouandme.registry.ModItems;
import com.xiaoshi2022.kamenriderbossyouandme.riders.RiderIds;
import com.xiaoshi2022.kamenriderbossyouandme.riders.RiderSkills;
import com.xiaoshi2022.kamenriderbossyouandme.util.CuriosRiderConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;

public class BloodConfig {
    public static final ResourceLocation BLOOD_BASE_ID = RiderIds.fromString("blood_base");

    public static final RiderConfig BLOOD = new CuriosRiderConfig(RiderIds.BLOOD_ID)
            .setMainDriverItem(ModItems.BUILD_DRIVER.get(), EquipmentSlot.FEET);

    public static final FormConfig BLOOD_BASE = new FormConfig(BLOOD_BASE_ID)
            .setArmor(
                    ModItems.BLOOD_HELMET.get(),
                    ModItems.BLOOD_CHESTPLATE.get(),
                    ModItems.BLOOD_LEGGINGS.get(),
                    null)
            // ===== 基础效果 =====
            .addEffect(MobEffects.INVISIBILITY, -1, 0, true)
            .addEffect(MobEffects.DAMAGE_RESISTANCE, -1, 3, true)   // 高防御
            .addEffect(MobEffects.DAMAGE_BOOST, -1, 3, true)        // 高攻击
            .addEffect(MobEffects.MOVEMENT_SPEED, -1, 2, true)      // 高速
            .addEffect(MobEffects.JUMP, -1, 2, true)               // 高跳跃
            .addEffect(MobEffects.FIRE_RESISTANCE, -1, 0, true)
            .addEffect(MobEffects.NIGHT_VISION, -1, 0, true)
            // ===== 技能配置 =====
            .addSkill(RiderSkills.BLOOD_WAVE)           // 血族·能量波
            .addSkill(RiderSkills.BLOOD_BARRIER)        // 血族·次元障壁
            .addSkill(RiderSkills.BLOOD_GRAVITY_COLLAPSE) // 血族·重力崩坏
            .addSkill(RiderSkills.RIDER_KICK)           // 通用骑士踢
            .setShouldPause(true)
            .setTriggerType(TriggerType.KEY);

    private static void registerBloodRider() {
        BLOOD_BASE.setAllowsEmptyDriver(true);

        BLOOD.addForm(BLOOD_BASE);
        BLOOD.setBaseForm(BLOOD_BASE.getFormId());

        RiderRegistry.registerRider(BLOOD);
    }

    public static void init() {
        registerBloodRider();
    }
}
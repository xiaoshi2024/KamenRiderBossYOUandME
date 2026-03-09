package com.xiaoshi2022.kamenriderbossyouandme.riders.driver;

import com.jpigeon.ridebattlelib.core.system.form.FormConfig;
import com.jpigeon.ridebattlelib.core.system.henshin.RiderConfig;
import com.jpigeon.ridebattlelib.core.system.henshin.RiderRegistry;
import com.jpigeon.ridebattlelib.core.system.henshin.helper.TriggerType;
import com.xiaoshi2022.kamenriderbossyouandme.registry.ModItems;
import com.xiaoshi2022.kamenriderbossyouandme.riders.RiderIds;
import com.xiaoshi2022.kamenriderbossyouandme.riders.RiderSkills;
import com.xiaoshi2022.kamenriderbossyouandme.util.CuriosRiderConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;

public class BrainConfig {
    public static final ResourceLocation BRAIN_BASE_ID = RiderIds.fromString("brain_base");

    //等待杰瑞更新，以防止鞋子bug
    public static final RiderConfig BRAIN = new CuriosRiderConfig(RiderIds.BRAIN_ID)
            .setMainDriverItem(ModItems.BRAIN_DRIVER.get(), EquipmentSlot.FEET);
    
    public static final FormConfig BRAIN_BASE = new FormConfig(BRAIN_BASE_ID)
            .setArmor(
                    ModItems.BRAIN_HELMET.get(),
                    ModItems.BRAIN_CHESTPLATE.get(),
                    ModItems.BRAIN_LEGGINGS.get(),
                    null)
            .addEffect(MobEffects.INVISIBILITY, -1, 0, true)
            .addEffect(MobEffects.NIGHT_VISION, -1, 0, true) // TOX-High Beam Eye 视觉传感器
            .addEffect(MobEffects.MOVEMENT_SPEED, -1, 1, true) // OverClock Armor 超频提升处理能力
            .addEffect(MobEffects.DAMAGE_RESISTANCE, -1, 1, true) // Grün Head Cowl 吸收冲击
            .addEffect(MobEffects.DAMAGE_BOOST, -1, 1, true) // Verde Breast Cowl 强化肌肉
            .addEffect(MobEffects.JUMP, -1, 1, true) // BN-Driving Suits 和 Optimizer Pad 提升身体能力与平衡
            .setShouldPause(true)
            .addSkill(RiderSkills.BRAIN_KICK)
            .addSkill(RiderSkills.BRAIN_HEADBUTT)
            .addSkill(RiderSkills.BRAIN_POISON)
            .setTriggerType(TriggerType.KEY);
    
    public static void registerBrainRider() {
        BRAIN_BASE.setAllowsEmptyDriver(true);
        
        BRAIN.addForm(BRAIN_BASE);
        BRAIN.setBaseForm(BRAIN_BASE.getFormId());
        
        RiderRegistry.registerRider(BRAIN);
    }
    
    public static void init() {
        registerBrainRider();
    }
}
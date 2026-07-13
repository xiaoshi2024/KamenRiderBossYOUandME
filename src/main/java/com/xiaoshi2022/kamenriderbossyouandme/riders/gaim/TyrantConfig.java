package com.xiaoshi2022.kamenriderbossyouandme.riders.gaim;

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

public class TyrantConfig {
    public static final ResourceLocation TYRANT_BASE_ID = RiderIds.fromString("tyrant_base");

    public static final RiderConfig TYRANT = new CuriosRiderConfig(RiderIds.TYRANT_ID)
            .setMainDriverItem(ModItems.GENESIS_DRIVER.get(), EquipmentSlot.FEET);

    public static final FormConfig TYRANT_BASE = new FormConfig(TYRANT_BASE_ID)
            .setArmor(
                    ModItems.TYRANT_HELMET.get(),
                    ModItems.TYRANT_CHESTPLATE.get(),
                    ModItems.TYRANT_LEGGINGS.get(),
                    null)
            .addEffect(MobEffects.INVISIBILITY, -1, 0, true)
            .addEffect(MobEffects.DAMAGE_RESISTANCE, -1, 2, true)
            .addEffect(MobEffects.DAMAGE_BOOST, -1, 2, true)
            .addEffect(MobEffects.MOVEMENT_SPEED, -1, 1, true)
            .addEffect(MobEffects.JUMP, -1, 1, true)
            .addEffect(MobEffects.FIRE_RESISTANCE, -1, 0, true)
            .addSkill(RiderSkills.TYRANT_KICK)
            .addSkill(RiderSkills.TYRANT_INTANGIBILITY)
            .setShouldPause(true)
            .setTriggerType(TriggerType.KEY);

    private static void registerTyrantRider() {
        // ✅ 关键：允许空驱动器
        TYRANT_BASE.setAllowsEmptyDriver(true);

        TYRANT.addForm(TYRANT_BASE);
        TYRANT.setBaseForm(TYRANT_BASE.getFormId());

        RiderRegistry.registerRider(TYRANT);
    }

    public static void init() {
        registerTyrantRider();
    }
}
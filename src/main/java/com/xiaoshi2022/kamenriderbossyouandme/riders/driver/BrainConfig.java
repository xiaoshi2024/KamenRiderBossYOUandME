package com.xiaoshi2022.kamenriderbossyouandme.riders.driver;

import com.jpigeon.ridebattlelib.core.system.form.FormConfig;
import com.jpigeon.ridebattlelib.core.system.henshin.RiderConfig;
import com.jpigeon.ridebattlelib.core.system.henshin.RiderRegistry;
import com.jpigeon.ridebattlelib.core.system.henshin.helper.TriggerType;
import com.xiaoshi2022.kamenriderbossyouandme.registry.ModItems;
import com.xiaoshi2022.kamenriderbossyouandme.riders.RiderIds;
import com.xiaoshi2022.kamenriderbossyouandme.util.CuriosRiderConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;

public class BrainConfig {
    public static final ResourceLocation BRAIN_BASE_ID = RiderIds.fromString("brain_base");
    
    public static final RiderConfig BRAIN = new CuriosRiderConfig(RiderIds.BRAIN_ID)
            .setMainDriverItem(ModItems.BRAIN_DRIVER.get(), EquipmentSlot.FEET);
    
    public static final FormConfig BRAIN_BASE = new FormConfig(BRAIN_BASE_ID)
            .setArmor(
                    ModItems.BRAIN_HELMET.get(),
                    ModItems.BRAIN_CHESTPLATE.get(),
                    ModItems.BRAIN_LEGGINGS.get(),
                    null)
            .addEffect(MobEffects.INVISIBILITY, -1, 0, true)
            .setShouldPause(true)
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
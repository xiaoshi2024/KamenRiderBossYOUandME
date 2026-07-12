// registry/ModEntitys.java
package com.xiaoshi2022.kamenriderbossyouandme.registry;

import com.xiaoshi2022.kamenriderbossyouandme.entity.FusionEffectEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import static com.xiaoshi2022.kamenriderbossyouandme.KamenRiderBossYOUandME.MODID;

public class ModEntitys {

    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(Registries.ENTITY_TYPE, MODID);

    /**
     * 融合特效实体 - 3人融合 + 特效
     */
    // ModEntitys.java
    public static final DeferredHolder<EntityType<?>, EntityType<FusionEffectEntity>> FUSION_EFFECT =
            ENTITIES.register("fusion_effect",
                    () -> EntityType.Builder.<FusionEffectEntity>of(FusionEffectEntity::new, MobCategory.MISC)
                            .sized(3.0F, 4.0F)
                            .clientTrackingRange(64)
                            .updateInterval(1)
                            .noSave()        // 保留：不保存到世界
                            // 移除 .noSummon() → 允许 /summon
                            .build("fusion_effect")
            );
}
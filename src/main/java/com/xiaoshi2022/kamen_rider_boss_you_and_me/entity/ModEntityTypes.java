package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.block.kivas.entity.SeatEntity;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.Another_Zi_o;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.BatDarksEntity;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.GiifuDemosEntity;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.Inves.ElementaryInvesHelheim;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.Lord.LordBaronEntity;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.StoriousEntity;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.giifu.Gifftarian;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.giifu.GiifuHumanEntity;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.kivat.KivatBatTwoNd;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me.MODID;


public class ModEntityTypes {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, "kamen_rider_boss_you_and_me");

    public static final RegistryObject<EntityType<GiifuDemosEntity>> GIIFUDEMOS_ENTITY = registerMob("giifudemos", GiifuDemosEntity::new,
            0.6f, 1.8f, 0x1F1F1F, 0x0D0D0D);

    // 在 ModEntityTypes 类中
    public static final RegistryObject<EntityType<GiifuHumanEntity>> GIIFU_HUMAN =
            ENTITY_TYPES.register("giifu_human", // 确保这个字符串是唯一的
                    () -> EntityType.Builder.of(GiifuHumanEntity::new, MobCategory.MONSTER)
                            .sized(0.6f, 1.95f) // 设置碰撞箱大小 (宽度, 高度)
                            .build(new ResourceLocation(MODID, "giifu_human").toString()));

    public static final RegistryObject<EntityType<StoriousEntity>> STORIOUS = registerMob("storious", StoriousEntity::new,
                0.6f, 1.9f, 0x1F1F1F, 0x0D0D0D);
    public static final RegistryObject<EntityType<Gifftarian>> GIFFTARIAN = registerMob("gifftarian", Gifftarian::new,
                0.6f, 1.9f, 0x1F1F1F, 0x0D0D0D);
    public static final RegistryObject<EntityType<ElementaryInvesHelheim>> INVES_HEILEHIM = registerMob("inves_heilehim", ElementaryInvesHelheim::new,
                0.6f, 1.9f, 0x1F1F1F, 0x0D0D0D);
    // 确保注册名是 "lord_baron"
    public static final RegistryObject<EntityType<LordBaronEntity>> LORD_BARON = registerMob("lord_baron", LordBaronEntity::new,
            0.6f, 1.9f, 0x1F1F1F, 0x0D0D0D);

    public static final RegistryObject<EntityType<Another_Zi_o>> ANOTHER_ZI_O = registerMob("another_zi_o", Another_Zi_o::new,
            0.6f, 1.9f, 0x1F1F1F, 0x0D0D0D);
//    public static final RegistryObject<EntityType<KaitoVillager>> KAITO =
//            ENTITY_TYPES.register("kaito",
//                    () -> EntityType.Builder.of(KaitoVillager::new, MobCategory.CREATURE)
//                            .sized(0.6F, 1.95F)
//                            .build("kaito"));

    public static final RegistryObject<EntityType<KivatBatTwoNd>> KIVAT_BAT_II =
            ENTITY_TYPES.register("kivat_bat_ii",
                    () -> EntityType.Builder.of(KivatBatTwoNd::new, MobCategory.AMBIENT)
                            .sized(0.5F, 0.5F)
                            .clientTrackingRange(8)
                            .build("kivat_bat_ii"));

    //椅子实体
    public static final RegistryObject<EntityType<SeatEntity>> SEAT = ENTITY_TYPES.register("seat",
            () -> EntityType.Builder.<SeatEntity>of(SeatEntity::new, MobCategory.MISC)
                    .sized(0.001F, 0.001F) // 非常小的碰撞箱
                    .build("seat"));
    
    // BatDarks 特效实体
    public static final RegistryObject<EntityType<BatDarksEntity>> BAT_DARKS = ENTITY_TYPES.register("bat_darks",
            () -> EntityType.Builder.<BatDarksEntity>of(BatDarksEntity::new, MobCategory.MISC)
                    .sized(0.6F, 1.9F) // 调整碰撞箱大小，使其更易于观察
                    .clientTrackingRange(8)
                    .build("bat_darks"));

//    public static final RegistryObject<EntityType<KnecromghostEntity>> KNECROMGHOST = registerMob("knecromghost", KnecromghostEntity::new,
//            0.6f, 1.6f, 0x1F1F1F, 0x0D0D0D);

    public static <T extends Mob> RegistryObject<EntityType<T>> registerMob(String name, EntityType.EntityFactory<T> entity,
                                                                            float width, float height, int primaryEggColor, int secondaryEggColor) {
        RegistryObject<EntityType<T>> entityType = ENTITY_TYPES.register(name,
                () -> EntityType.Builder.of(entity, MobCategory.CREATURE).sized(width, height).build(name));

        return entityType;
    }
}
package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.block.kivas.entity.SeatEntity;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.*;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.EliteMonster.EliteMonsterNpc;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.Inves.ElementaryInvesHelheim;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.Lord.LordBaronEntity;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.Roidmude.BrainRoidmudeEntity;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.anotherRiders.Another_Decade;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.anotherRiders.Another_Den_o;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.anotherRiders.Another_Zi_o;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.giifu.Gifftarian;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.giifu.GiifuHumanEntity;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.kivat.KivatBatTwoNd;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.misc.DimensionalBarrier;
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
                
    // 时劫者实体 - 动态选择MCA村民或默认村民
    public static final RegistryObject<EntityType<TimeJackerEntity>> TIME_JACKER = ENTITY_TYPES.register("time_jacker",
            () -> EntityType.Builder.of(TimeJackerEntity::create, MobCategory.CREATURE)
                    .sized(0.6f, 1.95f)
                    .build(new ResourceLocation(MODID, "time_jacker").toString()));
                    
    // 时间王族实体 - 动态选择MCA村民或默认村民
    public static final RegistryObject<EntityType<TimeRoyaltyEntity>> TIME_ROYALTY = ENTITY_TYPES.register("time_royalty",
            () -> EntityType.Builder.of(TimeRoyaltyEntity::create, MobCategory.CREATURE)
                    .sized(0.6f, 1.95f)
                    .build(new ResourceLocation(MODID, "time_royalty").toString()));

    // 注册MCA时劫者实体 - 供MCA模组使用
    public static final RegistryObject<EntityType<MCATimeJackerEntity>> MCA_TIME_JACKER = ENTITY_TYPES.register("mca_time_jacker",
            () -> EntityType.Builder.of(MCATimeJackerEntity::new, MobCategory.CREATURE)
                    .sized(0.6f, 1.95f)
                    .build(new ResourceLocation(MODID, "mca_time_jacker").toString()));

    // 注册MCA时间王族实体 - 供MCA模组使用
    public static final RegistryObject<EntityType<MCATimeRoyaltyEntity>> MCA_TIME_ROYALTY = ENTITY_TYPES.register("mca_time_royalty",
            () -> EntityType.Builder.of(MCATimeRoyaltyEntity::new, MobCategory.CREATURE)
                    .sized(0.6f, 1.95f)
                    .build(new ResourceLocation(MODID, "mca_time_royalty").toString()));
    public static final RegistryObject<EntityType<Gifftarian>> GIFFTARIAN = registerMob("gifftarian", Gifftarian::new,
                0.6f, 1.9f, 0x1F1F1F, 0x0D0D0D);
    public static final RegistryObject<EntityType<ElementaryInvesHelheim>> INVES_HEILEHIM = registerMob("inves_heilehim", ElementaryInvesHelheim::new,
                0.6f, 1.9f, 0x1F1F1F, 0x0D0D0D);
    // 确保注册名是 "lord_baron"
    public static final RegistryObject<EntityType<LordBaronEntity>> LORD_BARON = registerMob("lord_baron", LordBaronEntity::new,
            0.6f, 1.9f, 0x1F1F1F, 0x0D0D0D);

    public static final RegistryObject<EntityType<DukeKnightEntity>> DUKE_KNIGHT = registerMob("duke_knight", DukeKnightEntity::new,
            0.6f, 1.9f, 0x1F1F1F, 0x0D0D0D);

    public static final RegistryObject<EntityType<Another_Zi_o>> ANOTHER_ZI_O = registerMob("another_zi_o", Another_Zi_o::new,
            0.6f, 1.9f, 0x1F1F1F, 0x0D0D0D);
    
    public static final RegistryObject<EntityType<Another_Den_o>> ANOTHER_DEN_O = registerMob("another_den_o", Another_Den_o::new,
            0.6f, 1.9f, 0x1F1F1F, 0x0D0D0D);
    
    public static final RegistryObject<EntityType<Another_Decade>> ANOTHER_DECADE = registerMob("another_decade", Another_Decade::new,
            0.6f, 1.9f, 0x1F1F1F, 0x0D0D0D);

    // 头脑机械变异体实体
    public static final RegistryObject<EntityType<BrainRoidmudeEntity>> BRAIN_ROIDMUDE = registerMob("brain_roidmude", BrainRoidmudeEntity::new,
            0.6f, 2.0f, 0x4A0000, 0x800000);

    public static final RegistryObject<EntityType<EliteMonsterNpc>> ELITE_MONSTER_NPC =
            ENTITY_TYPES.register("elite_monster_npc",
                    () -> EntityType.Builder.of(EliteMonsterNpc::new, MobCategory.MISC)
                            .sized(0.6f, 1.95f) // 尺寸
                            .clientTrackingRange(8) // 添加客户端跟踪范围
                            .build("elite_monster_npc"));

    // 异类电班列实体
    public static final RegistryObject<EntityType<AnotherDenlinerEntity>> ANOTHER_DENLINER = ENTITY_TYPES.register("another_denliner",
            () -> EntityType.Builder.<AnotherDenlinerEntity>of(AnotherDenlinerEntity::new, MobCategory.MISC)
                    .sized(4.0F, 3.0F)
                    .clientTrackingRange(16)
                    .build("another_denliner"));

    // 眼魔实体（使用原版动画集）- 修改为MISC类别，防止自然生成
    public static final RegistryObject<EntityType<Gamma_s_Entity>> GAMMA_S = ENTITY_TYPES.register("gamma_s",
            () -> EntityType.Builder.of(Gamma_s_Entity::new, MobCategory.MISC)
                    .sized(0.6f, 1.9f)
                    .clientTrackingRange(8)
                    .build("gamma_s"));
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
    
    // 樱花飓风摩托车实体
    public static final RegistryObject<EntityType<EntitySakuraHurricane>> SAKURA_HURRICANE = registerMob("sakura_hurricane", EntitySakuraHurricane::new,
            1.0f, 0.7f, 0xFF6B6B, 0xFFE066);
    
    // BatDarks 特效实体
    public static final RegistryObject<EntityType<BatDarksEntity>> BAT_DARKS = ENTITY_TYPES.register("bat_darks",
            () -> EntityType.Builder.<BatDarksEntity>of(BatDarksEntity::new, MobCategory.MISC)
                    .sized(0.6F, 1.9F) // 调整碰撞箱大小，使其更易于观察
                    .clientTrackingRange(8)
                    .build("bat_darks"));

    // NOX变身特效实体
    public static final RegistryObject<EntityType<NoxSpecialEntity>> NOX_SPECIAL = ENTITY_TYPES.register(
            "nox_special",
            () -> EntityType.Builder.of(NoxSpecialEntity::new, MobCategory.MISC)
                    .setShouldReceiveVelocityUpdates(false)
                    .setTrackingRange(10)
                    .setUpdateInterval(Integer.MAX_VALUE) // 尽量少更新，因为是特效
                    .build("nox_special")
    );


    // BatStampFinish 特效实体
    public static final RegistryObject<EntityType<BatStampFinishEntity>> BAT_STAMP_FINISH = ENTITY_TYPES.register("bat_stamp_finish",
            () -> EntityType.Builder.<BatStampFinishEntity>of(BatStampFinishEntity::new, MobCategory.MISC)
                    .sized(0.8F, 1.5F) // 调整碰撞箱大小
                    .clientTrackingRange(8)
                    .build("bat_stamp_finish"));

    public static final RegistryObject<EntityType<KnecromghostEntity>> KNECROMGHOST = ENTITY_TYPES.register("knecromghost",
            () -> EntityType.Builder.<KnecromghostEntity>of(KnecromghostEntity::new, MobCategory.MISC)
                    .sized(0.8F, 1.5F) // 调整碰撞箱大小
                    .clientTrackingRange(8)
                    .build("knecromghost"));

    // 巴隆香蕉能量特效实体
    public static final RegistryObject<EntityType<BaronBananaEnergyEntity>> BARON_BANANA_ENERGY = ENTITY_TYPES.register("baron_banana_energy",
            () -> EntityType.Builder.<BaronBananaEnergyEntity>of(BaronBananaEnergyEntity::new, MobCategory.MISC)
                    .sized(0.6F, 1.0F) // 调整碰撞箱大小
                    .clientTrackingRange(8)
                    .build("baron_banana_energy"));
    
    // 巴隆柠檬能量特效实体
    public static final RegistryObject<EntityType<BaronLemonEnergyEntity>> BARON_LEMON_ENERGY = ENTITY_TYPES.register("baron_lemon_energy",
            () -> EntityType.Builder.<BaronLemonEnergyEntity>of(BaronLemonEnergyEntity::new, MobCategory.MISC)
                    .sized(0.6F, 1.0F) // 调整碰撞箱大小
                    .clientTrackingRange(8)
                    .build("baron_lemon_energy"));
    
    // 蜜瓜能量剑气实体
    public static final RegistryObject<EntityType<MelonEnergySlashEntity>> MELON_ENERGY_SLASH = ENTITY_TYPES.register("melon_energy_slash",
            () -> EntityType.Builder.<MelonEnergySlashEntity>of(MelonEnergySlashEntity::new, MobCategory.MISC)
                    .sized(0.8F, 0.2F) // 调整碰撞箱大小，使其呈扁平状
                    .clientTrackingRange(8)
                    .build("melon_energy_slash"));
    
    // 黑暗Kiva封印结界实体
    public static final RegistryObject<EntityType<DarkKivaSealBarrierEntity>> DARK_KIVA_SEAL_BARRIER = ENTITY_TYPES.register("dark_kiva_seal_barrier",
            () -> EntityType.Builder.<DarkKivaSealBarrierEntity>of(DarkKivaSealBarrierEntity::new, MobCategory.MISC)
                    .sized(2.0F, 2.0F) // 默认大小
                    .clientTrackingRange(16)
                    .build("dark_kiva_seal_barrier"));

    // 樱桃能量箭矢实体
    public static final RegistryObject<EntityType<CherryEnergyArrowEntity>> CHERRY_ENERGY_ARROW = ENTITY_TYPES.register("cherry_energy_arrow",
            () -> EntityType.Builder.<CherryEnergyArrowEntity>of(CherryEnergyArrowEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F) // 箭矢大小
                    .clientTrackingRange(16)
                    .updateInterval(20)
                    .build("cherry_energy_arrow"));
    
    // 眼魂实体
    public static final RegistryObject<EntityType<GhostEyeEntity>> GHOST_EYE_ENTITY = ENTITY_TYPES.register("ghost_eye_entity",
            () -> EntityType.Builder.<GhostEyeEntity>of(GhostEyeEntity::new, MobCategory.MISC)
                    .sized(0.8F, 0.8F) // 眼魂实体大小
                    .clientTrackingRange(8)
                    .updateInterval(1)
                    .build("ghost_eye_entity"));
    
    // 眼魔眼魂实体
    public static final RegistryObject<EntityType<GammaEyeconEntity>> GAMMA_EYECON_ENTITY = ENTITY_TYPES.register("gamma_eyecon_entity",
            () -> EntityType.Builder.<GammaEyeconEntity>of(GammaEyeconEntity::new, MobCategory.AMBIENT)
                    .sized(0.8F, 0.8F) // 眼魂实体大小
                    .clientTrackingRange(8)
                    .updateInterval(1)
                    .build("gamma_eyecon_entity"));

    // NecromEyex 特效实体
    public static final RegistryObject<EntityType<NecromEyexEntity>> NECROM_EYEX = ENTITY_TYPES.register("necrom_eyex",
            () -> EntityType.Builder.<NecromEyexEntity>of(NecromEyexEntity::new, MobCategory.MISC)
                    .sized(0.8F, 1.5F) // 调整碰撞箱大小
                    .clientTrackingRange(8)
                    .build("necrom_eyex"));
    
    // 次元壁实体
    public static final RegistryObject<EntityType<DimensionalBarrier>> DIMENSIONAL_BARRIER = ENTITY_TYPES.register("dimensional_barrier",
            () -> EntityType.Builder.<DimensionalBarrier>of(DimensionalBarrier::new, MobCategory.MISC)
                    .sized(2.0F, 2.0F) // 次元壁大小
                    .clientTrackingRange(16)
                    .build("dimensional_barrier"));

    public static final RegistryObject<EntityType<DarkGhostRiderKickEntity>> DARKGHOST_RIDER_KICK = ENTITY_TYPES.register("darkghost_rider_kick",
            () -> EntityType.Builder.<DarkGhostRiderKickEntity>of(DarkGhostRiderKickEntity::new, MobCategory.MISC)
                    .sized(0.8F, 1.5F) // 调整碰撞箱大小
                    .clientTrackingRange(8)
                    .build("necrom_eyex"));

    public static <T extends Mob> RegistryObject<EntityType<T>> registerMob(String name, EntityType.EntityFactory<T> entity,
                                                                            float width, float height, int primaryEggColor, int secondaryEggColor) {
        RegistryObject<EntityType<T>> entityType = ENTITY_TYPES.register(name,
                () -> EntityType.Builder.of(entity, MobCategory.CREATURE).sized(width, height).build(name));

        return entityType;
    }
}
package com.xiaoshi2022.kamen_rider_boss_you_and_me.util;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.ModEntityTypes;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.GiifuDemosEntity;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.StoriousEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.UUID;


public class VillagerTransformationUtil {
    // 全局存储：记录人类的NBT数据和UUID
    private static final HashMap<UUID, CompoundTag> villagerRecord = new HashMap<>();

    // 人类 -> 怪人
    public static void transformToGiifuDemos(ServerLevel level, Object villagerEntity, CompoundTag tag) {
        // 检查是否是MCA村民或普通村民
        boolean isMCAVillager = MCAUtil.isVillagerEntityMCA((Entity) villagerEntity);
        
        // 获取目标实体的位置
        BlockPos spawnPos = BlockPos.containing(((Entity) villagerEntity).position());

        // 尝试生成新的自定义实体
        GiifuDemosEntity spawnedEntity = (GiifuDemosEntity) ModEntityTypes.GIIFUDEMOS_ENTITY.get().spawn(level, spawnPos, MobSpawnType.MOB_SUMMONED);

        if (spawnedEntity != null) {
            // 加载NBT数据
            if (tag != null) {
                spawnedEntity.load(tag);
            }

            // 设置UUID
            spawnedEntity.setUUID(UUID.randomUUID());

            // 继承交易系统
            if (isMCAVillager) {
                // 使用MCAUtil获取MCA村民数据
                Object villagerData = MCAUtil.getVillagerData(villagerEntity);
                if (villagerData instanceof VillagerData) {
                    spawnedEntity.setVillagerData((VillagerData) villagerData);
                }
                int villagerXp = MCAUtil.getVillagerXp(villagerEntity);
                spawnedEntity.setVillagerXp(villagerXp);
                Object offers = MCAUtil.getOffers(villagerEntity);
                if (offers != null) {
                    try {
                        // 使用反射设置交易列表
                        java.lang.reflect.Method setTradeOffersMethod = spawnedEntity.getClass().getMethod("setTradeOffers", offers.getClass());
                        setTradeOffersMethod.invoke(spawnedEntity, offers);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else if (villagerEntity instanceof Villager vanillaVillager) {
                // 处理普通村民
                spawnedEntity.setVillagerData(vanillaVillager.getVillagerData());
                spawnedEntity.setVillagerXp(vanillaVillager.getVillagerXp());
                spawnedEntity.setTradeOffers(vanillaVillager.getOffers());
            }

            // 设置生成位置为人类的当前位置
            spawnedEntity.setPos(((Entity) villagerEntity).getX(), ((Entity) villagerEntity).getY(), ((Entity) villagerEntity).getZ());

            // 记录人类的NBT数据和UUID
            CompoundTag villagerNBT = new CompoundTag();
            ((Entity) villagerEntity).save(villagerNBT);
            villagerRecord.put(spawnedEntity.getUUID(), villagerNBT);

            // 移除原始实体
            ((Entity) villagerEntity).remove(Entity.RemovalReason.DISCARDED);

            System.out.println("Villager has been successfully transformed into GiifuDemosEntity.");
        } else {
            System.out.println("Failed to spawn GiifuDemosEntity.");
        }
    }

    // 人类 -> 怪人
    public static void transformToStorious(ServerLevel level, Object villagerEntity, CompoundTag tag) {
        // 检查是否是MCA村民或普通村民
        boolean isMCAVillager = MCAUtil.isVillagerEntityMCA((Entity) villagerEntity);
        
        // 获取目标实体的位置
        BlockPos spawnPos = BlockPos.containing(((Entity) villagerEntity).position());

        // 尝试生成新的自定义实体
        StoriousEntity spawnedEntity = (StoriousEntity) ModEntityTypes.STORIOUS.get().spawn(level, spawnPos, MobSpawnType.MOB_SUMMONED);

        if (spawnedEntity != null) {
            // 加载NBT数据
            if (tag != null) {
                spawnedEntity.load(tag);
            }

            // 设置UUID
            spawnedEntity.setUUID(UUID.randomUUID());

            // 继承交易系统
            if (isMCAVillager) {
                // 使用MCAUtil获取MCA村民数据
                Object villagerData = MCAUtil.getVillagerData(villagerEntity);
                if (villagerData instanceof VillagerData) {
                    spawnedEntity.setVillagerData((VillagerData) villagerData);
                }
                int villagerXp = MCAUtil.getVillagerXp(villagerEntity);
                spawnedEntity.setVillagerXp(villagerXp);
                Object offers = MCAUtil.getOffers(villagerEntity);
                if (offers != null) {
                    try {
                        // 使用反射设置交易列表
                        java.lang.reflect.Method setTradeOffersMethod = spawnedEntity.getClass().getMethod("setTradeOffers", offers.getClass());
                        setTradeOffersMethod.invoke(spawnedEntity, offers);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else if (villagerEntity instanceof Villager vanillaVillager) {
                // 处理普通村民
                spawnedEntity.setVillagerData(vanillaVillager.getVillagerData());
                spawnedEntity.setVillagerXp(vanillaVillager.getVillagerXp());
                spawnedEntity.setTradeOffers(vanillaVillager.getOffers());
            }

            // 设置生成位置为人类的当前位置
            spawnedEntity.setPos(((Entity) villagerEntity).getX(), ((Entity) villagerEntity).getY(), ((Entity) villagerEntity).getZ());

            // 记录人类的NBT数据和UUID
            CompoundTag villagerNBT = new CompoundTag();
            ((Entity) villagerEntity).save(villagerNBT);
            villagerRecord.put(spawnedEntity.getUUID(), villagerNBT);

            // 移除原始实体
            ((Entity) villagerEntity).remove(Entity.RemovalReason.DISCARDED);

            System.out.println("Villager has been successfully transformed into StoriousEntity.");
        } else {
            System.out.println("Failed to spawn StoriousEntity.");
        }
    }


    // 怪人 -> 人类（从恶魔
    public static void transformToVillager(ServerLevel serverLevel, GiifuDemosEntity giifuDemosEntity, CompoundTag tag) {
        UUID giifuDemosUUID = giifuDemosEntity.getUUID();
        if (villagerRecord.containsKey(giifuDemosUUID)) {
            CompoundTag recordedNBT = villagerRecord.get(giifuDemosUUID);
            BlockPos spawnPos = BlockPos.containing(giifuDemosEntity.position());

            Villager newVillager = null;
            
            // 尝试生成MCA村民（如果MCA可用）
            if (MCAUtil.isMCAAvailable()) {
                newVillager = MCAUtil.spawnMCAVillager(serverLevel, spawnPos, MobSpawnType.MOB_SUMMONED);
            }
            
            // 如果MCA不可用或生成失败，生成普通村民
            if (newVillager == null) {
                // 获取村庄类型
                VillagerType villagerType = BuiltInRegistries.VILLAGER_TYPE.get(new ResourceLocation("plains"));
                // 创建普通村民
                newVillager = new Villager(net.minecraft.world.entity.EntityType.VILLAGER, serverLevel);
                newVillager.setVillagerData(new VillagerData(villagerType, VillagerProfession.NONE, 1));
                newVillager.setPos(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());
                serverLevel.addFreshEntity(newVillager);
            }

            if (newVillager != null) {
                newVillager.load(recordedNBT);

                // 继承 VillagerData（职业、等级等）
                if (recordedNBT.contains("VillagerData")) {
                    CompoundTag villagerDataTag = recordedNBT.getCompound("VillagerData");
                    VillagerProfession profession = ForgeRegistries.VILLAGER_PROFESSIONS.getValue(new ResourceLocation(villagerDataTag.getString("Profession")));
                    int level = villagerDataTag.getInt("Level");
                    String type = villagerDataTag.getString("Type");
                    VillagerType villagerType = BuiltInRegistries.VILLAGER_TYPE.get(new ResourceLocation(type));
                    newVillager.setVillagerData(new VillagerData(villagerType, profession, level));
                }

                // 设置生成位置为怪人的当前位置
                newVillager.setPos(giifuDemosEntity.getX(), giifuDemosEntity.getY(), giifuDemosEntity.getZ());

                // 移除原始 GiifuDemosEntity
                giifuDemosEntity.remove(Entity.RemovalReason.DISCARDED);

                System.out.println("GiifuDemosEntity has been successfully transformed into Villager.");
            } else {
                System.out.println("Failed to spawn Villager.");
            }

            // 清除记录
            villagerRecord.remove(giifuDemosUUID);
        } else {
            System.out.println("No recorded data found for this GiifuDemosEntity.");
        }
    }
    // 怪人 -> 人类(从书
    public static void transformToBookVillager(ServerLevel serverLevel, StoriousEntity storiousEntity, CompoundTag tag) {
        UUID giifuDemosUUID = storiousEntity.getUUID();
        if (villagerRecord.containsKey(giifuDemosUUID)) {
            CompoundTag recordedNBT = villagerRecord.get(giifuDemosUUID);
            BlockPos spawnPos = BlockPos.containing(storiousEntity.position());

            Villager newVillager = null;
            
            // 尝试生成MCA村民（如果MCA可用）
            if (MCAUtil.isMCAAvailable()) {
                newVillager = MCAUtil.spawnMCAVillager(serverLevel, spawnPos, MobSpawnType.MOB_SUMMONED);
            }
            
            // 如果MCA不可用或生成失败，生成普通村民
            if (newVillager == null) {
                // 获取村庄类型
                VillagerType villagerType = BuiltInRegistries.VILLAGER_TYPE.get(new ResourceLocation("plains"));
                // 创建普通村民
                newVillager = new Villager(net.minecraft.world.entity.EntityType.VILLAGER, serverLevel);
                newVillager.setVillagerData(new VillagerData(villagerType, VillagerProfession.NONE, 1));
                newVillager.setPos(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());
                serverLevel.addFreshEntity(newVillager);
            }

            if (newVillager != null) {
                newVillager.load(recordedNBT);

                // 继承 VillagerData（职业、等级等）
                if (recordedNBT.contains("VillagerData")) {
                    CompoundTag villagerDataTag = recordedNBT.getCompound("VillagerData");
                    VillagerProfession profession = ForgeRegistries.VILLAGER_PROFESSIONS.getValue(new ResourceLocation(villagerDataTag.getString("Profession")));
                    int level = villagerDataTag.getInt("Level");
                    String type = villagerDataTag.getString("Type");
                    VillagerType villagerType = BuiltInRegistries.VILLAGER_TYPE.get(new ResourceLocation(type));
                    newVillager.setVillagerData(new VillagerData(villagerType, profession, level));
                }

                // 设置生成位置为怪人的当前位置
                newVillager.setPos(storiousEntity.getX(), storiousEntity.getY(), storiousEntity.getZ());

                // 移除原始 storiousEntity
                storiousEntity.remove(Entity.RemovalReason.DISCARDED);

                System.out.println("StoriousEntity has been successfully transformed into Villager.");
            } else {
                System.out.println("Failed to spawn Villager.");
            }

            // 清除记录
            villagerRecord.remove(giifuDemosUUID);
        } else {
            System.out.println("No recorded data found for this StoriousEntity.");
        }
    }
}
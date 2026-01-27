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
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.UUID;


public class VillagerTransformationUtil {
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

            // 保存原始村民数据到实体的persistentData中，而不是静态映射
            CompoundTag villagerNBT = new CompoundTag();
            ((Entity) villagerEntity).save(villagerNBT);
            spawnedEntity.getPersistentData().put(GiifuDemosEntity.ORIGINAL_VILLAGER_TAG, villagerNBT);
            
            // System.out.println("Stored villager data in GiifuDemosEntity: " + spawnedEntity.getPersistentData().contains(GiifuDemosEntity.ORIGINAL_VILLAGER_TAG));

            // 移除原始实体
            ((Entity) villagerEntity).remove(Entity.RemovalReason.DISCARDED);

            // System.out.println("Villager has been successfully transformed into GiifuDemosEntity.");
        } else {
            // System.out.println("Failed to spawn GiifuDemosEntity.");
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

            // 保存原始村民数据到实体的persistentData中
            CompoundTag villagerNBT = new CompoundTag();
            ((Entity) villagerEntity).save(villagerNBT);
            spawnedEntity.getPersistentData().put(StoriousEntity.ORIGINAL_VILLAGER_TAG, villagerNBT);

            // 移除原始实体
            ((Entity) villagerEntity).remove(Entity.RemovalReason.DISCARDED);

            // System.out.println("Villager has been successfully transformed into StoriousEntity.");
            // System.out.println("Stored villager data in StoriousEntity: " + spawnedEntity.getPersistentData().contains(StoriousEntity.ORIGINAL_VILLAGER_TAG));
        } else {
            // System.out.println("Failed to spawn StoriousEntity.");
        }
    }


    // 怪人 -> 人类（从恶魔
    public static void transformToVillager(ServerLevel serverLevel, GiifuDemosEntity giifuDemosEntity, CompoundTag tag) {
        // 首先尝试从道具的NBT中获取原始村民数据，如果没有再从实体的persistentData中获取
        CompoundTag recordedNBT = null;
        
        // 检查道具tag中是否有存储的村民数据
        if (tag != null && tag.contains("StoredVillagerData")) {
            recordedNBT = tag.getCompound("StoredVillagerData");
            // System.out.println("Reading villager data from item tag");
        } 
        // 检查实体的persistentData中是否有存储的村民数据
        else if (giifuDemosEntity.getPersistentData().contains(GiifuDemosEntity.ORIGINAL_VILLAGER_TAG)) {
            recordedNBT = giifuDemosEntity.getPersistentData().getCompound(GiifuDemosEntity.ORIGINAL_VILLAGER_TAG);
            // System.out.println("Reading villager data from entity persistentData");
        }
        
        if (recordedNBT != null) {
            BlockPos spawnPos = BlockPos.containing(giifuDemosEntity.position());

            // 直接从NBT数据恢复实体，支持MCA村民和原版村民
            Entity restoredEntity = EntityType.loadEntityRecursive(
                recordedNBT, serverLevel, 
                entity -> {
                    entity.setPos(giifuDemosEntity.getX(), giifuDemosEntity.getY(), giifuDemosEntity.getZ());
                    return entity;
                }
            );

            if (restoredEntity != null) {
                // 设置最大生命值
                if (restoredEntity instanceof LivingEntity livingEntity) {
                    livingEntity.setHealth(livingEntity.getMaxHealth());
                }
                
                // 添加到世界
                serverLevel.addFreshEntity(restoredEntity);
                
                // 移除原始 GiifuDemosEntity
                giifuDemosEntity.remove(Entity.RemovalReason.DISCARDED);

                // System.out.println("GiifuDemosEntity has been successfully transformed into original villager.");
                return;
            }
            
            // 如果直接从NBT恢复失败，尝试生成新村民
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

                // System.out.println("GiifuDemosEntity has been successfully transformed into Villager.");
            } else {
                // System.out.println("Failed to spawn Villager.");
            }
        } else {
            // System.out.println("No recorded data found for this GiifuDemosEntity.");
        }
    }
    // 怪人 -> 人类(从书
    public static void transformToBookVillager(ServerLevel serverLevel, StoriousEntity storiousEntity, CompoundTag tag) {
        // 首先尝试从道具的NBT中获取原始村民数据，如果没有再从实体的persistentData中获取
        CompoundTag recordedNBT = null;
        
        // 检查道具tag中是否有存储的村民数据
        if (tag != null && tag.contains("StoredVillagerData")) {
            recordedNBT = tag.getCompound("StoredVillagerData");
            // System.out.println("Reading villager data from item tag");
        } 
        // 检查实体的persistentData中是否有存储的村民数据
        else if (storiousEntity.getPersistentData().contains(StoriousEntity.ORIGINAL_VILLAGER_TAG)) {
            recordedNBT = storiousEntity.getPersistentData().getCompound(StoriousEntity.ORIGINAL_VILLAGER_TAG);
            // System.out.println("Reading villager data from entity persistentData");
        }
        
        if (recordedNBT != null) {
            BlockPos spawnPos = BlockPos.containing(storiousEntity.position());

            // 直接从NBT数据恢复实体，支持MCA村民和原版村民
            Entity restoredEntity = EntityType.loadEntityRecursive(
                recordedNBT, serverLevel, 
                entity -> {
                    entity.setPos(storiousEntity.getX(), storiousEntity.getY(), storiousEntity.getZ());
                    return entity;
                }
            );

            if (restoredEntity != null) {
                // 设置最大生命值
                if (restoredEntity instanceof LivingEntity livingEntity) {
                    livingEntity.setHealth(livingEntity.getMaxHealth());
                }
                
                // 添加到世界
                serverLevel.addFreshEntity(restoredEntity);
                
                // 移除原始 StoriousEntity
                storiousEntity.remove(Entity.RemovalReason.DISCARDED);

                // System.out.println("StoriousEntity has been successfully transformed into original villager.");
                return;
            }
            
            // 如果直接从NBT恢复失败，尝试生成新村民
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

                // System.out.println("StoriousEntity has been successfully transformed into Villager.");
            } else {
                // System.out.println("Failed to spawn Villager.");
            }
        } else {
            // System.out.println("No recorded data found for this StoriousEntity.");
        }
    }
}
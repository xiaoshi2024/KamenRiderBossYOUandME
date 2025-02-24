package com.xiaoshi2022.kamen_rider_boss_you_and_me.util;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.ModEntityTypes;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.GiifuDemosEntity;
import forge.net.mca.entity.EntitiesMCA;
import forge.net.mca.entity.VillagerEntityMCA;
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
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.UUID;


public class VillagerTransformationUtil {
    // 全局存储：记录人类的NBT数据和UUID
    private static final HashMap<UUID, CompoundTag> villagerRecord = new HashMap<>();

    // 人类 -> 怪人
    public static void transformToGiifuDemos(ServerLevel level, VillagerEntityMCA mcaVillager, CompoundTag tag) {
        // 获取目标实体的位置
        BlockPos spawnPos = BlockPos.containing(mcaVillager.position());

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
            spawnedEntity.setVillagerData(mcaVillager.getVillagerData());
            spawnedEntity.setVillagerXp(mcaVillager.getVillagerXp());
            spawnedEntity.setTradeOffers(mcaVillager.getOffers());

            // 设置生成位置为人类的当前位置
            spawnedEntity.setPos(mcaVillager.getX(), mcaVillager.getY(), mcaVillager.getZ());

            // 记录人类的NBT数据和UUID
            CompoundTag villagerNBT = new CompoundTag();
            mcaVillager.save(villagerNBT);
            villagerRecord.put(spawnedEntity.getUUID(), villagerNBT);

            // 移除原始的 VillagerEntityMCA
            mcaVillager.remove(Entity.RemovalReason.DISCARDED);

            System.out.println("VillagerEntityMCA has been successfully transformed into GiifuDemosEntity.");
        } else {
            System.out.println("Failed to spawn GiifuDemosEntity.");
        }
    }

    // 怪人 -> 人类
    public static void transformToVillager(ServerLevel serverLevel, GiifuDemosEntity giifuDemosEntity, CompoundTag tag) {
        UUID giifuDemosUUID = giifuDemosEntity.getUUID();
        if (villagerRecord.containsKey(giifuDemosUUID)) {
            CompoundTag recordedNBT = villagerRecord.get(giifuDemosUUID);

            // 创建新的 VillagerEntityMCA
            VillagerEntityMCA newVillager = EntitiesMCA.MALE_VILLAGER.get().spawn(serverLevel, BlockPos.containing(giifuDemosEntity.position()), MobSpawnType.MOB_SUMMONED);

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

                System.out.println("GiifuDemosEntity has been successfully transformed into VillagerEntityMCA.");
            } else {
                System.out.println("Failed to spawn VillagerEntityMCA.");
            }

            // 清除记录
            villagerRecord.remove(giifuDemosUUID);
        } else {
            System.out.println("No recorded data found for this GiifuDemosEntity.");
        }
    }
}
package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "kamen_rider_boss_you_and_me", bus = Mod.EventBusSubscriber.Bus.MOD)
public class RiderZombieSpawner {

    // 注册实体属性
    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        // 这里需要确保你的ModEntities中有RIDER_ZOMBIE的引用
        // event.put(ModEntities.RIDER_ZOMBIE.get(), RiderZombie.createAttributes().build());
    }

    // 注册生成位置
    @SubscribeEvent
    public static void registerSpawnPlacements(SpawnPlacementRegisterEvent event) {
        // 这里需要确保你的ModEntities中有RIDER_ZOMBIE的引用
        // event.register(ModEntities.RIDER_ZOMBIE.get(), SpawnPlacements.Type.ON_GROUND,
        //         Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, RiderZombieSpawner::checkSpawnRules, SpawnPlacementRegisterEvent.Operation.REPLACE);
    }

    // 检查生成规则
    private static boolean checkSpawnRules(EntityType<? extends Mob> type, Level level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        return canSpawnAt(level, pos, random);
    }

    // 判断是否可以在指定位置生成
    private static boolean canSpawnAt(Level level, BlockPos pos, RandomSource random) {
        // 检查亮度
        if (level.getBrightness(LightLayer.BLOCK, pos) > 0) {
            return false;
        }

        // 检查方块
        BlockState blockState = level.getBlockState(pos.below());
        return level.getDifficulty() != net.minecraft.world.Difficulty.PEACEFUL &&
                canSpawnOn(blockState) &&
                level.getBlockState(pos).isAir() &&
                level.getBlockState(pos.above()).isAir();
    }

    // 判断是否可以在指定方块上生成
    private static boolean canSpawnOn(BlockState blockState) {
        return blockState.canOcclude() && blockState.isCollisionShapeFullBlock(null, null);
    }

    // 手动生成RiderZombie的方法（供其他地方调用）
    public static void spawnRiderZombie(ServerLevel level, BlockPos pos, int count) {
        if (!level.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)) {
            return;
        }

        // 这里需要使用正确的实体类型
        // 暂时注释掉，因为我们还没有注册实体类型
        /*
        for (int i = 0; i < count; i++) {
            // 稍微随机化生成位置
            BlockPos spawnPos = pos.offset(
                    level.random.nextInt(6) - 3,
                    0,
                    level.random.nextInt(6) - 3
            );

            // 确保生成位置有效
            if (canSpawnAt(level, spawnPos, level.random)) {
                RiderZombie zombie = ModEntities.RIDER_ZOMBIE.get().create(level);
                if (zombie != null) {
                    zombie.setPos(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D);
                    zombie.finalizeSpawn(level, level.getCurrentDifficultyAt(spawnPos), MobSpawnType.MOB_SUMMONED, null, null);
                    ForgeEventFactory.onFinalizeSpawn(zombie, level, level.getCurrentDifficultyAt(spawnPos), MobSpawnType.MOB_SUMMONED, null, null);
                    level.addFreshEntity(zombie);
                }
            }
        }
        */
    }
}
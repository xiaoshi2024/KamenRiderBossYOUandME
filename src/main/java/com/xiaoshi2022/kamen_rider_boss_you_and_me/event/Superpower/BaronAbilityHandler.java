package com.xiaoshi2022.kamen_rider_boss_you_and_me.event.Superpower;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.rider_barons.rider_baronsItem;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.BaronBananaEnergyEntity;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.ModEntityTypes;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.KRBVariables;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 假面骑士基础巴隆能力处理器
 * 实现香蕉能量实体和缓慢效果
 */
@Mod.EventBusSubscriber(modid = "kamen_rider_boss_you_and_me", bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class BaronAbilityHandler {

    /* ====== 数值常量 ====== */
    private static final int BANANA_ENERGY_COOLDOWN = 200; // 香蕉能量技能冷却（10秒）
    private static final double ENERGY_COST = 20.0; // 技能消耗的骑士能量

    /* ====== 主 Tick：处理持续效果和冷却 ====== */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent e) {
        if (e.phase != TickEvent.Phase.END || !(e.player instanceof ServerPlayer sp)) return;
        
        // 获取玩家变量
        KRBVariables.PlayerVariables variables = sp.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new KRBVariables.PlayerVariables());
        
        // 检查是否穿着基础巴隆盔甲
        boolean isBaron = isBaronArmorEquipped(sp);
        
        // 处理冷却时间
        handleCooldowns(variables, sp.level().getGameTime());
        
        // 更新套装基础效果
        updateBaseEffects(sp, variables, isBaron);
    }

    /* ====== 能力触发方法（可被网络包调用） ====== */

    /**
     * 香蕉能量：V键触发，在玩家前方生成香蕉能量实体，对范围内敌人造成缓慢效果
     */
    public static void tryBananaEnergy(ServerPlayer sp) {
        if (!isBaronArmorEquipped(sp) || !sp.isAlive() || isPlayerControlled(sp)) return;
        
        KRBVariables.PlayerVariables variables = sp.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new KRBVariables.PlayerVariables());
        long currentTime = sp.level().getGameTime();
        
        // 检查冷却时间
        if (variables.baron_banana_energy_cooldown > currentTime) {
            long remaining = (variables.baron_banana_energy_cooldown - currentTime) / 20;
            sp.sendSystemMessage(net.minecraft.network.chat.Component.literal("技能冷却中，还需 " + remaining + " 秒"));
            return;
        }
        
        // 检查骑士能量
        if (!RiderEnergyHandler.consumeRiderEnergy(sp, ENERGY_COST)) {
            sp.sendSystemMessage(net.minecraft.network.chat.Component.literal("骑士能量不足，需要 " + (int)ENERGY_COST + " 点能量"));
            return;
        }
        
        // 设置冷却时间
        variables.baron_banana_energy_cooldown = currentTime + BANANA_ENERGY_COOLDOWN;
        variables.syncPlayerVariables(sp);
        
        // 生成香蕉能量实体
        spawnBananaEnergyEntity(sp);
        
        // 播放技能音效
        playSound(sp, SoundEvents.PLAYER_SPLASH_HIGH_SPEED, 1.0f, 1.2f);
    }

    /* ====== 辅助方法 ====== */

    // 检查玩家是否穿着全套基础巴隆盔甲
    private static boolean isBaronArmorEquipped(ServerPlayer player) {
        // 检查胸甲是否为基础巴隆盔甲
        return player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST).getItem() instanceof rider_baronsItem;
    }
    
    /**
      * 检查玩家是否被控（有控制类负面效果）
      */
     private static boolean isPlayerControlled(ServerPlayer player) {
         // 检查玩家是否有以下控制类负面效果
         return player.hasEffect(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN) || // 缓慢
                player.hasEffect(net.minecraft.world.effect.MobEffects.BLINDNESS) || // 失明
                player.hasEffect(net.minecraft.world.effect.MobEffects.CONFUSION) || // 反胃
                player.hasEffect(net.minecraft.world.effect.MobEffects.POISON) || // 中毒
                player.hasEffect(net.minecraft.world.effect.MobEffects.WITHER) || // 凋零
                player.hasEffect(net.minecraft.world.effect.MobEffects.WEAKNESS); // 虚弱
     }

    // 处理冷却时间
    private static void handleCooldowns(KRBVariables.PlayerVariables variables, long currentTime) {
        // 不需要特别处理，冷却时间在触发技能时设置
    }

    // 更新基础效果
    private static void updateBaseEffects(ServerPlayer sp, KRBVariables.PlayerVariables variables, boolean isBaron) {
        // 基础巴隆盔甲的被动效果可以在这里实现
        // 暂时不需要特殊的被动效果
    }

    // 生成香蕉能量实体
    private static void spawnBananaEnergyEntity(ServerPlayer sp) {
        if (!sp.level().isClientSide()) {
            // 创建香蕉能量实体
            BaronBananaEnergyEntity energyEntity = ModEntityTypes.BARON_BANANA_ENERGY.get().create(sp.level());
            if (energyEntity != null) {
                // 获取玩家视线方向的目标位置
                // 射线检测距离为10格
                net.minecraft.world.phys.HitResult hitResult = sp.pick(10.0D, 0.0F, false);
                if (hitResult.getType() == net.minecraft.world.phys.HitResult.Type.BLOCK) {
                    // 如果射线击中方块，实体生成在方块位置上方
                    net.minecraft.core.BlockPos blockPos = ((net.minecraft.world.phys.BlockHitResult)hitResult).getBlockPos();
                    energyEntity.setPos(blockPos.getX() + 0.5, blockPos.getY() + 1.0, blockPos.getZ() + 0.5);
                } else {
                    // 如果没有击中方块，实体生成在视线方向的最远端
                    net.minecraft.world.phys.Vec3 lookVec = sp.getViewVector(1.0F);
                    energyEntity.setPos(sp.getX() + lookVec.x * 10.0, sp.getY() + sp.getEyeHeight() + lookVec.y * 10.0, sp.getZ() + lookVec.z * 10.0);
                }
                
                // 设置实体的所有者为当前玩家
                energyEntity.setOwnerId(sp.getUUID());
                
                // 添加实体到世界
                sp.level().addFreshEntity(energyEntity);
            }
        }
    }

    // 播放音效的辅助方法
    private static void playSound(ServerPlayer player, SoundEvent sound, float volume, float pitch) {
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), sound, 
                net.minecraft.sounds.SoundSource.PLAYERS, volume, pitch);
    }
}
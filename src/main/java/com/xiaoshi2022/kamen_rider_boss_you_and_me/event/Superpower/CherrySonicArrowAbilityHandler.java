package com.xiaoshi2022.kamen_rider_boss_you_and_me.event.Superpower;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.CherryEnergyArrowEntity;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.event.Superpower.RiderEnergyHandler;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me;
import com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.sonicarrow;
import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModSounds;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 樱桃变种音速弓技能处理器
 * 处理骑士技能1：当玩家手持樱桃变种音速弓时，按下V键触发特殊技能
 * 发射樱桃形能量箭矢，命中后束缚并压碎目标
 */
@Mod.EventBusSubscriber(modid = kamen_rider_boss_you_and_me.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class CherrySonicArrowAbilityHandler {

    /* ====== 数值常量 ====== */
    private static final int CHERRY_ARROW_COOLDOWN = 0; // 技能无冷却时间（tick）
    private static final double ENERGY_COST = 20.0; // 技能消耗的骑士能量
    private static final double ARROW_DAMAGE = 15.0; // 樱桃能量箭矢的基础伤害

    /* ====== 主 Tick：冷却管理 ====== */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent e) {
        if (e.phase != TickEvent.Phase.END || !(e.player instanceof ServerPlayer sp)) return;
        
        // 技能冷却管理
        if (sp.getPersistentData().contains("cherry_sonic_arrow_cooldown")) {
            int cooldown = sp.getPersistentData().getInt("cherry_sonic_arrow_cooldown");
            if (cooldown > 0) {
                sp.getPersistentData().putInt("cherry_sonic_arrow_cooldown", cooldown - 1);
            } else {
                sp.getPersistentData().remove("cherry_sonic_arrow_cooldown");
            }
        }
    }

    /* ====== 能力触发方法（可被网络包直接调用） ====== */
    
    /**
     * 骑士技能1：樱桃能量箭矢
     * 当玩家手持音速弓樱桃模式时，按下V键发射樱桃形能量箭矢，命中后束缚并压碎目标
     */
    public static void tryCherryEnergyArrow(ServerPlayer sp) {
        // 检查玩家状态
        if (!sp.isAlive() || isPlayerControlled(sp)) return;
        
        // 检查是否手持音速弓樱桃模式
        ItemStack mainHandItem = sp.getMainHandItem();
        if (!(mainHandItem.getItem() instanceof sonicarrow)) return;
        
        sonicarrow sonicArrow = (sonicarrow) mainHandItem.getItem();
        if (sonicArrow.getCurrentMode(mainHandItem) != sonicarrow.Mode.CHERRY) return;
        
        // 检查冷却
        if (sp.getPersistentData().contains("cherry_sonic_arrow_cooldown")) return;
        
        // 检查并消耗骑士能量
        if (!RiderEnergyHandler.consumeRiderEnergy(sp, ENERGY_COST)) {
            return;
        }
        
        // 播放 "Cherry Energy！" 音效
        sp.level().playSound(null, sp.getX(), sp.getY(), sp.getZ(),
                ModSounds.CHERYYENERGY.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        
        // 射线检测，获取视线方向的目标位置
        HitResult hitResult = sp.pick(50.0D, 0.0F, false);
        
        // 创建并发射樱桃能量箭矢
        CherryEnergyArrowEntity arrow = new CherryEnergyArrowEntity(sp.level(), sp);
        arrow.setDamage(ARROW_DAMAGE);
        arrow.shootFromRotation(sp, sp.getXRot(), sp.getYRot(), 0.0F, 3.0F, 1.0F);
        sp.level().addFreshEntity(arrow);
        
        // 设置冷却
        sp.getPersistentData().putInt("cherry_sonic_arrow_cooldown", CHERRY_ARROW_COOLDOWN);
    }

    /* ====== 工具方法 ====== */
    
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
    
    /* 禁止实例化 */
    private CherrySonicArrowAbilityHandler() {}
    
}
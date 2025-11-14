package com.xiaoshi2022.kamen_rider_boss_you_and_me.advancement;

import com.google.gson.JsonObject;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.bloodline.effects.ModEffects;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.varibales.KRBVariables;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.PlayerBloodlineHelper;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;

public class TamedKivatTrigger extends SimpleCriterionTrigger<TamedKivatTrigger.Instance> {

    public static final ResourceLocation ID = new ResourceLocation(kamen_rider_boss_you_and_me.MODID, "tamed_kivat");
    public static final TamedKivatTrigger INSTANCE = new TamedKivatTrigger();

    // 新增：牙血鬼血脉觉醒成就ID
    public static final ResourceLocation FANG_BLOODLINE_AWAKEN_ID = new ResourceLocation(kamen_rider_boss_you_and_me.MODID, "fang_bloodline_awaken");
    
    // 新增：检测并觉醒牙血鬼血脉
    public static void checkAndAwakenFangBloodline(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            // 检查玩家是否已经有足够高的牙血鬼血脉纯度
            float purity = PlayerBloodlineHelper.getFangPurity(player);
            
            // 如果纯度达到阈值并且玩家还不是牙血鬼血脉一族
            if (purity >= 0.7f) { // 70% 纯度阈值，可以调整
                KRBVariables.PlayerVariables variables = serverPlayer.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new KRBVariables.PlayerVariables());
                
                // 检查是否已经觉醒过牙血鬼血脉
                if (!variables.isFangBloodline) { // 使用专用的isFangBloodline字段来标记牙血鬼血脉状态
                    // 设置为牙血鬼血脉一族
                    variables.isFangBloodline = true;
                    variables.syncPlayerVariables(serverPlayer);
                    
                    // 应用牙血鬼之脉效果
                    serverPlayer.addEffect(new MobEffectInstance(
                            ModEffects.FANG_BLOODLINE.get(), 
                            Integer.MAX_VALUE, 
                            0, 
                            false, 
                            false, 
                            true
                    ));
                    
                    // 显示觉醒消息
                    serverPlayer.displayClientMessage(
                            Component.translatable("message.fang_bloodline.awaken"), 
                            true
                    );
                    
                    // 触发成就：觉醒！牙血鬼之脉
                    INSTANCE.trigger(serverPlayer);
                }
            }
        }
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public Instance createInstance(JsonObject json, ContextAwarePredicate player, DeserializationContext ctx) {
        return new Instance(player);
    }

    /* 服务器主动触发 */
    public void trigger(ServerPlayer player) {
        this.trigger(player, Instance::test);
    }

    public static class Instance extends AbstractCriterionTriggerInstance {
        public Instance(ContextAwarePredicate player) {
            super(ID, player);
        }

        public boolean test() {   // 本示例无需额外条件
            return true;
        }
    }
}
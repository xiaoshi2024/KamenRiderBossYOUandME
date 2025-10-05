package com.xiaoshi2022.kamen_rider_boss_you_and_me.advancement;

import com.google.gson.JsonObject;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class MixedRaceTrigger extends SimpleCriterionTrigger<MixedRaceTrigger.Instance> {

    public static final ResourceLocation ID = new ResourceLocation(kamen_rider_boss_you_and_me.MODID, "mixed_race");
    // 私有化构造函数，防止外部实例化
    private MixedRaceTrigger() {
    }
    
    // 单例实例
    private static final MixedRaceTrigger INSTANCE = new MixedRaceTrigger();
    
    // 提供获取实例的方法
    public static MixedRaceTrigger getInstance() {
        return INSTANCE;
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
        this.trigger(player, instance -> instance.test());
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
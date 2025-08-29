package com.xiaoshi2022.kamen_rider_boss_you_and_me.util;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public final class KickDamageHelper {

    /* 与剧中表现对齐的踢击伤害表（半心 = 1） */
    // 假设该方法存在，添加黑暗Kiva的伤害计算
    public static float getKickDamage(Entity entity) {
        if (entity instanceof LivingEntity livingEntity) {
            ItemStack helmet = livingEntity.getItemBySlot(EquipmentSlot.HEAD);
            if (helmet.getItem() == ModItems.DARK_KIVA_HELMET.get()) {
                // 黑暗Kiva的特殊伤害计算，例如：基础伤害+吸血加成
                return 40.0f; // 示例数值，可根据需求调整
            }
            if (helmet.is(ModItems.MARIKA_HELMET.get()))           return 35F; // 假面骑士玛丽卡 - 最低伤害基准
            if (helmet.is(ModItems.SIGURD_HELMET.get()))           return 38F; // 假面骑士西格
            if (helmet.is(ModItems.RIDER_BARONS_HELMET.get()))     return 40F; // 巴隆
            if (helmet.is(ModItems.BARON_LEMON_HELMET.get()))       return 42F; // 巴隆·柠檬
            if (helmet.is(ModItems.ZANGETSU_SHIN_HELMET.get()))    return 45F; // 斩月·真
            if (helmet.is(ModItems.DUKE_HELMET.get()))             return 48F; // 公爵
            if (helmet.is(ModItems.TYRANT_HELMET.get()))           return 50F; // 假面骑士暴君
            if (helmet.is(ModItems.DARK_ORANGELS_HELMET.get()))    return 55F; // 黑暗铠武
            if (helmet.is(ModItems.DARK_KIVA_HELMET.get()))        return 60F; // 黑暗月骑 - 参考剧中表现设置最高伤害
    
            return 20F; // 默认
        }
        return 0.0f;
    }

    public static float getKickExplosionRadius(Entity entity) {
        ItemStack helmet = getHelmet(entity);

        if (helmet.is(ModItems.MARIKA_HELMET.get()))           return 2.0F;
        if (helmet.is(ModItems.SIGURD_HELMET.get()))           return 2.2F;
        if (helmet.is(ModItems.RIDER_BARONS_HELMET.get()))     return 2.5F;
        if (helmet.is(ModItems.BARON_LEMON_HELMET.get()))       return 2.8F;
        if (helmet.is(ModItems.ZANGETSU_SHIN_HELMET.get()))    return 3.0F;
        if (helmet.is(ModItems.DUKE_HELMET.get()))             return 3.5F;
        if (helmet.is(ModItems.TYRANT_HELMET.get()))           return 3.8F;
        if (helmet.is(ModItems.DARK_ORANGELS_HELMET.get()))    return 4.0F;
        if (helmet.is(ModItems.DARK_KIVA_HELMET.get()))        return 4.5F; // 黑暗月骑 - 更大的爆炸范围

        return 2.0F; // 默认
    }

    private static ItemStack getHelmet(Entity e) {
        return e instanceof LivingEntity l ? l.getItemBySlot(EquipmentSlot.HEAD) : ItemStack.EMPTY;
    }

    private KickDamageHelper() {}
}
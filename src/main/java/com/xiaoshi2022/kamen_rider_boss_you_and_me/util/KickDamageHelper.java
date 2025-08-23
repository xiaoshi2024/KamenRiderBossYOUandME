package com.xiaoshi2022.kamen_rider_boss_you_and_me.util;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public final class KickDamageHelper {

    /* 与剧中表现对齐的踢击伤害表（半心 = 1） */
    public static float getKickDamage(Entity entity) {
        ItemStack helmet = getHelmet(entity);

        if (helmet.is(ModItems.BARON_LEMON_HELMET.get()))       return 16F; // 巴隆·柠檬
        if (helmet.is(ModItems.DUKE_HELMET.get()))             return 20F; // 公爵
        if (helmet.is(ModItems.MARIKA_HELMET.get()))           return 14F; // 假面骑士玛丽卡
        if (helmet.is(ModItems.ZANGETSU_SHIN_HELMET.get()))    return 18F; // 斩月·真
        if (helmet.is(ModItems.DARK_ORANGELS_HELMET.get()))    return 22F; // 黑暗铠武
        if (helmet.is(ModItems.RIDER_BARONS_HELMET.get()))     return 16F; // 巴隆
        if (helmet.is(ModItems.SIGURD_HELMET.get()))           return 15F; // 假面骑士西格

        return 8F; // 默认
    }

    public static float getKickExplosionRadius(Entity entity) {
        ItemStack helmet = getHelmet(entity);

        if (helmet.is(ModItems.BARON_LEMON_HELMET.get()))       return 2.5F;
        if (helmet.is(ModItems.DUKE_HELMET.get()))             return 3.5F;
        if (helmet.is(ModItems.MARIKA_HELMET.get()))           return 2.0F;
        if (helmet.is(ModItems.ZANGETSU_SHIN_HELMET.get()))    return 3.0F;
        if (helmet.is(ModItems.DARK_ORANGELS_HELMET.get()))    return 4.0F;
        if (helmet.is(ModItems.RIDER_BARONS_HELMET.get()))     return 2.5F;
        if (helmet.is(ModItems.SIGURD_HELMET.get()))           return 2.2F;

        return 2.0F; // 默认
    }

    private static ItemStack getHelmet(Entity e) {
        return e instanceof LivingEntity l ? l.getItemBySlot(EquipmentSlot.HEAD) : ItemStack.EMPTY;
    }

    private KickDamageHelper() {}
}
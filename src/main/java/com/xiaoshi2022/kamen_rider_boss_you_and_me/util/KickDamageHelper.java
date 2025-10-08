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
            if (helmet.is(ModItems.MARIKA_HELMET.get()))           return 73F; // 假面骑士玛丽卡 - 最低伤害基准
            if (helmet.is(ModItems.SIGURD_HELMET.get()))           return 68F; // 假面骑士西格
            if (helmet.is(ModItems.RIDER_BARONS_HELMET.get()))     return 70F; // 巴隆
            if (helmet.is(ModItems.BARON_LEMON_HELMET.get()))       return 79F; // 巴隆·柠檬
            if (helmet.is(ModItems.ZANGETSU_SHIN_HELMET.get()))    return 80F; // 斩月·真
            if (helmet.is(ModItems.DUKE_HELMET.get()))             return 78F; // 公爵
            if (helmet.is(ModItems.TYRANT_HELMET.get()))           return 98F; // 假面骑士暴君
            if (helmet.is(ModItems.DARK_ORANGELS_HELMET.get()))    return 99F; // 黑暗铠武
            if (helmet.is(ModItems.DARK_KIVA_HELMET.get()))        return 101F; // 黑暗月骑 - 参考剧中表现设置最高伤害
            if (helmet.is(ModItems.RIDERNECROM_HELMET.get()))      return 89F; // 假面骑士幽冥

            if (helmet.is(ModItems.EVIL_BATS_HELMET.get()))        return 87F; // 艾比尔
    
            return 20F; // 默认
        }
        return 0.0f;
    }

    /**
     * 获取不同骑士踢的爆炸半径
     */
    public static float getKickExplosionRadius(Entity entity) {
        ItemStack helmet = getHelmet(entity);

        if (helmet.is(ModItems.MARIKA_HELMET.get()))           return 3.0F;
        if (helmet.is(ModItems.SIGURD_HELMET.get()))           return 3.2F;
        if (helmet.is(ModItems.RIDER_BARONS_HELMET.get()))     return 3.5F;
        if (helmet.is(ModItems.BARON_LEMON_HELMET.get()))       return 3.8F;
        if (helmet.is(ModItems.ZANGETSU_SHIN_HELMET.get()))    return 4.0F;
        if (helmet.is(ModItems.DUKE_HELMET.get()))             return 4.5F;
        if (helmet.is(ModItems.TYRANT_HELMET.get()))           return 4.8F;
        if (helmet.is(ModItems.DARK_ORANGELS_HELMET.get()))    return 5.0F;
        if (helmet.is(ModItems.DARK_KIVA_HELMET.get()))        return 6.5F; // 黑暗月骑 - 更大的爆炸范围
        if (helmet.is(ModItems.RIDERNECROM_HELMET.get()))      return 5.2F; // 假面骑士幽冥 - 中等爆炸范围

        if (helmet.is(ModItems.EVIL_BATS_HELMET.get()))        return 5.4F; // 艾比尔

        return 2.0F; // 默认
    }

    /**
     * 获取不同骑士踢的方块破坏强度（0.0-1.0，1.0表示完全破坏）
     */
    public static float getKickBlockDestructionStrength(Entity entity) {
        ItemStack helmet = getHelmet(entity);

        if (helmet.is(ModItems.MARIKA_HELMET.get()))           return 0.4F; // 较弱的破坏能力
        if (helmet.is(ModItems.SIGURD_HELMET.get()))           return 0.5F; // 中等破坏能力
        if (helmet.is(ModItems.RIDER_BARONS_HELMET.get()))     return 0.6F; // 较强的破坏能力
        if (helmet.is(ModItems.BARON_LEMON_HELMET.get()))       return 0.7F; // 很强的破坏能力
        if (helmet.is(ModItems.ZANGETSU_SHIN_HELMET.get()))    return 0.75F; // 极强的破坏能力
        if (helmet.is(ModItems.DUKE_HELMET.get()))             return 0.8F; // 非常强的破坏能力
        if (helmet.is(ModItems.TYRANT_HELMET.get()))           return 0.9F; // 接近完全破坏
        if (helmet.is(ModItems.DARK_ORANGELS_HELMET.get()))    return 0.95F; // 几乎完全破坏
        if (helmet.is(ModItems.DARK_KIVA_HELMET.get()))        return 1.0F; // 完全破坏所有可破坏方块
        if (helmet.is(ModItems.RIDERNECROM_HELMET.get()))      return 0.85F; // 超强破坏能力

        if (helmet.is(ModItems.EVIL_BATS_HELMET.get()))        return 0.88F; // 极强破坏能力

        return 0.3F; // 默认较弱的破坏能力
    }

    /**
     * 检查方块是否为基夫石像（不应被骑士踢破坏）
     */
    public static boolean isGiifuStatue(Entity entity) {
        return entity != null && entity.getType().equals(net.minecraft.world.entity.EntityType.ENDER_DRAGON) && 
               entity.hasCustomName() && "基夫石像".equals(entity.getCustomName().getString());
    }

    private static ItemStack getHelmet(Entity e) {
        return e instanceof LivingEntity l ? l.getItemBySlot(EquipmentSlot.HEAD) : ItemStack.EMPTY;
    }

    private KickDamageHelper() {}
}
package com.xiaoshi2022.kamen_rider_boss_you_and_me.event;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.ModEntityTypes;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.kivat.KivatBatTwoNd;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class KivatItemTossHandler {
    @SubscribeEvent
    public static void onPlayerToss(ItemTossEvent e) {
        ItemEntity ie = e.getEntity();
        ItemStack stack = ie.getItem();
        if (!stack.is(ModItems.KIVAT_BAT_TWO_ND_ITEM.get())) return;

        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.getBoolean("FromKivatEntity")) return;

        e.setCanceled(true);

        Level level = ie.level();
        KivatBatTwoNd bat = ModEntityTypes.KIVAT_BAT_II.get().create(level);
        if (bat == null) return;

        // 去掉坐标相关字段，防止被 load 覆盖
        tag.remove("Pos");
        tag.remove("Rotation");
        tag.remove("Motion");

        Player player = e.getPlayer();
        Vec3 pos = player.position().add(player.getLookAngle().scale(1.0));

        // 先 load 其他数据（血量、名字、模式等）
        bat.load(tag);               // 读存档数据
        bat.absMoveTo(pos.x, pos.y, pos.z, player.getYRot(), 0); // 关键：强刷坐标
        bat.setDeltaMovement(Vec3.ZERO);

        // 安全地停止导航
        if (bat.getNavigation() != null) {
            bat.getNavigation().stop();
        }

        // 再手动设置新位置
        bat.moveTo(pos.x, pos.y + 0.5, pos.z, player.getYRot(), 0);

        if (tag.hasUUID("OwnerUUID")) {
            bat.tame(player);
        }

        level.addFreshEntity(bat);
        ie.discard();
    }
}
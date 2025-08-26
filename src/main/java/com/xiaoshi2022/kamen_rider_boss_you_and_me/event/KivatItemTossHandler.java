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

        // 取消原本的掉落，避免玩家真正捡起
        e.setCanceled(true);

        Level level = ie.level();
        KivatBatTwoNd bat = ModEntityTypes.KIVAT_BAT_II.get().create(level);
        if (bat == null) return;

        // 位置：直接出现在玩家面前 1 格
        Player player = e.getPlayer();
        Vec3 pos = player.position().add(player.getLookAngle().scale(1.0));
        bat.moveTo(pos.x, pos.y + 0.5, pos.z, player.getYRot(), 0);

        // 恢复数据
        bat.load(tag);
        if (tag.hasUUID("OwnerUUID")) {
            bat.tame(player);
        }

        level.addFreshEntity(bat);
        ie.discard();   // 物品消失
    }
}
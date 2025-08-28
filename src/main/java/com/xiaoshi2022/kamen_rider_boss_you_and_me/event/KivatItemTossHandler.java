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
        Level level = e.getEntity().level();
        if (level.isClientSide) return;          // 只在服务端执行

        ItemEntity ie = e.getEntity();
        ItemStack stack = ie.getItem();
        if (!stack.is(ModItems.KIVAT_BAT_TWO_ND_ITEM.get())) return;

        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.getBoolean("FromKivatEntity")) return;

        e.setCanceled(true);                     // 1. 取消原掉落
        ie.discard();                            // 2. 把原来的 ItemEntity 移除

        KivatBatTwoNd bat = ModEntityTypes.KIVAT_BAT_II.get().create(level);
        if (bat == null) return;

        tag.remove("Pos");
        tag.remove("Rotation");
        tag.remove("Motion");

        Player player = e.getPlayer();
        Vec3 pos = player.position().add(player.getLookAngle().scale(1.0));

        bat.load(tag);
        bat.moveTo(pos.x, pos.y + 0.5, pos.z, player.getYRot(), 0);
        bat.setDeltaMovement(Vec3.ZERO);
        if (tag.hasUUID("OwnerUUID")) bat.tame(player);

        level.addFreshEntity(bat);               // 3. 生成实体
    }
}
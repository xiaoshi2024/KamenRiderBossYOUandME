package com.xiaoshi2022.kamen_rider_boss_you_and_me.network.henshin;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public final class EvilArmorSequence {
    public static void equip(ServerPlayer sp) {
        sp.getInventory().armor.set(3, new ItemStack(ModItems.EVIL_BATS_HELMET.get()));
        sp.getInventory().armor.set(2, new ItemStack(ModItems.EVIL_BATS_CHESTPLATE.get()));
        sp.getInventory().armor.set(1, new ItemStack(ModItems.EVIL_BATS_LEGGINGS.get()));
    }
}

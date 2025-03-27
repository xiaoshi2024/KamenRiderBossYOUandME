package com.xiaoshi2022.kamen_rider_boss_you_and_me.procedures;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.InvisibilityPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.PacketHandler;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

public class TransformPlayerProcedure {
    public static void execute(Entity entity) {
        if (entity == null) {
            return;
        }

        // 检查玩家是否装备了其他盔甲
        boolean hasOtherArmor = false;
        if (entity instanceof Player player) {
            for (int i = 0; i < player.getInventory().armor.size(); i++) {
                ItemStack stack = player.getInventory().armor.get(i);
                if (!stack.isEmpty() &&
                        stack.getItem() != ModItems.RIDERNECROM_HELMET.get() &&
                        stack.getItem() != ModItems.RIDERNECROM_CHESTPLATE.get() &&
                        stack.getItem() != ModItems.RIDERNECROM_LEGGINGS.get() &&
                        stack.getItem() != ModItems.RIDERNECROM_BOOTS.get()) {
                    hasOtherArmor = true;
                    break;
                }
            }
        } else if (entity instanceof LivingEntity living) {
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                ItemStack stack = living.getItemBySlot(slot);
                if (!stack.isEmpty() &&
                        stack.getItem() != ModItems.RIDERNECROM_HELMET.get() &&
                        stack.getItem() != ModItems.RIDERNECROM_CHESTPLATE.get() &&
                        stack.getItem() != ModItems.RIDERNECROM_LEGGINGS.get() &&
                        stack.getItem() != ModItems.RIDERNECROM_BOOTS.get()) {
                    hasOtherArmor = true;
                    break;
                }
            }
        }

        if (hasOtherArmor) {
            // 如果装备了其他盔甲，发送提示信息
            if (entity instanceof Player player) {
                player.displayClientMessage(Component.literal("你不能在装备其他盔甲的情况下变身！"), true);
            }
            return;
        }

        // 如果没有装备其他盔甲，执行变身逻辑
        transformPlayer(entity);
    }

    private static void transformPlayer(Entity entity) {
        if (entity instanceof Player player) {
            // 清空盔甲槽位
            player.getInventory().armor.set(3, new ItemStack(Blocks.AIR));
            player.getInventory().armor.set(2, new ItemStack(Blocks.AIR));
            player.getInventory().armor.set(1, new ItemStack(Blocks.AIR));
            player.getInventory().armor.set(0, new ItemStack(Blocks.AIR));
            player.getInventory().setChanged();

            // 设置变身盔甲
            player.getInventory().armor.set(3, new ItemStack(ModItems.RIDERNECROM_HELMET.get()));
            player.getInventory().armor.set(2, new ItemStack(ModItems.RIDERNECROM_CHESTPLATE.get()));
            player.getInventory().armor.set(1, new ItemStack(ModItems.RIDERNECROM_LEGGINGS.get()));
            player.getInventory().armor.set(0, new ItemStack(ModItems.RIDERNECROM_BOOTS.get()));
            player.getInventory().setChanged();

            // 发送隐身效果网络包
            PacketHandler.INSTANCE.sendToServer(new InvisibilityPacket(true));
        } else if (entity instanceof LivingEntity living) {
            // 清空盔甲槽位
            living.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Blocks.AIR));
            living.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Blocks.AIR));
            living.setItemSlot(EquipmentSlot.LEGS, new ItemStack(Blocks.AIR));
            living.setItemSlot(EquipmentSlot.FEET, new ItemStack(Blocks.AIR));

            // 设置变身盔甲
            living.setItemSlot(EquipmentSlot.HEAD, new ItemStack(ModItems.RIDERNECROM_HELMET.get()));
            living.setItemSlot(EquipmentSlot.CHEST, new ItemStack(ModItems.RIDERNECROM_CHESTPLATE.get()));
            living.setItemSlot(EquipmentSlot.LEGS, new ItemStack(ModItems.RIDERNECROM_LEGGINGS.get()));
            living.setItemSlot(EquipmentSlot.FEET, new ItemStack(ModItems.RIDERNECROM_BOOTS.get()));
        }
    }
}
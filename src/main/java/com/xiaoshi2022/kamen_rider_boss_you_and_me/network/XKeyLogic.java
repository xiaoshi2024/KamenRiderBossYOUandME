package com.xiaoshi2022.kamen_rider_boss_you_and_me.network;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.TwoWeaponItem;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.Two_sidriver;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.ModEntityTypes;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.BatDarksEntity;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.henshin.EvilArmorSequence;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModBossSounds;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

import java.util.Optional;

public final class XKeyLogic {
    private XKeyLogic() {}

    /* -------- ① 加载武器 -------- */
    public static void loadWeapon(ServerPlayer sp, boolean isBat) {
        Optional<SlotResult> opt = CuriosApi.getCuriosInventory(sp)
                .resolve()
                .flatMap(inv -> inv.findFirstCurio(s -> s.getItem() instanceof Two_sidriver));
        if (opt.isEmpty()) return;

        ItemStack belt = opt.get().stack();
        // 二次校验，防止并发
        if (Two_sidriver.getDriverType(belt) != Two_sidriver.DriverType.DEFAULT) return;

        ItemStack weapon = findTwoWeapon(sp);
        if (weapon == null) {      // 空手按了 X，提示一句
            sp.sendSystemMessage(Component.literal("§c需要手持 TwoWeaponItem 才能装载！"));
            return;
        }
        weapon.shrink(1);
        if (isBat) {
            Two_sidriver.setDriverType(belt, Two_sidriver.DriverType.BAT);
            Two_sidriver.syncToTracking(sp, belt);
            sp.sendSystemMessage(Component.literal("§a武器装载——Bat 形态！"));
        } else {
            Two_sidriver.setDriverType(belt, Two_sidriver.DriverType.X);
            Two_sidriver.syncToTracking(sp, belt);
            sp.sendSystemMessage(Component.literal("§a武器装载——X 形态！"));
        }
    }

    /* -------- ② 最终变身 -------- */
    public static void toEvil(ServerPlayer sp) {
        Optional<SlotResult> opt = CuriosApi.getCuriosInventory(sp)
                .resolve()
                .flatMap(inv -> inv.findFirstCurio(s -> s.getItem() instanceof Two_sidriver));
        if (opt.isEmpty()) {
            sp.sendSystemMessage(Component.literal("§c未找到腰带！"));
            return;
        }

        ItemStack belt = opt.get().stack();
        if (Two_sidriver.getDriverType(belt) != Two_sidriver.DriverType.BAT) {
            sp.sendSystemMessage(Component.literal("§c当前不是BAT形态！"));
            return;
        }

        // 查找附近的 BatDarksEntity，优先选择已经绑定到当前玩家的实体
        BatDarksEntity targetEntity = null;
        for (BatDarksEntity batDarksEntity : sp.level().getEntitiesOfClass(BatDarksEntity.class, sp.getBoundingBox().inflate(32.0D))) {
            if (batDarksEntity.getTargetPlayerId() != null && batDarksEntity.getTargetPlayerId().equals(sp.getUUID())) {
                System.out.println("找到已绑定到当前玩家的BatDarksEntity，实体ID: " + batDarksEntity.getId());
                targetEntity = batDarksEntity;
                break;
            } else if (targetEntity == null && batDarksEntity.distanceTo(sp) < 10.0D) {
                System.out.println("找到附近的BatDarksEntity，距离: " + batDarksEntity.distanceTo(sp));
                targetEntity = batDarksEntity;
            }
        }

        // 如果没有找到，自动生成一个新的BatDarksEntity
        if (targetEntity == null) {
            System.out.println("未找到BatDarksEntity，自动生成一个新的实体");
            targetEntity = new BatDarksEntity(ModEntityTypes.BAT_DARKS.get(), sp.level());
            targetEntity.setPos(sp.getX(), sp.getY() + 1, sp.getZ());
            targetEntity.setTargetPlayer(sp); // 绑定玩家
            sp.level().addFreshEntity(targetEntity);
        }
        
        targetEntity.startHenshin(); // 触发变身动画

        // 直接执行变身逻辑（不需要延迟）
        EvilArmorSequence.equip(sp);
        Two_sidriver.setDriverType(belt, Two_sidriver.DriverType.DEFAULT);
        Two_sidriver.setWeaponMode(belt, Two_sidriver.WeaponMode.IDLE);
        Two_sidriver.syncToTracking(sp, belt);

        /* --------------------------------- 停止待机音效 --------------------------------- */
        ResourceLocation soundLoc = new ResourceLocation(
                "kamen_rider_boss_you_and_me",
                "evil_by"
        );
        PacketHandler.sendToAllTracking(
                new SoundStopPacket(sp.getId(), soundLoc),
                sp
        );
        PacketHandler.sendToServer(new SoundStopPacket(sp.getId(), soundLoc));

        // 播放新音效
        sp.level().playSound(null, sp.getX(), sp.getY(), sp.getZ(),
                ModBossSounds.EVILR.get(), SoundSource.PLAYERS, 1, 1);

        // 给剑
        ItemStack batWep = new ItemStack(ModItems.TWO_WEAPON.get());
        TwoWeaponItem.setVariant(batWep, TwoWeaponItem.Variant.BAT);
        sp.getInventory().placeItemBackInInventory(batWep);

        sp.sendSystemMessage(Component.literal("§5最终变身——Evil 装甲完成！"));
    }

    /* 工具方法 */
    private static ItemStack findTwoWeapon(ServerPlayer sp) {
        if (sp.getMainHandItem().getItem() instanceof TwoWeaponItem) return sp.getMainHandItem();
        if (sp.getOffhandItem().getItem() instanceof TwoWeaponItem) return sp.getOffhandItem();
        return null;
    }
}
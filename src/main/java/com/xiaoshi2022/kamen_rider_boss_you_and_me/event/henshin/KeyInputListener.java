package com.xiaoshi2022.kamen_rider_boss_you_and_me.event.henshin;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.TwoWeaponItem;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.Genesis_driver;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.Two_sidriver;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.sengokudrivers_epmty;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.PacketHandler;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.henshin.*;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.CurioUtils;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.KeyBinding;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

import java.util.Optional;

@Mod.EventBusSubscriber(modid = "kamen_rider_boss_you_and_me", value = Dist.CLIENT)
public final class KeyInputListener {

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (!KeyBinding.CHANGE_KEY.isDown()) return;

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        /* ===== 1. 先处理 Two_sidriver 装载武器 ===== */
        if (tryLoadTwoWeaponOnX(player)) return; // 如果成功装载，本次 X 键不再往下走变身逻辑


        // 1. 创世纪驱动器
        Optional<Genesis_driver> genesis = CurioUtils.findFirstCurio(player,
                        stack -> stack.getItem() instanceof Genesis_driver)
                .map(slot -> (Genesis_driver) slot.stack().getItem());

        if (genesis.isPresent()) {
            Genesis_driver belt = genesis.get();
            Genesis_driver.BeltMode mode = belt.getMode(CurioUtils.findFirstCurio(player,
                    stack -> stack.getItem() instanceof Genesis_driver).get().stack());

            String riderType = switch (mode) {
                case LEMON -> RiderTypes.LEMON_ENERGY;
                case MELON  -> RiderTypes.MELON_ENERGY;
                case CHERRY -> RiderTypes.CHERRY_ENERGY;
                case PEACH -> RiderTypes.PEACH_ENERGY;
                case DRAGONFRUIT -> RiderTypes.DRAGONFRUIT_ENERGY;
                default -> null;
            };

            if (riderType != null) {
                if (isWearingArmor(player, riderType)) return;   // 已变身
                if (riderType.equals(RiderTypes.CHERRY_ENERGY)) {
                    PacketHandler.INSTANCE.sendToServer(
                            new CherryTransformationRequestPacket(player.getUUID()));
                } else if (riderType.equals(RiderTypes.PEACH_ENERGY)) {
                    PacketHandler.INSTANCE.sendToServer(
                            new MarikaTransformationRequestPacket(player.getUUID()));
                } else if (riderType.equals(RiderTypes.DRAGONFRUIT_ENERGY)) {
                    PacketHandler.INSTANCE.sendToServer(
                            new DragonfruitTransformationRequestPacket(player.getUUID()));
                } else {
                    PacketHandler.INSTANCE.sendToServer(
                            new TransformationRequestPacket(player.getUUID(), riderType, false));
                }
            }
            return;
        }

        // 2. 战极腰带
        Optional<sengokudrivers_epmty> sengoku =
                CurioUtils.findFirstCurio(player,
                                stack -> stack.getItem() instanceof sengokudrivers_epmty)
                        .map(slot -> (sengokudrivers_epmty) slot.stack().getItem());

        if (sengoku.isPresent()) {
            sengokudrivers_epmty.BeltMode mode =
                    sengoku.get().getMode(CurioUtils.findFirstCurio(player,
                            stack -> stack.getItem() instanceof sengokudrivers_epmty).get().stack());

            String riderType = switch (mode) {
                case BANANA      -> RiderTypes.BANANA;
                case ORANGELS -> RiderTypes.ORANGELS;   // 新增
                default          -> null;
            };

            if (riderType != null) {
                if (isWearingArmor(player, riderType)) return;
                PacketHandler.INSTANCE.sendToServer(
                        new TransformationRequestPacket(player.getUUID(), riderType, false));
            }
        }
    }

    /**
     * X 键专属：第一次手持 TwoWeaponItem + 佩戴 DEFAULT 形态 Two_sidriver → 装载并切换 X 形态
     * @return true 表示已处理，本次 X 键不再往下走
     */
    private static boolean tryLoadTwoWeaponOnX(LocalPlayer player) {
        Optional<SlotResult> opt = CuriosApi.getCuriosInventory(player)
                .resolve()
                .flatMap(inv -> inv.findFirstCurio(s -> s.getItem() instanceof Two_sidriver));
        if (opt.isEmpty()) return false;

        ItemStack belt = opt.get().stack();
        Two_sidriver.DriverType type = Two_sidriver.getDriverType(belt);

        if (type == Two_sidriver.DriverType.DEFAULT) {
            // ① 还需手持武器才发加载包
            boolean hasWeapon = player.getMainHandItem().getItem() instanceof TwoWeaponItem
                    || player.getOffhandItem().getItem() instanceof TwoWeaponItem;
            if (!hasWeapon) return false;   // 不给提示，静默忽略
            PacketHandler.INSTANCE.sendToServer(new XKeyLoadPacket(player.getUUID()));
            return true;
        }

        if (type == Two_sidriver.DriverType.BAT) {
            // ② BAT 形态直接发最终变身包
            PacketHandler.INSTANCE.sendToServer(new XKeyEvilPacket(player.getUUID()));
            return true;
        }

        return false;   // X 形态什么都不做
    }

    /* ========= 判断是否已穿对应骑士装甲 ========= */
    private static boolean isWearingArmor(LocalPlayer player, String riderType) {
        // 检查LEMON_ENERGY类型是否装备全套盔甲
        if (RiderTypes.LEMON_ENERGY.equals(riderType)) {
            // 检查是否装备Baron Lemon全套盔甲
            boolean isBaronLemon = player.getInventory().armor.get(3).is(ModItems.BARON_LEMON_HELMET.get()) &&
                                   player.getInventory().armor.get(2).is(ModItems.BARON_LEMON_CHESTPLATE.get()) &&
                                   player.getInventory().armor.get(1).is(ModItems.BARON_LEMON_LEGGINGS.get());
            
            // 检查是否装备Duke全套盔甲
            boolean isDuke = player.getInventory().armor.get(3).is(ModItems.DUKE_HELMET.get()) &&
                             player.getInventory().armor.get(2).is(ModItems.DUKE_CHESTPLATE.get()) &&
                             player.getInventory().armor.get(1).is(ModItems.DUKE_LEGGINGS.get());
            
            return isBaronLemon || isDuke;
        }
        
        return switch (riderType) {
            case RiderTypes.MELON_ENERGY ->
                    player.getInventory().armor.get(3).is(ModItems.ZANGETSU_SHIN_HELMET.get()) &&
                    player.getInventory().armor.get(2).is(ModItems.ZANGETSU_SHIN_CHESTPLATE.get()) &&
                    player.getInventory().armor.get(1).is(ModItems.ZANGETSU_SHIN_LEGGINGS.get());
            case RiderTypes.BANANA ->
                    player.getInventory().armor.get(3).is(ModItems.RIDER_BARONS_HELMET.get()) &&
                    player.getInventory().armor.get(2).is(ModItems.RIDER_BARONS_CHESTPLATE.get()) &&
                    player.getInventory().armor.get(1).is(ModItems.RIDER_BARONS_LEGGINGS.get());
            case RiderTypes.ORANGELS ->
                    player.getInventory().armor.get(3).is(ModItems.DARK_ORANGELS_HELMET.get()) &&
                            player.getInventory().armor.get(2).is(ModItems.DARK_ORANGELS_CHESTPLATE.get()) &&
                            player.getInventory().armor.get(1).is(ModItems.DARK_ORANGELS_LEGGINGS.get());
            case RiderTypes.PEACH_ENERGY ->
                    player.getInventory().armor.get(3).is(ModItems.MARIKA_HELMET.get()) &&
                    player.getInventory().armor.get(2).is(ModItems.MARIKA_CHESTPLATE.get()) &&
                    player.getInventory().armor.get(1).is(ModItems.MARIKA_LEGGINGS.get());
            case RiderTypes.CHERRY_ENERGY ->
                    player.getInventory().armor.get(3).is(ModItems.SIGURD_HELMET.get()) &&
                    player.getInventory().armor.get(2).is(ModItems.SIGURD_CHESTPLATE.get()) &&
                    player.getInventory().armor.get(1).is(ModItems.SIGURD_LEGGINGS.get());
            case RiderTypes.DRAGONFRUIT_ENERGY ->
                    player.getInventory().armor.get(3).is(ModItems.TYRANT_HELMET.get()) &&
                    player.getInventory().armor.get(2).is(ModItems.TYRANT_CHESTPLATE.get()) &&
                    player.getInventory().armor.get(1).is(ModItems.TYRANT_LEGGINGS.get());
            default -> false;
        };
    }
}
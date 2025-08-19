package com.xiaoshi2022.kamen_rider_boss_you_and_me.event.henshin;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.Genesis_driver;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.sengokudrivers_epmty;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.PacketHandler;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.henshin.TransformationRequestPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.henshin.RiderTypes;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.CurioUtils;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.KeyBinding;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Optional;

@Mod.EventBusSubscriber(modid = "kamen_rider_boss_you_and_me", value = Dist.CLIENT)
public final class KeyInputListener {

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (!KeyBinding.CHANGE_KEY.isDown()) return;

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

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
                default -> null;
            };

            if (riderType != null) {
                if (isWearingArmor(player, riderType)) return;   // 已变身
                PacketHandler.INSTANCE.sendToServer(
                        new TransformationRequestPacket(player.getUUID(), riderType, false));
            }
            return;
        }

        // 2. 战极腰带
        Optional<sengokudrivers_epmty> sengoku = CurioUtils.findFirstCurio(player,
                        stack -> stack.getItem() instanceof sengokudrivers_epmty)
                .map(slot -> (sengokudrivers_epmty) slot.stack().getItem());

        if (sengoku.isPresent() &&
                sengoku.get().getMode(CurioUtils.findFirstCurio(player,
                        stack -> stack.getItem() instanceof sengokudrivers_epmty).get().stack())
                        == sengokudrivers_epmty.BeltMode.BANANA) {

            if (isWearingArmor(player, RiderTypes.BANANA)) return;
            PacketHandler.INSTANCE.sendToServer(
                    new TransformationRequestPacket(player.getUUID(), RiderTypes.BANANA, false));
        }
    }

    /* ========= 判断是否已穿对应骑士装甲 ========= */
    private static boolean isWearingArmor(LocalPlayer player, String riderType) {
        return switch (riderType) {
            case RiderTypes.LEMON_ENERGY ->
                    player.getInventory().armor.get(3).is(ModItems.BARON_LEMON_HELMET.get());
            case RiderTypes.MELON_ENERGY ->
                    player.getInventory().armor.get(3).is(ModItems.ZANGETSU_SHIN_HELMET.get());
            case RiderTypes.BANANA ->
                    player.getInventory().armor.get(3).is(ModItems.RIDER_BARONS_HELMET.get());
            default -> false;
        };
    }
}
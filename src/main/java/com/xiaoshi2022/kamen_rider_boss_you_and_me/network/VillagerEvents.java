package com.xiaoshi2022.kamen_rider_boss_you_and_me.network;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.giifusteamp;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.GiifuDemosEntity;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.VillagerTransformationUtil;
import forge.net.mca.entity.VillagerEntityMCA;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class VillagerEvents {
    // Shift + 右键人类变成怪人
    @SubscribeEvent
    public static void onRightClick(PlayerInteractEvent.EntityInteract event) {
        Player player = event.getEntity();
        if (player.level().isClientSide || !(player.level() instanceof ServerLevel)) return;

        if (player.isShiftKeyDown()) { // 检查是否按下Shift键
            ItemStack itemInHand = player.getMainHandItem();
            if (itemInHand.getItem() instanceof giifusteamp) {
                if (event.getTarget() instanceof VillagerEntityMCA mcaVillager) {
                    VillagerTransformationUtil.transformToGiifuDemos((ServerLevel) player.level(), mcaVillager, itemInHand.getTag());
                }
            }
        }
    }

    // 攻击怪人变成人类
    @SubscribeEvent
    public static void onAttack(LivingHurtEvent event) {
        LivingEntity hurtEntity = event.getEntity();
        if (hurtEntity instanceof GiifuDemosEntity giifuDemosEntity) {
            if (event.getSource().getDirectEntity() instanceof Player player) {
                ItemStack itemInHand = player.getMainHandItem();
                if (itemInHand.getItem() instanceof giifusteamp) {
                    VillagerTransformationUtil.transformToVillager((ServerLevel) player.level(), giifuDemosEntity, itemInHand.getTag());
                }
            }
        }
    }
}
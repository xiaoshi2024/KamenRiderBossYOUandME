package com.xiaoshi2022.kamen_rider_boss_you_and_me.block.giifu;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.block.client.giifu.GiifuSleepingStateBlockEntity;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;


@Mod.EventBusSubscriber(modid = "kamen_rider_boss_you_and_me")
public class GiifuDamageHandler {

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        Level level = event.getLevel();
        if (level.isClientSide) return;

        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof GiifuSleepingStateBlock)) return;

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof GiifuSleepingStateBlockEntity blockEntity)) return;

        Player player = event.getEntity();
        float damage = (float) player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE).getValue();

        blockEntity.addDamage(damage); // 累加伤害
    }
}
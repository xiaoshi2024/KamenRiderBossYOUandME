package com.xiaoshi2022.kamen_rider_boss_you_and_me.network.ghosteye;


import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.ModEntityTypes;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.GhostEyeEntity;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.KRBVariables;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;
import tocraft.walkers.api.PlayerShape;

import java.util.function.Supplier;

/**
 * 眼魔变身为眼魂实体的数据包
 * 当玩家按下B键时发送到服务器，触发形态转换
 */
public class GhostEyeTransformPacket {

    public GhostEyeTransformPacket() {
        // 空构造函数，用于网络传输
    }

    public static void encode(GhostEyeTransformPacket packet, FriendlyByteBuf buffer) {
        // 不需要编码任何数据
    }

    public static GhostEyeTransformPacket decode(FriendlyByteBuf buffer) {
        return new GhostEyeTransformPacket();
    }

    public static void handle(GhostEyeTransformPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            // 获取发送数据包的玩家
            ServerPlayer player = context.getSender();
            if (player != null) {
                Level level = player.level();
                
                // 创建眼魂实体实例，但不添加到世界
                GhostEyeEntity ghostEyeEntity = ModEntityTypes.GHOST_EYE_ENTITY.get().create(level);
                if (ghostEyeEntity != null) {
                    // 设置眼魂实体的位置和旋转为玩家的位置和旋转
                    ghostEyeEntity.setPos(player.getX(), player.getY(), player.getZ());
                    ghostEyeEntity.setYRot(player.getYRot());
                    ghostEyeEntity.setXRot(player.getXRot());
                    
                    // 使用PlayerShape API进行变形
                    if (PlayerShape.updateShapes(player, ghostEyeEntity)) {
                        // 获取玩家变量
                        KRBVariables.PlayerVariables variables = player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new KRBVariables.PlayerVariables());
                        
                        // 设置飞行能力（参照KRB模组的NBT模式）
                        variables.currentFlightController = "GhostEye";
                        player.getAbilities().mayfly = true;
                        player.getAbilities().flying = true;
                        player.onUpdateAbilities();
                        
                        // 在玩家NBT中存储飞行状态
                        CompoundTag playerData = player.getPersistentData();
                        playerData.putBoolean("isGhostEyeForm", true);
                        playerData.putLong("ghostEyeFormStartTime", level.getGameTime()); // 记录形态开始时间
                        
                        // 添加眼魔形态相关效果 - 持续时间从9999刻(8分钟)延长到12000刻(10分钟)
                        // 添加isGhostEyePlayer方法检查的所有效果
                        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 12000, 1, false, false));
                        player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 12000, 0, false, false));
                        player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 12000, 0, false, false));
                        player.addEffect(new MobEffectInstance(MobEffects.JUMP, 12000, 1, false, false));
                        
                        // 同步变量
                        variables.syncPlayerVariables(player);
                        
                        // 给玩家发送提示消息
                        player.displayClientMessage(net.minecraft.network.chat.Component.literal("已变为眼魂实体！按Shift+B变回人形，按空格键飞行"), true);
                    } else {
                        // 如果变形失败
                        player.displayClientMessage(net.minecraft.network.chat.Component.literal("变形失败！"), true);
                    }
                }
            }
        });
        context.setPacketHandled(true);
    }
}
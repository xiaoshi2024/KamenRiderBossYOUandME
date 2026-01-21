package com.xiaoshi2022.kamen_rider_boss_you_and_me.network.Superpower;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class KnightPoisonPacket {
    private final int playerId;

    public KnightPoisonPacket(int playerId) {
        this.playerId = playerId;
    }

    public static void encode(KnightPoisonPacket packet, FriendlyByteBuf buffer) {
        buffer.writeInt(packet.playerId);
    }

    public static KnightPoisonPacket decode(FriendlyByteBuf buffer) {
        return new KnightPoisonPacket(buffer.readInt());
    }

    public static void handle(KnightPoisonPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            // 服务器处理骑士剧毒技能
            if (context.getDirection().getReceptionSide().isServer()) {
                handleServerKnightPoison(packet);
            }
        });
        context.setPacketHandled(true);
    }

    private static void handleServerKnightPoison(KnightPoisonPacket packet) {
        net.minecraft.server.level.ServerLevel world = net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer().getLevel(net.minecraft.world.level.Level.OVERWORLD);
        if (world != null) {
            net.minecraft.world.entity.Entity entity = world.getEntity(packet.playerId);
            if (entity instanceof net.minecraft.server.level.ServerPlayer player) {
                // 检查是否装备了Brain头盔或处于Brain形态
                boolean hasBrainHelmet = player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD).is(com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems.BRAIN_HELMET.get());
                boolean isBrainTransformed = false;
                
                try {
                    isBrainTransformed = player.getCapability(com.xiaoshi2022.kamen_rider_boss_you_and_me.network.varibales.KRBVariables.PLAYER_VARIABLES_CAPABILITY, null)
                            .orElse(new com.xiaoshi2022.kamen_rider_boss_you_and_me.network.varibales.KRBVariables.PlayerVariables())
                            .isBrainTransformed;
                } catch (Exception e) {
                    // 处理可能的异常
                }

                if (isBrainTransformed || hasBrainHelmet) {
                    // 检查骑士能量
                    com.xiaoshi2022.kamen_rider_boss_you_and_me.network.varibales.KRBVariables.PlayerVariables variables = player.getCapability(com.xiaoshi2022.kamen_rider_boss_you_and_me.network.varibales.KRBVariables.PLAYER_VARIABLES_CAPABILITY).orElse(new com.xiaoshi2022.kamen_rider_boss_you_and_me.network.varibales.KRBVariables.PlayerVariables());
                    double energyCost = 40.0D; // 骑士剧毒消耗40点骑士能量
                    
                    if (variables.riderEnergy >= energyCost) {
                        // 检查玩家是否手持纸
                        net.minecraft.world.item.ItemStack mainHand = player.getMainHandItem();
                        net.minecraft.world.item.ItemStack offHand = player.getOffhandItem();
                        
                        // 选择主手或副手的纸
                        net.minecraft.world.item.ItemStack paperStack = mainHand.getItem() == net.minecraft.world.item.Items.PAPER ? mainHand : 
                                              (offHand.getItem() == net.minecraft.world.item.Items.PAPER ? offHand : null);
                        
                        if (paperStack != null) {
                            // 获取原来纸的数量
                            int count = paperStack.getCount();
                            
                            // 将纸转化为相同数量的剧毒手帕
                            net.minecraft.world.item.ItemStack poisonHandkerchief = new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.PAPER, count);
                            net.minecraft.nbt.CompoundTag nbt = new net.minecraft.nbt.CompoundTag();
                            nbt.putBoolean("IsPoisonHandkerchief", true);
                            nbt.putString("CustomName", "剧毒手帕");
                            poisonHandkerchief.setTag(nbt);
                            
                            // 替换纸为剧毒手帕
                            if (paperStack == mainHand) {
                                player.setItemInHand(InteractionHand.MAIN_HAND, poisonHandkerchief);
                            } else {
                                player.setItemInHand(InteractionHand.OFF_HAND, poisonHandkerchief);
                            }
                            
                            // 消耗骑士能量
                            variables.riderEnergy -= energyCost;
                            variables.syncPlayerVariables(player);
                            
                            // 播放转化动画
                            net.minecraft.network.chat.Component animation = net.minecraft.network.chat.Component.literal("brain_knight_poison");
                            com.xiaoshi2022.kamen_rider_boss_you_and_me.network.PacketHandler.sendAnimationToAllTrackingAndSelf(animation, player.getId(), true, player);
                            
                            // 生成毒雾粒子效果
                            for (int i = 0; i < 20; i++) {
                                double x = player.getX() + (player.getRandom().nextDouble() - 0.5) * 2.0;
                                double y = player.getY() + player.getRandom().nextDouble() * player.getBbHeight();
                                double z = player.getZ() + (player.getRandom().nextDouble() - 0.5) * 2.0;
                                player.level().addParticle(
                                        net.minecraft.core.particles.ParticleTypes.SMOKE,
                                        x, y, z,
                                        0.0, 0.1, 0.0
                                );
                            }
                        }
                    }
                }
            }
        }
    }
}
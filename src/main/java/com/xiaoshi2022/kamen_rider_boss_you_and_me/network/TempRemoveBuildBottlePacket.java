package com.xiaoshi2022.kamen_rider_boss_you_and_me.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.BuildDriver;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModBossSounds;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.CurioUtils;
import top.theillusivec4.curios.api.SlotResult;

import java.util.Optional;
import java.util.function.Supplier;

public class TempRemoveBuildBottlePacket {
    
    public TempRemoveBuildBottlePacket() {
    }
    
    public static void encode(TempRemoveBuildBottlePacket msg, FriendlyByteBuf buffer) {
    }
    
    public static TempRemoveBuildBottlePacket decode(FriendlyByteBuf buffer) {
        return new TempRemoveBuildBottlePacket();
    }
    
    public static void handle(TempRemoveBuildBottlePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            
            // 找到玩家装备的Build Driver
            Optional<SlotResult> curioOptional = CurioUtils.findFirstCurio(player, stack -> stack.getItem() instanceof BuildDriver);
            
            if (curioOptional.isPresent()) {
                SlotResult slotResult = curioOptional.get();
                ItemStack beltStack = slotResult.stack();
                BuildDriver belt = (BuildDriver) beltStack.getItem();
                BuildDriver.BeltMode mode = belt.getMode(beltStack);
                
                // 检查是否是BlackBuild或BlackBuildKr形态
                boolean isBlackBuildArmor = player.getInventory().armor.get(3).getItem() == ModItems.BLACK_BUILD_HELMET.get();
                boolean isBlackBuildKrArmor = player.getInventory().armor.get(3).getItem() == ModItems.BLACK_BUILD_KR_HELMET.get();
                
                if ((isBlackBuildArmor && (mode == BuildDriver.BeltMode.HAZARD_RT || mode == BuildDriver.BeltMode.HAZARD_RT_MOULD)) || (isBlackBuildKrArmor && mode == BuildDriver.BeltMode.HAZARD_KR)) {
                    // 根据当前形态创建对应的满瓶物品
                    if (isBlackBuildArmor) {
                        // 创建兔子和坦克满瓶物品
                        ItemStack rabbitBottle = new ItemStack(ModItems.RABBIT_ITEM.get());
                        ItemStack tankBottle = new ItemStack(ModItems.TANK_ITEM.get());
                        
                        // 返还物品给玩家
                        if (!player.getInventory().add(rabbitBottle)) player.spawnAtLocation(rabbitBottle);
                        if (!player.getInventory().add(tankBottle)) player.spawnAtLocation(tankBottle);
                    } else if (isBlackBuildKrArmor) {
                        // 创建海贼和列车满瓶物品
                        ItemStack kaizokuBottle = new ItemStack(ModItems.KAIZOKU_ITEM.get());
                        ItemStack ressyaBottle = new ItemStack(ModItems.RESSYA_ITEM.get());
                        
                        // 返还物品给玩家
                        if (!player.getInventory().add(kaizokuBottle)) player.spawnAtLocation(kaizokuBottle);
                        if (!player.getInventory().add(ressyaBottle)) player.spawnAtLocation(ressyaBottle);
                    }
                    
                    // 切换腰带模式到HAZARD_EMPTY
                    belt.setMode(beltStack, BuildDriver.BeltMode.HAZARD_EMPTY);
                    
                    // 更新Curios槽位
                    CurioUtils.updateCurioSlot(player, slotResult.slotContext().identifier(),
                            slotResult.slotContext().index(), beltStack);
                    
                    // 播放解除音效
                    player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                            ModBossSounds.LOCKOFF.get(),
                            net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
                    
                    // 发送客户端提示消息
                    player.sendSystemMessage(net.minecraft.network.chat.Component.literal("满瓶已临时取下！"));
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}

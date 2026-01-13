package com.xiaoshi2022.kamen_rider_boss_you_and_me.util;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.KnightInvokerBuckle;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.NoxSpecialEntity;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.ModEntityTypes;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.Drivershenshin.BeltAnimationPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.PacketHandler;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.SoundStopPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = "kamen_rider_boss_you_and_me", bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class KnightInvokerSequence {

    // 玩家 UUID -> 倒计时（tick 数）
    private static final Map<UUID, Integer> HENSHIN_COOLDOWN = new ConcurrentHashMap<>();

    /**
     * 开始变身序列，包括延迟装备盔甲
     */
    public static void startHenshin(ServerPlayer player) {
        // 检查玩家是否装备了KnightInvokerBuckle且处于NOX模式
        Optional<ItemStack> beltStackOpt = CurioUtils.findFirstCurio(player,
                stack -> stack.getItem() instanceof KnightInvokerBuckle)
                .map(SlotResult::stack);
        
        if (beltStackOpt.isPresent()) {
            ItemStack beltStack = beltStackOpt.get();
            KnightInvokerBuckle belt = (KnightInvokerBuckle) beltStack.getItem();
            
            // 只有NOX模式下才触发变身
            if (belt.getMode(beltStack) == KnightInvokerBuckle.BeltMode.NOX) {
                // 停止nox_b音效
                ResourceLocation noxBoundLoc = new ResourceLocation(
                        "kamen_rider_boss_you_and_me",
                        "nox_b"
                );
                // 只发送给当前玩家
                PacketHandler.sendToClient(
                        new SoundStopPacket(player.getId(), noxBoundLoc),
                        player
                );
                PacketHandler.sendToServer(new SoundStopPacket(player.getId(), noxBoundLoc));

                // 启动变身动画
                belt.startHenshinAnimation(player, beltStack);

                // 给玩家添加临时隐身效果，持续4秒（80刻），符合NOX剧中设定
                player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                        net.minecraft.world.effect.MobEffects.INVISIBILITY,
                        4 * 20, // 4秒
                        0, false, false, true));
                
                // 播放玩家动画 - spintwo
                playPlayerAnimation(player, "spintwo");
                
                // 生成NOX变身特效实体
                NoxSpecialEntity noxSpecial = ModEntityTypes.NOX_SPECIAL.get().create(player.level());
                if (noxSpecial != null) {
                    noxSpecial.setPos(player.getX(), player.getY(), player.getZ());
                    noxSpecial.setTargetPlayer(player);
                    player.level().addFreshEntity(noxSpecial);
                }
                
                // 播放nox_c音效 - 变身完成
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                        com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModBossSounds.NOX_C.get(),
                        net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
                
                // 设置4秒后装备盔甲
                HENSHIN_COOLDOWN.put(player.getUUID(), 4 * 20);
            }
        }
    }

    /**
     * 每tick检查倒计时，时间到了装备盔甲
     */
    @SubscribeEvent
    public static void onTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide()) return;
        
        ServerPlayer player = (ServerPlayer) event.player;
        UUID uuid = player.getUUID();
        
        if (!HENSHIN_COOLDOWN.containsKey(uuid)) return;
        
        int left = HENSHIN_COOLDOWN.get(uuid) - 1;
        if (left <= 0) {
            HENSHIN_COOLDOWN.remove(uuid);
            if (!player.isAlive()) return;
            
            // 检查玩家是否装备了KnightInvokerBuckle且处于NOX模式
            Optional<ItemStack> beltStackOpt = CurioUtils.findFirstCurio(player,
                    stack -> stack.getItem() instanceof KnightInvokerBuckle)
                    .map(SlotResult::stack);
            
            if (beltStackOpt.isPresent()) {
                ItemStack beltStack = beltStackOpt.get();
                KnightInvokerBuckle belt = (KnightInvokerBuckle) beltStack.getItem();
                
                // 只有NOX模式下才装备盔甲
                if (belt.getMode(beltStack) == KnightInvokerBuckle.BeltMode.NOX) {
                    // 装备头盔
                    ItemStack helmet = new ItemStack(ModItems.NOX_KNIGHT_HELMET.get());
                    player.setItemSlot(EquipmentSlot.HEAD, helmet);
                    
                    // 装备胸甲
                    ItemStack chestplate = new ItemStack(ModItems.NOX_KNIGHT_CHESTPLATE.get());
                    player.setItemSlot(EquipmentSlot.CHEST, chestplate);
                    
                    // 装备护腿
                    ItemStack leggings = new ItemStack(ModItems.NOX_KNIGHT_LEGGINGS.get());
                    player.setItemSlot(EquipmentSlot.LEGS, leggings);
                    

                }
            }
        } else {
            HENSHIN_COOLDOWN.put(uuid, left);
        }
    }
    
    /* ========== 玩家动画支持 ========== */
    /* 发送动画到客户端 */
    public static void playPlayerAnimation(ServerPlayer player, String animationName) {
        if (player.level().isClientSide()) return;

        PacketHandler.sendAnimationToAll(
                Component.literal(animationName),
                player.getId(),
                false
        );
    }
}
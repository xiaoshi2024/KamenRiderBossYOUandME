//package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.npc;
//
//import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.Genesis_driver;
//import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems;
//import forge.net.mca.entity.VillagerEntityMCA;
//import forge.net.mca.entity.ai.relationship.Gender;
//import net.minecraft.network.chat.Component;
//import net.minecraft.server.level.ServerPlayer;
//import net.minecraft.sounds.SoundEvents;
//import net.minecraft.sounds.SoundSource;
//import net.minecraft.world.InteractionHand;
//import net.minecraft.world.InteractionResult;
//import net.minecraft.world.entity.EntityType;
//import net.minecraft.world.entity.player.Player;
//import net.minecraft.world.item.ItemStack;
//import net.minecraft.world.level.Level;
//import net.minecraftforge.items.IItemHandlerModifiable;
//import top.theillusivec4.curios.api.CuriosApi;
//
//public class KaitoVillager extends VillagerEntityMCA {
//
//    public KaitoVillager(EntityType<? extends VillagerEntityMCA> type, Level level) {
//        super((EntityType<VillagerEntityMCA>) type, level, Gender.MALE);
//    }
//
//    /**
//     * 处理玩家与村民的交互：
//     * 1. 若玩家手持 Genesis_driver，则当作礼物送出并穿到村民身上。
//     * 2. 其余交互交由 MCA 默认逻辑处理。
//     */
//    @Override
//    public InteractionResult mobInteract(Player player, InteractionHand hand) {
//        ItemStack held = player.getItemInHand(hand);
//
//        // 只在服务端执行
//        if (!level().isClientSide && player instanceof ServerPlayer serverPlayer) {
//            if (held.getItem() instanceof Genesis_driver) {
//                // 1. 让 MCA 先处理礼物逻辑（消耗物品、更新好感度）
//                InteractionResult mcaResult = super.mobInteract(player, hand);
//
//                // 2. 如果 MCA 成功收礼，再把腰带真正塞进村民的 Curios 腰带槽
//                if (mcaResult.consumesAction()) {
//                    CuriosApi.getCuriosInventory(this).ifPresent(inv ->
//                            inv.getStacksHandler("belt").ifPresent(handler -> {
//                                IItemHandlerModifiable beltInv = handler.getStacks();
//                                if (beltInv.getSlots() > 0 && beltInv.getStackInSlot(0).isEmpty()) {
//                                    // 用 split(1) 保证只拿 1 个
//                                    beltInv.setStackInSlot(0, held.split(1));
//                                    player.displayClientMessage(
//                                            Component.literal("驱纹戒斗：这份力量，我收下了。"), false);
//                                }
//                            })
//                    );
//                }
//                return mcaResult;
//            }
//        }
//
//        // 其余情况走默认流程
//        return super.mobInteract(player, hand);
//    }
//
//    @Override
//    public void onAddedToWorld() {
//        super.onAddedToWorld();
//        if (!level().isClientSide) {
//            CuriosApi.getCuriosInventory(this).ifPresent(inv -> {
//                inv.getStacksHandler("belt").ifPresent(handler -> {
//                    IItemHandlerModifiable beltInv = handler.getStacks();
//                    if (beltInv.getSlots() > 0 && beltInv.getStackInSlot(0).isEmpty()) {
//                        // ✅ 自动穿上腰带
//                        beltInv.setStackInSlot(0, new ItemStack(ModItems.GENESIS_DRIVER.get()));
//                        System.out.println("Kaito 已自动装备 Genesis_driver");
//                    }
//                });
//            });
//        }
//    }
//
//    /**
//     * 仅作演示：触发额外事件（可删除或按需调用）
//     */
//    private void triggerKamenEvent(Player player) {
//        player.level().playSound(
//                null,
//                player.blockPosition(),
//                SoundEvents.EVOKER_CAST_SPELL,
//                SoundSource.PLAYERS,
//                1F,
//                1F);
//        player.displayClientMessage(
//                Component.literal("驱纹戒斗：你也想参加这场游戏吗？"),
//                false);
//    }
//}
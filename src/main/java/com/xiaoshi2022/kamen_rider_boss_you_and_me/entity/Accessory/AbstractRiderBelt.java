package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.CurioUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoItem;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.Optional;

/**
 * 骑士腰带抽象基类，实现通用的右键装备功能
 */
public abstract class AbstractRiderBelt extends Item implements GeoItem, ICurioItem {

    // 腰带最大耐久值
    public static final int MAX_DURABILITY = 100;

    public AbstractRiderBelt(Properties properties) {
        super(properties.durability(MAX_DURABILITY));
    }

    /**
     * 减少腰带耐久度
     */
    public boolean damageBelt(ItemStack stack, int amount, Player player) {
        if (stack == null || player == null) {
            return false;
        }
        if (player.isCreative()) {
            // 创造模式下不减少耐久
            return false;
        }

        int newDamage = stack.getDamageValue() + amount;
        stack.setDamageValue(newDamage);

        // 检查腰带是否损坏
        if (newDamage >= stack.getMaxDamage()) {
            // 腰带损坏，返回true表示需要移除
            return true;
        }
        return false;
    }

    /**
     * 右键使用腰带直接装备到腰带槽位
     */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (level == null || player == null || hand == null) {
            return InteractionResultHolder.pass(ItemStack.EMPTY);
        }
        
        ItemStack originalStack = player.getItemInHand(hand);
        if (originalStack == null || originalStack.isEmpty()) {
            return InteractionResultHolder.pass(ItemStack.EMPTY);
        }

        if (level.isClientSide) {
            // 在客户端不执行实际装备逻辑
            return InteractionResultHolder.success(originalStack);
        }

        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.pass(originalStack);
        }

        ItemStack beltStack = originalStack.copy();

        // 检查玩家是否已经装备了腰带
        Optional<SlotResult> existingBeltOpt = CurioUtils.findFirstCurio(serverPlayer, 
                stack -> stack != null && stack.getItem() instanceof AbstractRiderBelt);

        // 如果已有腰带，将其替换回背包
        if (existingBeltOpt.isPresent()) {
            SlotResult slotResult = existingBeltOpt.get();
            ItemStack existingBelt = slotResult.stack();
            
            // 尝试将原有腰带添加到玩家背包
            if (existingBelt != null && !existingBelt.isEmpty()) {
                if (!serverPlayer.getInventory().add(existingBelt)) {
                    // 如果背包满了，将腰带掉落在地上
                    serverPlayer.drop(existingBelt, false);
                }
            }
            
            // 清空原有腰带槽位
            CuriosApi.getCuriosInventory(serverPlayer).ifPresent(inv -> {
                inv.getStacksHandler(slotResult.slotContext().identifier()).ifPresent(handler -> {
                    handler.getStacks().setStackInSlot(slotResult.slotContext().index(), ItemStack.EMPTY);
                    handler.update();
                });
            });
        }

        // 装备新腰带
        CurioUtils.forceEquipBelt(serverPlayer, beltStack);
        
        // 从玩家手中移除腰带
        if (!player.isCreative()) {
            originalStack.shrink(1);
        }
        
        // 触发腰带装备后的自定义逻辑
        onBeltEquipped(serverPlayer, beltStack);
        
        return InteractionResultHolder.success(originalStack);
    }

    /**
     * 腰带装备后的自定义逻辑，子类可以重写
     */
    protected abstract void onBeltEquipped(ServerPlayer player, ItemStack beltStack);
}
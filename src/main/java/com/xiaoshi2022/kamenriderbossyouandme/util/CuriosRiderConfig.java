package com.xiaoshi2022.kamenriderbossyouandme.util;

import com.jpigeon.ridebattlelib.core.system.henshin.RiderConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

import java.util.Optional;

public class CuriosRiderConfig extends RiderConfig {

    // 在子类中维护辅助槽位信息
    private EquipmentSlot customAuxSlot = EquipmentSlot.OFFHAND;

    public CuriosRiderConfig(ResourceLocation riderId) {
        super(riderId);
    }

    @Override
    public boolean isEquippedByPlayer(Player player) {
        // 1. 先检查原版装备槽（使用父类配置的槽位）
        Item driverItem = this.getDriverItem();
        EquipmentSlot slot = this.getDriverSlot();
        if (slot != null) {
            ItemStack driverStack = player.getItemBySlot(slot);
            if (!driverStack.isEmpty() && driverStack.is(driverItem)) {
                return true;
            }
        }

        // 2. 检查Curios的belt槽
        Optional<ICuriosItemHandler> curiosInventory = CuriosApi.getCuriosInventory(player);
        return curiosInventory.map(handler ->
                handler.findFirstCurio(driverItem).isPresent()
        ).orElse(false);
    }

    @Override
    public boolean isAuxDriverEquippedByPlayer(Player player) {
        Item auxItem = this.getAuxDriverItem();
        if (auxItem == null || auxItem == net.minecraft.world.item.Items.AIR) {
            return false;
        }

        // 使用我们自己的槽位记录（通过setAuxDriverItem设置）
        ItemStack auxStack = player.getItemBySlot(this.customAuxSlot);
        if (!auxStack.isEmpty() && auxStack.is(auxItem)) {
            return true;
        }

        // 检查Curios槽
        Optional<ICuriosItemHandler> curiosInventory = CuriosApi.getCuriosInventory(player);
        return curiosInventory.map(handler ->
                handler.findFirstCurio(auxItem).isPresent()
        ).orElse(false);
    }

    // ==================== 重写setter方法维护自己的槽位 ====================

    @Override
    public CuriosRiderConfig setMainDriverItem(Item item, EquipmentSlot slot) {
        super.setMainDriverItem(item, slot);
        return this;
    }

    @Override
    public CuriosRiderConfig setAuxDriverItem(Item item, EquipmentSlot slot) {
        super.setAuxDriverItem(item, slot);
        this.customAuxSlot = slot;  // 记录辅助槽位
        return this;
    }

    @Override
    public CuriosRiderConfig setMainDriverItem(Item item) {
        super.setMainDriverItem(item);
        return this;
    }

    @Override
    public CuriosRiderConfig setAuxDriverItem(Item item) {
        super.setAuxDriverItem(item);
        this.customAuxSlot = EquipmentSlot.OFFHAND;  // 使用默认值
        return this;
    }

    @Override
    public CuriosRiderConfig setTriggerItem(Item item) {
        super.setTriggerItem(item);
        return this;
    }

    @Override
    public CuriosRiderConfig setAllowDynamicForms(boolean allow) {
        super.setAllowDynamicForms(allow);
        return this;
    }

    // ==================== 新增辅助方法 ====================

    /**
     * 获取当前设置的辅助槽位
     */
    public EquipmentSlot getAuxSlot() {
        return this.customAuxSlot;
    }

    /**
     * 检查物品是否在Curios的指定槽位中
     */
    public boolean isItemInCuriosSlot(Player player, Item item, String slotType) {
        Optional<ICuriosItemHandler> curiosInventory = CuriosApi.getCuriosInventory(player);
        if (curiosInventory.isEmpty()) {
            return false;
        }

        var handler = curiosInventory.get();
        var stacksHandler = handler.getStacksHandler(slotType);

        if (stacksHandler.isPresent()) {
            var stacks = stacksHandler.get().getStacks();
            for (int i = 0; i < stacks.getSlots(); i++) {
                ItemStack stackInSlot = stacks.getStackInSlot(i);
                if (!stackInSlot.isEmpty() && stackInSlot.is(item)) {
                    return true;
                }
            }
        }

        return false;
    }
}
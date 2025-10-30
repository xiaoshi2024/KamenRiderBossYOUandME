package com.xiaoshi2022.kamen_rider_boss_you_and_me.curio;

import static com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me.MODID;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.Genesis_driver;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.extensions.IForgeMenuType;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

// 导入腰带类

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class EntityCuriosListMenu extends AbstractContainerMenu {

    private final LivingEntity target;
    private final List<ICurioStacksHandler> handlers = new ArrayList<>();
    private final int totalCurioSlots;

    // 定义MenuType，但不在这里注册
    // 注册应该在Mod的初始化阶段使用DeferredRegister进行
    public static final MenuType<EntityCuriosListMenu> TYPE = IForgeMenuType.create(EntityCuriosListMenu::new);

    // 网络构造函数
    public EntityCuriosListMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
        this(containerId, playerInventory, getTargetFromBuffer(playerInventory.player, buf));
    }

    // 主构造函数
    public EntityCuriosListMenu(int containerId, Inventory playerInventory, LivingEntity target) {
        super(TYPE, containerId);
        this.target = target;
        this.totalCurioSlots = initMenu(playerInventory);
    }

    private static LivingEntity getTargetFromBuffer(Player player, FriendlyByteBuf buf) {
        int entityId = buf.readInt();
        Entity entity = player.level().getEntity(entityId);
        return entity instanceof LivingEntity ? (LivingEntity) entity : null;
    }

    private int initMenu(Inventory playerInventory) {
        int slotCount = 0;

        if (target == null) {
            return slotCount;
        }

        // 获取Curio库存
        Optional<top.theillusivec4.curios.api.type.capability.ICuriosItemHandler> inventoryOpt = CuriosApi.getCuriosInventory(target).resolve();
        if (inventoryOpt.isPresent()) {
            Map<String, ICurioStacksHandler> curios = inventoryOpt.get().getCurios();

            // 为每个handler添加槽位
            for (Map.Entry<String, ICurioStacksHandler> entry : curios.entrySet()) {
                ICurioStacksHandler handler = entry.getValue();
                handlers.add(handler);
                slotCount += handler.getSlots();
                addCurioSlots(handler, entry.getKey());
            }
        }

        // 添加玩家物品栏
        addPlayerInventory(playerInventory);

        return slotCount;
    }

    private void addCurioSlots(ICurioStacksHandler handler, String identifier) {
        // 参考音速弓的输入槽位位置 (30, 28)
        int slotX = 30;
        int slotY = 28;

        for (int i = 0; i < handler.getSlots(); i++) {
            final int slotIndex = i;
            SlotContext ctx = new SlotContext(identifier, target, slotIndex, false, true);

            // 使用固定的槽位位置，与音速弓保持一致

            this.addSlot(new CurioSlot(handler.getStacks(), slotIndex, slotX, slotY, ctx) {
                @Override
                public boolean mayPickup(Player player) {
                    // 允许玩家取出物品
                    return true;
                }

                @Override
                public boolean mayPlace(ItemStack stack) {
                    // 允许放入任何物品，包括腰带
                    return true;
                }

                @Override
                public void setChanged() {
                    super.setChanged();
                    // 通知客户端更新
                    if (target instanceof ServerPlayer serverPlayer) {
                        serverPlayer.inventoryMenu.broadcastChanges();
                    }
                }
            });
        }
    }

    private void addPlayerInventory(Inventory playerInventory) {
        // 参考RiderFusionMachine的布局，调整玩家物品栏位置
        int startX = 8;
        int startY = 84; // 更合理的位置，给上方留出更多空间

        // 主物品栏 (3x9)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9,
                        startX + col * 18, startY + row * 18));
            }
        }

        // 快捷栏 (1x9)
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col,
                    startX + col * 18, startY + 58));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        // 检查目标实体是否仍然有效且在范围内
        return target != null &&
                target.isAlive() &&
                player.distanceToSqr(target) < 64.0D; // 8格范围内
    }

    /**
     * 获取目标实体
     */
    public LivingEntity getTarget() {
        return target;
    }
    
    /**
     * 静态方法，用于打开实体Curio菜单
     */
    public static void openMenu(net.minecraft.server.level.ServerPlayer player, LivingEntity targetEntity) {
        if (targetEntity != null && targetEntity.isAlive()) {
            // 检查Curios是否已加载
            if (BeltCurioIntegration.isCuriosLoaded()) {
                // 创建菜单提供者并打开界面
                EntityCurioInteractionHandler.EntityCuriosMenuProvider provider = 
                        new EntityCurioInteractionHandler.EntityCuriosMenuProvider(targetEntity, 0);
                net.minecraftforge.network.NetworkHooks.openScreen(player, provider, buf -> {
                    buf.writeInt(targetEntity.getId());
                    buf.writeInt(0);
                });
            }
        }
    }

    /**
     * 获取Curio处理器列表
     */
    public List<ICurioStacksHandler> getHandlers() {
        return handlers;
    }

    /**
     * 获取总Curio槽位数
     */
    public int getTotalCurioSlots() {
        return totalCurioSlots;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            itemstack = slotStack.copy();

            int playerInventoryStart = totalCurioSlots;
            int playerInventoryEnd = playerInventoryStart + 36; // 27主物品栏 + 9快捷栏

            // 从Curio槽位移动到玩家物品栏
            if (index < totalCurioSlots) {
                if (!this.moveItemStackTo(slotStack, playerInventoryStart, playerInventoryEnd, true)) {
                    return ItemStack.EMPTY;
                }
            }
            // 从玩家物品栏移动到Curio槽位
            else {
                if (!this.moveItemStackTo(slotStack, 0, totalCurioSlots, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (slotStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (slotStack.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, slotStack);
        }

        return itemstack;
    }

    /**
     * 检查特定槽位是否包含指定类型的物品
     */
    public boolean hasItemInSlot(String slotType, int slotIndex, ItemStack stack) {
        for (Slot slot : this.slots) {
            if (slot instanceof CurioSlot curioSlot) {
                SlotContext context = curioSlot.getContext();
                if (context.identifier().equals(slotType) && context.index() == slotIndex) {
                    return ItemStack.matches(slot.getItem(), stack);
                }
            }
        }
        return false;
    }

    /**
     * 获取特定槽位的物品
     */
    @Nullable
    public ItemStack getItemInSlot(String slotType, int slotIndex) {
        for (Slot slot : this.slots) {
            if (slot instanceof CurioSlot curioSlot) {
                SlotContext context = curioSlot.getContext();
                if (context.identifier().equals(slotType) && context.index() == slotIndex) {
                    return slot.getItem();
                }
            }
        }
        return null;
    }



    @Override
    public void removed(Player player) {
        super.removed(player);
        // 菜单关闭时的清理工作
        if (target instanceof ServerPlayer serverPlayer) {
            serverPlayer.inventoryMenu.broadcastChanges();
        }
    }

    /**
     * Curio槽位类
     */
    public static class CurioSlot extends Slot {
        private final SlotContext context;
        private final IDynamicStackHandler handler;

        public CurioSlot(IDynamicStackHandler handler, int index, int x, int y, SlotContext context) {
            // 不再尝试将IDynamicStackHandler转换为Container
            // 创建一个适配的Container实现
            super(new Container() {
                @Override
                public int getContainerSize() {
                    return handler.getSlots();
                }

                @Override
                public boolean isEmpty() {
                    for (int i = 0; i < handler.getSlots(); i++) {
                        if (!handler.getStackInSlot(i).isEmpty()) {
                            return false;
                        }
                    }
                    return true;
                }

                @Override
                public ItemStack getItem(int i) {
                    return handler.getStackInSlot(i);
                }

                @Override
                public ItemStack removeItem(int i, int j) {
                    return handler.extractItem(i, j, false);
                }

                @Override
                public ItemStack removeItemNoUpdate(int i) {
                    ItemStack stack = handler.getStackInSlot(i);
                    handler.setStackInSlot(i, ItemStack.EMPTY);
                    return stack;
                }

                @Override
                public void setItem(int i, ItemStack itemStack) {
                    handler.setStackInSlot(i, itemStack);
                }

                @Override
                public void setChanged() {
                    // 适配接口，无需实现
                }

                @Override
                public boolean stillValid(Player player) {
                    return true;
                }

                @Override
                public void clearContent() {
                    for (int i = 0; i < handler.getSlots(); i++) {
                        handler.setStackInSlot(i, ItemStack.EMPTY);
                    }
                }
            }, index, x, y);
            this.handler = handler;
            this.context = context;
        }

        public SlotContext getContext() {
            return context;
        }
    }
}

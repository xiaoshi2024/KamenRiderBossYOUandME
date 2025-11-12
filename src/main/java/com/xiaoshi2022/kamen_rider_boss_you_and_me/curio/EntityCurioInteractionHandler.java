package com.xiaoshi2022.kamen_rider_boss_you_and_me.curio;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.Genesis_driver;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me.MODID;

@Mod.EventBusSubscriber(modid = MODID)
public class EntityCurioInteractionHandler {

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        Player player = event.getEntity();
        Entity target = event.getTarget();

        // 仅处理LivingEntity
        if (!(target instanceof LivingEntity livingTarget)) {
            return;
        }
        
        // 检查玩家是否手持腰带
        ItemStack heldItem = player.getItemInHand(event.getHand());
        
        // 情况1：玩家手持腰带
        if (heldItem.getItem() instanceof Genesis_driver) {
            // 检查是否可以为该实体打开Curio界面
            if (canOpenCurioInventory(livingTarget) && player instanceof ServerPlayer serverPlayer) {
                // 尝试打开Curio界面
                tryOpenCurioInventory(serverPlayer, livingTarget);
                event.setCanceled(true);
            }
        }
        // 情况2：玩家空手且按住shift键
        else if (heldItem.isEmpty() && player.isShiftKeyDown() && player instanceof ServerPlayer serverPlayer) {
            // 检查是否可以为该实体打开Curio界面
            if (canOpenCurioInventory(livingTarget)) {
                // 使用与情况1相同的方法尝试打开Curio界面
                tryOpenCurioInventory(serverPlayer, livingTarget);
                event.setCanceled(true);
            }
        }
    }

    /**
     * 检查是否可以为实体打开Curio界面
     */
    private static boolean canOpenCurioInventory(LivingEntity entity) {
        if (entity == null) {
            return false;
        }
        
        // 排除玩家实体，因为玩家有自己的Curio界面系统
        if (entity instanceof Player) {
            return false;
        }
        
        // 只允许村民和僵尸实体打开Curio界面
        // 检查实体类型是否为村民或僵尸
        String entityTypeName = entity.getType().getDescriptionId();
        
        // 标准村民检查
        boolean isVillager = entityTypeName.contains("villager") || 
                            entity.getClass().getSimpleName().contains("Villager");
        
        // 僵尸检查
        boolean isZombie = entityTypeName.contains("zombie") || 
                          entity.getClass().getSimpleName().contains("Zombie");
        
        // 允许MCA村民：检查类名中是否包含"MCA"和村民相关词汇
        boolean isMCAVillager = entity.getClass().getName().contains("mca") && 
                               (entityTypeName.contains("villager") || 
                                entity.getClass().getSimpleName().contains("Villager"));

        // 允许本模组的npc：检查类名中是否包含"kamen_rider_boss_you_and_me"和elite_monster_npc相关词汇
        boolean isKRBelite = entity.getClass().getName().contains("kamen_rider_boss_you_and_me") &&
                               (entityTypeName.contains("elite_monster_npc") ||
                                entity.getClass().getSimpleName().contains("elite_monster_npc"));
        
        return isVillager || isZombie || isMCAVillager || isKRBelite;
    }

    /**
     * 尝试打开实体的Curio界面
     */
    public static void tryOpenCurioInventory(ServerPlayer player, LivingEntity target) {
        // 检查Curios是否已加载
        if (!BeltCurioIntegration.isCuriosLoaded()) {
            return;
        }

        // 不再检查是否有Curio槽位，直接尝试打开界面
        // 这样即使实体没有Curio槽位，界面也会打开并显示可用的槽位
        try {
            // 使用NetworkHooks打开界面
            net.minecraftforge.network.NetworkHooks.openScreen(player, new EntityCuriosMenuProvider(target, 0), buf -> {
                buf.writeInt(target.getId());
                buf.writeInt(0);
            });
        } catch (Exception e) {
            // 如果打开界面失败，尝试直接使用EntityCuriosListMenu.openMenu方法
            EntityCuriosListMenu.openMenu(player, target);
        }
    }

    /**
     * 实体Curio菜单提供者
     */
    public static class EntityCuriosMenuProvider implements MenuProvider {
        private final LivingEntity entity;
        private final int page;

        public EntityCuriosMenuProvider(LivingEntity entity, int page) {
            this.entity = entity;
            this.page = page;
        }

        @Override
        public net.minecraft.network.chat.Component getDisplayName() {
            return entity.getDisplayName();
        }

        @Override
        public net.minecraft.world.inventory.AbstractContainerMenu createMenu(int containerId, net.minecraft.world.entity.player.Inventory playerInventory, Player player) {
            // 返回自定义的Curio菜单
            return new EntityCuriosListMenu(containerId, playerInventory, entity);
        }
    }
}
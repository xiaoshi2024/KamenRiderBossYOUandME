package com.xiaoshi2022.kamen_rider_boss_you_and_me.event;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.List;
import java.util.function.Supplier;

import static com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems.SENGOKUDRIVERS_EPMTY;

@Mod.EventBusSubscriber(modid = "kamen_rider_boss_you_and_me")
public class HelheimVineHandler {

    // 硬编码的锁种列表作为备选方案
    private static final List<Supplier<Item>> LOCKSEEDS = List.of(
            com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems.BANANAFRUIT::get,
            com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModItems.CHERYY::get,
            com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModItems.MELON::get,
            ModItems.LEMON_ENERGY::get
            // 可以继续添加更多锁种...
    );

    // 检查是否是锁种(NBT方式)
    private static boolean isLockseed(ItemStack stack) {
        return stack.hasTag() && stack.getTag().contains("is_lockseed");
    }

    // 获取随机锁种(优先NBT检测，失败后使用硬编码列表)
    public static ItemStack getRandomLockseed(RandomSource random) {
        // 先尝试NBT检测
        List<Item> dynamicLockseeds = ForgeRegistries.ITEMS.getValues().stream()
                .filter(item -> isLockseed(new ItemStack(item)))
                .toList();

        // 如果NBT检测为空，回退到硬编码列表
        List<Item> finalList = dynamicLockseeds.isEmpty() ?
                LOCKSEEDS.stream().map(Supplier::get).toList() :
                dynamicLockseeds;

        if (finalList.isEmpty()) return ItemStack.EMPTY;
        return new ItemStack(finalList.get(random.nextInt(finalList.size())));
    }

    @SubscribeEvent
    public static void onHelheimVineBreak(BlockEvent.BreakEvent event) {
        // 1. 检查方块是否是赫尔海姆藤蔓
        if (!isHelheimVine(event.getState())) {
            return;
        }

        Player player = event.getPlayer();
        Level level = (Level) event.getLevel();
        BlockPos pos = event.getPos();

        // 2. 检查玩家是否佩戴战极驱动器
        if (!isWearingSengokuDriver(player)) {
            return;
        }

        // 3. 随机掉落锁种
        ItemStack lockseed = getRandomLockseed(level.random);
        if (!lockseed.isEmpty()) {
            level.addFreshEntity(new ItemEntity(
                    level,
                    pos.getX() + 0.5,
                    pos.getY() + 0.5,
                    pos.getZ() + 0.5,
                    lockseed
            ));
        }
    }

    // 检查是否是赫尔海姆藤蔓
    private static boolean isHelheimVine(BlockState state) {
        ResourceLocation blockId = ForgeRegistries.BLOCKS.getKey(state.getBlock());
        return blockId != null
                && (blockId.getNamespace().equals("kamen_rider_weapon_craft")
                || blockId.getNamespace().equals("kamen_rider_boss_you_and_me"))
                && blockId.getPath().contains("helheimvine");
    }

    // 检查是否佩戴战极驱动器
    private static boolean isWearingSengokuDriver(Player player) {
        return CuriosApi.getCuriosInventory(player)
                .map(curios -> !curios.findCurios(stack ->
                        stack.getItem() == SENGOKUDRIVERS_EPMTY.get()).isEmpty())
                .orElse(false);
    }
}
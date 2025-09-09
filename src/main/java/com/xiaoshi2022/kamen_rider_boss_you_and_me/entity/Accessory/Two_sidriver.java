package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.two_sidriver.Two_sidriverRenderer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.*;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.fml.common.Mod;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Consumer;

@Mod.EventBusSubscriber(modid = "kamen_rider_boss_you_and_me")
public class Two_sidriver extends Item implements GeoItem, ICurioItem {

    /* =============== 形态 & 武器 =============== */
    public enum DriverType {
        DEFAULT, BAT, X
    }
    public enum WeaponMode {
        IDLE, BLADE, GUN
    }

    /* =============== 动画常量 =============== */
    private static final RawAnimation IDLE   = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation BLADE  = RawAnimation.begin().thenLoop("blade");
    private static final RawAnimation GUN    = RawAnimation.begin().thenLoop("gun");

    private static final String KEY_TYPE    = "DriverType";
    private static final String KEY_WEAPON  = "WeaponMode";

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public Two_sidriver(Properties p) { super(p); }

    /* ================= GeoItem ================= */
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 5, this::predicate));
    }

    private <E extends GeoItem> PlayState predicate(AnimationState<E> state) {
        ItemStack stack = state.getData(DataTickets.ITEMSTACK);
        WeaponMode mode = getWeaponMode(stack);
        return switch (mode) {
            case BLADE -> state.setAndContinue(BLADE);
            case GUN   -> state.setAndContinue(GUN);
            default    -> state.setAndContinue(IDLE);
        };
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return cache; }

    /* =============== 客户端渲染器 =============== */
    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private final Two_sidriverRenderer renderer = new Two_sidriverRenderer();
            @Override public BlockEntityWithoutLevelRenderer getCustomRenderer() { return renderer; }
        });
    }

    /* =============== Curios =============== */
    @Override
    public void onEquip(SlotContext ctx, ItemStack prev, ItemStack stack) {
        if (ctx.entity() instanceof ServerPlayer sp) syncToTracking(sp, stack);
    }

    @Override
    public void curioTick(SlotContext ctx, ItemStack stack) {
        if (!(ctx.entity() instanceof ServerPlayer sp)) return;
        if (sp.tickCount % 20 == 0) syncToSelf(sp, stack);
    }

    /* =============== 工具 =============== */
    public static DriverType getDriverType(ItemStack stack) {
        return DriverType.values()[stack.getOrCreateTag().getInt(KEY_TYPE) % 3];
    }
    public static void setDriverType(ItemStack stack, DriverType type) {
        stack.getOrCreateTag().putInt(KEY_TYPE, type.ordinal());
    }
    public static WeaponMode getWeaponMode(ItemStack stack) {
        return WeaponMode.values()[stack.getOrCreateTag().getInt(KEY_WEAPON) % 3];
    }
    public static void setWeaponMode(ItemStack stack, WeaponMode mode) {
        stack.getOrCreateTag().putInt(KEY_WEAPON, mode.ordinal());
    }

    /* =============== 业务入口 =============== */
    /** 形态切换：印章/合体调用 */
    public static void switchDriver(ServerPlayer player, DriverType type) {
        withBelt(player, stack -> {
            setDriverType(stack, type);
            syncToTracking(player, stack);
        });
    }

    /** 武器切换：按键/技能调用 */
    public static void switchWeapon(ServerPlayer player, WeaponMode mode) {
        withBelt(player, stack -> {
            setWeaponMode(stack, mode);
            syncToTracking(player, stack);
        });
    }

    /* =============== 私有辅助 =============== */
    private static void withBelt(ServerPlayer player, java.util.function.Consumer<ItemStack> op) {
        Optional<ItemStack> opt = CuriosApi.getCuriosInventory(player)
                .resolve()
                .flatMap(inv -> inv.findFirstCurio(s -> s.getItem() instanceof Two_sidriver))
                .map(sr -> sr.stack());
        opt.ifPresent(op);
    }

    public static void syncToTracking(ServerPlayer player, ItemStack stack) {
        PacketHandler.sendToAllTracking(new DriverSyncPacket(player.getId(), getDriverType(stack), getWeaponMode(stack)), player);
    }

    private static void syncToSelf(ServerPlayer player, ItemStack stack) {
        PacketHandler.sendToClient(new DriverSyncPacket(player.getId(), getDriverType(stack), getWeaponMode(stack)), player);
    }

    /* =============== 同步 NBT（可选） =============== */
    @Override
    public CompoundTag getShareTag(ItemStack stack) {
        CompoundTag tag = super.getShareTag(stack);
        if (tag == null) tag = new CompoundTag();
        tag.putInt(KEY_TYPE, getDriverType(stack).ordinal());
        tag.putInt(KEY_WEAPON, getWeaponMode(stack).ordinal());
        return tag;
    }

    @Override
    public void readShareTag(ItemStack stack, @Nullable CompoundTag nbt) {
        super.readShareTag(stack, nbt);
        if (nbt != null) {
            if (nbt.contains(KEY_TYPE)) setDriverType(stack, DriverType.values()[nbt.getInt(KEY_TYPE)]);
            if (nbt.contains(KEY_WEAPON)) setWeaponMode(stack, WeaponMode.values()[nbt.getInt(KEY_WEAPON)]);
        }
    }
}
package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.two_sidriver.Two_sidriverRenderer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.Drivershenshin.DriverSyncPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.PacketHandler;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.varibales.KRBVariables;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.fml.common.Mod;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Consumer;

@Mod.EventBusSubscriber(modid = "kamen_rider_boss_you_and_me")
public class Two_sidriver extends AbstractRiderBelt implements GeoItem, ICurioItem {

    public void triggerAnim(LivingEntity entity, String controller, String animationName) {
        // 在这里添加触发动画的逻辑
        // 例如，使用 GeckoLib 的 AnimationController 来触发动画
    }

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
    
    /**
     * 腰带装备后的自定义逻辑
     */
    @Override
    protected void onBeltEquipped(ServerPlayer player, ItemStack beltStack) {
        // 装备Two_sidriver腰带后的自定义逻辑
        // 可以在这里添加变身音效、粒子效果等
    }

    /* ================= GeoItem ================= */
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 5, this::predicate));
    }

    private <E extends GeoItem> PlayState predicate(AnimationState<E> state) {
        ItemStack stack = state.getData(DataTickets.ITEMSTACK);
        if (stack == null) {
            return state.setAndContinue(IDLE);
        }
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
        super.onEquip(ctx, prev, stack); // 调用父类方法
        if (ctx.entity() instanceof ServerPlayer sp) syncToTracking(sp, stack);
    }

    @Override
    public void curioTick(SlotContext ctx, ItemStack stack) {
        if (!(ctx.entity() instanceof ServerPlayer sp)) return;
        if (sp.tickCount % 20 == 0) syncToSelf(sp, stack);
        
        // 检测玩家是否为变身状态和腰带是否存在
        checkPlayerTransformation(sp);
    }
    
    // 新增：检测玩家是否为变身状态和腰带是否存在
    private void checkPlayerTransformation(ServerPlayer player) {
        // 获取玩家变量
        KRBVariables.PlayerVariables variables = player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new KRBVariables.PlayerVariables());

        // 如果玩家装备了EvilBatsArmor且腰带存在状态变化，自动处理
        if (variables.isEvilBatsTransformed) {
            // 检查玩家是否还装备着Two_sidriver腰带
            boolean hasTwoSidriver = false;
            for (ItemStack stack : player.getInventory().items) {
                if (stack.getItem() instanceof Two_sidriver) {
                    hasTwoSidriver = true;
                    break;
                }
            }

            // 如果玩家装备了EvilBatsArmor但没有腰带，自动解除变身
            if (!hasTwoSidriver) {
                // 解除EvilBats变身
                com.xiaoshi2022.kamen_rider_boss_you_and_me.event.TransformationHandler.completeBeltRelease(player, "EVIL_BATS");
            }
        }

        // 同步玩家状态
        variables.syncPlayerVariables(player);
    }
    /* =============== 工具 =============== */
    public static DriverType getDriverType(ItemStack stack) {
        if (stack == null) {
            return DriverType.DEFAULT;
        }
        return DriverType.values()[stack.getOrCreateTag().getInt(KEY_TYPE) % 3];
    }
    public static void setDriverType(ItemStack stack, DriverType type) {
        if (stack == null) {
            return;
        }
        stack.getOrCreateTag().putInt(KEY_TYPE, type.ordinal());
    }
    public static WeaponMode getWeaponMode(ItemStack stack) {
        if (stack == null) {
            return WeaponMode.IDLE;
        }
        return WeaponMode.values()[stack.getOrCreateTag().getInt(KEY_WEAPON) % 3];
    }
    public static void setWeaponMode(ItemStack stack, WeaponMode mode) {
        if (stack == null) {
            return;
        }
        stack.getOrCreateTag().putInt(KEY_WEAPON, mode.ordinal());
    }

    /* =============== 业务入口 =============== */
    /** 形态切换：印章/合体调用 */
    public static void switchDriver(ServerPlayer player, DriverType type) {
        withBelt(player, stack -> {
            setDriverType(stack, type);
            syncToTracking(player, stack);
            syncToSelf(player, stack);
        });
    }
    /** 武器切换：按键/技能调用 */
    public static void switchWeapon(ServerPlayer player, WeaponMode mode) {
        withBelt(player, stack -> {
            setWeaponMode(stack, mode);
            syncToTracking(player, stack);
            syncToSelf(player, stack);
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

    public static void syncToSelf(ServerPlayer player, ItemStack stack) {
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
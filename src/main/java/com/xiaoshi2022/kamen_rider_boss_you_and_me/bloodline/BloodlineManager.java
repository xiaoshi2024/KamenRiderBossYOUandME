package com.xiaoshi2022.kamen_rider_boss_you_and_me.bloodline;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.PacketHandler;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.SyncBloodlinePacket;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Mod.EventBusSubscriber(modid = "kamen_rider_boss_you_and_me", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BloodlineManager {
    public static final Capability<Bloodline> CAP = CapabilityManager.get(new CapabilityToken<>(){});
    private static final ResourceLocation ID = new ResourceLocation("kamen_rider_boss_you_and_me", "bloodline");

    public static Bloodline get(Player player) {
        return player.getCapability(CAP).orElseThrow(NullPointerException::new);
    }

    public static void sync(ServerPlayer player) {
        if (player != null && !player.hasDisconnected()) {
            PacketHandler.sendToClient(
                    new SyncBloodlinePacket(get(player)),
                    player
            );
        }
    }

    /* ===== Capability 注册 ===== */
    @SubscribeEvent
    public static void onAttach(AttachCapabilitiesEvent<Entity> e) {
        if (e.getObject() instanceof Player player) {
            e.addCapability(new ResourceLocation("kamen_rider_boss_you_and_me", "bloodline"),
                    new Provider());
        }
    }

    /* ===== 玩家跨维度/重生保留数据 ===== */
    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        // 确保在服务端
        if (event.getEntity().level().isClientSide()) return;

        // 源玩家（已死）
        Player original = event.getOriginal();
        // 新玩家（重生）
        Player clone = event.getEntity();

        // 从源玩家读取数据（不需要revive）
        CompoundTag nbt = new CompoundTag();
        // 安全检查：确保原始玩家仍有Bloodline能力
        original.getCapability(CAP).ifPresent(oldBloodline -> {
            oldBloodline.save(nbt);                    // 读取旧数据
            get(clone).load(nbt);                       // 写进新玩家
        });
    }

    private static class Provider implements ICapabilitySerializable<CompoundTag> {
        private final Bloodline instance = new Bloodline();
        private final LazyOptional<Bloodline> opt = LazyOptional.of(() -> instance);

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag tag = new CompoundTag();
            instance.save(tag);
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            instance.load(nbt);
        }

        @Nonnull
        @Override
        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
            return CAP.orEmpty(cap, opt);
        }
    }
}
package com.xiaoshi2022.kamen_rider_boss_you_and_me.block.kivas.entity;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.block.kivas.ThroneBlock;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.ModEntityTypes;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class SeatEntity extends Entity {

    private BlockPos thronePos;

    public SeatEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setInvulnerable(true);
        this.setNoGravity(true);
    }

    public SeatEntity(Level level, BlockPos pos, double height) {
        this(ModEntityTypes.SEAT.get(), level);
        this.thronePos = pos;
        // 🎯 使用传入的高度
        this.setPos(pos.getX() + 0.5, pos.getY() + height, pos.getZ() + 0.5);
    }

    // 🎯 修改方法签名，添加高度参数
    public static boolean create(Level level, BlockPos pos, double height, Player player, Direction direction) {
        if (level.isClientSide()) {
            return false;
        }

        // 🎯 参考MrCrayfish的简洁实现
        List<SeatEntity> seats = level.getEntitiesOfClass(SeatEntity.class, new AABB(pos));
        if (!seats.isEmpty()) {
            return false;
        }

        // 🎯 创建座椅实体（使用传入的高度）
        SeatEntity seat = new SeatEntity(level, pos, height);
        if (!level.addFreshEntity(seat)) {
            return false;
        }

        // 🎯 直接让玩家乘坐（MrCrayfish的方式）
        return player.startRiding(seat, false);
    }


    @Override
    public void tick() {
        super.tick();

        // 🎯 简单的存在检查
        if (this.isRemoved()) {
            return;
        }

        // 检查王座是否还存在
        if (thronePos != null && level().getBlockState(thronePos).isAir()) {
            this.discard();
            return;
        }

        // 🎯 如果没有乘客，移除座椅
        if (!this.isVehicle()) {
            this.discard();
            return;
        }

        // 🎯 检测下船键
        if (!this.level().isClientSide() && this.isVehicle()) {
            Entity passenger = this.getFirstPassenger();
            if (passenger instanceof Player player && player.isShiftKeyDown()) {
                this.ejectPassengers();
                this.discard();
                return;
            }
        }
    }

    @Override
    protected void defineSynchedData() {}

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {}

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {}

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return this.getPassengers().isEmpty();
    }

    @Override
    public boolean shouldRiderSit() {
        return true;
    }
}
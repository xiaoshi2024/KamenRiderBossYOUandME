//package com.xiaoshi2022.kamen_rider_boss_you_and_me.mixin;
//
//import com.xiaoshi2022.kamen_rider_boss_you_and_me.mixinTool.PlayerExpand;
//import net.minecraft.nbt.CompoundTag;
//import net.minecraft.world.entity.player.Player;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.Unique;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.Inject;
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//
//@Mixin(Player.class)
//public abstract class PlayerMixin implements PlayerExpand {
//
//    // 存储玩家是否正在进行基夫变异的状态
//    @Unique
//    private boolean isUndergoingGiifuMutation = false;
//
//    // 存储变异动画的开始时间
//    @Unique
//    private long giifuMutationStartTime = 0L;
//
//    // 变异动画的持续时间（毫秒）
//    @Unique
//    private static final long GIIFU_MUTATION_DURATION = 2000; // 2秒
//
//    @Override
//    public boolean isUndergoingGiifuMutation() {
//        // 如果已经过了持续时间，自动结束变异状态
//        if (isUndergoingGiifuMutation && System.currentTimeMillis() - giifuMutationStartTime > GIIFU_MUTATION_DURATION) {
//            setUndergoingGiifuMutation(false);
//        }
//        return isUndergoingGiifuMutation;
//    }
//
//    @Override
//    public void setUndergoingGiifuMutation(boolean undergoing) {
//        this.isUndergoingGiifuMutation = undergoing;
//        if (undergoing) {
//            this.giifuMutationStartTime = System.currentTimeMillis();
//        }
//    }
//
//    @Override
//    public void triggerGiifuMutationAnimation() {
//        // 设置为正在变异状态
//        setUndergoingGiifuMutation(true);
//
//        // 在实际应用中，这里可以触发更多的动画效果或逻辑
//        // 例如播放音效、粒子效果等
//    }
//
//    @Override
//    public void stopGiifuMutationAnimation() {
//        // 停止变异动画
//        setUndergoingGiifuMutation(false);
//    }
//
//    // 保存数据到NBT，确保状态在世界保存和加载时不会丢失
//    @Inject(method = "addAdditionalSaveData", at = @At("HEAD"))
//    private void saveGiifuMutationState(CompoundTag tag, CallbackInfo ci) {
//        tag.putBoolean("IsUndergoingGiifuMutation", isUndergoingGiifuMutation);
//        if (isUndergoingGiifuMutation) {
//            tag.putLong("GiifuMutationStartTime", giifuMutationStartTime);
//        }
//    }
//
//    // 从NBT加载数据，恢复状态
//    @Inject(method = "readAdditionalSaveData", at = @At("HEAD"))
//    private void loadGiifuMutationState(CompoundTag tag, CallbackInfo ci) {
//        if (tag.contains("IsUndergoingGiifuMutation")) {
//            isUndergoingGiifuMutation = tag.getBoolean("IsUndergoingGiifuMutation");
//            if (isUndergoingGiifuMutation && tag.contains("GiifuMutationStartTime")) {
//                giifuMutationStartTime = tag.getLong("GiifuMutationStartTime");
//            }
//        }
//    }
//}
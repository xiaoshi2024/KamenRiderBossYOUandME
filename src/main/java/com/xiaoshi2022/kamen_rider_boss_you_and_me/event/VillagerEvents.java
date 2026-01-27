package com.xiaoshi2022.kamen_rider_boss_you_and_me.event;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.StoriousMonsterBook;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.giifusteamp;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.property.aiziowc;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.client.ParticleTicker;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.ModEntityTypes;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.GiifuDemosEntity;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.StoriousEntity;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.anotherRiders.Another_Den_o;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.anotherRiders.Another_Zi_o;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.Roidmude.BrainRoidmudeEntity;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.giifu.Gifftarian;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModBossSounds;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.MCAUtil;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.VillagerTransformationUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class VillagerEvents {

    // 放在 VillagerEvents 内
    @SubscribeEvent
    public static void onServerTick(net.minecraftforge.event.TickEvent.ServerTickEvent event) {
        if (event.phase == net.minecraftforge.event.TickEvent.Phase.END) {
            ParticleTicker.tick((ServerLevel) event.getServer().getLevel(net.minecraft.world.level.Level.OVERWORLD));
            // 如果你有多维，遍历所有维度即可
        }
    }

    /* ---------- Shift+右键变异 ---------- */
    @SubscribeEvent
    public static void onRightClick(PlayerInteractEvent.EntityInteract event) {
        Player player = event.getEntity();
        if (player.level().isClientSide || !(player.level() instanceof ServerLevel level)) return;

        if (player.isShiftKeyDown()) {
            ItemStack stack = player.getMainHandItem();
            Entity target = event.getTarget();
            
            // 检查是否是村民（原版或MCA）
            if (!(target instanceof net.minecraft.world.entity.npc.Villager || MCAUtil.isVillagerEntityMCA(target))) return;

            if (stack.getItem() instanceof aiziowc) {
                Vec3 center = target.position()
                        .add(0, target.getBbHeight() * 0.55, 0);   // 腰部
                ParticleTicker.add(level, center);
            }

            // 异类时王表盘处理
            if (stack.getItem() instanceof aiziowc) {
                aiziowc item = (aiziowc) stack.getItem();
                aiziowc.Mode mode = item.getCurrentMode(stack);
                
                // 异类时王处理（默认模式或异类模式）
                if (mode == aiziowc.Mode.DEFAULT || mode == aiziowc.Mode.ANOTHER) {
                    int used = aiziowc.getUseCount(stack);

                    // 检查是否已经使用了2次（第3次使用）
                    if (used >= 2) {
                        // 第3次使用，表盘消失
                        stack.shrink(1);
                        player.displayClientMessage(Component.literal("§d表盘已耗尽，无法再产生异类骑士……"), true);
                        return;
                    }

                    // 消耗1个物品！
                    ItemStack copy = stack.copy();
                    stack.shrink(1);
                    
                    // 增加使用计数到复制的物品，然后掉落
                    aiziowc.incrUseCount(copy);
                    player.level().addFreshEntity(new net.minecraft.world.entity.item.ItemEntity(player.level(), player.getX(), player.getY(), player.getZ(), copy));
                    
                    // 生成异类时王、保存村民数据等...
                    int remain = aiziowc.MAX_DAMAGE - used - 1;
                    CompoundTag villagerTag = new CompoundTag();
                    
                    // 使用反射调用save方法
                    try {
                        java.lang.reflect.Method saveMethod = target.getClass().getMethod("save", CompoundTag.class);
                        saveMethod.invoke(target, villagerTag);
                    } catch (Exception e) {
                        // 如果反射失败，使用默认的save方法
                        ((LivingEntity) target).save(villagerTag);
                    }

                    Another_Zi_o another = ModEntityTypes.ANOTHER_ZI_O.get().create(level);
                    another.moveTo(target.getX(), target.getY(), target.getZ(), target.getYRot(), 0);
                    another.getPersistentData().put("StoredVillager", villagerTag);
                    another.getPersistentData().putInt("DropDamage", remain);
                    level.addFreshEntity(another);
                    target.discard();

                    return;
                }
                // 异类电王处理（电王模式）
                else if (mode == aiziowc.Mode.DEN_O) {
                    int used = aiziowc.getUseCount(stack);

                    // 检查是否已经使用了2次（第3次使用）
                    if (used >= 2) {
                        // 第3次使用，表盘消失
                        stack.shrink(1);
                        player.displayClientMessage(Component.literal("§d表盘已耗尽，无法再产生异类骑士……"), true);
                        return;
                    }

                    // 增加使用计数（先增加再使用）
                    aiziowc.incrUseCount(stack);

                    // 消耗1个物品！
                    stack.shrink(1);

                    // 生成异类电王、保存村民数据等...
                    int remain = aiziowc.MAX_DAMAGE - used - 1;
                    CompoundTag villagerTag = new CompoundTag();
                    
                    // 使用反射调用save方法
                    try {
                        java.lang.reflect.Method saveMethod = target.getClass().getMethod("save", CompoundTag.class);
                        saveMethod.invoke(target, villagerTag);
                    } catch (Exception e) {
                        // 如果反射失败，使用默认的save方法
                        ((LivingEntity) target).save(villagerTag);
                    }

                    Another_Den_o anotherDenO = ModEntityTypes.ANOTHER_DEN_O.get().create(level);
                    anotherDenO.moveTo(target.getX(), target.getY(), target.getZ(), target.getYRot(), 0);
                    anotherDenO.getPersistentData().put("StoredVillager", villagerTag);
                    anotherDenO.getPersistentData().putInt("DropDamage", remain);
                    level.addFreshEntity(anotherDenO);
                    target.discard();

                    return;
                }
                // 异类decade处理（DCD模式）
                else if (mode == aiziowc.Mode.DCD) {
                    // DCD模式只能对时间王族使用，这里检查目标是否为时间王族
                    // 注意：这里的逻辑是在VillagerEvents中处理，而不是在Item的interactLivingEntity方法中
                    // 因为之前的修改被移除了，所以在这里重新实现
                    player.displayClientMessage(Component.literal("§c只有时间王族可以使用DCD模式的异类表盘！"), true);
                    return;
                }
            }

            /* 原有逻辑 */
            if (stack.getItem() instanceof giifusteamp) {
                try {
                    // 使用反射调用transformToGiifuDemos方法
                    java.lang.reflect.Method transformMethod = VillagerTransformationUtil.class.getMethod("transformToGiifuDemos", ServerLevel.class, Object.class, CompoundTag.class);
                    transformMethod.invoke(null, level, target, stack.getTag());
                } catch (Exception e) {
                    // 如果反射失败，使用默认的方式
                    GiifuDemosEntity giifu = new GiifuDemosEntity(ModEntityTypes.GIIFUDEMOS_ENTITY.get(), level);
                    giifu.setPos(target.getX(), target.getY(), target.getZ());
                    level.addFreshEntity(giifu);
                    target.discard();
                }
            } else if (stack.getItem() instanceof StoriousMonsterBook) {
                try {
                    // 使用反射调用transformToStorious方法
                    java.lang.reflect.Method transformMethod = VillagerTransformationUtil.class.getMethod("transformToStorious", ServerLevel.class, Object.class, CompoundTag.class);
                    transformMethod.invoke(null, level, target, stack.getTag());
                } catch (Exception e) {
                    // 如果反射失败，使用默认的方式
                    StoriousEntity storious = new StoriousEntity(ModEntityTypes.STORIOUS.get(), level);
                    storious.setPos(target.getX(), target.getY(), target.getZ());
                    level.addFreshEntity(storious);
                    target.discard();
                }
            }
        }
    }

    /* ---------- 怪人击杀/攻击事件 ---------- */
    @SubscribeEvent
    public static void onAttack(LivingHurtEvent event) {
        LivingEntity hurtEntity = event.getEntity();
        if (!(event.getSource().getDirectEntity() instanceof Player player)) return;
        ServerLevel level = (ServerLevel) player.level();

        if (hurtEntity instanceof GiifuDemosEntity || hurtEntity instanceof StoriousEntity) {
            // 添加调试信息
            // System.out.println("VillagerEvents: Attack event triggered for " + hurtEntity.getClass().getName());
            
            ItemStack itemInHand = player.getMainHandItem();
            // System.out.println("VillagerEvents: Player holding " + itemInHand.getItem().getClass().getName());
            
            if (itemInHand.getItem() instanceof giifusteamp) {
                // System.out.println("VillagerEvents: Player holding giifusteamp");
                if (hurtEntity instanceof GiifuDemosEntity giifuDemosEntity) {
                    // System.out.println("VillagerEvents: Transforming GiifuDemosEntity to villager");
                    VillagerTransformationUtil.transformToVillager((ServerLevel) player.level(), giifuDemosEntity, itemInHand.getTag());
                }
            } else if (itemInHand.getItem() instanceof StoriousMonsterBook) {
                // System.out.println("VillagerEvents: Player holding StoriousMonsterBook");
                if (hurtEntity instanceof StoriousEntity storiousEntity) {
                    // System.out.println("VillagerEvents: Transforming StoriousEntity to villager");
                    VillagerTransformationUtil.transformToBookVillager((ServerLevel) player.level(), storiousEntity, itemInHand.getTag());
                }
            }
        }
        // 新增逻辑：当玩家使用基夫印章攻击村民时，模拟被恶魔吞噬
        if (MCAUtil.isVillagerEntityMCA(hurtEntity)) {
            if (event.getSource().getDirectEntity() instanceof Player attackingPlayer) {
                ItemStack itemInHand = player.getMainHandItem();
                if (itemInHand.getItem() instanceof giifusteamp giifusteamp) {
                    // 播放音效
                    if (player.level() instanceof ServerLevel serverLevel) {
                        serverLevel.playSound(null, hurtEntity.getX(), hurtEntity.getY(), hurtEntity.getZ(),
                                ModBossSounds.SEAL.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
                    }
                    // 播放吞噬动画并移除村民
                    hurtEntity.remove(Entity.RemovalReason.KILLED);

                    // 生成一个基夫门徒实体
                    Gifftarian giifuEntity = new Gifftarian(ModEntityTypes.GIFFTARIAN.get(), hurtEntity.level());
                    giifuEntity.setPos(hurtEntity.getX(), hurtEntity.getY(), hurtEntity.getZ());
                    hurtEntity.level().addFreshEntity(giifuEntity);

                    // 触发动画（通过标志位或其他方式）
                    shouldPlayRoutAnimation = true;
                }
            }
        }
    }
    // 提取条件逻辑
    public static boolean shouldTransformToGifftarian() {
        // 这里需要一个全局状态来判断是否触发动画
        return shouldPlayRoutAnimation;
    }

    public static boolean shouldPlayRoutAnimation = false; // 全局布尔字段
    
    /* ---------- 村民危险时变回Brain Roidmude ---------- */
    @SubscribeEvent
    public static void onVillagerHurt(LivingHurtEvent event) {
        LivingEntity hurtEntity = event.getEntity();
        // 检查是否是村民（原版或MCA）
        if (!(hurtEntity instanceof net.minecraft.world.entity.npc.Villager || MCAUtil.isVillagerEntityMCA(hurtEntity))) return;
        
        try {
            net.minecraft.nbt.CompoundTag persistentData;
            
            // 根据实体类型获取persistentData
            if (MCAUtil.isVillagerEntityMCA(hurtEntity)) {
                // 使用反射获取MCA村民的persistentData
                java.lang.reflect.Method getPersistentDataMethod = hurtEntity.getClass().getMethod("getPersistentData");
                persistentData = (net.minecraft.nbt.CompoundTag) getPersistentDataMethod.invoke(hurtEntity);
            } else {
                // 直接获取原版村民的persistentData
                persistentData = hurtEntity.getPersistentData();
            }
            
            // 检查村民是否是Brain Roidmude拟态的
            if (persistentData.contains("BrainRoidmudeData")) {
                // 村民受到伤害，变回Brain Roidmude
                transformToBrainRoidmude((ServerLevel) hurtEntity.level(), hurtEntity, persistentData);
            }
        } catch (Exception e) {
            // System.out.println("Failed to check Brain Roidmude data: " + e.getMessage());
        }
    }
    
    // 将村民转换回Brain Roidmude实体
    private static void transformToBrainRoidmude(ServerLevel level, LivingEntity villager, CompoundTag persistentData) {
        try {
            // 获取存储的Brain Roidmude数据
            CompoundTag brainData = persistentData.getCompound("BrainRoidmudeData");
            
            // 创建Brain Roidmude实体
            BrainRoidmudeEntity brainRoidmude = com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.ModEntityTypes.BRAIN_ROIDMUDE.get().create(level);
            
            // 加载Brain Roidmude数据
            brainRoidmude.load(brainData);
            brainRoidmude.moveTo(villager.getX(), villager.getY(), villager.getZ(), villager.getYRot(), villager.getXRot());
            
            // 设置危险冷却时间，防止立即又拟态回去
            try {
                // 使用反射设置dangerCooldown
                java.lang.reflect.Field dangerCooldownField = BrainRoidmudeEntity.class.getDeclaredField("dangerCooldown");
                dangerCooldownField.setAccessible(true);
                dangerCooldownField.set(brainRoidmude, 100); // 设置100刻（5秒）的冷却时间
            } catch (Exception e) {
                // System.out.println("Failed to set danger cooldown: " + e.getMessage());
            }
            
            // 播放转换粒子效果
            for (int i = 0; i < 30; i++) {
                level.addParticle(
                        net.minecraft.core.particles.ParticleTypes.SMOKE, 
                        villager.getX() + (villager.getRandom().nextDouble() - 0.5) * villager.getBbWidth(),
                        villager.getY() + villager.getRandom().nextDouble() * villager.getBbHeight(),
                        villager.getZ() + (villager.getRandom().nextDouble() - 0.5) * villager.getBbWidth(),
                        0.0D, 0.1D, 0.0D
                );
            }
            
            // 添加Brain Roidmude到世界
            level.addFreshEntity(brainRoidmude);
            
            // 移除村民
            villager.discard();
            
            // System.out.println("Villager transformed back to Brain Roidmude due to danger");
        } catch (Exception e) {
            // System.out.println("Failed to transform villager back to Brain Roidmude: " + e.getMessage());
            // e.printStackTrace();
        }
    }
}
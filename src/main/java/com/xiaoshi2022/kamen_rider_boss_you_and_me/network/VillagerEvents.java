package com.xiaoshi2022.kamen_rider_boss_you_and_me.network;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.StoriousMonsterBook;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.giifusteamp;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.property.aiziowc;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.client.ParticleTicker;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.ModEntityTypes;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.anotherRiders.Another_Zi_o;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.anotherRiders.Another_Den_o;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.GiifuDemosEntity;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.StoriousEntity;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.giifu.Gifftarian;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModBossSounds;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.VillagerTransformationUtil;
import forge.net.mca.entity.VillagerEntityMCA;
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
            if (!(event.getTarget() instanceof VillagerEntityMCA villager)) return;

            if (stack.getItem() instanceof aiziowc) {
                Vec3 center = villager.position()
                        .add(0, villager.getBbHeight() * 0.55, 0);   // 腰部
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

                    // 增加使用计数（先增加再使用）
                    aiziowc.incrUseCount(stack);

                    // 消耗1个物品！
                    stack.shrink(1);
                    
                    // 生成异类时王、保存村民数据等...
                    int remain = aiziowc.MAX_DAMAGE - used - 1;
                    CompoundTag villagerTag = new CompoundTag();
                    villager.save(villagerTag);

                    Another_Zi_o another = ModEntityTypes.ANOTHER_ZI_O.get().create(level);
                    another.moveTo(villager.getX(), villager.getY(), villager.getZ(), villager.getYRot(), 0);
                    another.getPersistentData().put("StoredVillager", villagerTag);
                    another.getPersistentData().putInt("DropDamage", remain);
                    level.addFreshEntity(another);
                    villager.discard();

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
                    villager.save(villagerTag);

                    Another_Den_o anotherDenO = ModEntityTypes.ANOTHER_DEN_O.get().create(level);
                    anotherDenO.moveTo(villager.getX(), villager.getY(), villager.getZ(), villager.getYRot(), 0);
                    anotherDenO.getPersistentData().put("StoredVillager", villagerTag);
                    anotherDenO.getPersistentData().putInt("DropDamage", remain);
                    level.addFreshEntity(anotherDenO);
                    villager.discard();

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
                VillagerTransformationUtil.transformToGiifuDemos(level, villager, stack.getTag());
            } else if (stack.getItem() instanceof StoriousMonsterBook) {
                VillagerTransformationUtil.transformToStorious(level, villager, stack.getTag());
            }
        }
    }

    /* ---------- 怪人击杀/攻击事件 ---------- */
    @SubscribeEvent
    public static void onAttack(LivingHurtEvent event) {
        LivingEntity hurtEntity = event.getEntity();
        if (!(event.getSource().getDirectEntity() instanceof Player player)) return;
        ServerLevel level = (ServerLevel) player.level();

        if (hurtEntity instanceof GiifuDemosEntity giifuDemosEntity || hurtEntity instanceof StoriousEntity storiousEntity) {
            if (event.getSource().getDirectEntity() instanceof Player attackingPlayer) {
                ItemStack itemInHand = player.getMainHandItem();
                if (itemInHand.getItem() instanceof giifusteamp giifuSteamItem) {
                    // 使用 GiifuSteamItem 转换 GiifuDemosEntity 为村民
                    if (hurtEntity instanceof GiifuDemosEntity giifuDemosEntity) {
                        VillagerTransformationUtil.transformToVillager((ServerLevel) player.level(), giifuDemosEntity, itemInHand.getTag());
                    }
                } else if (itemInHand.getItem() instanceof StoriousMonsterBook storiousMonsterBook) {
                    // 使用 StoriousMonsterBook 转换 StoriousEntity 为村民
                    if (hurtEntity instanceof StoriousEntity storiousEntity) {
                        VillagerTransformationUtil.transformToBookVillager((ServerLevel) player.level(), storiousEntity, itemInHand.getTag());
                    }
                }
            }
        }
        // 新增逻辑：当玩家使用基夫印章攻击村民时，模拟被恶魔吞噬
        if (hurtEntity instanceof VillagerEntityMCA villager) {
            if (event.getSource().getDirectEntity() instanceof Player attackingPlayer) {
                ItemStack itemInHand = player.getMainHandItem();
                if (itemInHand.getItem() instanceof giifusteamp giifusteamp) {
                    // 播放音效
                    if (player.level() instanceof ServerLevel serverLevel) {
                        serverLevel.playSound(null, villager.getX(), villager.getY(), villager.getZ(),
                                ModBossSounds.SEAL.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
                    }
                    // 播放吞噬动画并移除村民
                    villager.remove(Entity.RemovalReason.KILLED);

                    // 生成一个基夫门徒实体
                    Gifftarian giifuEntity = new Gifftarian(ModEntityTypes.GIFFTARIAN.get(), villager.level());
                    giifuEntity.setPos(villager.getX(), villager.getY(), villager.getZ());
                    villager.level().addFreshEntity(giifuEntity);

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
}
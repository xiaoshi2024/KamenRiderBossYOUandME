package com.xiaoshi2022.kamen_rider_boss_you_and_me.network;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.StoriousMonsterBook;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.giifusteamp;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.property.aiziowc;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.client.ParticleTicker;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.ModEntityTypes;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.Another_Zi_o;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.GiifuDemosEntity;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.StoriousEntity;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.giifu.Gifftarian;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModBossSounds;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.VillagerTransformationUtil;
import forge.net.mca.entity.EntitiesMCA;
import forge.net.mca.entity.VillagerEntityMCA;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
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
                int used = aiziowc.getUseCount(stack);

                // 检查是否已经使用了2次（第3次使用）
                if (used >= 2) {
                    // 第3次使用，表盘消失
                    stack.shrink(1);
                    player.displayClientMessage(Component.literal("§d表盘已耗尽，无法再产生异类时王……"), true);
                    return;
                }

                // 增加使用计数（先增加再使用）
                aiziowc.incrUseCount(stack);

                // 消耗1个物品！这是关键缺失的一步
                stack.shrink(1);

                // 其余逻辑：生成异类时王、保存村民数据等...
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
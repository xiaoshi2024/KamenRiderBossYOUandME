package com.xiaoshi2022.kamen_rider_boss_you_and_me.network;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.StoriousMonsterBook;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.giifusteamp;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.ModEntityTypes;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.GiifuDemosEntity;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.StoriousEntity;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.giifu.Gifftarian;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModBossSounds;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.VillagerTransformationUtil;
import forge.net.mca.entity.VillagerEntityMCA;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class VillagerEvents {

    // Shift + 右键人类变成怪人
    @SubscribeEvent
    public static void onRightClick(PlayerInteractEvent.EntityInteract event) {
        Player player = event.getEntity();
        if (player.level().isClientSide || !(player.level() instanceof ServerLevel)) return;

        if (player.isShiftKeyDown()) { // 检查是否按下Shift键
            ItemStack itemInHand = player.getMainHandItem();
            if (event.getTarget() instanceof VillagerEntityMCA mcaVillager) {
                if (itemInHand.getItem() instanceof giifusteamp) {
                    VillagerTransformationUtil.transformToGiifuDemos((ServerLevel) player.level(), mcaVillager, itemInHand.getTag());
                } else if (itemInHand.getItem() instanceof StoriousMonsterBook) {
                    VillagerTransformationUtil.transformToStorious((ServerLevel) player.level(), mcaVillager, itemInHand.getTag());
                }
            }
        }
    }

    @SubscribeEvent
    public static void onAttack(LivingHurtEvent event) {
        LivingEntity hurtEntity = event.getEntity();
        if (hurtEntity instanceof GiifuDemosEntity giifuDemosEntity || hurtEntity instanceof StoriousEntity storiousEntity) {
            if (event.getSource().getDirectEntity() instanceof Player player) {
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
            if (event.getSource().getDirectEntity() instanceof Player player) {
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
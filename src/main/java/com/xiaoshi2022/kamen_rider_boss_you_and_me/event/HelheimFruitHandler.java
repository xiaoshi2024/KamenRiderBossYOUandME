package com.xiaoshi2022.kamen_rider_boss_you_and_me.event;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.advancement.OverlordTrigger;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.sengokudrivers_epmty;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.ModEntityTypes;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.Inves.ElementaryInvesHelheim;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.Lord.LordBaronEntity;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.PacketHandler;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.SyncOwnerPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.varibales.KRBVariables;
import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import tocraft.walkers.api.PlayerShape;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

import java.util.Optional;

@Mod.EventBusSubscriber(modid = "kamen_rider_boss_you_and_me")
public class HelheimFruitHandler {

    @SubscribeEvent
    public static void onHelheimFruitEaten(LivingEntityUseItemEvent.Finish event) {
        // 1. 检查是否是赫尔果实
        if (!event.getItem().is(ModItems.HELHEIMFRUIT.get())) return;

        // 2. 检查是否是玩家且服务器端
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        // 3. 检查是否佩戴战极驱动器(不检查激活状态)
        if (!isWearingSengokuDriver(player)) {
            player.displayClientMessage(
                    Component.translatable("msg.kamen_rider.no_belt")
                            .withStyle(ChatFormatting.RED),
                    true);
            return;
        }

        // 4. 25%变星爵巴隆，75%变普通异域者
        if (player.getRandom().nextFloat() < 0.25f) { // 25%几率
            transformToStarLordBaron(player);
        } else {
            transformToNormalInves(player);
        }
    }

    // 简化版腰带检查，只检查是否佩戴，不检查激活状态
    private static boolean isWearingSengokuDriver(Player player) {
        try {
            Optional<SlotResult> beltOptional = CuriosApi.getCuriosInventory(player).resolve().flatMap(
                    curios -> curios.findFirstCurio(item -> item.getItem() instanceof sengokudrivers_epmty)
            );

            return beltOptional.isPresent();
        } catch (Exception e) {
            kamen_rider_boss_you_and_me.LOGGER.error("检查腰带时出错", e);
            return false;
        }
    }

    private static void transformToStarLordBaron(ServerPlayer player) {
        Level level = player.level();
        EntityType<?> lordBaronType = ModEntityTypes.LORD_BARON.get();

        if (lordBaronType != null) {
            Entity entity = lordBaronType.create(level);
            if (entity instanceof LordBaronEntity lordBaron) {
                // 设置属性
                lordBaron.moveTo(player.getX(), player.getY(), player.getZ(),
                        player.getYRot(), player.getXRot());

                // 关键修复：设置主人
                lordBaron.setOwner(player);

                // 强化属性
                lordBaron.getAttribute(Attributes.MAX_HEALTH).setBaseValue(150.0);
                lordBaron.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(12.0);
                lordBaron.setHealth(lordBaron.getMaxHealth());

                // 使用 Walkers API 进行变身
                if (PlayerShape.updateShapes(player, lordBaron)) {
                    controlNearbyInves(lordBaron, level);

                    // 设置玩家为Overlord状态
                    player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(variables -> {
                        variables.isOverlord = true;
                        variables.syncPlayerVariables(player);
                    });
                    
                    // 触发成就
                    OverlordTrigger.getInstance().trigger(player);
                    
                    player.displayClientMessage(
                            Component.translatable("msg.kamen_rider.star_lord_transformation")
                                    .withStyle(ChatFormatting.GOLD),
                            true);
                    playTransformationEffects(level, lordBaron.position());
                    // 发送网络包到服务器，通知服务器玩家已经变身
                    PacketHandler.sendToServer(new SyncOwnerPacket(lordBaron.getId(), player.getUUID()));

                    // 如果需要，也可以发送到所有跟踪该实体的客户端
                    PacketHandler.sendToAllTracking(new SyncOwnerPacket(lordBaron.getId(), player.getUUID()), lordBaron);
                } else {
                    lordBaron.discard();
                }
            }
        }
    }

    private static void transformToNormalInves(ServerPlayer player) {
        Level level = player.level();
        EntityType<?> invesType = ModEntityTypes.INVES_HEILEHIM.get();

        if (invesType != null) {
            Entity entity = invesType.create(level);
            if (entity instanceof ElementaryInvesHelheim inves) {
                inves.moveTo(player.getX(), player.getY(), player.getZ(),
                        player.getYRot(), player.getXRot());

                // 设置为敌对状态
                inves.setTarget(player);

                if (PlayerShape.updateShapes(player, inves)) {
                    player.displayClientMessage(
                            Component.translatable("msg.kamen_rider.inves_transformation")
                                    .withStyle(ChatFormatting.RED),
                            true);
                    playTransformationEffects(level, inves.position());
                } else {
                    inves.discard();
                    player.displayClientMessage(
                            Component.translatable("msg.kamen_rider.transformation_failed")
                                    .withStyle(ChatFormatting.RED),
                            true);
                }
            }
        }
    }

    private static void controlNearbyInves(LordBaronEntity lordBaron, Level level) {
        AABB area = new AABB(lordBaron.blockPosition()).inflate(20.0);
        level.getEntitiesOfClass(ElementaryInvesHelheim.class, area).forEach(inves -> {
            // 清除旧的阵营关系
            inves.setFactionLeader(null);

            // 1. 彻底清除仇恨和攻击目标
            inves.setTarget(null);
            inves.setLastHurtByMob(null);
            inves.setLastHurtMob(null);

            // 2. 设置主人
            inves.setMaster(lordBaron);

            // 3. 添加视觉效果
            inves.addEffect(new MobEffectInstance(MobEffects.GLOWING, 400, 0, false, true));
            inves.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 400, 1, false, true));
            inves.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 400, 1, false, true));

            // 4. 播放音效
            level.playSound(null, inves.getX(), inves.getY(), inves.getZ(),
                    SoundEvents.ILLUSIONER_CAST_SPELL, SoundSource.HOSTILE, 1.0f, 1.0f);
        });
    }

    private static void playTransformationEffects(Level level, Vec3 pos) {
        level.playSound(null, pos.x(), pos.y(), pos.z(),
                SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.HOSTILE, 1.0f, 0.8f);

        for (int i = 0; i < 30; i++) {
            level.addParticle(ParticleTypes.ELECTRIC_SPARK,
                    pos.x() + (level.random.nextDouble() - 0.5) * 2.0,
                    pos.y() + level.random.nextDouble(),
                    pos.z() + (level.random.nextDouble() - 0.5) * 2.0,
                    0, 0.1, 0);
        }
    }
}
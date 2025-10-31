package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.Another_Den_os;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.anotherRiders.Another_Den_o;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3d;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.BlockAndItemGeoLayer;

import javax.annotation.Nullable;

public class Another_Den_oRenderer extends GeoEntityRenderer<Another_Den_o> {

    private int currentTick;

    public Another_Den_oRenderer(EntityRendererProvider.Context context) {
        super(context, new Another_Den_oModel());

        // 右手持物层
        addRenderLayer(new BlockAndItemGeoLayer<>(this) {
            @Nullable
            @Override
            protected ItemStack getStackForBone(GeoBone bone, Another_Den_o entity) {
                if ("rightItem".equals(bone.getName())) {
                    return entity.getMainHandItem();
                }
                return null;
            }

            @Override
            protected ItemDisplayContext getTransformTypeForStack(GeoBone bone, ItemStack stack, Another_Den_o entity) {
                return ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
            }

            @Override
            protected void renderStackForBone(PoseStack poseStack, GeoBone bone, ItemStack stack,
                                              Another_Den_o entity, MultiBufferSource bufferSource,
                                              float partialTick, int packedLight, int packedOverlay) {
                poseStack.translate(0.1F, 0.2F, 0F);
                poseStack.mulPose(Axis.XP.rotationDegrees(-90));
                super.renderStackForBone(poseStack, bone, stack, entity, bufferSource, partialTick, packedLight, packedOverlay);
            }
        });
    }

    // 粒子特效
    @Override
    public void renderFinal(PoseStack poseStack, Another_Den_o animatable, BakedGeoModel model,
                            MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick,
                            int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {

        if (this.currentTick < 0 || this.currentTick != animatable.tickCount) {
            this.currentTick = animatable.tickCount;

            // 模型中腰带骨骼名称，请与 Blockbench 保持一致
            this.model.getBone("denobelt").ifPresent(belt -> {
                RandomSource rand = animatable.getRandom();
                Vector3d pos = belt.getWorldPosition();
                animatable.level().addParticle(ParticleTypes.FLAME,
                        pos.x(), pos.y(), pos.z(),
                        rand.nextDouble() - 0.5D,
                        -rand.nextDouble(),
                        rand.nextDouble() - 0.5D);
            });
        }
        super.renderFinal(poseStack, animatable, model, bufferSource, buffer, partialTick,
                packedLight, packedOverlay, red, green, blue, alpha);
    }
}
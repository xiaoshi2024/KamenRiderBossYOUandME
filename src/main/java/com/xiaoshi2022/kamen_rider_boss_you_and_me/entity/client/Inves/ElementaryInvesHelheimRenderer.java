package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.Inves;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.Inves.ElementaryInvesHelheim;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3d;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.BlockAndItemGeoLayer;

import javax.annotation.Nullable;

public class ElementaryInvesHelheimRenderer extends GeoEntityRenderer<ElementaryInvesHelheim> {
    private int currentTick;

    public ElementaryInvesHelheimRenderer(EntityRendererProvider.Context context) {
        super(context, new ElementaryInvesHelheimModel());
        this.shadowRadius = 0.5f;
        //  可以添加发光纹理
//        addRenderLayer(new AutoGlowingGeoLayer<>(this));
        // 添加手持物品渲染层
        addRenderLayer(new BlockAndItemGeoLayer<>(this) {
            @Nullable
            @Override
            protected ItemStack getStackForBone(GeoBone bone, ElementaryInvesHelheim entity) {
                // 绑定右手骨骼（名称需与Blockbench模型一致）
                if ("rightItem".equals(bone.getName())) {
                    return entity.getMainHandItem();
                }
                return null;
            }

            @Override
            protected ItemDisplayContext getTransformTypeForStack(GeoBone bone, ItemStack stack, ElementaryInvesHelheim entity) {
                // 设置第三人称右手渲染模式
                return ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
            }

            @Override
            protected void renderStackForBone(PoseStack poseStack, GeoBone bone, ItemStack stack, ElementaryInvesHelheim entity,
                                              MultiBufferSource bufferSource, float partialTick, int packedLight, int packedOverlay) {
                // 微调物品位置（可选）
                poseStack.translate(0.1f, 0.2f, 0); // X/Y/Z偏移
                poseStack.mulPose(Axis.XP.rotationDegrees(-90)); // 旋转
                super.renderStackForBone(poseStack, bone, stack, entity, bufferSource, partialTick, packedLight, packedOverlay);
            }
        });
    }


    @Override
    public ResourceLocation getTextureLocation(ElementaryInvesHelheim animatable) {
        String variant = animatable.getVariant();
        return new ResourceLocation(kamen_rider_boss_you_and_me.MODID, "textures/entity/inves_heilehim_" + variant + ".png");
    }

    // 渲染时在耳朵周围添加一些粒子
    @Override
    public void renderFinal(PoseStack poseStack, ElementaryInvesHelheim animatable, BakedGeoModel model, MultiBufferSource
    bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        if (this.currentTick < 0 || this.currentTick != animatable.tickCount) {
            this.currentTick = animatable.tickCount;

            // Find the earbone and use it as the point of reference
            this.model.getBone("leftear").ifPresent(ear -> {
                RandomSource rand = animatable.getRandom();
                Vector3d earPos = ear.getWorldPosition();

                animatable.getCommandSenderWorld().addParticle(ParticleTypes.PORTAL,
                        earPos.x(),
                        earPos.y(),
                        earPos.z(),
                        rand.nextDouble() - 0.5D,
                        -rand.nextDouble(),
                        rand.nextDouble() - 0.5D);
            });
        }

        super.renderFinal(poseStack, animatable, model, bufferSource, buffer, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
    }
}

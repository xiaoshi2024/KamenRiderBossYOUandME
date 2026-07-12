package com.xiaoshi2022.kamenriderbossyouandme.impl.geckolib.entity;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

import javax.annotation.Nullable;

/**
 * 通用骑士实体渲染器（支持透明度）
 */
public class RiderEffectRenderer<T extends BaseKamenRiderEffectEntity> extends GeoEntityRenderer<T> {

    public RiderEffectRenderer(
            EntityRendererProvider.Context context,
            GeoModel<T> model
    ) {
        super(context, model);
        this.shadowRadius = 0.0f;
    }

    @Override
    public void render(T entity, float entityYaw, float partialTicks,
                       @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight) {

        poseStack.pushPose();

        // 计算插值旋转（平滑过渡）
        float interpolatedYaw = Mth.lerp(partialTicks, entity.yRotO, entity.getYRot());

        // 应用旋转
        // Y轴旋转（偏航）需要减去180度，因为模型通常面朝-Z方向
        poseStack.mulPose(Axis.YP.rotationDegrees(-interpolatedYaw));

        // 处理透明度
        if (entity.shouldApplyTransparency()) {
            float alpha = entity.getCurrentAlpha();

            if (alpha < 0.01f) {
                poseStack.popPose();
                return;
            }

            // 应用透明度前保存当前状态
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();

            // 应用透明度
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha);

            super.render(entity, entityYaw, partialTicks, poseStack, bufferSource, packedLight);
        } else {
            // 正常渲染
            super.render(entity, entityYaw, partialTicks, poseStack, bufferSource, packedLight);
        }

        poseStack.popPose();
    }

    @Override
    public @Nullable RenderType getRenderType(BaseKamenRiderEffectEntity animatable, ResourceLocation texture,
                                              @Nullable MultiBufferSource bufferSource, float partialTick) {
        // 根据是否应用透明度选择合适的渲染类型
        if (animatable.shouldApplyTransparency()) {
            return RenderType.entityTranslucent(texture);
        }
        return RenderType.entityCutoutNoCull(texture);
    }
}

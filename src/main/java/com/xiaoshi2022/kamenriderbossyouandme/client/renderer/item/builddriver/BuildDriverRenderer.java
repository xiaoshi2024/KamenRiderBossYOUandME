package com.xiaoshi2022.kamenriderbossyouandme.client.renderer.item.builddriver;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.xiaoshi2022.kamenriderbossyouandme.Accessory.BuildDriver;
import com.xiaoshi2022.kamenriderbossyouandme.client.model.item.builddriver.BuildDriverModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

public class BuildDriverRenderer extends GeoItemRenderer<BuildDriver> implements ICurioRenderer {
    private final BuildDriverModel defaultModel = new BuildDriverModel(BuildDriver.BeltMode.DEFAULT);
    private final BuildDriverModel rtModel = new BuildDriverModel(BuildDriver.BeltMode.RT);
    private final BuildDriverModel rModel = new BuildDriverModel(BuildDriver.BeltMode.R);
    private final BuildDriverModel tModel = new BuildDriverModel(BuildDriver.BeltMode.T);
    private final BuildDriverModel hazardEmptyModel = new BuildDriverModel(BuildDriver.BeltMode.HAZARD_EMPTY);
    private final BuildDriverModel hazardRtModel = new BuildDriverModel(BuildDriver.BeltMode.HAZARD_RT);
    private final BuildDriverModel hazardRModel = new BuildDriverModel(BuildDriver.BeltMode.HAZARD_R);
    private final BuildDriverModel hazardTModel = new BuildDriverModel(BuildDriver.BeltMode.HAZARD_T);
    private final BuildDriverModel hazardKModel = new BuildDriverModel(BuildDriver.BeltMode.HAZARD_K);
    private final BuildDriverModel hazardKrModel = new BuildDriverModel(BuildDriver.BeltMode.HAZARD_KR);
    private final BuildDriverModel hazardRessyaModel = new BuildDriverModel(BuildDriver.BeltMode.HAZARD_RESSYA);
    private final BuildDriverModel hazardRtMouldModel = new BuildDriverModel(BuildDriver.BeltMode.HAZARD_RT_MOULD);
    
    private ItemStack currentItemStack;
    
    public BuildDriverRenderer() {
        super(new BuildDriverModel());
    }
    
    @Override
    public GeoModel<BuildDriver> getGeoModel() {
        if (currentItemStack != null && currentItemStack.getItem() instanceof BuildDriver belt) {
            BuildDriver.BeltMode mode = belt.getMode(currentItemStack);
            boolean isTransforming = belt.getIsTransforming(currentItemStack);
            
            if ((isTransforming && mode == BuildDriver.BeltMode.HAZARD_RT) || mode == BuildDriver.BeltMode.HAZARD_RT_MOULD) {
                return hazardRtMouldModel;
            }
            
            return switch (mode) {
                case RT -> rtModel;
                case R -> rModel;
                case T -> tModel;
                case HAZARD_EMPTY -> hazardEmptyModel;
                case HAZARD_RT -> hazardRtModel;
                case HAZARD_R -> hazardRModel;
                case HAZARD_T -> hazardTModel;
                case HAZARD_K -> hazardKModel;
                case HAZARD_KR -> hazardKrModel;
                case HAZARD_RESSYA -> hazardRessyaModel;
                default -> defaultModel;
            };
        }
        return super.getGeoModel();
    }
    
    @Override
    public void renderByItem(ItemStack stack, 
                             ItemDisplayContext transformType, 
                             PoseStack poseStack, 
                             MultiBufferSource buffer, 
                             int packedLight, 
                             int packedOverlay) {
        this.currentItemStack = stack;
        super.renderByItem(stack, transformType, poseStack, buffer, packedLight, packedOverlay);
    }

    @Override
    public <T extends LivingEntity, M extends EntityModel<T>> void render(ItemStack itemStack, SlotContext slotContext, PoseStack poseStack, RenderLayerParent<T, M> renderLayerParent, MultiBufferSource multiBufferSource, int light, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        this.currentItemStack = itemStack;
        
        poseStack.pushPose();
        if (renderLayerParent.getModel() instanceof HumanoidModel) {
            @SuppressWarnings("unchecked")
            HumanoidModel<T> model = (HumanoidModel<T>) renderLayerParent.getModel();

            model.body.translateAndRotate(poseStack);

            poseStack.scale(0.5F, 0.5F, 0.5F);
            poseStack.translate(0.0F, 1.2, -0.2);

            poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));

            ItemInHandRenderer renderer = new ItemInHandRenderer(
                    Minecraft.getInstance(),
                    Minecraft.getInstance().getEntityRenderDispatcher(),
                    Minecraft.getInstance().getItemRenderer()
            );
            renderer.renderItem(
                    slotContext.entity(),
                    itemStack,
                    ItemDisplayContext.FIXED,
                    false,
                    poseStack,
                    multiBufferSource,
                    light
            );
        }
        poseStack.popPose();
    }
}
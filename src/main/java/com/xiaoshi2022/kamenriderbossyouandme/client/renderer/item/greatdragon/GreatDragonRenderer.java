package com.xiaoshi2022.kamenriderbossyouandme.client.renderer.item.greatdragon;

import com.xiaoshi2022.kamenriderbossyouandme.client.model.item.greatdragon.GreatDragonModel;
import com.xiaoshi2022.kamenriderbossyouandme.items.prop.GreatDragon;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class GreatDragonRenderer extends GeoItemRenderer<GreatDragon> {

    private final GreatDragonModel normalModel = new GreatDragonModel(GreatDragon.Mode.NORMAL);
    private final GreatDragonModel emptyModel = new GreatDragonModel(GreatDragon.Mode.EMPTY);

    private ItemStack currentItemStack;

    public GreatDragonRenderer() {
        super(new GreatDragonModel());
    }

    @Override
    public GeoModel<GreatDragon> getGeoModel() {
        if (currentItemStack != null && currentItemStack.getItem() instanceof GreatDragon item) {
            GreatDragon.Mode mode = item.getMode(currentItemStack);
            return switch (mode) {
                case NORMAL -> normalModel;
                default -> emptyModel;
            };
        }
        return emptyModel;
    }

    @Override
    public void renderByItem(ItemStack stack,
                             net.minecraft.world.item.ItemDisplayContext transformType,
                             com.mojang.blaze3d.vertex.PoseStack poseStack,
                             net.minecraft.client.renderer.MultiBufferSource buffer,
                             int packedLight,
                             int packedOverlay) {
        this.currentItemStack = stack;
        super.renderByItem(stack, transformType, poseStack, buffer, packedLight, packedOverlay);
    }
}
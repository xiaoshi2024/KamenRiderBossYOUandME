package com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.knightinvoker;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.KnightInvokerBuckle;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.model.GeoModel;

public class KnightInvokerBuckleModel extends GeoModel<KnightInvokerBuckle> {
    private final boolean isEmpty;
    
    public KnightInvokerBuckleModel(ResourceLocation knight_invoker_buckle, boolean isEmpty) {
        super();
        this.isEmpty = isEmpty;
    }
    
    @Override
    public ResourceLocation getModelResource(KnightInvokerBuckle animatable) {
        if (isEmpty) {
            return new ResourceLocation("kamen_rider_boss_you_and_me", "geo/item/knight_invoker_empty.geo.json");
        } else {
            return new ResourceLocation("kamen_rider_boss_you_and_me", "geo/item/knight_invoker.geo.json");
        }
    }

    @Override
    public ResourceLocation getTextureResource(KnightInvokerBuckle animatable){
        return new ResourceLocation("kamen_rider_boss_you_and_me", "textures/item/knight_invoker.png");
    }

    @Override
    public ResourceLocation getAnimationResource(KnightInvokerBuckle animatable) {
        return new ResourceLocation("kamen_rider_boss_you_and_me", "animations/item/knight_invoker.animation.json");
    }
}
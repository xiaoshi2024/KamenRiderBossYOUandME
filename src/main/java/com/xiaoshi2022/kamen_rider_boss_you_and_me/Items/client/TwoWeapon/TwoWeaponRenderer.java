package com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.TwoWeapon;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.TwoWeaponItem;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.KRBVariables;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class TwoWeaponRenderer extends GeoItemRenderer<TwoWeaponItem> {

    private static final GeoModel<TwoWeaponItem> DEFAULT_MODEL =
            new TwoWeaponModel<>(new ResourceLocation("kamen_rider_boss_you_and_me", "two_weapon"));
    private static final GeoModel<TwoWeaponItem> BAT_MODEL     = new TwoWeaponBatModel<>();

    public TwoWeaponRenderer() {
        super(DEFAULT_MODEL);
    }

    @Override
    public GeoModel<TwoWeaponItem> getGeoModel() {
        /* 3. 把 ItemStack 传进去，不然会报“0 个实参” */
        TwoWeaponItem.Variant v = TwoWeaponItem.getVariant(currentItemStack);
        
        // 检测玩家是否为变身状态和腰带是否存在
        checkPlayerTransformationState();
        
        return switch (v) {
            case BAT -> BAT_MODEL;
            default  -> DEFAULT_MODEL;
        };
    }
    
    // 新增：检测玩家是否为变身状态和腰带是否存在
    private void checkPlayerTransformationState() {
        // 获取当前客户端玩家
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;
        
        // 获取玩家变量
        KRBVariables.PlayerVariables variables = player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(null);
        if (variables == null) return;
        
        // 检查双面武器是否为BAT形态
        TwoWeaponItem.Variant variant = TwoWeaponItem.getVariant(currentItemStack);
        
        // 如果玩家使用的是BAT形态的双面武器，但EvilBats变身状态为false，可以考虑在客户端进行一些视觉提示
        if (variant == TwoWeaponItem.Variant.BAT && !variables.isEvilBatsTransformed) {
            // 这里可以添加一些视觉提示，比如闪烁效果或者粒子效果
            // 注意：这只是客户端的视觉效果，实际的解除变身逻辑在服务端处理
        }
    }

    @Override
    public ResourceLocation getTextureLocation(TwoWeaponItem animatable) {
        TwoWeaponItem.Variant v = TwoWeaponItem.getVariant(currentItemStack);
        return switch (v) {
            case BAT -> BAT_MODEL.getTextureResource(animatable);
            default  -> DEFAULT_MODEL.getTextureResource(animatable);
        };
    }
}
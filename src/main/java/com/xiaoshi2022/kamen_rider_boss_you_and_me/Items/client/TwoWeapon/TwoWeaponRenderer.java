package com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.TwoWeapon;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.TwoWeaponItem;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.weapon.TwoWeaponGunItem;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.weapon.TwoWeaponSwordItem;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.KRBVariables;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class TwoWeaponRenderer extends GeoItemRenderer<TwoWeaponItem> {

    private static final GeoModel<TwoWeaponItem> DEFAULT_MODEL = 
            new TwoWeaponModel<>(new ResourceLocation("kamen_rider_boss_you_and_me", "two_weapon"));
    private static final GeoModel<TwoWeaponItem> SWORD_MODEL = new TwoWeaponSwordModel<>();
    private static final GeoModel<TwoWeaponItem> GUN_MODEL = new TwoWeaponGunModel<>();
    private static final GeoModel<TwoWeaponItem> BAT_MODEL = new TwoWeaponBatModel<>();

    public TwoWeaponRenderer() {
        super(DEFAULT_MODEL);
    }

    @Override
    public GeoModel<TwoWeaponItem> getGeoModel() {
        // 检测玩家是否为变身状态和腰带是否存在
        checkPlayerTransformationState();
        
        // 判断物品类型来决定使用什么模型
        if (currentItemStack.getItem() instanceof TwoWeaponItem) {
            // 检查武器是否为BAT变种
            TwoWeaponItem.Variant v = TwoWeaponItem.getVariant(currentItemStack);
            if (v == TwoWeaponItem.Variant.BAT) {
                // 所有BAT变种的武器都使用BAT_MODEL
                return BAT_MODEL;
            }
            
            // 非BAT变种时，根据物品类型选择模型
            if (currentItemStack.getItem() instanceof TwoWeaponSwordItem) {
                // 对于非BAT变种的剑形态武器，使用专门的剑模型
                return SWORD_MODEL;
            } else if (currentItemStack.getItem() instanceof TwoWeaponGunItem) {
                // 对于非BAT变种的枪形态武器，使用专门的枪模型
                return GUN_MODEL;
            } else {
                // 对于原始的双面武器，使用默认模型
                return DEFAULT_MODEL;
            }
        }
        return DEFAULT_MODEL;
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
        
        // 检查双面武器是否为BAT形态（只针对原始的TwoWeaponItem）
        if (currentItemStack.getItem() instanceof TwoWeaponItem) {
            TwoWeaponItem.Variant variant = TwoWeaponItem.getVariant(currentItemStack);
            
            // 如果玩家使用的是BAT形态的双面武器，但EvilBats变身状态为false，可以考虑在客户端进行一些视觉提示
            if (variant == TwoWeaponItem.Variant.BAT && !variables.isEvilBatsTransformed) {
                // 这里可以添加一些视觉提示，比如闪烁效果或者粒子效果
                // 注意：这只是客户端的视觉效果，实际的解除变身逻辑在服务端处理
            }
        }
    }

    @Override
    public ResourceLocation getTextureLocation(TwoWeaponItem animatable) {
        // 修复ClassCastException：完全基于currentItemStack而不依赖animatable参数
        if (currentItemStack.getItem() instanceof TwoWeaponItem) {
            // 检查武器是否为BAT变种
            TwoWeaponItem.Variant v = TwoWeaponItem.getVariant(currentItemStack);
            if (v == TwoWeaponItem.Variant.BAT) {
                // 所有BAT变种的武器都使用BAT_MODEL的纹理
                return BAT_MODEL.getTextureResource((TwoWeaponItem) null);
            }
            
            // 非BAT变种时，根据物品类型选择纹理
            if (currentItemStack.getItem() instanceof TwoWeaponSwordItem) {
                // 对于非BAT变种的剑形态武器，使用剑模型的纹理
                return SWORD_MODEL.getTextureResource((TwoWeaponItem) null);
            } else if (currentItemStack.getItem() instanceof TwoWeaponGunItem) {
                // 对于非BAT变种的枪形态武器，使用枪模型的纹理
                return GUN_MODEL.getTextureResource((TwoWeaponItem) null);
            } else {
                // 对于原始的双面武器，使用默认模型的纹理
                return DEFAULT_MODEL.getTextureResource((TwoWeaponItem) null);
            }
        }
        return DEFAULT_MODEL.getTextureResource((TwoWeaponItem) null);
    }
}
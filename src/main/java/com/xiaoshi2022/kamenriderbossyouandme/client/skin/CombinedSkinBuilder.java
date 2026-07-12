package com.xiaoshi2022.kamenriderbossyouandme.client.skin;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 组合纹理构建器
 *
 * 纹理布局 (256x256):
 *   - 玩家1: [0, 0] 到 [64, 64]
 *   - 玩家2: [64, 0] 到 [128, 64]
 *   - 玩家3: [128, 0] 到 [192, 64]
 *   - 特效: [0, 64] 到 [256, 256] (bloodtx.png)
 */
@OnlyIn(Dist.CLIENT)
public class CombinedSkinBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(CombinedSkinBuilder.class);

    private static final int SKIN_SIZE = 64;
    private static final int TEXTURE_WIDTH = 256;
    private static final int TEXTURE_HEIGHT = 256;
    private static final int EFFECT_Y_OFFSET = 64;

    private static final Map<String, ResourceLocation> CACHE = new ConcurrentHashMap<>();

    // ✅ 正确的史蒂夫皮肤路径
    private static final ResourceLocation STEVE_SKIN =
            ResourceLocation.fromNamespaceAndPath("minecraft", "textures/entity/player/wide/steve.png");

    // 亚历克斯皮肤（备用）
    private static final ResourceLocation ALEX_SKIN =
            ResourceLocation.fromNamespaceAndPath("minecraft", "textures/entity/player/wide/alex.png");

    private static final ResourceLocation EFFECT_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("kamenriderbossyouandme", "textures/entity/bloodtx.png");

    private static ResourceLocation defaultCombined = null;

    public static ResourceLocation getOrCreate(String key, ResourceLocation[] skins) {
        if (key == null || key.isEmpty() || skins == null || skins.length < 3) {
            return getDefaultCombined();
        }

        ResourceLocation cached = CACHE.get(key);
        if (cached != null) {
            return cached;
        }

        try {
            ResourceLocation combined = buildCombinedSkin(skins);
            CACHE.put(key, combined);
            LOGGER.info("✅ 组合纹理已创建: {}", key);
            return combined;
        } catch (Exception e) {
            LOGGER.error("❌ 创建组合纹理失败: {}", key, e);
            return getDefaultCombined();
        }
    }

    private static ResourceLocation buildCombinedSkin(ResourceLocation[] skins) throws IOException {
        TextureManager textureManager = Minecraft.getInstance().getTextureManager();
        var resourceManager = Minecraft.getInstance().getResourceManager();

        NativeImage combined = new NativeImage(TEXTURE_WIDTH, TEXTURE_HEIGHT, true);

        // 1. 先加载特效纹理
        loadEffectTexture(combined, resourceManager);

        // 2. 加载3个玩家皮肤
        for (int i = 0; i < 3 && i < skins.length; i++) {
            ResourceLocation skin = skins[i];
            if (skin == null) {
                skin = STEVE_SKIN;
            }
            loadSkinToRegion(combined, resourceManager, skin, i);
        }

        String hash = UUID.randomUUID().toString().substring(0, 8);
        ResourceLocation location = ResourceLocation.fromNamespaceAndPath(
                "kamenriderbossyouandme",
                "skins/combined_" + hash
        );

        textureManager.register(location, new DynamicTexture(combined));
        return location;
    }

    private static void loadEffectTexture(NativeImage combined, net.minecraft.server.packs.resources.ResourceManager resourceManager) {
        try {
            var resource = resourceManager.getResource(EFFECT_TEXTURE);
            if (resource.isPresent()) {
                try (var input = resource.get().open()) {
                    NativeImage effectImage = NativeImage.read(input);

                    int effectHeight = effectImage.getHeight();
                    int effectWidth = effectImage.getWidth();

                    for (int x = 0; x < Math.min(effectWidth, TEXTURE_WIDTH); x++) {
                        for (int y = 0; y < Math.min(effectHeight, TEXTURE_HEIGHT - EFFECT_Y_OFFSET); y++) {
                            int color = effectImage.getPixelRGBA(x, y);
                            combined.setPixelRGBA(x, y + EFFECT_Y_OFFSET, color);
                        }
                    }
                    effectImage.close();
                    LOGGER.debug("✅ 特效纹理已加载");
                }
            } else {
                LOGGER.warn("⚠️ 找不到特效纹理: {}", EFFECT_TEXTURE);
            }
        } catch (Exception e) {
            LOGGER.warn("⚠️ 加载特效纹理失败", e);
        }
    }

    private static void loadSkinToRegion(NativeImage combined, net.minecraft.server.packs.resources.ResourceManager resourceManager,
                                         ResourceLocation skin, int index) {
        int offsetX = index * SKIN_SIZE;
        try {
            var resource = resourceManager.getResource(skin);
            if (resource.isPresent()) {
                try (var input = resource.get().open()) {
                    NativeImage skinImage = NativeImage.read(input);
                    copySkinToRegion(combined, skinImage, offsetX);
                    skinImage.close();
                    LOGGER.debug("✅ 皮肤 {} -> 区域 {}", skin, index);
                    return;
                }
            }

            // 如果找不到，尝试使用史蒂夫皮肤
            LOGGER.warn("⚠️ 找不到皮肤: {}, 使用史蒂夫替代", skin);
            loadSteveSkinToRegion(combined, resourceManager, offsetX);

        } catch (Exception e) {
            LOGGER.warn("⚠️ 加载皮肤失败: {}, 使用史蒂夫替代", skin, e);
            loadSteveSkinToRegion(combined, resourceManager, offsetX);
        }
    }

    private static void copySkinToRegion(NativeImage combined, NativeImage skinImage, int offsetX) {
        int skinWidth = Math.min(skinImage.getWidth(), SKIN_SIZE);
        int skinHeight = Math.min(skinImage.getHeight(), SKIN_SIZE);

        for (int x = 0; x < skinWidth; x++) {
            for (int y = 0; y < skinHeight; y++) {
                int color = skinImage.getPixelRGBA(x, y);
                combined.setPixelRGBA(offsetX + x, y, color);
            }
        }
    }

    private static void loadSteveSkinToRegion(NativeImage combined, net.minecraft.server.packs.resources.ResourceManager resourceManager, int offsetX) {
        try {
            var resource = resourceManager.getResource(STEVE_SKIN);
            if (resource.isPresent()) {
                try (var input = resource.get().open()) {
                    NativeImage skinImage = NativeImage.read(input);
                    copySkinToRegion(combined, skinImage, offsetX);
                    skinImage.close();
                    LOGGER.debug("✅ 史蒂夫皮肤已加载到区域 {}", offsetX / SKIN_SIZE);
                }
            } else {
                // 尝试亚历克斯皮肤
                var alexResource = resourceManager.getResource(ALEX_SKIN);
                if (alexResource.isPresent()) {
                    try (var input = alexResource.get().open()) {
                        NativeImage skinImage = NativeImage.read(input);
                        copySkinToRegion(combined, skinImage, offsetX);
                        skinImage.close();
                        LOGGER.debug("✅ 亚历克斯皮肤已加载到区域 {}", offsetX / SKIN_SIZE);
                    }
                } else {
                    LOGGER.error("❌ 找不到任何默认皮肤！");
                }
            }
        } catch (Exception e) {
            LOGGER.error("❌ 加载默认皮肤失败", e);
        }
    }

    private static ResourceLocation getDefaultCombined() {
        if (defaultCombined != null) {
            return defaultCombined;
        }

        try {
            defaultCombined = buildCombinedSkin(new ResourceLocation[]{STEVE_SKIN, STEVE_SKIN, STEVE_SKIN});
            LOGGER.info("✅ 默认组合纹理已创建");
            return defaultCombined;
        } catch (Exception e) {
            LOGGER.error("❌ 创建默认组合纹理失败", e);
            return EFFECT_TEXTURE;
        }
    }

    public static void clearCache() {
        CACHE.clear();
        defaultCombined = null;
        LOGGER.info("🧹 已清除组合纹理缓存");
    }

    public static int getCacheSize() {
        return CACHE.size();
    }
}
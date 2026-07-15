package com.xiaoshi2022.kamenriderbossyouandme.client.skin;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.HttpTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
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
            NativeImage skinImage = loadSkinImage(skin, resourceManager);
            if (skinImage != null) {
                copySkinToRegion(combined, skinImage, offsetX);
                skinImage.close();
                LOGGER.debug("✅ 皮肤 {} -> 区域 {}", skin, index);
                return;
            }

            LOGGER.warn("⚠️ 找不到皮肤: {}, 使用史蒂夫替代", skin);
            loadSteveSkinToRegion(combined, resourceManager, offsetX);

        } catch (Exception e) {
            LOGGER.warn("⚠️ 加载皮肤失败: {}, 使用史蒂夫替代", skin, e);
            loadSteveSkinToRegion(combined, resourceManager, offsetX);
        }
    }

    private static NativeImage loadSkinImage(ResourceLocation skin, net.minecraft.server.packs.resources.ResourceManager resourceManager) {
        try {
            var resource = resourceManager.getResource(skin);
            if (resource.isPresent()) {
                try (var input = resource.get().open()) {
                    return NativeImage.read(input);
                }
            }
        } catch (Exception e) {
            LOGGER.debug("资源管理器找不到皮肤 {}, 尝试从缓存路径获取", skin);
        }

        Path cachedPath = SkinIntegration.getSkinFilePath(skin);
        if (cachedPath != null && Files.exists(cachedPath)) {
            try (InputStream input = Files.newInputStream(cachedPath)) {
                NativeImage image = NativeImage.read(input);
                LOGGER.debug("✅ 从缓存路径加载皮肤: {}", skin);
                return image;
            } catch (Exception e) {
                LOGGER.debug("从缓存路径加载皮肤失败: {}", e.getMessage());
            }
        }

        TextureManager textureManager = Minecraft.getInstance().getTextureManager();
        Object texture = textureManager.getTexture(skin);
        
        if (texture != null) {
            try {
                java.lang.reflect.Method loadMethod = texture.getClass().getMethod("load", TextureManager.class, ResourceLocation.class);
                loadMethod.setAccessible(true);
                loadMethod.invoke(texture, textureManager, skin);
            } catch (Exception e) {
                LOGGER.debug("调用纹理load方法失败: {}", e.getMessage());
            }
        }

        if (texture instanceof HttpTexture httpTexture) {
            try {
                java.lang.reflect.Field textureDataField = HttpTexture.class.getDeclaredField("textureData");
                textureDataField.setAccessible(true);
                Object textureData = textureDataField.get(httpTexture);
                
                if (textureData != null) {
                    java.lang.reflect.Method getDataMethod = textureData.getClass().getMethod("getData");
                    getDataMethod.setAccessible(true);
                    NativeImage data = (NativeImage) getDataMethod.invoke(textureData);
                    if (data != null) {
                        NativeImage copy = new NativeImage(data.getWidth(), data.getHeight(), true);
                        copy.copyFrom(data);
                        return copy;
                    }
                }
            } catch (Exception e) {
                LOGGER.debug("从 HttpTexture 获取textureData失败: {}", e.getMessage());
            }
            
            try {
                java.lang.reflect.Field fileField = HttpTexture.class.getDeclaredField("file");
                fileField.setAccessible(true);
                java.io.File file = (java.io.File) fileField.get(httpTexture);
                if (file != null && file.exists()) {
                    try (InputStream input = Files.newInputStream(file.toPath())) {
                        return NativeImage.read(input);
                    }
                }
            } catch (Exception e) {
                LOGGER.debug("从 HttpTexture 获取文件失败: {}", e.getMessage());
            }
        }

        if (texture instanceof DynamicTexture dynamicTexture) {
            try {
                java.lang.reflect.Field pixelsField = DynamicTexture.class.getDeclaredField("pixels");
                pixelsField.setAccessible(true);
                NativeImage pixels = (NativeImage) pixelsField.get(dynamicTexture);
                if (pixels != null) {
                    NativeImage copy = new NativeImage(pixels.getWidth(), pixels.getHeight(), true);
                    copy.copyFrom(pixels);
                    return copy;
                }
            } catch (Exception e) {
                LOGGER.debug("从 DynamicTexture 获取皮肤失败: {}", e.getMessage());
            }
        }

        return null;
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

    public static void invalidateCache(String key) {
        if (key != null && CACHE.remove(key) != null) {
            LOGGER.debug("🗑️ 组合纹理缓存已失效: {}", key);
        }
    }

    public static int getCacheSize() {
        return CACHE.size();
    }
}
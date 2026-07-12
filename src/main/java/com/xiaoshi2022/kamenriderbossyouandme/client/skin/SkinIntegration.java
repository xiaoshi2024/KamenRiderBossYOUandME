// client/skin/SkinIntegration.java
package com.xiaoshi2022.kamenriderbossyouandme.client.skin;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.HttpTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModList;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.UUID;

import static com.xiaoshi2022.kamenriderbossyouandme.KamenRiderBossYOUandME.MODID;

public class SkinIntegration {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static boolean cslLoaded = false;

    static {
        try {
            cslLoaded = ModList.get().isLoaded("customskinloader");
            if (cslLoaded) {
                LOGGER.info("✅ CustomSkinLoader 已安装");
            }
        } catch (Exception e) {
            cslLoaded = false;
        }
    }

    public static ResourceLocation getPlayerSkin(String username) {
        if (!cslLoaded || username == null || username.isEmpty()) {
            return null;
        }

        try {
            UUID fakeUuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + username).getBytes());
            GameProfile profile = new GameProfile(fakeUuid, username);

            Class<?> customSkinLoaderClass = Class.forName("customskinloader.CustomSkinLoader");
            Object userProfile = customSkinLoaderClass.getMethod("loadProfile", GameProfile.class)
                    .invoke(null, profile);

            if (userProfile != null) {
                String skinUrl = (String) userProfile.getClass().getField("skinUrl").get(userProfile);

                if (skinUrl != null && !skinUrl.isEmpty()) {
                    LOGGER.info("📥 CSL 找到皮肤: {} -> {}", username, skinUrl);
                    MinecraftProfileTexture texture = new MinecraftProfileTexture(skinUrl, null);
                    return registerTextureDirectly(texture, username);
                }
            }
        } catch (Exception e) {
            LOGGER.warn("从 CSL 获取皮肤失败: {}", e.getMessage());
        }

        return null;
    }

    private static ResourceLocation registerTextureDirectly(MinecraftProfileTexture texture, String username) {
        try {
            String hash = com.google.common.hash.Hashing.sha1()
                    .hashUnencodedChars(texture.getHash() != null ? texture.getHash() : username)
                    .toString();

            // 使用自己的 MODID
            ResourceLocation location = ResourceLocation.fromNamespaceAndPath(
                    MODID,
                    "skins/" + hash
            );

            TextureManager textureManager = Minecraft.getInstance().getTextureManager();
            Path skinRoot = Minecraft.getInstance().getResourcePackDirectory().getParent()
                    .resolve("assets").resolve("skins");

            Path skinPath = skinRoot.resolve(hash.length() > 2 ? hash.substring(0, 2) : "xx").resolve(hash);
            skinPath.toFile().getParentFile().mkdirs();

            HttpTexture httpTexture = new HttpTexture(
                    skinPath.toFile(),
                    texture.getUrl(),
                    DefaultPlayerSkin.getDefaultTexture(),
                    true,
                    null
            );

            textureManager.register(location, httpTexture);
            LOGGER.info("✅ 皮肤已注册: {}", location);
            return location;

        } catch (Exception e) {
            LOGGER.error("注册皮肤失败", e);
            return null;
        }
    }

    public static boolean isCslAvailable() {
        return cslLoaded;
    }
}
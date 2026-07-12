// client/skin/SkinLoader.java
package com.xiaoshi2022.kamenriderbossyouandme.client.skin;

import com.mojang.logging.LogUtils;
import com.xiaoshi2022.kamenriderbossyouandme.entity.FusionEffectEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.slf4j.Logger;

import java.util.UUID;
import java.util.function.Consumer;

/**
 * 皮肤加载器 - 处理异步皮肤加载
 * 从 corpseorigin 完整搬用
 */
@OnlyIn(Dist.CLIENT)
public class SkinLoader {
    private static final Logger LOGGER = LogUtils.getLogger();

    /**
     * 为融合实体异步加载皮肤（指定玩家索引）
     */
    public static void loadSkinAsync(FusionEffectEntity entity, String username, int playerIndex) {
        if (username == null || username.isEmpty()) {
            ResourceLocation defaultSkin = getDefaultSkin(username);
            entity.setPlayerSkin(playerIndex, defaultSkin);
            entity.setSkinState(playerIndex, SkinState.LOADED);  // ✅ 改为 setSkinState
            return;
        }

        ResourceLocation cached = SkinCache.get(username);
        if (cached != null) {
            entity.setPlayerSkin(playerIndex, cached);
            entity.setSkinState(playerIndex, SkinState.LOADED);  // ✅ 改为 setSkinState
            LOGGER.debug("✅ 从缓存加载皮肤: {} (索引: {})", username, playerIndex);
            return;
        }

        entity.setSkinState(playerIndex, SkinState.LOADING);  // ✅ 改为 setSkinState

        Thread skinThread = new Thread(() -> {
            try {
                LOGGER.info("🔍 开始加载皮肤: {} (索引: {})", username, playerIndex);

                ResourceLocation skin = null;
                if (SkinIntegration.isCslAvailable()) {
                    skin = SkinIntegration.getPlayerSkin(username);
                }

                if (skin == null) {
                    skin = getDefaultSkin(username);
                    LOGGER.info("⚠️ 使用默认皮肤: {} (索引: {})", username, playerIndex);
                }

                final ResourceLocation finalSkin = skin;

                Minecraft.getInstance().execute(() -> {
                    entity.setPlayerSkin(playerIndex, finalSkin);
                    entity.setSkinState(playerIndex, SkinState.LOADED);  // ✅ 改为 setSkinState
                    SkinCache.put(username, finalSkin);

                    if (finalSkin != getDefaultSkin(username)) {
                        LOGGER.info("✅ 皮肤加载成功: {} (索引: {})", username, playerIndex);
                    }
                });

            } catch (Exception e) {
                LOGGER.error("皮肤加载异常: {} (索引: {})", username, playerIndex, e);
                Minecraft.getInstance().execute(() -> {
                    ResourceLocation defaultSkin = getDefaultSkin(username);
                    entity.setPlayerSkin(playerIndex, defaultSkin);
                    entity.setSkinState(playerIndex, SkinState.FAILED);  // ✅ 改为 setSkinState
                    SkinCache.put(username, defaultSkin);
                });
            }
        });

        skinThread.setDaemon(true);
        skinThread.setName("SkinLoader-" + username + "-" + playerIndex);
        skinThread.start();
    }

    /**
     * 简单版本 - 只加载皮肤，不关联实体
     */
    public static void loadSkinAsync(String username, Consumer<ResourceLocation> callback) {
        if (username == null || username.isEmpty()) {
            callback.accept(DefaultPlayerSkin.getDefaultTexture());
            return;
        }

        ResourceLocation cached = SkinCache.get(username);
        if (cached != null) {
            callback.accept(cached);
            return;
        }

        Thread skinThread = new Thread(() -> {
            try {
                ResourceLocation skin = null;
                if (SkinIntegration.isCslAvailable()) {
                    skin = SkinIntegration.getPlayerSkin(username);
                }

                if (skin == null) {
                    skin = getDefaultSkin(username);
                }

                final ResourceLocation finalSkin = skin;
                Minecraft.getInstance().execute(() -> {
                    SkinCache.put(username, finalSkin);
                    callback.accept(finalSkin);
                });

            } catch (Exception e) {
                LOGGER.error("皮肤加载异常: {}", username, e);
                Minecraft.getInstance().execute(() ->
                        callback.accept(DefaultPlayerSkin.getDefaultTexture())
                );
            }
        });

        skinThread.setDaemon(true);
        skinThread.setName("SkinLoader-" + username);
        skinThread.start();
    }

    private static ResourceLocation getDefaultSkin(String username) {
        UUID uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + username).getBytes());
        return DefaultPlayerSkin.getDefaultTexture();
    }
}
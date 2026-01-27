package com.xiaoshi2022.kamen_rider_boss_you_and_me.client.renderer;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.TimeJackerEntity;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.TimeRoyaltyEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.VillagerRenderer;
import net.minecraftforge.fml.ModList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class MCARendererFactory {

    private static final Logger LOGGER = LogManager.getLogger(MCARendererFactory.class);
    private static final String MCA_MOD_ID = "mca";
    private static final String MCA_RENDERER_CLASS = "forge.net.mca.client.render.VillagerEntityMCARenderer";

    private static final boolean MCA_LOADED = ModList.get().isLoaded(MCA_MOD_ID);

    /**
     * 创建时劫者渲染器提供者
     */
    public static EntityRendererProvider<TimeJackerEntity> createTimeJackerRenderer() {
        return createRenderer();
    }

    /**
     * 创建时间王族渲染器提供者
     */
    public static EntityRendererProvider<TimeRoyaltyEntity> createTimeRoyaltyRenderer() {
        return createRenderer();
    }

    /**
     * 通用的渲染器创建方法
     */
    @SuppressWarnings("unchecked")
    private static <T extends net.minecraft.world.entity.npc.Villager> EntityRendererProvider<T> createRenderer() {
        return context -> {
            // 回退到原版村民渲染器，避免MCA的casting issues
            LOGGER.debug("Using vanilla VillagerRenderer to avoid MCA casting issues");
            return (net.minecraft.client.renderer.entity.EntityRenderer<T>) new VillagerRenderer(context);
        };
    }

    /**
     * 检查MCA是否可用
     */
    public static boolean isMCALoaded() {
        return MCA_LOADED;
    }
}
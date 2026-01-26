package com.xiaoshi2022.kamen_rider_boss_you_and_me.util;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;

/**
 * MCA软依赖工具类，用于处理与MinecraftComesAlive的软联动
 * 使用反射机制，避免硬依赖MCA
 */
public class MCAUtil {
    
    // MCA相关类的名称（使用正确的完整路径）
    private static final String VILLAGER_ENTITY_MCA = "forge.net.mca.entity.VillagerEntityMCA";
    private static final String VILLAGER_ENTITY_MCA_RENDERER = "forge.net.mca.client.render.VillagerEntityMCARenderer";
    private static final String ENTITIES_MCA = "forge.net.mca.entity.EntitiesMCA";
    
    // MCA相关类的缓存
    private static Class<?> villagerEntityMCAClass = null;
    private static Class<?> villagerEntityMCARendererClass = null; // 只在客户端使用
    private static Class<?> entitiesMCAClass = null;
    
    // 检查MCA是否已安装
    private static Boolean isMCAAvailable = null;
    
    /**
     * 检查MCA是否可用，仅检查服务器端可用的类
     */
    private static synchronized boolean checkMCAAvailability() {
        if (isMCAAvailable == null) {
            try {
                // 只检查服务器端可用的类，不检查客户端渲染类
                villagerEntityMCAClass = Class.forName(VILLAGER_ENTITY_MCA);
                entitiesMCAClass = Class.forName(ENTITIES_MCA);
                isMCAAvailable = true;
            } catch (ClassNotFoundException | NoClassDefFoundError e) {
                isMCAAvailable = false;
            }
        }
        return isMCAAvailable;
    }

    // 在MCAUtil.java中添加
    public static net.minecraft.client.renderer.entity.EntityRenderer<?> createMCARenderer(
            net.minecraft.client.renderer.entity.EntityRendererProvider.Context context) {
        if (!isMCAAvailable()) {
            return null;
        }

        try {
            // 使用反射创建MCA渲染器
            Class<?> mcaRendererClass = Class.forName("forge.net.mca.client.render.VillagerEntityMCARenderer");
            java.lang.reflect.Constructor<?> constructor = mcaRendererClass.getConstructor(
                    net.minecraft.client.renderer.entity.EntityRendererProvider.Context.class);
            return (net.minecraft.client.renderer.entity.EntityRenderer<?>) constructor.newInstance(context);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 检查MCA是否已安装
     */
    public static boolean isMCAAvailable() {
        return checkMCAAvailability();
    }
    
    /**
     * 检查实体是否是MCA村民
     */
    public static boolean isVillagerEntityMCA(Entity entity) {
        if (!isMCAAvailable()) {
            return false;
        }
        return villagerEntityMCAClass.isInstance(entity);
    }
    
    /**
     * 获取MCA村民渲染器工厂
     */
    @Nullable
    public static EntityRendererProvider<?> getVillagerEntityMCARendererFactory() {
        if (!isMCAAvailable()) {
            return null;
        }
        
        try {
            // 动态加载渲染器类
            Class<?> rendererClass = Class.forName(VILLAGER_ENTITY_MCA_RENDERER);
            // 使用反射创建渲染器实例
            return (EntityRendererProvider<?>) rendererClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 获取MCA村民的村民数据
     */
    @Nullable
    public static Object getVillagerData(Object villagerEntityMCA) {
        if (!isMCAAvailable() || !villagerEntityMCAClass.isInstance(villagerEntityMCA)) {
            return null;
        }
        
        try {
            Method method = villagerEntityMCAClass.getMethod("getVillagerData");
            return method.invoke(villagerEntityMCA);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 获取MCA村民的村民经验值
     */
    public static int getVillagerXp(Object villagerEntityMCA) {
        if (!isMCAAvailable() || !villagerEntityMCAClass.isInstance(villagerEntityMCA)) {
            return 0;
        }
        
        try {
            Method method = villagerEntityMCAClass.getMethod("getVillagerXp");
            return (int) method.invoke(villagerEntityMCA);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
    
    /**
     * 获取MCA村民的交易列表
     */
    @Nullable
    public static Object getOffers(Object villagerEntityMCA) {
        if (!isMCAAvailable() || !villagerEntityMCAClass.isInstance(villagerEntityMCA)) {
            return null;
        }
        
        try {
            Method method = villagerEntityMCAClass.getMethod("getOffers");
            return method.invoke(villagerEntityMCA);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 生成MCA村民
     */
    @Nullable
    public static Villager spawnMCAVillager(Object serverLevel, Object blockPos, Object mobSpawnType) {
        if (!isMCAAvailable()) {
            return null;
        }
        
        try {
            // 获取EntitiesMCA类的静态字段
            Field maleVillagerField = entitiesMCAClass.getField("MALE_VILLAGER");
            Object maleVillager = maleVillagerField.get(null);
            
            // 获取spawn方法并调用
            Method spawnMethod = maleVillager.getClass().getMethod("spawn", Object.class, Object.class, Object.class);
            Object result = spawnMethod.invoke(maleVillager, serverLevel, blockPos, mobSpawnType);
            
            if (result instanceof Villager villager) {
                return villager;
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 获取MCA村民实体类型
     */
    @Nullable
    public static Optional<? extends Object> getVillagerEntityMCAType() {
        if (!isMCAAvailable()) {
            return Optional.empty();
        }
        
        try {
            Field maleVillagerField = entitiesMCAClass.getField("MALE_VILLAGER");
            return Optional.ofNullable(maleVillagerField.get(null));
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }
    
    /**
     * 创建MCA村民实例
     */
    @Nullable
    public static Villager createMCAVillager(EntityType<? extends Villager> entityType, Level level) {
        if (!isMCAAvailable()) {
            return null;
        }
        
        try {
            // 获取MCA VillagerEntityMCA构造函数 - 尝试不同的参数类型
            Constructor<?>[] constructors = villagerEntityMCAClass.getDeclaredConstructors();
            
            // 遍历所有构造函数，找到合适的构造函数
            for (Constructor<?> constructor : constructors) {
                Class<?>[] paramTypes = constructor.getParameterTypes();
                
                // 查找参数数量为2的构造函数
                if (paramTypes.length == 2) {
                    constructor.setAccessible(true);
                    // 尝试调用构造函数，使用正确的参数类型
                    return (Villager) constructor.newInstance(entityType, level);
                }
            }
            
            // 如果没有找到合适的构造函数，返回null
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 创建MCA渲染器实例
     */
    @Nullable
    public static Object createMCARendererInstance(EntityRendererProvider.Context context) {
        if (!isMCAAvailable()) {
            return null;
        }
        
        try {
            // 动态加载渲染器类
            Class<?> rendererClass = Class.forName(VILLAGER_ENTITY_MCA_RENDERER);
            // 获取MCA渲染器构造函数
            Constructor<?> constructor = rendererClass.getConstructor(EntityRendererProvider.Context.class);
            return constructor.newInstance(context);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    /**
     * 获取MCA村民渲染器类
     */
    @Nullable
    public static Class<?> getVillagerEntityMCARendererClass() {
        if (!isMCAAvailable()) {
            return null;
        }
        
        try {
            // 动态加载渲染器类
            return Class.forName(VILLAGER_ENTITY_MCA_RENDERER);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取MCA村民实体类
     */
    @Nullable
    public static Class<?> getVillagerEntityMCAClass() {
        if (!isMCAAvailable) {
            return null;
        }
        return villagerEntityMCAClass;
    }
    
    /**
     * 从NBT标签中恢复MCA村民实体
     */
    public static void restoreMCAVillager(net.minecraft.server.level.ServerLevel serverLevel, net.minecraft.nbt.CompoundTag villagerTag, double x, double y, double z) {
        if (!isMCAAvailable()) {
            return;
        }
        
        try {
            // 获取实体类型
            net.minecraft.world.entity.EntityType<?> originalType = net.minecraft.world.entity.EntityType.VILLAGER;
            
            // 使用MCAUtil.createMCAVillager()方法创建MCA村民实例
            Villager restored = createMCAVillager((EntityType<? extends Villager>) originalType, serverLevel);
            if (restored == null) {
                // 如果创建失败，尝试备用方案
                return;
            }
            
            // 加载NBT数据
            restored.load(villagerTag);
            
            // 设置位置
            restored.setPos(x, y, z);
            
            // 设置最大生命值
            restored.setHealth(restored.getMaxHealth());
            
            // 添加到世界
            serverLevel.addFreshEntity(restored);
        } catch (Exception e) {
            e.printStackTrace();
            // 异常情况下，尝试使用普通村民恢复
            try {
                net.minecraft.world.entity.npc.Villager restored = (net.minecraft.world.entity.npc.Villager) net.minecraft.world.entity.EntityType.VILLAGER.create(serverLevel);
                if (restored != null) {
                    restored.load(villagerTag);
                    restored.setPos(x, y, z);
                    restored.setHealth(restored.getMaxHealth());
                    serverLevel.addFreshEntity(restored);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}

package com.xiaoshi2022.kamenriderbossyouandme.impl.geckolib;

import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animation.AnimationController;

import java.util.HashMap;
import java.util.Map;

/**
 * 简化版动画状态管理器
 * 只做一件事：管理动画状态切换
 */
public class AnimationManager<T extends GeoAnimatable> {
    private final T animatable;
    private final Map<String, AnimationController<T>> controllers = new HashMap<>();
    private String currentState = "idle";

    public AnimationManager(T animatable) {
        this.animatable = animatable;
    }

    /**
     * 注册控制器
     */
    public void registerController(String name, AnimationController<T> controller) {
        controllers.put(name, controller);
    }

    /**
     * 切换状态
     */
    public void setState(String stateName) {
        if (!currentState.equals(stateName)) {
            currentState = stateName;
            // 重置所有控制器
            controllers.values().forEach(AnimationController::forceAnimationReset);
        }
    }

    /**
     * 获取当前状态
     */
    public String getCurrentState() {
        return currentState;
    }

    /**
     * 获取指定控制器
     */
    public AnimationController<T> getController(String name) {
        return controllers.get(name);
    }

    public T getAnimatable() {
        return animatable;
    }
}
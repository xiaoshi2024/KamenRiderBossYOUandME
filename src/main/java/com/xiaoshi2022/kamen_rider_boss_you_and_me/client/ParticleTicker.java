package com.xiaoshi2022.kamen_rider_boss_you_and_me.client;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

import java.util.concurrent.ConcurrentHashMap;

public class ParticleTicker {
    private static final ConcurrentHashMap<ServerLevel, java.util.List<TickTask>> tasks = new ConcurrentHashMap<>();

    public record TickTask(Vec3 center, int left) {}

    public static void add(ServerLevel level, Vec3 center) {
        tasks.computeIfAbsent(level, k -> new java.util.ArrayList<>())
                .add(new TickTask(center, 60)); // 60 tick
    }

    public static void tick(ServerLevel level) {
        java.util.List<TickTask> list = tasks.get(level);
        if (list == null) return;
        list.removeIf(task -> {
            long now = level.getGameTime();
            double angle = (now * 0.8) * Math.PI / 180.0;
            for (int i = 0; i < 8; i++) {
                double a = angle + i * Math.PI / 4.0;
                double x = task.center().x + Math.cos(a) * 0.25;
                double y = task.center().y + Math.sin(a * 0.5) * 0.1;
                double z = task.center().z + Math.sin(a) * 0.25;
                level.sendParticles(ParticleTypes.ENTITY_EFFECT,
                        x, y, z, 1, 0.2D, 0.0D, 0.4D, 1);
            }
            return task.left() <= 1; // 减 1 并判断是否结束
        });
        list.replaceAll(task -> new TickTask(task.center(), task.left() - 1));
    }
}
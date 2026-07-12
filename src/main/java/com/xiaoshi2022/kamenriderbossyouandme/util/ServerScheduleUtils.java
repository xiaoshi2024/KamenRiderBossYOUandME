package com.xiaoshi2022.kamenriderbossyouandme.util;

import com.xiaoshi2022.kamenriderbossyouandme.KamenRiderBossYOUandME;
import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

@EventBusSubscriber(modid = KamenRiderBossYOUandME.MODID)
public class ServerScheduleUtils {

    private static final List<TaskEntry> tasks = new ArrayList<>();
    private static final int MAX_TASKS = 200;

    private static class TaskEntry {
        int remainingTicks;
        final Runnable task;

        TaskEntry(int remainingTicks, Runnable task) {
            this.remainingTicks = remainingTicks;
            this.task = task;
        }
    }

    public static void scheduleTicks(int ticks, Runnable task) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return;
        }
        synchronized (tasks) {
            if (tasks.size() >= MAX_TASKS) {
                KamenRiderBossYOUandME.LOGGER.warn("ServerScheduleUtils 任务队列已满，拒绝新任务! 当前任务数: {}", tasks.size());
                return;
            }
            tasks.add(new TaskEntry(ticks, task));
        }
    }

    public static void scheduleSeconds(float seconds, Runnable task) {
        scheduleTicks((int) (seconds * 20.0F), task);
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        Deque<Runnable> toRun = new ArrayDeque<>();

        synchronized (tasks) {
            Iterator<TaskEntry> iterator = tasks.iterator();
            while (iterator.hasNext()) {
                TaskEntry entry = iterator.next();
                entry.remainingTicks--;
                if (entry.remainingTicks <= 0) {
                    toRun.add(entry.task);
                    iterator.remove();
                }
            }
        }

        Runnable task;
        while ((task = toRun.poll()) != null) {
            try {
                task.run();
            } catch (Exception e) {
                KamenRiderBossYOUandME.LOGGER.error("ServerScheduleUtils 任务出错", e);
            }
        }
    }
}

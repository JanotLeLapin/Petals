package io.github.petals.structures;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import io.github.petals.Game;

public class PetalsScheduler implements Game.Scheduler {
    private final Map<Long, PetalsTask> tasks = new HashMap<>();

    public static class PetalsTask implements Runnable {
        private Thread t;

        private final AtomicBoolean running = new AtomicBoolean(false);
        private final Runnable runnable;

        private final long delay;
        private final long interval;
        private final Plugin plugin;
        private final Game.Scheduler scheduler;

        public PetalsTask(Plugin plugin, Game.Scheduler scheduler, long delay, long interval, Runnable runnable) {
            this.plugin = plugin;
            this.scheduler = scheduler;
            this.delay = delay;
            this.interval = interval;
            this.runnable = runnable;
        }

        public long start() {
            this.t = new Thread(this);
            this.t.start();

            return this.t.getId();
        }

        public void cancel() {
            this.running.set(false);
        }

        @Override
        public void run() {
            running.set(true);

            try {
                Thread.sleep(this.delay);
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }

            do {
                Bukkit.getScheduler().runTask(this.plugin, this.runnable);
                try {
                    Thread.sleep(this.interval);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Thread.currentThread().interrupt();
                }
            } while (this.running.get() && this.interval > 0);

            this.scheduler.cancel(this.t.getId());
        }
    }

    @Override
    public long runTaskTimer(Plugin plugin, long delay, long period, Runnable runnable) {
        PetalsTask task = new PetalsTask(plugin, this, delay, period, runnable);
        long id = task.start();
        this.tasks.put(id, task);

        return id;
    }

    @Override
    public long runTaskLater(Plugin plugin, long delay, Runnable runnable) {
        return this.runTaskTimer(plugin, delay, 0, runnable);
    }

    @Override
    public void cancel(long taskId) {
        PetalsTask t = this.tasks.get(taskId);
        if (t != null) {
            t.cancel();
            this.tasks.remove(taskId);
        }
    }

    @Override
    public void clear() {
        tasks.forEach((id, task) -> task.cancel());
        tasks.clear();
    }
}


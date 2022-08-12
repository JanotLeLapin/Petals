package io.github.petals.structures;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import io.github.petals.Game;
import redis.clients.jedis.JedisPooled;

public class PetalsScheduler implements Game.Scheduler {
    private Game game;
    private JedisPooled pooled;

    public PetalsScheduler(String gameId, JedisPooled pooled) {
        this.game = new PetalsGame(gameId, pooled);
        this.pooled = pooled;
    }

    private void removeTask(int task) {
        pooled.srem(game.uniqueId() + ":tasks", String.valueOf(task));
    }

    private void addTask(int task) {
        pooled.sadd(game.uniqueId() + ":tasks", String.valueOf(task));
    }

    private BukkitRunnable createBukkitRunnable(Runnable runnable) {
        return new BukkitRunnable() {
            @Override
            public void run() {
                removeTask(this.getTaskId());
                runnable.run();
            }
        };
    }

    @Override
    public BukkitTask runTaskLater(long delay, Runnable runnable) {
        BukkitTask task = createBukkitRunnable(runnable).runTaskLater(this.game.plugin(), delay);
        addTask(task.getTaskId());

        return task;
    }

    @Override
    public BukkitTask runTaskTimer(long delay, long period, Runnable runnable) {
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(game.plugin(), runnable, delay, period);
        addTask(task.getTaskId());

        return task;
    }

    @Override
    public void cancel(int taskId) {
        Bukkit.getScheduler().cancelTask(taskId);
        this.removeTask(taskId);
    }

    @Override
    public void clear() {
        pooled
            .smembers(game.uniqueId() + ":tasks")
            .stream()
            .forEach(id -> Bukkit.getScheduler().cancelTask(Integer.parseInt(id)));

        pooled.del(game.uniqueId() + ":tasks");
    }
}


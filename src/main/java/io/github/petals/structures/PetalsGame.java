package io.github.petals.structures;

import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;

import io.github.petals.Game;
import io.github.petals.Petal;
import redis.clients.jedis.JedisPooled;

public class PetalsGame implements Game {
    private String uniqueId;
    private JedisPooled pooled;

    public PetalsGame(String uniqueId, JedisPooled pooled) {
        this.uniqueId = uniqueId;
        this.pooled = pooled;
    }

    @Override
    public String uniqueId() {
        return this.uniqueId;
    }

    @Override
    public boolean exists() {
        return this.uniqueId == null ? false : pooled.sismember("games", this.uniqueId);
    }

    @Override
    public boolean running() {
        return !pooled.hget(this.uniqueId, "start").equals("-1");
    }

    @Override
    public long ticks() {
        return running()
            ? Bukkit.getWorlds().get(0).getFullTime() - Long.parseLong(pooled.hget(this.uniqueId, "start"))
            : 0;
    }

    @Override
    public Petal plugin() {
        String pluginName = pooled.hget(this.uniqueId, "plugin");
        return (Petal) Bukkit.getPluginManager().getPlugin(pluginName);
    }

    @Override
    public void delete() {
        // Delete each player related to the game
        this.players().forEach(player -> player.delete());
        this.worlds().forEach(world -> world.delete());

        // Delete the game
        pooled.srem("games", this.uniqueId);
        pooled.del(this.uniqueId + ":players");
        pooled.del(this.uniqueId);
    }

    @Override
    public Player host() {
        return new PetalsPlayer(pooled.hget(this.uniqueId, "host"), pooled);
    }

    @Override
    public Set<Player> players() {
        Set<String> playerIds = pooled.smembers(this.uniqueId + ":players");
        return playerIds.stream().map(id -> new PetalsPlayer(id, pooled)).collect(Collectors.toSet());
    }

    @Override
    public World home() {
        return new PetalsWorld(pooled.hget(this.uniqueId, "home"), pooled);
    }

    @Override
    public Set<World> worlds() {
        Set<String> worldNames = pooled.smembers(this.uniqueId + ":worlds");
        return worldNames.stream().map(name -> new PetalsWorld(name, pooled)).collect(Collectors.toSet());
    }
}


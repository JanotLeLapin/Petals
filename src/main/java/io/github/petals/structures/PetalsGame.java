package io.github.petals.structures;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;

import io.github.petals.Game;
import io.github.petals.Petal;
import redis.clients.jedis.JedisPooled;

public class PetalsGame implements Game {
    private UUID uniqueId;
    private JedisPooled pooled;

    public PetalsGame(UUID uniqueId, JedisPooled pooled) {
        this.uniqueId = uniqueId;
        this.pooled = pooled;
    }

    @Override
    public UUID uniqueId() {
        return this.uniqueId;
    }

    @Override
    public boolean exists() {
        return pooled.sismember("games", this.uniqueId.toString());
    }

    @Override
    public boolean running() {
        return !pooled.hget(this.uniqueId.toString(), "start").equals("-1");
    }

    @Override
    public long ticks() {
        return running()
            ? Bukkit.getWorlds().get(0).getFullTime() - Long.parseLong(pooled.hget(this.uniqueId.toString(), "start"))
            : 0;
    }

    @Override
    public Petal plugin() {
        String pluginName = pooled.hget(this.uniqueId.toString(), "plugin");
        return (Petal) Bukkit.getPluginManager().getPlugin(pluginName);
    }

    @Override
    public void delete() {
        // Delete each player related to the game
        this.players().forEach(player -> player.delete());

        // Delete the game
        pooled.srem("games", this.uniqueId.toString());
        pooled.del(this.uniqueId.toString() + ":players");
        pooled.del(this.uniqueId.toString());
    }

    @Override
    public Player host() {
        UUID hostId = UUID.fromString(pooled.hget(this.uniqueId.toString(), "host"));
        return new PetalsPlayer(hostId, pooled);
    }

    @Override
    public Set<Player> players() {
        Set<String> playerIds = pooled.smembers(this.uniqueId.toString() + ":players");
        return playerIds.stream().map(id -> new PetalsPlayer(UUID.fromString(id), pooled)).collect(Collectors.toSet());
    }
}


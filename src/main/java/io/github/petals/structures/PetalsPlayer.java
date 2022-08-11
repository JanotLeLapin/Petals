package io.github.petals.structures;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import io.github.petals.Game;
import redis.clients.jedis.JedisPooled;

public class PetalsPlayer implements Game.Player {
    private String uniqueId;
    private JedisPooled pooled;

    public PetalsPlayer(String uniqueId, JedisPooled pooled) {
        this.uniqueId = uniqueId;
        this.pooled = pooled;
    }

    @Override
    public String uniqueId() {
        return this.uniqueId;
    }

    @Override
    public boolean exists() {
        return this.uniqueId == null ? false : this.pooled.sismember("players", this.uniqueId);
    }

    @Override
    public Game game() {
        return new PetalsGame(this.pooled.hget(this.uniqueId, "game"), pooled);
    }

    @Override
    public OfflinePlayer player() {
        return Bukkit.getOfflinePlayer(UUID.fromString(this.uniqueId));
    }

    @Override
    public void delete() {
        this.pooled.srem("players", this.uniqueId);
        this.pooled.del(this.uniqueId);
    }
}


package io.github.petals.structures;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import io.github.petals.Game;
import redis.clients.jedis.JedisPooled;

public class PetalsPlayer implements Game.Player {
    private UUID uniqueId;
    private JedisPooled pooled;

    public PetalsPlayer(UUID uniqueId, JedisPooled pooled) {
        this.uniqueId = uniqueId;
        this.pooled = pooled;
    }

    @Override
    public UUID uniqueId() {
        return this.uniqueId;
    }

    @Override
    public boolean exists() {
        return this.pooled.sismember("players", this.uniqueId.toString());
    }

    @Override
    public Game game() {
        UUID gameId = UUID.fromString(this.pooled.hget(this.uniqueId.toString(), "game"));
        return new PetalsGame(gameId, pooled);
    }

    @Override
    public OfflinePlayer player() {
        return Bukkit.getOfflinePlayer(this.uniqueId);
    }
}


package io.github.petals.structures;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import io.github.petals.Game;
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
        // TODO: Use world time & start time to calculate ticks
        return 0;
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


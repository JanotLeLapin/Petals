package io.github.petals;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import io.github.petals.structures.PetalsGame;
import redis.clients.jedis.JedisPooled;

public class PetalsPlugin extends Petals {
    private JedisPooled pooled;

    @Override
    public void onEnable() {
        this.saveDefaultConfig();

        // Connect to database
        final String host = this.getConfig().getString("redis.host", "127.0.0.1");
        final short port = (short) this.getConfig().getInt("redis.port", 6379);
        this.pooled = new JedisPooled(host, port);
    }

    @Override
    public Set<Game> games() {
        Set<String> gameIds = pooled.smembers("games");
        return gameIds.stream().map(id -> new PetalsGame(UUID.fromString(id), pooled)).collect(Collectors.toSet());
    }
}


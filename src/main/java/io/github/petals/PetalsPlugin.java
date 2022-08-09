package io.github.petals;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import io.github.petals.structures.PetalsGame;
import redis.clients.jedis.JedisPooled;

public class PetalsPlugin extends Petals {
    // TODO: Initialize this
    private JedisPooled pooled;

    @Override
    public void onEnable() {
        this.getLogger().info("Hello, World!");
    }

    @Override
    public Set<Game> games() {
        Set<String> gameIds = pooled.smembers("games");
        return gameIds.stream().map(id -> new PetalsGame(UUID.fromString(id), pooled)).collect(Collectors.toSet());
    }
}


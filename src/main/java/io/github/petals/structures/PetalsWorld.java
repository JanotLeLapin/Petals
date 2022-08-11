package io.github.petals.structures;

import java.util.UUID;

import org.bukkit.Bukkit;

import io.github.petals.Game;
import io.github.petals.Game.World;
import redis.clients.jedis.JedisPooled;

public class PetalsWorld implements World {
    private String name;
    private JedisPooled pooled;

    public PetalsWorld(String name, JedisPooled pooled) {
        this.name = name;
        this.pooled = pooled;
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public boolean exists() {
        return pooled.hget("worlds", this.name) != null;
    }

    @Override
    public Game game() {
        return new PetalsGame(pooled.hget("worlds", this.name), this.pooled);
    }

    @Override
    public org.bukkit.World world() {
        return Bukkit.getWorld(this.name);
    }

    @Override
    public void delete() {
        pooled.hdel("worlds", this.name);
    }
}


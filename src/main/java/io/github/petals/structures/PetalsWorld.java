package io.github.petals.structures;

import org.bukkit.Bukkit;

import io.github.petals.Game;
import io.github.petals.Game.World;
import redis.clients.jedis.JedisPooled;

public class PetalsWorld extends PetalsBase implements World {
    private final JedisPooled pooled;

    public PetalsWorld(final String name, final JedisPooled pooled) {
        super(name);
        this.pooled = pooled;
    }

    @Override
    public String name() {
        return this.uniqueId;
    }

    @Override
    public boolean exists() {
        return this.uniqueId == null ? false : pooled.hget("worlds", this.uniqueId) != null;
    }

    @Override
    public Game<?> game() {
        return new PetalsGame<>(pooled.hget("worlds", this.uniqueId), this.pooled);
    }

    @Override
    public org.bukkit.World world() {
        return Bukkit.getWorld(this.uniqueId);
    }

    @Override
    public void delete() {
        pooled.hdel("worlds", this.uniqueId);
    }
}


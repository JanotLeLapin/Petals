package io.github.petals.structures;

import java.util.Optional;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import io.github.petals.Game;
import io.github.petals.Metadata;
import io.github.petals.Util;
import io.github.petals.state.PetalsState;
import io.github.petals.state.State;
import redis.clients.jedis.JedisPooled;

public class PetalsPlayer<T extends State<?>> extends PetalsBase implements Game.Player<T> {
    private final JedisPooled pooled;

    public PetalsPlayer(final String uniqueId, JedisPooled pooled) {
        super(uniqueId);
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
    public Game<?> game() {
        return new PetalsGame<>(this.pooled.hget(this.uniqueId, "game"), pooled);
    }

    @Override
    public Optional<Player> player() {
        Player p = Bukkit.getPlayer(UUID.fromString(this.uniqueId));
        return p == null ? Optional.empty() : Optional.of(p);
    }

    @Override
    public T state() {
        return (T) Util.createState(this, pooled);
    }

    @Override
    public <U extends State<?>> U state(Class<U> state) {
        this.pooled.hset(this.uniqueId, "state", state.getName());
        return (U) this.state();
    }

    @Override
    public void delete() {
        this.game().plugin().onRemovePlayer((State<Game.Player<?>>) this.state());

        State<?> state = this.state();
        if (state != null) state.raw().clear();

        this.pooled.srem("players", this.uniqueId);
        this.pooled.del(this.uniqueId);
    }
}


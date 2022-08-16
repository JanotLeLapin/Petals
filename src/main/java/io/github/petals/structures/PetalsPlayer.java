package io.github.petals.structures;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import io.github.petals.Game;
import io.github.petals.Metadata;
import io.github.petals.role.Role;
import redis.clients.jedis.JedisPooled;

public class PetalsPlayer<T extends Role> implements Game.Player<T> {
    private String uniqueId;
    private T role = null;
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
    public Optional<Player> player() {
        Player p = Bukkit.getPlayer(UUID.fromString(this.uniqueId));
        return p == null ? Optional.empty() : Optional.of(p);
    }

    @Override
    public Map<String, String> meta() {
        return new Metadata(this.uniqueId + ":meta", this.pooled);
    }

    @Override
    public T role() {
        if (this.role != null) return this.role;

        // Cache role
        String roleClass = pooled.hget(uniqueId, "role");
        if (roleClass != null) {
            try {
                Class<?> role = Class.forName(pooled.hget(uniqueId, "role"));
                if (!Role.class.isAssignableFrom(role)) throw new ClassCastException(String.format("%s does not implement Role", role.getName()));

                this.role = (T) role.getConstructor(Game.Player.class).newInstance(this);
            } catch (ClassNotFoundException | ClassCastException | InvocationTargetException | IllegalAccessException | InstantiationException | NoSuchMethodException e) {
                e.printStackTrace();
            }
        }

        return this.role;
    }

    @Override
    public void role(Class<? extends Role> role) {
        this.pooled.hset(this.uniqueId, "role", role.getName());
    }

    @Override
    public void delete() {
        this.game().plugin().onRemovePlayer((Game.Player<Role>) this);

        this.meta().clear();

        this.pooled.srem("players", this.uniqueId);
        this.pooled.del(this.uniqueId);
    }
}


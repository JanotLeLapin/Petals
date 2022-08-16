package io.github.petals.structures;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;

import io.github.petals.Game;
import io.github.petals.Petal;
import io.github.petals.PetalsPlugin;
import io.github.petals.role.Role;
import redis.clients.jedis.JedisPooled;

public class PetalsGame implements Game {
    private String uniqueId;
    private JedisPooled pooled;

    public PetalsGame(String uniqueId, JedisPooled pooled) {
        this.uniqueId = uniqueId;
        this.pooled = pooled;
    }

    @Override
    public String uniqueId() {
        return this.uniqueId;
    }

    @Override
    public boolean exists() {
        return this.uniqueId == null ? false : pooled.sismember("games", this.uniqueId);
    }

    @Override
    public boolean running() {
        return !pooled.hget(this.uniqueId, "start").equals("-1");
    }

    @Override
    public long ticks() {
        return running()
            ? Bukkit.getWorlds().get(0).getFullTime() - Long.parseLong(pooled.hget(this.uniqueId, "start"))
            : -1;
    }

    @Override
    public Petal plugin() {
        String pluginName = pooled.hget(this.uniqueId, "plugin");
        return (Petal) Bukkit.getPluginManager().getPlugin(pluginName);
    }

    @Override
    public Scheduler scheduler() {
        return new PetalsScheduler(this.uniqueId, pooled);
    }

    @Override
    public void delete() {
        // Delete players
        this.players().forEach(player -> player.delete());
        // Delete worlds
        this.worlds().forEach(world -> world.delete());

        // Delete tasks
        this.scheduler().clear();

        // Delete the game
        pooled.srem("games", this.uniqueId);
        pooled.del(this.uniqueId + ":players");
        pooled.del(this.uniqueId);
    }

    @Override
    public Player<Role> host() {
        return new PetalsPlayer<Role>(pooled.hget(this.uniqueId, "host"), pooled);
    }

    @Override
    public Set<Player<Role>> players() {
        Set<String> playerIds = pooled.smembers(this.uniqueId + ":players");
        return playerIds.stream().map(id -> this.player(id).get()).collect(Collectors.toSet());
    }

    @Override
    public Optional<Player<Role>> player(String uniqueId) {
        PetalsPlayer<Role> p = new PetalsPlayer<>(uniqueId, pooled);
        return p.exists() && p.game().uniqueId().equals(this.uniqueId) ? Optional.of(p) : Optional.empty();
    }

    @Override
    public <T extends Role> Set<Player<T>> players(Class<T> role) {
        return pooled
            .smembers(this.uniqueId + ":players")
            .stream()
            .filter(player -> {
                String r = pooled.hget(player, "role");
                return r == null ? false : r.equals(role.getName());
            })
            .map(id -> new PetalsPlayer<T>(id, pooled))
            .collect(Collectors.toSet());
    }

    @Override
    public <T extends Role> Optional<Player<T>> player(String uniqueId, Class<T> role) {
        PetalsPlayer<T> p = new PetalsPlayer<>(uniqueId, pooled);
        return p.exists() && p.game().uniqueId().equals(this.uniqueId) ? Optional.of(p) : Optional.empty();
    }

    @Override
    public Player<Role> addPlayer(String uniqueId) throws IllegalStateException {
        Player<Role> p = PetalsPlugin.petals().database().createPlayer(uniqueId, this.uniqueId);
        this.plugin().onAddPlayer(p);
        return p;
    }

    @Override
    public Set<World> worlds() {
        Set<String> worldNames = pooled.smembers(this.uniqueId + ":worlds");
        return worldNames.stream().map(name -> this.world(name)).collect(Collectors.toSet());
    }

    @Override
    public World world(String name) {
        return new PetalsWorld(name, pooled);
    }

    @Override
    public World addWorld(String name) throws IllegalStateException {
        return PetalsPlugin.petals().database().createWorld(name, this.uniqueId);
    }

    @Override
    public void start() throws IllegalStateException {
        if (this.running()) throw new IllegalStateException(String.format("Game %s already running", this.uniqueId()));
        pooled.hset(this.uniqueId(), "start", String.valueOf(Bukkit.getWorlds().get(0).getFullTime()));
        this.plugin().onStartGame(this);
    }

    @Override
    public void stop() throws IllegalStateException {
        if (!this.running()) throw new IllegalStateException(String.format("Game %s is not running", this.uniqueId()));
        this.plugin().onStopGame(this);
        this.delete();
    }
}


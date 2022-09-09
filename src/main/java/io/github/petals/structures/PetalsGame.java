package io.github.petals.structures;

import java.util.Date;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import io.github.petals.Game;
import io.github.petals.Petal;
import io.github.petals.PetalsPlugin;
import io.github.petals.Util;
import io.github.petals.state.State;
import redis.clients.jedis.JedisPooled;

public class PetalsGame<T extends State<?>> extends PetalsBase implements Game<T> {
    private final JedisPooled pooled;

    public PetalsGame(final String uniqueId, final JedisPooled pooled) {
        super(uniqueId);
        this.pooled = pooled;
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
    public long time() {
        return running()
            ? new Date().getTime() - Long.parseLong(pooled.hget(this.uniqueId, "start"))
            : -1;
    }

    @Override
    public Petal plugin() {
        String pluginName = pooled.hget(this.uniqueId, "plugin");
        return (Petal) org.bukkit.Bukkit.getPluginManager().getPlugin(pluginName);
    }

    @Override
    public Scheduler scheduler() {
        return new PetalsScheduler();
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
        try {
            plugin().onDeleteGame((State<Game<?>>) this.state());
        } catch (Throwable e) {
            e.printStackTrace();
        }

        State<?> state = this.state();
        if (state != null) state.raw().clear();

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
    public Player<?> host() {
        return new PetalsPlayer<State<?>>(pooled.hget(this.uniqueId, "host"), pooled);
    }

    @Override
    public Set<Player<?>> players() {
        Set<String> playerIds = pooled.smembers(this.uniqueId + ":players");
        return playerIds.stream().map(id -> this.player(id).get()).collect(Collectors.toSet());
    }

    @Override
    public <U extends State<?>> Set<Player<U>> players(Class<U> state) {
        return pooled
            .smembers(this.uniqueId + ":players")
            .stream()
            .filter(player -> Util.isStateAssignable(player, state, pooled))
            .map(id -> new PetalsPlayer<U>(id, pooled))
            .collect(Collectors.toSet());
    }

    @Override
    public Optional<Player<State<?>>> player(String uniqueId) {
        PetalsPlayer<State<?>> p = new PetalsPlayer<>(uniqueId, pooled);
        return p.exists() && p.game().uniqueId().equals(this.uniqueId) ? Optional.of(p) : Optional.empty();
    }

    @Override
    public Optional<Player<State<?>>> player(org.bukkit.entity.Player player) {
        return this.player(player.getUniqueId().toString());
    }

    @Override
    public <U extends State<?>> Optional<Player<U>> player(String uniqueId, Class<U> role) {
        PetalsPlayer<U> p = new PetalsPlayer<>(uniqueId, pooled);
        return
            p.exists()
            && Util.isStateAssignable(uniqueId, role, pooled)
            && p.game().uniqueId().equals(this.uniqueId) ? Optional.of(p) : Optional.empty();
    }

    @Override
    public <U extends State<?>> Optional<Player<U>> player(org.bukkit.entity.Player player, Class<U> role) {
        return this.player(player.getUniqueId().toString(), role);
    }

    @Override
    public Player<State<?>> addPlayer(String uniqueId) throws IllegalStateException {
        Player<State<?>> p = PetalsPlugin.petals().database().createPlayer(uniqueId, this.uniqueId, plugin());
        try {
            this.plugin().onAddPlayer((State<Player<?>>) p.state());
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return p;
    }

    @Override
    public Player<State<?>> addPlayer(org.bukkit.entity.Player player) throws IllegalStateException {
        return this.addPlayer(player.getUniqueId().toString());
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
    public void start() {
        try {
            this.plugin().onStartGame((State<Game<?>>) this.state());
        } catch (Throwable e) {
            e.printStackTrace();
        }
        pooled.hset(this.uniqueId(), "start", String.valueOf(new Date().getTime()));
    }
}


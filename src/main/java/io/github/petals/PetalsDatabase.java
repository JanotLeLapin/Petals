package io.github.petals;

import java.util.HashMap;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import io.github.petals.Game.Player;
import io.github.petals.Game.World;
import io.github.petals.role.Role;
import io.github.petals.structures.PetalsGame;
import io.github.petals.structures.PetalsPlayer;
import io.github.petals.structures.PetalsWorld;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.exceptions.JedisConnectionException;

public class PetalsDatabase implements Database {
    private JedisPooled pooled;

    public PetalsDatabase(String host, int port) {
        this.pooled = new JedisPooled(host, port);
    }

    @Override
    public boolean ping() {
        try {
            this.pooled.get("ping");
        } catch (JedisConnectionException e) {
            return false;
        }
        return true;
    }

    @Override
    public Set<Game> games() {
        Set<String> gameIds = pooled.smembers("games");
        return gameIds.stream().map(id -> new PetalsGame(id, pooled)).collect(Collectors.toSet());
    }

    @Override
    public Optional<Player<Role>> player(String uniqueId) {
        PetalsPlayer<Role> p = new PetalsPlayer<>(uniqueId, pooled);
        return p.exists() ? Optional.of(p) : Optional.empty();
    }

    @Override
    public Optional<Player<Role>> player(org.bukkit.entity.Player player) {
        PetalsPlayer<Role> p = new PetalsPlayer<>(player.getUniqueId().toString(), pooled);
        return p.exists() ? Optional.of(p) : Optional.empty();
    }

    @Override
    public <T extends Role> Optional<Player<T>> player(String uniqueId, Class<T> role) {
        PetalsPlayer<T> p = new PetalsPlayer<>(uniqueId, pooled);
        return p.exists() ? Optional.of(p) : Optional.empty();
    }

    @Override
    public Optional<World> world(String name) {
        World w = new PetalsWorld(name, pooled);
        return w.exists() ? Optional.of(w) : Optional.empty();
    }

    @Override
    public Optional<World> world(org.bukkit.World world) {
        World w = new PetalsWorld(world.getName(), pooled);
        return w.exists() ? Optional.of(w) : Optional.empty();
    }

    @Override
    public Game createGame(String host, Petal plugin) throws IllegalStateException {
        String uniqueId = UUID.randomUUID().toString();

        // Add player
        Player<Role> p = createPlayer(host, uniqueId);
        plugin.onAddPlayer(p);

        // Create game
        HashMap<String, String> map = new HashMap<>();
        map.put("start", "-1");
        map.put("host", host);
        map.put("plugin", plugin.getName());
        pooled.hset(uniqueId, map);
        pooled.sadd("games", uniqueId);

        Game game = new PetalsGame(uniqueId, pooled);
        plugin.onCreateGame(game);
        return game;
    }

    public World createWorld(String name, String game) throws IllegalStateException {
        if (pooled.sismember("worlds", name)) {
            throw new IllegalStateException(String.format("World with ID: \"%s\" already present", name));
        }

        pooled.hset("worlds", name, game);
        pooled.sadd(game + ":worlds", name);

        return new PetalsWorld(name, pooled);
    }

    public Player<Role> createPlayer(String player, String game) throws IllegalStateException {
        if (pooled.sismember("players", player)) {
            throw new IllegalStateException(String.format("Player with ID: \"%s\" already present", player));
        };

        HashMap<String, String> map = new HashMap<>();
        map.put("game", game);
        pooled.hset(player, map);

        pooled.sadd(game + ":players", player);
        pooled.sadd("players", player);

        Player<Role> p = new PetalsPlayer<>(player, pooled);
        return p;
    }
}


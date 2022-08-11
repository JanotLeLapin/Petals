package io.github.petals;

import java.util.HashMap;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import io.github.petals.Game.Player;
import io.github.petals.Game.World;
import io.github.petals.structures.PetalsGame;
import io.github.petals.structures.PetalsPlayer;
import io.github.petals.structures.PetalsWorld;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.exceptions.JedisConnectionException;

public class PetalsPlugin extends JavaPlugin implements Petals {
    private JedisPooled pooled;

    static PetalsPlugin petals() {
        return (PetalsPlugin) Bukkit.getPluginManager().getPlugin("Petals");
    }

    @Override
    public void onEnable() {
        this.saveDefaultConfig();

        // Connect to database
        final String host = this.getConfig().getString("redis.host", "127.0.0.1");
        final short port = (short) this.getConfig().getInt("redis.port", 6379);
        this.pooled = new JedisPooled(host, port);
        try {
            this.pooled.get("ping");
        } catch (JedisConnectionException e) {
            this.getLogger().info("Could not reach database");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        this.getLogger().info("Connected to database");

        // Register command
        PluginCommand pluginCmd = this.getCommand("petals");
        PetalsCommand petalsCmd = new PetalsCommand();
        pluginCmd.setExecutor(petalsCmd);
        pluginCmd.setTabCompleter(petalsCmd);
        this.getLogger().info("Registered command");

        this.getLogger().info("Enabled Petals");
    }

    @Override
    public void onDisable() {
        try {
            this.games().forEach(game -> game.delete());
        } catch (JedisConnectionException e) {}
    }

    @Override
    public Set<Game> games() {
        Set<String> gameIds = pooled.smembers("games");
        return gameIds.stream().map(id -> new PetalsGame(id, pooled)).collect(Collectors.toSet());
    }

    @Override
    public Set<Player> players() {
        Set<String> playerIds = pooled.smembers("players");
        return playerIds.stream().map(id -> new PetalsPlayer(id, pooled)).collect(Collectors.toSet());
    }

    @Override
    public Player player(String uniqueId) {
        return new PetalsPlayer(uniqueId, pooled);
    }

    public Game createGame(String host, String home, String plugin) {
        String uniqueId = UUID.randomUUID().toString();

        createPlayer(host, uniqueId);
        createWorld(home, uniqueId);

        // Create game
        HashMap<String, String> map = new HashMap<>();
        map.put("start", "-1");
        map.put("host", host);
        map.put("home", home);
        map.put("plugin", plugin);
        pooled.hset(uniqueId, map);

        pooled.sadd("games", uniqueId);

        return new PetalsGame(uniqueId, pooled);
    }

    public World createWorld(String name, String game) {
        pooled.hset("worlds", name, game);
        pooled.sadd(game + ":worlds", name);

        return new PetalsWorld(name, pooled);
    }

    public Player createPlayer(String player, String game) {
        HashMap<String, String> map = new HashMap<>();
        map.put("game", game);
        pooled.hset(player, map);

        pooled.sadd(game + ":players", player);
        pooled.sadd("players", player);

        return new PetalsPlayer(player, pooled);
    }
}


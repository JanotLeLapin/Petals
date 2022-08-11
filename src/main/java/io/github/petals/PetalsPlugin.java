package io.github.petals;

import java.util.HashMap;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;

import io.github.petals.Game.Player;
import io.github.petals.structures.PetalsGame;
import io.github.petals.structures.PetalsPlayer;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.exceptions.JedisConnectionException;

public class PetalsPlugin extends Petals {
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
        return gameIds.stream().map(id -> new PetalsGame(UUID.fromString(id), pooled)).collect(Collectors.toSet());
    }

    @Override
    public Set<Player> players() {
        Set<String> playerIds = pooled.smembers("players");
        return playerIds.stream().map(id -> new PetalsPlayer(UUID.fromString(id), pooled)).collect(Collectors.toSet());
    }

    @Override
    public Player player(UUID uniqueId) {
        return new PetalsPlayer(uniqueId, pooled);
    }

    public Game createGame(UUID host, String plugin) {
        UUID uniqueId = UUID.randomUUID();
        String uniqueIdStr = uniqueId.toString();

        // Create game
        HashMap<String, String> map = new HashMap<>();
        map.put("start", "-1");
        map.put("plugin", plugin);
        map.put("host", host.toString());
        pooled.hset(uniqueIdStr, map);

        pooled.sadd("games", uniqueIdStr);

        // Create host player
        createPlayer(host, uniqueId);

        return new PetalsGame(uniqueId, pooled);
    }

    public Player createPlayer(UUID player, UUID game) {
        String playerStr = player.toString();
        String gameStr = game.toString();

        HashMap<String, String> map = new HashMap<>();
        map.put("game", gameStr);
        pooled.hset(playerStr, map);

        pooled.sadd(gameStr + ":players", playerStr);
        pooled.sadd("players", playerStr);

        return new PetalsPlayer(player, pooled);
    }
}


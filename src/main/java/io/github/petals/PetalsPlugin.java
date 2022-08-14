package io.github.petals;

import java.io.WriteAbortedException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.vehicle.VehicleEvent;
import org.bukkit.event.weather.WeatherEvent;
import org.bukkit.event.world.WorldEvent;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import io.github.petals.Game.Player;
import io.github.petals.Game.World;
import io.github.petals.event.GameListener;
import io.github.petals.role.Role;
import io.github.petals.structures.PetalsGame;
import io.github.petals.structures.PetalsPlayer;
import io.github.petals.structures.PetalsScheduler;
import io.github.petals.structures.PetalsWorld;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.exceptions.JedisConnectionException;

public class PetalsPlugin extends JavaPlugin implements Petals {
    private JedisPooled pooled;

    public static PetalsPlugin petals() {
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
    public void registerEvents(GameListener listener, Petal plugin) {
        Method[] handlers = listener.getClass().getDeclaredMethods();
        for (int i = 0; i < handlers.length; i++) {
            Method handler = handlers[i];

            EventHandler annotation = handler.getDeclaredAnnotation(EventHandler.class);
            if (annotation == null
                || handler.getParameterCount() < 1
                || !Event.class.isAssignableFrom(handler.getParameterTypes()[0])) continue;

            EventExecutor executor;
            switch (handler.getParameterCount()) {
                case 1:
                    executor = new EventExecutor() {
                        @Override
                        public void execute(Listener _listener, Event event) throws EventException {
                            try {
                                handler.invoke(listener, event);
                            } catch (InvocationTargetException | IllegalAccessException e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    break;
                case 2:
                    executor = new EventExecutor() {
                        @Override
                        public void execute(Listener _listener, Event event) throws EventException {
                            Optional<World> world = Optional.empty();
                            if (event instanceof BlockEvent) world = world(((BlockEvent) event).getBlock().getWorld());
                            else if (event instanceof EntityEvent) world = world(((EntityEvent) event).getEntity().getWorld());
                            else if (event instanceof InventoryEvent) world = world(((InventoryEvent) event).getView().getPlayer().getWorld());
                            else if (event instanceof PlayerEvent) world = world(((PlayerEvent) event).getPlayer().getWorld());
                            else if (event instanceof VehicleEvent) world = world(((VehicleEvent) event).getVehicle().getWorld());
                            else if (event instanceof WeatherEvent) world = world(((WeatherEvent) event).getWorld());
                            else if (event instanceof WorldEvent) world = world(((WorldEvent) event).getWorld());
                            else return;
                            if (!world.isPresent()) return;

                            final Game game = world.get().game();
                            if (!game.exists() || !game.plugin().equals(plugin)) return;

                            try {
                                handler.invoke(listener, event, game);
                            } catch (InvocationTargetException | IllegalAccessException e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    break;
                default:
                    continue;
            }

            this.getServer().getPluginManager().registerEvent(
                (Class<Event>) handler.getParameterTypes()[0],
                listener,
                annotation.priority(),
                executor,
                (Plugin) plugin,
                annotation.ignoreCancelled()
            );
        }
    }

    @Override
    public Game createGame(String host, Petal plugin) {
        String uniqueId = UUID.randomUUID().toString();

        // Create game
        HashMap<String, String> map = new HashMap<>();
        map.put("start", "-1");
        map.put("host", host);
        map.put("plugin", plugin.getName());
        pooled.hset(uniqueId, map);
        pooled.sadd("games", uniqueId);

        createPlayer(host, uniqueId);

        Game game = new PetalsGame(uniqueId, pooled);
        plugin.onCreateGame(game);
        return game;
    }

    public World createWorld(String name, String game) {
        pooled.hset("worlds", name, game);
        pooled.sadd(game + ":worlds", name);

        return new PetalsWorld(name, pooled);
    }

    public <T extends Role> Player<T> createPlayer(String player, String game) {
        HashMap<String, String> map = new HashMap<>();
        map.put("game", game);
        pooled.hset(player, map);

        pooled.sadd(game + ":players", player);
        pooled.sadd("players", player);

        Player<T> p = new PetalsPlayer<>(player, pooled);
        p.game().plugin().onAddPlayer((Player<Role>) p);
        return p;
    }
}


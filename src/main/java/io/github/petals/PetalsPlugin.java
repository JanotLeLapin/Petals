package io.github.petals;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

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

import io.github.petals.Game.World;
import io.github.petals.event.GameListener;
import redis.clients.jedis.exceptions.JedisConnectionException;

public class PetalsPlugin extends JavaPlugin implements Petals {
    private PetalsDatabase database;

    public static PetalsPlugin petals() {
        return (PetalsPlugin) Bukkit.getPluginManager().getPlugin("Petals");
    }

    @Override
    public void onEnable() {
        this.saveDefaultConfig();

        // Connect to database
        final String host = this.getConfig().getString("redis.host", "127.0.0.1");
        final int port = this.getConfig().getInt("redis.port", 6379);
        this.database = new PetalsDatabase(host, port);
        if (!this.database.ping()) {
            this.getLogger().severe("Could not reach database.");
            Bukkit.getPluginManager().disablePlugin(this);
        };
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
            this.database.games().forEach(game -> game.delete());
        } catch (JedisConnectionException e) {}
    }

    @Override
    public PetalsDatabase database() {
        return this.database;
    }

    private EventExecutor lambdaToExecutor(Function<Event, Optional<World>> lambda, BiConsumer<World, Event> callback) {
        return new EventExecutor() {
            @Override
            public void execute(Listener listener, Event event) throws EventException {
                lambda.apply(event).ifPresent(world -> callback.accept(world, event));
            }
        };
    }

    private EventExecutor classToExecutor(Class<Event> clazz, BiConsumer<World, Event> callback) {
        // Find appropriate executor from Event class at load time to save performance
        if (BlockEvent.class.isAssignableFrom(clazz)) return lambdaToExecutor(event -> database.world(((BlockEvent) event).getBlock().getWorld()), callback);
        else if (EntityEvent.class.isAssignableFrom(clazz)) return lambdaToExecutor(event -> database.world(((EntityEvent) event).getEntity().getWorld()), callback);
        else if (InventoryEvent.class.isAssignableFrom(clazz)) return lambdaToExecutor(event -> database.world(((InventoryEvent) event).getView().getPlayer().getWorld()), callback);
        else if (PlayerEvent.class.isAssignableFrom(clazz)) return lambdaToExecutor(event -> database.world(((PlayerEvent) event).getPlayer().getWorld()), callback);
        else if (VehicleEvent.class.isAssignableFrom(clazz)) return lambdaToExecutor(event -> database.world(((VehicleEvent) event).getVehicle().getWorld()), callback);
        else if (WeatherEvent.class.isAssignableFrom(clazz)) return lambdaToExecutor(event -> database.world(((WeatherEvent) event).getWorld()), callback);
        else if (WorldEvent.class.isAssignableFrom(clazz)) return lambdaToExecutor(event -> database.world(((WorldEvent) event).getWorld()), callback);

        this.getLogger().info(String.format("Unsupported Event: \"%s\". This Event will not fire a GameListener handler.", clazz.getName()));

        return null;
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

            Class<Event> eventClass = (Class<Event>) handler.getParameterTypes()[0];

            EventExecutor executor;
            switch (handler.getParameterCount()) {
                case 1:
                    executor = new EventExecutor() {
                        @Override
                        public void execute(Listener _listener, Event event) throws EventException {
                            if (eventClass.isAssignableFrom(event.getClass())) {
                                try {
                                    handler.invoke(listener, event);
                                } catch (InvocationTargetException | IllegalAccessException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    };
                    break;
                case 2:
                    executor = classToExecutor(eventClass, (world, event) -> {
                        final Game game = world.game();
                        if (eventClass.isAssignableFrom(event.getClass()) && game.exists() && game.plugin().getName().equals(plugin.getName())) {
                            try {
                                handler.invoke(listener, event, game);
                            } catch (InvocationTargetException | IllegalAccessException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    if (executor == null) return;

                    break;
                default:
                    continue;
            }

            this.getServer().getPluginManager().registerEvent(
                eventClass,
                listener,
                annotation.priority(),
                executor,
                (Plugin) plugin,
                annotation.ignoreCancelled()
            );
        }
    }
}


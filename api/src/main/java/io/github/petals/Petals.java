package io.github.petals;

import java.util.Optional;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import io.github.petals.Game.Player;
import io.github.petals.Game.World;
import io.github.petals.event.GameListener;
import io.github.petals.role.Role;

/** The Petals API */
public interface Petals extends Plugin {
    /** @return an instance of the Petals plugin */
    static Petals petals() {
        return (Petals) Bukkit.getPluginManager().getPlugin("Petals");
    }

    /** @return a set containing every currently stored games */
    public Set<Game> games();

    /**
     * Finds a player stored in the database from its ID
     *
     * @param uniqueId The player ID to lookup
     * @return The player handle
     */
    public Optional<Player<Role>> player(String uniqueId);
    /**
     * Finds a player stored in the database from a Bukkit Player instance
     *
     * @param player The Bukkit object
     * @return The player handle
     */
    public Optional<Player<Role>> player(org.bukkit.entity.Player player);
    /**
     * Finds a world stored in the database from its name
     *
     * @param name The world name to lookup
     * @return The world handle
     */
    public Optional<World> world(String name);
    /**
     * Finds a world stored in the database from a Bukkit World instance
     *
     * @param world The Bukkit object
     * @return The world handle
     */
    public Optional<World> world(org.bukkit.World world);

    /**
     * Listens for events
     *
     * @param listener The event handler. See {@link GameListener}
     * @param plugin The {@link Petal} plugin associated with the listener
     */
    public void registerEvents(GameListener listener, Petal plugin);

    /**
     * Creates a new game
     *
     * @param host The host player ID for this game
     * @param plugin The plugin responsible for this game
     * @return The game
     */
    public Game createGame(String host, Petal plugin);
}


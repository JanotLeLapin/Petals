package io.github.petals;

import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import io.github.petals.Game.Player;
import io.github.petals.Game.World;
import io.github.petals.event.GameListener;

/** The Petals API */
public interface Petals extends Plugin {
    /** @return an instance of the Petals plugin */
    static Petals petals() {
        return (Petals) Bukkit.getPluginManager().getPlugin("Petals");
    }

    /** @return a set containing every currently stored games */
    public Set<Game> games();

    /**
     * Finds a player stored in the database from its ID. Although never null, it may not exist
     *
     * @param uniqueId The player ID to lookup
     * @return The player handle
     */
    public Player player(String uniqueId);
    /**
     * Finds a world stored in the database from its name. Although never null, it may not exist
     *
     * @param name The world name to lookup
     * @return The world handle
     */
    public World world(String name);

    /**
     * Listens for events
     *
     * @param listener The event handler. See {@link GameListener}
     * @param plugin The {@link Petal} plugin associated with the listener
     */
    public void registerEvents(GameListener listener, Petal plugin);
}


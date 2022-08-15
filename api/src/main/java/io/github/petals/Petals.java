package io.github.petals;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import io.github.petals.event.GameListener;

/** The Petals API */
public interface Petals extends Plugin {
    /** @return an instance of the Petals plugin */
    static Petals petals() {
        return (Petals) Bukkit.getPluginManager().getPlugin("Petals");
    }

    /**
     * Listens for events
     *
     * @param listener The event handler. See {@link GameListener}
     * @param plugin The {@link Petal} plugin associated with the listener
     */
    public void registerEvents(GameListener listener, Petal plugin);

    /** @return a Database object */
    public Database database();
}


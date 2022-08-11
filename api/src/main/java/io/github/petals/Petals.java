package io.github.petals;

import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import io.github.petals.Game.Player;

public abstract class Petals extends JavaPlugin {
    static Petals petals() {
        return (Petals) Bukkit.getPluginManager().getPlugin("Petals");
    }

    public abstract Set<Game> games();

    public abstract Set<Player> players();
    public abstract Player player(String uniqueId);
}


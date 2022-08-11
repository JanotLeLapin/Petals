package io.github.petals;

import java.util.Set;

import org.bukkit.Bukkit;

import io.github.petals.Game.Player;
import io.github.petals.Game.World;
import io.github.petals.event.GameListener;

public interface Petals {
    static Petals petals() {
        return (Petals) Bukkit.getPluginManager().getPlugin("Petals");
    }

    public Set<Game> games();

    public Set<Player> players();
    public Player player(String uniqueId);

    public World world(String name);

    public void registerEvents(GameListener listener, Petal plugin);
}


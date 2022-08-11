package io.github.petals;

import org.bukkit.plugin.Plugin;

import io.github.petals.Game.Player;

public interface Petal extends Plugin {
    public void onCreateGame(Game game);
    public void onStartGame(Game game);
    public void onStopGame(Game game);

    public void onAddPlayer(Player player);
    public void onRemovePlayer(Player player);
}


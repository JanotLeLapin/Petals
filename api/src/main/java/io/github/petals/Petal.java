package io.github.petals;

import org.bukkit.plugin.Plugin;

public interface Petal extends Plugin {
    public void onCreateGame(Game game);
    public void onStartGame(Game game);
    public void onStopGame(Game game);
}


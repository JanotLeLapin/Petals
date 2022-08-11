package io.github.petals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.StringUtil;

public class PetalsCommand implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player) || args.length == 0) return false;

        if (args[0].equals("create")) {
            if (args.length < 2) return false;

            final Plugin plugin = Bukkit.getPluginManager().getPlugin(args[1]);
            if (plugin == null || !(plugin instanceof Petal)) return false;

            // TODO: Create game
            return true;
        }

        if (args[0].equals("start") || args[0].equals("get") || args[0].equals("stop")) {
            final Game game = Petals
                .petals()
                .player(((Player) sender).getUniqueId())
                .game();
            if (!game.exists()) return false;

            switch (args[0]) {
                case "start":
                    // TODO: Start game
                    game.plugin().onStartGame(game);
                    return true;
                case "get":
                    sender.sendMessage(game.uniqueId().toString());
                    return true;
                case "stop":
                    // TODO: Start game
                    game.plugin().onStopGame(game);
                    return true;
            }
        }

        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        ArrayList<String> completions = new ArrayList<>();
        switch (args.length) {
            case 1:
                completions.add("create");
                completions.add("start");
                completions.add("get");
                completions.add("stop");
                break;
            case 2:
                if (args[0].equals("create")) {
                    List<String> plugins = Arrays
                        .asList(Bukkit.getPluginManager().getPlugins())
                        .stream()
                        .filter(plugin -> plugin instanceof Petal)
                        .map(plugin -> plugin.getName())
                        .collect(Collectors.toList());

                    completions.addAll(plugins);
                }
                break;
        }

        ArrayList<String> matches = new ArrayList<>();
        StringUtil.copyPartialMatches(args[args.length - 1], completions, matches);
        return matches;
    }
}


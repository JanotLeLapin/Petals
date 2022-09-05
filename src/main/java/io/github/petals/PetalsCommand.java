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

            PetalsPlugin.petals().database().createGame(((Player) sender).getUniqueId().toString(), (Petal) plugin);

            return true;
        }

        if (args[0].equals("start") || args[0].equals("get") || args[0].equals("delete")) {
            final Game<?> game;
            if (args.length < 2) {
                game = Petals
                    .petals()
                    .database()
                    .player(((Player) sender).getUniqueId().toString())
                    .get()
                    .game();
            } else {
                game = Petals
                    .petals()
                    .database()
                    .games()
                    .stream()
                    .filter(g -> g.uniqueId().equals(args[1]))
                    .collect(Collectors.toList())
                    .get(0);
            }
            if (!game.exists()) return false;

            switch (args[0]) {
                case "start":
                    game.start();
                    return true;
                case "get":
                    sender.sendMessage(new String[] {
                        String.format("Game ID: %s", game.uniqueId()),
                        String.format("Player count: %d", game.players().size()),
                        String.format("Host: %s", game.host().player().map(host -> host.getName()).orElseGet(game.host()::uniqueId)),
                        String.format("Seconds elapsed: %d", game.ticks() / 20),
                        String.format("Plugin: %s", game.plugin().getName()),
                        String.format("Status: %s", game.running() ? "running" : "creating"),
                    });
                    return true;
                case "delete":
                    game.delete();
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
                completions.add("delete");
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
                } else {
                    List<String> games = Petals
                        .petals()
                        .database()
                        .games()
                        .stream()
                        .map(game -> game.uniqueId())
                        .collect(Collectors.toList());

                    completions.addAll(games);
                }
                break;
        }

        ArrayList<String> matches = new ArrayList<>();
        StringUtil.copyPartialMatches(args[args.length - 1], completions, matches);
        return matches;
    }
}


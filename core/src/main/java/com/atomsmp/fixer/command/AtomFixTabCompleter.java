package com.atomsmp.fixer.command;

import com.atomsmp.fixer.AtomSMPFixer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * /atomfix komutu için tab completion sınıfı
 *
 * @author AtomSMP
 * @version 4.0.0
 */
public class AtomFixTabCompleter implements TabCompleter {

    private final AtomSMPFixer plugin;

    private static final List<String> MAIN_COMMANDS = Arrays.asList(
        "reload", "status", "toggle", "stats", "info", "antivpn", "antibot"
    );

    private static final List<String> ANTIVPN_SUBS = Arrays.asList(
        "stats", "refresh", "check", "add", "remove", "whitelist", "recent"
    );

    private static final List<String> ANTIBOT_SUBS = Arrays.asList(
        "status", "whitelist", "blacklist", "reset", "score"
    );

    private static final List<String> WHITELIST_ACTIONS = Arrays.asList("add", "remove");

    public AtomFixTabCompleter(@NotNull AtomSMPFixer plugin) {
        this.plugin = plugin;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (!sender.hasPermission("atomsmpfixer.admin")) {
            return new ArrayList<>();
        }

        if (args.length == 1) {
            return filterSuggestions(MAIN_COMMANDS, args[0]);
        }

        if (args.length == 2) {
            String sub = args[0].toLowerCase();
            if (sub.equals("toggle")) {
                return filterSuggestions(new ArrayList<>(plugin.getModuleManager().getModuleNames()), args[1]);
            }
            if (sub.equals("antivpn")) {
                return filterSuggestions(ANTIVPN_SUBS, args[1]);
            }
            if (sub.equals("antibot")) {
                return filterSuggestions(ANTIBOT_SUBS, args[1]);
            }
        }

        if (args.length == 3) {
            String sub = args[0].toLowerCase();
            String sub2 = args[1].toLowerCase();
            if (sub.equals("antivpn") && sub2.equals("whitelist")) {
                return filterSuggestions(WHITELIST_ACTIONS, args[2]);
            }
        }

        return new ArrayList<>();
    }

    @NotNull
    private List<String> filterSuggestions(@NotNull List<String> suggestions, @NotNull String input) {
        String lowerInput = input.toLowerCase();
        return suggestions.stream()
            .filter(s -> s.toLowerCase().startsWith(lowerInput))
            .sorted()
            .collect(Collectors.toList());
    }
}

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
 * Komut otomatik tamamlama önerileri sağlar
 *
 * @author AtomSMP
 * @version 1.0.0
 */
public class AtomFixTabCompleter implements TabCompleter {

    private final AtomSMPFixer plugin;

    // Ana komutlar
    private static final List<String> MAIN_COMMANDS = Arrays.asList(
        "reload",
        "status",
        "toggle",
        "info"
    );

    /**
     * AtomFixTabCompleter constructor
     *
     * @param plugin Ana plugin instance
     */
    public AtomFixTabCompleter(@NotNull AtomSMPFixer plugin) {
        this.plugin = plugin;
    }

    /**
     * Tab completion
     *
     * @param sender Komut gönderen
     * @param command Komut
     * @param alias Komut alias
     * @param args Komut argümanları
     * @return Öneriler listesi
     */
    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        // İzin kontrolü
        if (!sender.hasPermission("atomsmpfixer.admin")) {
            return new ArrayList<>();
        }

        // İlk argüman - ana komutlar
        if (args.length == 1) {
            return filterSuggestions(MAIN_COMMANDS, args[0]);
        }

        // İkinci argüman
        if (args.length == 2) {
            String subCommand = args[0].toLowerCase();

            // Toggle komutu için modül isimleri
            if (subCommand.equals("toggle")) {
                List<String> moduleNames = new ArrayList<>(plugin.getModuleManager().getModuleNames());
                return filterSuggestions(moduleNames, args[1]);
            }
        }

        return new ArrayList<>();
    }

    /**
     * Önerileri filtreler
     * Kullanıcının yazdığı ile başlayanları döner
     *
     * @param suggestions Öneri listesi
     * @param input Kullanıcı girdisi
     * @return Filtrelenmiş öneri listesi
     */
    @NotNull
    private List<String> filterSuggestions(@NotNull List<String> suggestions, @NotNull String input) {
        String lowerInput = input.toLowerCase();

        return suggestions.stream()
            .filter(suggestion -> suggestion.toLowerCase().startsWith(lowerInput))
            .sorted()
            .collect(Collectors.toList());
    }
}

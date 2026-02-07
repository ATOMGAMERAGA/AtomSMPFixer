package com.atomsmp.fixer.listener;

import com.atomsmp.fixer.AtomSMPFixer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Bukkit eventlerini dinleyen listener sınıfı
 * Oyuncu join/quit ve diğer Bukkit eventlerini işler
 *
 * @author AtomSMP
 * @version 1.0.0
 */
public class BukkitListener implements Listener {

    private final AtomSMPFixer plugin;

    /**
     * BukkitListener constructor
     *
     * @param plugin Ana plugin instance
     */
    public BukkitListener(@NotNull AtomSMPFixer plugin) {
        this.plugin = plugin;
    }

    /**
     * Oyuncu sunucuya katıldığında
     *
     * @param event Join eventi
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(@NotNull PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Debug log
        if (plugin.getConfigManager().isDebugEnabled()) {
            plugin.getLogManager().debug("Oyuncu katıldı: " + player.getName());
        }

        // PlayerData oluşturma gibi işlemler
        // Modüller eklendiğinde buraya eklenecek
    }

    /**
     * Oyuncu sunucudan ayrıldığında
     *
     * @param event Quit eventi
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(@NotNull PlayerQuitEvent event) {
        Player player = event.getPlayer();

        // Debug log
        if (plugin.getConfigManager().isDebugEnabled()) {
            plugin.getLogManager().debug("Oyuncu ayrıldı: " + player.getName());
        }

        // PlayerData temizleme gibi işlemler
        // Modüller eklendiğinde buraya eklenecek
    }
}

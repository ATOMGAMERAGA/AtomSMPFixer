package com.atomsmp.fixer.listener;

import com.atomsmp.fixer.AtomSMPFixer;
import com.atomsmp.fixer.module.OfflinePacketModule;
import com.atomsmp.fixer.module.PacketDelayModule;
import com.atomsmp.fixer.module.PacketExploitModule;
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

        // OfflinePacketModule'e login bildirimi
        OfflinePacketModule offlineModule = plugin.getModuleManager().getModule(OfflinePacketModule.class);
        if (offlineModule != null) {
            offlineModule.onPlayerLogin(player.getUniqueId());
        }
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

        // Modüllere oyuncu çıkış bildirimi - veri temizleme
        OfflinePacketModule offlineModule = plugin.getModuleManager().getModule(OfflinePacketModule.class);
        if (offlineModule != null) {
            offlineModule.onPlayerLogout(player.getUniqueId());
        }

        PacketDelayModule delayModule = plugin.getModuleManager().getModule(PacketDelayModule.class);
        if (delayModule != null) {
            delayModule.removePlayerData(player.getUniqueId());
        }

        PacketExploitModule exploitModule = plugin.getModuleManager().getModule(PacketExploitModule.class);
        if (exploitModule != null) {
            exploitModule.removePlayerData(player.getUniqueId());
        }
    }
}

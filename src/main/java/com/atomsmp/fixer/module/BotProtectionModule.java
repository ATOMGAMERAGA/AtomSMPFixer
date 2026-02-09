package com.atomsmp.fixer.module;

import com.atomsmp.fixer.AtomSMPFixer;
import com.atomsmp.fixer.util.BotUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BotProtectionModule extends AbstractModule implements Listener {

    private final Set<UUID> pendingVerification = ConcurrentHashMap.newKeySet();
    private final Set<String> onlinePlayerNames = ConcurrentHashMap.newKeySet();
    // IP Address -> Offense Count
    private final Map<String, Integer> ipOffenseCount = new ConcurrentHashMap<>();

    public BotProtectionModule(AtomSMPFixer plugin) {
        super(plugin, "bot-korumasi", "Bot algılama ve koruma modülü");
    }

    @Override
    public void onEnable() {
        super.onEnable();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        // Populate initially
        for (Player p : Bukkit.getOnlinePlayers()) {
            onlinePlayerNames.add(p.getName());
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        HandlerList.unregisterAll(this);
        pendingVerification.clear();
        onlinePlayerNames.clear();
        ipOffenseCount.clear();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        if (!isEnabled()) return;

        // 1. Check Similar Names
        if (getConfigBoolean("benzer-isim-kontrolu.aktif", true)) {
            checkSimilarNames(event);
        }

        // 2. Offense Check (Ban logic)
        String ip = event.getAddress().getHostAddress();
        if (ipOffenseCount.getOrDefault(ip, 0) >= 2) {
             // Already marked for ban, but if they try to join again before the command executed?
             // Usually handled by the ban command itself.
             // But we can disallow login here too.
        }
    }

    private void checkSimilarNames(AsyncPlayerPreLoginEvent event) {
        String name = event.getName();
        int threshold = getConfigInt("benzer-isim-kontrolu.karakter-farki", 3);
        int maxSimilar = getConfigInt("benzer-isim-kontrolu.max-benzer-sayisi", 10);
        
        List<String> similarPlayers = new ArrayList<>();
        for (String pName : onlinePlayerNames) {
            if (BotUtils.getLevenshteinDistance(name, pName) <= threshold) {
                similarPlayers.add(pName);
            }
        }

        // Add current player to count (checking against others)
        if (similarPlayers.size() >= maxSimilar - 1) {
             event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, 
                ChatColor.RED + "Bot saldırısı şüphesi (Benzer isimler).");
            
            incrementBlockedCount();
            logExploit(name, "Benzer isim saldırısı tespit edildi. Benzerler: " + similarPlayers.size());

            // Ban others on main thread
            new BukkitRunnable() {
                @Override
                public void run() {
                    for (String similarName : similarPlayers) {
                         Player target = Bukkit.getPlayerExact(similarName);
                         if (target != null) {
                             performBan(target.getName(), target.getAddress().getAddress().getHostAddress());
                             target.kickPlayer(ChatColor.RED + "Bot saldırısı şüphesi (Toplu işlem).");
                         }
                    }
                }
            }.runTask(plugin);
            
            // Ban current IP
            performBan(name, event.getAddress().getHostAddress());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent event) {
        if (!isEnabled()) return;

        Player player = event.getPlayer();
        onlinePlayerNames.add(player.getName());

        if (getConfigBoolean("dogrulama.aktif", true)) {
            if (player.hasPermission("atomsmpfixer.bypass")) return;

            pendingVerification.add(player.getUniqueId());
            
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                getConfigString("dogrulama.dogrulama-mesaji", "&cLütfen doğrulamak için hareket edin!")));

            // Timeout scheduler
            int timeout = getConfigInt("dogrulama.sure", 15);
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (player.isOnline() && pendingVerification.contains(player.getUniqueId())) {
                        player.kickPlayer(ChatColor.RED + "Doğrulama zaman aşımı.");
                        handleOffense(player.getName(), player.getAddress().getAddress().getHostAddress());
                    }
                }
            }.runTaskLater(plugin, timeout * 20L);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        if (!isEnabled()) return;

        Player player = event.getPlayer();
        if (pendingVerification.contains(player.getUniqueId())) {
            // Check if actual movement occurred (threshold 0.1 blocks)
            if (event.getFrom().getWorld() != event.getTo().getWorld()) return;
            double distanceSq = event.getFrom().distanceSquared(event.getTo());
            if (distanceSq > 0.01) { // 0.1^2
                
                // Verified!
                pendingVerification.remove(player.getUniqueId());
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                    getConfigString("dogrulama.dogrulandi-mesaji", "&aDoğrulama başarılı!")));
            }
        }
    }
    
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        pendingVerification.remove(event.getPlayer().getUniqueId());
        onlinePlayerNames.remove(event.getPlayer().getName());
    }

    private void handleOffense(String playerName, String ip) {
        int offenses = ipOffenseCount.getOrDefault(ip, 0) + 1;
        ipOffenseCount.put(ip, offenses);

        if (offenses == 1) {
            // First offense is just the kick (handled by timeout usually)
            logExploit(playerName, "Bot şüphesi: İlk ihlal (IP: " + ip + ")");
        } else if (offenses >= 2) {
            // Second offense -> BAN
            performBan(playerName, ip);
            ipOffenseCount.remove(ip); // Reset after ban
        }
    }

    private void performBan(String playerName, String ip) {
        String banCmd = getConfigString("ceza.ban-komutu", "ban %player% 30dk Bot saldırısı tespit edildi");
        banCmd = banCmd.replace("%player%", playerName);
        
        final String commandToRun = banCmd;
        new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), commandToRun);
                // Also log to panic/bot log using BotUtils
                BotUtils.logPanicBan(plugin, playerName, ip, "bot-bans.log");
            }
        }.runTask(plugin);
        
        logExploit(playerName, "Bot olarak algılandı ve yasaklandı. IP: " + ip);
    }
}

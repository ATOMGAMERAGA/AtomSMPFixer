package com.atomsmp.fixer.listener;

import com.atomsmp.fixer.AtomSMPFixer;
import com.atomsmp.fixer.data.VerifiedPlayerCache;
import com.atomsmp.fixer.module.AdvancedPayloadModule;
import com.atomsmp.fixer.module.OfflinePacketModule;
import com.atomsmp.fixer.module.PacketDelayModule;
import com.atomsmp.fixer.module.PacketExploitModule;
import com.atomsmp.fixer.module.TokenBucketModule;
import com.atomsmp.fixer.reputation.IPReputationManager;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
     * Oyuncu bağlanmaya çalıştığında (Async)
     * IP itibarını ve proxy/VPN durumunu kontrol eder
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerPreLogin(@NotNull AsyncPlayerPreLoginEvent event) {
        // Record connection for Attack Mode tracking
        plugin.getAttackModeManager().recordConnection();

        String ip = event.getAddress().getHostAddress();

        // v2.3 — Attack mode connection blocking
        if (plugin.getAttackModeManager().shouldBlockConnection(ip)) {
            plugin.getLogManager().logBot(event.getName(), ip, "Attack mode: dogrulanmamis IP engellendi");
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
                    LegacyComponentSerializer.legacyAmpersand().deserialize(
                            "&cSunucu saldiri altinda. Lutfen daha sonra tekrar deneyin."));
            return;
        }

        // v2.3 — Verified player cache: skip checks for verified players
        VerifiedPlayerCache cache = plugin.getVerifiedPlayerCache();
        if (cache != null && cache.isVerified(event.getName(), ip)) {
            if (cache.shouldSkipIpCheck()) {
                // Skip IP reputation check for verified players
                return;
            }
        }

        // IP kontrolü
        try {
            // Async kontrol, 2 saniye timeout (Sunucuyu bekletmemek için)
            IPReputationManager.ReputationResult result = plugin.getReputationManager().checkIp(ip)
                    .get(2, TimeUnit.SECONDS);

            if (result.isBlocked) {
                plugin.getLogManager().logBot(event.getName(), ip, 
                        String.format("Korumaya Takıldı! Risk: %d | Tip: %s | Ülke: %s | ASN: %s", 
                        result.riskScore, result.type, result.country, result.asn));
                
                String kickMessage = plugin.getConfig().getString("anti-vpn.kick-message", 
                        "&cProxy veya VPN kullanımı yasaktır!");
                
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, 
                        LegacyComponentSerializer.legacyAmpersand().deserialize(kickMessage));
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            // Hata durumunda veya timeout'ta oyuncunun girişine izin ver (fail-safe)
            if (plugin.getConfigManager().isDebugEnabled()) {
                plugin.getLogManager().debug("IP kontrolü sırasında hata veya zaman aşımı: " + ip);
            }
        }
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

        // v2.3 — Record verified IP (player successfully joined)
        if (player.getAddress() != null) {
            String ip = player.getAddress().getAddress().getHostAddress();
            plugin.getAttackModeManager().recordVerifiedIp(ip);

            // v2.3 — Add to verified player cache
            if (plugin.getVerifiedPlayerCache() != null) {
                plugin.getVerifiedPlayerCache().addVerified(player.getName(), ip);
            }
        }

        // OfflinePacketModule'e login bildirimi
        OfflinePacketModule offlineModule = plugin.getModuleManager().getModule(OfflinePacketModule.class);
        if (offlineModule != null && player.getAddress() != null) {
            offlineModule.onPlayerLogin(player.getUniqueId(), player.getAddress().getAddress());
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

        // v2.0 — Yeni modül temizlikleri
        TokenBucketModule tokenModule = plugin.getModuleManager().getModule(TokenBucketModule.class);
        if (tokenModule != null) {
            tokenModule.removePlayerData(player.getUniqueId());
        }

        AdvancedPayloadModule payloadModule = plugin.getModuleManager().getModule(AdvancedPayloadModule.class);
        if (payloadModule != null) {
            payloadModule.removePlayerData(player.getUniqueId());
        }
    }
}

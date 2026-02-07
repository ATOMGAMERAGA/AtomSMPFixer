package com.atomsmp.fixer.module;

import com.atomsmp.fixer.AtomSMPFixer;
import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Çevrimdışı Paket Modülü
 *
 * Paket geldiğinde oyuncunun online olup olmadığını kontrol eder.
 * Offline paket exploit'lerini önler.
 *
 * Özellikler:
 * - Online durum kontrolü
 * - Grace period (tolerans süresi)
 * - Login tracking
 * - Offline paket engelleme
 *
 * @author AtomSMP
 * @version 1.0.0
 */
public class OfflinePacketModule extends AbstractModule implements PacketListener {

    // Oyuncu login zamanlarını saklayan map
    private final Map<UUID, Long> loginTimes;

    // Config cache
    private long toleranceMs;

    /**
     * OfflinePacketModule constructor
     *
     * @param plugin Ana plugin instance
     */
    public OfflinePacketModule(@NotNull AtomSMPFixer plugin) {
        super(plugin, "cevrimdisi-paket", "Çevrimdışı paket kontrolü");
        this.loginTimes = new ConcurrentHashMap<>();
    }

    @Override
    public void onEnable() {
        super.onEnable();

        // Config değerlerini yükle
        loadConfig();

        // PacketEvents listener'ı kaydet
        com.github.retrooper.packetevents.PacketEvents.getAPI()
            .getEventManager()
            .registerListener(this);

        // Online oyuncuların login time'ını kaydet
        for (Player player : Bukkit.getOnlinePlayers()) {
            loginTimes.put(player.getUniqueId(), System.currentTimeMillis());
        }

        debug("Modül aktifleştirildi. Tolerans süresi: " + toleranceMs + "ms");
    }

    @Override
    public void onDisable() {
        super.onDisable();

        // PacketEvents listener'ı kaldır
        com.github.retrooper.packetevents.PacketEvents.getAPI()
            .getEventManager()
            .unregisterListener(this);

        // Login time'ları temizle
        loginTimes.clear();

        debug("Modül devre dışı bırakıldı.");
    }

    /**
     * Config değerlerini yükler
     */
    private void loadConfig() {
        this.toleranceMs = getConfigLong("tolerans-suresi-ms", 3000L); // 3 saniye

        debug("Config yüklendi: tolerance=" + toleranceMs + "ms");
    }

    /**
     * Paket alındığında çağrılır
     */
    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (!isEnabled()) {
            return;
        }

        Object playerObj = event.getPlayer();
        if (!(playerObj instanceof Player player)) {
            // Oyuncu bulunamadı
            debug("Paket alındı ama oyuncu bulunamadı");
            return;
        }

        UUID uuid = player.getUniqueId();

        // Login time kaydı yoksa ekle
        loginTimes.putIfAbsent(uuid, System.currentTimeMillis());

        // Oyuncunun online olup olmadığını kontrol et
        Player onlinePlayer = Bukkit.getPlayer(uuid);
        if (onlinePlayer == null || !onlinePlayer.isOnline()) {
            incrementBlockedCount();

            logExploit(player.getName(),
                String.format("Çevrimdışı oyuncudan paket alındı! Paket: %s",
                    event.getPacketType().getName()));

            event.setCancelled(true);
            debug(player.getName() + " için paket engellendi (çevrimdışı)");
            return;
        }

        // Grace period kontrolü (yeni login olan oyuncular için)
        long loginTime = loginTimes.get(uuid);
        long timeSinceLogin = System.currentTimeMillis() - loginTime;

        if (timeSinceLogin < toleranceMs) {
            // Grace period içinde, normal işlem
            debug(player.getName() + " grace period içinde (" + timeSinceLogin + "ms)");
        }
    }

    /**
     * Oyuncu login olduğunda çağrılır
     */
    public void onPlayerLogin(@NotNull UUID uuid) {
        loginTimes.put(uuid, System.currentTimeMillis());
        debug("Login time kaydedildi: " + uuid);
    }

    /**
     * Oyuncu logout olduğunda çağrılır
     */
    public void onPlayerLogout(@NotNull UUID uuid) {
        loginTimes.remove(uuid);
        debug("Login time kaldırıldı: " + uuid);
    }

    /**
     * Oyuncunun grace period içinde olup olmadığını kontrol eder
     */
    public boolean isInGracePeriod(@NotNull UUID uuid) {
        Long loginTime = loginTimes.get(uuid);
        if (loginTime == null) {
            return false;
        }

        long timeSinceLogin = System.currentTimeMillis() - loginTime;
        return timeSinceLogin < toleranceMs;
    }

    /**
     * Memory optimization - kullanılmayan kayıtları temizler
     */
    public void cleanup() {
        long currentTime = System.currentTimeMillis();
        long expiryTime = 300000; // 5 dakika

        loginTimes.entrySet().removeIf(entry -> {
            // Online değilse ve 5 dakikadan eskiyse kaldır
            Player player = Bukkit.getPlayer(entry.getKey());
            return (player == null || !player.isOnline()) &&
                   (currentTime - entry.getValue() > expiryTime);
        });
    }

    /**
     * Modül istatistiklerini döndürür
     */
    public String getStatistics() {
        return String.format("Takip edilen oyuncu: %d, Engellenen paket: %d",
            loginTimes.size(),
            getBlockedCount());
    }
}

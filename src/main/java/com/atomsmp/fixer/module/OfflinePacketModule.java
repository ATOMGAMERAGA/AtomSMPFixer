package com.atomsmp.fixer.module;

import com.atomsmp.fixer.AtomSMPFixer;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
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
public class OfflinePacketModule extends AbstractModule {

    private PacketListenerAbstract listener;

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

        // PacketEvents listener'ı oluştur ve kaydet
        listener = new PacketListenerAbstract(PacketListenerPriority.NORMAL) {
            @Override
            public void onPacketReceive(PacketReceiveEvent event) {
                handlePacketReceive(event);
            }
        };

        com.github.retrooper.packetevents.PacketEvents.getAPI()
            .getEventManager()
            .registerListener(listener);

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
        if (listener != null) {
            com.github.retrooper.packetevents.PacketEvents.getAPI()
                .getEventManager()
                .unregisterListener(listener);
        }

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
    private void handlePacketReceive(PacketReceiveEvent event) {
        if (!isEnabled()) {
            return;
        }

        // Sadece Play aşamasındaki paketleri kontrol et
        // Login/Handshake aşamasındaki paketleri atla - bu aşamada oyuncu henüz Bukkit'e kayıtlı değil
        if (!(event.getPacketType() instanceof PacketType.Play.Client)) {
            return;
        }

        Object playerObj = event.getPlayer();
        if (!(playerObj instanceof Player player)) {
            return;
        }

        UUID uuid = player.getUniqueId();

        // Login time kaydı yoksa ekle (ilk Play paketi geldiğinde)
        loginTimes.putIfAbsent(uuid, System.currentTimeMillis());

        // Grace period kontrolü - yeni giriş yapan oyuncuları engelleme
        Long loginTime = loginTimes.get(uuid);
        if (loginTime != null) {
            long timeSinceLogin = System.currentTimeMillis() - loginTime;
            if (timeSinceLogin < toleranceMs) {
                debug(player.getName() + " grace period içinde (" + timeSinceLogin + "ms)");
                return;
            }
        }

        // Oyuncunun online olup olmadığını kontrol et
        Player onlinePlayer = Bukkit.getPlayer(uuid);
        if (onlinePlayer == null || !onlinePlayer.isOnline()) {
            incrementBlockedCount();

            logExploit(player.getName(),
                String.format("Çevrimdışı oyuncudan paket alındı! Paket: %s",
                    event.getPacketType().getName()));

            event.setCancelled(true);
            debug(player.getName() + " için paket engellendi (çevrimdışı)");
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

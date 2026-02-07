package com.atomsmp.fixer.module;

import com.atomsmp.fixer.AtomSMPFixer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Envanter Duplikasyon Modülü
 *
 * Envanter açıkken blok kırma exploit'ini önler.
 * BlockBreakEvent + InventoryOpenEvent kombinasyonu ile duplikasyonu engeller.
 *
 * Özellikler:
 * - Envanter açık tracking
 * - Blok kırma engelleme
 * - Timing kontrolü
 * - Duplikasyon exploit önleme
 *
 * @author AtomSMP
 * @version 1.0.0
 */
public class InventoryDuplicationModule extends AbstractModule implements Listener {

    // Envanteri açık olan oyuncular
    private final Map<UUID, Long> openInventories;

    /**
     * InventoryDuplicationModule constructor
     *
     * @param plugin Ana plugin instance
     */
    public InventoryDuplicationModule(@NotNull AtomSMPFixer plugin) {
        super(plugin, "envanter-duplikasyon", "Envanter duplikasyonu önleme");
        this.openInventories = new ConcurrentHashMap<>();
    }

    @Override
    public void onEnable() {
        super.onEnable();

        // Event listener kaydet
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        debug("Modül aktifleştirildi.");
    }

    @Override
    public void onDisable() {
        super.onDisable();

        // Map'i temizle
        openInventories.clear();

        // Event listener'ı kaldır
        BlockBreakEvent.getHandlerList().unregister(this);
        InventoryOpenEvent.getHandlerList().unregister(this);

        debug("Modül devre dışı bırakıldı.");
    }

    /**
     * Envanter açılma olayını dinler
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!isEnabled()) {
            return;
        }

        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }

        UUID uuid = player.getUniqueId();
        openInventories.put(uuid, System.currentTimeMillis());

        debug(player.getName() + " envanterini açtı");
    }

    /**
     * Blok kırma olayını dinler
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!isEnabled()) {
            return;
        }

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        // Oyuncunun envanteri açık mı kontrol et
        Long openTime = openInventories.get(uuid);
        if (openTime != null) {
            long timeSinceOpen = System.currentTimeMillis() - openTime;

            // 50ms içinde blok kırma = şüpheli
            if (timeSinceOpen < 50) {
                incrementBlockedCount();

                logExploit(player.getName(),
                    String.format("Envanter açıkken blok kırma tespit edildi! Timing: %dms", timeSinceOpen));

                event.setCancelled(true);
                player.closeInventory();

                // Oyuncuya mesaj gönder
                player.sendMessage(plugin.getMessageManager()
                    .getMessage("envanter-acikken-blok-kirma"));

                debug(player.getName() + " için blok kırma engellendi (envanter açık)");
            }
        }
    }

    /**
     * Oyuncunun envanterini kapatır
     */
    public void closeInventory(@NotNull UUID uuid) {
        openInventories.remove(uuid);
        debug("Envanter kapatıldı: " + uuid);
    }

    /**
     * Tüm envanter kayıtlarını temizler
     */
    public void clearAll() {
        openInventories.clear();
        debug("Tüm envanter kayıtları temizlendi");
    }

    /**
     * Oyuncunun envanterinin açık olup olmadığını kontrol eder
     */
    public boolean hasOpenInventory(@NotNull UUID uuid) {
        return openInventories.containsKey(uuid);
    }

    /**
     * Memory optimization - kullanılmayan kayıtları temizler
     */
    public void cleanup() {
        long currentTime = System.currentTimeMillis();
        long expiryTime = 10000; // 10 saniye

        openInventories.entrySet().removeIf(entry ->
            currentTime - entry.getValue() > expiryTime);
    }

    /**
     * Modül istatistiklerini döndürür
     */
    public String getStatistics() {
        return String.format("Açık envanter: %d, Engellenen duplikasyon: %d",
            openInventories.size(),
            getBlockedCount());
    }
}

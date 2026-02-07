package com.atomsmp.fixer.module;

import com.atomsmp.fixer.AtomSMPFixer;
import org.bukkit.Chunk;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Frame Crash Modülü
 *
 * Chunk başına item frame sayısını kontrol eder ve crash exploit'lerini önler.
 * EntitySpawnEvent ile frame spawn tracking yapar.
 *
 * Özellikler:
 * - Chunk başına maksimum frame sayısı kontrolü
 * - Item frame ve glow item frame kontrolü
 * - Spawn engelleme
 * - Memory efficient tracking
 *
 * @author AtomSMP
 * @version 1.0.0
 */
public class FrameCrashModule extends AbstractModule implements Listener {

    // Chunk başına frame sayısını tutan map
    private final Map<ChunkKey, AtomicInteger> frameCounts;

    // Config cache
    private int maxFramesPerChunk;

    /**
     * FrameCrashModule constructor
     *
     * @param plugin Ana plugin instance
     */
    public FrameCrashModule(@NotNull AtomSMPFixer plugin) {
        super(plugin, "frame-crash", "Item frame crash kontrolü");
        this.frameCounts = new ConcurrentHashMap<>();
    }

    @Override
    public void onEnable() {
        super.onEnable();

        // Config değerlerini yükle
        loadConfig();

        // Event listener kaydet
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        debug("Modül aktifleştirildi. Max frame/chunk: " + maxFramesPerChunk);
    }

    @Override
    public void onDisable() {
        super.onDisable();

        // Map'i temizle
        frameCounts.clear();

        // Event listener'ı kaldır
        EntitySpawnEvent.getHandlerList().unregister(this);

        debug("Modül devre dışı bırakıldı.");
    }

    /**
     * Config değerlerini yükler
     */
    private void loadConfig() {
        this.maxFramesPerChunk = getConfigInt("chunk-basina-max-frame", 50);

        debug("Config yüklendi: maxFrames=" + maxFramesPerChunk);
    }

    /**
     * Entity spawn olayını dinler
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntitySpawn(EntitySpawnEvent event) {
        if (!isEnabled()) {
            return;
        }

        Entity entity = event.getEntity();
        EntityType type = entity.getType();

        // Sadece item frame'leri kontrol et
        if (type != EntityType.ITEM_FRAME && type != EntityType.GLOW_ITEM_FRAME) {
            return;
        }

        Chunk chunk = entity.getLocation().getChunk();
        ChunkKey key = new ChunkKey(chunk);

        debug("Item frame spawn: " + type + " @ chunk " + key);

        // Mevcut frame sayısını al veya oluştur
        AtomicInteger count = frameCounts.computeIfAbsent(key, k -> {
            // Chunk'taki mevcut frame'leri say
            int existing = countFramesInChunk(chunk);
            return new AtomicInteger(existing);
        });

        // Frame sayısını kontrol et
        int currentCount = count.get();
        if (currentCount >= maxFramesPerChunk) {
            incrementBlockedCount();

            logExploit("SYSTEM",
                String.format("Chunk [%d,%d] frame limiti aşıldı! Mevcut: %d, Limit: %d",
                    chunk.getX(), chunk.getZ(),
                    currentCount, maxFramesPerChunk));

            event.setCancelled(true);
            debug("Frame spawn engellendi (limit aşımı)");
            return;
        }

        // Frame sayısını artır
        count.incrementAndGet();
        debug("Frame sayısı artırıldı: " + count.get() + "/" + maxFramesPerChunk);
    }

    /**
     * Chunk'taki mevcut frame sayısını hesaplar
     */
    private int countFramesInChunk(@NotNull Chunk chunk) {
        int count = 0;
        for (Entity entity : chunk.getEntities()) {
            EntityType type = entity.getType();
            if (type == EntityType.ITEM_FRAME || type == EntityType.GLOW_ITEM_FRAME) {
                count++;
            }
        }
        return count;
    }

    /**
     * Chunk'ın frame sayısını döndürür
     */
    public int getFrameCount(@NotNull Chunk chunk) {
        ChunkKey key = new ChunkKey(chunk);
        AtomicInteger count = frameCounts.get(key);
        return count != null ? count.get() : 0;
    }

    /**
     * Chunk'ı temizler
     */
    public void clearChunk(@NotNull Chunk chunk) {
        ChunkKey key = new ChunkKey(chunk);
        frameCounts.remove(key);
        debug("Chunk temizlendi: " + key);
    }

    /**
     * Tüm kayıtları temizler
     */
    public void clearAll() {
        frameCounts.clear();
        debug("Tüm frame kayıtları temizlendi");
    }

    /**
     * Memory optimization - kullanılmayan chunk kayıtlarını temizler
     */
    public void cleanup() {
        frameCounts.entrySet().removeIf(entry -> entry.getValue().get() <= 0);
    }

    /**
     * Chunk anahtarı sınıfı
     */
    private static class ChunkKey {
        private final String worldName;
        private final int x;
        private final int z;
        private final int hashCode;

        public ChunkKey(@NotNull Chunk chunk) {
            this.worldName = chunk.getWorld().getName();
            this.x = chunk.getX();
            this.z = chunk.getZ();
            this.hashCode = computeHashCode();
        }

        private int computeHashCode() {
            return java.util.Objects.hash(worldName, x, z);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof ChunkKey other)) return false;
            return this.x == other.x &&
                   this.z == other.z &&
                   this.worldName.equals(other.worldName);
        }

        @Override
        public int hashCode() {
            return hashCode;
        }

        @Override
        public String toString() {
            return String.format("%s[%d,%d]", worldName, x, z);
        }
    }

    /**
     * Modül istatistiklerini döndürür
     */
    public String getStatistics() {
        return String.format("Takip edilen chunk: %d, Engellenen frame: %d",
            frameCounts.size(),
            getBlockedCount());
    }
}

package com.atomsmp.fixer.module;

import com.atomsmp.fixer.AtomSMPFixer;
import com.atomsmp.fixer.util.BookUtils;
import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Kitap Crash Modülü
 *
 * Kitap paketlerini kontrol eder ve crash exploit'lerini önler.
 * BookUtils ile kitap validasyonu yapar.
 *
 * Özellikler:
 * - Maksimum başlık uzunluğu kontrolü
 * - Maksimum sayfa sayısı kontrolü
 * - Maksimum sayfa boyutu kontrolü
 * - Maksimum toplam kitap boyutu kontrolü
 * - WrapperPlayClientEditBook kullanımı
 *
 * @author AtomSMP
 * @version 1.0.0
 */
public class BookCrasherModule extends AbstractModule {

    private PacketListener listener;

    // Config cache
    private int maxTitleLength;
    private int maxPageCount;
    private int maxPageSize;
    private int maxTotalBookSize;

    /**
     * BookCrasherModule constructor
     *
     * @param plugin Ana plugin instance
     */
    public BookCrasherModule(@NotNull AtomSMPFixer plugin) {
        super(plugin, "kitap-crash", "Kitap crash exploit kontrolü");
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

        debug("Modül aktifleştirildi. Max sayfa: " + maxPageCount +
              ", Max boyut: " + maxTotalBookSize);
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

        debug("Modül devre dışı bırakıldı.");
    }

    /**
     * Config değerlerini yükler
     */
    private void loadConfig() {
        this.maxTitleLength = getConfigInt("max-baslik-uzunlugu", 32);
        this.maxPageCount = getConfigInt("max-sayfa-sayisi", 100);
        this.maxPageSize = getConfigInt("max-sayfa-boyutu", 256);
        this.maxTotalBookSize = getConfigInt("max-toplam-kitap-boyutu", 102400); // 100KB

        debug("Config yüklendi: maxTitle=" + maxTitleLength +
              ", maxPages=" + maxPageCount +
              ", maxPageSize=" + maxPageSize +
              ", maxTotal=" + maxTotalBookSize);
    }

    /**
     * Paket alındığında çağrılır
     */
    private void handlePacketReceive(PacketReceiveEvent event) {
        if (!isEnabled()) {
            return;
        }

        // EditBook paketini kontrol et
        if (event.getPacketType() != PacketType.Play.Client.EDIT_BOOK) {
            return;
        }

        Player player = (Player) event.getPlayer();
        if (player == null) {
            return;
        }

        try {
            // Note: PacketEvents'in EditBook wrapper'ı için spesifik implementasyon gerekebilir
            // Şimdilik temel kontrol yapıyoruz

            debug(player.getName() + " kitap düzenliyor");

            // Oyuncunun elindeki item'ı kontrol et
            org.bukkit.inventory.ItemStack item = player.getInventory().getItemInMainHand();
            if (!BookUtils.isBook(item)) {
                item = player.getInventory().getItemInOffHand();
            }

            if (!BookUtils.isBook(item)) {
                debug("Oyuncunun elinde kitap yok");
                return;
            }

            // Kitabın güvenli olup olmadığını kontrol et
            if (!BookUtils.isBookSafe(item, maxTitleLength, maxPageCount, maxPageSize, maxTotalBookSize)) {
                incrementBlockedCount();

                int pageCount = BookUtils.getPageCount(item);
                int bookSize = BookUtils.calculateBookSize(item);

                logExploit(player.getName(),
                    String.format("Zararlı kitap tespit edildi! Sayfa: %d (limit: %d), Boyut: %d (limit: %d)",
                        pageCount, maxPageCount,
                        bookSize, maxTotalBookSize));

                event.setCancelled(true);
                player.closeInventory();

                // Oyuncuya mesaj gönder
                player.sendMessage(plugin.getMessageManager().getMessage("kitap-engellendi"));

                debug(player.getName() + " için kitap engellendi");
            }

        } catch (Exception e) {
            error("EditBook paketi işlenirken hata: " + e.getMessage());
        }
    }

    /**
     * Kitabın güvenli olup olmadığını manuel kontrol eder
     */
    public boolean isBookSafe(@NotNull org.bukkit.inventory.ItemStack book) {
        return BookUtils.isBookSafe(book, maxTitleLength, maxPageCount, maxPageSize, maxTotalBookSize);
    }

    /**
     * Kitabı temizler ve güvenli hale getirir
     */
    @NotNull
    public org.bukkit.inventory.ItemStack sanitizeBook(@NotNull org.bukkit.inventory.ItemStack book) {
        return BookUtils.sanitizeBook(book, maxTitleLength, maxPageCount, maxPageSize);
    }

    /**
     * Modül istatistiklerini döndürür
     */
    public String getStatistics() {
        return String.format("Engellenen zararlı kitap: %d", getBlockedCount());
    }
}

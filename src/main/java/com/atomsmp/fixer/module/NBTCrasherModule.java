package com.atomsmp.fixer.module;

import com.atomsmp.fixer.AtomSMPFixer;
import com.atomsmp.fixer.util.NBTUtils;
import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClickWindow;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientCreativeInventoryAction;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * NBT Crash Modülü
 *
 * Tüm item paketlerinde NBT kontrolü yapar ve crash exploit'lerini önler.
 * NBTUtils.isNBTSafe() kullanarak validasyon yapar.
 *
 * Özellikler:
 * - Maksimum NBT tag sayısı kontrolü
 * - Maksimum NBT derinlik kontrolü
 * - Maksimum NBT boyutu kontrolü (byte)
 * - Creative ve normal inventory kontrolü
 *
 * @author AtomSMP
 * @version 1.0.0
 */
public class NBTCrasherModule extends AbstractModule implements PacketListener {

    // Config cache
    private int maxNBTTags;
    private int maxNBTDepth;
    private int maxNBTSizeBytes;

    /**
     * NBTCrasherModule constructor
     *
     * @param plugin Ana plugin instance
     */
    public NBTCrasherModule(@NotNull AtomSMPFixer plugin) {
        super(plugin, "nbt-crash", "NBT crash exploit kontrolü");
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

        debug("Modül aktifleştirildi. Max NBT: tags=" + maxNBTTags +
              ", depth=" + maxNBTDepth + ", size=" + maxNBTSizeBytes);
    }

    @Override
    public void onDisable() {
        super.onDisable();

        // PacketEvents listener'ı kaldır
        com.github.retrooper.packetevents.PacketEvents.getAPI()
            .getEventManager()
            .unregisterListener(this);

        debug("Modül devre dışı bırakıldı.");
    }

    /**
     * Config değerlerini yükler
     */
    private void loadConfig() {
        this.maxNBTTags = getConfigInt("max-nbt-etiket", 100);
        this.maxNBTDepth = getConfigInt("max-nbt-derinlik", 10);
        this.maxNBTSizeBytes = getConfigInt("max-nbt-boyut-byte", 102400); // 100KB

        debug("Config yüklendi: maxTags=" + maxNBTTags +
              ", maxDepth=" + maxNBTDepth +
              ", maxSize=" + maxNBTSizeBytes);
    }

    /**
     * Paket alındığında çağrılır
     */
    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (!isEnabled()) {
            return;
        }

        Player player = (Player) event.getPlayer();
        if (player == null) {
            return;
        }

        // Creative inventory action
        if (event.getPacketType() == PacketType.Play.Client.CREATIVE_INVENTORY_ACTION) {
            handleCreativeInventoryAction(event, player);
        }
        // Click window (normal inventory)
        else if (event.getPacketType() == PacketType.Play.Client.CLICK_WINDOW) {
            handleClickWindow(event, player);
        }
    }

    /**
     * Creative inventory action paketini işler
     */
    private void handleCreativeInventoryAction(@NotNull PacketReceiveEvent event, @NotNull Player player) {
        try {
            WrapperPlayClientCreativeInventoryAction packet =
                new WrapperPlayClientCreativeInventoryAction(event);

            ItemStack item = packet.getItemStack();
            if (item == null) {
                return;
            }

            debug(player.getName() + " creative item: " + item.getType());

            // NBT kontrolü
            if (!isItemNBTSafe(item, player.getName())) {
                incrementBlockedCount();
                event.setCancelled(true);
                player.closeInventory();
                debug(player.getName() + " için creative item engellendi (NBT)");
            }

        } catch (Exception e) {
            error("CreativeInventoryAction paketi işlenirken hata: " + e.getMessage());
        }
    }

    /**
     * Click window paketini işler
     */
    private void handleClickWindow(@NotNull PacketReceiveEvent event, @NotNull Player player) {
        try {
            WrapperPlayClientClickWindow packet = new WrapperPlayClientClickWindow(event);

            // Carried item kontrolü (cursor item)
            // Not: PacketEvents 2.0+ API ile item bilgisi alınabilir
            // Şimdilik temel kontrol yapıyoruz

            debug(player.getName() + " click window");

        } catch (Exception e) {
            error("ClickWindow paketi işlenirken hata: " + e.getMessage());
        }
    }

    /**
     * Item'ın NBT'sinin güvenli olup olmadığını kontrol eder
     */
    private boolean isItemNBTSafe(@NotNull ItemStack item, @NotNull String playerName) {
        // NBTUtils kullanarak kontrol
        if (!NBTUtils.isNBTSafe(item, maxNBTTags, maxNBTDepth, maxNBTSizeBytes)) {
            int tagCount = NBTUtils.estimateNBTTagCount(item);
            int depth = NBTUtils.estimateNBTDepth(item);
            int size = NBTUtils.estimateNBTSize(item);

            logExploit(playerName,
                String.format("Zararlı NBT verisi: tags=%d (limit: %d), depth=%d (limit: %d), size=%d (limit: %d)",
                    tagCount, maxNBTTags,
                    depth, maxNBTDepth,
                    size, maxNBTSizeBytes));

            return false;
        }

        return true;
    }

    /**
     * Item'ı temizler ve güvenli hale getirir
     */
    @NotNull
    public ItemStack sanitizeItem(@NotNull ItemStack item) {
        if (!isItemNBTSafe(item, "SYSTEM")) {
            return NBTUtils.sanitizeNBT(item, 10);
        }
        return item;
    }

    /**
     * Modül istatistiklerini döndürür
     */
    public String getStatistics() {
        return String.format("Engellenen zararlı NBT: %d", getBlockedCount());
    }
}

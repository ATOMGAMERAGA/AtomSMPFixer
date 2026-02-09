package com.atomsmp.fixer.module;

import com.atomsmp.fixer.AtomSMPFixer;
import com.atomsmp.fixer.util.NBTUtils;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClickWindow;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientCreativeInventoryAction;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
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
public class NBTCrasherModule extends AbstractModule {

    private PacketListenerAbstract listener;

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

        debug("Modül aktifleştirildi. Max NBT: tags=" + maxNBTTags +
              ", depth=" + maxNBTDepth + ", size=" + maxNBTSizeBytes);
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
        this.maxNBTTags = getConfigInt("max-nbt-etiket", 1000);
        this.maxNBTDepth = getConfigInt("max-nbt-derinlik", 16);
        this.maxNBTSizeBytes = getConfigInt("max-nbt-boyut-byte", 500000); // 500KB

        debug("Config yüklendi: maxTags=" + maxNBTTags +
              ", maxDepth=" + maxNBTDepth +
              ", maxSize=" + maxNBTSizeBytes);
    }

    /**
     * Paket alındığında çağrılır
     */
    private void handlePacketReceive(PacketReceiveEvent event) {
        if (!isEnabled()) {
            return;
        }

        if (!(event.getPlayer() instanceof Player player)) {
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

            com.github.retrooper.packetevents.protocol.item.ItemStack peItem = packet.getItemStack();
            if (peItem == null || peItem.isEmpty()) {
                return;
            }

            debug(player.getName() + " creative item gönderdi");

            // NBT kontrolü - Paketten gelen item'ı kontrol et
            ItemStack bukkitItem = SpigotConversionUtil.toBukkitItemStack(peItem);
            if (bukkitItem != null && bukkitItem.getType() != org.bukkit.Material.AIR) {
                if (!isItemNBTSafe(bukkitItem, player.getName())) {
                    incrementBlockedCount();
                    event.setCancelled(true);
                    player.closeInventory();
                    debug(player.getName() + " için creative item engellendi (NBT)");
                }
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

            // Cursor item kontrolü
            com.github.retrooper.packetevents.protocol.item.ItemStack peItem = packet.getCarriedItemStack();
            if (peItem == null || peItem.isEmpty()) {
                return;
            }

            // NBT kontrolü
            ItemStack bukkitItem = SpigotConversionUtil.toBukkitItemStack(peItem);
            if (bukkitItem != null && bukkitItem.getType() != org.bukkit.Material.AIR) {
                if (!isItemNBTSafe(bukkitItem, player.getName())) {
                    incrementBlockedCount();
                    event.setCancelled(true);
                    player.closeInventory();
                    debug(player.getName() + " için click window item engellendi (NBT)");
                }
            }

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

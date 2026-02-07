package com.atomsmp.fixer.module;

import com.atomsmp.fixer.AtomSMPFixer;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Lectern;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

/**
 * Kürsü Crash Modülü
 *
 * Lectern (kürsü) etkileşimlerini kontrol eder ve crash exploit'lerini önler.
 * Geçersiz slot etkileşimlerini ve zararlı item yerleştirmelerini engeller.
 *
 * Özellikler:
 * - Geçersiz slot kontrolü
 * - Lectern inventory kontrolü
 * - Slot boundary validation
 * - Crash exploit önleme
 *
 * @author AtomSMP
 * @version 1.0.0
 */
public class LecternCrasherModule extends AbstractModule implements Listener {

    // Lectern inventory boyutu
    private static final int LECTERN_SIZE = 1;

    /**
     * LecternCrasherModule constructor
     *
     * @param plugin Ana plugin instance
     */
    public LecternCrasherModule(@NotNull AtomSMPFixer plugin) {
        super(plugin, "kursu-crash", "Kürsü crash exploit kontrolü");
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

        // Event listener'ı kaldır
        InventoryClickEvent.getHandlerList().unregister(this);

        debug("Modül devre dışı bırakıldı.");
    }

    /**
     * Inventory click olayını dinler
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!isEnabled()) {
            return;
        }

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        Inventory inventory = event.getInventory();
        InventoryHolder holder = inventory.getHolder();

        // Lectern mi kontrol et
        if (!(holder instanceof Lectern)) {
            return;
        }

        int slot = event.getSlot();

        debug(player.getName() + " lectern'e tıkladı. Slot: " + slot);

        // Geçersiz slot kontrolü
        if (slot < 0) {
            incrementBlockedCount();

            logExploit(player.getName(),
                String.format("Negatif lectern slot: %d", slot));

            event.setCancelled(true);
            player.closeInventory();

            debug(player.getName() + " için lectern etkileşimi engellendi (negatif slot)");
            return;
        }

        // Slot boundary kontrolü
        if (slot >= LECTERN_SIZE && event.getClickedInventory() == inventory) {
            incrementBlockedCount();

            logExploit(player.getName(),
                String.format("Geçersiz lectern slot: %d (Max: %d)", slot, LECTERN_SIZE));

            event.setCancelled(true);
            player.closeInventory();

            debug(player.getName() + " için lectern etkileşimi engellendi (slot aşımı)");
            return;
        }

        // Raw slot kontrolü (ek güvenlik)
        int rawSlot = event.getRawSlot();
        if (rawSlot < 0 || rawSlot >= inventory.getSize() + player.getInventory().getSize()) {
            incrementBlockedCount();

            logExploit(player.getName(),
                String.format("Geçersiz raw slot: %d", rawSlot));

            event.setCancelled(true);
            player.closeInventory();

            debug(player.getName() + " için lectern etkileşimi engellendi (raw slot)");
        }
    }

    /**
     * Lectern'in güvenli olup olmadığını kontrol eder
     */
    public boolean isLecternSafe(@NotNull Block block) {
        if (block.getType() != Material.LECTERN) {
            return true;
        }

        if (!(block.getState() instanceof Lectern lectern)) {
            return true;
        }

        // Lectern'deki kitabı kontrol et
        Inventory inventory = lectern.getInventory();
        if (inventory.isEmpty()) {
            return true;
        }

        // Inventory boyutu kontrolü
        return inventory.getSize() == LECTERN_SIZE;
    }

    /**
     * Modül istatistiklerini döndürür
     */
    public String getStatistics() {
        return String.format("Engellenen lectern etkileşimi: %d", getBlockedCount());
    }
}

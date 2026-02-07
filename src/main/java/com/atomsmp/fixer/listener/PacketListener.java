package com.atomsmp.fixer.listener;

import com.atomsmp.fixer.AtomSMPFixer;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import org.jetbrains.annotations.NotNull;

/**
 * PacketEvents ana listener sınıfı
 * Gelen ve giden paketleri dinler ve modüllere iletir
 *
 * @author AtomSMP
 * @version 1.0.0
 */
public class PacketListener extends PacketListenerAbstract {

    private final AtomSMPFixer plugin;

    /**
     * PacketListener constructor
     *
     * @param plugin Ana plugin instance
     */
    public PacketListener(@NotNull AtomSMPFixer plugin) {
        super(PacketListenerPriority.NORMAL);
        this.plugin = plugin;
    }

    /**
     * Gelen paket eventi
     * Client -> Server paketlerini işler
     *
     * @param event Paket alma eventi
     */
    @Override
    public void onPacketReceive(@NotNull PacketReceiveEvent event) {
        // Bypass iznine sahip oyuncuları atla
        if (event.getUser() != null && event.getUser().getUUID() != null) {
            org.bukkit.entity.Player player = plugin.getServer().getPlayer(event.getUser().getUUID());
            if (player != null && player.hasPermission("atomsmpfixer.bypass")) {
                return;
            }
        }

        // Debug modu
        if (plugin.getConfigManager().isDebugEnabled()) {
            plugin.getLogManager().debug("Paket alındı: " + event.getPacketType().getName());
        }

        // Performans izleme başlat
        long startTime = System.nanoTime();

        try {
            // Paket türüne göre işlem yap
            handleIncomingPacket(event);

        } catch (Exception e) {
            plugin.getLogger().severe("Paket işlenirken hata: " + event.getPacketType().getName());
            e.printStackTrace();
        } finally {
            // Performans izleme bitir
            long duration = System.nanoTime() - startTime;
            if (duration > 1_000_000) { // 1ms üzeri
                plugin.getLogManager().debug("Paket işleme süresi: " + (duration / 1_000_000.0) + "ms");
            }
        }
    }

    /**
     * Giden paket eventi
     * Server -> Client paketlerini işler
     *
     * @param event Paket gönderme eventi
     */
    @Override
    public void onPacketSend(@NotNull PacketSendEvent event) {
        // Bypass iznine sahip oyuncuları atla
        if (event.getUser() != null && event.getUser().getUUID() != null) {
            org.bukkit.entity.Player player = plugin.getServer().getPlayer(event.getUser().getUUID());
            if (player != null && player.hasPermission("atomsmpfixer.bypass")) {
                return;
            }
        }

        // Debug modu
        if (plugin.getConfigManager().isDebugEnabled()) {
            plugin.getLogManager().debug("Paket gönderildi: " + event.getPacketType().getName());
        }

        try {
            // Paket türüne göre işlem yap
            handleOutgoingPacket(event);

        } catch (Exception e) {
            plugin.getLogger().severe("Paket gönderilirken hata: " + event.getPacketType().getName());
            e.printStackTrace();
        }
    }

    /**
     * Gelen paketleri işler
     *
     * @param event Paket alma eventi
     */
    private void handleIncomingPacket(@NotNull PacketReceiveEvent event) {
        if (!(event.getPacketType() instanceof PacketType.Play.Client packetType)) {
            return;
        }

        // Burada modül sistemi genişletildiğinde,
        // her modül kendi paket türünü handle edebilir
        // Örnek:
        // - EDIT_BOOK -> BookCrashModule
        // - UPDATE_SIGN -> SignCrashModule
        // - CUSTOM_PAYLOAD -> CustomPayloadModule
        // - vb.

        // Şimdilik temel yapı hazır
        // Modüller eklendiğinde buraya entegre edilecek
    }

    /**
     * Giden paketleri işler
     *
     * @param event Paket gönderme eventi
     */
    private void handleOutgoingPacket(@NotNull PacketSendEvent event) {
        if (!(event.getPacketType() instanceof PacketType.Play.Server packetType)) {
            return;
        }

        // Giden paketler için özel işlemler
        // Örneğin chunk paketlerini izleme, vb.
    }
}

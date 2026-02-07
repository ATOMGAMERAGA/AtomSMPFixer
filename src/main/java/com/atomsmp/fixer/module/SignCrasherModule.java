package com.atomsmp.fixer.module;

import com.atomsmp.fixer.AtomSMPFixer;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientUpdateSign;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

/**
 * Tabela Crash Modülü
 *
 * Tabela düzenleme paketlerini kontrol eder ve crash exploit'lerini önler.
 * WrapperPlayClientUpdateSign kullanarak paket kontrolü yapar.
 *
 * Özellikler:
 * - Maksimum satır uzunluğu kontrolü
 * - Renk kodu temizleme
 * - Özel karakter engelleme
 * - String sanitization
 *
 * @author AtomSMP
 * @version 1.0.0
 */
public class SignCrasherModule extends AbstractModule {

    private PacketListenerAbstract listener;

    // Config cache
    private int maxLineLength;
    private boolean cleanColorCodes;
    private boolean blockSpecialChars;

    // Özel karakter pattern'i
    private static final Pattern SPECIAL_CHARS = Pattern.compile("[\\x00-\\x1F\\x7F]");
    private static final Pattern COLOR_CODES = Pattern.compile("§[0-9a-fk-or]");

    /**
     * SignCrasherModule constructor
     *
     * @param plugin Ana plugin instance
     */
    public SignCrasherModule(@NotNull AtomSMPFixer plugin) {
        super(plugin, "tabela-crash", "Tabela crash exploit kontrolü");
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

        debug("Modül aktifleştirildi. Max satır uzunluğu: " + maxLineLength);
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
        this.maxLineLength = getConfigInt("max-satir-uzunlugu", 80);
        this.cleanColorCodes = getConfigBoolean("renk-kodlarini-temizle", false);
        this.blockSpecialChars = getConfigBoolean("ozel-karakterleri-engelle", true);

        debug("Config yüklendi: maxLength=" + maxLineLength +
              ", cleanColors=" + cleanColorCodes +
              ", blockSpecial=" + blockSpecialChars);
    }

    /**
     * Paket alındığında çağrılır
     */
    private void handlePacketReceive(PacketReceiveEvent event) {
        if (!isEnabled()) {
            return;
        }

        // UpdateSign paketini kontrol et
        if (event.getPacketType() != PacketType.Play.Client.UPDATE_SIGN) {
            return;
        }

        Player player = (Player) event.getPlayer();
        if (player == null) {
            return;
        }

        try {
            WrapperPlayClientUpdateSign packet = new WrapperPlayClientUpdateSign(event);
            String[] lines = packet.getTextLines();

            boolean modified = false;
            String[] newLines = new String[lines.length];

            for (int i = 0; i < lines.length; i++) {
                String line = lines[i];
                String sanitized = sanitizeLine(line);

                newLines[i] = sanitized;

                // Değişiklik oldu mu kontrol et
                if (!line.equals(sanitized)) {
                    modified = true;
                    debug(player.getName() + " tabela satırı temizlendi: " + i);
                }

                // Uzunluk kontrolü
                if (sanitized.length() > maxLineLength) {
                    incrementBlockedCount();

                    logExploit(player.getName(),
                        String.format("Çok uzun tabela satırı: %d karakter (Limit: %d)",
                            sanitized.length(), maxLineLength));

                    // Satırı kısalt
                    newLines[i] = sanitized.substring(0, maxLineLength);
                    modified = true;
                }
            }

            // Eğer değişiklik yapıldıysa paketi güncelle
            if (modified) {
                packet.setTextLines(newLines);
                debug(player.getName() + " tabela paketi güncellendi");
            }

        } catch (Exception e) {
            error("UpdateSign paketi işlenirken hata: " + e.getMessage());
        }
    }

    /**
     * Satırı temizler ve güvenli hale getirir
     */
    @NotNull
    private String sanitizeLine(@NotNull String line) {
        String result = line;

        // Özel karakterleri temizle
        if (blockSpecialChars) {
            result = SPECIAL_CHARS.matcher(result).replaceAll("");
        }

        // Renk kodlarını temizle
        if (cleanColorCodes) {
            result = COLOR_CODES.matcher(result).replaceAll("");
        }

        // Null byte kontrolü
        result = result.replace("\0", "");

        // Unicode replacement character kontrolü
        result = result.replace("\uFFFD", "");

        return result;
    }

    /**
     * Modül istatistiklerini döndürür
     */
    public String getStatistics() {
        return String.format("Engellenen tabela: %d", getBlockedCount());
    }
}

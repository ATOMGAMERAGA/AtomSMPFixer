package com.atomsmp.fixer.module;

import com.atomsmp.fixer.AtomSMPFixer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Komut Crash Modülü
 *
 * Zararlı komutları tespit eder ve engeller.
 * Regex pattern matching kullanarak crash exploit'lerini önler.
 *
 * Özellikler:
 * - Regex tabanlı komut engelleme
 * - Özelleştirilebilir engelleme listesi
 * - Büyük/küçük harf duyarsız kontrol
 * - Wildcard ve pattern desteği
 *
 * @author AtomSMP
 * @version 1.0.0
 */
public class CommandsCrashModule extends AbstractModule implements Listener {

    // Engellenecek komut pattern'lari
    private final List<Pattern> blockedPatterns;

    /**
     * CommandsCrashModule constructor
     *
     * @param plugin Ana plugin instance
     */
    public CommandsCrashModule(@NotNull AtomSMPFixer plugin) {
        super(plugin, "komut-crash", "Zararlı komutları engeller");
        this.blockedPatterns = new ArrayList<>();
    }

    @Override
    public void onEnable() {
        super.onEnable();

        // Config değerlerini yükle
        loadConfig();

        // Event listener kaydet
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        debug("Modül aktifleştirildi. Engellenecek pattern sayısı: " + blockedPatterns.size());
    }

    @Override
    public void onDisable() {
        super.onDisable();

        // Pattern'lari temizle
        blockedPatterns.clear();

        // Event listener'ı kaldır
        PlayerCommandPreprocessEvent.getHandlerList().unregister(this);

        debug("Modül devre dışı bırakıldı.");
    }

    /**
     * Config değerlerini yükler
     */
    private void loadConfig() {
        blockedPatterns.clear();

        List<String> commandList = plugin.getConfigManager()
            .getConfig()
            .getStringList("moduller." + name + ".engellenen-komutlar");

        if (commandList == null || commandList.isEmpty()) {
            // Varsayılan engellenecek komutlar
            commandList = getDefaultBlockedCommands();
        }

        // Regex pattern'lari oluştur
        for (String cmd : commandList) {
            try {
                Pattern pattern = Pattern.compile(cmd, Pattern.CASE_INSENSITIVE);
                blockedPatterns.add(pattern);
                debug("Pattern eklendi: " + cmd);
            } catch (PatternSyntaxException e) {
                error("Geçersiz regex pattern: " + cmd + " - " + e.getMessage());
            }
        }

        debug("Config yüklendi: " + blockedPatterns.size() + " pattern");
    }

    /**
     * Varsayılan engellenecek komutları döndürür
     */
    @NotNull
    private List<String> getDefaultBlockedCommands() {
        List<String> defaults = new ArrayList<>();

        // Selector exploit'leri — çift parametreli selector'lar
        defaults.add(".*@[aeprs]\\[.*distance=.*,.*distance=.*\\].*");
        defaults.add(".*@[aeprs]\\[.*type=.*,.*type=.*\\].*");

        // Uzun komutlar (crash) — 2000+ karakter
        defaults.add(".{2000,}");

        // Aşırı entity selector — 10+ selector
        defaults.add(".*(@[aeprs]\\[.*\\]){10,}.*");

        // Derin recursive execute — 6+ iç içe execute (normal kullanım 1-3)
        defaults.add(".*/execute(\\s+.*\\s+execute){5,}.*");

        return defaults;
    }

    /**
     * Komut işleme olayını dinler
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
        if (!isEnabled()) {
            return;
        }

        Player player = event.getPlayer();
        String command = event.getMessage();

        debug(player.getName() + " komutu: " + command);

        // Her pattern'i kontrol et
        for (Pattern pattern : blockedPatterns) {
            if (pattern.matcher(command).matches()) {
                // Engellenmesi gereken komut
                incrementBlockedCount();

                logExploit(player.getName(),
                    String.format("Engellenmiş komut kullanımı: %s (Pattern: %s)",
                        command.length() > 100 ? command.substring(0, 100) + "..." : command,
                        pattern.pattern()));

                event.setCancelled(true);

                // Oyuncuya mesaj gönder
                player.sendMessage(plugin.getMessageManager().getMessage("komut-engellendi"));

                debug(player.getName() + " için komut engellendi (pattern match)");
                return;
            }
        }

        // Uzunluk kontrolü (ek güvenlik) — Minecraft vanilya limiti 32500 karakter
        if (command.length() > 10000) {
            incrementBlockedCount();

            logExploit(player.getName(),
                String.format("Çok uzun komut: %d karakter (Limit: 10000)", command.length()));

            event.setCancelled(true);
            player.sendMessage(plugin.getMessageManager().getMessage("komut-cok-uzun"));

            debug(player.getName() + " için komut engellendi (çok uzun)");
        }
    }

    /**
     * Yeni pattern ekler
     */
    public void addPattern(@NotNull String regex) {
        try {
            Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
            blockedPatterns.add(pattern);
            info("Pattern eklendi: " + regex);
        } catch (PatternSyntaxException e) {
            error("Geçersiz regex pattern: " + regex + " - " + e.getMessage());
        }
    }

    /**
     * Pattern kaldırır
     */
    public void removePattern(int index) {
        if (index >= 0 && index < blockedPatterns.size()) {
            Pattern removed = blockedPatterns.remove(index);
            info("Pattern kaldırıldı: " + removed.pattern());
        }
    }

    /**
     * Tüm pattern'lari döndürür
     */
    @NotNull
    public List<String> getPatterns() {
        List<String> patterns = new ArrayList<>();
        for (Pattern pattern : blockedPatterns) {
            patterns.add(pattern.pattern());
        }
        return patterns;
    }

    /**
     * Modül istatistiklerini döndürür
     */
    public String getStatistics() {
        return String.format("Pattern sayısı: %d, Engellenen komut: %d",
            blockedPatterns.size(),
            getBlockedCount());
    }
}

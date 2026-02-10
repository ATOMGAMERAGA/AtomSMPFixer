package com.atomsmp.fixer.command;

import com.atomsmp.fixer.AtomSMPFixer;
import com.atomsmp.fixer.manager.StatisticsManager;
import com.atomsmp.fixer.module.AbstractModule;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * /atomfix komutu executor sınıfı
 * Reload, status, toggle ve info alt komutlarını işler
 *
 * @author AtomSMP
 * @version 1.0.0
 */
public class AtomFixCommand implements CommandExecutor {

    private final AtomSMPFixer plugin;

    /**
     * AtomFixCommand constructor
     *
     * @param plugin Ana plugin instance
     */
    public AtomFixCommand(@NotNull AtomSMPFixer plugin) {
        this.plugin = plugin;
    }

    /**
     * Komut çalıştırıldığında
     *
     * @param sender Komut gönderen
     * @param command Komut
     * @param label Komut label
     * @param args Komut argümanları
     * @return Başarılı ise true
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        // İzin kontrolü
        if (!sender.hasPermission("atomsmpfixer.admin")) {
            plugin.getMessageManager().sendMessage(sender, "genel.izin-yok");
            return true;
        }

        // Argüman yoksa info göster
        if (args.length == 0) {
            showInfo(sender);
            return true;
        }

        // Alt komutları işle
        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "reload" -> handleReload(sender);
            case "status" -> handleStatus(sender);
            case "toggle" -> handleToggle(sender, args);
            case "stats" -> handleStats(sender);
            case "info" -> showInfo(sender);
            default -> plugin.getMessageManager().sendMessage(sender, "genel.bilinmeyen-komut");
        }

        return true;
    }

    /**
     * Reload alt komutu
     *
     * @param sender Komut gönderen
     */
    private void handleReload(@NotNull CommandSender sender) {
        // İzin kontrolü
        if (!sender.hasPermission("atomsmpfixer.reload")) {
            plugin.getMessageManager().sendMessage(sender, "genel.izin-yok");
            return;
        }

        long startTime = System.currentTimeMillis();

        // Config'i yeniden yükle
        plugin.getConfigManager().reload();

        // Message cache'i temizle
        plugin.getMessageManager().clearCache();

        // Modülleri yeniden yükle
        plugin.getModuleManager().reloadModules();

        long duration = System.currentTimeMillis() - startTime;

        // Başarı mesajı
        plugin.getMessageManager().sendPrefixedMessage(sender, "genel.yeniden-yuklendi");
        sender.sendMessage(Component.text("Yükleme süresi: " + duration + "ms", NamedTextColor.GRAY));

        // Log
        plugin.getLogManager().info("Config yeniden yüklendi. (" + sender.getName() + ")");
    }

    /**
     * Status alt komutu
     *
     * @param sender Komut gönderen
     */
    private void handleStatus(@NotNull CommandSender sender) {
        // Başlık
        plugin.getMessageManager().sendMessage(sender, "durum.baslik");

        // Modül durumları
        for (AbstractModule module : plugin.getModuleManager().getAllModules()) {
            String durumYolu = module.isEnabled() ? "durum.acik" : "durum.kapali";
            Component durum = plugin.getMessageManager().getMessage(durumYolu);

            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("modul", module.getName());
            placeholders.put("durum", ""); // Component olarak ekleneceği için boş

            // Modül satırı
            String modulSatir = "  <gray>• <white>" + module.getName() + ": ";
            Component satirComponent = plugin.getMessageManager().parse(modulSatir).append(durum);

            // Engelleme sayısı
            if (module.getBlockedCount() > 0) {
                satirComponent = satirComponent.append(
                    Component.text(" (" + module.getBlockedCount() + " engelleme)", NamedTextColor.YELLOW)
                );
            }

            sender.sendMessage(satirComponent);
        }

        // İstatistikler
        Map<String, String> stats = new HashMap<>();
        stats.put("sayi", String.valueOf(plugin.getModuleManager().getTotalBlockedCount()));
        plugin.getMessageManager().sendMessage(sender, "durum.istatistik", stats);

        // TPS bilgisi
        double tps = plugin.getServer().getTPS()[0];
        Map<String, String> tpsMap = new HashMap<>();
        tpsMap.put("tps", String.format("%.2f", tps));
        plugin.getMessageManager().sendMessage(sender, "durum.tps", tpsMap);

        // Bellek kullanımı
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024;
        Map<String, String> memoryMap = new HashMap<>();
        memoryMap.put("bellek", String.valueOf(usedMemory));
        plugin.getMessageManager().sendMessage(sender, "durum.bellek", memoryMap);

        // Alt bilgi
        plugin.getMessageManager().sendMessage(sender, "durum.alt-bilgi");
    }

    /**
     * Toggle alt komutu
     *
     * @param sender Komut gönderen
     * @param args Komut argümanları
     */
    private void handleToggle(@NotNull CommandSender sender, @NotNull String[] args) {
        // Argüman kontrolü
        if (args.length < 2) {
            plugin.getMessageManager().sendMessage(sender, "toggle.kullanim");

            // Mevcut modülleri listele
            plugin.getMessageManager().sendMessage(sender, "toggle.liste-baslik");
            for (String moduleName : plugin.getModuleManager().getModuleNames()) {
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("modul", moduleName);
                plugin.getMessageManager().sendMessage(sender, "toggle.liste-satir", placeholders);
            }
            return;
        }

        String moduleName = args[1];

        // Modül var mı kontrol et
        if (!plugin.getModuleManager().hasModule(moduleName)) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("modul", moduleName);
            plugin.getMessageManager().sendMessage(sender, "genel.modul-bulunamadi", placeholders);
            return;
        }

        // Toggle
        boolean newState = plugin.getModuleManager().toggleModule(moduleName);

        // Mesaj gönder
        String messageKey = newState ? "genel.modul-acildi" : "genel.modul-kapandi";
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("modul", moduleName);
        plugin.getMessageManager().sendPrefixedMessage(sender, messageKey, placeholders);

        // Log
        plugin.getLogManager().info(moduleName + " modülü " + (newState ? "açıldı" : "kapatıldı") + ". (" + sender.getName() + ")");
    }

    /**
     * Stats alt komutu - detayli istatistik goruntuleme
     *
     * @param sender Komut gonderen
     */
    private void handleStats(@NotNull CommandSender sender) {
        StatisticsManager stats = plugin.getStatisticsManager();

        // Baslik
        sender.sendMessage(plugin.getMessageManager().parse(
                "<gradient:#00d4ff:#00ff88><bold>═══ AtomSMPFixer İstatistikler ═══</bold></gradient>"));

        if (stats == null || !stats.isEnabled()) {
            sender.sendMessage(Component.text("İstatistik sistemi devre dışı.", NamedTextColor.RED));
            return;
        }

        // Toplam engelleme (tum zamanlar)
        sender.sendMessage(Component.text("Toplam Engelleme (Tüm Zamanlar): ", NamedTextColor.GRAY)
                .append(Component.text(String.valueOf(stats.getTotalBlockedAllTime()), NamedTextColor.WHITE)));

        // Toplam engelleme (bu oturum)
        sender.sendMessage(Component.text("Toplam Engelleme (Bu Oturum): ", NamedTextColor.GRAY)
                .append(Component.text(String.valueOf(plugin.getModuleManager().getTotalBlockedCount()), NamedTextColor.WHITE)));

        // Modul bazli istatistikler
        sender.sendMessage(Component.text(""));
        sender.sendMessage(Component.text("Modül Bazlı İstatistikler:", NamedTextColor.AQUA));

        Map<String, Long> moduleTotals = stats.getAllModuleTotals();
        moduleTotals.entrySet().stream()
                .filter(e -> e.getValue() > 0)
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(15)
                .forEach(entry -> {
                    long todayCount = stats.getModuleBlockedToday(entry.getKey());
                    sender.sendMessage(Component.text("  • " + entry.getKey() + ": ", NamedTextColor.GRAY)
                            .append(Component.text(entry.getValue() + " toplam", NamedTextColor.WHITE))
                            .append(Component.text(" (" + todayCount + " bugün)", NamedTextColor.YELLOW)));
                });

        // Saldiri gecmisi
        List<StatisticsManager.AttackRecord> attacks = stats.getAttackHistory();
        sender.sendMessage(Component.text(""));
        sender.sendMessage(Component.text("Saldırı Geçmişi: ", NamedTextColor.AQUA)
                .append(Component.text(attacks.size() + " kayıt", NamedTextColor.WHITE)));

        int displayLimit = Math.min(attacks.size(), 5);
        for (int i = 0; i < displayLimit; i++) {
            StatisticsManager.AttackRecord attack = attacks.get(i);
            sender.sendMessage(Component.text("  • " + attack.date + ": ", NamedTextColor.GRAY)
                    .append(Component.text(attack.getDurationSeconds() + "sn", NamedTextColor.WHITE))
                    .append(Component.text(" | Peak: " + attack.peakConnectionRate + "/sn", NamedTextColor.RED))
                    .append(Component.text(" | Engel: " + attack.blockedCount, NamedTextColor.YELLOW)));
        }

        // Attack mode durumu
        if (plugin.getAttackModeManager().isAttackMode()) {
            sender.sendMessage(Component.text(""));
            sender.sendMessage(Component.text("⚠ SALDIRI MODU AKTİF!", NamedTextColor.RED));
            sender.sendMessage(Component.text("  Bağlantı hızı: " + plugin.getAttackModeManager().getCurrentRate() + "/sn", NamedTextColor.YELLOW));
            sender.sendMessage(Component.text("  Doğrulanmış IP: " + plugin.getAttackModeManager().getVerifiedIpCount(), NamedTextColor.GRAY));
        }

        sender.sendMessage(plugin.getMessageManager().parse(
                "<gradient:#00d4ff:#00ff88><bold>══════════════════════════════════</bold></gradient>"));
    }

    /**
     * Info alt komutu
     *
     * @param sender Komut gönderen
     */
    private void showInfo(@NotNull CommandSender sender) {
        // Başlık
        plugin.getMessageManager().sendMessage(sender, "info.baslik");

        // Versiyon
        Map<String, String> versionMap = new HashMap<>();
        versionMap.put("versiyon", plugin.getDescription().getVersion());
        plugin.getMessageManager().sendMessage(sender, "info.versiyon", versionMap);

        // Geliştirici
        plugin.getMessageManager().sendMessage(sender, "info.gelistirici");

        // Paper versiyon
        Map<String, String> paperMap = new HashMap<>();
        paperMap.put("paper", plugin.getServer().getMinecraftVersion());
        plugin.getMessageManager().sendMessage(sender, "info.paper-versiyon", paperMap);

        // PacketEvents versiyon
        Map<String, String> peMap = new HashMap<>();
        peMap.put("packetevents", com.github.retrooper.packetevents.PacketEvents.getAPI().getVersion().toString());
        plugin.getMessageManager().sendMessage(sender, "info.packetevents-versiyon", peMap);

        // Java versiyon
        Map<String, String> javaMap = new HashMap<>();
        javaMap.put("java", System.getProperty("java.version"));
        plugin.getMessageManager().sendMessage(sender, "info.java-versiyon", javaMap);

        // Aktif modül
        Map<String, String> moduleMap = new HashMap<>();
        moduleMap.put("aktif", String.valueOf(plugin.getModuleManager().getEnabledModuleCount()));
        moduleMap.put("toplam", String.valueOf(plugin.getModuleManager().getTotalModuleCount()));
        plugin.getMessageManager().sendMessage(sender, "info.aktif-modul", moduleMap);

        // Komutlar
        plugin.getMessageManager().sendMessage(sender, "info.komutlar");

        // Verified player cache info
        if (plugin.getVerifiedPlayerCache() != null && plugin.getVerifiedPlayerCache().isEnabled()) {
            Map<String, String> cacheMap = new HashMap<>();
            cacheMap.put("sayi", String.valueOf(plugin.getVerifiedPlayerCache().getCacheSize()));
            plugin.getMessageManager().sendMessage(sender, "info.dogrulanmis-onbellek", cacheMap);
        }
    }
}

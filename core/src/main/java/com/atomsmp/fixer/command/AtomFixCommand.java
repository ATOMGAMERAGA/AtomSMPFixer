package com.atomsmp.fixer.command;

import com.atomsmp.fixer.AtomSMPFixer;
import com.atomsmp.fixer.manager.StatisticsManager;
import com.atomsmp.fixer.module.AbstractModule;
import com.atomsmp.fixer.reputation.IPReputationManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * /atomfix komutu executor sınıfı
 *
 * @author AtomSMP
 * @version 3.4.1
 */
public class AtomFixCommand implements CommandExecutor {

    private final AtomSMPFixer plugin;

    public AtomFixCommand(@NotNull AtomSMPFixer plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("atomsmpfixer.admin")) {
            plugin.getMessageManager().sendMessage(sender, "genel.izin-yok");
            return true;
        }

        if (args.length == 0) {
            showInfo(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "reload" -> handleReload(sender);
            case "status" -> handleStatus(sender);
            case "toggle" -> handleToggle(sender, args);
            case "stats" -> handleStats(sender);
            case "info" -> showInfo(sender);
            case "antivpn" -> handleAntiVpn(sender, args);
            case "antibot" -> handleAntiBot(sender, args);
            default -> plugin.getMessageManager().sendMessage(sender, "genel.bilinmeyen-komut");
        }

        return true;
    }

    // ── Reload ──

    private void handleReload(@NotNull CommandSender sender) {
        if (!sender.hasPermission("atomsmpfixer.reload")) {
            plugin.getMessageManager().sendMessage(sender, "genel.izin-yok");
            return;
        }

        long startTime = System.currentTimeMillis();
        plugin.getConfigManager().reload();
        plugin.getMessageManager().clearCache();
        plugin.getModuleManager().reloadModules();
        long duration = System.currentTimeMillis() - startTime;

        plugin.getMessageManager().sendPrefixedMessage(sender, "genel.yeniden-yuklendi");
        sender.sendMessage(Component.text("Yükleme süresi: " + duration + "ms", NamedTextColor.GRAY));
        plugin.getLogManager().info("Config yeniden yüklendi. (" + sender.getName() + ")");
    }

    // ── Status ──

    private void handleStatus(@NotNull CommandSender sender) {
        plugin.getMessageManager().sendMessage(sender, "durum.baslik");

        for (AbstractModule module : plugin.getModuleManager().getAllModules()) {
            String durumYolu = module.isEnabled() ? "durum.acik" : "durum.kapali";
            Component durum = plugin.getMessageManager().getMessage(durumYolu);

            String modulSatir = "  <gray>• <white>" + module.getName() + ": ";
            Component satirComponent = plugin.getMessageManager().parse(modulSatir).append(durum);

            if (module.getBlockedCount() > 0) {
                satirComponent = satirComponent.append(
                    Component.text(" (" + module.getBlockedCount() + " engelleme)", NamedTextColor.YELLOW));
            }
            sender.sendMessage(satirComponent);
        }

        Map<String, String> stats = new HashMap<>();
        stats.put("sayi", String.valueOf(plugin.getModuleManager().getTotalBlockedCount()));
        plugin.getMessageManager().sendMessage(sender, "durum.istatistik", stats);

        double tps = plugin.getServer().getTPS()[0];
        Map<String, String> tpsMap = new HashMap<>();
        tpsMap.put("tps", String.format("%.2f", tps));
        plugin.getMessageManager().sendMessage(sender, "durum.tps", tpsMap);

        Runtime runtime = Runtime.getRuntime();
        long usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024;
        Map<String, String> memoryMap = new HashMap<>();
        memoryMap.put("bellek", String.valueOf(usedMemory));
        plugin.getMessageManager().sendMessage(sender, "durum.bellek", memoryMap);

        plugin.getMessageManager().sendMessage(sender, "durum.alt-bilgi");
    }

    // ── Toggle ──

    private void handleToggle(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length < 2) {
            plugin.getMessageManager().sendMessage(sender, "toggle.kullanim");
            plugin.getMessageManager().sendMessage(sender, "toggle.liste-baslik");
            for (String moduleName : plugin.getModuleManager().getModuleNames()) {
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("modul", moduleName);
                plugin.getMessageManager().sendMessage(sender, "toggle.liste-satir", placeholders);
            }
            return;
        }

        String moduleName = args[1];
        if (!plugin.getModuleManager().hasModule(moduleName)) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("modul", moduleName);
            plugin.getMessageManager().sendMessage(sender, "genel.modul-bulunamadi", placeholders);
            return;
        }

        boolean newState = plugin.getModuleManager().toggleModule(moduleName);
        String messageKey = newState ? "genel.modul-acildi" : "genel.modul-kapandi";
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("modul", moduleName);
        plugin.getMessageManager().sendPrefixedMessage(sender, messageKey, placeholders);
        plugin.getLogManager().info(moduleName + " modülü " + (newState ? "açıldı" : "kapatıldı") + ". (" + sender.getName() + ")");
    }

    // ── Stats ──

    private void handleStats(@NotNull CommandSender sender) {
        StatisticsManager stats = plugin.getStatisticsManager();

        sender.sendMessage(plugin.getMessageManager().parse(
                "<gradient:#00d4ff:#00ff88><bold>═══ AtomSMPFixer İstatistikler ═══</bold></gradient>"));

        if (stats == null || !stats.isEnabled()) {
            sender.sendMessage(Component.text("İstatistik sistemi devre dışı.", NamedTextColor.RED));
            return;
        }

        sender.sendMessage(Component.text("Toplam Engelleme (Tüm Zamanlar): ", NamedTextColor.GRAY)
                .append(Component.text(String.valueOf(stats.getTotalBlockedAllTime()), NamedTextColor.WHITE)));
        sender.sendMessage(Component.text("Toplam Engelleme (Bu Oturum): ", NamedTextColor.GRAY)
                .append(Component.text(String.valueOf(plugin.getModuleManager().getTotalBlockedCount()), NamedTextColor.WHITE)));

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

        if (plugin.getAttackModeManager().isAttackMode()) {
            sender.sendMessage(Component.text(""));
            sender.sendMessage(Component.text("SALDIRI MODU AKTIF!", NamedTextColor.RED).decorate(TextDecoration.BOLD));
            sender.sendMessage(Component.text("  Bağlantı hızı: " + plugin.getAttackModeManager().getCurrentRate() + "/sn", NamedTextColor.YELLOW));
            sender.sendMessage(Component.text("  Doğrulanmış IP: " + plugin.getAttackModeManager().getVerifiedIpCount(), NamedTextColor.GRAY));
        }

        sender.sendMessage(plugin.getMessageManager().parse(
                "<gradient:#00d4ff:#00ff88><bold>══════════════════════════════════</bold></gradient>"));
    }

    // ═══════════════════════════════════════════════════
    //  Anti-VPN Komutu — /atomfix antivpn <alt_komut>
    // ═══════════════════════════════════════════════════

    private void handleAntiVpn(@NotNull CommandSender sender, @NotNull String[] args) {
        IPReputationManager rep = plugin.getReputationManager();

        if (args.length < 2) {
            showAntiVpnHelp(sender);
            return;
        }

        String sub = args[1].toLowerCase();

        switch (sub) {
            case "stats" -> showAntiVpnStats(sender, rep);
            case "refresh" -> {
                if (rep.isRefreshing()) {
                    sender.sendMessage(Component.text("Zaten bir indirme işlemi devam ediyor!", NamedTextColor.RED));
                } else {
                    sender.sendMessage(Component.text("Proxy listeleri yeniden indiriliyor...", NamedTextColor.YELLOW));
                    rep.refreshProxyList();
                    sender.sendMessage(Component.text("Arka planda indirme başlatıldı.", NamedTextColor.GREEN));
                }
            }
            case "check" -> {
                if (args.length < 3) {
                    sender.sendMessage(Component.text("Kullanım: /atomfix antivpn check <IP>", NamedTextColor.RED));
                    return;
                }
                handleIpCheck(sender, rep, args[2]);
            }
            case "add" -> {
                if (args.length < 3) {
                    sender.sendMessage(Component.text("Kullanım: /atomfix antivpn add <IP>", NamedTextColor.RED));
                    return;
                }
                handleIpAdd(sender, rep, args[2]);
            }
            case "remove" -> {
                if (args.length < 3) {
                    sender.sendMessage(Component.text("Kullanım: /atomfix antivpn remove <IP>", NamedTextColor.RED));
                    return;
                }
                handleIpRemove(sender, rep, args[2]);
            }
            case "whitelist" -> {
                if (args.length < 3) {
                    sender.sendMessage(Component.text("Kullanım: /atomfix antivpn whitelist <add|remove> <IP>", NamedTextColor.RED));
                    return;
                }
                handleWhitelist(sender, rep, args);
            }
            case "recent" -> showRecentBlocks(sender, rep);
            default -> showAntiVpnHelp(sender);
        }
    }

    private void showAntiVpnHelp(@NotNull CommandSender sender) {
        sender.sendMessage(plugin.getMessageManager().parse(
                "<gradient:#ff6b6b:#ffa500><bold>═══ Anti-VPN Komutları ═══</bold></gradient>"));
        sender.sendMessage(Component.text(""));
        sender.sendMessage(Component.text("/atomfix antivpn stats", NamedTextColor.AQUA)
                .append(Component.text(" — Detaylı istatistikler", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/atomfix antivpn refresh", NamedTextColor.AQUA)
                .append(Component.text(" — Proxy listelerini yenile", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/atomfix antivpn check <IP>", NamedTextColor.AQUA)
                .append(Component.text(" — IP'yi tüm katmanlarda kontrol et", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/atomfix antivpn add <IP>", NamedTextColor.AQUA)
                .append(Component.text(" — Manuel kara listeye ekle", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/atomfix antivpn remove <IP>", NamedTextColor.AQUA)
                .append(Component.text(" — Manuel kara listeden kaldır", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/atomfix antivpn whitelist <add|remove> <IP>", NamedTextColor.AQUA)
                .append(Component.text(" — Beyaz liste yönetimi", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/atomfix antivpn recent", NamedTextColor.AQUA)
                .append(Component.text(" — Son engellenen IP'ler", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text(""));
        sender.sendMessage(plugin.getMessageManager().parse(
                "<gradient:#ff6b6b:#ffa500><bold>═════════════════════════</bold></gradient>"));
    }

    private void showAntiVpnStats(@NotNull CommandSender sender, @NotNull IPReputationManager rep) {
        sender.sendMessage(plugin.getMessageManager().parse(
                "<gradient:#ff6b6b:#ffa500><bold>═══ Anti-VPN Detaylı İstatistikler ═══</bold></gradient>"));

        sender.sendMessage(Component.text("Sistem: ", NamedTextColor.GRAY)
                .append(Component.text(rep.isEnabled() ? "AKTIF" : "DEVRE DISI",
                        rep.isEnabled() ? NamedTextColor.GREEN : NamedTextColor.RED)));

        sender.sendMessage(Component.text(""));
        sender.sendMessage(Component.text("Veritabanı:", NamedTextColor.AQUA).decorate(TextDecoration.BOLD));
        sender.sendMessage(Component.text("  Proxy Listesi: ", NamedTextColor.GRAY)
                .append(Component.text(formatNumber(rep.getProxyListSize()) + " IP", NamedTextColor.WHITE)));
        sender.sendMessage(Component.text("  Manuel Kara Liste: ", NamedTextColor.GRAY)
                .append(Component.text(rep.getManualBlocklistSize() + " IP", NamedTextColor.WHITE)));
        sender.sendMessage(Component.text("  API Önbellek: ", NamedTextColor.GRAY)
                .append(Component.text(rep.getApiCacheSize() + " kayıt", NamedTextColor.WHITE)));
        sender.sendMessage(Component.text("  CIDR Aralıkları: ", NamedTextColor.GRAY)
                .append(Component.text(rep.getBlockedCidrCount() + " aralık", NamedTextColor.WHITE)));
        sender.sendMessage(Component.text("  Engellenen ASN: ", NamedTextColor.GRAY)
                .append(Component.text(rep.getBlockedAsnCount() + " ASN", NamedTextColor.WHITE)));
        sender.sendMessage(Component.text("  Beyaz Liste: ", NamedTextColor.GRAY)
                .append(Component.text(rep.getWhitelistedIpCount() + " IP, " + rep.getWhitelistedPlayerCount() + " oyuncu", NamedTextColor.WHITE)));

        sender.sendMessage(Component.text(""));
        sender.sendMessage(Component.text("Engelleme İstatistikleri:", NamedTextColor.AQUA).decorate(TextDecoration.BOLD));
        sender.sendMessage(Component.text("  Toplam Kontrol: ", NamedTextColor.GRAY)
                .append(Component.text(formatNumber(rep.getTotalCheckCount()), NamedTextColor.WHITE)));
        sender.sendMessage(Component.text("  Toplam Engelleme: ", NamedTextColor.GRAY)
                .append(Component.text(formatNumber(rep.getTotalBlockCount()), NamedTextColor.RED)));
        sender.sendMessage(Component.text("  Proxy Listesi Engelleme: ", NamedTextColor.GRAY)
                .append(Component.text(formatNumber(rep.getProxyListBlockCount()), NamedTextColor.RED)));
        sender.sendMessage(Component.text("  API Engelleme: ", NamedTextColor.GRAY)
                .append(Component.text(String.valueOf(rep.getApiBlockCount()), NamedTextColor.RED)));
        sender.sendMessage(Component.text("  Manuel Engelleme: ", NamedTextColor.GRAY)
                .append(Component.text(String.valueOf(rep.getManualBlockCount()), NamedTextColor.RED)));
        sender.sendMessage(Component.text("  CIDR Engelleme: ", NamedTextColor.GRAY)
                .append(Component.text(String.valueOf(rep.getCidrBlockCount()), NamedTextColor.RED)));
        sender.sendMessage(Component.text("  Beyaz Liste Geçiş: ", NamedTextColor.GRAY)
                .append(Component.text(String.valueOf(rep.getWhitelistPassCount()), NamedTextColor.GREEN)));

        // Son yenileme
        long lastRefresh = rep.getLastRefreshTime();
        String refreshStr = lastRefresh > 0
                ? new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(lastRefresh))
                : "Henüz yenilenmedi";
        sender.sendMessage(Component.text(""));
        sender.sendMessage(Component.text("Son Yenileme: ", NamedTextColor.GRAY)
                .append(Component.text(refreshStr, NamedTextColor.WHITE)));
        sender.sendMessage(Component.text("Yenileme Durumu: ", NamedTextColor.GRAY)
                .append(Component.text(rep.isRefreshing() ? "DEVAM EDIYOR..." : "Boşta",
                        rep.isRefreshing() ? NamedTextColor.YELLOW : NamedTextColor.GREEN)));

        sender.sendMessage(plugin.getMessageManager().parse(
                "<gradient:#ff6b6b:#ffa500><bold>═══════════════════════════════════</bold></gradient>"));
    }

    private void handleIpCheck(@NotNull CommandSender sender, @NotNull IPReputationManager rep, @NotNull String ip) {
        sender.sendMessage(Component.text("IP kontrol ediliyor: " + ip + "...", NamedTextColor.YELLOW));

        IPReputationManager.CheckDetail detail = rep.checkIpDetailed(ip);

        sender.sendMessage(plugin.getMessageManager().parse(
                "<gradient:#ff6b6b:#ffa500><bold>═══ IP Kontrol Sonucu ═══</bold></gradient>"));
        sender.sendMessage(Component.text("IP: ", NamedTextColor.GRAY)
                .append(Component.text(ip, NamedTextColor.WHITE)));

        // Katman sonuçları
        sender.sendMessage(Component.text(""));
        sender.sendMessage(Component.text("Katman Sonuçları:", NamedTextColor.AQUA));
        sender.sendMessage(statusLine("Beyaz Liste", !detail.whitelisted, detail.whitelisted ? "MUAF" : "Hayır"));
        sender.sendMessage(statusLine("Manuel Kara Liste", detail.manualBlocked, detail.manualBlocked ? "ENGELLENDI" : "Temiz"));
        sender.sendMessage(statusLine("Proxy Listesi", detail.proxyListed, detail.proxyListed ? "BULUNDU" : "Temiz"));
        sender.sendMessage(statusLine("CIDR Engelleme", detail.cidrBlocked, detail.cidrBlocked ? "ENGELLENDI" : "Temiz"));

        if (detail.apiResult != null) {
            IPReputationManager.ReputationResult api = detail.apiResult;
            sender.sendMessage(statusLine("API Kontrolü", api.isBlocked,
                    api.isBlocked ? "ENGELLENDI" : "Temiz"));
            sender.sendMessage(Component.text("  Risk: ", NamedTextColor.GRAY)
                    .append(Component.text(api.riskScore + "/100", riskColor(api.riskScore))));
            sender.sendMessage(Component.text("  Tip: ", NamedTextColor.GRAY)
                    .append(Component.text(api.type, NamedTextColor.WHITE)));
            sender.sendMessage(Component.text("  Ülke: ", NamedTextColor.GRAY)
                    .append(Component.text(api.country, NamedTextColor.WHITE)));
            sender.sendMessage(Component.text("  ASN: ", NamedTextColor.GRAY)
                    .append(Component.text(api.asn, NamedTextColor.WHITE)));
        } else {
            sender.sendMessage(Component.text("  API: ", NamedTextColor.GRAY)
                    .append(Component.text("Önbellekte kayıt yok", NamedTextColor.YELLOW)));
        }

        sender.sendMessage(Component.text(""));
        sender.sendMessage(Component.text("Genel Sonuç: ", NamedTextColor.GRAY)
                .append(Component.text(detail.isBlocked() ? "ENGELLENIR" : "IZIN VERILIR",
                        detail.isBlocked() ? NamedTextColor.RED : NamedTextColor.GREEN)
                        .decorate(TextDecoration.BOLD)));

        sender.sendMessage(plugin.getMessageManager().parse(
                "<gradient:#ff6b6b:#ffa500><bold>═════════════════════════</bold></gradient>"));
    }

    private void handleIpAdd(@NotNull CommandSender sender, @NotNull IPReputationManager rep, @NotNull String ip) {
        if (rep.addToManualBlocklist(ip)) {
            sender.sendMessage(Component.text(ip + " manuel kara listeye eklendi.", NamedTextColor.GREEN));
            plugin.getLogManager().info("[Anti-VPN] " + ip + " manuel kara listeye eklendi. (" + sender.getName() + ")");
        } else {
            sender.sendMessage(Component.text(ip + " zaten manuel kara listede.", NamedTextColor.YELLOW));
        }
    }

    private void handleIpRemove(@NotNull CommandSender sender, @NotNull IPReputationManager rep, @NotNull String ip) {
        if (rep.removeFromManualBlocklist(ip)) {
            sender.sendMessage(Component.text(ip + " manuel kara listeden kaldırıldı.", NamedTextColor.GREEN));
            plugin.getLogManager().info("[Anti-VPN] " + ip + " manuel kara listeden kaldırıldı. (" + sender.getName() + ")");
        } else {
            sender.sendMessage(Component.text(ip + " manuel kara listede bulunamadı.", NamedTextColor.RED));
        }
    }

    private void handleWhitelist(@NotNull CommandSender sender, @NotNull IPReputationManager rep, @NotNull String[] args) {
        if (args.length < 4) {
            sender.sendMessage(Component.text("Kullanım: /atomfix antivpn whitelist <add|remove> <IP>", NamedTextColor.RED));
            return;
        }

        String action = args[2].toLowerCase();
        String ip = args[3];

        switch (action) {
            case "add" -> {
                if (rep.addToWhitelist(ip)) {
                    sender.sendMessage(Component.text(ip + " beyaz listeye eklendi.", NamedTextColor.GREEN));
                    plugin.getLogManager().info("[Anti-VPN] " + ip + " beyaz listeye eklendi. (" + sender.getName() + ")");
                } else {
                    sender.sendMessage(Component.text(ip + " zaten beyaz listede.", NamedTextColor.YELLOW));
                }
            }
            case "remove" -> {
                if (rep.removeFromWhitelist(ip)) {
                    sender.sendMessage(Component.text(ip + " beyaz listeden kaldırıldı.", NamedTextColor.GREEN));
                    plugin.getLogManager().info("[Anti-VPN] " + ip + " beyaz listeden kaldırıldı. (" + sender.getName() + ")");
                } else {
                    sender.sendMessage(Component.text(ip + " beyaz listede bulunamadı.", NamedTextColor.RED));
                }
            }
            default -> sender.sendMessage(Component.text("Geçersiz eylem. Kullanım: add veya remove", NamedTextColor.RED));
        }
    }

    private void showRecentBlocks(@NotNull CommandSender sender, @NotNull IPReputationManager rep) {
        List<IPReputationManager.BlockRecord> recent = rep.getRecentBlocks();

        sender.sendMessage(plugin.getMessageManager().parse(
                "<gradient:#ff6b6b:#ffa500><bold>═══ Son Engellenen IP'ler ═══</bold></gradient>"));

        if (recent.isEmpty()) {
            sender.sendMessage(Component.text("Henüz engellenen IP yok.", NamedTextColor.GRAY));
        } else {
            int limit = Math.min(recent.size(), 15);
            for (int i = 0; i < limit; i++) {
                IPReputationManager.BlockRecord record = recent.get(i);
                sender.sendMessage(Component.text("[" + record.getTimeFormatted() + "] ", NamedTextColor.DARK_GRAY)
                        .append(Component.text(record.ip, NamedTextColor.RED))
                        .append(Component.text(" — ", NamedTextColor.GRAY))
                        .append(Component.text(record.playerName, NamedTextColor.WHITE))
                        .append(Component.text(" — ", NamedTextColor.GRAY))
                        .append(Component.text(record.reason, NamedTextColor.YELLOW)));
            }
            if (recent.size() > 15) {
                sender.sendMessage(Component.text("  ... ve " + (recent.size() - 15) + " daha", NamedTextColor.GRAY));
            }
        }

        sender.sendMessage(plugin.getMessageManager().parse(
                "<gradient:#ff6b6b:#ffa500><bold>═══════════════════════════</bold></gradient>"));
    }

    // ═══════════════════════════════════════════════════
    //  AntiBot Komutu — /atomfix antibot <alt_komut>
    // ═══════════════════════════════════════════════════

    private void handleAntiBot(@NotNull CommandSender sender, @NotNull String[] args) {
        var antiBotModule = plugin.getModuleManager().getModule(com.atomsmp.fixer.module.antibot.AntiBotModule.class);
        if (antiBotModule == null) {
            sender.sendMessage(Component.text("AntiBot modülü yüklü değil!", NamedTextColor.RED));
            return;
        }

        if (args.length < 2) {
            showAntiBotHelp(sender);
            return;
        }

        String sub = args[1].toLowerCase();
        switch (sub) {
            case "status" -> {
                sender.sendMessage(plugin.getMessageManager().parse("<gradient:#00d4ff:#00ff88><bold>═══ AntiBot Durumu ═══</bold></gradient>"));
                sender.sendMessage(Component.text("Saldırı Modu: ", NamedTextColor.GRAY)
                        .append(Component.text(antiBotModule.getAttackTracker().isUnderAttack() ? "AKTIF" : "Normal", 
                                antiBotModule.getAttackTracker().isUnderAttack() ? NamedTextColor.RED : NamedTextColor.GREEN)));
                sender.sendMessage(Component.text("Kara Listedeki IP Sayısı: ", NamedTextColor.GRAY)
                        .append(Component.text("N/A", NamedTextColor.WHITE))); // BlacklistManager doesn't expose size yet
                sender.sendMessage(Component.text("Beyaz Listedeki Oyuncu Sayısı: ", NamedTextColor.GRAY)
                        .append(Component.text("N/A", NamedTextColor.WHITE))); // WhitelistManager doesn't expose size yet
                sender.sendMessage(plugin.getMessageManager().parse("<gradient:#00d4ff:#00ff88><bold>══════════════════════</bold></gradient>"));
            }
            case "whitelist" -> {
                if (args.length < 3) {
                    sender.sendMessage(Component.text("Kullanım: /atomfix antibot whitelist <oyuncu>", NamedTextColor.RED));
                    return;
                }
                org.bukkit.OfflinePlayer target = Bukkit.getOfflinePlayer(args[2]);
                antiBotModule.getWhitelistManager().whitelist(target.getUniqueId());
                sender.sendMessage(Component.text(target.getName() + " beyaz listeye eklendi.", NamedTextColor.GREEN));
            }
            case "blacklist" -> {
                if (args.length < 3) {
                    sender.sendMessage(Component.text("Kullanım: /atomfix antibot blacklist <IP> [süre_dk]", NamedTextColor.RED));
                    return;
                }
                String ip = args[2];
                long duration = args.length > 3 ? Long.parseLong(args[3]) * 60000L : 3600000L;
                antiBotModule.getBlacklistManager().blacklist(ip, duration, "Manual blacklist by " + sender.getName());
                sender.sendMessage(Component.text(ip + " kara listeye eklendi.", NamedTextColor.GREEN));
            }
            default -> showAntiBotHelp(sender);
        }
    }

    private void showAntiBotHelp(@NotNull CommandSender sender) {
        sender.sendMessage(plugin.getMessageManager().parse("<gradient:#00d4ff:#00ff88><bold>═══ AntiBot Komutları ═══</bold></gradient>"));
        sender.sendMessage(Component.text("/atomfix antibot status", NamedTextColor.AQUA).append(Component.text(" - Durum özeti", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/atomfix antibot whitelist <oyuncu>", NamedTextColor.AQUA).append(Component.text(" - Manuel beyaz liste", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/atomfix antibot blacklist <IP> [süre]", NamedTextColor.AQUA).append(Component.text(" - Manuel kara liste", NamedTextColor.GRAY)));
        sender.sendMessage(plugin.getMessageManager().parse("<gradient:#00d4ff:#00ff88><bold>════════════════════════</bold></gradient>"));
    }

    // ── Info ──

    private void showInfo(@NotNull CommandSender sender) {
        plugin.getMessageManager().sendMessage(sender, "info.baslik");

        Map<String, String> versionMap = new HashMap<>();
        versionMap.put("versiyon", plugin.getDescription().getVersion());
        plugin.getMessageManager().sendMessage(sender, "info.versiyon", versionMap);
        plugin.getMessageManager().sendMessage(sender, "info.gelistirici");

        Map<String, String> paperMap = new HashMap<>();
        paperMap.put("paper", plugin.getServer().getMinecraftVersion());
        plugin.getMessageManager().sendMessage(sender, "info.paper-versiyon", paperMap);

        Map<String, String> peMap = new HashMap<>();
        peMap.put("packetevents", com.github.retrooper.packetevents.PacketEvents.getAPI().getVersion().toString());
        plugin.getMessageManager().sendMessage(sender, "info.packetevents-versiyon", peMap);

        Map<String, String> javaMap = new HashMap<>();
        javaMap.put("java", System.getProperty("java.version"));
        plugin.getMessageManager().sendMessage(sender, "info.java-versiyon", javaMap);

        Map<String, String> moduleMap = new HashMap<>();
        moduleMap.put("aktif", String.valueOf(plugin.getModuleManager().getEnabledModuleCount()));
        moduleMap.put("toplam", String.valueOf(plugin.getModuleManager().getTotalModuleCount()));
        plugin.getMessageManager().sendMessage(sender, "info.aktif-modul", moduleMap);

        plugin.getMessageManager().sendMessage(sender, "info.komutlar");

        if (plugin.getVerifiedPlayerCache() != null && plugin.getVerifiedPlayerCache().isEnabled()) {
            Map<String, String> cacheMap = new HashMap<>();
            cacheMap.put("sayi", String.valueOf(plugin.getVerifiedPlayerCache().getCacheSize()));
            plugin.getMessageManager().sendMessage(sender, "info.dogrulanmis-onbellek", cacheMap);
        }

        // Anti-VPN durumu
        IPReputationManager rep = plugin.getReputationManager();
        sender.sendMessage(Component.text("  Anti-VPN: ", NamedTextColor.GRAY)
                .append(Component.text(rep.isEnabled() ? "AKTIF" : "DEVRE DISI",
                        rep.isEnabled() ? NamedTextColor.GREEN : NamedTextColor.RED))
                .append(Component.text(rep.isEnabled() ? " (" + formatNumber(rep.getProxyListSize()) + " proxy IP)" : "", NamedTextColor.GRAY)));
    }

    // ── Yardımcı Metodlar ──

    private Component statusLine(String label, boolean isBad, String value) {
        return Component.text("  " + label + ": ", NamedTextColor.GRAY)
                .append(Component.text(value, isBad ? NamedTextColor.RED : NamedTextColor.GREEN));
    }

    private NamedTextColor riskColor(int risk) {
        if (risk >= 80) return NamedTextColor.RED;
        if (risk >= 50) return NamedTextColor.YELLOW;
        if (risk >= 20) return NamedTextColor.GOLD;
        return NamedTextColor.GREEN;
    }

    private String formatNumber(int num) {
        if (num >= 1_000_000) return String.format("%.1fM", num / 1_000_000.0);
        if (num >= 1_000) return String.format("%.1fK", num / 1_000.0);
        return String.valueOf(num);
    }
}

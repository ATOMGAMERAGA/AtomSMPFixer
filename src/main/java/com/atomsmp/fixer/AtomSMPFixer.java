package com.atomsmp.fixer;

import com.atomsmp.fixer.command.AtomFixCommand;
import com.atomsmp.fixer.command.AtomFixTabCompleter;
import com.atomsmp.fixer.listener.BukkitListener;
import com.atomsmp.fixer.listener.InventoryListener;
import com.atomsmp.fixer.listener.PacketListener;
import com.atomsmp.fixer.manager.ConfigManager;
import com.atomsmp.fixer.manager.LogManager;
import com.atomsmp.fixer.manager.MessageManager;
import com.atomsmp.fixer.manager.ModuleManager;
import com.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

/**
 * AtomSMPFixer - Paper 1.21.4 Exploit Fixer Plugin
 * Gelişmiş exploit düzeltme ve sunucu koruma sistemi
 *
 * @author AtomSMP
 * @version 1.0.0
 */
public final class AtomSMPFixer extends JavaPlugin {

    // Singleton instance
    private static AtomSMPFixer instance;

    // Manager sınıfları
    private ConfigManager configManager;
    private MessageManager messageManager;
    private LogManager logManager;
    private ModuleManager moduleManager;

    // Listener'lar
    private PacketListener packetListener;
    private BukkitListener bukkitListener;
    private InventoryListener inventoryListener;

    /**
     * Plugin aktif edildiğinde çağrılır
     */
    @Override
    public void onLoad() {
        // Singleton instance
        instance = this;

        // PacketEvents'i yükle
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().load();
    }

    /**
     * Plugin etkinleştirildiğinde çağrılır
     */
    @Override
    public void onEnable() {
        long startTime = System.currentTimeMillis();

        // ASCII Art Banner
        printBanner();

        // PacketEvents kontrolü
        if (!checkPacketEvents()) {
            getLogger().severe("╔════════════════════════════════════════╗");
            getLogger().severe("║  PacketEvents bulunamadı!              ║");
            getLogger().severe("║  Plugin devre dışı bırakılıyor...      ║");
            getLogger().severe("╚════════════════════════════════════════╝");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Manager'ları başlat
        initializeManagers();

        // Listener'ları kaydet
        registerListeners();

        // Komutları kaydet
        registerCommands();

        // PacketEvents'i başlat
        PacketEvents.getAPI().init();

        // Periyodik temizlik görevi (bellek sızıntısı önleme) - her 5 dakikada bir
        startCleanupTask();

        // Başarı mesajı
        long loadTime = System.currentTimeMillis() - startTime;
        getLogger().info("╔════════════════════════════════════════╗");
        getLogger().info("║  AtomSMPFixer başarıyla yüklendi!     ║");
        getLogger().info("║  Versiyon: " + getDescription().getVersion() + "                         ║");
        getLogger().info("║  Yükleme süresi: " + loadTime + "ms                  ║");
        getLogger().info("║  Aktif modül: " + moduleManager.getEnabledModuleCount() + "/" + moduleManager.getTotalModuleCount() + "                    ║");
        getLogger().info("╚════════════════════════════════════════╝");
    }

    /**
     * Plugin devre dışı bırakıldığında çağrılır
     */
    @Override
    public void onDisable() {
        getLogger().info("AtomSMPFixer kapatılıyor...");

        // Modülleri devre dışı bırak
        if (moduleManager != null) {
            moduleManager.disableAllModules();
        }

        // Log sistemini durdur
        if (logManager != null) {
            logManager.stop();
        }

        // PacketEvents'i kapat
        if (PacketEvents.getAPI() != null) {
            PacketEvents.getAPI().terminate();
        }

        getLogger().info("AtomSMPFixer başarıyla kapatıldı.");
    }

    /**
     * Manager'ları başlatır
     */
    private void initializeManagers() {
        getLogger().info("Manager'lar başlatılıyor...");

        // Config Manager
        this.configManager = new ConfigManager(this);
        configManager.load();

        // Message Manager
        this.messageManager = new MessageManager(this);

        // Log Manager
        this.logManager = new LogManager(this);
        logManager.start();

        // Module Manager
        this.moduleManager = new ModuleManager(this);

        // Modülleri kaydet
        registerModules();

        // Modülleri etkinleştir
        moduleManager.enableAllModules();

        getLogger().info("Tüm manager'lar başlatıldı.");
    }

    /**
     * Listener'ları kaydeder
     */
    private void registerListeners() {
        getLogger().info("Listener'lar kaydediliyor...");

        // PacketEvents Listener
        this.packetListener = new PacketListener(this);
        PacketEvents.getAPI().getEventManager().registerListener(packetListener);

        // Bukkit Listener
        this.bukkitListener = new BukkitListener(this);
        getServer().getPluginManager().registerEvents(bukkitListener, this);

        // Inventory Listener
        this.inventoryListener = new InventoryListener(this);
        getServer().getPluginManager().registerEvents(inventoryListener, this);

        getLogger().info("Listener'lar kaydedildi.");
    }

    /**
     * Komutları kaydeder
     */
    private void registerCommands() {
        getLogger().info("Komutlar kaydediliyor...");

        // /atomfix komutu
        AtomFixCommand atomFixCommand = new AtomFixCommand(this);
        AtomFixTabCompleter tabCompleter = new AtomFixTabCompleter(this);

        getCommand("atomfix").setExecutor(atomFixCommand);
        getCommand("atomfix").setTabCompleter(tabCompleter);

        getLogger().info("Komutlar kaydedildi.");
    }

    /**
     * Periyodik temizlik görevini başlatır
     * Modüllerin cleanup() metodlarını çağırarak bellek sızıntısını önler
     */
    private void startCleanupTask() {
        // Her 5 dakikada bir (6000 tick) cleanup çalıştır
        getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
            try {
                var offlineModule = moduleManager.getModule(com.atomsmp.fixer.module.OfflinePacketModule.class);
                if (offlineModule != null) offlineModule.cleanup();

                var exploitModule = moduleManager.getModule(com.atomsmp.fixer.module.PacketExploitModule.class);
                if (exploitModule != null) exploitModule.cleanup();

                // FrameCrashModule.cleanup() Bukkit API kullanıyor, sync olmalı
                getServer().getScheduler().runTask(this, () -> {
                    var frameModule = moduleManager.getModule(com.atomsmp.fixer.module.FrameCrashModule.class);
                    if (frameModule != null) frameModule.cleanup();
                });

                var invModule = moduleManager.getModule(com.atomsmp.fixer.module.InventoryDuplicationModule.class);
                if (invModule != null) invModule.cleanup();

            } catch (Exception e) {
                getLogger().warning("Cleanup görevi sırasında hata: " + e.getMessage());
            }
        }, 6000L, 6000L);
    }

    /**
     * PacketEvents'in yüklenip yüklenmediğini kontrol eder
     *
     * @return Yüklü ise true
     */
    private boolean checkPacketEvents() {
        return PacketEvents.getAPI() != null;
    }

    /**
     * Modülleri kaydeder
     */
    private void registerModules() {
        getLogger().info("Modüller kaydediliyor...");

        // Tüm modülleri kaydet
        moduleManager.registerModule(new com.atomsmp.fixer.module.TooManyBooksModule(this));
        moduleManager.registerModule(new com.atomsmp.fixer.module.PacketDelayModule(this));
        moduleManager.registerModule(new com.atomsmp.fixer.module.PacketExploitModule(this));
        moduleManager.registerModule(new com.atomsmp.fixer.module.CustomPayloadModule(this));
        moduleManager.registerModule(new com.atomsmp.fixer.module.CommandsCrashModule(this));
        moduleManager.registerModule(new com.atomsmp.fixer.module.CreativeItemsModule(this));
        moduleManager.registerModule(new com.atomsmp.fixer.module.SignCrasherModule(this));
        moduleManager.registerModule(new com.atomsmp.fixer.module.LecternCrasherModule(this));
        moduleManager.registerModule(new com.atomsmp.fixer.module.MapLabelCrasherModule(this));
        moduleManager.registerModule(new com.atomsmp.fixer.module.InvalidSlotModule(this));
        moduleManager.registerModule(new com.atomsmp.fixer.module.NBTCrasherModule(this));
        moduleManager.registerModule(new com.atomsmp.fixer.module.BookCrasherModule(this));
        moduleManager.registerModule(new com.atomsmp.fixer.module.CowDuplicationModule(this));
        moduleManager.registerModule(new com.atomsmp.fixer.module.DispenserCrasherModule(this));
        moduleManager.registerModule(new com.atomsmp.fixer.module.OfflinePacketModule(this));
        moduleManager.registerModule(new com.atomsmp.fixer.module.InventoryDuplicationModule(this));
        moduleManager.registerModule(new com.atomsmp.fixer.module.MuleDuplicationModule(this));
        moduleManager.registerModule(new com.atomsmp.fixer.module.PortalBreakModule(this));
        moduleManager.registerModule(new com.atomsmp.fixer.module.BundleDuplicationModule(this));
        moduleManager.registerModule(new com.atomsmp.fixer.module.NormalizeCoordinatesModule(this));
        moduleManager.registerModule(new com.atomsmp.fixer.module.FrameCrashModule(this));

        getLogger().info("Toplam " + moduleManager.getTotalModuleCount() + " modül kaydedildi.");
    }

    /**
     * ASCII Art banner yazdırır
     */
    private void printBanner() {
        getLogger().info("╔════════════════════════════════════════╗");
        getLogger().info("║    _  _            ___ __  __ ___      ║");
        getLogger().info("║   /_\\| |_ ___ _ __/ __|  \\/  | _ \\     ║");
        getLogger().info("║  / _ \\ _/ _ \\ '  \\__ \\ |\\/| |  _/     ║");
        getLogger().info("║ /_/ \\_\\__\\___/_|_|___|_|  |_|_|       ║");
        getLogger().info("║          Exploit Fixer v" + getDescription().getVersion() + "          ║");
        getLogger().info("╚════════════════════════════════════════╝");
    }

    // ═══════════════════════════════════════
    // Getter Metodları
    // ═══════════════════════════════════════

    /**
     * Singleton instance alır
     *
     * @return Plugin instance
     */
    @NotNull
    public static AtomSMPFixer getInstance() {
        return instance;
    }

    /**
     * ConfigManager alır
     *
     * @return ConfigManager instance
     */
    @NotNull
    public ConfigManager getConfigManager() {
        return configManager;
    }

    /**
     * MessageManager alır
     *
     * @return MessageManager instance
     */
    @NotNull
    public MessageManager getMessageManager() {
        return messageManager;
    }

    /**
     * LogManager alır
     *
     * @return LogManager instance
     */
    @NotNull
    public LogManager getLogManager() {
        return logManager;
    }

    /**
     * ModuleManager alır
     *
     * @return ModuleManager instance
     */
    @NotNull
    public ModuleManager getModuleManager() {
        return moduleManager;
    }

    /**
     * PacketListener alır
     *
     * @return PacketListener instance
     */
    @NotNull
    public PacketListener getPacketListener() {
        return packetListener;
    }

    /**
     * BukkitListener alır
     *
     * @return BukkitListener instance
     */
    @NotNull
    public BukkitListener getBukkitListener() {
        return bukkitListener;
    }

    /**
     * InventoryListener alır
     *
     * @return InventoryListener instance
     */
    @NotNull
    public InventoryListener getInventoryListener() {
        return inventoryListener;
    }
}

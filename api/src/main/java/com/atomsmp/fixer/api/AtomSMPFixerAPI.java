package com.atomsmp.fixer.api;

import com.atomsmp.fixer.api.module.IModuleManager;
import com.atomsmp.fixer.api.stats.IStatisticsProvider;
import com.atomsmp.fixer.api.storage.IStorageProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * AtomSMPFixer Public API - Ana erişim noktası
 * Diğer pluginlerin AtomSMPFixer ile entegre olmasını sağlar.
 *
 * @author AtomSMP
 * @since 3.0.0
 */
public final class AtomSMPFixerAPI {

    private static AtomSMPFixerAPI instance;

    private final IModuleManager moduleManager;
    private final IStorageProvider storageProvider;
    private final IStatisticsProvider statisticsProvider;
    private final IReputationService reputationService;
    private final String version;

    /**
     * API instance oluşturur. Sadece core plugin tarafından çağrılmalıdır.
     */
    public AtomSMPFixerAPI(
            @NotNull IModuleManager moduleManager,
            @Nullable IStorageProvider storageProvider,
            @NotNull IStatisticsProvider statisticsProvider,
            @Nullable IReputationService reputationService,
            @NotNull String version
    ) {
        this.moduleManager = moduleManager;
        this.storageProvider = storageProvider;
        this.statisticsProvider = statisticsProvider;
        this.reputationService = reputationService;
        this.version = version;
        instance = this;
    }

    /**
     * API singleton instance alır.
     *
     * @return API instance
     * @throws IllegalStateException Plugin henüz yüklenmemişse
     */
    @NotNull
    public static AtomSMPFixerAPI getInstance() {
        if (instance == null) {
            throw new IllegalStateException("AtomSMPFixer API henüz başlatılmadı!");
        }
        return instance;
    }

    /**
     * API'nin başlatılıp başlatılmadığını kontrol eder.
     *
     * @return Başlatılmışsa true
     */
    public static boolean isAvailable() {
        return instance != null;
    }

    /**
     * Modül yönetim sistemi.
     *
     * @return IModuleManager instance
     */
    @NotNull
    public IModuleManager getModuleManager() {
        return moduleManager;
    }

    /**
     * Depolama sağlayıcısı (MySQL/SQLite/File).
     * Sprint 2'de implement edilecek, o zamana kadar null dönebilir.
     *
     * @return IStorageProvider instance veya null
     */
    @Nullable
    public IStorageProvider getStorageProvider() {
        return storageProvider;
    }

    /**
     * İstatistik sağlayıcısı.
     *
     * @return IStatisticsProvider instance
     */
    @NotNull
    public IStatisticsProvider getStatistics() {
        return statisticsProvider;
    }

    /**
     * IP reputation servisi.
     * Yapılandırılmamışsa null dönebilir.
     *
     * @return IReputationService instance veya null
     */
    @Nullable
    public IReputationService getReputationService() {
        return reputationService;
    }

    /**
     * Plugin versiyonu.
     *
     * @return Versiyon string
     */
    @NotNull
    public String getVersion() {
        return version;
    }

    /**
     * API instance'ını temizler. Sadece core plugin onDisable'da çağırmalıdır.
     */
    public static void shutdown() {
        instance = null;
    }
}

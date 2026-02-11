# Changelog

Tum onemli degisiklikler bu dosyada belgelenir.

## [3.0.0] - 2026-02-11

### Enterprise Donusumu - Sprint 1

Bu surum, AtomSMPFixer'in enterprise-grade bir urune donusturulmesinin ilk adimidir.

### Eklenen
- **Maven Multi-Module Yapisi:** Proje 3 modulden olusan bir yapiya donusturuldu:
  - `atomsmpfixer-api` - Diger pluginlerin entegre olabilecegi public Java API
  - `atomsmpfixer-core` - Ana plugin JAR'i (tum mevcut islevsellik)
  - `atomsmpfixer-velocity` - Velocity proxy modulu (Sprint 6'da implement edilecek)
- **Public API Interfaces:**
  - `AtomSMPFixerAPI` - Ana API erisim noktasi (singleton)
  - `IModule` / `IModuleManager` - Modul sistemi arayuzleri
  - `IStorageProvider` - Veritabani soyutlama katmani (MySQL/SQLite/File)
  - `IStatisticsProvider` - Istatistik sorgulama arayuzu
  - `IReputationService` - IP reputation servisi arayuzu
- **Custom Bukkit Events:**
  - `ExploitBlockedEvent` - Exploit engellendiginde tetiklenir (cancellable)
  - `AttackModeToggleEvent` - Attack mode durumu degistiginde tetiklenir
  - `PlayerReputationCheckEvent` - IP reputation kontrolunde tetiklenir (cancellable)
  - `ModuleToggleEvent` - Modul aktif/pasif edildiginde tetiklenir (cancellable)
- **Shaded Dependencies (core):**
  - HikariCP 5.1.0 - MySQL connection pooling (`com.atomsmp.fixer.lib.hikari`)
  - Jedis 5.1.0 - Redis client (`com.atomsmp.fixer.lib.jedis`)
  - commons-pool2 - Connection pooling (`com.atomsmp.fixer.lib.pool2`)
  - SLF4J 2.0.9 - Logging facade (`com.atomsmp.fixer.lib.slf4j`)

### Degistirilen
- Parent POM `pom` packaging ile multi-module reactor olarak yeniden yapilandirildi
- `AbstractModule` artik `IModule` interface'ini implement ediyor
- `ModuleManager` artik `IModuleManager` interface'ini implement ediyor
- `StatisticsManager` artik `IStatisticsProvider` interface'ini implement ediyor
- Tum kaynak kod `core/src/main/java/` altina tasindi
- Tum resource dosyalari `core/src/main/resources/` altina tasindi
- Version 2.3.1'den 3.0.0'a yukseltildi

### Geriye Uyumluluk
- Tum mevcut 40 modul calismaya devam ediyor
- `config.yml` ve `messages.yml` formati degismedi
- `plugin.yml` ayni kaldipacketevents bagimliligi korunuyor
- Paket adlari degismedi (`com.atomsmp.fixer.*`)

---

## [2.3.1] - 2026-02-10

### Duzeltilen
- Web-panel konfigurasyonu guncellendi
- Version bump

## [2.3.0] - 2026-02-10

### Eklenen
- Discord Webhook entegrasyonu
- StatisticsManager - kalici JSON istatistikleri
- VerifiedPlayerCache - dogrulanmis oyuncu onbellek sistemi
- SmartLagModule - heuristik lag tespiti
- DuplicationFixModule - gelismis portal/shulker dupe korumasi
- ConnectionThrottleModule - baglanti hiz sinirlandirici

## [2.2.3] - 2026-02-09

### Duzeltilen
- Port check devre disi birakildi ('End of stream' hatasi cozuldu)
- Login delay varsayilan degerleri dusuruldu

## [2.2.2] - 2026-02-09

### Duzeltilen
- Baglanti askilma sorunu session tracking iyilestirmesiyle cozuldu

## [2.2.1] - 2026-02-08

### Duzeltilen
- BotProtection false positive sorunlari (Hostname ve Gravity check)

## [2.2.0] - 2026-02-08

### Eklenen
- FallingBlockLimiterModule
- ExplosionLimiterModule
- MovementSecurityModule
- VisualCrasherModule
- AdvancedChatModule
- PistonLimiterModule

## [2.1.0] - 2026-02-07

### Eklenen
- AtomShield Bot Korumasi (BotProtectionModule)
- IP Reputation sistemi
- Web Panel
- HeuristicEngine

## [2.0.0] - 2026-02-06

### Eklenen
- 9 yeni guvenlik modulu (TokenBucket, AdvancedPayload, NettyCrash, ItemSanitizer, BundleLock, ShulkerByte, StorageEntityLock, RedstoneLimiter, ViewDistanceMask)

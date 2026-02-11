# AtomSMPFixer

[![Build Status](https://github.com/ATOMGAMERAGA/AtomSMPFixer/actions/workflows/build.yml/badge.svg)](https://github.com/ATOMGAMERAGA/AtomSMPFixer/actions/workflows/build.yml)
[![Release](https://github.com/ATOMGAMERAGA/AtomSMPFixer/actions/workflows/release.yml/badge.svg)](https://github.com/ATOMGAMERAGA/AtomSMPFixer/actions/workflows/release.yml)
[![License](https://img.shields.io/badge/license-All%20Rights%20Reserved-red.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Paper](https://img.shields.io/badge/Paper-1.21.4-blue.svg)](https://papermc.io/)
[![Version](https://img.shields.io/badge/version-3.0.0-brightgreen.svg)](https://github.com/ATOMGAMERAGA/AtomSMPFixer/releases/latest)

**Paper 1.21.4** icin gelistirilmis enterprise-grade **Exploit Fixer** plugin'i. 40+ modul, public API, web panel, Discord entegrasyonu ve IP reputation sistemi ile sunucunuzu tam kapsamli koruma altina alin.

## Icindekiler

- [Ozellikler](#ozellikler)
- [v3.0 - Yenilikler](#v30---yenilikler)
- [Gereksinimler](#gereksinimler)
- [Kurulum](#kurulum)
- [Moduller](#moduller)
- [API Kullanimi](#api-kullanimi)
- [Komutlar](#komutlar)
- [Konfigurasyon](#konfigurasyon)
- [Izinler](#izinler)
- [Build](#build)
- [Mimari](#mimari)
- [Roadmap](#roadmap)
- [Destek](#destek)

## Ozellikler

- **40+ Exploit Fixer Modulu** - Crasher, dupe, packet exploit, bot korumasi ve daha fazlasi
- **Public Java API** - Diger pluginler icin entegrasyon arayuzu
- **Custom Bukkit Events** - Exploit engelleme, attack mode ve modul degisiklik eventleri
- **PacketEvents Entegrasyonu** - Gelismis paket seviyesi koruma
- **AtomShield Bot Korumasi** - Hibrit bot tespiti (Handshake, Protokol, Davranis analizi)
- **IP Reputation Sistemi** - 7 katmanli VPN/Proxy tespit ve ASN engelleme
- **Web Panel** - Gercek zamanli istatistik dashboard'u
- **Discord Webhook** - Anlik exploit bildirim entegrasyonu
- **Heuristik Analiz** - Davranis bazli saldiri tespiti
- **Attack Mode** - Otomatik saldiri algisinde gelismis koruma modu
- **Ultra-Performansli** - Thread-safe tasarim, async islemler, minimal TPS etkisi
- **Turkce Destek** - MiniMessage formati ile renkli mesajlar
- **Gelismis Log Sistemi** - Async log yazma, gunluk dosyalar, otomatik temizleme
- **Hot-Reload** - Sunucuyu kapatmadan config yenileme

## v3.0 - Yenilikler

v3.0, AtomSMPFixer'in enterprise donusumunun ilk asamasidir:

### Maven Multi-Module Yapisi
Proje artik 3 Maven modulunden olusmaktadir:

| Modul | Artifact | Aciklama |
|-------|----------|----------|
| **api** | `atomsmpfixer-api` | Public API interfaces - diger pluginler bu JAR'a baglanir |
| **core** | `atomsmpfixer-core` | Ana plugin JAR'i - sunucuya yuklenecek dosya |
| **velocity** | `atomsmpfixer-velocity` | Velocity proxy modulu (yakin surumde) |

### Public API
Diger pluginler `atomsmpfixer-api` JAR'ina baglanaraktasinAtomSMPFixer ile entegre olabilir:
- `AtomSMPFixerAPI` - Singleton erisim noktasi
- `IModule` / `IModuleManager` - Modul sorgulama
- `IStorageProvider` - Veritabani soyutlama (MySQL/SQLite/File)
- `IStatisticsProvider` - Istatistik sorgulama
- `IReputationService` - IP reputation kontrolu

### Custom Bukkit Events
- `ExploitBlockedEvent` - Herhangi bir exploit engellendiginde (cancellable)
- `AttackModeToggleEvent` - Attack mode aktif/pasif oldugunda
- `PlayerReputationCheckEvent` - IP reputation kontrolu yapildiginda (cancellable)
- `ModuleToggleEvent` - Modul durumu degistiginde (cancellable)

### Shaded Enterprise Dependencies
Core JAR icerisinde relocate edilerek paketlenen kutuphaneler:
- **HikariCP 5.1.0** - MySQL connection pooling
- **Jedis 5.1.0** - Redis client
- **SLF4J 2.0.9** - Logging facade

## Gereksinimler

- **Java 21** veya uzeri
- **Paper 1.21.4** (Spigot/CraftBukkit desteklenmez)
- **PacketEvents 2.6.0+** (Zorunlu bagimlilik)

## Kurulum

### Otomatik Kurulum (Onerilen)

1. [Releases](https://github.com/ATOMGAMERAGA/AtomSMPFixer/releases/latest) sayfasindan `AtomSMPFixer-3.0.0.jar` dosyasini indirin
2. JAR dosyasini sunucunuzun `plugins/` klasorune kopyalayin
3. [PacketEvents](https://modrinth.com/plugin/packetevents) plugin'ini indirip `plugins/` klasorune ekleyin
4. Sunucuyu baslatin

### Manuel Build

```bash
git clone https://github.com/ATOMGAMERAGA/AtomSMPFixer.git
cd AtomSMPFixer
mvn clean package
```

Build edilen plugin JAR: `core/target/AtomSMPFixer-3.0.0.jar`

## Moduller

### Temel Koruma Modulleri (21)

| Modul | Aciklama | Config Key |
|-------|----------|------------|
| **TooManyBooks** | Chunk basina kitap limiti (crasher/dupe) | `cok-fazla-kitap` |
| **PacketDelay** | Paket spam kontrolu (bundle dupe) | `paket-gecikme` |
| **PacketExploit** | Zararli paket engelleme (netty crasher) | `paket-exploit` |
| **CustomPayload** | Custom payload kanal kontrolu | `ozel-payload` |
| **CommandsCrash** | Zararli komut engelleme | `komut-crash` |
| **CreativeItems** | Hacked creative item duzeltme | `creative-item` |
| **SignCrasher** | Gecersiz tabela engelleme | `tabela-crash` |
| **LecternCrasher** | Kursu exploit engelleme | `kursu-crash` |
| **MapLabelCrasher** | Harita etiketi limiti | `harita-etiketi-crash` |
| **InvalidSlot** | Gecersiz slot etkilesimi engelleme | `gecersiz-slot` |
| **NBTCrasher** | Asiri NBT verisi engelleme | `nbt-crash` |
| **BookCrasher** | Kitap boyut/sayfa kontrolu | `kitap-crash` |
| **CowDuplication** | Inek kirkma duplikasyon engelleme | `inek-duplikasyon` |
| **DispenserCrasher** | Dispenser crash engelleme | `dispenser-crash` |
| **OfflinePacket** | Cevrimdisi paket engelleme | `cevrimdisi-paket` |
| **InventoryDuplication** | Envanter duplikasyon engelleme | `envanter-duplikasyon` |
| **MuleDuplication** | Katir/esek duplikasyon engelleme | `katir-duplikasyon` |
| **PortalBreak** | Portal kirma exploit engelleme | `portal-kirma` |
| **BundleDuplication** | Bundle duplikasyon engelleme | `bundle-duplikasyon` |
| **NormalizeCoordinates** | Koordinat normallestirme | `koordinat-normallestirme` |
| **FrameCrash** | Item frame crash engelleme | `frame-crash` |

### Gelismis Guvenlik Modulleri (v2.0+)

| Modul | Aciklama | Config Key |
|-------|----------|------------|
| **TokenBucket** | 4 kovalirate limiter (hareket/sohbet/envanter/diger) | `jeton-kovasi` |
| **AdvancedPayload** | Kanal whitelist, boyut limiti, brand analizi | `gelismis-payload` |
| **NettyCrash** | Netty pipeline enjeksiyonu + NaN/Infinity dogrulama | `netty-crash` |
| **ItemSanitizer** | Item guvenlik temizleyicisi | `item-temizleyici` |
| **BundleLock** | Bundle race condition korumasi | `bundle-kilit` |
| **ShulkerByte** | Shulker byte boyutu kontrolu (chunk ban koruma) | `shulker-bayt` |
| **StorageEntityLock** | Donkey/Llama cift erisim kilidi | `depolama-entity-kilit` |
| **RedstoneLimiter** | Chunk bazli redstone sinirlandirici | `redstone-sinirlandirici` |
| **ViewDistanceMask** | View distance paket maskeleme (Anti-NoCom) | `gorunum-mesafesi-maskeleme` |

### Bot Korumasi ve Saldiri Onleme (v2.1+)

| Modul | Aciklama | Config Key |
|-------|----------|------------|
| **AtomShield** | Hibrit bot korumasi (Handshake, Protokol, Davranis analizi) | `bot-korumasi` |
| **ConnectionThrottle** | Baglanti hiz sinirlandirici | `baglanti-sinirlandirici` |
| **FallingBlock** | Kum/Cakil sinirlandirici | `kum-cakil-sinirlandirici` |
| **ExplosionLimiter** | Patlama ve blok hasari siniri | `patlama-sinirlandirici` |
| **MovementSecurity** | Gecersiz koordinat ve asiri hiz korumasi | `hareket-guvenligi` |
| **VisualCrasher** | Havai fisek ve partikul sinirlandirici | `gorsel-crasher` |
| **AdvancedChat** | Unicode filtreleme ve tab-complete rate limiting | `gelismis-sohbet` |
| **PistonLimiter** | Piston hareketi ve 0-tick engelleyici | `piston-sinirlandirici` |
| **SmartLag** | Heuristik lag tespiti ve entity yogunluk analizi | `akilli-lag-tespiti` |
| **DuplicationFix** | Gelismis portal ve shulker dupe korumasi | `gelismis-duplikasyon` |

Her modul `config.yml` dosyasindan ayri ayri kontrol edilebilir.

## API Kullanimi

Diger pluginler AtomSMPFixer API'sini kullanarak entegre olabilir:

### Maven Dependency

```xml
<dependency>
    <groupId>com.atomsmp</groupId>
    <artifactId>atomsmpfixer-api</artifactId>
    <version>3.0.0</version>
    <scope>provided</scope>
</dependency>
```

### Ornek Kod

```java
import com.atomsmp.fixer.api.AtomSMPFixerAPI;
import com.atomsmp.fixer.api.module.IModuleManager;
import com.atomsmp.fixer.api.event.ExploitBlockedEvent;

// API erisimi
if (AtomSMPFixerAPI.isAvailable()) {
    AtomSMPFixerAPI api = AtomSMPFixerAPI.getInstance();

    // Modul bilgisi
    IModuleManager modules = api.getModuleManager();
    System.out.println("Aktif modul: " + modules.getEnabledModuleCount());
    System.out.println("Toplam engelleme: " + modules.getTotalBlockedCount());

    // Istatistikler
    long totalBlocked = api.getStatistics().getTotalBlocked();
}

// Event dinleme
@EventHandler
public void onExploitBlocked(ExploitBlockedEvent event) {
    String module = event.getModuleName();
    String player = event.getPlayerName();
    // Ozel islem...
}
```

## Komutlar

| Komut | Aciklama | Izin |
|-------|----------|------|
| `/atomfix reload` | Config'i yeniden yukle | `atomsmpfixer.reload` |
| `/atomfix status` | Durum, TPS ve istatistikler | `atomsmpfixer.admin` |
| `/atomfix toggle <modul>` | Modul ac/kapa | `atomsmpfixer.admin` |
| `/atomfix info` | Plugin bilgileri | `atomsmpfixer.admin` |
| `/panic` | Acil durum bot korumasi | `atomsmpfixer.panic` |

**Kisa Komutlar:** `/af`, `/atomsmpfixer`

## Konfigurasyon

### config.yml

```yaml
genel:
  onek: "<gradient:#00d4ff:#00ff88>AtomSMPFixer</gradient> <dark_gray>></dark_gray>"
  debug: false
  log:
    aktif: true
    klasor: "logs/atomsmpfixer"
    gunluk-dosya: true
    log-saklama-gunu: 7

moduller:
  cok-fazla-kitap:
    aktif: true
    chunk-basina-max-kitap: 20
    max-sayfa-uzunlugu: 256
    max-toplam-boyut: 40000
    eylem: "ENGELLE"

  paket-exploit:
    aktif: true
    max-paket-boyutu: 32767
    max-paket-orani: 500

  # ... diger moduller
```

### messages.yml

Tum mesajlar `messages.yml` dosyasindan ozellestirilebilir. MiniMessage formatini destekler.

## Izinler

| Izin | Aciklama | Varsayilan |
|------|----------|-----------|
| `atomsmpfixer.admin` | Tum komutlara erisim | OP |
| `atomsmpfixer.bypass` | Tum exploit kontrollerini atla | Yok |
| `atomsmpfixer.reload` | Config yeniden yukleme | OP |
| `atomsmpfixer.notify` | Exploit bildirimlerini alma | OP |
| `atomsmpfixer.panic` | Panic komutu erisimi | OP |

## Build

### Gereksinimler

- Java 21 JDK
- Maven 3.8+

### Build Komutlari

```bash
# Tum modulleri build et
mvn clean package

# Testleri atla
mvn clean package -DskipTests
```

Build ciktilari:
- Plugin JAR: `core/target/AtomSMPFixer-3.0.0.jar`
- API JAR: `api/target/atomsmpfixer-api-3.0.0.jar`

## Performans

- **Bellek Kullanimi:** ~2-5 MB (1000 oyuncu icin)
- **TPS Etkisi:** < 0.02 (neredeyse sifir)
- **Paket Isleme:** < 1ms ortalama
- **Startup Suresi:** < 500ms

## Mimari

```
AtomSMPFixer/
├── pom.xml                              # Parent POM (multi-module reactor)
├── api/                                 # Public API modulu
│   └── src/main/java/
│       └── com/atomsmp/fixer/api/
│           ├── AtomSMPFixerAPI.java      # Singleton API erisim noktasi
│           ├── IReputationService.java   # IP reputation arayuzu
│           ├── module/                   # IModule, IModuleManager
│           ├── storage/                  # IStorageProvider
│           ├── stats/                    # IStatisticsProvider
│           └── event/                    # Custom Bukkit events
├── core/                                # Ana plugin modulu
│   └── src/main/java/
│       └── com/atomsmp/fixer/
│           ├── AtomSMPFixer.java         # Plugin entry point
│           ├── command/                  # Komut sistemi
│           ├── data/                     # Veri modelleri
│           ├── heuristic/               # Heuristik analiz motoru
│           ├── listener/                 # Packet, Bukkit, Inventory listenerler
│           ├── manager/                  # 7 manager sinifi
│           ├── module/                   # 40+ exploit fixer modulu
│           ├── reputation/              # IP reputation sistemi
│           ├── util/                     # Yardimci siniflar
│           └── web/                      # Web panel
└── velocity/                            # Velocity proxy modulu (yakin surumde)
```

## Roadmap

- [x] 40+ exploit fixer modulu
- [x] Web panel dashboard
- [x] Discord webhook entegrasyonu
- [x] IP reputation sistemi
- [x] AtomShield bot korumasi
- [x] Maven multi-module yapisi
- [x] Public Java API
- [x] Custom Bukkit events
- [ ] MySQL/SQLite veri depolama (Sprint 2)
- [ ] Redis cross-server sync (Sprint 2)
- [ ] Coklu dil destegi TR+EN (Sprint 3)
- [ ] Config dogrulama ve migration (Sprint 3)
- [ ] Prometheus metrics export (Sprint 4)
- [ ] bStats entegrasyonu (Sprint 4)
- [ ] Lisans ve ticari sistem (Sprint 5)
- [ ] Velocity proxy modulu (Sprint 6)
- [ ] JUnit 5 test suite (Sprint 7)
- [ ] GitHub Actions CI/CD (Sprint 7)

---

**Gelistirici:** AtomSMP
**Surum:** v3.0.0
**Paper Surumu:** 1.21.4
**Java Surumu:** 21
**PacketEvents Surumu:** 2.6.0+

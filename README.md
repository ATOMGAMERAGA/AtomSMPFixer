# 🛡️ AtomSMPFixer

[![Build Status](https://github.com/ATOMGAMERAGA/AtomSMPFixer/actions/workflows/build.yml/badge.svg)](https://github.com/ATOMGAMERAGA/AtomSMPFixer/actions/workflows/build.yml)
[![Release](https://github.com/ATOMGAMERAGA/AtomSMPFixer/actions/workflows/release.yml/badge.svg)](https://github.com/ATOMGAMERAGA/AtomSMPFixer/actions/workflows/release.yml)
[![License](https://img.shields.io/badge/license-All%20Rights%20Reserved-red.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Paper](https://img.shields.io/badge/Paper-1.21.4-blue.svg)](https://papermc.io/)

**Paper 1.21.4** için geliştirilmiş kapsamlı ve profesyonel **Exploit Fixer** plugin'i.

## 📋 İçindekiler

- [Özellikler](#-özellikler)
- [Gereksinimler](#-gereksinimler)
- [Kurulum](#-kurulum)
- [Modüller](#️-modüller)
- [Komutlar](#-komutlar)
- [Konfigürasyon](#️-konfigürasyon)
- [İzinler](#-i̇zinler)
- [Build](#-build)
- [Destek](#-destek)

## ✨ Özellikler

- 🛡️ **30 Farklı Exploit Fixer Modülü** - Chunk crasher, dupe, packet exploit ve daha fazlası
- 📡 **PacketEvents Entegrasyonu** - Gelişmiş paket seviyesi koruma
- 🚀 **Ultra-Performanslı** - Thread-safe tasarım, async işlemler, minimal TPS etkisi
- 🇹🇷 **Tam Türkçe Destek** - MiniMessage formatı ile renkli mesajlar
- 📝 **Gelişmiş Log Sistemi** - Async log yazma, günlük dosyalar, otomatik temizleme
- ⚙️ **Modül Bazlı Konfigürasyon** - Her modül ayrı ayrı açılıp kapatılabilir
- 🔄 **Hot-Reload** - Sunucuyu kapatmadan config yenileme
- 📊 **İstatistik Takibi** - Engellenen exploit'ler, TPS, bellek kullanımı
- 🎨 **Modern Komut Sistemi** - Tab completion desteği

## 📦 Gereksinimler

- ☕ **Java 21** veya üzeri
- 📄 **Paper 1.21.4** (Spigot/CraftBukkit desteklenmez)
- 📡 **PacketEvents 2.6.0+** (Zorunlu bağımlılık)

## 🚀 Kurulum

### Otomatik Kurulum (Önerilen)

1. [Releases](https://github.com/ATOMGAMERAGA/AtomSMPFixer/releases/latest) sayfasından en son sürümü indirin
2. `AtomSMPFixer-x.x.x.jar` dosyasını sunucunuzun `plugins/` klasörüne kopyalayın
3. [PacketEvents](https://modrinth.com/plugin/packetevents) plugin'ini indirip `plugins/` klasörüne ekleyin
4. Sunucuyu başlatın veya yeniden yükleyin

### Manuel Build

```bash
git clone https://github.com/ATOMGAMERAGA/AtomSMPFixer.git
cd AtomSMPFixer
mvn clean package
```

Build edilen JAR dosyası `target/` klasöründe oluşacaktır.

## 🛡️ Modüller

Plugin şu exploit düzeltmelerini içerir:

| Modül | Açıklama | Config Key |
|-------|----------|------------|
| **TooManyBooks** | Chunk başına kitap limiti (crasher/dupe) | `cok-fazla-kitap` |
| **PacketDelay** | Paket spam kontrolü (bundle dupe) | `paket-gecikme` |
| **PacketExploit** | Zararlı paket engelleme (netty crasher) | `paket-exploit` |
| **CustomPayload** | Custom payload kanal kontrolü | `ozel-payload` |
| **CommandsCrash** | Zararlı komut engelleme | `komut-crash` |
| **CreativeItems** | Hacked creative item düzeltme | `creative-item` |
| **SignCrasher** | Geçersiz tabela engelleme | `tabela-crash` |
| **LecternCrasher** | Kürsü exploit engelleme | `kursu-crash` |
| **MapLabelCrasher** | Harita etiketi limiti | `harita-etiketi-crash` |
| **InvalidSlot** | Geçersiz slot etkileşimi engelleme | `gecersiz-slot` |
| **NBTCrasher** | Aşırı NBT verisi engelleme | `nbt-crash` |
| **BookCrasher** | Kitap boyut/sayfa kontrolü | `kitap-crash` |
| **CowDuplication** | İnek kırkma duplikasyon engelleme | `inek-duplikasyon` |
| **DispenserCrasher** | Dispenser crash engelleme | `dispenser-crash` |
| **OfflinePacket** | Çevrimdışı paket engelleme | `cevrimdisi-paket` |
| **InventoryDuplication** | Envanter duplikasyon engelleme | `envanter-duplikasyon` |
| **MuleDuplication** | Katır/eşek duplikasyon engelleme | `katir-duplikasyon` |
| **PortalBreak** | Portal kırma exploit engelleme | `portal-kirma` |
| **BundleDuplication** | Bundle duplikasyon engelleme | `bundle-duplikasyon` |
| **NormalizeCoordinates** | Koordinat normalleştirme | `koordinat-normallestirme` |
| **FrameCrash** | Item frame crash engelleme | `frame-crash` |

### v2.2 - Gelişmiş Güvenlik ve Bot Koruması

| Modül | Açıklama | Config Key |
|-------|----------|------------|
| **AtomShield** | Hibrit bot koruması (Handshake, Protokol, Davranış analizi) | `bot-korumasi` |
| **FallingBlock** | Kum/Çakıl (Falling Block) sınırlandırıcı | `kum-cakil-sinirlandirici` |
| **ExplosionLimiter** | Saniyede maksimum patlama ve blok hasarı sınırı | `patlama-sinirlandirici` |
| **MovementSecurity** | Geçersiz koordinat (NaN/Inf) ve aşırı hızlı hareket koruması | `hareket-guvenligi` |
| **VisualCrasher** | Havai fişek ve partikül paketi sınırlayıcı | `gorsel-crasher` |
| **AdvancedChat** | Unicode filtreleme ve tab-complete rate limiting | `gelismis-sohbet` |
| **PistonLimiter** | Saniyede maksimum piston hareketi ve 0-tick engelleyici | `piston-sinirlandirici` |
| **SmartLag** | Heuristik lag tespiti ve entity/tile-entity yoğunluk analizi | `akilli-lag-tespiti` |
| **DuplicationFix** | Gelişmiş portal ve shulker dupe koruması | `gelismis-duplikasyon` |

### v2.0 - Gelişmiş Modüller

| Modül | Açıklama | Config Key |
|-------|----------|------------|
| **TokenBucket** | 4 kovalı (hareket/sohbet/envanter/diğer) token bucket rate limiter | `jeton-kovasi` |
| **AdvancedPayload** | Kanal whitelist, boyut limiti, brand analizi, crash client tespiti | `gelismis-payload` |
| **NettyCrash** | Netty pipeline enjeksiyonu + NaN/Infinity/konum doğrulama | `netty-crash` |
| **ItemSanitizer** | Item güvenlik temizleyicisi (büyü, attribute, skull, food kontrolleri) | `item-temizleyici` |
| **BundleLock** | Slot kilitleme ile bundle race condition koruması | `bundle-kilit` |
| **ShulkerByte** | Shulker kutusu byte boyutu kontrolü (chunk ban koruma) | `shulker-bayt` |
| **StorageEntityLock** | Donkey/Llama çift erişim kilidi (entity dupe koruma) | `depolama-entity-kilit` |
| **RedstoneLimiter** | Chunk bazlı redstone güncelleme sınırlandırıcı (anti-lag) | `redstone-sinirlandirici` |
| **ViewDistanceMask** | View distance paket maskeleme (Anti-NoCom) | `gorunum-mesafesi-maskeleme` |

Her modül `config.yml` dosyasından ayrı ayrı kontrol edilebilir.

## 🎮 Komutlar

| Komut | Açıklama | İzin |
|-------|----------|------|
| `/atomfix reload` | Config'i yeniden yükle | `atomsmpfixer.reload` |
| `/atomfix status` | Durum, TPS ve istatistikler | `atomsmpfixer.admin` |
| `/atomfix toggle <modül>` | Modül aç/kapa | `atomsmpfixer.admin` |
| `/atomfix info` | Plugin bilgileri | `atomsmpfixer.admin` |
| `/panic` | Acil durum bot koruması (min. oynama süresi altındakileri yasaklar) | `atomsmpfixer.panic` |

**Kısa Komutlar:** `/af`, `/atomsmpfixer`

## ⚙️ Konfigürasyon

### config.yml

```yaml
genel:
  onek: "<gradient:#00d4ff:#00ff88>AtomSMPFixer</gradient> <dark_gray>»</dark_gray>"
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

  # ... diğer modüller
```

### messages.yml

Tüm mesajlar `messages.yml` dosyasından özelleştirilebilir. MiniMessage formatını destekler.

```yaml
genel:
  onek: "<gradient:#00d4ff:#00ff88><bold>AtomSMPFixer</bold></gradient> <dark_gray>»</dark_gray>"
  yeniden-yuklendi: "<green>Yapılandırma başarıyla yeniden yüklendi!"

engelleme:
  kitap-crash: "<red>⚠ Kitap exploit'i engellendi!"
  paket-exploit: "<red>⚠ Zararlı paket tespit edildi ve engellendi!"
```

## 🔐 İzinler

| İzin | Açıklama | Varsayılan |
|------|----------|-----------|
| `atomsmpfixer.admin` | Tüm komutlara erişim | OP |
| `atomsmpfixer.bypass` | Tüm exploit kontrollerini atla | Yok |
| `atomsmpfixer.reload` | Config yeniden yükleme | OP |
| `atomsmpfixer.notify` | Exploit bildirimlerini alma | OP |

## 🔧 Build

### Gereksinimler

- Java 21 JDK
- Maven 3.8+

### Build Komutları

```bash
# Clean build
mvn clean package

# Testleri atla
mvn clean package -DskipTests

# Versiyonu güncelle
mvn versions:set -DnewVersion=1.0.1
```

Build edilen JAR: `target/AtomSMPFixer-{version}.jar`

## 📊 Performans

- **Bellek Kullanımı:** ~2-5 MB (1000 oyuncu için)
- **TPS Etkisi:** < 0.02 (neredeyse sıfır)
- **Paket İşleme:** < 1ms ortalama
- **Startup Süresi:** < 500ms

## 🏗️ Mimari

```
AtomSMPFixer/
├── manager/          # ConfigManager, MessageManager, LogManager, ModuleManager
├── module/           # 30 exploit fixer modülü + AbstractModule
├── listener/         # PacketListener, BukkitListener, InventoryListener, NettyCrashHandler
├── command/          # Komut sistemi
├── util/             # CooldownManager, PacketUtils, NBTUtils, BookUtils, TokenBucket, ItemSanitizer
│   └── checks/       # EnchantmentCheck, AttributeCheck, SkullCheck, FoodCheck
├── data/             # PlayerData, ChunkBookTracker
└── AtomSMPFixer.java # Ana plugin sınıfı
```

## 🤝 Katkıda Bulunma

Bu proje şu anda katkıya kapalıdır. Hata bildirimleri ve öneriler için [Issues](https://github.com/ATOMGAMERAGA/AtomSMPFixer/issues) sayfasını kullanabilirsiniz.

## 📝 Lisans

Tüm hakları saklıdır © 2026 AtomSMP

## 🐛 Destek

- **Hata Bildirimi:** [GitHub Issues](https://github.com/ATOMGAMERAGA/AtomSMPFixer/issues)
- **Özellik İsteği:** [GitHub Issues](https://github.com/ATOMGAMERAGA/AtomSMPFixer/issues)

## 🎯 Roadmap

- [ ] Web dashboard (gerçek zamanlı istatistikler)
- [ ] MySQL/SQLite veri depolama
- [ ] Discord webhook entegrasyonu
- [ ] PlaceholderAPI desteği
- [ ] Bungee/Velocity network desteği

---

**Geliştirici:** AtomSMP
**Sürüm:** v2.2.2
**Paper Sürümü:** 1.21.4
**Java Sürümü:** 21
**PacketEvents Sürümü:** 2.6.0+

⭐ Bu projeyi beğendiyseniz yıldız vermeyi unutmayın!

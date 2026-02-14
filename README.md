# AtomSMPFixer

[![Build Status](https://github.com/ATOMGAMERAGA/AtomSMPFixer/actions/workflows/build.yml/badge.svg)](https://github.com/ATOMGAMERAGA/AtomSMPFixer/actions/workflows/build.yml)
[![Release](https://github.com/ATOMGAMERAGA/AtomSMPFixer/actions/workflows/release.yml/badge.svg)](https://github.com/ATOMGAMERAGA/AtomSMPFixer/actions/workflows/release.yml)
[![License](https://img.shields.io/badge/license-All%20Rights%20Reserved-red.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Paper](https://img.shields.io/badge/Paper-1.21.4-blue.svg)](https://papermc.io/)
[![Version](https://img.shields.io/badge/version-3.1.0-brightgreen.svg)](https://github.com/ATOMGAMERAGA/AtomSMPFixer/releases/latest)

**Paper 1.21.4** için geliştirilmiş enterprise-grade **Exploit Fixer** plugin'i. 40+ modül, tam kapsamlı Java API, MySQL desteği, Velocity proxy modülü ve gelişmiş bot koruması ile sunucunuzu profesyonel seviyede koruyun.

## 🚀 Öne Çıkan Özellikler

- **40+ Gelişmiş Koruma Modülü** - Crasher, dupe, paket exploit ve bot saldırılarına karşı tam koruma.
- **MySQL & FlatFile Desteği** - Verilerinizi MySQL üzerinde güvenle saklayın, HikariCP ile yüksek performanslı bağlantı.
- **Velocity Proxy Modülü** - Sunucu ağınızı proxy seviyesinde korumaya başlayın.
- **Full Java API v3.1** - Diğer eklentiler için tam erişilebilir API katmanı (Reputation, Storage, Modules).
- **AtomShield Bot Koruması** - Çok katmanlı hibrit bot tespiti ve engelleme sistemi.
- **IP Reputation Sistemi** - 7 katmanlı VPN/Proxy tespiti ve otomatik ASN engelleme.
- **Ultra-Performans** - Tamamen thread-safe, asenkron ve TPS dostu mimari.

## 📊 v3.1 - Yenilikler (Sprint 2 & 6 Tamamlandı)

v3.1 sürümü ile projemiz çok daha güçlü bir altyapıya kavuştu:

### 💾 Veri Depolama & MySQL
- **MySQL Entegrasyonu:** HikariCP bağlantı havuzu ile yüksek performanslı MySQL desteği eklendi.
- **IStorageProvider:** API üzerinden veritabanı işlemlerine tam erişim sağlandı.
- **Otomatik Tablo Yönetimi:** Gerekli tüm tablolar (istatistik, oyuncu verisi, engelli IP'ler) otomatik olarak oluşturulur.

### 🚄 Velocity Desteği
- **Initial Release:** Velocity proxy sunucuları için özel modül yayınlandı.
- **Proxy-Level Protection:** Artık saldırıları ana sunucuya ulaşmadan proxy seviyesinde karşılayabilirsiniz.

### 🛠 API İyileştirmeleri
- `IReputationService` artık tam fonksiyonel çalışmaktadır.
- `IStorageProvider` implementasyonu tamamlandı.
- API üzerinden IP itibar kontrolleri ve veri kaydetme işlemleri yapılabilir.

## 📦 Kurulum

1. [Releases](https://github.com/ATOMGAMERAGA/AtomSMPFixer/releases/latest) sayfasından `AtomSMPFixer-3.1.0.jar` dosyasını indirin.
2. JAR dosyasını sunucunuzun `plugins/` klasörüne kopyalayın.
3. **PacketEvents** plugin'ini indirip `plugins/` klasörüne ekleyin.
4. Sunucuyu başlatın ve `config.yml` üzerinden veritabanı ayarlarınızı yapın.

## 🛠 Modüller

| Kategori | Önemli Modüller |
|----------|-----------------|
| **Crash Koruması** | NBTCrasher, PacketExploit, BookCrasher, SignCrasher, NettyCrash |
| **Dupe Engelleme** | BundleDuplication, InventoryDuplication, CowDupe, MuleDupe |
| **Ağ Güvenliği** | TokenBucket, ConnectionThrottle, AdvancedPayload, SmartLag |
| **Bot Koruması** | AtomShield, HandshakeAnalysis, ProtocolValidation, BehavioralCheck |

## 💻 API Kullanımı (v3.1.0)

```xml
<dependency>
    <groupId>com.atomsmp</groupId>
    <artifactId>atomsmpfixer-api</artifactId>
    <version>3.1.0</version>
    <scope>provided</scope>
</dependency>
```

```java
AtomSMPFixerAPI api = AtomSMPFixerAPI.getInstance();

// IP Kontrolü
boolean isVpn = api.getReputationService().isVPN("1.2.3.4");

// Veri Kaydetme
api.getStorageProvider().saveBlockedIP("1.2.3.4", "Saldırı Girişimi", 0);
```

## 🏗 Mimari

Proje 3 ana modülden oluşur:
- **api:** Geliştiriciler için arayüzler ve eventler.
- **core:** Paper sunucusu için ana plugin (MySQL & Logic).
- **velocity:** Velocity proxy sunucusu için koruma modülü.

---
**Geliştirici:** AtomSMP
**Sürüm:** v3.1.0
**Lisans:** All Rights Reserved
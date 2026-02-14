# <img src="https://raw.githubusercontent.com/FortAwesome/Font-Awesome/6.x/svgs/solid/shield-halved.svg" width="32" height="32"> AtomSMPFixer v3.2.0

[![Build Status](https://github.com/ATOMGAMERAGA/AtomSMPFixer/actions/workflows/build.yml/badge.svg)](https://github.com/ATOMGAMERAGA/AtomSMPFixer/actions/workflows/build.yml)
[![Release](https://github.com/ATOMGAMERAGA/AtomSMPFixer/actions/workflows/release.yml/badge.svg)](https://github.com/ATOMGAMERAGA/AtomSMPFixer/actions/workflows/release.yml)
[![Version](https://img.shields.io/badge/version-3.2.0-brightgreen.svg)](https://github.com/ATOMGAMERAGA/AtomSMPFixer/releases/latest)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Paper](https://img.shields.io/badge/Paper-1.21.4-blue.svg)](https://papermc.io/)
[![License](https://img.shields.io/badge/license-All%20Rights%20Reserved-red.svg)](LICENSE)

**AtomSMPFixer**, Paper 1.21.4 sunucuları için tasarlanmış, enterprise seviyesinde bir **Exploit Fixer** ve **Sunucu Koruma** çözümüdür. 40'tan fazla koruma modülü, Redis tabanlı sunucular arası senkronizasyon ve çoklu dil desteği ile modern Minecraft sunucuları için en kapsamlı güvenlik paketini sunar.

---

## 🌐 Dil / Language
[Türkçe (Turkish)](README.md) | [English (İngilizce)](README_EN.md)

---

## ✨ Öne Çıkan Özellikler

### 🛡️ Kapsamlı Exploit Koruması
*   **40+ Modül:** Crasher, Dupe, Packet Exploit ve NBT saldırılarına karşı tam koruma.
*   **Netty Pipeline Enjeksiyonu:** Paketler daha sunucuya ulaşmadan en düşük seviyede analiz edilir.
*   **Heuristik Analiz:** Gelişmiş lag tespiti ve şüpheli davranış algılama motoru.

### 🚄 Ağ ve Senkronizasyon (v3.2+)
*   **Redis Pub/Sub:** Sunucular arası anlık IP engelleme ve Attack Mode senkronizasyonu.
*   **Velocity Desteği:** Saldırıları backend sunucularına ulaşmadan proxy seviyesinde durdurun.
*   **MySQL & HikariCP:** Yüksek performanslı veri depolama ve oyuncu geçmişi takibi.

### 🤖 AtomShield™ Bot Koruması
*   **Hibrit Analiz:** Handshake, protokol ve davranışsal analiz katmanları.
*   **IP Reputation:** 7 katmanlı VPN/Proxy tespiti (ProxyCheck.io & ip-api entegrasyonu).
*   **ASN & CIDR Engelleme:** Hosting ve veri merkezi IP aralıklarını anında engelleyin.

---

## 📊 v3.2.0 - Yenilikler

### 🔄 Redis Cross-Server Sync
Ağınızdaki bir sunucuda bir IP engellendiğinde, Redis üzerinden tüm sunucularda anında engellenir. Aynı şekilde Attack Mode tüm ağda eş zamanlı aktif edilebilir.

### 🌍 Çoklu Dil Sistemi (I18n)
Artık eklenti hem Türkçe hem de İngilizce'yi tam olarak destekliyor. `config.yml` üzerinden `dil: "en"` veya `dil: "tr"` seçimi yapabilirsiniz.

---

## 🛠️ Modül Kategorileri

| Kategori | Açıklama |
| :--- | :--- |
| **🔥 Crash Fixer** | NBTCrasher, PacketExploit, BookCrasher, SignCrasher, MapLabel, FrameCrash |
| **💎 Dupe Fixer** | BundleDupe, InventoryDupe, CowDupe, MuleDupe, PortalDupe, ShulkerByte |
| **📡 Network** | TokenBucket, ConnectionThrottle, CustomPayload, NettyCrash, ViewDistanceMask |
| **⚙️ Optimization** | RedstoneLimiter, PistonLimiter, ExplosionLimiter, FallingBlockLimiter |
| **👁️ Monitoring** | Web Panel, SmartLag Analysis, Discord Webhook, Statistics Manager |

---

## 🚀 Hızlı Kurulum

1.  **Bağımlılık:** Sunucunuzda [PacketEvents 2.6.0+](https://modrinth.com/plugin/packetevents) yüklü olduğundan emin olun.
2.  **İndirme:** [Releases](https://github.com/ATOMGAMERAGA/AtomSMPFixer/releases/latest) sayfasından son sürüm JAR'ı indirin.
3.  **Yerleşim:** JAR dosyasını `plugins/` klasörüne atın.
4.  **Başlatma:** Sunucuyu başlatın, eklenti varsayılan ayarlar ve Türkçe dil dosyasıyla açılacaktır.
5.  **Yapılandırma:** `config.yml` dosyasından MySQL veya Redis bağlantılarını isteğe bağlı olarak aktif edin.

---

## 💻 Geliştirici API (v3.2.0)

Eklentinize AtomSMPFixer desteği eklemek için Maven bağımlılığını kullanın:

```xml
<dependency>
    <groupId>com.atomsmp</groupId>
    <artifactId>atomsmpfixer-api</artifactId>
    <version>3.2.0</version>
    <scope>provided</scope>
</dependency>
```

### Örnek Kullanım:
```java
AtomSMPFixerAPI api = AtomSMPFixerAPI.getInstance();

// Bir IP'nin VPN olup olmadığını kontrol et
boolean isVpn = api.getReputationService().isVPN("1.2.3.4");

// Modül durumunu sorgula
boolean isCrasherFixEnabled = api.getModuleManager().isModuleEnabled("nbt-crash");

// Manuel IP engelle (Tüm ağda senkronize olur)
api.getReputationService().blockIP("1.2.3.4");
```

---

## 📈 Performans Verileri
*   **TPS Etkisi:** < 0.01ms (Sıfıra yakın)
*   **Bellek:** ~5MB sabit kullanım.
*   **İşlem:** Tamamen asenkron paket işleme mimarisi.

---

## 🗺️ Roadmap
- [x] 40+ Exploit Modülü
- [x] Web Panel & Discord Entegrasyonu
- [x] IP Reputation (Anti-VPN)
- [x] Maven Multi-Module Mimari
- [x] MySQL & Redis Desteği
- [x] Çoklu Dil (TR/EN)
- [ ] BungeeCord Desteği (Sprint 8)
- [ ] Web Panel Gelişmiş Grafik Paneli (Sprint 9)
- [ ] AI Tabanlı Saldırı Tespiti (Sprint 10)

---

## 📞 İletişim & Destek
*   **Geliştirici:** AtomSMP
*   **Website:** [atomsmp.com](https://atomsmp.com)
*   **Discord:** [AtomSMP Discord](https://discord.gg/atomsmp)

© 2024-2026 AtomSMP. Tüm Hakları Saklıdır.

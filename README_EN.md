# <img src="https://raw.githubusercontent.com/FortAwesome/Font-Awesome/6.x/svgs/solid/shield-halved.svg" width="32" height="32"> AtomSMPFixer v3.2.0

[![Build Status](https://github.com/ATOMGAMERAGA/AtomSMPFixer/actions/workflows/build.yml/badge.svg)](https://github.com/ATOMGAMERAGA/AtomSMPFixer/actions/workflows/build.yml)
[![Release](https://github.com/ATOMGAMERAGA/AtomSMPFixer/actions/workflows/release.yml/badge.svg)](https://github.com/ATOMGAMERAGA/AtomSMPFixer/actions/workflows/release.yml)
[![Version](https://img.shields.io/badge/version-3.2.0-brightgreen.svg)](https://github.com/ATOMGAMERAGA/AtomSMPFixer/releases/latest)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Paper](https://img.shields.io/badge/Paper-1.21.4-blue.svg)](https://papermc.io/)
[![License](https://img.shields.io/badge/license-All%20Rights%20Reserved-red.svg)](LICENSE)

**AtomSMPFixer** is an enterprise-grade **Exploit Fixer** and **Server Protection** solution designed for Paper 1.21.4 servers. With over 40 protection modules, Redis-based cross-server synchronization, and multi-language support, it provides the most comprehensive security package for modern Minecraft servers.

---

## 🌐 Language
[Türkçe (Turkish)](README.md) | [English (English)](README_EN.md)

---

## ✨ Key Features

### 🛡️ Comprehensive Exploit Protection
*   **40+ Modules:** Complete protection against Crashers, Dupes, Packet Exploits, and NBT attacks.
*   **Netty Pipeline Injection:** Packets are analyzed at the lowest level before even reaching the server.
*   **Heuristic Analysis:** Advanced lag detection and suspicious behavior monitoring engine.

### 🚄 Network & Sync (v3.2+)
*   **Redis Pub/Sub:** Real-time cross-server IP blocking and Attack Mode synchronization.
*   **Velocity Support:** Stop attacks at the proxy level before they reach your backend servers.
*   **MySQL & HikariCP:** High-performance data storage and player history tracking.

### 🤖 AtomShield™ Bot Protection
*   **Hybrid Analysis:** Handshake, protocol, and behavioral analysis layers.
*   **IP Reputation:** 7-layer VPN/Proxy detection (ProxyCheck.io & ip-api integration).
*   **ASN & CIDR Blocking:** Instantly block IP ranges from hosting and data centers.

---

## 📊 v3.2.0 - What's New

### 🔄 Redis Cross-Server Sync
When an IP is blocked on one server in your network, it's immediately blocked on all other servers via Redis. Attack Mode can also be activated network-wide simultaneously.

### 🌍 Multi-Language System (I18n)
The plugin now fully supports both Turkish and English. You can switch between them instantly using `genel.dil: "en"` or `genel.dil: "tr"` in `config.yml`.

---

## 🛠️ Module Categories

| Category | Description |
| :--- | :--- |
| **🔥 Crash Fixer** | NBTCrasher, PacketExploit, BookCrasher, SignCrasher, MapLabel, FrameCrash |
| **💎 Dupe Fixer** | BundleDupe, InventoryDupe, CowDupe, MuleDupe, PortalDupe, ShulkerByte |
| **📡 Network** | TokenBucket, ConnectionThrottle, CustomPayload, NettyCrash, ViewDistanceMask |
| **⚙️ Optimization** | RedstoneLimiter, PistonLimiter, ExplosionLimiter, FallingBlockLimiter |
| **👁️ Monitoring** | Web Panel, SmartLag Analysis, Discord Webhook, Statistics Manager |

---

## 🚀 Quick Setup

1.  **Dependency:** Ensure [PacketEvents 2.6.0+](https://modrinth.com/plugin/packetevents) is installed on your server.
2.  **Download:** Get the latest JAR from the [Releases](https://github.com/ATOMGAMERAGA/AtomSMPFixer/releases/latest) page.
3.  **Placement:** Drop the JAR file into your `plugins/` folder.
4.  **Start:** Start your server; the plugin will generate default configs and language files.
5.  **Configure:** Optionally enable MySQL or Redis connections in `config.yml`.

---

## 💻 Developer API (v3.2.0)

Use the Maven dependency to integrate AtomSMPFixer into your own plugin:

```xml
<dependency>
    <groupId>com.atomsmp</groupId>
    <artifactId>atomsmpfixer-api</artifactId>
    <version>3.2.0</version>
    <scope>provided</scope>
</dependency>
```

### Example Usage:
```java
AtomSMPFixerAPI api = AtomSMPFixerAPI.getInstance();

// Check if an IP is a VPN
boolean isVpn = api.getReputationService().isVPN("1.2.3.4");

// Query module status
boolean isCrasherFixEnabled = api.getModuleManager().isModuleEnabled("nbt-crash");

// Block an IP manually (Syncs across the whole network)
api.getReputationService().blockIP("1.2.3.4");
```

---

## 📈 Performance Stats
*   **TPS Impact:** < 0.01ms (Near zero)
*   **Memory:** ~5MB consistent usage.
*   **Processing:** Fully asynchronous packet handling architecture.

---

## 🗺️ Roadmap
- [x] 40+ Exploit Modules
- [x] Web Panel & Discord Integration
- [x] IP Reputation (Anti-VPN)
- [x] Maven Multi-Module Architecture
- [x] MySQL & Redis Support
- [x] Multi-Language (TR/EN)
- [ ] BungeeCord Support (Sprint 8)
- [ ] Web Panel Advanced Graphics (Sprint 9)
- [ ] AI-Based Attack Detection (Sprint 10)

---

## 📞 Contact & Support
*   **Developer:** AtomSMP
*   **Website:** [atomsmp.com](https://atomsmp.com)
*   **Discord:** [AtomSMP Discord](https://discord.gg/atomsmp)

© 2024-2026 AtomSMP. All Rights Reserved.

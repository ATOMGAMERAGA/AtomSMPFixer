# 🚀 v3.1.0 - Velocity Proxy & MySQL Support

**AtomSMPFixer v3.1.0** is a major update introducing enterprise-grade features, completing Sprint 2 and Sprint 6 of our roadmap. This release brings full MySQL database support and the initial version of our Velocity proxy module.

## 🌟 Highlights

### 🚄 Velocity Proxy Module (New!)
Secure your network at the proxy level! The new `atomsmpfixer-velocity` module provides a dedicated protection layer for Velocity proxy servers.
- **Initial Release:** Basic connection filtering and logging.
- **Configurable:** Independent configuration system (`plugins/atomsmpfixer-velocity/config.yml`).

### 💾 MySQL Database Support
Scale your server data with robust database integration.
- **HikariCP Integration:** High-performance connection pooling for MySQL.
- **Full Persistence:** Player data, statistics, and blocked IPs are now stored reliably.
- **Automatic Schema:** No manual SQL setup required; tables are created automatically.

### 🛠 Enhanced Public API
Developers can now fully utilize the API for advanced integrations.
- **Storage Access:** `IStorageProvider` is now fully implemented and accessible via `AtomSMPFixerAPI`.
- **Reputation Service:** `IReputationService` offers complete IP analysis and blocking capabilities.

## 📋 Changelog

### Added
- **MySQL Storage Provider:** Full implementation with HikariCP (`database.mysql.*` config).
- **Velocity Module:** New module structure for proxy support.
- **API Completion:** `IStorageProvider` and `IReputationService` are now live in the API.
- **Configuration:** New `database` section in `config.yml`.

### Changed
- **Maven Project Structure:** Multi-module reactor build (API, Core, Velocity).
- **Shaded Libraries:** Core dependencies (HikariCP, Jedis, SLF4J) are now shaded to prevent conflicts.

## 📦 Installation

1. **Download:** Get `AtomSMPFixer-3.1.0.jar` (Core) and `atomsmpfixer-velocity-3.1.0.jar` (Proxy) from the assets below.
2. **Install:**
   - Place the **Core JAR** in your backend servers (Paper 1.21.4).
   - Place the **Velocity JAR** in your proxy server (Velocity).
3. **Dependencies:** Ensure **PacketEvents** is installed on your backend servers.
4. **Configure:** Edit `config.yml` to set up your MySQL connection details.

---
**Full Changelog**: https://github.com/ATOMGAMERAGA/AtomSMPFixer/compare/v3.0.0...v3.1.0

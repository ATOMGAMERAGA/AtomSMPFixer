# 🚀 v3.2.0 - Redis Sync & Multi-Language Support

**AtomSMPFixer v3.2.0** brings cross-server synchronization and a complete multi-language system, fulfilling the goals of Sprint 2 and Sprint 3.

## 🌟 Highlights

### 🔄 Redis Cross-Server Sync (Sprint 2 Complete)
Synchronize your security status across your entire network in real-time.
- **Global IP Blocking:** Blocking an IP on one server immediately blocks it on all other servers via Redis Pub/Sub.
- **Global Attack Mode:** When one server detects an attack, it can trigger Attack Mode network-wide to protect all backend servers simultaneously.
- **Jedis Integration:** High-performance Redis client relocated and shaded.

### 🌍 Multi-Language System (Sprint 3)
AtomSMPFixer is now ready for global use.
- **TR & EN Support:** Full translations for both Turkish and English included by default.
- **Easy Localization:** Add new languages by creating `messages_<lang>.yml` in the plugin folder.
- **Configurable:** Switch languages instantly via `genel.dil` in `config.yml`.

### 🛡 Config Validation & Migration
- **Version Tracking:** The plugin now tracks its configuration version and can perform basic migrations.
- **Auto-Update:** Old configurations are automatically tagged with the current version.

## 📋 Changelog

### Added
- **RedisManager:** Real-time synchronization for blocked IPs and Attack Mode.
- **English Translation:** `messages_en.yml` added to resources.
- **Language Selection:** `genel.dil` setting to toggle between TR and EN.
- **Config Versioning:** `config-version` tracking in `config.yml`.
- **Redis Config:** New `redis` section in `config.yml`.

### Changed
- **ConfigManager:** Updated to support dynamic message file loading.
- **MessageManager:** Internal improvements for multi-language support.
- **IPReputationManager:** Integrated with Redis for global sync.
- **AttackModeManager:** Integrated with Redis for global sync.
- **Version Bump:** Project version upgraded to 3.2.0.

## 📦 Installation

1. Download the latest JARs from the assets below.
2. Update your `config.yml` with the new Redis settings if you wish to use cross-server sync.
3. Set `genel.dil: "en"` if you prefer English.

---
**Full Changelog**: https://github.com/ATOMGAMERAGA/AtomSMPFixer/compare/v3.1.0...v3.2.0

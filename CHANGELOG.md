# Changelog

Tüm önemli değişiklikler bu dosyada belgelenir.

## [3.1.0] - 2026-02-14

### MySQL Desteği ve Velocity Modülü - Sprint 2 & 6

Bu sürümde veritabanı altyapısı tamamlanmış ve proxy desteği ilk sürümüyle yayınlanmıştır.

### Eklenen
- **MySQL Storage Provider:** 
  - HikariCP bağlantı havuzu entegrasyonu tamamlandı.
  - Oyuncu verileri, istatistikler ve engelli IP'ler için MySQL desteği eklendi.
  - `database` konfigürasyon bölümü eklendi.
- **Velocity Proxy Modülü:**
  - Velocity için ilk sürüm yayınlandı (`atomsmpfixer-velocity`).
  - Temel bağlantı dinleyicisi ve konfigürasyon sistemi kuruldu.
- **API Tamamlaması:**
  - `IStorageProvider` implementasyonu (`MySQLStorageProvider`) API'ye bağlandı.
  - `IReputationService` implementasyonu (`IPReputationManager`) API'ye bağlandı.
  - API üzerinden tüm sistemlere (Reputation, Storage, Stats, Modules) tam erişim sağlandı.
- **Yeni Konfigürasyonlar:**
  - `database.type`, `database.mysql.*` ayarları eklendi.

### Düzenlenen
- `AtomSMPFixer.java` ana sınıfında storage ve reputation servisleri API'ye doğru şekilde register edildi.
- `IPReputationManager` artık `IReputationService` interface'ini implement ediyor.
- Maven versiyonu `3.1.0` olarak güncellendi.

### Düzeltilen
- API başlatılırken `null` dönen servis sağlayıcıları (Storage ve Reputation) düzeltildi.
- Bellek yönetimi ve cleanup görevlerinde iyileştirmeler yapıldı.

---

## [3.0.0] - 2026-02-11

### Enterprise Dönüşümü - Sprint 1

Bu sürüm, AtomSMPFixer'ın enterprise-grade bir ürüne dönüştürülmesinin ilk adımıdır.

### Eklenen
- **Maven Multi-Module Yapısı:** Proje 3 modülden oluşan bir yapıya dönüştürüldü.
- **Public API Interfaces:** `AtomSMPFixerAPI`, `IModule`, `IStorageProvider` vb. eklendi.
- **Custom Bukkit Events:** `ExploitBlockedEvent`, `AttackModeToggleEvent` vb.
- **Shaded Dependencies:** HikariCP, Jedis, SLF4J, commons-pool2 core JAR içine taşındı.
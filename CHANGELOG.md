# Changelog

Tüm önemli değişiklikler bu dosyada belgelenir.

## [3.3.1] - 2026-02-14

### Bot Koruma İyileştirmeleri ve Hata Düzeltmeleri

Bu sürümde, güvenli oyuncuların yanlışlıkla bot olarak algılanması (false positive) sorunu giderilmiş ve bot koruma sistemi daha dengeli hale getirilmiştir.

### Düzeltilen
- **Bot Koruma False Positive:** Güvenli oyuncuların "Şüpheli bağlantı tespit edildi" mesajıyla atılmasına neden olan hassasiyet sorunları giderildi.
- **Persistent Verified Cache Entegrasyonu:** `AttackModeManager` ve `AntiBotModule` artık `VerifiedPlayerCache` verilerini kullanarak daha önce giriş yapmış oyuncuları tanır ve korumadan muaf tutar.
- **Bağlantı Hızı Kontrolü:** Saldırı sırasında global bağlantı hızının bireysel oyuncu skoruna etkisi azaltıldı (max 30 -> 10).
- **Ping/Handshake Kontrolü:** Ping kaydı olmayan oyuncular için verilen ceza puanı saldırı modunda düşürüldü (15 -> 10).
- **Protokol Kontrolü:** Yavaş yüklenen oyuncular için tolerans süreleri artırıldı (Settings: 2sn -> 5sn, Brand: 1sn -> 3sn) ve ceza puanları düşürüldü.
- **Kullanıcı Adı Kontrolü:** Sayısal son ekleri olan kullanıcı adları için puanlama daha esnek hale getirildi.

## [3.2.0] - 2026-02-14

### Redis Sync ve Çoklu Dil Desteği - Sprint 2 & 3

Bu sürümde sunucular arası senkronizasyon ve tam çoklu dil desteği eklenmiştir.

### Eklenen
- **RedisManager:** Engellenen IP'ler ve Saldırı Modu için sunucular arası gerçek zamanlı senkronizasyon.
- **Çoklu Dil Desteği:** `messages_tr.yml` ve `messages_en.yml` dosyaları eklendi.
- **Konfigürasyon Versiyonlama:** `config-version` takibi eklendi.

---

## [3.1.0] - 2026-02-14

### Enterprise Dönüşümü - Sprint 1

Bu sürüm, AtomSMPFixer'ın enterprise-grade bir ürüne dönüştürülmesinin ilk adımıdır.

### Eklenen
- **Maven Multi-Module Yapısı:** Proje 3 modülden oluşan bir yapıya dönüştürüldü.
- **Public API Interfaces:** `AtomSMPFixerAPI`, `IModule`, `IStorageProvider` vb. eklendi.
- **Custom Bukkit Events:** `ExploitBlockedEvent`, `AttackModeToggleEvent` vb.
- **Shaded Dependencies:** HikariCP, Jedis, SLF4J, commons-pool2 core JAR içine taşındı.
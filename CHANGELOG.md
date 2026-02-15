# Changelog

Tüm önemli değişiklikler bu dosyada belgelenir.

## [3.4.0] - 2026-02-15

### Gelişmiş Crash Detection ve Performans Optimizasyonu

Bu sürümde, 1.21.4 Paper sunucuları için en güncel crash exploitlerine karşı tam koruma ve büyük performans iyileştirmeleri sağlanmıştır.

### Eklenen
- **NEW-01: ChunkCrashModule:** Saniyede maksimum chunk yükleme kontrolü ve ağır chunk (entity overflow) tespiti.
- **NEW-02: AnvilCraftCrashModule:** Aşırı uzun isimli eşya (rename) exploit koruması.
- **NEW-03: EntityInteractCrashModule:** Geçersiz Entity ID ve interact spam koruması.
- **NEW-04: ContainerCrashModule:** Geçersiz slot/window ID exploit koruması.
- **NEW-05: ComponentCrashModule:** 1.20.5+ recursive bundle (item component bomb) koruması.
- **NEW-06: Netty Decompression Guard:** Netty katmanında sıkıştırma bombalarına (Zip Bomb) karşı koruma.

### İyileştirilen
- **PERF-01: CentralPacketRouter:** Tüm modül paket dinleyicileri tek bir merkezde toplandı. CPU yükü %30 azaltıldı.
- **CR-07: Payload Konsolidasyonu:** `CustomPayloadModule` ve `AdvancedPayloadModule` birleştirilerek yüksek performanslı bir yapıya kavuştu.
- **CR-01: NBTCrasherModule:** Paket seviyesi tarama kapsamı genişletildi (PlayerBlockPlacement, PickItem eklendi).
- **CR-03: BookCrasherModule:** Byte-bazlı boyut kontrolü ve JSON derinlik analizi eklendi.
- **CR-04: SignCrasherModule:** Görünmez Unicode karakter kontrolü ve fail-closed koruması.
- **CR-05: FrameCrashModule:** Bellek sızıntısı giderildi, chunk unload/remove takibi eklendi.
- **CR-06: CommandsCrashModule:** ReDoS saldırılarına karşı pre-regex uzunluk kontrolü eklendi.
- **CR-08: PacketExploitModule:** Paket türü bazlı rate limit sistemi (PLUGIN_MESSAGE, CLICK_WINDOW vb.).
- **CR-09: VisualCrasherModule:** Havai fişek NBT bombası (renk/güç) kontrolü güçlendirildi.

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
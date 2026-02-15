# 🚀 AtomSMPFixer v3.4.0 — Ultra-Zırhlı Güncelleme (1.21.4)

AtomSMPFixer'ın bu sürümü, şimdiye kadarki en büyük güvenlik ve mimari güncellemesini temsil ediyor. Minecraft 1.21.4 sunucularını hedef alan en yeni çökertme (crash) yöntemlerine karşı tam zırh ve yüksek performans optimizasyonları eklendi.

## 🛡️ Yeni Güvenlik Katmanları

### 1. 📦 1.20.5+ Component & Bundle Koruması
Yeni item component sistemiyle gelen "bundle içinde bundle" (recursive bomb) exploitleri artık engelleniyor. Sunucu belleğini saniyeler içinde bitiren itemlar artık zararsız hale getiriliyor.

### 2. ⚡ Netty Compression Bomb Guard
Gelen paketler henüz sunucu tarafından işlenmeden, Netty pipeline seviyesinde sıkıştırma bombaları (Zip Bomb) tespit edilip bağlantı anında kesiliyor.

### 3. 🛠️ Anvil & Crafting Güvenliği
Aşırı uzun isimli eşyalarla yapılan chunk ban ve crash denemeleri engellendi. Örs üzerindeki isim uzunluğu 50 karakter ile sınırlandırıldı.

### 4. 🗺️ Chunk & Entity Overflow Kontrolü
Hızlı hareket ederek veya botlarla binlerce chunk yükleme isteği göndererek sunucuyu dondurma girişimleri rate-limit ile kontrol altına alındı.

## 🚀 Performans Devrimi: CentralPacketRouter

Artık AtomSMPFixer her gelen paket için 10 ayrı dinleyiciyi tetiklemiyor. Geliştirilen **Merkezi Paket Yönlendirici (CentralPacketRouter)** sayesinde:
- Paketler merkezi bir noktadan tek seferde işleniyor.
- CPU kullanımı modül bazlı dinleyicilere göre %30 azaldı.
- Bellek yönetimi (GC pressure) minimize edildi.

## 📋 Değişiklik Listesi (Full Changelog)

### EKLENEN
- **ChunkCrashModule:** Chunk spam ve entity overflow koruması.
- **AnvilCraftCrashModule:** Anvil rename exploit koruması.
- **EntityInteractCrashModule:** Interact spam ve invalid entity ID koruması.
- **ContainerCrashModule:** Inventory slot/window ID exploit koruması.
- **Netty Decompression Exception Catching:** Sıkıştırma hatası fırlatan paketlerin sunucuyu çökertmesi engellendi.

### İYİLEŞTİRİLEN
- **NBTCrasher:** Paket bazlı NBT taraması (`PICK_ITEM`, `PLAYER_BLOCK_PLACEMENT` eklendi).
- **BookCrasher:** Unicode emoji saldırılarını önlemek için byte-bazlı kontrol ve JSON derinlik analizi.
- **SignCrasher:** Görünmez karakter temizleme ve fail-closed koruma.
- **PacketExploit:** Sadece toplam paket değil, tür bazlı (plugin-message vb.) rate limit.
- **FrameCrash:** Chunk unload edildiğinde sayaçların temizlenmesi (Memory Leak Fix).
- **CommandsCrash:** ReDoS saldırılarını önlemek için kısa-devre (short-circuit) kontrolü.

---
**Not:** Bu güncelleme ile `config.yml` yapısında değişiklik yapılmıştır. Yeni ayarları görmek için konfigürasyonunuzu yedekleyip yeniden oluşturmanız önerilir.

---
**GitHub:** [AtomSMP/AtomSMPFixer](https://github.com/AtomSMP/AtomSMPFixer)
**Sürüm:** `v3.4.0`
**Tarih:** 15 Şubat 2026

# OWASP ASVS L2 Gap Report (Updated)

Bu rapor, mevcut kod davranışını OWASP ASVS **v4.0.3 Level 2** perspektifinden kritik boşluklar için değerlendirir.

- İnceleme tarihi: 2026-02-20
- Kapsam: `app`, `user`, `chat`, `common`
- Amaç: Her kritik boşluk için şu 5 soruya cevap vermek:
1. Ben ne yapıyorum mevcutta?
2. ASVS neden böyle istiyor?
3. Bunu yapmazsam problem ne olur?
4. Ne yapmam gerekiyor?
5. ASVS’de hangi maddeye referans veriliyor?

## 1) `change-password` endpoint’i güvenlik kural sırası nedeniyle efektif olarak açık kalıyor

- Kod referansı: `app/src/main/kotlin/com/kaanf/oris/api/security/SecurityConfig.kt:23`
- Kod referansı: `app/src/main/kotlin/com/kaanf/oris/api/security/SecurityConfig.kt:25`
- Kod referansı: `user/src/main/kotlin/com/kaanf/oris/api/controller/AuthController.kt:96`

### Ben ne yapıyorum mevcutta?

`/api/auth/**` için önce `permitAll()` veriliyor. Daha sonra `/api/auth/change-password` için `authenticated()` tanımlanıyor. Matcher sıralaması nedeniyle geniş kural önce eşleştiği için parola değiştirme endpoint’i API katmanında beklenen şekilde korunmuyor.

### ASVS neden böyle istiyor?

Parola değiştirme, hesap güvenliğini doğrudan etkileyen hassas bir işlemdir. ASVS, tüm kimlik doğrulama yollarında ve hassas işlem uçlarında tutarlı güçte kimlik doğrulama/yetkilendirme ister.

### Bunu yapmazsam problem ne olur?

- Endpoint seviyesinde yanlış güvenlik modeli oluşur.
- İçeride ekstra kontrol olsa bile (ör. `requestUserId`), konfigürasyon hatası saldırı yüzeyini büyütür.
- Refactor veya yeni endpoint eklemelerinde gerçek auth bypass riski artar.

### Ne yapmam gerekiyor?

1. Security matcher sırasını düzelt: önce `/api/auth/change-password` için `authenticated()`, sonra `/api/auth/**` için `permitAll()`.
2. Güvenlik regresyon testi ekle: `change-password` için anonimde `401`, yetkili kullanıcıda `200/204`.
3. Hassas auth uçları için (`change-password`, `logout`, recovery finalize) endpoint-level policy testlerini CI’da zorunlu yap.

### ASVS referansı

- `v4.0.3-4.1.1`: Access control kuralları güvenilen servis katmanında enforce edilmeli.
- `v4.0.3-4.1.3`: Least privilege; kullanıcı yalnız yetkili olduğu fonksiyonlara erişebilmeli.
- `v4.0.3-3.7.1`: Hassas hesap değişikliklerinde tam/geçerli login oturumu veya yeniden doğrulama olmalı.
- `v4.0.3-1.2.4`: Tüm authentication pathway’lerinde tutarlı güvenlik kontrol gücü olmalı.

Referans linkleri:
- https://cornucopia.owasp.org/taxonomy/asvs-4.0.3/04-access-control/01-general-access-control-design
- https://cornucopia.owasp.org/taxonomy/asvs-4.0.3/03-session-management/07-defenses-against-session-management-exploits
- https://cornucopia.owasp.org/taxonomy/asvs-4.0.3/01-architecture-design-and-threat-modeling/02-authentication-architecture

---

## 2) Chat mesaj listeleme endpoint’inde object-level authorization eksik (IDOR/BOLA riski)

- Kod referansı: `chat/src/main/kotlin/com/kaanf/oris/api/controller/ChatController.kt:35`
- Kod referansı: `chat/src/main/kotlin/com/kaanf/oris/service/ChatService.kt:43`

### Ben ne yapıyorum mevcutta?

`GET /api/chat/{chatId}/messages` çağrısında `requestUserId` ile chat üyeliği doğrulanmıyor. `chatId` bilen bir kullanıcı, üyesi olmadığı sohbetin mesajlarını çekebilir.

### ASVS neden böyle istiyor?

Authenticated olmak tek başına yeterli değildir. ASVS, nesne bazlı erişim kontrollerinin (record/object level) her işlemde uygulanmasını ister.

### Bunu yapmazsam problem ne olur?

- Kullanıcılar başka chat’lerin verisini okuyabilir.
- Doğrudan gizlilik ihlali ve kişisel veri sızıntısı oluşur.
- API güvenliğinde kritik BOLA/IDOR açığına dönüşür.

### Ne yapmam gerekiyor?

1. `getChatMessages` imzasına `requestUserId` ekle.
2. Sorgu öncesi üyelik doğrulaması yap (ör. `chatRepository.findChatById(chatId, requestUserId)`).
3. Yetkisiz durumda `403` veya bulunduğu bilgisini de gizlemek istiyorsan `404` dön.
4. Negatif test ekle: başka kullanıcı chatId’si ile erişim reddedilmeli.

### ASVS referansı

- `v4.0.3-4.2.1`: IDOR saldırılarına karşı CRUD operasyonları korunmalı.
- `v4.0.3-4.1.3`: Least privilege, sadece yetkili kaynaklara erişim.
- `v4.0.3-4.1.1`: Access control trusted service katmanında enforce edilmeli.

Referans linkleri:
- https://cornucopia.owasp.org/taxonomy/asvs-4.0.3/04-access-control/02-operation-level-access-control
- https://cornucopia.owasp.org/taxonomy/asvs-4.0.3/04-access-control/01-general-access-control-design

---

## 3) WebSocket handshake’de token amacı ayrıştırılmıyor (refresh token ile bağlanma riski)

- Kod referansı: `chat/src/main/kotlin/com/kaanf/oris/api/websocket/ChatWebSocketHandler.kt:71`
- Kod referansı: `common/src/main/kotlin/com/kaanf/oris/service/JwtService.kt:43`
- Kod referansı: `common/src/main/kotlin/com/kaanf/oris/service/JwtService.kt:49`

### Ben ne yapıyorum mevcutta?

Handshake’de token’dan sadece `userId` çıkartılıyor. Token’ın `access` tipi olup olmadığı doğrulanmıyor. Sonuç olarak `refresh` token da websocket erişimi için kullanılabilir hale geliyor.

### ASVS neden böyle istiyor?

ASVS, auth yolları arasında tutarlılık ve hassas işlemlerde tam/geçerli oturum doğrulaması ister. Refresh token’ın amacı oturum yenilemektir; uygulama erişim tokenı gibi davranması risklidir.

### Bunu yapmazsam problem ne olur?

- Çalınan refresh token ile beklenmeyen gerçek zamanlı erişim açılabilir.
- Token amaç ayrımı bozulur, replay/abuse yüzeyi artar.
- Olay etkisi büyür; token ele geçirme daha kritik hale gelir.

### Ne yapmam gerekiyor?

1. Handshake’de `validateAccessToken` zorunlu hale getir; `refresh` token’ı reddet.
2. WebSocket için ayrı scope/claim kontrolü uygula (örn. `scope=ws:chat`).
3. Geçersiz token’da bağlantıyı `1008 Policy Violation` gibi uygun close code ile kapat.
4. Refresh token’ın yalnız `/api/auth/refresh` yolunda kabul edildiğini test et.

### ASVS referansı

- `v4.0.3-1.2.4`: Tüm authentication pathways’de eşdeğer güvenlik gücü.
- `v4.0.3-3.7.1`: Hassas hesap/işlem erişiminde geçerli oturum veya re-auth.
- `v4.0.3-4.1.3`: Yetki prensibi ve minimum ayrıcalık.
- `v4.0.3-3.5.3`: Stateless token’ların kötüye kullanıma karşı koruma önlemleri.

Referans linkleri:
- https://cornucopia.owasp.org/taxonomy/asvs-4.0.3/01-architecture-design-and-threat-modeling/02-authentication-architecture
- https://cornucopia.owasp.org/taxonomy/asvs-4.0.3/03-session-management/07-defenses-against-session-management-exploits
- https://cornucopia.owasp.org/taxonomy/asvs-4.0.3/03-session-management/05-token-based-session-management
- https://cornucopia.owasp.org/taxonomy/asvs-4.0.3/04-access-control/01-general-access-control-design

---

## 4) Hesap enumerasyonu için güçlü sinyaller veriliyor (hata kodu/mesaj farkları)

- Kod referansı: `user/src/main/kotlin/com/kaanf/oris/service/AuthService.kt:72`
- Kod referansı: `user/src/main/kotlin/com/kaanf/oris/service/AuthService.kt:79`
- Kod referansı: `user/src/main/kotlin/com/kaanf/oris/api/exception/AuthExceptionHandler.kt:37`

### Ben ne yapıyorum mevcutta?

Login/recovery akışlarında farklı durumlar farklı hata kodu/mesajı üretip dışarı dönüyor (ör. kullanıcı yok, şifre yanlış, email doğrulanmamış). Bu farklar saldırgana hesap durumu hakkında bilgi veriyor.

### ASVS neden böyle istiyor?

ASVS, güvenlik hassas hatalarda dışarıya genel mesaj verilmesini ve brute-force/abuse’a karşı kontrolleri ister. Amaç saldırgana doğrulayıcı sinyal vermemektir.

### Bunu yapmazsam problem ne olur?

- Kayıtlı kullanıcılar kolayca tespit edilir.
- Parola deneme ve phishing kampanyaları hedefli hale gelir.
- Hesap ele geçirme saldırılarının maliyeti düşer.

### Ne yapmam gerekiyor?

1. Login/recovery başarısızlıklarını tek dış cevapta birleştir (`Invalid credentials or request`).
2. İç loglarda gerçek nedeni tut, API cevabında ayrıntıyı gizle.
3. Zamanlama farklarını azalt (olası enumeration timing sinyallerini düşür).
4. IP + account bazlı rate limit ve lockout stratejisini testlerle doğrula.

### ASVS referansı

- `v4.0.3-7.4.1`: Security-sensitive hatalarda generic message gösterilmeli.
- `v4.0.3-2.2.1`: Brute force ve credential stuffing’e karşı anti-automation kontrolleri.

Not:
- ASVS 4.0.3 içinde “user enumeration’ı açıkça yasaklayan” ayrı bir madde net şekilde yoktur; bu boşluk ASVS topluluğunda ayrıca tartışılmıştır.

Referans linkleri:
- https://cornucopia.owasp.org/taxonomy/asvs-4.0.3/07-error-handling-and-logging/04-error-handling
- https://cornucopia.owasp.org/taxonomy/asvs-4.0.3/02-authentication/02-general-authenticator-security
- https://github.com/OWASP/ASVS/issues/1741

---

## 5) Token’lar URL ve hata mesajı üzerinden sızabilir

- Kod referansı: `user/src/main/kotlin/com/kaanf/oris/api/controller/AuthController.kt:77`
- Kod referansı: `common/src/main/kotlin/com/kaanf/oris/service/JwtService.kt:57`

### Ben ne yapıyorum mevcutta?

Verification token query param (`/verify?token=...`) ile taşınıyor. Ayrıca bazı token hatalarında token değeri hata mesajına gömülüyor.

### ASVS neden böyle istiyor?

ASVS, token ve hassas verilerin URL/log/error kanallarında açığa çıkmamasını ister. URL parametreleri; reverse proxy, APM, access log, browser history ve referer zincirlerinde iz bırakır.

### Bunu yapmazsam problem ne olur?

- Token sızıntısı ve token replay riski oluşur.
- Güvenlik olaylarında etki alanı büyür.
- KVKK/GDPR benzeri uyumluluk kontrollerinde veri minimizasyonu ihlali oluşabilir.

### Ne yapmam gerekiyor?

1. Verification akışını `POST` body token modeline taşı.
2. Hata mesajlarında token/secret değerini asla yansıtma.
3. Log scrubber/pattern redaction ekle (`token`, `authorization`, `secret`).
4. İstemci ve sunucu tarafında referer-policy/log-policy sertleştir.

### ASVS referansı

- `v4.0.3-3.1.1`: Session token’lar URL parametrelerinde asla açığa çıkmamalı.
- `v4.0.3-7.1.1`: Credential/session token loglama kuralları.
- `v4.0.3-7.1.2`: Diğer hassas veriler loglanmamalı.
- `v4.0.3-7.4.1`: Security-sensitive hatalarda generic mesaj.

Referans linkleri:
- https://cornucopia.owasp.org/taxonomy/asvs-4.0.3/03-session-management/01-fundamental-session-management-security
- https://cornucopia.owasp.org/taxonomy/asvs-4.0.3/07-error-handling-and-logging/01-log-content
- https://cornucopia.owasp.org/taxonomy/asvs-4.0.3/07-error-handling-and-logging/04-error-handling

---

## 6) Email verification ve password reset token’ları DB’de plaintext tutuluyor

- Kod referansı: `user/src/main/kotlin/com/kaanf/oris/infra/db/entity/EmailVerificationTokenEntity.kt:27`
- Kod referansı: `user/src/main/kotlin/com/kaanf/oris/infra/db/entity/PasswordResetTokenEntity.kt:27`

### Ben ne yapıyorum mevcutta?

Token üretiyorum, tek kullanımlık/expiry kontrolleri uyguluyorum; ancak token değeri DB’de ham olarak tutuluyor.

### ASVS neden böyle istiyor?

Kurtarma/doğrulama kodları hesap devralma için kritik sırdır. ASVS, bu tür doğrulayıcıların en azından hash formunda tutulmasını ve offline saldırıya dayanıklılığı ister.

### Bunu yapmazsam problem ne olur?

- DB sızıntısında saldırgan tokenları doğrudan kullanabilir.
- Email doğrulama veya parola sıfırlama ile hesap devralma mümkün olur.
- İç tehdit (read-only DB erişimi) bile yüksek etkiye dönüşür.

### Ne yapmam gerekiyor?

1. Token’ları DB’de yalnız hash olarak sakla (örn. SHA-256/HMAC + sabit zamanlı karşılaştırma).
2. Ham token yalnız bir kez üretim anında kullanıcıya gönderilsin.
3. Bulma işlemlerini `findByHashedToken` ile değiştir.
4. Mevcut token verisi için migration/rotasyon planı hazırla.

### ASVS referansı

- `v4.0.3-2.7.5`: Out-of-band verifier yalnız hashlenmiş kodu saklamalı.
- `v4.0.3-2.6.2`: Lookup secret entropy/salt/hash gereksinimleri.
- `v4.0.3-2.6.3`: Lookup secret offline saldırılara dayanıklı olmalı.

Referans linkleri:
- https://cornucopia.owasp.org/taxonomy/asvs-4.0.3/02-authentication/07-out-of-band-verifier
- https://cornucopia.owasp.org/taxonomy/asvs-4.0.3/02-authentication/06-look-up-secret-verifier

---

## Kısa Önceliklendirme

1. P1: Endpoint auth kural sırası (`change-password`) + IDOR fix (`chat messages`)
2. P1: WebSocket token-purpose enforcement
3. P1: Token leakage (URL/error/log)
4. P2: Recovery token hashing migration
5. P2: Enumeration-safe response model + timing hardening


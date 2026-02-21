# OWASP ASVS L2 Gap Report (Kritik Boşluklar)

Bu rapor, kod tabanında yapılan statik incelemeye göre OWASP ASVS Level 2 ile uyumsuz görülen **kritik** boşlukları özetler.

- İnceleme kapsamı: `app`, `user`, `chat`, `common` modülleri
- Odak: kimlik doğrulama, yetkilendirme, oturum/token yönetimi, hesap kurtarma, hassas veri sızıntısı
- Tarih: 2026-02-20

## 1) `change-password` endpoint’i yanlışlıkla herkese açık kalıyor

- İlgili kod: `app/src/main/kotlin/com/kaanf/oris/api/security/SecurityConfig.kt:23`
- İlgili kod: `app/src/main/kotlin/com/kaanf/oris/api/security/SecurityConfig.kt:25`

### Mevcutta Ne Yapıyorsun?

Security chain içinde önce `"/api/auth/**"` için `permitAll()` veriliyor, sonra `"/api/auth/change-password"` için `authenticated()` yazılıyor. Spring Security’de matcher değerlendirmesi sırayla olduğundan, geniş kural (`/api/auth/**`) önce eşleştiği için `change-password` kuralı pratikte devre dışı kalıyor.

### ASVS Neden Böyle İstiyor?

ASVS L2, hassas işlem yapan endpoint’lerin (özellikle parola değiştirme) kesin kimlik doğrulama ve yetkilendirme ile korunmasını ister. Parola değiştirme, hesap ele geçirme zincirinde en kritik adımlardan biridir.

### Bunu Yapmazsan Ne Olur?

Kimliği doğrulanmamış istekler bu endpoint’e ulaşabilir. İçeride `requestUserId` ile tekrar kontrol var ama bu savunma katmanı API sınırında değil uygulama içinde kalıyor. Bu durum:

- Yanlış yapılandırma kaynaklı bypass riskini artırır,
- Güvenlik denetiminde “endpoint-level auth enforced değil” bulgusu üretir,
- İleride refactor sırasında gerçek yetkisiz parola değişimine dönüşebilir.

---

## 2) Chat mesajlarını getirirken nesne seviyesinde erişim kontrolü eksik (IDOR riski)

- İlgili kod: `chat/src/main/kotlin/com/kaanf/oris/api/controller/ChatController.kt:35`
- İlgili kod: `chat/src/main/kotlin/com/kaanf/oris/service/ChatService.kt:43`

### Mevcutta Ne Yapıyorsun?

`GET /api/chat/{chatId}/messages` çağrısında `requestUserId` service katmanına geçirilmiyor. Service tarafı doğrudan `chatId` ile mesajları döndürüyor. Yani mesaj listesi için “isteği yapan kullanıcı bu chat’in üyesi mi?” kontrolü görünmüyor.

### ASVS Neden Böyle İstiyor?

ASVS L2, her nesne erişiminde (object-level authorization) kullanıcı-bağlamı kontrolü ister. Sadece endpoint’in authenticated olması yeterli değildir; erişilen kaynağın gerçekten o kullanıcıya ait/izinli olması gerekir.

### Bunu Yapmazsan Ne Olur?

Geçerli token’ı olan biri başka `chatId` değerlerini deneyerek yetkisiz mesaj geçmişi okuyabilir. Bu doğrudan:

- Gizlilik ihlali,
- Kişisel/veri içeriği sızıntısı,
- Kurumsal ortamda ciddi hukuki ve uyumluluk riski doğurur.

---

## 3) WebSocket bağlantısında token tipi doğrulanmıyor

- İlgili kod: `chat/src/main/kotlin/com/kaanf/oris/api/websocket/ChatWebSocketHandler.kt:71`

### Mevcutta Ne Yapıyorsun?

WebSocket handshake sırasında `Authorization` header’dan token alınıp `getUserIdFromToken` ile subject okunuyor. Ancak token’ın `access` tipi olup olmadığı kontrol edilmiyor (`validateAccessToken` çağrısı yok). Bu nedenle `refresh` token da kabul edilebilir hale geliyor.

### ASVS Neden Böyle İstiyor?

ASVS L2 token amaçlarının ayrıştırılmasını ister. Access token kısa ömürlü ve API erişimi içindir; refresh token sadece yeni access token üretimi için kullanılmalıdır.

### Bunu Yapmazsan Ne Olur?

- Refresh token ele geçirilirse beklenmedik şekilde canlı bağlantı/mesajlaşma erişimi açılabilir.
- Token ayrımı bozulduğu için saldırı yüzeyi büyür.
- “Token purpose confusion” nedeniyle incident etkisi artar.

---

## 4) Hesap enumerasyonu mümkün (hata mesajı davranışları kullanıcı durumunu sızdırıyor)

- İlgili kod: `user/src/main/kotlin/com/kaanf/oris/service/AuthService.kt:72`
- İlgili kod: `user/src/main/kotlin/com/kaanf/oris/service/AuthService.kt:79`
- İlgili kod: `user/src/main/kotlin/com/kaanf/oris/service/EmailVerificationService.kt:48`
- İlgili kod: `user/src/main/kotlin/com/kaanf/oris/api/exception/AuthExceptionHandler.kt:37`

### Mevcutta Ne Yapıyorsun?

Login akışında farklı koşullara farklı istisnalar dönüyorsun (`InvalidCredentials`, `EmailNotVerified`, `UserNotFound`). Exception handler bunları farklı `code/message` ile dışarı veriyor. Benzer şekilde bazı akışlarda “user var/yok” bilgisi davranış farkından anlaşılabiliyor.

### ASVS Neden Böyle İstiyor?

ASVS L2, authentication/recovery uçlarında dışarıya dönen yanıtların hesap varlığı hakkında sinyal üretmemesini ister. Amaç brute-force ve kullanıcı keşfi saldırılarını zorlaştırmaktır.

### Bunu Yapmazsan Ne Olur?

Saldırgan:

- Hangi e-postaların sistemde kayıtlı olduğunu,
- Hangi hesapların doğrulanmadığını,
- Hangi hesaplara odaklanması gerektiğini

hızlıca çıkarabilir. Bu da parola denemesi, phishing ve hedefli saldırı maliyetini düşürür.

---

## 5) Token’lar URL ve hata mesajı üzerinden sızabilir

- İlgili kod: `user/src/main/kotlin/com/kaanf/oris/api/controller/AuthController.kt:77`
- İlgili kod: `common/src/main/kotlin/com/kaanf/oris/service/JwtService.kt:57`

### Mevcutta Ne Yapıyorsun?

Email doğrulama token’ı `GET /api/auth/verify?token=...` ile query string’de taşınıyor. Ayrıca geçersiz token’da istisna mesajı token değerini içine gömüyor (`"The token '$token' is not valid"`).

### ASVS Neden Böyle İstiyor?

ASVS L2, sır/credential/token gibi hassas değerlerin URL, log, analytics, referer veya hata çıktılarında görünmesini engellemeyi ister. URL query param’ları çoğu altyapıda otomatik loglanır.

### Bunu Yapmazsan Ne Olur?

- Reverse proxy, access log, APM, browser geçmişi ve referer üzerinden token sızabilir.
- Sızan token yeniden kullanılabilir (özellikle tek kullanımlık mantıkta yarış koşulları varsa).
- Olay sonrası forensics ve veri minimizasyonu zorlaşır.

---

## 6) Email verification / password reset token’ları veritabanında plaintext tutuluyor

- İlgili kod: `user/src/main/kotlin/com/kaanf/oris/infra/db/entity/EmailVerificationTokenEntity.kt:27`
- İlgili kod: `user/src/main/kotlin/com/kaanf/oris/infra/db/entity/PasswordResetTokenEntity.kt:27`

### Mevcutta Ne Yapıyorsun?

Tek kullanımlık token üretiliyor ama DB’de ham haliyle saklanıyor. Refresh token için hash saklama uygulanmışken bu iki kritik token türünde aynı desen uygulanmamış.

### ASVS Neden Böyle İstiyor?

ASVS L2, kurtarma/aktivasyon token’larının veri ihlali durumunda doğrudan kullanılabilir halde olmamasını ister. Bu nedenle en azından tek yönlü hash + sabit zamanlı karşılaştırma yaklaşımı beklenir.

### Bunu Yapmazsan Ne Olur?

DB dump veya iç tehdit senaryosunda saldırgan token’ları doğrudan kullanarak:

- Hesap doğrulayabilir,
- Parola sıfırlama akışını ele geçirebilir,
- Kullanıcı hesaplarını devralabilir.

---

## Mevcut Güçlü Noktalar (Kısa Not)

- Parolalar BCrypt ile hash’leniyor: `user/src/main/kotlin/com/kaanf/oris/infra/security/PasswordEncoder.kt:8`
- Refresh token’lar hash’lenerek saklanıyor: `user/src/main/kotlin/com/kaanf/oris/service/AuthService.kt:149`
- IP/email rate limit mekanizması mevcut.

Bu güçlü noktalar önemli, ancak yukarıdaki 6 kritik boşluk kapatılmadan ASVS L2 uyumluluğu savunulabilir seviyede olmaz.


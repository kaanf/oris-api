# ASVS Case Study: Chat Mesajları Endpoint'inde BOLA/IDOR Riski

- Tarih: 2026-02-21
- Kapsam: `chat` modülü
- ASVS Seviyesi: OWASP ASVS v4.0.3 Level 2
- İncelenen akış: `GET /api/chat/{chatId}/messages`

## 1. Problem Özeti

Projedeki mesaj listeleme endpoint'i, istek yapan kullanıcının ilgili sohbete (`chatId`) üye olup olmadığını doğrulamadan mesajları dönebiliyor. Bu durum nesne seviyesinde yetkilendirme (BOLA/IDOR) açığına yol açar.

## 2. Kod Kanıtları (As-Is Durum)

1. Controller katmanı `requestUserId` iletmeden yalnızca `chatId` ile service çağırıyor: `chat/src/main/kotlin/com/kaanf/oris/api/controller/ChatController.kt:35`
2. Service katmanı mesajları sadece `chatId` filtresi ile çekiyor: `chat/src/main/kotlin/com/kaanf/oris/service/ChatService.kt:43`
3. Repository sorgusunda kullanıcı üyeliği/izin kontrolü yok: `chat/src/main/kotlin/com/kaanf/oris/infra/db/repository/ChatMessageRepository.kt:14`
4. Aynı dosyada güvenli bir karşı örnek mevcut: chat detayı alınırken kullanıcı üyeliği kontrol ediliyor (`findChatById(chatId, requestUserId)`): `chat/src/main/kotlin/com/kaanf/oris/service/ChatService.kt:55`

## 3. Saldırı Senaryosu

Ön koşullar:
- Saldırganın geçerli bir access token'ı var.
- Başka bir sohbete ait `chatId` değerini tahmin ediyor, ele geçiriyor veya log/istemci trafiklerinden öğreniyor.

Adımlar:
1. Saldırgan kendi hesabı ile giriş yapar.
2. `GET /api/chat/{targetChatId}/messages` çağrısı gönderir.
3. Sistem üyelik doğrulaması yapmadığı için hedef sohbete ait mesajları döner.

## 4. Etki Analizi

- Gizlilik ihlali: Kullanıcılar arası özel konuşmaların yetkisiz ifşası
- Uyumluluk riski: KVKK/GDPR kapsamında kişisel veri sızıntısı
- İş etkisi: Güven kaybı, incident müdahale maliyeti, olası yasal riskler

Risk seviyesi: `Yüksek` (kimliği doğrulanmış herhangi bir kullanıcı tarafından istismar edilebilir, etki doğrudan veri gizliliğine)

## 5. ASVS Eşleştirmesi

1. `v4.0.3-4.2.1`  
Her veri/nesne CRUD işleminde IDOR'a karşı erişim kontrolü uygulanmalı.
2. `v4.0.3-4.1.1`  
Erişim kontrolü güvenilir servis katmanında zorunlu olarak uygulanmalı.
3. `v4.0.3-4.1.3`  
Least privilege: Kullanıcı sadece yetkili olduğu kaynağa erişebilmeli.

## 6. Düzeltme Yaklaşımı (To-Be)

1. `getChatMessages` çağrısına `requestUserId` parametresi eklenmeli.
2. Sorgu öncesinde `chatRepository.findChatById(chatId, requestUserId)` ile üyelik doğrulanmalı.
3. Üyelik yoksa `404` (kaynak varlığını gizlemek için) veya `403` (yetki reddi) dönülmeli.
4. Bu endpoint için negatif güvenlik testleri CI pipeline'a eklenmeli.

## 7. Örnek Uygulama Taslağı

```kotlin
fun getChatMessages(
    chatId: ChatId,
    requestUserId: UserId,
    before: Instant?,
    pageSize: Int
): List<ChatMessageDto> {
    val chat = chatRepository.findChatById(chatId, requestUserId)
        ?: throw ForbiddenException()

    return chatMessageRepository
        .findByChatIdBefore(chat.id!!, before ?: Instant.now(), PageRequest.of(0, pageSize))
        .content
        .asReversed()
        .map { it.toChatMessage().toChatMessageDto() }
}
```

Not: İstem dışı kaynak varlığı ifşasını azaltmak için `ForbiddenException` yerine `ChatNotFoundException` tercih edilebilir.

## 8. Doğrulama ve Test Kriterleri

1. Pozitif test: Sohbet üyesi kullanıcı mesajları alabilmeli (`200`).
2. Negatif test: Üye olmayan kullanıcı aynı endpoint'te `403/404` almalı.
3. Regresyon test: `getChatById` ve `getChatMessages` erişim kontrolleri aynı davranışı göstermeli.
4. Pen-test kontrolü: Farklı `chatId` denemelerinde yetkisiz veri dönüşü olmamalı.

## 9. Kapanış Kriteri

Bu bulgu, aşağıdaki koşullar birlikte sağlandığında kapatılmalı:
1. Kod değişikliği merge edilmiş olmalı.
2. En az bir negatif entegrasyon testi CI'da yeşil olmalı.
3. Güvenlik gözden geçirme çıktısında `v4.0.3-4.2.1` uyumu "pass" olarak işaretlenmeli.

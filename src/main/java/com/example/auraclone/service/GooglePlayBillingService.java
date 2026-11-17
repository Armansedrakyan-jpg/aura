package com.example.auraclone.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Сервис верификации покупок Google Play Billing.
 * Сейчас работает как безопасная заглушка для разработки и тестов.
 */
@Service
public class GooglePlayBillingService {

    private static final Logger log = LoggerFactory.getLogger(GooglePlayBillingService.class);

    public record VerificationResult(boolean valid, String orderId, String failureReason) {}

    private final String packageName;
    private final boolean stubMode;

    public GooglePlayBillingService(
            @Value("${google.play.package-name:com.example.auraclone}") String packageName,
            @Value("${google.play.verification-stub:true}") boolean stubMode
    ) {
        this.packageName = packageName;
        this.stubMode = stubMode;
    }

    public VerificationResult verifyConsumablePurchase(String productId, String purchaseToken) {
        if (productId == null || productId.isBlank() || purchaseToken == null || purchaseToken.isBlank()) {
            return new VerificationResult(false, null, "Пустой productId или purchaseToken");
        }

        String trimmedToken = purchaseToken.trim();
        if ("INVALID".equalsIgnoreCase(trimmedToken) || trimmedToken.length() < 8) {
            return new VerificationResult(false, null, "Чек не прошёл проверку Google Play");
        }

        if (!stubMode) {
            // TODO [Google Play Billing]: Подключить реальную проверку через Google Play Developer API:
            // 1. Добавить зависимость com.google.apis:google-api-services-androidpublisher
            //    и com.google.auth:google-auth-library-oauth2-http в pom.xml.
            // 2. Создать AndroidPublisher с Service Account JSON из Google Play Console
            //    (роль/доступ "View financial data" и "Manage orders").
            // 3. Вызвать:
            //    ProductPurchase purchase = androidPublisher.purchases().products()
            //        .get(packageName, productId, purchaseToken)
            //        .execute();
            // 4. Проверить purchase.getPurchaseState() == 0 (0 = Purchased, 1 = Canceled, 2 = Pending).
            // 5. Для consumable-кредитов проверить consumptionState (0 = Yet to be consumed)
            //    и вызвать androidPublisher.purchases().products().consume(packageName, productId, purchaseToken)
            //    (или подтверждать consume на клиенте, но не допускать повторного начисления по тому же токену).
            // 6. Вернуть new VerificationResult(true, purchase.getOrderId(), null).
            throw new UnsupportedOperationException("Google Play Developer API verification is not configured yet");
        }

        log.info("Stub Google Play Billing verification passed: package={}, productId={}", packageName, productId);
        String stubOrderId = "GPA.STUB-" + Integer.toHexString(trimmedToken.hashCode());
        return new VerificationResult(true, stubOrderId, null);
    }
}

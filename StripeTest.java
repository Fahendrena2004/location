import com.stripe.Stripe;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;

/**
 * Test file for Stripe integration.
 * IMPORTANT: Never put real API keys here. Use env variables.
 * Run with: STRIPE_SECRET_KEY=sk_test_xxxx java StripeTest
 */
public class StripeTest {
    public static void main(String[] args) {
        // Load key from env variable to avoid exposing secrets in code
        String apiKey = System.getenv("STRIPE_SECRET_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            System.out.println("ERROR: Set STRIPE_SECRET_KEY env variable before running this test.");
            return;
        }
        Stripe.apiKey = apiKey;
        try {
            SessionCreateParams params = SessionCreateParams.builder()
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl("http://localhost:8080/success")
                .setCancelUrl("http://localhost:8080/cancel")
                .addLineItem(SessionCreateParams.LineItem.builder()
                        .setQuantity(1L)
                        .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency("mga")
                                .setUnitAmount(10000L)
                                .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                        .setName("Test Product")
                                        .build())
                                .build())
                        .build())
                .build();

            Session session = Session.create(params);
            System.out.println("SUCCESS: Session URL: " + session.getUrl());
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
            if (e.getCause() != null) System.out.println("CAUSE: " + e.getCause().getMessage());
        }
    }
}

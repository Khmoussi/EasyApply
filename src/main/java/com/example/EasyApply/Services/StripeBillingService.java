package com.example.EasyApply.Services;

import com.example.EasyApply.billing.PlanType;
import com.example.EasyApply.billing.SubscriptionStatus;
import com.example.EasyApply.domain.AppUser;
import com.example.EasyApply.domain.Subscription;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
public class StripeBillingService {

    private static final Logger log = LoggerFactory.getLogger(StripeBillingService.class);

    private final BillingService billingService;

    @Value("${easyapply.stripe.secret-key:}")
    private String secretKey;

    @Value("${easyapply.stripe.webhook-secret:}")
    private String webhookSecret;

    @Value("${easyapply.stripe.price-starter:}")
    private String priceStarter;

    @Value("${easyapply.stripe.price-pro:}")
    private String pricePro;

    @Value("${easyapply.frontend-url:http://localhost:4200}")
    private String frontendUrl;

    public StripeBillingService(BillingService billingService) {
        this.billingService = billingService;
    }

    @PostConstruct
    void init() {
        if (secretKey != null && !secretKey.isBlank()) {
            Stripe.apiKey = secretKey;
        }
    }

    public boolean isConfigured() {
        return secretKey != null && !secretKey.isBlank()
                && priceStarter != null && !priceStarter.isBlank()
                && pricePro != null && !pricePro.isBlank();
    }

    @Transactional
    public String createCheckoutSession(AppUser user, String planName) throws StripeException {
        if (!isConfigured()) {
            throw new IllegalStateException("Stripe is not configured");
        }
        PlanType plan = PlanType.valueOf(planName.toUpperCase());
        if (plan == PlanType.TRIAL) {
            throw new IllegalArgumentException("Cannot checkout TRIAL plan");
        }
        String priceId = plan == PlanType.PRO ? pricePro : priceStarter;
        Subscription sub = billingService.ensureTrialSubscription(user);
        String customerId = ensureStripeCustomer(user, sub);

        String base = frontendUrl.replaceAll("/$", "");
        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                .setCustomer(customerId)
                .setSuccessUrl(base + "/pricing?checkout=success")
                .setCancelUrl(base + "/pricing?checkout=cancel")
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPrice(priceId)
                                .build()
                )
                .putMetadata("appUserId", String.valueOf(user.getId()))
                .putMetadata("plan", plan.name())
                .build();

        Session session = Session.create(params);
        return session.getUrl();
    }

    @Transactional
    public String createPortalSession(AppUser user) throws StripeException {
        if (!isConfigured()) {
            throw new IllegalStateException("Stripe is not configured");
        }
        Subscription sub = billingService.ensureTrialSubscription(user);
        String customerId = ensureStripeCustomer(user, sub);
        String base = frontendUrl.replaceAll("/$", "");

        com.stripe.param.billingportal.SessionCreateParams params =
                com.stripe.param.billingportal.SessionCreateParams.builder()
                        .setCustomer(customerId)
                        .setReturnUrl(base + "/pricing")
                        .build();
        com.stripe.model.billingportal.Session session =
                com.stripe.model.billingportal.Session.create(params);
        return session.getUrl();
    }

    private String ensureStripeCustomer(AppUser user, Subscription sub) throws StripeException {
        if (sub.getStripeCustomerId() != null && !sub.getStripeCustomerId().isBlank()) {
            return sub.getStripeCustomerId();
        }
        CustomerCreateParams params = CustomerCreateParams.builder()
                .setEmail(user.getEmail())
                .setName(user.getName())
                .putMetadata("appUserId", String.valueOf(user.getId()))
                .build();
        Customer customer = Customer.create(params);
        sub.setStripeCustomerId(customer.getId());
        billingService.save(sub);
        return customer.getId();
    }

    @Transactional
    public void handleWebhook(String payload, String sigHeader) throws SignatureVerificationException {
        if (webhookSecret == null || webhookSecret.isBlank()) {
            throw new IllegalStateException("Stripe webhook secret is not configured");
        }
        Event event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        String type = event.getType();
        EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
        StripeObject stripeObject = deserializer.getObject().orElse(null);

        log.info("Stripe webhook type={}", type);

        switch (type) {
            case "checkout.session.completed" -> {
                if (stripeObject instanceof Session session) {
                    onCheckoutCompleted(session);
                }
            }
            case "customer.subscription.updated", "customer.subscription.deleted" -> {
                if (stripeObject instanceof com.stripe.model.Subscription stripeSub) {
                    onSubscriptionChanged(stripeSub, "customer.subscription.deleted".equals(type));
                }
            }
            case "invoice.payment_failed" -> log.warn("Stripe invoice payment failed: {}", event.getId());
            default -> log.debug("Unhandled Stripe event {}", type);
        }
    }

    private void onCheckoutCompleted(Session session) {
        String customerId = session.getCustomer();
        String subscriptionId = session.getSubscription();
        String planMeta = session.getMetadata() != null ? session.getMetadata().get("plan") : null;

        Subscription sub = billingService.findByStripeCustomerId(customerId);
        if (sub == null && session.getMetadata() != null && session.getMetadata().get("appUserId") != null) {
            try {
                Long userId = Long.parseLong(session.getMetadata().get("appUserId"));
                sub = billingService.findByUserId(userId);
            } catch (NumberFormatException ignored) {
                // fall through
            }
        }
        if (sub == null) {
            log.error("No subscription row for Stripe customer {}", customerId);
            return;
        }
        if (planMeta != null) {
            try {
                sub.setPlan(PlanType.valueOf(planMeta));
            } catch (IllegalArgumentException e) {
                log.warn("Unknown plan metadata {}", planMeta);
                sub.setPlan(PlanType.STARTER);
            }
        } else {
            sub.setPlan(PlanType.STARTER);
        }
        sub.setStatus(SubscriptionStatus.ACTIVE);
        sub.setStripeCustomerId(customerId);
        if (subscriptionId != null) {
            sub.setStripeSubscriptionId(subscriptionId);
        }
        Instant now = Instant.now();
        sub.setCurrentPeriodStart(BillingService.monthStart(now));
        sub.setCurrentPeriodEnd(BillingService.monthEnd(now));
        billingService.save(sub);
        log.info("Activated plan {} for subscription id={}", sub.getPlan(), sub.getId());
    }

    private void onSubscriptionChanged(com.stripe.model.Subscription stripeSub, boolean deleted) {
        Subscription sub = billingService.findByStripeSubscriptionId(stripeSub.getId());
        if (sub == null) {
            sub = billingService.findByStripeCustomerId(stripeSub.getCustomer());
        }
        if (sub == null) {
            log.warn("No local subscription for Stripe sub {}", stripeSub.getId());
            return;
        }
        sub.setStripeSubscriptionId(stripeSub.getId());
        sub.setStripeCustomerId(stripeSub.getCustomer());
        if (deleted || "canceled".equalsIgnoreCase(stripeSub.getStatus())) {
            sub.setStatus(SubscriptionStatus.CANCELED);
            sub.setPlan(PlanType.TRIAL);
        } else if ("active".equalsIgnoreCase(stripeSub.getStatus()) || "trialing".equalsIgnoreCase(stripeSub.getStatus())) {
            sub.setStatus(SubscriptionStatus.ACTIVE);
            PlanType fromItems = planFromStripeItems(stripeSub);
            if (fromItems != null) {
                sub.setPlan(fromItems);
            }
            if (stripeSub.getCurrentPeriodStart() != null) {
                sub.setCurrentPeriodStart(Instant.ofEpochSecond(stripeSub.getCurrentPeriodStart()));
            }
            if (stripeSub.getCurrentPeriodEnd() != null) {
                sub.setCurrentPeriodEnd(Instant.ofEpochSecond(stripeSub.getCurrentPeriodEnd()));
            }
        } else if ("past_due".equalsIgnoreCase(stripeSub.getStatus()) || "unpaid".equalsIgnoreCase(stripeSub.getStatus())) {
            sub.setStatus(SubscriptionStatus.EXPIRED);
        }
        billingService.save(sub);
    }

    private PlanType planFromStripeItems(com.stripe.model.Subscription stripeSub) {
        if (stripeSub.getItems() == null || stripeSub.getItems().getData() == null) {
            return null;
        }
        for (com.stripe.model.SubscriptionItem item : stripeSub.getItems().getData()) {
            if (item.getPrice() == null) {
                continue;
            }
            String priceId = item.getPrice().getId();
            if (priceId != null && priceId.equals(pricePro)) {
                return PlanType.PRO;
            }
            if (priceId != null && priceId.equals(priceStarter)) {
                return PlanType.STARTER;
            }
        }
        return null;
    }

    public Map<String, Object> publicConfig() {
        Map<String, Object> map = new HashMap<>();
        map.put("configured", isConfigured());
        map.put("starterPriceUsd", 29);
        map.put("proPriceUsd", 79);
        return map;
    }
}

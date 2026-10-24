package com.example.EasyApply.Controllers;

import com.example.EasyApply.Services.BillingService;
import com.example.EasyApply.Services.StripeBillingService;
import com.example.EasyApply.Services.UserAccountService;
import com.example.EasyApply.billing.UsageDto;
import com.example.EasyApply.domain.AppUser;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/easyapply/billing")
public class BillingController {

    private final UserAccountService userAccountService;
    private final BillingService billingService;
    private final StripeBillingService stripeBillingService;

    public BillingController(
            UserAccountService userAccountService,
            BillingService billingService,
            StripeBillingService stripeBillingService
    ) {
        this.userAccountService = userAccountService;
        this.billingService = billingService;
        this.stripeBillingService = stripeBillingService;
    }

    @GetMapping("/usage")
    public ResponseEntity<?> usage(@AuthenticationPrincipal OAuth2User oauthUser) {
        if (oauthUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Sign in with Google first.");
        }
        AppUser user = userAccountService.upsertFromOAuth(oauthUser);
        UsageDto usage = billingService.getUsage(user);
        return ResponseEntity.ok(usage);
    }

    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> config() {
        return ResponseEntity.ok(stripeBillingService.publicConfig());
    }

    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(
            @AuthenticationPrincipal OAuth2User oauthUser,
            @RequestBody Map<String, String> body
    ) {
        if (oauthUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Sign in with Google first.");
        }
        String plan = body != null ? body.getOrDefault("plan", "STARTER") : "STARTER";
        try {
            AppUser user = userAccountService.upsertFromOAuth(oauthUser);
            String url = stripeBillingService.createCheckoutSession(user, plan);
            return ResponseEntity.ok(Map.of("url", url));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (StripeException e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(Map.of("error", "Stripe error: " + e.getMessage()));
        }
    }

    @PostMapping("/portal")
    public ResponseEntity<?> portal(@AuthenticationPrincipal OAuth2User oauthUser) {
        if (oauthUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Sign in with Google first.");
        }
        try {
            AppUser user = userAccountService.upsertFromOAuth(oauthUser);
            String url = stripeBillingService.createPortalSession(user);
            return ResponseEntity.ok(Map.of("url", url));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (StripeException e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(Map.of("error", "Stripe error: " + e.getMessage()));
        }
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> webhook(
            @RequestBody String payload,
            @RequestHeader(value = "Stripe-Signature", required = false) String sigHeader
    ) {
        try {
            stripeBillingService.handleWebhook(payload, sigHeader != null ? sigHeader : "");
            return ResponseEntity.ok("ok");
        } catch (SignatureVerificationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(e.getMessage());
        }
    }
}

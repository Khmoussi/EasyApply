package com.example.EasyApply.sources;

import com.example.EasyApply.Services.BillingService;
import com.example.EasyApply.Services.UserAccountService;
import com.example.EasyApply.domain.AppUser;
import com.example.EasyApply.sources.data.CompanySearchResponse;
import com.example.EasyApply.sources.data.LinkedInRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/scrap-sources")
public class ScrapSourcesController {

    private final SearchLinkedService searchLinkedService;
    private final UserAccountService userAccountService;
    private final BillingService billingService;

    public ScrapSourcesController(
            SearchLinkedService searchLinkedService,
            UserAccountService userAccountService,
            BillingService billingService
    ) {
        this.searchLinkedService = searchLinkedService;
        this.userAccountService = userAccountService;
        this.billingService = billingService;
    }

    @PostMapping("/searchLinked")
    public ResponseEntity<?> search(
            @AuthenticationPrincipal OAuth2User oauthUser,
            @RequestBody LinkedInRequest request
    ) {
        if (oauthUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Sign in with Google first.");
        }
        AppUser user = userAccountService.upsertFromOAuth(oauthUser);
        int effectiveLimit = billingService.assertCanSearch(user, request.getLimit());
        request.setLimit(effectiveLimit);

        CompanySearchResponse result = searchLinkedService.searchCompanies(request);
        int counted = result.getCompanies() != null ? result.getCompanies().size() : 0;
        billingService.recordCompanies(user, counted);
        return ResponseEntity.ok(result);
    }
}

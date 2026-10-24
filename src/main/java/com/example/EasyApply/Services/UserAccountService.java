package com.example.EasyApply.Services;

import com.example.EasyApply.domain.AppUser;
import com.example.EasyApply.domain.AppUserRepository;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserAccountService {

    private final AppUserRepository appUserRepository;
    private final BillingService billingService;

    public UserAccountService(AppUserRepository appUserRepository, BillingService billingService) {
        this.appUserRepository = appUserRepository;
        this.billingService = billingService;
    }

    @Transactional
    public AppUser upsertFromOAuth(OAuth2User oauthUser) {
        if (oauthUser == null) {
            throw new IllegalArgumentException("Not authenticated");
        }
        String sub = oauthUser.getName();
        String email = oauthUser.getAttribute("email");
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Google account has no email");
        }
        String name = oauthUser.getAttribute("name");
        String picture = oauthUser.getAttribute("picture");

        AppUser user = appUserRepository.findByGoogleSub(sub)
                .or(() -> appUserRepository.findByEmailIgnoreCase(email))
                .orElseGet(AppUser::new);

        user.setGoogleSub(sub);
        user.setEmail(email.trim());
        user.setName(name);
        user.setPicture(picture);
        AppUser saved = appUserRepository.save(user);
        billingService.ensureTrialSubscription(saved);
        return saved;
    }
}

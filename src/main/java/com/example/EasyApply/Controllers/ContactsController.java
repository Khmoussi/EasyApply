package com.example.EasyApply.Controllers;

import com.example.EasyApply.Data.ContactDto;
import com.example.EasyApply.Services.ContactLibraryService;
import com.example.EasyApply.Services.UserAccountService;
import com.example.EasyApply.domain.AppUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/easyapply/contacts")
public class ContactsController {

    private final UserAccountService userAccountService;
    private final ContactLibraryService contactLibraryService;

    public ContactsController(UserAccountService userAccountService, ContactLibraryService contactLibraryService) {
        this.userAccountService = userAccountService;
        this.contactLibraryService = contactLibraryService;
    }

    /** Personal library — contacts this user scraped (for resend). */
    @GetMapping
    public ResponseEntity<List<ContactDto>> myContacts(
            @AuthenticationPrincipal OAuth2User oauthUser,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String location
    ) {
        AppUser user = userAccountService.upsertFromOAuth(oauthUser);
        boolean filtering = (keyword != null && !keyword.isBlank()) || (location != null && !location.isBlank());
        List<ContactDto> rows = filtering
                ? contactLibraryService.searchMine(user, keyword, location)
                : contactLibraryService.listMine(user);
        return ResponseEntity.ok(rows);
    }

    /**
     * Shared directory built from all scrapes — search by keyword × location
     * without calling Harvest/Apify again.
     */
    @GetMapping("/directory")
    public ResponseEntity<List<ContactDto>> directory(
            @AuthenticationPrincipal OAuth2User oauthUser,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String location
    ) {
        // Ensure caller is a known user (auth required by SecurityConfig).
        userAccountService.upsertFromOAuth(oauthUser);
        return ResponseEntity.ok(contactLibraryService.searchDirectory(keyword, location));
    }
}

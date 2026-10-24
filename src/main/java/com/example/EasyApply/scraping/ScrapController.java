package com.example.EasyApply.scraping;

import com.example.EasyApply.Data.CompanyDetailsRequest;
import com.example.EasyApply.Data.CompanyDetailsResponse;
import com.example.EasyApply.Data.ScrapeEmailsRequest;
import com.example.EasyApply.Services.ContactLibraryService;
import com.example.EasyApply.Services.UserAccountService;
import com.example.EasyApply.domain.AppUser;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/scrap")
public class ScrapController {

    private final ScrapService scrapService;
    private final UserAccountService userAccountService;
    private final ContactLibraryService contactLibraryService;

    public ScrapController(
            ScrapService scrapService,
            UserAccountService userAccountService,
            ContactLibraryService contactLibraryService
    ) {
        this.scrapService = scrapService;
        this.userAccountService = userAccountService;
        this.contactLibraryService = contactLibraryService;
    }

    @GetMapping("/getEmail")
    List<String> findEmail() {
        return scrapService.scrapeEmails("https://www.anaxi-consulting.com");
    }

    @PostMapping(value = "/getEmails", consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<?> findEmailsPost(
            @AuthenticationPrincipal OAuth2User oauthUser,
            @RequestBody ScrapeEmailsRequest request
    ) {
        if (oauthUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Sign in with Google first.");
        }
        if (request == null || request.getCompanies() == null || request.getCompanies().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Request body must include a non-empty companies array."
            ));
        }

        ArrayList<CompanyDetailsRequest> companies = new ArrayList<>(request.getCompanies());
        CompanyDetailsResponse result = scrapService.scrapeEmailsFromCompanies(companies);

        AppUser user = userAccountService.upsertFromOAuth(oauthUser);
        int saved = contactLibraryService.saveScrapeResults(
                user,
                result.getSuccessMap(),
                request.getKeywords(),
                request.getLocations()
        );

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("successMap", result.getSuccessMap());
        body.put("failedMap", result.getFailedMap());
        body.put("savedToLibrary", saved);
        return ResponseEntity.ok(body);
    }

    @GetMapping("/getEmails")
    ResponseEntity<?> findEmailsGet() {
        Map<String, Object> err = new LinkedHashMap<>();
        err.put("error", "Use POST /scrap/getEmails with Content-Type: application/json.");
        err.put("path", "/scrap/getEmails");
        err.put("allowedMethods", List.of("POST"));
        err.put(
                "example",
                "{\"keywords\":[\"saas\"],\"locations\":[\"Tunisia\"],\"companies\":[{\"website\":\"https://example.com\",\"name\":\"Example\"}]}"
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON).body(err);
    }

    @GetMapping("/hello22")
    String hello() {
        return "hello";
    }
}

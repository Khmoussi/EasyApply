package com.example.EasyApply.Controllers;

import com.example.EasyApply.Services.UserAccountService;
import com.example.EasyApply.domain.AppUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/easyapply/auth")
public class AuthController {

    private final UserAccountService userAccountService;

    public AuthController(UserAccountService userAccountService) {
        this.userAccountService = userAccountService;
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(@AuthenticationPrincipal OAuth2User user) {
        if (user == null) {
            return ResponseEntity.ok(Map.of("authenticated", false));
        }
        AppUser persisted = userAccountService.upsertFromOAuth(user);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("authenticated", true);
        body.put("id", persisted.getId());
        body.put("name", persisted.getName());
        body.put("email", persisted.getEmail());
        body.put("picture", persisted.getPicture());
        return ResponseEntity.ok(body);
    }
}

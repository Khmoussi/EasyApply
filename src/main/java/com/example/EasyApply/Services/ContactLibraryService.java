package com.example.EasyApply.Services;

import com.example.EasyApply.Data.CompanyDetailsRequest;
import com.example.EasyApply.Data.ContactDto;
import com.example.EasyApply.domain.AppUser;
import com.example.EasyApply.domain.Company;
import com.example.EasyApply.domain.CompanyEmail;
import com.example.EasyApply.domain.CompanyEmailRepository;
import com.example.EasyApply.domain.CompanyRepository;
import com.example.EasyApply.domain.UserContact;
import com.example.EasyApply.domain.UserContactRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ContactLibraryService {

    private final CompanyRepository companyRepository;
    private final CompanyEmailRepository companyEmailRepository;
    private final UserContactRepository userContactRepository;

    public ContactLibraryService(
            CompanyRepository companyRepository,
            CompanyEmailRepository companyEmailRepository,
            UserContactRepository userContactRepository
    ) {
        this.companyRepository = companyRepository;
        this.companyEmailRepository = companyEmailRepository;
        this.userContactRepository = userContactRepository;
    }

    /**
     * Upserts global company + email rows and links them into the user's personal library.
     */
    @Transactional
    public int saveScrapeResults(
            AppUser user,
            Map<String, CompanyDetailsRequest> successMap,
            List<String> keywords,
            List<String> locations
    ) {
        if (successMap == null || successMap.isEmpty()) {
            return 0;
        }
        String keywordsText = join(keywords);
        String locationText = join(locations);
        int saved = 0;

        for (Map.Entry<String, CompanyDetailsRequest> entry : successMap.entrySet()) {
            String email = normalizeEmail(entry.getKey());
            CompanyDetailsRequest details = entry.getValue();
            if (email == null || details == null || details.getWebsite() == null || details.getWebsite().isBlank()) {
                continue;
            }

            Company company = upsertCompany(details, keywordsText, locationText);
            CompanyEmail companyEmail = companyEmailRepository
                    .findByCompanyIdAndEmailIgnoreCase(company.getId(), email)
                    .orElseGet(() -> {
                        CompanyEmail created = new CompanyEmail();
                        created.setCompany(company);
                        created.setEmail(email);
                        return companyEmailRepository.save(created);
                    });

            boolean exists = userContactRepository
                    .findByUserIdAndCompanyEmailId(user.getId(), companyEmail.getId())
                    .isPresent();
            if (!exists) {
                UserContact contact = new UserContact();
                contact.setUser(user);
                contact.setCompanyEmail(companyEmail);
                userContactRepository.save(contact);
                saved++;
            }
        }
        return saved;
    }

    @Transactional(readOnly = true)
    public List<ContactDto> listMine(AppUser user) {
        return userContactRepository.findAllByUserId(user.getId()).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ContactDto> searchMine(AppUser user, String keyword, String location) {
        return userContactRepository
                .searchByUser(user.getId(), blankToEmpty(keyword), blankToEmpty(location))
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ContactDto> searchDirectory(String keyword, String location) {
        return companyRepository
                .searchDirectory(blankToEmpty(keyword), blankToEmpty(location))
                .stream()
                .limit(200)
                .map(this::toDirectoryDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void markSent(AppUser user, List<String> emails) {
        if (emails == null || emails.isEmpty()) {
            return;
        }
        Map<String, UserContact> byEmail = new LinkedHashMap<>();
        for (UserContact contact : userContactRepository.findAllByUserId(user.getId())) {
            byEmail.put(contact.getCompanyEmail().getEmail().toLowerCase(Locale.ROOT), contact);
        }
        Instant now = Instant.now();
        for (String raw : emails) {
            String email = normalizeEmail(raw);
            if (email == null) {
                continue;
            }
            UserContact contact = byEmail.get(email);
            if (contact != null) {
                contact.setLastSentAt(now);
            }
        }
    }

    private Company upsertCompany(CompanyDetailsRequest details, String keywordsText, String locationText) {
        String websiteKey = normalizeWebsiteKey(details.getWebsite());
        Company company = companyRepository.findByWebsiteKey(websiteKey).orElseGet(Company::new);
        company.setWebsiteKey(websiteKey);
        company.setWebsite(details.getWebsite().trim());
        if (details.getName() != null && !details.getName().isBlank()) {
            company.setName(details.getName().trim());
        } else if (company.getName() == null) {
            company.setName(websiteKey);
        }
        if (details.getCountry() != null) {
            company.setCountry(blankToNull(details.getCountry()));
        }
        if (details.getState() != null) {
            company.setState(blankToNull(details.getState()));
        }
        if (details.getDomain() != null) {
            company.setDomain(blankToNull(details.getDomain()));
        }
        if (details.getLinkedinUrl() != null) {
            company.setLinkedinUrl(blankToNull(details.getLinkedinUrl()));
        }
        if (details.getId() != null) {
            company.setProviderId(blankToNull(details.getId()));
        }
        company.setKeywords(mergeText(company.getKeywords(), keywordsText));
        company.setLocationText(mergeText(company.getLocationText(), locationText));
        return companyRepository.save(company);
    }

    private ContactDto toDto(UserContact contact) {
        CompanyEmail ce = contact.getCompanyEmail();
        Company c = ce.getCompany();
        ContactDto dto = baseDto(c, ce.getEmail());
        dto.setContactId(contact.getId());
        dto.setSavedAt(contact.getSavedAt());
        dto.setLastSentAt(contact.getLastSentAt());
        return dto;
    }

    private ContactDto toDirectoryDto(CompanyEmail ce) {
        Company c = ce.getCompany();
        ContactDto dto = baseDto(c, ce.getEmail());
        dto.setSavedAt(ce.getFirstSeenAt());
        return dto;
    }

    private ContactDto baseDto(Company c, String email) {
        ContactDto dto = new ContactDto();
        dto.setCompanyId(c.getId());
        dto.setEmail(email);
        dto.setCompanyName(c.getName());
        dto.setWebsite(c.getWebsite());
        dto.setCountry(c.getCountry());
        dto.setState(c.getState());
        dto.setDomain(c.getDomain());
        dto.setLinkedinUrl(c.getLinkedinUrl());
        dto.setKeywords(c.getKeywords());
        dto.setLocationText(c.getLocationText());
        return dto;
    }

    static String normalizeWebsiteKey(String website) {
        String raw = website.trim();
        try {
            String withScheme = raw.matches("(?i)^https?://.*") ? raw : "https://" + raw;
            URI uri = URI.create(withScheme);
            String host = uri.getHost() != null ? uri.getHost() : raw;
            String path = uri.getPath() != null ? uri.getPath() : "";
            String key = (host + path).toLowerCase(Locale.ROOT);
            while (key.endsWith("/")) {
                key = key.substring(0, key.length() - 1);
            }
            if (key.startsWith("www.")) {
                key = key.substring(4);
            }
            return key;
        } catch (Exception e) {
            String key = raw.toLowerCase(Locale.ROOT);
            while (key.endsWith("/")) {
                key = key.substring(0, key.length() - 1);
            }
            return key;
        }
    }

    private static String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private static String join(List<String> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        List<String> cleaned = new ArrayList<>();
        for (String v : values) {
            if (v != null && !v.isBlank()) {
                cleaned.add(v.trim());
            }
        }
        return cleaned.isEmpty() ? null : String.join(" ", cleaned);
    }

    private static String mergeText(String existing, String incoming) {
        if (incoming == null || incoming.isBlank()) {
            return existing;
        }
        if (existing == null || existing.isBlank()) {
            return incoming;
        }
        String lowerExisting = existing.toLowerCase(Locale.ROOT);
        StringBuilder out = new StringBuilder(existing);
        for (String token : incoming.split("\\s+")) {
            if (token.isBlank()) {
                continue;
            }
            if (!lowerExisting.contains(token.toLowerCase(Locale.ROOT))) {
                out.append(' ').append(token);
                lowerExisting = out.toString().toLowerCase(Locale.ROOT);
            }
        }
        return out.toString();
    }

    private static String blankToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}

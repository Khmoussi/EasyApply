package com.example.EasyApply.sources;

import com.example.EasyApply.Data.CompanyDetailsRequest;
import com.example.EasyApply.sources.data.CompanySearchResponse;
import com.example.EasyApply.sources.data.LinkedInCompanyDTO;
import com.example.EasyApply.sources.data.LinkedInRequest;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
@ConditionalOnProperty(name = "easyapply.linkedin.provider", havingValue = "apify", matchIfMissing = true)
public class ApifySearchLinkedService implements SearchLinkedService {

    private static final Logger log = LoggerFactory.getLogger(ApifySearchLinkedService.class);

    private final String baseUrl;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ApifySearchLinkedService(@Value("${easyapply.apify.api-key:}") String apiKey) {
        this.baseUrl = "https://api.apify.com/v2/acts/bebity~linkedin-premium-actor/run-sync-get-dataset-items?token=" + apiKey;
    }

    @Override
    public CompanySearchResponse searchCompanies(LinkedInRequest request) {
        int page = request.getPage() > 0 ? request.getPage() : 1;
        // Apify sync actor has no page cursor — only the first page is supported.
        if (page > 1) {
            log.info("Apify LinkedIn search has no pagination; ignoring page={}", page);
            return new CompanySearchResponse(List.of(), page, 1, false);
        }

        Map<String, Object> body = new HashMap<>();
        body.put("action", "get-companies");
        body.put("limit", request.getLimit());
        body.put("keywords", request.getKeywords());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            log.info("Calling Apify get-companies; body={}", body);

            ResponseEntity<String> rawResponse = restTemplate.exchange(
                    baseUrl,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            String rawJson = rawResponse.getBody();
            log.info("Raw Apify JSON response:\n{}", rawJson);

            if (rawJson == null || rawJson.isBlank()) {
                return CompanySearchResponse.of(List.of(), 1, 1);
            }

            List<LinkedInCompanyDTO> companies = objectMapper.readValue(
                    rawJson,
                    new TypeReference<>() {}
            );

            Set<String> excludeIds = toLowerSet(request.getExcludeIds());
            Set<String> excludeUrls = toLowerSet(request.getExcludeLinkedInUrls());
            List<CompanyDetailsRequest> mapped = mapToCompanyDetails(companies, excludeIds, excludeUrls);
            // Single-shot provider: never advertise more pages.
            return new CompanySearchResponse(mapped, 1, 1, false);

        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == HttpStatus.REQUEST_TIMEOUT.value()) {
                throw new ResponseStatusException(
                        HttpStatus.GATEWAY_TIMEOUT,
                        "LinkedIn actor exceeded Apify's 300s sync timeout. Lower `limit`, narrow `keywords`, or switch to an async Apify run.",
                        e);
            }
            log.error("Apify LinkedIn API client error {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
            return new CompanySearchResponse(List.of(), 1, 1, false);
        } catch (Exception e) {
            log.error("Error calling Apify LinkedIn API: {}", e.getMessage(), e);
            return new CompanySearchResponse(List.of(), 1, 1, false);
        }
    }

    private List<CompanyDetailsRequest> mapToCompanyDetails(
            List<LinkedInCompanyDTO> companies,
            Set<String> excludeIds,
            Set<String> excludeUrls
    ) {
        List<CompanyDetailsRequest> result = new ArrayList<>();
        for (LinkedInCompanyDTO company : companies) {
            if (company.getStatus() != null && !"FOUND".equalsIgnoreCase(company.getStatus())) {
                log.warn(
                        "Skipping Apify row status={} reason={} input={}",
                        company.getStatus(),
                        company.getReason(),
                        company.getInput()
                );
                continue;
            }
            if (company.getName() == null || company.getName().isBlank()) {
                log.warn("Skipping Apify row with empty name; status={} input={}", company.getStatus(), company.getInput());
                continue;
            }

            String linkedinUrl = blankToNull(company.getLinkedinUrl());
            String id = blankToNull(company.getCompanyId());
            if (id != null && excludeIds.contains(id.toLowerCase(Locale.ROOT))) {
                continue;
            }
            if (linkedinUrl != null && excludeUrls.contains(linkedinUrl.toLowerCase(Locale.ROOT))) {
                continue;
            }

            String website = company.getWebsiteUrl();
            String name = company.getName();
            String country = company.getHeadquarter() != null
                    ? company.getHeadquarter().getCountry()
                    : null;
            String state = company.getHeadquarter() != null
                    ? company.getHeadquarter().getCity()
                    : null;
            String domain = company.getIndustry();
            result.add(new CompanyDetailsRequest(website, name, country, state, domain, id, linkedinUrl));
        }
        return result;
    }

    private static Set<String> toLowerSet(List<String> values) {
        Set<String> set = new HashSet<>();
        if (values == null) {
            return set;
        }
        for (String v : values) {
            if (v != null && !v.isBlank()) {
                set.add(v.trim().toLowerCase(Locale.ROOT));
            }
        }
        return set;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}

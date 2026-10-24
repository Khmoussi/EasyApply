package com.example.EasyApply.sources;

import com.example.EasyApply.Data.CompanyDetailsRequest;
import com.example.EasyApply.sources.data.CompanySearchResponse;
import com.example.EasyApply.sources.data.HarvestCompanyDetailsResponse;
import com.example.EasyApply.sources.data.HarvestCompanyDetailsResponse.Company;
import com.example.EasyApply.sources.data.HarvestCompanyDetailsResponse.CompanyLocation;
import com.example.EasyApply.sources.data.HarvestCompanyDetailsResponse.Industry;
import com.example.EasyApply.sources.data.HarvestCompanySearchResponse;
import com.example.EasyApply.sources.data.HarvestCompanySearchResponse.HarvestCompanyShort;
import com.example.EasyApply.sources.data.HarvestCompanySearchResponse.Pagination;
import com.example.EasyApply.sources.data.LinkedInRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Harvest API LinkedIn company search backup.
 * 1) {@code GET /linkedin/company-search} for candidates (paginated via {@code page})
 * 2) {@code GET /linkedin/company?url=} for website (needed for email scraping)
 *
 * @see <a href="https://skmtc.dev/harvestapi/apis/harvestapi-linkedin-api-reference/docs/linkedin/company-search/get">company-search</a>
 * @see <a href="https://skmtc.dev/harvestapi/apis/harvestapi-linkedin-api-reference/docs/linkedin/company/get">company</a>
 */
@Service
@ConditionalOnProperty(name = "easyapply.linkedin.provider", havingValue = "harvest")
public class HarvestSearchLinkedService implements SearchLinkedService {

    private static final Logger log = LoggerFactory.getLogger(HarvestSearchLinkedService.class);
    private static final String SEARCH_URL = "https://api.harvest-api.com/linkedin/company-search";
    private static final String COMPANY_URL = "https://api.harvest-api.com/linkedin/company";

    private final String apiKey;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public HarvestSearchLinkedService(@Value("${easyapply.harvest.api-key:}") String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public CompanySearchResponse searchCompanies(LinkedInRequest request) {
        String search = joinNonBlank(request.getKeywords(), " ");
        int page = request.getPage() > 0 ? request.getPage() : 1;
        if (search.isBlank()) {
            log.warn("Harvest company search skipped: no keywords");
            return CompanySearchResponse.of(List.of(), page, 0);
        }

        UriComponentsBuilder uriBuilder = UriComponentsBuilder
                .fromHttpUrl(SEARCH_URL)
                .queryParam("search", search)
                .queryParam("page", page);

        String location = joinNonBlank(request.getLocation(), ", ");
        if (!location.isBlank()) {
            uriBuilder.queryParam("location", location);
        }

        URI uri = uriBuilder.build().encode().toUri();
        HttpEntity<Void> entity = authorizedEntity();

        try {
            log.info("Calling Harvest company-search; uri={}", uri);

            ResponseEntity<String> rawResponse = restTemplate.exchange(
                    uri,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            String rawJson = rawResponse.getBody();
            log.info("Raw Harvest company-search JSON:\n{}", rawJson);

            if (rawJson == null || rawJson.isBlank()) {
                return CompanySearchResponse.of(List.of(), page, 0);
            }

            HarvestCompanySearchResponse response = objectMapper.readValue(rawJson, HarvestCompanySearchResponse.class);

            if (response.getError() != null && !response.getError().isBlank()) {
                log.error("Harvest company search error: {}", response.getError());
            }

            List<HarvestCompanyShort> elements = response.getElements();
            if (elements == null || elements.isEmpty()) {
                int totalPages = paginationTotalPages(response.getPagination(), page, false);
                return CompanySearchResponse.of(List.of(), page, totalPages);
            }

            Set<String> excludeIds = toLowerSet(request.getExcludeIds());
            Set<String> excludeUrls = toLowerSet(request.getExcludeLinkedInUrls());
            List<HarvestCompanyShort> fresh = elements.stream()
                    .filter(c -> !isExcluded(c, excludeIds, excludeUrls))
                    .collect(Collectors.toList());

            int skipped = elements.size() - fresh.size();
            if (skipped > 0) {
                log.info("Skipped {} already-seen companies before detail enrichment", skipped);
            }

            int limit = request.getLimit() > 0 ? request.getLimit() : fresh.size();
            List<HarvestCompanyShort> limited = fresh.stream().limit(limit).collect(Collectors.toList());
            List<CompanyDetailsRequest> companies = enrichWithCompanyDetails(limited);

            int totalPages = paginationTotalPages(response.getPagination(), page, !elements.isEmpty());
            return CompanySearchResponse.of(companies, page, totalPages);

        } catch (Exception e) {
            log.error("Error calling Harvest LinkedIn API: {}", e.getMessage(), e);
            return CompanySearchResponse.of(List.of(), page, 0);
        }
    }

    private List<CompanyDetailsRequest> enrichWithCompanyDetails(List<HarvestCompanyShort> companies) {
        List<CompanyDetailsRequest> result = new ArrayList<>();
        for (HarvestCompanyShort shortCompany : companies) {
            if (shortCompany.getName() == null || shortCompany.getName().isBlank()) {
                continue;
            }

            Company details = fetchCompanyDetails(shortCompany);
            String website = details != null ? blankToNull(details.getWebsite()) : null;
            String name = details != null && details.getName() != null && !details.getName().isBlank()
                    ? details.getName()
                    : shortCompany.getName();

            String country = null;
            String state = null;
            if (details != null && details.getLocations() != null) {
                CompanyLocation hq = details.getLocations().stream()
                        .filter(CompanyLocation::isHeadquarter)
                        .findFirst()
                        .orElse(details.getLocations().isEmpty() ? null : details.getLocations().get(0));
                if (hq != null) {
                    country = hq.getCountry();
                    state = hq.getCity();
                }
            }
            if (country == null && shortCompany.getLocation() != null) {
                country = shortCompany.getLocation().getLinkedinText();
            }

            String domain = shortCompany.getIndustries();
            if (details != null && details.getIndustries() != null && !details.getIndustries().isEmpty()) {
                Industry first = details.getIndustries().get(0);
                domain = first.getTitle() != null ? first.getTitle() : first.getName();
            }

            if (website == null) {
                log.warn(
                        "No website for company={} linkedinUrl={} universalName={}",
                        name,
                        shortCompany.getLinkedinUrl(),
                        shortCompany.getUniversalName()
                );
            }

            result.add(new CompanyDetailsRequest(
                    website,
                    name,
                    country,
                    state,
                    domain,
                    blankToNull(shortCompany.getId()),
                    blankToNull(shortCompany.getLinkedinUrl())
            ));
        }
        return result;
    }

    private Company fetchCompanyDetails(HarvestCompanyShort shortCompany) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(COMPANY_URL);
        if (shortCompany.getLinkedinUrl() != null && !shortCompany.getLinkedinUrl().isBlank()) {
            builder.queryParam("url", shortCompany.getLinkedinUrl());
        } else if (shortCompany.getUniversalName() != null && !shortCompany.getUniversalName().isBlank()) {
            builder.queryParam("universalName", shortCompany.getUniversalName());
        } else {
            log.warn("Cannot fetch company details: missing linkedinUrl and universalName for {}", shortCompany.getName());
            return null;
        }

        URI uri = builder.build().encode().toUri();
        try {
            log.info("Calling Harvest company details; uri={}", uri);
            ResponseEntity<String> rawResponse = restTemplate.exchange(
                    uri,
                    HttpMethod.GET,
                    authorizedEntity(),
                    String.class
            );
            String rawJson = rawResponse.getBody();
            log.info("Raw Harvest company JSON for {}:\n{}", shortCompany.getName(), rawJson);

            if (rawJson == null || rawJson.isBlank()) {
                return null;
            }

            HarvestCompanyDetailsResponse response = objectMapper.readValue(rawJson, HarvestCompanyDetailsResponse.class);
            if (response.getError() != null && !response.getError().isBlank()) {
                log.error("Harvest company details error for {}: {}", shortCompany.getName(), response.getError());
                return null;
            }
            return response.getElement();
        } catch (Exception e) {
            log.error(
                    "Failed to fetch Harvest company details for {} ({}): {}",
                    shortCompany.getName(),
                    shortCompany.getLinkedinUrl(),
                    e.getMessage()
            );
            return null;
        }
    }

    private HttpEntity<Void> authorizedEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-API-Key", apiKey);
        return new HttpEntity<>(headers);
    }

    private static boolean isExcluded(HarvestCompanyShort company, Set<String> excludeIds, Set<String> excludeUrls) {
        if (company.getId() != null && excludeIds.contains(company.getId().toLowerCase(Locale.ROOT))) {
            return true;
        }
        if (company.getLinkedinUrl() != null && excludeUrls.contains(company.getLinkedinUrl().toLowerCase(Locale.ROOT))) {
            return true;
        }
        if (company.getUniversalName() != null && excludeIds.contains(company.getUniversalName().toLowerCase(Locale.ROOT))) {
            return true;
        }
        return false;
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

    private static int paginationTotalPages(Pagination pagination, int requestedPage, boolean hadElements) {
        if (pagination != null && pagination.getTotalPages() > 0) {
            return pagination.getTotalPages();
        }
        // Fallback when Harvest omits pagination: assume another page only if this page had results.
        return hadElements ? requestedPage + 1 : Math.max(requestedPage - 1, 0);
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static String joinNonBlank(List<String> values, String delimiter) {
        if (values == null || values.isEmpty()) {
            return "";
        }
        return values.stream()
                .filter(v -> v != null && !v.isBlank())
                .map(String::trim)
                .collect(Collectors.joining(delimiter));
    }
}

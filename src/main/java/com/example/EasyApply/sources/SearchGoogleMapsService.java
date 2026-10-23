package com.example.EasyApply.sources;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class SearchGoogleMapsService {

    private final String apiKey;
    private final String baseUrl = "https://serpapi.com/search.json";
    private final RestTemplate restTemplate = new RestTemplate();

    public SearchGoogleMapsService(@Value("${easyapply.serpapi.api-key:}") String apiKey) {
        this.apiKey = apiKey;
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> searchWebsites() {
        List<Map<String, Object>> allResults = new ArrayList<>();

        String nextUrl = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .queryParam("engine", "google_maps")
                .queryParam("q", "it companies")
                .queryParam("type", "search")
                .queryParam("api_key", apiKey)
                .toUriString();

        int pageLimit = 10;
        int page = 0;

        while (nextUrl != null && page < pageLimit) {
            URI uri = page == 0
                    ? URI.create(nextUrl)
                    : addApiKeySafely(nextUrl);

            System.out.println("Calling: " + uri);

            Map<String, Object> response = restTemplate.getForObject(uri, Map.class);
            if (response == null) {
                break;
            }

            List<Map<String, Object>> localResults =
                    (List<Map<String, Object>>) response.get("local_results");
            if (localResults != null) {
                allResults.addAll(localResults);
            }

            Map<String, Object> pagination =
                    (Map<String, Object>) response.get("serpapi_pagination");
            if (pagination == null || !pagination.containsKey("next")) {
                break;
            }

            nextUrl = (String) pagination.get("next");
            page++;
        }

        return allResults;
    }

    private URI addApiKeySafely(String nextUrl) {
        return UriComponentsBuilder
                .fromUriString(nextUrl)
                .queryParam("api_key", apiKey)
                .build(true)
                .toUri();
    }
}

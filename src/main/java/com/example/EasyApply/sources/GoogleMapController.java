package com.example.EasyApply.sources;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/scrap-sources")
public class GoogleMapController {

    private final SearchGoogleMapsService searchGoogleMapsService;

    public GoogleMapController(SearchGoogleMapsService searchGoogleMapsService) {
        this.searchGoogleMapsService = searchGoogleMapsService;
    }

    @GetMapping("/search")
    public List<Map<String, Object>> search() {
        return searchGoogleMapsService.searchWebsites();
    }
}

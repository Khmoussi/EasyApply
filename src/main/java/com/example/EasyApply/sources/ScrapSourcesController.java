package com.example.EasyApply.sources;

import com.example.EasyApply.sources.data.LinkedInRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/scrap-sources")
public class ScrapSourcesController {

    private final SearchLinkedService searchLinkedService;

    public ScrapSourcesController(SearchLinkedService searchLinkedService) {
        this.searchLinkedService = searchLinkedService;
    }

    @PostMapping("/searchLinked")
    public ResponseEntity<?> search(@RequestBody LinkedInRequest request) {
        return ResponseEntity.ok(searchLinkedService.searchCompanies(request));
    }
}

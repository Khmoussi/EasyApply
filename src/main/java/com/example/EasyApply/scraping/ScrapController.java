package com.example.EasyApply.scraping;

import com.example.EasyApply.Data.CompanyDetailsRequest;
import com.example.EasyApply.Data.CompanyDetailsResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

    public ScrapController(ScrapService scrapService) {
        this.scrapService = scrapService;
    }

    @GetMapping("/getEmail")
    List<String> findEmail() {
        return scrapService.scrapeEmails("https://www.anaxi-consulting.com");
    }

    @PostMapping(value = "/getEmails", consumes = MediaType.APPLICATION_JSON_VALUE)
    CompanyDetailsResponse findEmailsPost(@RequestBody ArrayList<CompanyDetailsRequest> request) {
        return scrapService.scrapeEmailsFromCompanies(request);
    }

    @GetMapping("/getEmails")
    ResponseEntity<?> findEmailsGet(@RequestBody(required = false) ArrayList<CompanyDetailsRequest> request) {
        if (request == null || request.isEmpty()) {
            Map<String, Object> err = new LinkedHashMap<>();
            err.put("error", "Use POST /scrap/getEmails with Content-Type: application/json and a JSON array of companies.");
            err.put("path", "/scrap/getEmails");
            err.put("allowedMethods", List.of("POST"));
            err.put(
                    "example",
                    "curl -X POST http://localhost:8080/scrap/getEmails -H \"Content-Type: application/json\" -d \"[...]\""
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON).body(err);
        }
        return ResponseEntity.ok(scrapService.scrapeEmailsFromCompanies(request));
    }

    @GetMapping("/hello22")
    String hello() {
        return "hello";
    }
}

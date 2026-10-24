package com.example.EasyApply.sources;

import com.example.EasyApply.sources.data.CompanySearchResponse;
import com.example.EasyApply.sources.data.LinkedInRequest;

/**
 * LinkedIn company search. Implementation is chosen by
 * {@code easyapply.linkedin.provider} ({@code apify} or {@code harvest}).
 */
public interface SearchLinkedService {

    CompanySearchResponse searchCompanies(LinkedInRequest request);
}

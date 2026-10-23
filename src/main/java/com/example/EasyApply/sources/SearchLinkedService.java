package com.example.EasyApply.sources;

import com.example.EasyApply.Data.CompanyDetailsRequest;
import com.example.EasyApply.sources.data.LinkedInRequest;

import java.util.List;

/**
 * LinkedIn company search. Implementation is chosen by
 * {@code easyapply.linkedin.provider} ({@code apify} or {@code harvest}).
 */
public interface SearchLinkedService {

    List<CompanyDetailsRequest> searchCompanies(LinkedInRequest request);
}

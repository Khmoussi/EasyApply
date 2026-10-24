package com.example.EasyApply.sources.data;

import com.example.EasyApply.Data.CompanyDetailsRequest;

import java.util.List;

/**
 * Paginated LinkedIn company search result.
 */
public class CompanySearchResponse {
    private List<CompanyDetailsRequest> companies;
    private int page;
    private int totalPages;
    private boolean hasMore;

    public CompanySearchResponse() {
    }

    public CompanySearchResponse(List<CompanyDetailsRequest> companies, int page, int totalPages, boolean hasMore) {
        this.companies = companies;
        this.page = page;
        this.totalPages = totalPages;
        this.hasMore = hasMore;
    }

    public static CompanySearchResponse of(List<CompanyDetailsRequest> companies, int page, int totalPages) {
        boolean hasMore = totalPages > 0 ? page < totalPages : !companies.isEmpty();
        return new CompanySearchResponse(companies, page, totalPages, hasMore);
    }

    public List<CompanyDetailsRequest> getCompanies() {
        return companies;
    }

    public void setCompanies(List<CompanyDetailsRequest> companies) {
        this.companies = companies;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public boolean isHasMore() {
        return hasMore;
    }

    public void setHasMore(boolean hasMore) {
        this.hasMore = hasMore;
    }
}

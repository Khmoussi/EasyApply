package com.example.EasyApply.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, Long> {
    Optional<Company> findByWebsiteKey(String websiteKey);

    @Query("""
            select distinct ce from CompanyEmail ce
            join fetch ce.company c
            where (:keyword = '' or lower(c.name) like lower(concat('%', :keyword, '%'))
                or lower(coalesce(c.domain, '')) like lower(concat('%', :keyword, '%'))
                or lower(coalesce(c.keywords, '')) like lower(concat('%', :keyword, '%'))
                or lower(ce.email) like lower(concat('%', :keyword, '%')))
              and (:location = '' or lower(coalesce(c.country, '')) like lower(concat('%', :location, '%'))
                or lower(coalesce(c.state, '')) like lower(concat('%', :location, '%'))
                or lower(coalesce(c.locationText, '')) like lower(concat('%', :location, '%')))
            order by c.name asc
            """)
    List<CompanyEmail> searchDirectory(
            @Param("keyword") String keyword,
            @Param("location") String location
    );
}

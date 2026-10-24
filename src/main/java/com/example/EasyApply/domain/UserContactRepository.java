package com.example.EasyApply.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserContactRepository extends JpaRepository<UserContact, Long> {
    Optional<UserContact> findByUserIdAndCompanyEmailId(Long userId, Long companyEmailId);

    @Query("""
            select uc from UserContact uc
            join fetch uc.companyEmail ce
            join fetch ce.company c
            where uc.user.id = :userId
            order by uc.savedAt desc
            """)
    List<UserContact> findAllByUserId(@Param("userId") Long userId);

    @Query("""
            select uc from UserContact uc
            join fetch uc.companyEmail ce
            join fetch ce.company c
            where uc.user.id = :userId
              and (:keyword = '' or lower(c.name) like lower(concat('%', :keyword, '%'))
                or lower(coalesce(c.domain, '')) like lower(concat('%', :keyword, '%'))
                or lower(coalesce(c.keywords, '')) like lower(concat('%', :keyword, '%'))
                or lower(ce.email) like lower(concat('%', :keyword, '%')))
              and (:location = '' or lower(coalesce(c.country, '')) like lower(concat('%', :location, '%'))
                or lower(coalesce(c.state, '')) like lower(concat('%', :location, '%'))
                or lower(coalesce(c.locationText, '')) like lower(concat('%', :location, '%')))
            order by uc.savedAt desc
            """)
    List<UserContact> searchByUser(
            @Param("userId") Long userId,
            @Param("keyword") String keyword,
            @Param("location") String location
    );
}

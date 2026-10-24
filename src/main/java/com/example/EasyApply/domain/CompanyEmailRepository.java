package com.example.EasyApply.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompanyEmailRepository extends JpaRepository<CompanyEmail, Long> {
    Optional<CompanyEmail> findByCompanyIdAndEmailIgnoreCase(Long companyId, String email);
}

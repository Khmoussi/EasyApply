package com.example.EasyApply.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface UsageCounterRepository extends JpaRepository<UsageCounter, Long> {
    Optional<UsageCounter> findByUserIdAndPeriodStart(Long userId, Instant periodStart);
}

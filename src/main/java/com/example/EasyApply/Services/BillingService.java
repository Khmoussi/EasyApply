package com.example.EasyApply.Services;

import com.example.EasyApply.billing.PlanType;
import com.example.EasyApply.billing.QuotaExceededException;
import com.example.EasyApply.billing.SubscriptionStatus;
import com.example.EasyApply.billing.UsageDto;
import com.example.EasyApply.domain.AppUser;
import com.example.EasyApply.domain.Subscription;
import com.example.EasyApply.domain.SubscriptionRepository;
import com.example.EasyApply.domain.UsageCounter;
import com.example.EasyApply.domain.UsageCounterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class BillingService {

    public static final int TRIAL_DAYS = 14;

    private final SubscriptionRepository subscriptionRepository;
    private final UsageCounterRepository usageCounterRepository;

    public BillingService(
            SubscriptionRepository subscriptionRepository,
            UsageCounterRepository usageCounterRepository
    ) {
        this.subscriptionRepository = subscriptionRepository;
        this.usageCounterRepository = usageCounterRepository;
    }

    @Transactional
    public Subscription ensureTrialSubscription(AppUser user) {
        return subscriptionRepository.findByUserId(user.getId())
                .orElseGet(() -> createTrial(user));
    }

    private Subscription createTrial(AppUser user) {
        Instant now = Instant.now();
        Subscription sub = new Subscription();
        sub.setUser(user);
        sub.setPlan(PlanType.TRIAL);
        sub.setStatus(SubscriptionStatus.TRIALING);
        sub.setTrialEndsAt(now.plus(TRIAL_DAYS, ChronoUnit.DAYS));
        sub.setCurrentPeriodStart(monthStart(now));
        sub.setCurrentPeriodEnd(monthEnd(now));
        Subscription saved = subscriptionRepository.save(sub);
        ensureUsageCounter(user, monthStart(now));
        return saved;
    }

    @Transactional
    public Subscription getOrRefreshSubscription(AppUser user) {
        Subscription sub = ensureTrialSubscription(user);
        if (sub.getStatus() == SubscriptionStatus.TRIALING
                && sub.getTrialEndsAt() != null
                && Instant.now().isAfter(sub.getTrialEndsAt())) {
            sub.setStatus(SubscriptionStatus.EXPIRED);
            subscriptionRepository.save(sub);
        }
        return sub;
    }

    @Transactional(readOnly = true)
    public boolean canUseProduct(Subscription sub) {
        if (sub.getStatus() == SubscriptionStatus.ACTIVE) {
            return true;
        }
        if (sub.getStatus() == SubscriptionStatus.TRIALING) {
            return sub.getTrialEndsAt() == null || !Instant.now().isAfter(sub.getTrialEndsAt());
        }
        return false;
    }

    @Transactional
    public void requireActive(AppUser user) {
        Subscription sub = getOrRefreshSubscription(user);
        if (!canUseProduct(sub)) {
            throw new QuotaExceededException(
                    "TRIAL_EXPIRED",
                    "subscription",
                    0,
                    0,
                    sub.getPlan()
            );
        }
    }

    /**
     * Validates company quota and returns the effective search limit (clamped to plan + remaining).
     */
    @Transactional
    public int assertCanSearch(AppUser user, int requestedLimit) {
        requireActive(user);
        Subscription sub = getOrRefreshSubscription(user);
        PlanType plan = sub.getPlan();
        UsageCounter usage = currentUsage(user);

        int maxLimit = plan.getMaxSearchLimit();
        int remaining = Math.max(0, plan.getCompaniesPerMonth() - usage.getCompaniesUsed());
        if (remaining <= 0) {
            throw new QuotaExceededException(
                    "QUOTA_EXCEEDED",
                    "companies",
                    usage.getCompaniesUsed(),
                    plan.getCompaniesPerMonth(),
                    plan
            );
        }

        int limit = requestedLimit > 0 ? requestedLimit : maxLimit;
        limit = Math.min(limit, maxLimit);
        limit = Math.min(limit, remaining);
        if (limit <= 0) {
            throw new QuotaExceededException(
                    "QUOTA_EXCEEDED",
                    "companies",
                    usage.getCompaniesUsed(),
                    plan.getCompaniesPerMonth(),
                    plan
            );
        }
        return limit;
    }

    @Transactional
    public void assertCanSend(AppUser user, int recipientCount) {
        requireActive(user);
        if (recipientCount <= 0) {
            return;
        }
        Subscription sub = getOrRefreshSubscription(user);
        PlanType plan = sub.getPlan();
        UsageCounter usage = currentUsage(user);
        int remaining = plan.getSendsPerMonth() - usage.getSendsUsed();
        if (recipientCount > remaining) {
            throw new QuotaExceededException(
                    "QUOTA_EXCEEDED",
                    "sends",
                    usage.getSendsUsed(),
                    plan.getSendsPerMonth(),
                    plan
            );
        }
    }

    @Transactional
    public void recordCompanies(AppUser user, int n) {
        if (n <= 0) {
            return;
        }
        UsageCounter usage = currentUsage(user);
        usage.setCompaniesUsed(usage.getCompaniesUsed() + n);
        usageCounterRepository.save(usage);
    }

    @Transactional
    public void recordSends(AppUser user, int n) {
        if (n <= 0) {
            return;
        }
        UsageCounter usage = currentUsage(user);
        usage.setSendsUsed(usage.getSendsUsed() + n);
        usageCounterRepository.save(usage);
    }

    @Transactional
    public UsageDto getUsage(AppUser user) {
        Subscription sub = getOrRefreshSubscription(user);
        PlanType plan = sub.getPlan();
        UsageCounter usage = currentUsage(user);

        UsageDto dto = new UsageDto();
        dto.setPlan(plan.name());
        dto.setStatus(sub.getStatus().name());
        dto.setTrialEndsAt(sub.getTrialEndsAt());
        dto.setPeriodStart(usage.getPeriodStart());
        dto.setPeriodEnd(monthEnd(usage.getPeriodStart()));
        dto.setCompaniesUsed(usage.getCompaniesUsed());
        dto.setCompaniesLimit(plan.getCompaniesPerMonth());
        dto.setCompaniesRemaining(Math.max(0, plan.getCompaniesPerMonth() - usage.getCompaniesUsed()));
        dto.setSendsUsed(usage.getSendsUsed());
        dto.setSendsLimit(plan.getSendsPerMonth());
        dto.setSendsRemaining(Math.max(0, plan.getSendsPerMonth() - usage.getSendsUsed()));
        dto.setMaxSearchLimit(plan.getMaxSearchLimit());
        dto.setCanUseProduct(canUseProduct(sub));
        return dto;
    }

    @Transactional
    public Subscription save(Subscription subscription) {
        return subscriptionRepository.save(subscription);
    }

    @Transactional(readOnly = true)
    public Subscription findByStripeCustomerId(String customerId) {
        return subscriptionRepository.findByStripeCustomerId(customerId).orElse(null);
    }

    @Transactional(readOnly = true)
    public Subscription findByStripeSubscriptionId(String subscriptionId) {
        return subscriptionRepository.findByStripeSubscriptionId(subscriptionId).orElse(null);
    }

    @Transactional(readOnly = true)
    public Subscription findByUserId(Long userId) {
        return subscriptionRepository.findByUserId(userId).orElse(null);
    }

    private UsageCounter currentUsage(AppUser user) {
        Instant start = monthStart(Instant.now());
        return ensureUsageCounter(user, start);
    }

    private UsageCounter ensureUsageCounter(AppUser user, Instant periodStart) {
        return usageCounterRepository.findByUserIdAndPeriodStart(user.getId(), periodStart)
                .orElseGet(() -> {
                    UsageCounter created = new UsageCounter();
                    created.setUser(user);
                    created.setPeriodStart(periodStart);
                    created.setCompaniesUsed(0);
                    created.setSendsUsed(0);
                    return usageCounterRepository.save(created);
                });
    }

    static Instant monthStart(Instant instant) {
        ZonedDateTime zdt = instant.atZone(ZoneOffset.UTC);
        return zdt.withDayOfMonth(1).toLocalDate().atStartOfDay(ZoneOffset.UTC).toInstant();
    }

    static Instant monthEnd(Instant instant) {
        ZonedDateTime zdt = instant.atZone(ZoneOffset.UTC);
        return zdt.withDayOfMonth(1).plusMonths(1).toLocalDate().atStartOfDay(ZoneOffset.UTC).toInstant();
    }
}

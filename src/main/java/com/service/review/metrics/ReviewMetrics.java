package com.service.review.metrics;

import com.service.review.enums.ContextType;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class ReviewMetrics {
    private final MeterRegistry meterRegistry;

    public void incrementApproved(String domain, ContextType context) {
        meterRegistry.counter(
                "review_approved_total",
                "domain", domain,
                "context", context.name().toLowerCase()
        ).increment();
    }

    public void incrementRejected(String domain, ContextType context) {
        meterRegistry.counter(
                "review_rejected_total",
                "domain", domain,
                "context", context.name().toLowerCase()
        ).increment();
    }

    public void incrementPersistFailure(String domain, ContextType context) {
        meterRegistry.counter(
                "review_persist_failed_total",
                "domain", domain,
                "context", context.name().toLowerCase()
        ).increment();
    }

    public void incrementEventPublished(String domain, ContextType context, String eventType) {
        meterRegistry.counter(
                "review_events_published_total",
                "domain", domain,
                "context", context.name().toLowerCase(),
                "event_type", eventType
        ).increment();
    }

    public void incrementEventPublishFailure(String domain, ContextType context, String eventType) {
        meterRegistry.counter(
                "review_events_publish_failed_total",
                "domain", domain,
                "context", context.name().toLowerCase(),
                "event_type", eventType
        ).increment();
    }

    public void incrementImageDownloadFailure(String domain, ContextType context) {
        meterRegistry.counter(
                "review_image_download_failed_total",
                "domain", domain,
                "context", context.name().toLowerCase()
        ).increment();
    }

    public void recordProcessingTime(String domain, ContextType context, String status, long durationMs) {
        Timer.builder("review_processing_seconds")
                .tags("domain", domain, "context", context.name().toLowerCase(), "status", status)
                .register(meterRegistry)
                .record(durationMs, TimeUnit.MILLISECONDS);
    }

    public void incrementContextFetched(String domain, ContextType context) {
        meterRegistry.counter(
                "review_context_fetched_total",
                "domain", domain,
                "context", context.name().toLowerCase()
        ).increment();
    }

    public void incrementContextFetchFailure(String domain, ContextType context) {
        meterRegistry.counter(
                "review_context_fetch_failed_total",
                "domain", domain,
                "context", context.name().toLowerCase()
        ).increment();
    }
}
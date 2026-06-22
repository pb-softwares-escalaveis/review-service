package com.service.review.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class ConsumerMetrics {
    private final MeterRegistry meterRegistry;

    public void incrementReceived(String domain, String context) {
        meterRegistry.counter(
                "review_events_received_total",
                "domain", domain,
                "context", context
        ).increment();
    }

    public void incrementProcessed(String domain, String context) {
        meterRegistry.counter(
                "review_events_processed_total",
                "domain", domain,
                "context", context
        ).increment();
    }

    public void incrementFailed(String domain, String context, String failureReason) {
        meterRegistry.counter(
                "review_events_failed_total",
                "domain", domain,
                "context", context,
                "failure_reason", failureReason
        ).increment();
    }

    public void incrementContextNotFound(String domain, String context) {
        meterRegistry.counter(
                "review_context_not_found_total",
                "domain", domain,
                "context", context
        ).increment();
    }

    public void recordProcessingTime(String domain, String context, String status, long durationMs) {
        Timer.builder("review_event_processing_seconds")
                .description("Tempo de processamento de eventos de revisão")
                .tags("domain", domain, "context", context, "status", status)
                .register(meterRegistry)
                .record(durationMs, TimeUnit.MILLISECONDS);
    }
}
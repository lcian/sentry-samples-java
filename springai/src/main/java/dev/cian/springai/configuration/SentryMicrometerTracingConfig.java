package dev.cian.springai.configuration;

import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import io.sentry.opentelemetry.OtelSentryPropagator;
import io.sentry.opentelemetry.OtelSentrySpanProcessor;
import io.sentry.opentelemetry.SentrySampler;
import io.sentry.opentelemetry.SentrySpanExporter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class SentryMicrometerTracingConfig {
    @Bean
    SpanProcessor sentrySpanExporter() {
        return BatchSpanProcessor.builder(new SentrySpanExporter()).build();
    }

    @Bean
    SpanProcessor otelSentrySpanExporter() {
        return new OtelSentrySpanProcessor();
    }

    @Bean
    Sampler sentrySampler() {
        return new SentrySampler();
    }

    @Bean
    TextMapPropagator otelSentryPropagator() {
        return new OtelSentryPropagator();
    }

  /*  @Bean
    FilterRegistrationBean<SentryTracingFilter> sentryTracingFilter() {
        FilterRegistrationBean<SentryTracingFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new SentryTracingFilter());
        registrationBean.setEnabled(false);
        return registrationBean;
    } */
}

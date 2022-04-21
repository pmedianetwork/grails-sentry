package grails.plugin.sentry

import org.springframework.web.servlet.HandlerMapping

class SentryTracingInterceptor {

    SentryTracingInterceptor() {
        match(controller: '*')
    }

    /**
     * Sets the request attribute that is used by {@link io.sentry.spring.tracing.TransactionNameProvider TransactionNameProvider} as sentry transaction name.
     * Without it transaction wouldn't be send to sentry by {@link io.sentry.spring.tracing.SentryTracingFilter SentryTracingFilter}.
     */
    boolean before() {
        if (!request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE)) {
            request.setAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE, request.requestURI ?: '/')
        }
        true
    }

}

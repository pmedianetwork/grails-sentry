package grails.plugin.sentry

import io.sentry.Hint
import io.sentry.SentryBaseEvent
import io.sentry.SentryOptions
import org.slf4j.MDC

/**
 * Attaches context tags defined in {@link SentryOptions#getContextTags()} from {@link MDC} to
 * {@link SentryBaseEvent#getTags()}.
 *
 * Copied from {@link io.sentry.spring.ContextTagsEventProcessor}.
 * Workaround <a href="https://github.com/getsentry/sentry-java/issues/2495}">Issue #2495</a>.
 */
final class ContextTagsEventProcessor extends BaseEventProcessor {

    private final SentryOptions options

    ContextTagsEventProcessor(final SentryOptions options) {
        this.options = options
    }

    @Override
    SentryBaseEvent process(SentryBaseEvent event, Hint hint) {
        final Map<String, String> contextMap = MDC.getCopyOfContextMap()
        if (contextMap != null) {
            final Map<String, String> mdcProperties = contextMap.findAll { it.value != null }
            if (!mdcProperties.isEmpty() && !options.getContextTags().isEmpty()) {
                for (final String contextTag : options.getContextTags()) {
                    // if mdc tag is listed in SentryOptions, apply as event tag
                    if (mdcProperties.containsKey(contextTag)) {
                        event.setTag(contextTag, mdcProperties.get(contextTag))
                    }
                }
            }
        }
        return event
    }

}

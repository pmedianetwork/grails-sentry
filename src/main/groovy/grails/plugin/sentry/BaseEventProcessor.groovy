package grails.plugin.sentry

import io.sentry.EventProcessor
import io.sentry.Hint
import io.sentry.SentryBaseEvent
import io.sentry.SentryEvent
import io.sentry.protocol.SentryTransaction

/**
 * Abstract {@link EventProcessor} to simplify handling both error events and transactions at once.
 */
abstract class BaseEventProcessor implements EventProcessor {

    @Override
    SentryEvent process(final SentryEvent event, final Hint hint) {
        (SentryEvent) process((SentryBaseEvent) event, hint)
    }

    @Override
    SentryTransaction process(final SentryTransaction transaction, final Hint hint) {
        (SentryTransaction) process((SentryBaseEvent) transaction, hint)
    }

    abstract SentryBaseEvent process(final SentryBaseEvent event, final Hint hint)

}

package grails.plugin.sentry

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.sentry.EventProcessor
import io.sentry.SentryEvent
import org.codehaus.groovy.runtime.StackTraceUtils
import org.springframework.beans.factory.annotation.Autowired

@Slf4j
@CompileStatic
class GrailsEventProcessor implements EventProcessor {

    @Autowired
    SentryConfig sentryConfig

    @Override
    SentryEvent process(SentryEvent event, Object hint) {

        // remove trash from message
        event.message.formatted -= ' Stacktrace follows:'
        event.message.formatted -= 'Full Stack Trace:'

        // more concrete message in transaction
        if (event.throwable?.cause?.stackTrace?.length > 0) {
            event.transaction = event.throwable.cause.stackTrace[0].toString()
            event.logger = event.throwable.cause.stackTrace[0].className
        } else {
            event.transaction = event.logger
        }

        // revert order of exceptions to retain old SDK behavior
        event.exceptions = event.exceptions.reverse()

        // sanitize stacktrace
        if (sentryConfig?.sanitizeStackTrace && !Boolean.getBoolean("groovy.full.stacktrace")) {
            event.exceptions?.each {exception ->
                if (exception?.stacktrace?.frames) {
                    exception.stacktrace.frames = exception.stacktrace.frames.findAll {frame ->
                        StackTraceUtils.isApplicationClass(frame.module)
                    }
                }
            }
        }

        log.debug("Sentry event processed by GrailsEventProcessor:\n" + event.properties)

        return event

    }

}

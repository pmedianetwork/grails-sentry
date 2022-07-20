package grails.plugin.sentry

import io.sentry.ISpan
import io.sentry.Sentry
import io.sentry.SpanStatus

class SentryControllerInterceptor {

    int order = LOWEST_PRECEDENCE

    public static final String SENTRY_CONTROLLER_SPAN = 'SENTRY_CONTROLLER_SPAN'
    public static final String SENTRY_VIEW_SPAN = 'SENTRY_VIEW_SPAN'

    SentryControllerInterceptor() {
        match(controller: '*')
    }

    boolean before() {
        request[SENTRY_CONTROLLER_SPAN] = Sentry.span?.startChild('grails.controller', "${controllerName}/${actionName}")
        true
    }

    boolean after() {
        if (request[SENTRY_CONTROLLER_SPAN] instanceof ISpan) {
            (request[SENTRY_CONTROLLER_SPAN] as ISpan).finish(SpanStatus.OK)
        }
        if (view) {
            request[SENTRY_VIEW_SPAN] = Sentry.span?.startChild('grails.view', view)
        }
        true
    }

    void afterView() {
        if (request[SENTRY_VIEW_SPAN] instanceof ISpan) {
            (request[SENTRY_VIEW_SPAN] as ISpan).finish(SpanStatus.OK)
        }
        true
    }

}

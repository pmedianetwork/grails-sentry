package grails.plugin.sentry

import io.sentry.Sentry
import io.sentry.SentryTraceHeader

class SentryTagLib {

    static final namespace = 'sentry'

    SentryConfig sentryPluginConfig

    /**
     * Meta tag containing sentry trace.
     *
     * To enable <a href="https://docs.sentry.io/product/sentry-basics/tracing/distributed-tracing/">distributed tracing</a>
     * you need to <a href="https://docs.sentry.io/platforms/java/performance/connect-services/">connect services</a> (frontend)
     * by adding this tag (`<sentry:traceMeta />`) to `<head>` of your gsp view.
     */
    def traceMeta = { attrs, body ->
        SentryTraceHeader sentryTrace = Sentry.currentHub.span?.toSentryTrace()
        if (sentryTrace) {
            out << "<meta name=\"${SentryTraceHeader.SENTRY_TRACE_HEADER}\" content=\"${sentryTrace.value}\" />"
        }
    }

    /**
     * Link to current trace in sentry app.
     *
     * If `linkPrefix` is configured and current trace is sampled then this tag displays link composed of `linkPrefix`
     * and current trace id.
     *
     * Link label can by provided in tag's body otherwise embedded svg sentry logo is displayed.
     */
    def traceLink = { attrs, body ->
        SentryTraceHeader sentryTrace = Sentry.currentHub.span?.toSentryTrace()
        String linkPrefix = sentryPluginConfig.linkPrefix
        if (sentryTrace?.sampled && linkPrefix) {
            out << """<a href="${linkPrefix}/performance/trace/${sentryTrace.traceId}" target="_blank class="sentry-trace-link">"""
            out << (body() ?: """
                <svg style="vertical-align: text-bottom; margin: 0 8px;" fill="currentColor" viewBox="0 0 16 16" height="16px" width="16px">
                    <path d="M15.8,14.57a1.53,1.53,0,0,0,0-1.52L9.28,1.43a1.46,1.46,0,0,0-2.56,0L4.61,5.18l.54.32A10.43,10.43,0,0,1,8.92,9.39a10.84,10.84,0,0,1,1.37,4.67H8.81a9.29,9.29,0,0,0-1.16-3.91A9,9,0,0,0,4.41,6.81L3.88,6.5,1.91,10l.53.32a5.12,5.12,0,0,1,2.42,3.73H1.48a.25.25,0,0,1-.21-.12.24.24,0,0,1,0-.25L2.21,12a3.32,3.32,0,0,0-1.07-.63L.2,13.05a1.53,1.53,0,0,0,0,1.52,1.46,1.46,0,0,0,1.28.76H6.13V14.7a6.55,6.55,0,0,0-.82-3.16,6.31,6.31,0,0,0-1.73-2l.74-1.32a7.85,7.85,0,0,1,2.26,2.53,8,8,0,0,1,1,3.92v.63h3.94V14.7A12.14,12.14,0,0,0,10,8.75a11.8,11.8,0,0,0-3.7-4l1.5-2.67a.24.24,0,0,1,.42,0l6.52,11.63a.24.24,0,0,1,0,.25.24.24,0,0,1-.21.12H13c0,.43,0,.85,0,1.27h1.53a1.46,1.46,0,0,0,1.28-.76"></path>
                </svg>
            """)
            out << '</a>'
        }
    }
}

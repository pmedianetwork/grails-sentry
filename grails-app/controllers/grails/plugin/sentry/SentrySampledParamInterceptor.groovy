package grails.plugin.sentry

import io.sentry.Sentry

class SentrySampledParamInterceptor {

    SentrySampledParamInterceptor() {
        match(controller: '*')
    }

    /**
     * Forces sentry sampled according to "sentrySampled" request param.
     */
    boolean before() {
        if (params.sentrySampled != null) {
            Sentry.span?.spanContext?.setSampled(Boolean.valueOf(params.sentrySampled as String))
        }
        true
    }

}

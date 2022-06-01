package grails.plugin.sentry

import ch.qos.logback.classic.Logger
import com.stehno.ersatz.ErsatzServer
import grails.testing.mixin.integration.Integration
import io.sentry.Sentry
import io.sentry.logback.SentryAppender
import io.sentry.servlet.SentryServletRequestListener
import org.slf4j.LoggerFactory
import spock.lang.Specification

@Integration
class SanityIntegrationSpec extends Specification {

    SentryAppender sentryAppender
    GrailsEventProcessor grailsEventProcessor
    SentryServletRequestListener sentryServletRequestListener

    def "everything works"() {
        expect: "if everything is ok sentry then beans are injected"
            sentryAppender
            grailsEventProcessor
            sentryServletRequestListener
        when: "mock http server is started"
            ErsatzServer server = new ErsatzServer().expectations {
                post("/api/123/envelope/") {
                    called 1
                    responder {
                        code 200
                    }
                }
            }
            server.start()
            println "Ersatz server started at localhost:${server.httpPort}"
        and: "mock server is used as Sentry endpoint"
            Sentry.init("http://foo:bar@localhost:${server.httpPort}/123?async=false")
            LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME).error("Test Me", new Exception("Failure!"))
            Sentry.flush(60 * 1000)
        then: "event is send to the mock server"
            server.verify()
    }

}

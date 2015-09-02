package grails.plugin.sentry

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.LoggerContext
import grails.plugins.*
import org.slf4j.LoggerFactory
import net.kencochrane.raven.DefaultRavenFactory
import net.kencochrane.raven.dsn.Dsn

class SentryGrailsPlugin extends Plugin {

    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "3.0.1 > *"
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
        "grails-app/views/error.gsp"
    ]

    def title = "Sentry Plugin"
    def author = "Benoit Hediard"
    def authorEmail = "ben@benorama.com"
    def description = "Sentry Client for Grails"
    def profiles = ['web']
    def documentation = "http://github.com/agorapulse/grails-raven/blob/master/README.md"

    def license = "APACHE"
    def developers = [ [ name: "Benoit Hediard", email: "ben@benorama.com" ] ]
    def issueManagement = [ system: "GitHub", url: "http://github.com/agorapulse/grails-raven/issues" ]
    def scm = [ url: "http://github.com/agorapulse/grails-raven" ]

    Closure doWithSpring() {
        {->
            def pluginConfig = grailsApplication.config.grails?.plugin?.sentry
            if (pluginConfig?.dsn) {
                log.info "Sentry config found, creating Sentry client and corresponding Logback appender"
                ravenFactory(DefaultRavenFactory)
                raven(ravenFactory: "createRavenInstance", new Dsn(pluginConfig.dsn)) { bean ->
                    bean.autowire = 'byName'
                }
                sentryAppender(GrailsLogbackSentryAppender, ref('raven'), pluginConfig)
            } else {
                log.warn "Raven config not found, add 'grails.plugin.sentry.dsn' to your config to enable Sentry client"
            }
        }
    }

    void doWithApplicationContext() {
        def configLoggers = grailsApplication.config.grails?.plugin?.sentry?.loggers

        GrailsLogbackSentryAppender appender = applicationContext.sentryAppender
        if (appender) {
            LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory()
            if (configLoggers) {
                def loggers = configLoggers.tokenize(',')
                loggers.each { String logger ->
                    loggerContext.getLogger(logger).addAppender(appender)
                }
            } else {
                loggerContext.getLogger(Logger.ROOT_LOGGER_NAME).addAppender(appender)
            }
            appender.start()
        }
    }
    
}
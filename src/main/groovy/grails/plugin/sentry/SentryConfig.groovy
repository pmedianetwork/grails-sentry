package grails.plugin.sentry

import ch.qos.logback.classic.Level
import groovy.transform.ToString

/**
 * @author <a href='mailto:alexey@zhokhov.com'>Alexey Zhokhov</a>
 */
/*
    EXAMPLE
        dsn: https://foo:bar@api.sentry.io/123
        loggers: [LOGGER1, LOGGER2, LOGGER3]
        environment: staging
        serverName: dev.server.com
        minLevel: ERROR
        tags: {tag1: val1,  tag2: val2, tag3: val3}
        subsystems:
            MODULE1: [com.company.services.module1, com.company.controllers.module1]
            MODULE2: [com.company.services.module2, com.company.controllers.module2]
            MODULE3: [com.company.services.module3, com.company.controllers.module3]
        logHttpRequest: true
        disableMDCInsertingServletFilter: true
        springSecurityUser: true
        sanitizeStackTrace: true
        inAppIncludes:
          - my.app.package.one
          - my.app.package.two
        tracesSampleRate: 0.5
        traceJDBC: true
        linkPrefix: https://sentry.example.com/organizations/company
        springSecurityUserProperties:
            id: 'id'
            email: 'emailAddress'
            username: 'login'
        priorities:
            HIGH: [java.lang, com.microsoft.sqlserver.jdbc.SQLServerException]
            MID: [com.company.exception]
            LOW: [java.io]
 */

@ToString
class SentryConfig {

    SentryConfig(Map config = [:]) {
        if (!config) {
            active = false

            return
        }

        if (config.dsn) {
            dsn = config.dsn?.toString()
            active = true
        }

        if (config.containsKey('active') && config.active as String == 'false') {
            active = false
        }

        if (config.loggers) {
            if (config.loggers instanceof List) {
                loggers = (config.loggers as List).collect { it.toString() }
            }
            if (config.loggers instanceof String) {
                loggers = (config.loggers as String).split(",").collect { it.toString() }
            }
        }

        environment = config.environment ?: environment
        serverName = config.serverName ?: serverName

        if (config.minLevel) {
            minLevel = Level.toLevel(config.minLevel.toString().toUpperCase())
        }

        if (config.tags && config.tags instanceof Map) {
            tags = config.tags as Map<String, String>
        }

        if (config.logHttpRequest as String == 'true') {
            logHttpRequest = true
        }
        if (config.disableMDCInsertingServletFilter as String == 'true') {
            disableMDCInsertingServletFilter = true
        }
        if (config.springSecurityUser as String == 'true') {
            springSecurityUser = true
        }
        if (config.sanitizeStackTrace as String == 'true') {
            sanitizeStackTrace = true
        }

        if (config.inAppIncludes) {
            if (config.inAppIncludes instanceof List) {
                inAppIncludes = config.inAppIncludes as List
            }
            if (config.inAppIncludes instanceof String) {
                inAppIncludes = (config.inAppIncludes as String).split(",") as List
            }
        }

        if (config.tracesSampleRate?.toString()?.isDouble()) {
            tracesSampleRate = config.tracesSampleRate as Double
        }

        if (config.traceJDBC as String == 'true') {
            traceJDBC = true
        }

        linkPrefix = config.linkPrefix as String

        if (config.springSecurityUserProperties && config.springSecurityUserProperties instanceof Map) {
            springSecurityUserProperties = new SpringSecurityUserProperties(
                    id: (config.springSecurityUserProperties as Map).id as String ?: null,
                    email: (config.springSecurityUserProperties as Map).email as String ?: null,
                    username: (config.springSecurityUserProperties as Map).username as String ?: null,
                    data: (config.springSecurityUserProperties as Map).data as List ?: null
            )
        }
    }

    boolean active = false
    String dsn
    List<String> loggers = []
    String environment
    String serverName
    Level minLevel = Level.WARN
    Map<String, String> tags = [:]
    boolean logHttpRequest = false
    boolean disableMDCInsertingServletFilter = false
    boolean springSecurityUser = false
    boolean sanitizeStackTrace = false
    List<String> inAppIncludes = []
    double tracesSampleRate = 0.0
    boolean traceJDBC = false
    String linkPrefix

    // TODO
    // priorities
    // subsystems

    SpringSecurityUserProperties springSecurityUserProperties

    @ToString
    static class SpringSecurityUserProperties {
        String id
        String email
        String username
        List<String> data = []
    }

}

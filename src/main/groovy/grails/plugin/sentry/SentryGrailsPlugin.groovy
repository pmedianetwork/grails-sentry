/*
 * Copyright 2016 Alan Rafael Fachini, authors, and contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package grails.plugin.sentry

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.helpers.MDCInsertingServletFilter
import grails.plugins.Plugin
import grails.util.Environment
import grails.util.Metadata
import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import groovy.util.logging.Slf4j
import io.sentry.EventProcessor
import io.sentry.HubAdapter
import io.sentry.Sentry
import io.sentry.SentryOptions
import io.sentry.jdbc.SentryJdbcEventListener
import io.sentry.logback.SentryAppender
import io.sentry.servlet.SentryServletRequestListener
import io.sentry.spring.ContextTagsEventProcessor
import io.sentry.spring.HttpServletRequestSentryUserProvider
import io.sentry.spring.SentryUserFilter
import io.sentry.spring.tracing.SentryTracingFilter
import org.slf4j.LoggerFactory
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.core.Ordered

@CompileStatic(TypeCheckingMode.SKIP)
@Slf4j
class SentryGrailsPlugin extends Plugin {

    private static final String TAG_GRAILS_APP_NAME = 'grails_app_name'
    private static final String TAG_GRAILS_VERSION = 'grails_version'

    // the version or versions of Grails the plugin is designed for
    def grailsVersion = '3.0.0 > *'

    def title = 'Sentry Plugin'
    def author = 'Benoit Hediard'
    def authorEmail = 'ben@benorama.com'
    def description = 'Sentry Client for Grails'
    def profiles = ['web']
    def documentation = 'http://github.com/agorapulse/grails-sentry/blob/master/README.md'

    def license = 'APACHE'
    def developers = [[name: 'Benoit Hediard', email: 'ben@benorama.com'], [name: 'Alexey Zhokhov', email: 'alexey@zhokhov.com']]
    def issueManagement = [system: 'GitHub', url: 'http://github.com/agorapulse/grails-sentry/issues']
    def scm = [url: 'http://github.com/agorapulse/grails-sentry']

    @Lazy
    private SentryConfig pluginConfig = new SentryConfig(config.grails.plugin.sentry)

    Closure doWithSpring() {
        { ->
            // Register sentryPluginConfig bean
            delegate.parentCtx.beanFactory.registerSingleton('sentryPluginConfig', pluginConfig)

            if (!pluginConfig?.active) {
                log.info "Sentry disabled"
                return
            }

            if (pluginConfig?.dsn) {
                log.info 'Sentry config found, creating Sentry client and corresponding Logback appender'

                delegate.parentCtx.beanFactory.registerSingleton('sentryHub', HubAdapter.getInstance())

                sentryOptions(SentryOptions)
                sentryAppender(SentryAppender)

                grailsEventProcessor(GrailsEventProcessor)
                contextTagsEventProcessor(ContextTagsEventProcessor)

                httpServletRequestSentryUserProvider(HttpServletRequestSentryUserProvider)
                sentryUserFilter(SentryUserFilter)
                sentryUserFilterRegistration(FilterRegistrationBean) {
                    filter = sentryUserFilter
                }

                if (pluginConfig.logHttpRequest) {
                    sentryServletRequestListener(SentryServletRequestListener)
                }

                if (pluginConfig.springSecurityUser) {
                    springSecuritySentryUserProvider(SpringSecuritySentryUserProvider){
                        springSecurityService = ref('springSecurityService')
                    }
                }

                if (pluginConfig.tracesSampleRate > 0) {
                    sentryTracingFilter(SentryTracingFilter)
                    sentryTracingFilterRegistration(FilterRegistrationBean) {
                        filter = sentryTracingFilter
                        order = Ordered.HIGHEST_PRECEDENCE + 1
                    }
                    if (pluginConfig.traceJDBC) {
                        sentryDataSourceWrapper(SentryDataSourceWrapper)
                        sentryJdbcEventListener(SentryJdbcEventListener)
                    }
                }

                if (!pluginConfig.disableMDCInsertingServletFilter) {
                    log.info 'Activating MDCInsertingServletFilter'
                    mdcInsertingServletFilter(FilterRegistrationBean) {
                        filter = bean(MDCInsertingServletFilter)
                    }
                }
            } else {
                log.warn "Sentry config not found, add 'grails.plugin.sentry.dsn' to your config to enable Sentry client"
            }
        }
    }

    void doWithApplicationContext() {
        if (!pluginConfig?.active) {
            return
        }

        SentryAppender appender = applicationContext.getBean(SentryAppender)
        if (appender) {
            LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory()
            if (pluginConfig.loggers) {
                pluginConfig.loggers.each { String logger ->
                    loggerContext.getLogger(logger).addAppender(appender)
                }
            } else {
                loggerContext.getLogger(Logger.ROOT_LOGGER_NAME).addAppender(appender)
            }
            if (pluginConfig.minLevel) {
                appender.minimumEventLevel = pluginConfig.minLevel
            }
            appender.setContext(loggerContext)
            appender.start()
        }

        SentryOptions options = applicationContext.getBean(SentryOptions)
        options.enableExternalConfiguration = true
        options.sendDefaultPii = true
        options.dsn = pluginConfig.dsn
        options.tracesSampleRate = pluginConfig.tracesSampleRate
        options.inAppIncludes.addAll pluginConfig.inAppIncludes
        options.environment = pluginConfig.environment ?: Environment.current.name
        options.serverName = pluginConfig.serverName

        Metadata metadata = Metadata.current
        options.release = metadata.getApplicationVersion()
        options.setTag(TAG_GRAILS_APP_NAME, metadata.getApplicationName())
        options.setTag(TAG_GRAILS_VERSION, metadata.getGrailsVersion())

        pluginConfig.tags?.each { String key, String value ->
            options.setTag(key, value)
        }

        applicationContext.getBeansOfType(EventProcessor).each {beanName, bean ->
            log.info "Registering sentry event processor $beanName (${bean.getClass()})"
            options.addEventProcessor(bean)
        }

        Sentry.init(options)
    }

}

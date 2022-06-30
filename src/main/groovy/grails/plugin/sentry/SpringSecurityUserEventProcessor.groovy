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

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import io.sentry.EventProcessor
import io.sentry.SentryBaseEvent
import io.sentry.SentryEvent
import io.sentry.protocol.SentryTransaction
import io.sentry.protocol.User
import org.grails.web.util.WebUtils

import javax.servlet.http.HttpServletRequest

/**
 * @author <a href='mailto:alexey@zhokhov.com'>Alexey Zhokhov</a>
 */
@CompileStatic
class SpringSecurityUserEventProcessor implements EventProcessor {

    private static final List<String> IP_HEADERS = ['X-Real-IP',
                                                    'Client-IP',
                                                    'X-Forwarded-For',
                                                    'Proxy-Client-IP',
                                                    'WL-Proxy-Client-IP',
                                                    'rlnclientipaddr']

    SentryConfig config

    SpringSecurityUserEventProcessor(SentryConfig config) {
        this.config = config
    }

    def springSecurityService

    @Override
    SentryEvent process(SentryEvent event, Object hint) {
        process(event)
    }

    @Override
    SentryTransaction process(SentryTransaction transaction, Object hint) {
        process(transaction)
    }

    @CompileStatic(TypeCheckingMode.SKIP)
    <E extends SentryBaseEvent> E process(E event) {
        def isLoggedIn = springSecurityService?.isLoggedIn()

        if (isLoggedIn) {
            def principal = springSecurityService.getPrincipal()

            if (principal != null && principal != 'anonymousUser') {
                String idPropertyName = 'id'
                String emailPropertyName = null
                String usernamePropertyName = 'username'
                List data = null

                if (config?.springSecurityUserProperties) {
                    if (config.springSecurityUserProperties.id) {
                        idPropertyName = config.springSecurityUserProperties.id
                    }
                    if (config.springSecurityUserProperties.email) {
                        emailPropertyName = config.springSecurityUserProperties.email
                    }
                    if (config.springSecurityUserProperties.username) {
                        usernamePropertyName = config.springSecurityUserProperties.username
                    }
                    if (config.springSecurityUserProperties.data) {
                        data = config.springSecurityUserProperties.data
                    }
                }

                def id = principal[idPropertyName].toString()
                String username = principal[usernamePropertyName].toString()
                String ipAddress = getIpAddress(request)
                String email = emailPropertyName ? principal[emailPropertyName].toString() : null
                Map<String, Object> extraData = [:]
                data.each { Object key ->
                    extraData[key as String] = principal[key as String].toString()
                }

                User user = new User(id: id, username: username, ipAddress: ipAddress, email: email, unknown: extraData)
                event.setUser(user)
            }
        }
        return event
    }

    private static HttpServletRequest getRequest() {
        try {
            WebUtils.retrieveGrailsWebRequest()?.request
        } catch (e) {
            null
        }

    }

    private static String getIpAddress(HttpServletRequest request) {
        String unknown = '127.0.0.1'
        String ipAddress = unknown

        if (request) {
            IP_HEADERS.each { header ->
                if (!ipAddress || unknown.equalsIgnoreCase(ipAddress))
                    ipAddress = request.getHeader(header)
            }

            if (!ipAddress)
                ipAddress = request.remoteAddr
        }

        return ipAddress
    }

}

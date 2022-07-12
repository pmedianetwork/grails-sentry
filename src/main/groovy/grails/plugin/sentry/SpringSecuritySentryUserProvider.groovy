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
import io.sentry.protocol.User
import io.sentry.spring.SentryUserProvider
import org.grails.web.util.WebUtils

import javax.servlet.http.HttpServletRequest

@CompileStatic
class SpringSecuritySentryUserProvider implements SentryUserProvider {

    private static final List<String> IP_HEADERS = ['X-Real-IP',
                                                    'Client-IP',
                                                    'X-Forwarded-For',
                                                    'Proxy-Client-IP',
                                                    'WL-Proxy-Client-IP',
                                                    'rlnclientipaddr']

    private SentryConfig config

    SpringSecuritySentryUserProvider(SentryConfig config) {
        this.config = config
    }

    def springSecurityService

    @CompileStatic(TypeCheckingMode.SKIP)
    @Override
    User provideUser() {
        def isLoggedIn = springSecurityService?.isLoggedIn()

        if (isLoggedIn) {
            def principal = springSecurityService.getPrincipal()

            if (principal && !(principal instanceof String)) {
                String idPropertyName = config.springSecurityUserProperties?.id ?: 'id'
                String emailPropertyName = config.springSecurityUserProperties?.email
                String usernamePropertyName = config.springSecurityUserProperties?.username ?: 'username'
                List<String> data = config.springSecurityUserProperties?.data ?: []

                String id = principal[idPropertyName]
                String username = principal[usernamePropertyName]
                String ipAddress = getIpAddress(request)
                String email = emailPropertyName ? principal[emailPropertyName] : null
                Map<String, String> extraData = [:]
                data.each { String key ->
                    String value = principal[key] as String
                    if (value != null) {
                        extraData[key] = value
                    }
                }

                User user = new User(id: id, username: username, ipAddress: ipAddress, email: email, unknown: extraData)
                return user
            }
        }
        return null
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

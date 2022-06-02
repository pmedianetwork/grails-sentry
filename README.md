Sentry Grails Plugin
====================

# Introduction

Sentry plugin provides a Grails client for integrating apps with [Sentry](http://www.getsentry.com). 
[Sentry](http://www.getsentry.com) is an event logging platform primarily focused on capturing and aggregating exceptions.

It uses the official [Sentry.io](https://github.com/getsentry/sentry-java) client under the cover.

# Installation

Declare the plugin dependency in the _build.gradle_ file, as shown here:

```groovy
dependencies {
    ...
    compile("com.github.pmedianetwork:grails-sentry:1.0.0")
    ...
}
```

# Config

Add your Sentry DSN to your _grails-app/conf/application.yml_.

```yml
grails:
    plugin:
        sentry:
            dsn: https://{PUBLIC_KEY}@app.getsentry.com/{PROJECT_ID}
```

The DSN can be found in project's _Settings_ under _Client Keys (DSN)_ section.

The plugin will sent notifications to Sentry by default, if you want to disable notifications for an specific environment set the `active` option as false.

```yml
environments:
    development:
        grails:
            plugin:
                sentry:
                    active: false
    test:
        grails:
            plugin:
                sentry:
                    active: false
```

You can also configure the multiple logger to which you want to append the sentry appender.
You can also set the server name, but it is recommended to don't set this configuration and let the plugin to resolve it.


## Optional configurations

```yml
grails:
    plugin:
        sentry:
            dsn: https://foo@api.sentry.io/123
            loggers: [LOGGER1, LOGGER2, LOGGER3]
            environment: staging
            serverName: dev.server.com
            minLevel: ERROR
            tags: {tag1: val1,  tag2: val2, tag3: val3}
            logHttpRequest: true
            disableMDCInsertingServletFilter: true
            inAppIncludes:
              - my.app.package.one
              - my.app.package.two
            tracesSampleRate: 0.5
            traceJDBC: true
            linkPrefix: https://sentry.example.com/organizations/company
            springSecurityUser: true
            springSecurityUserProperties:
                id: 'id'
                email: 'emailAddress'
                username: 'login'
                data: # Additional properties to be retrieved from user details object and passed as extra properties to Sentry user interface.
                    - 'authorities'
```

Check [Sentry-java](https://github.com/getsentry/sentry-java) documentation to configure connection, protocol and async options in your DSN. If you are sending extra tags from the plugin for the exceptions, make sure to enable the corresponding tag on sentry tag settings for the particular project to see the tag as a filter on the exception stream on sentry.


# Usage

## Logback Appender

The Logback Appender is automatically configured by the plugin, you just have to set enabled environments as shown in Configuration section.

All application exceptions will be logged on sentry by the appender.
The appender is configured to log just the `WARN` level and up.
To log manually just use the `log.error()` method.

## Capturing events manually

You also can use `Sentry` class to sent info messages to Sentry:

```groovy
import io.sentry.Sentry
import io.sentry.SentryEvent
import io.sentry.SentryLevel
import io.sentry.protocol.Message

// Send simple message
Sentry.currentHub.captureMessage("some message")

// Send exception
Sentry.currentHub.captureException(new Exception("some exception"))

// Custom event
SentryEvent event = new SentryEvent(
        message: new Message(message: "This is a test"),
        level: SentryLevel.INFO,
        logger: MyClass.class.name,
)

Sentry.currentHub.captureEvent(event)
```

## Performance monitoring

Sentry [performance monitoring](https://docs.sentry.io/platforms/java/performance/) is disabled by default.
You have to set config `tracesSampleRate` to value greater than `0` to enable it.
[Sample rate](https://docs.sentry.io/platforms/java/configuration/sampling/#sampling-transaction-events) represents percentage of requests that should be traced (value `1` = 100%).
Requests handled by controllers are then automatically traced in Sentry according to sample rate. 
You can also enable [JDBC tracing](https://docs.sentry.io/platforms/java/performance/instrumentation/jdbc/) by setting config `traceJDBC` to `true`.

To enable [distributed tracing](https://docs.sentry.io/product/sentry-basics/tracing/distributed-tracing/) you need to [connect services](https://docs.sentry.io/platforms/java/performance/connect-services/) (frontend) 
by adding `<sentry:traceMeta />` to `<head>` of your gsp view. 

If you configure `linkPrefix` you can also add `<sentry:traceLink />` to your gsp view which displays direct link to transaction in sentry application.

# Latest releases
* 2022-05-16 **1.0.0**: upgrade Sentry java lib to 5.7.0 + performance monitoring

# Historical releases ("org.grails.plugins:sentry")

* 2018-07-16 **V11.7.25** : upgrade Sentry java lib to 1.7.25 + stack trace sanitizer
* 2018-07-16 **V11.7.24** : upgrade Sentry java lib to 1.7.24 + Grails 4 upgrade
* 2018-05-18 **V11.7.4** : upgrade Sentry java lib to 1.7.4 + bug fixes
* 2018-02-09 **V11.6.5** : upgrade Sentry java lib to 1.6.5 + bug fixes
* 2017-11-09 **V11.4.0.3** : fixes
* 2017-08-03 **V11.4.0.2** : fixes
* 2017-08-03 **V11.4.0** : upgrade Sentry java lib to 1.4.0 + bug fix, thanks to [donbeave](https://github.com/donbeave) PR #37
* 2017-07-17 **V11.3.0** : upgrade Sentry java lib to 1.3.0 + bug fix, thanks to [donbeave](https://github.com/donbeave) PR #34
* 2017-07-04 **V11.2.0** : upgrade Sentry java lib to 1.2.0 (which replaces the deprecated Raven java lib)
* 2017-06-06 **V8.0.3** : upgrade Raven java lib to 8.0.3
* 2017-02-01 **V7.8.1** : upgrade Raven java lib to 7.8.1
* 2016-11-22 **V7.8.0.2** : event environment support 
* 2016-10-29 **V7.8.0.1** : minor bug fix, thanks to [donbeave](https://github.com/donbeave) PR
* 2016-10-19 **V7.8.0** : upgrade Raven java lib to 7.8.0
* 2016-10-10 **V7.7.1** : upgrade Raven java lib to 7.7.1
* 2016-09-27 **V7.7.0.1** : bug fix
* 2016-09-26 **V7.7.0** : upgrade Raven java lib to 7.7.0, release support added to events
* 2016-08-22 **V7.6.0** : upgrade Raven java lib to 7.6.0, Spring Security integration improvements, thanks to [donbeave](https://github.com/donbeave) PR
* 2016-07-22 **V7.4.0** : upgrade Raven java lib to 7.4.0, better logging and support for Spring Security Core , thanks to [donbeave](https://github.com/donbeave) PR
* 2016-06-22 **V7.3.0** : upgrade Raven java lib to 7.3.0
* 2016-05-03 **V7.2.1** : upgrade Raven java lib to 7.2.1
* 2016-04-12 **V7.1.0.1** : minor update
* 2016-04-06 **V7.1.0** : upgrade Raven java lib to 7.1.0, thanks to [donbeave](https://github.com/donbeave) PR (WARNING: Raven package has been renamed from `net.kencochrane.raven` to `com.getsentry.raven`)
* 2015-08-31 **V6.0.0** : initial release for Grails 3.x

## Bugs

To report any bug, please use the project [Issues](https://github.com/pmedianetwork/grails-sentry/issues/new) section on GitHub.

## Contributing

Please contribute using [Github Flow](https://guides.github.com/introduction/flow/). Create a branch, add commits, and [open a pull request](https://github.com/pmedianetwork/grails-sentry/compare/).

## License

Copyright © 2016 Alan Rafael Fachini, authors, and contributors. All rights reserved.

This project is licensed under the Apache License, Version 2.0 - see the [LICENSE](LICENSE) file for details.

## Maintained by

[![adverity](https://www.adverity.com/hubfs/adverity-logo-1.svg)](https://www.adverity.com)

package grails.plugin.sentry

import com.p6spy.engine.spy.P6DataSource
import org.springframework.beans.BeansException
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.jdbc.datasource.DelegatingDataSource

/**
 * Inject {@link com.p6spy.engine.spy.P6DataSource P6DataSource} wrapper into existing DataSource to enable
 * {@link io.sentry.jdbc.SentryJdbcEventListener SentryJdbcEventListener} to trace jdbc queries in sentry.
 */
class SentryDataSourceWrapper implements BeanPostProcessor {

    @Override
    Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        bean
    }

    @Override
    Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof DelegatingDataSource) {
            wrapDataSource((DelegatingDataSource) bean)
        }
        bean
    }

    private void wrapDataSource(DelegatingDataSource dataSource) {
        if (dataSource.targetDataSource instanceof DelegatingDataSource) {
            wrapDataSource((DelegatingDataSource) dataSource.targetDataSource)
        } else {
            dataSource.targetDataSource = new P6DataSource(dataSource.targetDataSource)
        }
    }

}

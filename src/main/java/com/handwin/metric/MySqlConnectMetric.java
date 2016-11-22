package com.handwin.metric;

import com.codahale.metrics.Gauge;
import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * Created by piguangtao on 14/12/26.
 */
@Service
public class MySqlConnectMetric extends DefaultMetric implements InitializingBean {

    @Autowired
    @Qualifier(value = "mysql002.chatgame.me")
    private BasicDataSource dataSource;

    private Gauge<Integer> activeConnects = null;

    private Gauge<Integer> idleConnects = null;

    /**
     * Invoked by a BeanFactory after it has set all bean properties supplied
     * (and satisfied BeanFactoryAware and ApplicationContextAware).
     * <p>This method allows the bean instance to perform initialization only
     * possible when all bean properties have been set and to throw an
     * exception in the event of misconfiguration.
     *
     * @throws Exception in the event of misconfiguration (such
     *                   as failure to set an essential property) or if initialization fails.
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        if (!isMetricEnable()) return;
        activeConnects = getMetricRegistry().register("mysql-active-connect-count", () -> dataSource.getNumActive());
        idleConnects = getMetricRegistry().register("mysql-idle-connect-count", () -> dataSource.getNumIdle());
    }
}

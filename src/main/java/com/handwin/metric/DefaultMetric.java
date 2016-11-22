package com.handwin.metric;

import com.codahale.metrics.MetricRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

/**
 * Created by piguangtao on 14/12/26.
 */
public class DefaultMetric {
    @Autowired
    private MetricRegistry metricRegistry;

    @Value("${metric.enable}")
    private String metricEnable;

    public boolean isMetricEnable(){
        return metricEnable != null && "yes".equalsIgnoreCase(metricEnable.trim());
    }

    public MetricRegistry getMetricRegistry() {
        return metricRegistry;
    }
}

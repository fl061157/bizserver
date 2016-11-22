package com.handwin.metric;

import com.rabbitmq.client.Envelope;

/**
 * Created by piguangtao on 14/12/25.
 */
public interface IMqMetricFilter {
    public void before(Envelope envelope);
    public void after(Envelope envelope);
}

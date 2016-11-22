package com.handwin.metric;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.gs.collections.impl.map.mutable.ConcurrentHashMap;
import com.rabbitmq.client.Envelope;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by piguangtao on 14/12/25.
 */
@Service
public class MqMetricFilterImpl extends DefaultMetric implements IMqMetricFilter,InitializingBean {

    private ConcurrentHashMap<String, Meter> meterMap = new ConcurrentHashMap<>();

    private ConcurrentHashMap<String, Timer> timerMap = new ConcurrentHashMap<>();

    private ConcurrentHashMap<Long, Timer.Context> timerContextMap = new ConcurrentHashMap<>(10000);

    private static AtomicLong timerContextCount = new AtomicLong(0);


    private Counter counter;


    @Override
    public void afterPropertiesSet() throws Exception {
        counter = getMetricRegistry().counter("mq-consumer-working-thread-counter");
    }


    @Override
    public void before(Envelope envelope) {
        if (!isMetricEnable() || null == envelope) {
            return;
        }
        String meterKey = getKey("meter", envelope);
        Meter meter = meterMap.getIfAbsentPut(meterKey, getMetricRegistry().meter(meterKey));
        meter.mark();

        String timerKey = getKey("timer", envelope);
        Timer timer = timerMap.getIfAbsentPut(timerKey, getMetricRegistry().timer(timerKey));
        Timer.Context timerContext = timer.time();

        //超过一定容量时，抛弃度量
        if (timerContextCount.getAndIncrement() < 7000) {
            timerContextMap.put(envelope.getDeliveryTag(), timerContext);
        }

        counter.inc();
    }

    @Override
    public void after(Envelope envelope) {
        if (!isMetricEnable() || null == envelope) {
            return;
        }
        Timer.Context context = timerContextMap.get(envelope.getDeliveryTag());
        if (null != context) {
            context.close();
        }
        timerContextMap.remove(envelope.getDeliveryTag());

        counter.dec();
    }

    private String getKey(String prefix, Envelope envelope) {
        return String.format("%s_%s_%s", prefix, envelope.getExchange(), envelope.getRoutingKey());
    }


}

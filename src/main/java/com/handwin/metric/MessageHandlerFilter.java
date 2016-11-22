package com.handwin.metric;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.gs.collections.impl.map.mutable.ConcurrentHashMap;
import com.handwin.packet.*;
import com.handwin.server.handler.Handler;
import io.netty.util.internal.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by piguangtao on 14/12/25.
 */
public abstract class MessageHandlerFilter<P extends BasePacket> extends DefaultMetric implements Handler<P>{

    private ConcurrentHashMap<String, Meter> meterMap = new ConcurrentHashMap<>();

    private ConcurrentHashMap<String, Timer> timerMap = new ConcurrentHashMap<>();

    private ConcurrentHashMap<String, Timer.Context> timerContextMap = new ConcurrentHashMap<>(10000);

    private static AtomicLong timerContextCount = new AtomicLong(0);

    @Override
    public void before(String traceId,P packet) {
        if (!isMetricEnable() || null == packet) {
            return;
        }
        String key = getKey(packet);
        if (null == key) return;

        Meter meter = meterMap.getIfAbsentPut(key, getMetricRegistry().meter(key));
        if (null != meter) {
            meter.mark();
        }

        if(StringUtils.isBlank(traceId)) return;

        String timerKey = "timer_"+key;
        Timer timer = timerMap.getIfAbsentPut(timerKey,getMetricRegistry().timer(timerKey));
        Timer.Context timerContext = timer.time();

        //超过一定容量时，抛弃度量
        if (timerContextCount.getAndIncrement()<7000) {
            timerContextMap.put(traceId, timerContext);
        }

    }

    @Override
    public void after(String traceId,P packet) {

        if(!isMetricEnable() || null == traceId){
            return;
        }
        Timer.Context context = timerContextMap.get(traceId);
        if (null != context) {
            context.close();
        }
        timerContextMap.remove(traceId);

    }

    private String getKey(P packet) {
        String key = null;
        if (packet instanceof TextMessagePacket) {
            key = "plain-message-meter";
        } else if (packet instanceof ImageMessagePacket) {
            key = "image-message-meter";
        } else if (packet instanceof VoiceMessagePacket) {
            key = "audio-message-meter";
        } else if (packet instanceof VideoMessagePacket) {
            key = "video-message-meter";
        } else if (packet instanceof CallPacket) {
            CallPacket callPacket = (CallPacket) packet;
            switch (callPacket.getCallStatus()) {
                case VIDEO_REQUEST:
                    key = "video-call-req-meter";
                    break;
                case AUDIO_REQUEST:
                    key = "audio-call-req-meter";
                    break;
                case VIDEO_ACCEPT:
                    key = "video-call-accept-meter";
                case AUDIO_ACCEPT:
                    key = "audio-call-accept-meter";
                    break;
                case REJECT:
                    key = "call-reject-meter";
                    break;
                case BUSY:
                    key = "call-busy-meter";
                    break;
                case HANGUP:
                case VIDEO_REQUEST_AGAIN:
                case AUDIO_REQUEST_AGAIN:
                case RECEIVED: {
                    //不统计
                    break;
                }
                default:
                    break;
            }
        }
        return key;
    }
}

package com.handwin.server.handler;

import com.google.common.collect.Maps;
import com.handwin.packet.BasePacket;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @param <P>
 * @author fangliang
 */
@Service
public class HandlerHolder<P extends BasePacket> {

    private final Map<Class<P>, Handler<P>> handlerCache = Maps.newHashMap();

    public Handler<P> getHandler(Class<P> packetClass) {
        return handlerCache.get(packetClass);
    }

    public void putHandler(Class<P> packetClass, Handler<P> handler) {
        handlerCache.put(packetClass, handler);
    }

}

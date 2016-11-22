package com.handwin.server.controller;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.handwin.protocal.v5.codec.V5GenericPacket;
import com.handwin.protocal.v5.codec.V5PacketHead;
import com.handwin.server.Channel;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Danny on 2014-12-03.
 */
@Service
public class ServiceControllerManager {

    private static final Logger logger = LoggerFactory.getLogger(ServiceControllerManager.class);

    private final Map<String, Set<ServiceController>> controllerMap = Maps.newHashMap();
    private final Map<String, Set<ServiceInterceptor>> interceptorMap = Maps.newHashMap();
    private final Set<ServiceInterceptor> allInterceptors = Sets.newLinkedHashSet();

    @Autowired
    @Qualifier("defaultServiceController")
    private ServiceController defaultServiceController;

    private Set<ServiceController> getControllers(String name) {
        name = StringUtils.lowerCase(name);
        Set<ServiceController> sw = controllerMap.get(name);

        if (sw == null) {
            return Collections.EMPTY_SET;
        }

        return sw.stream()
                .filter(w -> w != null)
                .collect(Collectors.toSet());
    }

    private Set<ServiceInterceptor> getInterceptors(String name) {
        name = StringUtils.lowerCase(name);
        Set<ServiceInterceptor> sw = interceptorMap.get(name);

        if (sw == null) {
            return Collections.EMPTY_SET;
        }

        Set<ServiceInterceptor> set = sw.stream()
                .filter(w -> w != null)
                .collect(Collectors.toSet());
        set.addAll(allInterceptors.stream()
                .filter(w -> w != null)
                .collect(Collectors.toSet()));

        return set;
    }

    public void regisgerController(String serviceName, ServiceController controller) {
        logger.info("register controller for {}", serviceName);
        serviceName = StringUtils.lowerCase(serviceName);
        Set<ServiceController> set = controllerMap.get(serviceName);
        if (set == null) {
            set = new LinkedHashSet<>();
            controllerMap.put(serviceName, set);
        }
        set.add(controller);
    }

    public void regisgerController(List<String> serviceNames, ServiceController controller) {
        if (null == serviceNames) return;
        serviceNames.forEach(service -> {
            logger.info("register controller for {}. controller:{}", service, controller.getClass().getName());
            service = StringUtils.lowerCase(service);
            Set<ServiceController> set = controllerMap.get(service);
            if (set == null) {
                set = new LinkedHashSet<>();
                controllerMap.put(service, set);
            }
            set.add(controller);
        });

    }

    public void regisgerInterceptor(String serviceName, ServiceInterceptor interceptor) {
        logger.info("register interceptor for {}", serviceName);
        if ("*".equals(serviceName)) {
            allInterceptors.add(interceptor);
        } else {
            Set<ServiceInterceptor> set = interceptorMap.get(serviceName);
            if (set == null) {
                set = new LinkedHashSet<>();
                interceptorMap.put(serviceName, set);
            }
            set.add(interceptor);
        }
    }

    public void handler(Channel channel, V5GenericPacket v5GenericPacket) {
        V5PacketHead head = v5GenericPacket.getPacketHead();
        String serviceName = StringUtils.trimToNull(head.getService());
        if (serviceName != null) {
            Set<ServiceController> controllers = getControllers(serviceName);
            Set<ServiceInterceptor> interceptors = getInterceptors(serviceName);
            for (ServiceInterceptor si : interceptors) {
                if (!si.preHanle(channel, head, v5GenericPacket)) {
                    return;
                }
            }
            if (controllers != null && controllers.size() > 0) {
                for (ServiceController c : controllers) {
                    try {
                        c.handle(channel, head, v5GenericPacket);
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            } else {
                logger.warn("no found controller for service '{}', use default service controller handle",
                        serviceName);
                defaultServiceController.handle(channel, head, v5GenericPacket);
            }
            interceptors.stream().forEach(si -> si.postHanle(channel, head, v5GenericPacket));
        } else {
            logger.error("service name is null.");
        }
    }
}

package com.handwin.server.controller;

import com.handwin.packet.GenericPacket;
import com.handwin.packet.PacketHead;
import com.handwin.protocal.v5.codec.V5GenericPacket;
import com.handwin.protocal.v5.codec.V5PacketHead;
import com.handwin.server.Channel;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.stream.Stream;

/**
 * Created by Danny on 2014-12-03.
 */
@Service
public class ServiceControllerBeanPostProcessor implements BeanPostProcessor {
    private static Logger logger = LoggerFactory.getLogger(ServiceControllerBeanPostProcessor.class);

    @Autowired
    private ServiceControllerManager serviceControllerManager;

    public static final ReflectionUtils.MethodFilter HANDLER_METHOD_FILTER = new ReflectionUtils.MethodFilter() {
        @Override
        public boolean matches(Method method) {
            boolean match = false;
            match = null != AnnotationUtils.findAnnotation(method, Controller.class);
            if (match) {
                match = false;
                Class<?>[] parameterTypes = method.getParameterTypes();
                if (parameterTypes != null
                        && parameterTypes.length > 1) {
                    match = true;
                    boolean findChannelClass = false;
                    for (Class clazz : parameterTypes) {
                        if (clazz == Channel.class) {
                            findChannelClass = true;
                            continue;
                        }
                        if (clazz != PacketHead.class
                                || clazz != GenericPacket.class
                                || clazz != Map.class
                                || clazz != byte[].class) ;
                        {
                            match = false;
                            break;
                        }
                    }
                    if (!findChannelClass) {
                        match = false;
                    }
                }
            }
            if (match) {
                logger.debug("find Controller annotation.");
            }
            return match;
        }
    };

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof ServiceController) {
            final List<String> serviceNames = new ArrayList<>();
            Controller controllerAnno = AnnotationUtils.findAnnotation(bean.getClass(), Controller.class);
            if (controllerAnno == null || null == controllerAnno.value() || controllerAnno.value().length < 1) {
                String shortName = ClassUtils.getShortName(bean.getClass());
                if (shortName.endsWith("ServiceController")) {
                    serviceNames.add(StringUtils.substringBeforeLast(shortName, "ServiceController"));
                } else if (shortName.endsWith("Controller")) {
                    serviceNames.add(StringUtils.substringBeforeLast(shortName, "Controller"));
                }
                if (null == serviceNames || serviceNames.size() < 1) {
                    serviceNames.add(shortName);
                }
            } else {
                if (!controllerAnno.disable()) {
                    Stream.of(controllerAnno.value()).forEach(serviceName -> serviceNames.add(serviceName));
                }
            }

            if (null != serviceNames && serviceNames.size() > 0) {
                serviceControllerManager.regisgerController(serviceNames, (ServiceController) bean);
                logger.info("add controller. serviceName:{},bean:{}", serviceNames, bean.getClass());
            }
        }

        if (bean instanceof ServiceInterceptor) {
            String serviceName = null;
            Interceptor interceptorAnno = AnnotationUtils.findAnnotation(bean.getClass(), Interceptor.class);
            if (interceptorAnno == null || StringUtils.isBlank(interceptorAnno.value())) {
                String shortName = ClassUtils.getShortName(bean.getClass());
                if (shortName.endsWith("ServiceInterceptor")) {
                    serviceName = StringUtils.substringBeforeLast(shortName, "ServiceInterceptor");
                } else if (shortName.endsWith("Interceptor")) {
                    serviceName = StringUtils.substringBeforeLast(shortName, "Interceptor");
                }
                if (StringUtils.isBlank(serviceName)) {
                    serviceName = shortName;
                }
            } else {
                if (interceptorAnno.disable()) {
                    serviceName = null;
                } else {
                    serviceName = interceptorAnno.value();
                }
            }

            if (StringUtils.isNotBlank(serviceName)) {
                serviceControllerManager.regisgerInterceptor(serviceName, (ServiceInterceptor) bean);
            }
        }

        Set<Method> methods = findHandlerMethods(bean.getClass(), HANDLER_METHOD_FILTER);
        addServiceController(bean, methods);

        return bean;
    }

    private void addServiceController(final Object bean, final Set<Method> methods) {
        if (methods == null || methods.isEmpty()) {
            return;
        }
        logger.debug("add Annotation service handler.");

        Controller controllerAnno;

        for (final Method method : methods) {
            controllerAnno = AnnotationUtils.findAnnotation(method, Controller.class);

            List<String> services = new ArrayList<>();
            String[] serviceNames = controllerAnno.value();
            if (serviceNames == null || serviceNames.length < 1) {
                String methodName = method.getName();
                if (methodName.endsWith("ServiceHandle")) {
                    services.add(StringUtils.substringBeforeLast(methodName, "ServiceHandle"));
                } else if (methodName.endsWith("Handle")) {
                    services.add(StringUtils.substringBeforeLast(methodName, "Handle"));
                }
                if (null == services || services.size() < 1) {
                    services.add(methodName);
                }
            }

            if (null != services && services.size() > 0) {
                ServiceController serviceController = new SimpleServiceController(bean, method);
                serviceControllerManager.regisgerController(services, serviceController);
            }
        }
    }

    protected final static class SimpleServiceController implements ServiceController {

        final private Method method;
        final private Object bean;
        final private Class<?>[] argTypes;

        SimpleServiceController(Object bean, Method method) {
            this.method = method;
            this.bean = bean;
            this.argTypes = method.getParameterTypes();
        }

        @Override
        public void handle(Channel channel, V5PacketHead packetHead, V5GenericPacket genericPacket) {
            Object[] values = new Object[argTypes.length];
            int i = 0;
            for (Class clazz : argTypes) {
                if (clazz == Channel.class) {
                    values[i] = channel;
                } else if (clazz == PacketHead.class) {
                    values[i] = packetHead;
                } else if (clazz == GenericPacket.class) {
                    values[i] = genericPacket;
                } else if (clazz == Map.class) {
                    values[i] = genericPacket.getBodyMap();
                } else if (clazz == byte[].class) {
                    values[i] = genericPacket.getBodySrcBytes();
                } else {
                    values[i] = null;
                }
                i++;
            }
            ReflectionUtils.invokeMethod(method, bean, values);
        }
    }


    public static Set<Method> findHandlerMethods(Class<?> handlerType,
                                                 final ReflectionUtils.MethodFilter handlerMethodFilter) {
        final Set<Method> handlerMethods = new LinkedHashSet<Method>();

        if (handlerType == null) {
            return handlerMethods;
        }

        Set<Class<?>> handlerTypes = new LinkedHashSet<Class<?>>();
        Class<?> specificHandlerType = null;
        if (!Proxy.isProxyClass(handlerType)) {
            handlerTypes.add(handlerType);
            specificHandlerType = handlerType;
        }
        handlerTypes.addAll(Arrays.asList(handlerType.getInterfaces()));
        for (Class<?> currentHandlerType : handlerTypes) {
            final Class<?> targetClass = (specificHandlerType != null ? specificHandlerType : currentHandlerType);
            ReflectionUtils.doWithMethods(currentHandlerType, new ReflectionUtils.MethodCallback() {
                @Override
                public void doWith(Method method) {
                    Method specificMethod = ClassUtils.getMostSpecificMethod(method, targetClass);
                    Method bridgedMethod = BridgeMethodResolver.findBridgedMethod(specificMethod);
                    if (handlerMethodFilter.matches(specificMethod) &&
                            (bridgedMethod == specificMethod || !handlerMethodFilter.matches(bridgedMethod))) {
                        handlerMethods.add(specificMethod);
                    }
                }
            }, ReflectionUtils.USER_DECLARED_METHODS);
        }
        return handlerMethods;
    }
}


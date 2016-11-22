package com.handwin.safeguard;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

/**
 * Created by piguangtao on 15/3/13.
 */
public class ExceptionCheckFilter extends Filter<ILoggingEvent> {

    String exceptions;

    /**
     * If the decision is <code>{@link ch.qos.logback.core.spi.FilterReply#DENY}</code>, then the event will be
     * dropped. If the decision is <code>{@link ch.qos.logback.core.spi.FilterReply#NEUTRAL}</code>, then the next
     * filter, if any, will be invoked. If the decision is
     * <code>{@link ch.qos.logback.core.spi.FilterReply#ACCEPT}</code> then the event will be logged without
     * consulting with other filters in the chain.
     *
     * @param event The event to decide upon.
     */
    @Override
    public FilterReply decide(ILoggingEvent event) {
        if (null == event) return FilterReply.DENY;

        if (!event.getLevel().isGreaterOrEqual(Level.WARN)) return FilterReply.DENY;

        boolean isContains = false;

        if (null != exceptions && !"".equals(exceptions)) {
            String[] exception = exceptions.split(",");
            for (int i = 0; i < exception.length; i++) {
                IThrowableProxy proxy = event.getThrowableProxy();
                if (null != proxy) {
                    String exceptionClassName = proxy.getClassName();
                    if (null != exceptionClassName && !"".equals(exceptionClassName) && exceptionClassName.contains(exception[i])) {
                        isContains = true;
                        break;
                    }
                }
            }
        }
        return isContains ? FilterReply.ACCEPT : FilterReply.DENY;

    }

    public void setExceptions(String exceptions) {
        this.exceptions = exceptions;
    }
}

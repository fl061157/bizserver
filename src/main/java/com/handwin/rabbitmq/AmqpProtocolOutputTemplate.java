package com.handwin.rabbitmq;

import com.handwin.exception.ServerException;
import com.handwin.exception.ServerException.ErrorCode;
import com.handwin.mq.RabbitTemplate;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;


/**
 * @author fangliang
 */

public class AmqpProtocolOutputTemplate implements ProtocolOutptTemplate {

    private static final Logger logger = LoggerFactory.getLogger(AmqpProtocolOutputTemplate.class);
    private Map<String, RabbitTemplate> templates = new HashMap<String, RabbitTemplate>();

    public Map<String, RabbitTemplate> getTemplates() {
        return templates;
    }

    private String defaultCountryCode;

    private String localCountryCode;


    public String getDefaultCountryCode() {
        return defaultCountryCode;
    }

    public void setDefaultCountryCode(String defaultCountryCode) {
        this.defaultCountryCode = defaultCountryCode;
    }

    public String getLocalCountryCode() {
        return localCountryCode;
    }

    public void setLocalCountryCode(String localCountryCode) {
        this.localCountryCode = localCountryCode;
    }

    public void setTemplates(Map<String, RabbitTemplate> templates) {
        this.templates = templates;
    }

    @Override
    public void send(String region, String exchange, String routeKey, byte[] message)
            throws ServerException {
        RabbitTemplate template;
        if (StringUtils.isBlank(region)) {
            region = defaultCountryCode;
        }
        template = templates.get(region);
        if (template == null) {
            logger.info("defaultCountryCode:{} ", defaultCountryCode);
            template = templates.get(defaultCountryCode);
        }

        try {
            template.write(exchange, routeKey, message);
        } catch (Throwable e) {
            logger.error("[MessagePublisher],error:{}", e);
            throw new ServerException(ErrorCode.CanNotHandleIoError);
        }
    }

    @Override
    public void send(String exchange, String routeKey, byte[] message)
            throws ServerException {
        RabbitTemplate template = templates.get(defaultCountryCode);
        try {
            template.write(exchange, routeKey, message);
        } catch (Throwable e) {
            logger.error("[MessagePublisher],error:{}", e);
            throw new ServerException(ErrorCode.CanNotHandleIoError);
        }
    }


}

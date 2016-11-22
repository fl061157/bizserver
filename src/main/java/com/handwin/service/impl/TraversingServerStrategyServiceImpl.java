package com.handwin.service.impl;

import com.handwin.config.SystemConfigBean;
import com.handwin.entity.TraversingServerQuery;
import com.handwin.entity.TraversingServerResult;
import com.handwin.utils.SystemUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.fluent.Request;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by piguangtao on 15/4/15.
 */
@Service("traversingServerStrategyServiceImpl")
public class TraversingServerStrategyServiceImpl extends DefaultStrategyServiceImpl implements InitializingBean {
    private static final Logger LOGGER = LoggerFactory.getLogger(TraversingServerStrategyServiceImpl.class);

    @Autowired
    private SystemConfigBean systemConfigBean;

    String[] ipServers;

    String traversingServerPath;

    @Autowired
    private ObjectMapper objectMapper;


    @Override
    public void afterPropertiesSet() throws Exception {
        try {
            String ipStrategyUrl = systemConfigBean.getIpStrategyUrl();
            traversingServerPath = systemConfigBean.getTraversingServerPath();
            ipServers = ipStrategyUrl.split(",");
        } catch (Exception e) {
            LOGGER.warn("fails to form configurations.", e);
        }
    }

    @Override
    public TraversingServerResult getTraversingServers(TraversingServerQuery query) {
        LOGGER.debug("[tcpServers-query].query:{}", query);
        TraversingServerResult strategyResult = null;
        if (null == query || !query.isValid()) return strategyResult;
        String url = null;
        try {
            url = String.format("%s%s?userId=%s&ip=%s&countryCode=%s&mobile=%s", SystemUtils.getRandom(ipServers), traversingServerPath, query.getUserId(), query.getIp(), query.getCountryCode(), query.getMobile());
            Request request = Request.Get(url);

            String result = request.execute().returnContent().asString();
            if (StringUtils.isNotBlank(result)) {
                TraversingServerResult.TraversingServer[] traversingServers = objectMapper.readValue(result, new TypeReference<TraversingServerResult.TraversingServer[]>() {
                });

                if (null != traversingServers) {
                    strategyResult = new TraversingServerResult();
                    strategyResult.setServers(traversingServers);
                }
                LOGGER.debug("[traversing server query.]url:{},result:{}", url, result);
            } else {
                LOGGER.warn("fails to get tcpServer.url:{}", url);
            }
        } catch (Throwable e) {
            LOGGER.error(String.format("[tcp-server-query].fails to get udp server. query:%s", url), e);
        }
        return strategyResult;
    }
}


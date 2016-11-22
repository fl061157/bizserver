package com.handwin.service.impl;

import com.handwin.config.SystemConfigBean;
import com.handwin.entity.TcpStrategyQuery;
import com.handwin.entity.TcpStrategyResult;
import com.handwin.utils.SystemUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.fluent.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by piguangtao on 15/3/21.
 */
@Service("tcpStrategyServiceImpl")
public class TcpStrategyServiceImpl extends DefaultStrategyServiceImpl implements InitializingBean {
    private static final Logger logger = LoggerFactory.getLogger(TcpStrategyServiceImpl.class);

    @Autowired
    private SystemConfigBean systemConfigBean;

    String[] ipServers;

    String tcpPath;


    @Override
    public void afterPropertiesSet() throws Exception {
        try {
            String ipStrategyUrl = systemConfigBean.getIpStrategyUrl();
            tcpPath = systemConfigBean.getIpStrategyTcpPath();
            ipServers = ipStrategyUrl.split(",");
        } catch (Exception e) {
            logger.warn("fails to form configurations.", e);
        }
    }

    @Override
    public TcpStrategyResult getTcpServers(TcpStrategyQuery query) {
        logger.debug("[tcpServers-query].query:{}", query);
        TcpStrategyResult strategyResult = null;
        if (null == query || !query.isValid()) return strategyResult;
        try {
            String url = String.format("%s%s?ip=%s&countryCode=%s",SystemUtils.getRandom(ipServers),tcpPath,query.getIp(),query.getContryCode());
            Request request = Request.Get(url);

            String result = request.execute().returnContent().asString();
            if(StringUtils.isNotBlank(result)){
                strategyResult = new TcpStrategyResult();
                strategyResult.setTcpServers(result);
            }
            else {
                logger.warn("fails to get tcpServer");
            }
            logger.debug("[tcpServers-query].query:{}.result:{}", query,result);
        } catch (Throwable e) {
            logger.error("[tcp-server-query].fails to get udp server. query:" + query, e);
        }
        return strategyResult;
    }


}

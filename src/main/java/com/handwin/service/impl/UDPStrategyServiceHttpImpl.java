package com.handwin.service.impl;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.handwin.config.SystemConfigBean;
import com.handwin.entity.P2PStrategy;
import com.handwin.entity.UdpStrategy2Result;
import com.handwin.entity.UdpStrategyQuery;
import com.handwin.metric.OperationErrorMetric;
import com.handwin.utils.SystemUtils;
import com.ryantenney.metrics.annotation.Counted;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.fluent.Request;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;


/**
 * Created by piguangtao on 14-3-7.
 */
@Service("udpStrategyServiceHttpImpl")
public class UDPStrategyServiceHttpImpl extends DefaultStrategyServiceImpl implements InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(UDPStrategyServiceHttpImpl.class);
    String ipStrategyServerUrl;
    String udpPath;
    String updIpDefault;
    short udpPortDefault;
    String udpNodeDefault;
    short p2pDefault;
    String[] ipServers;
    ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private SystemConfigBean systemConfigBean;

    @Autowired
    private OperationErrorMetric operationErrorMetric;

    public UDPStrategyServiceHttpImpl() {
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    public void afterPropertiesSet() throws Exception {
        try {
            ipStrategyServerUrl = systemConfigBean.getIpStrategyUrl();
            udpPath = systemConfigBean.getIpStrategyUdpPath();
            updIpDefault = systemConfigBean.getUdpIpDefault();
            udpPortDefault = Short.parseShort(systemConfigBean.getUdpPortDefaul());
            udpNodeDefault = systemConfigBean.getUdpNodeDefault();
            p2pDefault = Short.parseShort(systemConfigBean.getUdpP2pDefault());
            ipServers = ipStrategyServerUrl.split(",");
        } catch (Exception e) {
            logger.error("fails to form configurations");
        }
    }

    @Timed(name = "udp-strategy-timer", absolute = true)
    @Counted(name = "udp-strategy-requesting-count", absolute = true)
    public UdpStrategy2Result getUdp(final UdpStrategyQuery query) {
        logger.debug("[udp-service-query].query:{}", query);
        if (null == ipServers || ipServers.length < 1) {
            return getUdpStrategyDefault();
        }
        try {
            Request request = Request.Post(SystemUtils.getRandom(ipServers) + udpPath)
                    .bodyForm(((Supplier<List<BasicNameValuePair>>) () -> {
                        List<BasicNameValuePair> tmp = Arrays.asList(new BasicNameValuePair("ip1", query.getIp1()),
                                new BasicNameValuePair("ip2", query.getIp2()), new BasicNameValuePair("id1", "user1_udps"),
                                new BasicNameValuePair("id2", "user2_udps"), new BasicNameValuePair("appId", String.valueOf(query.getAppId())));
                        List<BasicNameValuePair> basicNameValuePairList = new ArrayList<BasicNameValuePair>();
                        basicNameValuePairList.addAll(tmp);
                        if (null != query.getNet1()) {
                            basicNameValuePairList.add(new BasicNameValuePair("net1", query.getNet1()));
                        }
                        if (null != query.getNet2()) {
                            basicNameValuePairList.add(new BasicNameValuePair("net2", query.getNet2()));
                        }
                        return basicNameValuePairList;
                    }).get(), Charset.forName("UTF-8"));

            String result = request.execute().returnContent().asString();
            if (StringUtils.isBlank(result)) {
                logger.warn("[udp-service-query].no udp server return. return default udp.");
                operationErrorMetric.getUdpStrategyErrors().inc();
                return getUdpStrategyDefault();
            } else {
                return objectMapper.readValue(result, UdpStrategy2Result.class);
            }
        } catch (Throwable e) {
            logger.error("[udp-service-query].fails to get udp server. query:" + query, e);
            operationErrorMetric.getUdpStrategyErrors().inc();
            return getUdpStrategyDefault();
        }
    }

    private UdpStrategy2Result getUdpStrategyDefault() {
        UdpStrategy2Result udpStrategyResult = new UdpStrategy2Result();
        udpStrategyResult.setP2p(P2PStrategy.ALL_P2P.getValue());

        UdpStrategy2Result.UDPInfo udpInfo = new UdpStrategy2Result.UDPInfo(updIpDefault, String.valueOf(udpPortDefault), udpNodeDefault);
        UdpStrategy2Result.UDPInfo[] udpInfos = new UdpStrategy2Result.UDPInfo[]{udpInfo};

        udpStrategyResult.setUser1Udps(udpInfos);
        udpStrategyResult.setUser2Udps(udpInfos);
        return udpStrategyResult;
    }

    public void setIpServers(String[] ipServersPar) {
        if (null == ipServers || !Arrays.equals(ipServers, ipServersPar)) {
            ipServers = ipServersPar;
        }
    }

    public void setDefaultUdp(String updIpDefaultPar, short udpPortDefaultPar, String udpNodeDefaultPar) {
        if (null != updIpDefaultPar && !updIpDefaultPar.equals(updIpDefault)) {
            updIpDefault = updIpDefaultPar;
        }

        if (udpPortDefaultPar != udpPortDefault) {
            udpPortDefault = udpPortDefaultPar;
        }

        if (null != udpNodeDefaultPar && udpNodeDefaultPar.equals(udpNodeDefault)) {
            udpNodeDefault = udpNodeDefaultPar;
        }
    }

    public void setIpStrategyServerUrl(String ipStrategyServerUrl) {
        this.ipStrategyServerUrl = ipStrategyServerUrl;
    }

    public void setUdpPath(String udpPath) {
        this.udpPath = udpPath;
    }


}

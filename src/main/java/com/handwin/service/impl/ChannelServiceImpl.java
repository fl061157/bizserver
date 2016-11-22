package com.handwin.service.impl;

import com.handwin.bean.Platform;
import com.handwin.entity.ChannelInfo;
import com.handwin.entity.User;
import com.handwin.exception.ServerException;
import com.handwin.server.Channel;
import com.handwin.server.proto.BaseRequestMessage;
import com.handwin.service.ChannelService;
import com.handwin.service.TcpSessionService;
import com.handwin.service.UserService;
import com.handwin.server.ChannelStrategy;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author fangliang
 */
@Service
public class ChannelServiceImpl implements ChannelService {

    private static final Logger logger = LoggerFactory.getLogger(ChannelServiceImpl.class);

    @Autowired
    private TcpSessionService onlineStatusService;

    @Autowired
    private UserService userService;

//    @Autowired
//    private ChannelStrategyService channelStrategyService;

    @Autowired
    private ChannelFactory channelFactory;


    @Override
    public Channel buildChannel(BaseRequestMessage baseRequest) {
        //ChannelStrategy channelStrategy = channelStrategyService.getChannelStrategy();

        ChannelStrategy channelStrategy = ChannelStrategy.MrChannel;
        return channelFactory.createChannel(channelStrategy, baseRequest);
    }

    @Override
    public Channel findChannel(User user) throws ServerException {
        if (user == null) {
            return null;
        }
        boolean isSystemAccount = userService.isSystemAccount(user);

        ChannelInfo channelInfo = onlineStatusService.getChannelInfo(user.getId(), user.getAppId(), isSystemAccount);
        logger.debug("[channel] userId:{},appId:{},channelInfo:{}", user.getId(), user.getAppId(), channelInfo);
        if (channelInfo == null) {
            return null;
        }
        //ChannelStrategy channelStrategy = channelStrategyService.getChannelStrategy();

        ChannelStrategy channelStrategy = ChannelStrategy.MrChannel;

        if (logger.isDebugEnabled()) {
            logger.debug("ChannelStrategy:{}", channelStrategy.toString());
        }
        return channelFactory.createChannel(channelStrategy, user, channelInfo);
    }

    @Override
    public Map<Platform, List<Channel>> findChannelMap(User user) throws ServerException {

        if (user == null) return null;

        Map<Platform, List<ChannelInfo>> infoMap = onlineStatusService.findChannelInfoMap(user.getId(), user.getAppId());

        if (MapUtils.isEmpty(infoMap)) return null;

        ChannelStrategy channelStrategy = ChannelStrategy.MrChannel;

        Map<Platform, List<Channel>> channelMap = new HashedMap();

        for (Map.Entry<Platform, List<ChannelInfo>> entry : infoMap.entrySet()) {

            Platform platform = entry.getKey();
            List<ChannelInfo> channelInfoList = entry.getValue();

            List<Channel> channelList = channelInfoList.stream().map(cInfo -> channelFactory.createChannel(channelStrategy, user, cInfo)).collect(Collectors.toList());

            channelMap.put(platform, channelList);

        }

        return channelMap;
    }

    @Override
    public String findUserID(Channel channel) {

        String userID = null;
        if (channel.getChannelInfo() != null) {
            userID = channel.getChannelInfo().getUserId();
            if (StringUtils.isBlank(userID)) {
                //TODO 暂时 ChannelUUID Can't find UserID
            }
        }
        return userID;
    }
}

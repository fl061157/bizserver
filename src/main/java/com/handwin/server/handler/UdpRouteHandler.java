package com.handwin.server.handler;

import com.alibaba.fastjson.JSON;
import com.handwin.entity.*;
import com.handwin.packet.*;
import com.handwin.packet.v5.V5SimpleMessagepacket;
import com.handwin.server.Channel;
import com.handwin.server.ProxyMessageSender;
import com.handwin.service.ChannelService;
import com.handwin.service.IUDPAssignService;
import com.handwin.utils.UserUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import static java.lang.String.valueOf;

/**
 * Created by fangliang on 16/6/1.
 */
@Service
public class UdpRouteHandler extends AbstractHandler<UdpRoutePacket> implements InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(UdpRouteHandler.class);

    @Autowired
    private TaskExecutor executorInterBizServers;

    @Autowired
    private ChannelService channelService;

    @Autowired
    protected IUDPAssignService udpAssignService;

    @Override
    public void afterPropertiesSet() throws Exception {
        register(UdpRoutePacket.class);
    }

    @Override
    public void handle(Channel channel, UdpRoutePacket packet) {

        int command = packet.getCommonad();
        String toUserID = packet.getUserID();
        String detectedID = packet.getDetectID();

        if (logger.isInfoEnabled()) {
            logger.info("[UdpRouteHandler] command:{} , toUserID:{} , detectedID:{} ", command, toUserID, detectedID);
        }

        final String from = channel.getChannelInfo().getUserId();
        User fromUser = null;

        if (StringUtils.isNotBlank(from)) {
            fromUser = userService.findById(from, channel.getChannelInfo().getAppID());
        }

        if (fromUser == null) {
            logger.warn("findFromUser empty:{} ", from);
            return;
        }


        User toUser;
        try {
            toUser = userService.findById(toUserID, channel.getChannelInfo().getAppID());
        } catch (Exception e) {
            logger.error("findToUser error", e.getCause());
            return;
        }

        if (toUserMsgCanHandleThisIDC(toUser)) {

            Channel toChannel = channelService.findChannel(toUser);
            if (toChannel != null) {
                if (command == 2) {

                    ChannelInfo toChannelInfo = toChannel.getChannelInfo();

                    UDPServerPacket2 udpSeverPacket = getUdpServerPacket(channel.getChannelInfo(), packet, toChannelInfo, fromUser, toUser);

                    UDPServerPacket usp1 = udpSeverPacket.getUdpServerPacket1();
                    UDPServerPacket usp2 = udpSeverPacket.getUdpServerPacket2();

                    UDPServerPacket.UdpInfo[] ui1;
                    UDPServerPacket.UdpInfo[] ui2;

                    usp1.getUdpInfo();

                    if (usp1 == null || usp2 == null || (ui1 = usp1.getUdpInfo()) == null || ui1.length == 0 ||
                            (ui2 = usp2.getUdpInfo()) == null || ui2.length == 0) {
                        logger.error("[UdpRouteHandler] getUdpServer Failure from:{} , to:{} , detectedID:{} ", fromUser.getId(), toUser.getId(), detectedID);
                        return;
                    }
                    UdpRoutePacket packet2From = copy(packet);
                    String ui1s = JSON.toJSONString(ui1);
                    packet2From.setExtraData(ui1s);
                    packet2From.setCommonad(4);
                    channel.write(packet2From);

                    UdpRoutePacket packet2To = copy(packet);
                    String ui2s = JSON.toJSONString(ui2);
                    packet2To.setExtraData(ui2s);
                    packet2To.setCommonad( 4 );
                    toChannel.write(packet2To);

                } else {
                    packet.setUserID(fromUser.getId());
                    toChannel.write(packet);
                }
            }
        } else {
            sendMsgToOtherBizServer(proxyMessageSender, toUser.getCountrycode(), packet);
        }


    }


    private boolean toUserMsgCanHandleThisIDC(User toUser) {
        return userService.isSystemAccount(toUser) || userService.isLocalUser(toUser.getCountrycode());
    }

    private void sendMsgToOtherBizServer(ProxyMessageSender sender, String countryCode, BasePacket packet) {
        executorInterBizServers.execute(() -> {
            if (packet instanceof V5SimpleMessagepacket) {
                sender.writeV5Protocol(countryCode, packet.getSrcMsgBytes());
            } else {
                sender.write(countryCode, packet);
            }
        });
    }


    public boolean isForward(User fromUser) {
        String sourceRegion = fromUser.getCountrycode();

        boolean isForward =
                !(StringUtils.isBlank(sourceRegion) ||
                        userService.isLocalUser(sourceRegion));
        return isForward;
    }


    private UDPServerPacket2 getUdpServerPacket(final ChannelInfo fromChannelInfo, final UdpRoutePacket packet, final ChannelInfo toChannelInfo,
                                                final User fromUser, final User toUser) {
        UDPServerPacket2 udpSeverPacket = null;
        String detectedID = packet.getDetectID();

        if (null == udpSeverPacket) {
            boolean isSystemAccount = userService.isSystemAccount(fromUser);
            try {
                udpSeverPacket = formUdpPacket(isSystemAccount, fromChannelInfo.getIp(), toChannelInfo.getIp(),
                        packet, UserUtils.getAppUserId(fromUser.getId(), valueOf(fromUser.getAppId())),
                        UserUtils.getAppUserId(toUser.getId(), valueOf(toUser.getAppId())));
            } catch (Throwable e) {
                logger.error("[UdpRouteHandler received],error:" + e.getMessage(), e);
            }
        } else {
            logger.debug("get udp server from cache. roomId:{},udpServerPacket:{}", detectedID, udpSeverPacket);
        }

        return udpSeverPacket;

    }

    private UDPServerPacket2 formUdpPacket(final boolean isSystemAccount, final String fromeIp, final String toIp,
                                           final UdpRoutePacket packet, String fromUserNameMd5, String toUserNameMd5) {

        return formUdpPacketByIpStrategy(isSystemAccount, fromeIp, toIp, packet.getDetectID(),
                fromUserNameMd5, toUserNameMd5);
    }


    private UDPServerPacket2 formUdpPacketByIpStrategy(final boolean isSystemAccount, final String fromeIp,
                                                       final String toIp, String detectedID,
                                                       String fromUserNameMd5, String toUserNameMd5) {
        logger.debug("[udpResponsePacket].ip service. detectedID:{}", detectedID);
        UdpStrategyQuery udpStrategyQuery = new UdpStrategyQuery();
        udpStrategyQuery.setIp1(fromeIp);
        udpStrategyQuery.setIp2(toIp);
        if (isSystemAccount) {
            udpStrategyQuery.setNet1("-1");
            udpStrategyQuery.setNet2("-1");
        }
        try {
            UdpStrategy2Result udpStrategyResult = udpAssignService.getUdpStrategy(udpStrategyQuery);

            UDPServerPacket2 udpServerPacket2 = new UDPServerPacket2();

            UDPServerPacket packet1 = new UDPServerPacket();
            packet1.setFlag(udpStrategyResult.getP2p());
            packet1.setRoomId(detectedID);
            packet1.setUdpInfo(transUdpInfos(udpStrategyResult.getUser1Udps()));

            udpServerPacket2.setId1(fromUserNameMd5);
            udpServerPacket2.setUdpServerPacket1(packet1);


            UDPServerPacket packet2 = new UDPServerPacket();
            packet2.setFlag(udpStrategyResult.getP2p());
            packet2.setRoomId(detectedID);
            packet2.setUdpInfo(transUdpInfos(udpStrategyResult.getUser2Udps()));

            udpServerPacket2.setId2(toUserNameMd5);
            udpServerPacket2.setUdpServerPacket2(packet2);

            return udpServerPacket2;
        } catch (Exception e) {
            logger.error("[CallHandler received]udpResponsePacket .fails from ip service. roomId:{}.", detectedID);
            return null;
        }
    }

    private UDPServerPacket.UdpInfo[] transUdpInfos(UdpStrategy2Result.UDPInfo[] userstrategyUdpInfos) {
        UDPServerPacket.UdpInfo[] result = null;
        if (null != userstrategyUdpInfos && userstrategyUdpInfos.length > 0) {
            result = new UDPServerPacket.UdpInfo[userstrategyUdpInfos.length];
            for (int i = 0; i < userstrategyUdpInfos.length; i++) {
                UdpStrategy2Result.UDPInfo udpInfo = userstrategyUdpInfos[i];
                UDPServerPacket.UdpInfo serverPacketUdpInfo = new UDPServerPacket.UdpInfo();
                serverPacketUdpInfo.setIp(udpInfo.getUdpHost());
                serverPacketUdpInfo.setPort(Integer.valueOf(udpInfo.getUdpPort()));
                serverPacketUdpInfo.setNodeId(udpInfo.getNodeId());
                result[i] = serverPacketUdpInfo;
            }
        }
        return result;
    }


    private UdpRoutePacket copy(UdpRoutePacket packet) {
        UdpRoutePacket routePacket = new UdpRoutePacket();
        routePacket.setPacketHead(packet.getPacketHead());
        routePacket.setCommandType(packet.getCommandType());
        routePacket.setDetectID(packet.getDetectID());
        routePacket.setUserID(packet.getUserID());
        routePacket.setPacketType(packet.getPacketType());
        routePacket.setCommonad(packet.getCommonad());
        routePacket.setTraceId(packet.getTraceId());
        return routePacket;

    }


}

package com.handwin.service;

import com.handwin.entity.P2PStrategy;
import com.handwin.entity.TraversingServerQuery;
import com.handwin.entity.TraversingServerResult;
import com.handwin.entity.User;
import com.handwin.exception.TraversingServerNotFoundException;
import com.handwin.packet.CallPacket;
import com.handwin.packet.CallStatus;
import com.handwin.packet.UDPServerPacket;
import com.handwin.server.Channel;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by piguangtao on 15/3/26.
 */
@Service
public class LiveService {
    private static final Logger LOGGER = LoggerFactory.getLogger(LiveService.class);

    @Value("${direct_enable}")
    private String enableDirect;

    @Value("${test_enable}")
    private String enableTest;

    @Value("${test_user}")
    private String testUser;

    @Autowired
    protected UserService userService;

    @Autowired
    private CallService callService;

    @Autowired
    @Qualifier("traversingServerStrategyServiceImpl")
    private IIpStrategyService strategyService;

    public boolean isDirectToLive(final User fromUser, final User toUser, final CallPacket packet) {
        if (null == fromUser || null == toUser || null == packet) return Boolean.FALSE;


        if (!userService.isSystemAccount(toUser)) {
            return Boolean.FALSE;
        }

        if (!"yes".equalsIgnoreCase(enableDirect)) {
            return Boolean.FALSE;
        }

        CallStatus callStatus = packet.getCallStatus();
        if (callStatus != CallStatus.VIDEO_REQUEST) {
            return Boolean.FALSE;
        }

        boolean isDirect = Boolean.FALSE;

        if ("yes".equalsIgnoreCase(enableTest) && StringUtils.isNotBlank(testUser)) {
            if (testUser.contains(fromUser.getId())) {
                isDirect = Boolean.TRUE;
            }
        } else {
            isDirect = Boolean.TRUE;
        }


        return isDirect;
    }

    public void handleDirectToLive(final Channel channel, final User user, final CallPacket packet) throws TraversingServerNotFoundException {
        if (null == channel || null == packet) return;

        String roomId = packet.getRoomId();
        if (StringUtils.isBlank(roomId)) {
            roomId = callService.createCallRoomId();
            packet.setRoomId(roomId);
        }

        //回复UDP Server (live server)
        UDPServerPacket udpServerPacket = getLiveServer(user, channel.getIp(), roomId);

        if (null == udpServerPacket) {
            throw new TraversingServerNotFoundException("no available traversing server.");
        }

        //server received
        channel.write(callService.buildCallResponsePacket(packet.getPeerName(),
                CallStatus.CALL_SERVER_RECEIVED, null, packet, false));

        //回复消息已经接受到
        channel.write(callService.buildCallResponsePacket(packet.getPeerName(),
                CallStatus.RECEIVED, null, packet, false));


        if (null != udpServerPacket) {
            channel.write(udpServerPacket);
        } else {
            LOGGER.warn("has no live server packet.");
        }

        //回复接听
        channel.write(callService.buildCallResponsePacket(packet.getPeerName(), CallStatus.VIDEO_REQUEST == packet.getCallStatus() ? CallStatus.VIDEO_ACCEPT : CallStatus.AUDIO_ACCEPT,
                null, packet, false));

    }


    protected UDPServerPacket getLiveServer(User user, String ip, String roomId) throws TraversingServerNotFoundException {
        UDPServerPacket udpServerPacket = null;
        TraversingServerQuery query = new TraversingServerQuery();
        query.setUserId(user.getId());
        query.setMobile(user.getMobile());
        query.setCountryCode(user.getCountrycode());
        query.setIp(ip);
        TraversingServerResult result = strategyService.getTraversingServers(query);
        if (null != result) {
            List<UDPServerPacket.UdpInfo> udpInfos = new ArrayList<>();

            TraversingServerResult.TraversingServer[] traversingServers = result.getServers();

            if (null != traversingServers && traversingServers.length > 0) {
                for (TraversingServerResult.TraversingServer server : traversingServers) {
                    UDPServerPacket.UdpInfo udpInfo = new UDPServerPacket.UdpInfo();
                    udpInfo.setIp(server.getIp());
                    udpInfo.setPort(server.getPort());
                    udpInfo.setNodeId(server.getId());
                    udpInfos.add(udpInfo);
                }
            }
            if (udpInfos.size() > 0) {
                udpServerPacket = new UDPServerPacket();
                udpServerPacket.setUdpInfo(udpInfos.toArray(new UDPServerPacket.UdpInfo[udpInfos.size()]));
                udpServerPacket.setRoomId(roomId);
                udpServerPacket.setFlag(P2PStrategy.ALL_NO_P2P.getValue());
            }
        }
        return udpServerPacket;
    }
}

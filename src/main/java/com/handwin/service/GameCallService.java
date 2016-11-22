package com.handwin.service;

import com.google.common.collect.Maps;
import com.handwin.bean.Platform;
import com.handwin.entity.P2PStrategy;
import com.handwin.entity.User;
import com.handwin.packet.*;
import com.handwin.server.Channel;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by wyang on 2014/8/18.
 */
@Service
public class GameCallService extends CallService implements InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(GameCallService.class);

    @Value("${game.server.map}")
    private String gameServerConfigs;
    private ConcurrentHashMap<Integer, String> gameIdServerMap = new ConcurrentHashMap<Integer, String>();

    public void afterPropertiesSet() throws Exception {
        initGameServerInfo(gameServerConfigs);
    }

    protected void initGameServerInfo(String gameServerConfigs) {
        if (null != gameServerConfigs) {
            String[] gameServers = gameServerConfigs.split(",");
            for (String gameServer : gameServers) {
                String[] gameIdServer = gameServer.split("_");
                gameIdServerMap.put(Integer.valueOf(gameIdServer[0]), gameIdServer[1].trim());
            }
        }
    }

    @Override
    public CallPacket buildCallResponsePacket(String peerName, CallStatus callStatus, Integer status,
                                              CallPacket callPacket, boolean includeUserData) {
        GameCallRespPacket respPacket = new GameCallRespPacket();
        PacketHead packetHead = new PacketHead();
        packetHead.setVersion((byte) 0x04);
        respPacket.setPacketHead(packetHead);
        respPacket.setCallStatus(callStatus);
        respPacket.setPeerName(peerName);
        respPacket.setSsrc(((GameCallReqPacket) callPacket).getSsrc());
        respPacket.setSubCallTypes(((GameCallReqPacket) callPacket).getSubCallTypes());
        respPacket.setGameRooms(((GameCallReqPacket) callPacket).getGameRooms());
        respPacket.setExtraData(((GameCallReqPacket) callPacket).getExtraData());
        return respPacket;
    }

    @Override
    public void notifyOnAccept(final Channel fromChannel, final CallPacket packet, final Map<Platform, List<Channel>> toChannelMap,
                               User fromUser, User toUser) {

        if (MapUtils.isNotEmpty(toChannelMap)) {

            toChannelMap.values().stream().forEach( channels -> channels.stream().forEach( toChannel ->
                    toChannel.write(buildCallResponsePacket(fromUser.getId(), packet.getCallStatus(),
                            packet.getStatus(), packet, true))
            ) );

        }

        //客户端不再需要gameServer
//        GameServerRespPacket resp = buildGameServerInfo((GameCallPacket)packet);
//        if (resp != null) {
//            logger.debug("notify both size game server info {}", resp);
//            toChannel.write(resp);
//            fromChannel.write(resp);
//        }
    }

//    private GameServerRespPacket buildGameServerInfo(final GameCallPacket packet) {
//        Integer[] subCallTypes = packet.getSubCallTypes();
//        Map<Integer, Integer> gameIds = Maps.newHashMap();
//        int i = 0;
//        for (Integer subCallType : subCallTypes) {
//            switch (subCallType) {
//                case (int) GameCallPacket.SUB_CALL_TYPE_VIDEO:
//                case (int) GameCallPacket.SUB_CALL_TYPE_AUDIO:
//                    //视频呼叫呼叫不处理
//                    break;
//                default: {
//                    gameIds.put(subCallType, i);
//                    break;
//                }
//            }
//            i++;
//        }
//
//        GameServerRespPacket respPacket = null;
//        if (gameIds.size() > 0) {
//            List<GameServerRespPacket.GameServerInfo> gameServerInfos = new ArrayList<GameServerRespPacket.GameServerInfo>();
//            for (Map.Entry<Integer, Integer> gameId : gameIds.entrySet()) {
//                String gameServer = gameIdServerMap.get(gameId.getKey());
//                if (null != gameServer) {
//                    String[] gameServerIpPort = gameServer.split(":");
//                    GameServerRespPacket.GameServerInfo gameServerInfo = new GameServerRespPacket.GameServerInfo();
//                    gameServerInfo.setGameId(gameId.getKey());
//                    gameServerInfo.setIp(gameServerIpPort[0]);
//                    gameServerInfo.setPort(Short.valueOf(gameServerIpPort[1]));
//                    gameServerInfo.setFlag(P2PStrategy.ALL_NO_P2P.getValue());
//                    if (packet.getGameRooms() != null) {
//                        gameServerInfo.setRoomId((packet.getGameRooms())[gameId.getValue()].getRoomId());
//                    }
//                    gameServerInfos.add(gameServerInfo);
//                }
//            }
//
//            if (gameServerInfos.size() > 0) {
//                respPacket = new GameServerRespPacket();
//                PacketHead head = new PacketHead();
//                head.setVersion((byte) 0x04);
//                respPacket.setPacketHead(head);
//                respPacket.setGameServerInfos(gameServerInfos.toArray(new GameServerRespPacket.GameServerInfo[]{}));
//            } else {
//                logger.error("[game server info].no corrent game server.gameId:{}",
//                        Arrays.toString(gameIds.keySet().toArray()));
//            }
//        } else {
//            logger.warn("no game ids for packet {}", packet);
//        }
//        return respPacket;
//    }
}

package com.handwin.codec;

import com.handwin.packet.*;
import com.handwin.utils.ByteBufUtils;
import com.handwin.utils.IPUtils;
import com.handwin.utils.UserUtils;
import io.netty.buffer.ByteBuf;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

@Component
public class CommandDecoderAndEncoder extends BasePacketDecodeAndEncoder<CommandPacket> implements InitializingBean {

    private static Logger logger = LoggerFactory.getLogger(CommandDecoderAndEncoder.class);

    @Autowired
    private PacketCodecs packetCodecs;

    public void afterPropertiesSet() throws Exception {
        register();
    }

    public CommandDecoderAndEncoder() {
    }

    @Override
    public void register() {
        packetCodecs.register((int) CommandPacket.COMMAND_PACKET_TYPE, this);
    }

    @Override
    public CommandPacket decode(ByteBuf buf, PacketHead head) {
        CommandPacket result = null;
        logger.debug("command packet decode for type {}, packet size {}.",
                head.getPacketType(), head.getPacketSize());
        byte commandType = buf.readByte();
        switch (commandType) {
            case LoginPacket.COMMAND_LOGIN_TYPE: {
                result = decodeLoginPacket(head, buf);   //TODO 所有的 PacketDecode 走自己的
                break;
            }
            case LoginResponsePacket.COMMAND_LOGIN_RESPONSE_TYPE: {
                result = decodeLoginResponsePacket(head, buf);
                break;
            }
            case HeartbeatPacket.COMMAND_HEARTBEAT_TYPE: {
                result = decodeHeartbeatPacket(head, buf);
                break;
            }
            case HeartbeatResponsePacket.COMMAND_HEARTBEAT_RESPONSE_TYPE: {
                result = decodeHeartbeatResponsePacket(head, buf);
                break;
            }
            case LogoutPacket.COMMAND_LOGOUT_TYPE: {
                result = decodeLogoutPacket(head, buf);
                break;
            }
            case CallPacket.COMMAND_CALL_TYPE: {
                result = decodeCallPacket(head, buf);
                break;
            }
            case CallResponsePacket.COMMAND_CALL_RESPONSE_TYPE: {
                result = decodeCallResponsePacket(head, buf);
                break;
            }

            case GameCallPacket.COMMAND_GAME_CALL_TYPE: {
                logger.debug("begin to decode game call rep packet.");
                result = decodeGameCall(buf, head);
                break;
            }

            case UdpServerAckPacket.COMMAND_UDPSERVER_ACK_TYPE: {
                logger.debug("begin to decode game call rep packet.");
                result = decodeUdpServerAckPacket(buf, head);
            }


            case UdpRoutePacket.COMMAND_UDP_ROUTE_TYPE: {

                if (logger.isInfoEnabled()) {
                    logger.info("Begin to Decode UdpRoutePacket... ");
                }
                result = decodeUdpRoutePacket(buf, head);
            }


            default: {
                logger.error("unknown command packet, command type {}", commandType);
                break;
            }
        }

        if (null != result) result.setPacketHead(head);
        return result;
    }


    private CallResponsePacket decodeCallResponsePacket(PacketHead head, ByteBuf buf) {
        CallResponsePacket callResponsePacket = new CallResponsePacket();
        callResponsePacket.setCommandType((byte) CallResponsePacket.COMMAND_CALL_RESPONSE_TYPE);
        if (!buf.isReadable()) {
            return callResponsePacket;
        }
        callResponsePacket.setCallStatus(CallStatus.getInstance(buf.readByte()));
        if (!buf.isReadable(32)) {
            return callResponsePacket;
        }
        callResponsePacket.setPeerName(ByteBufUtils.readUTF8String(buf, 32));
        return callResponsePacket;
    }


    private CallPacket decodeCallPacket(PacketHead head, ByteBuf buf) {
        CallPacket callPacket = new CallPacket();
        callPacket.setCommandType((byte) CallPacket.COMMAND_CALL_TYPE);
        if (!buf.isReadable()) {
            return callPacket;
        }
        callPacket.setCallStatus(CallStatus.getInstance(buf.readByte()));

        if (!buf.isReadable(32)) {
            return callPacket;
        }
//        callPacket.setPeerName(ByteBufUtils.readUTF8String(buf, 32));

        callPacket.setPeerName(ByteBufUtils.readNewUTF8String(buf, 32));

        if (buf.isReadable(SIZE_SHORT)) {
            int statusLength = buf.readUnsignedShort();
            Integer status = null;
            if (1 == statusLength) {
                if (!buf.isReadable()) {
                    return callPacket;
                }
                status = Integer.valueOf(buf.readByte());
            } else if (0 != statusLength) {
                //丢掉，暂不支持
                if (!buf.isReadable(statusLength)) {
                    return callPacket;
                }
                buf.readBytes(statusLength);
            }
            callPacket.setStatus(status);
        }

        if (buf.isReadable(SIZE_SHORT)) {
            int roomIdLength = buf.readUnsignedShort();
            String roomId = null;
            if (roomIdLength > 0) {
                if (buf.isReadable(roomIdLength)) {
                    //客户端 老版本兼容 老版本客户端没有携带有效的roomId时，此时roomId是数字0
                    byte roomIdPrefix = buf.getByte(buf.readerIndex());
                    if (isValidRoomId(roomIdPrefix)) {
                        roomId = ByteBufUtils.readUTF8String(buf, roomIdLength);
                    } else {
                        logger.debug("invalid roomId");
                        //忽略无效的roomId
                        ByteBufUtils.readUTF8String(buf, roomIdLength);
                    }
                } else {
                    logger.error("call packet data error.");
                    return callPacket;
                }
            }
            if (roomId != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("[CallPacket] roomId:{} ", roomId);
                }
                callPacket.setRoomId(roomId);
            }
        }

        if (buf.isReadable(SIZE_SHORT)) {
            int userDataLen = buf.readUnsignedShort();
            if (userDataLen > 0) {
                if (buf.isReadable(userDataLen)) {
                    byte[] userDataBytes = ByteBufUtils.readByteArray(buf, userDataLen);
                    callPacket.setUserData(userDataBytes);

                    logger.info("[CallPacket] UserData:{} , roomId:{} , CallStatus:{} ", new String(userDataBytes), callPacket.getRoomId(), callPacket.getCallStatus().name());

                } else {
                    logger.error("call packet data error.");
                    return callPacket;
                }
            }
        }

        return callPacket;
    }

    private LogoutPacket decodeLogoutPacket(PacketHead head, ByteBuf buf) {
        LogoutPacket logoutPacket = new LogoutPacket();
        logoutPacket.setCommandType((byte) LogoutPacket.COMMAND_LOGOUT_TYPE);
        if (buf.isReadable()) {
            logoutPacket.setChannelMode(ChannelMode.getInstance(buf.readByte()));
        }
        return logoutPacket;
    }

    private HeartbeatResponsePacket decodeHeartbeatResponsePacket(PacketHead head, ByteBuf buf) {
        HeartbeatResponsePacket heartbeatResponsePacket = new HeartbeatResponsePacket();
        heartbeatResponsePacket.setCommandType((byte) HeartbeatResponsePacket.COMMAND_HEARTBEAT_RESPONSE_TYPE);
        if (buf.isReadable(SIZE_LONG)) {
            heartbeatResponsePacket.setHeartBeatSentTime(buf.readLong());
        }
        return heartbeatResponsePacket;
    }

    private HeartbeatPacket decodeHeartbeatPacket(PacketHead head, ByteBuf buf) {
        HeartbeatPacket heartbeatPacket = new HeartbeatPacket();
        heartbeatPacket.setCommandType((byte) HeartbeatPacket.COMMAND_HEARTBEAT_TYPE);
        if (buf.isReadable()) {
            heartbeatPacket.setChannelMode(ChannelMode.getInstance(buf.readByte()));
        }
        return heartbeatPacket;
    }

    private LoginResponsePacket decodeLoginResponsePacket(PacketHead head, ByteBuf buf) {
        LoginResponsePacket loginResponsePacket = new LoginResponsePacket();
        loginResponsePacket.setCommandType((byte) LoginResponsePacket.COMMAND_LOGIN_RESPONSE_TYPE);
        if (buf.isReadable()) {
            loginResponsePacket.setLoginStatus(LoginStatus.getInstance(buf.readByte()));
        }
        return loginResponsePacket;
    }


    private LoginPacket decodeLoginPacket(PacketHead head, ByteBuf buf) {
        LoginPacket loginPacket = new LoginPacket();
        loginPacket.setCommandType((byte) LoginPacket.COMMAND_LOGIN_TYPE);
        loginPacket.setSessionId(ByteBufUtils.readUTF8String(buf, 32));
        loginPacket.setPacketHead(head);

        if (!buf.isReadable(SIZE_SHORT)) {
            return loginPacket;
        }
        int languageLen = buf.readUnsignedShort();

        if (languageLen > 0) {
            if (buf.isReadable(languageLen)) {
                loginPacket.setLanguage(ByteBufUtils.readUTF8String(buf, languageLen));
            } else {
                logger.error("login packet data error.");
                return loginPacket;
            }
        }

        if (!buf.isReadable(SIZE_SHORT)) {
            return loginPacket;
        }
        int timeZoneLen = buf.readUnsignedShort();

        if (timeZoneLen > 0) {
            if (buf.isReadable(timeZoneLen)) {
                loginPacket.setTimeZone(ByteBufUtils.readUTF8String(buf, timeZoneLen));
            } else {
                logger.error("login packet data error.");
                return loginPacket;
            }
        }

        if (!buf.isReadable(SIZE_SHORT)) {
            return loginPacket;
        }
        int regionCodeLen = buf.readUnsignedShort();

        if (regionCodeLen > 0) {
            if (buf.isReadable(regionCodeLen)) {
                loginPacket.setRegionCode(ByteBufUtils.readUTF8String(buf, regionCodeLen));
            } else {
                logger.error("login packet data error.");
                return loginPacket;
            }
        }

        if (buf.isReadable()) {
            loginPacket.setChannelMode(ChannelMode.getInstance(buf.readByte()));
        }

        while (true) {
            if (buf.isReadable()) {
                int keyLen = buf.readUnsignedShort();
                String key = ByteBufUtils.readUTF8String(buf, keyLen);
                int valueLen = buf.readUnsignedShort();
                byte[] value = new byte[valueLen];
                buf.readBytes(value);
                loginPacket.addAttr(key, value);
            } else {
                break;
            }
        }
        logger.debug("login session id {}, packet:{}", loginPacket.getSessionId(), loginPacket);

        return loginPacket;
    }

    private GameCallReqPacket decodeGameCall(ByteBuf buf, PacketHead head) {
        if (!buf.isReadable()) {
            return null;
        }
        CallStatus callType = CallStatus.getInstance(buf.readByte());
        GameCallReqPacket reqPacket = new GameCallReqPacket();
        reqPacket.setPacketHead(head);
        reqPacket.setCallStatus(callType);
        if (!buf.isReadable(32)) {
            return null;
        }
        //reqPacket.setPeerName(ByteBufUtils.readUTF8String(buf, 32));


        reqPacket.setPeerName(ByteBufUtils.readNewUTF8String( buf , 32 ));

        if (!buf.isReadable(2)) {
            return reqPacket;
        }
        byte[] ssrc = new byte[2];
        buf.readBytes(ssrc);
        reqPacket.setSsrc(ssrc);

        if (!buf.isReadable(SIZE_SHORT)) {
            return reqPacket;
        }
        short subCallTypeLength = (short) buf.readUnsignedShort();

        if (subCallTypeLength > 0) {
            boolean jump = false;
            Integer[] subCallTypes = new Integer[subCallTypeLength / 4];
            for (int i = 0; i < subCallTypes.length; i++) {
                if (!buf.isReadable(SIZE_INT)) {
                    jump = true;
                    break;
                }
                subCallTypes[i] = buf.readInt();
            }
            reqPacket.setSubCallTypes(subCallTypes);
            if (jump) {
                return reqPacket;
            }
        }

        if (!buf.isReadable(SIZE_SHORT)) {
            return reqPacket;
        }
        int dataLength = buf.readUnsignedShort();

        if (dataLength > 0) {
            List<GameCallPacket.GameRoom> gameRoomList = new ArrayList<GameCallPacket.GameRoom>();
            if (!buf.isReadable(SIZE_SHORT)) {
                return reqPacket;
            }
            int dataLength1 = buf.readUnsignedShort();
            int dataRemainLength = dataLength - 2;
            for (int readNum = 0; readNum < dataLength1; ) {
                int roomLength = buf.readUnsignedShort();
                int subCallType = buf.readInt();
                //客户端 老版本兼容 老版本客户端没有携带有效的roomId时，此时roomId是数字0
                byte roomIdPrefix = buf.getByte(buf.readerIndex());
                String roomId = null;
                if (isValidRoomId(roomIdPrefix)) {
                    roomId = ByteBufUtils.readUTF8String(buf, roomLength);
                } else {
                    logger.debug("game call has no valid roomId");
                    ByteBufUtils.readUTF8String(buf, roomLength);
                }
                readNum += 2 + 4 + roomLength;
                GameCallPacket.GameRoom gameRoom = new GameCallPacket.GameRoom(subCallType, roomId);
                gameRoomList.add(gameRoom);
            }
            if (dataLength1 > 0) {
                int size = gameRoomList.size();
                reqPacket.setGameRooms(gameRoomList.toArray(new GameCallPacket.GameRoom[size]));
                dataRemainLength = dataRemainLength - dataLength1;
            }

            int dataLength2 = 0;
            //还有dataLength2
            if (dataLength > 2 + dataLength1) {
                //忽略一个字节的长度字段
                dataLength2 = buf.readUnsignedShort();
                dataRemainLength = dataRemainLength - 2;

                if (dataLength2 > 0) {
                    if (CallStatus.HANGUP == reqPacket.getCallStatus()) {
                        //GameCallHandupReqPacket handupReqPacket = (GameCallHandupReqPacket) reqPacket;
                        if (buf.isReadable()) {
                            //    handupReqPacket.setDesCode(GameCallHandupReqPacket.DesCode.formDesCode(buf.readByte()));
                            // hack to be compatible with the CallPacket
                            reqPacket.setStatus(Integer.valueOf(buf.readByte()));
                            dataRemainLength = dataRemainLength - 1;
                        }
                    } else {
                        //忽略剩余的字段
                        buf.readBytes(dataLength2);
                        dataRemainLength = dataRemainLength - dataLength2;
                    }
                }
            }

            if (dataRemainLength > 0) {
                logger.debug("game call.has remain data.length:{}", dataRemainLength);
                //忽略不支持的参数
                buf.readBytes(dataRemainLength);
            }
        }

        if (buf.isReadable(SIZE_SHORT)) {
            int extraLength = buf.readUnsignedShort();
            if (extraLength > 0) {
                if (buf.isReadable(extraLength)) {
                    reqPacket.setExtraData(ByteBufUtils.readByteArray(buf, extraLength));
                } else {
                    logger.error("game packet data error.");
                    return reqPacket;
                }
            }
        }

        //设置gameCallRoom
        if (null == reqPacket.getGameRooms()) {
            Integer[] reqSubCallTypes = reqPacket.getSubCallTypes();
            GameCallPacket.GameRoom[] gameRooms = new GameCallPacket.GameRoom[reqSubCallTypes.length];
            for (int i = 0; i < reqSubCallTypes.length; i++) {
                gameRooms[i] = new GameCallPacket.GameRoom(reqSubCallTypes[i], "0000000000000000000000000000000000000000000000000000000000000000");
            }
            reqPacket.setGameRooms(gameRooms);
        }

        return reqPacket;
    }

    private UdpServerAckPacket decodeUdpServerAckPacket(ByteBuf buf, PacketHead head) {
        UdpServerAckPacket udpServerAckPacket = new UdpServerAckPacket();
        udpServerAckPacket.setPacketHead(head);
        if (buf.isReadable() && buf.readableBytes() >= 32) {
            //String userId = ByteBufUtils.readUTF8String(buf, 32);

            String userId = ByteBufUtils.readNewUTF8String(buf, 32);

            udpServerAckPacket.setPeerName(userId);
            if (buf.isReadable()) {
                byte[] left = new byte[buf.readableBytes()];
                buf.readBytes(left);
                udpServerAckPacket.setData(left);
            }
        }
        return udpServerAckPacket;
    }

    private UdpRoutePacket decodeUdpRoutePacket(ByteBuf buf, PacketHead head) {

        UdpRoutePacket udpRoutePacket = new UdpRoutePacket();
        udpRoutePacket.setPacketHead(head);
        if (!buf.isReadable()) {
            logger.error("[UdpRoutePacket] content is empty !");
            return udpRoutePacket;
        }
        udpRoutePacket.setCommonad(Integer.valueOf(buf.readByte()));

        if (!buf.isReadable(32)) {
            logger.error("[UdpRoutePacket] userID is empty !");
            return udpRoutePacket;
        }
        //udpRoutePacket.setUserID(ByteBufUtils.readUTF8String(buf, 32));

        udpRoutePacket.setUserID(ByteBufUtils.readNewUTF8String(buf, 32));


        if (!buf.isReadable(2)) {
            logger.error("[UdpRoutePacket] detected id length is empty !");
            return udpRoutePacket;
        }

        int dLen = buf.readUnsignedShort();

        if (logger.isInfoEnabled()) {
            logger.info("[UdpRoutePacket] dLen:{}", dLen);
        }

        if (!buf.isReadable(dLen)) {
            logger.error("[UdpRoutePacket] detected id content is empty , readableBytes:{} !", buf.readableBytes());
            return udpRoutePacket;
        }
        udpRoutePacket.setDetectID(ByteBufUtils.readUTF8String(buf, dLen));

        return udpRoutePacket;
    }


    @Override
    public void encode(CommandPacket msg, ByteBuf buf) {
        switch (msg.getPacketType()) {

            case LoginPacket.COMMAND_LOGIN_PACKET_TYPE: {
                logger.debug("encode login packet.");
                LoginPacket loginPacket = (LoginPacket) msg;
                buf.writeByte(LoginPacket.COMMAND_LOGIN_TYPE);
                ByteBufUtils.writeUTF8String(buf, loginPacket.getSessionId());
                break;
            }

            case LoginResponsePacket.COMMAND_LOGIN_RESPONSE_PACKET_TYPE: {
                logger.debug("encode login response packet.");
                LoginResponsePacket loginResponsePacket = (LoginResponsePacket) msg;
                buf.writeByte(LoginResponsePacket.COMMAND_LOGIN_RESPONSE_TYPE);
                buf.writeByte(loginResponsePacket.getLoginStatus().getStatus());
                if (null != loginResponsePacket.getTcpServersJson()) {
                    ByteBufUtils.writeUTF8StringPrefix2ByteLength(buf, loginResponsePacket.getTcpServersJson());
                } else {
                    buf.writeShort(0);
                }

                if (StringUtils.isNotBlank(loginResponsePacket.getClientIP())) {
                    ByteBufUtils.writeUTF8StringPrefix2ByteLength(buf, loginResponsePacket.getClientIP());
                }


                break;
            }

            case HeartbeatPacket.COMMAND_HEARTBEAT_PACKET_TYPE: {
                buf.writeByte(HeartbeatPacket.COMMAND_HEARTBEAT_TYPE);
                break;
            }

            case HeartbeatResponsePacket.COMMAND_HEARTBEAT_RESPONSE_PACKET_TYPE: {
                buf.writeByte(HeartbeatResponsePacket.COMMAND_HEARTBEAT_RESPONSE_TYPE);
                HeartbeatResponsePacket responsePacket = (HeartbeatResponsePacket) msg;
                buf.writeLong(responsePacket.getHeartBeatSentTime());
                break;
            }

            case CallPacket.COMMAND_CALL_PACKET_TYPE: {
                CallPacket callPacket = (CallPacket) msg;
                buf.writeByte(CallPacket.COMMAND_CALL_TYPE);
                buf.writeByte(callPacket.getCallStatus().id());

                //ByteBufUtils.writeUTF8String(buf, callPacket.getPeerName());

                UserUtils.writeThirdUser(callPacket.getPeerName(), msg.getPacketHead(), buf);

                break;
            }

            case CallResponsePacket.COMMAND_CALL_RESPONSE_PACKET_TYPE: {
                CallResponsePacket callResponsePacket = (CallResponsePacket) msg;
                buf.writeByte(CallPacket.COMMAND_CALL_RESPONSE_TYPE);
                buf.writeByte(callResponsePacket.getCallStatus().id());

                //ByteBufUtils.writeUTF8String(buf, callResponsePacket.getPeerName());

                UserUtils.writeThirdUser(callResponsePacket.getPeerName(), msg.getPacketHead(), buf);

                boolean isHasStatus = null != callResponsePacket.getStatus();
                if (isHasStatus) {
                    buf.writeShort(1);
                    buf.writeByte(callResponsePacket.getStatus());
                } else {
                    buf.writeShort(0);
                }

                if (null != callResponsePacket.getRoomId()) {
                    ByteBufUtils.writeUTF8StringPrefix2ByteLength(buf, callResponsePacket.getRoomId());
                } else {
                    buf.writeShort(0);
                }

                if (null != callResponsePacket.getUserData() && callResponsePacket.getUserData().length > 0) {
                    ByteBufUtils.writeByteArrayPrefix2ByteLength(buf, callResponsePacket.getUserData());
                } else {
                    buf.writeShort(0);
                }

                break;
            }

            case UDPServerPacket.COMMAND_UDP_PACKET_TYPE: {
                UDPServerPacket serverPacket = (UDPServerPacket) msg;
                buf.writeByte(UDPServerPacket.COMMAND_UDP_TYPE);

                UDPServerPacket.UdpInfo[] udpInfo = serverPacket.getUdpInfo();
                int udpsLength = udpInfo.length;
                buf.writeInt(IPUtils.ip2int(udpInfo[0].getIp()));
                buf.writeShort(udpInfo[0].getPort());
                buf.writeByte(serverPacket.getFlag());
                if (null != serverPacket.getRoomId()) {
                    ByteBufUtils.writeUTF8String(buf, serverPacket.getRoomId());
                    if (udpsLength > 1) {
                        buf.writeShort(6 * (udpsLength - 1));
                        for (int i = 1; i < udpsLength; i++) {
                            buf.writeInt(IPUtils.ip2int(udpInfo[i].getIp()));
                            buf.writeShort(udpInfo[i].getPort());
                        }
                    }
                }
                break;
            }

            case GameCallRespPacket.COMMAND_GAME_CALL_RESP_PACKET_TYPE: {
                GameCallRespPacket gameCallRespPacket = (GameCallRespPacket) msg;
                encodeGameCallRespPacket(buf, gameCallRespPacket);
                break;
            }

            case GameServerRespPacket.COMMAND_GAME_SERVER_RESP_PACKET_TYPE: {
                GameServerRespPacket respPacket = (GameServerRespPacket) msg;
                buf.writeByte(GameServerRespPacket.COMMAND_GAME_SERVER_RESP_TYPE);
                for (GameServerRespPacket.GameServerInfo gameServerInfo : respPacket.getGameServerInfos()) {
                    byte[] roomId = null;
                    int roomIdLenth = 0;
                    if (null != gameServerInfo.getRoomId()) {
                        roomId = gameServerInfo.getRoomId().getBytes(Charset.forName("utf-8"));
                        roomIdLenth = roomId.length;
                    }
                    buf.writeShort(4 + 4 + 2 + 1 + roomIdLenth);
                    buf.writeInt(gameServerInfo.getGameId());
                    buf.writeInt(IPUtils.ip2int(gameServerInfo.getIp()));
                    buf.writeShort(gameServerInfo.getPort());
                    buf.writeByte(gameServerInfo.getFlag());
                    if (null != roomId) {
                        buf.writeBytes(roomId);
                    }
                }
                break;
            }

            case UdpServerAckPacket.COMMAND_UDPSERVER_ACK_PACKET_TYPE: {
                UdpServerAckPacket respPacket = (UdpServerAckPacket) msg;
                buf.writeByte(UdpServerAckPacket.COMMAND_UDPSERVER_ACK_TYPE);
                //ByteBufUtils.writeUTF8String(buf, respPacket.getPeerName());

                UserUtils.writeThirdUser(respPacket.getPeerName(), msg.getPacketHead(), buf);

                if (null != respPacket.getData()) {
                    buf.writeBytes(respPacket.getData());
                }
                break;
            }

            case UdpRoutePacket.COMMAND_UDP_ROUTE_PACKET_TYPE: {
                UdpRoutePacket routePacket = (UdpRoutePacket) msg;
                buf.writeByte(UdpRoutePacket.COMMAND_UDP_ROUTE_TYPE);
                buf.writeByte(routePacket.getCommonad());
                if (StringUtils.isNotBlank(routePacket.getUserID())) {
                    //ByteBufUtils.writeUTF8String(buf, routePacket.getUserID());

                    UserUtils.writeThirdUser(routePacket.getUserID(), msg.getPacketHead(), buf);

                    if (StringUtils.isNotBlank(routePacket.getDetectID())) {
                        ByteBufUtils.writeUTF8StringPrefix2ByteLength(buf, routePacket.getDetectID());
                    }

                    if (StringUtils.isNotBlank(routePacket.getExtraData())) {
                        ByteBufUtils.writeUTF8StringPrefix2ByteLength(buf, routePacket.getExtraData());
                    }

                }
                break;
            }

            default: {
                logger.debug("unknown command packte, cna't encode.");
                break;
            }
        }
    }

    private void encodeGameCallRespPacket(ByteBuf buf, GameCallRespPacket respPacket) {
        buf.writeByte(GameCallRespPacket.COMMAND_GAME_CALL_RESPONSE_TYPE);
        buf.writeByte(respPacket.getCallStatus().id());

        UserUtils.writeThirdUser(respPacket.getPeerName(), respPacket.getPacketHead(), buf);
        //buf.writeBytes(respPacket.getPeerName().getBytes(Charset.forName("utf-8")));

        buf.writeBytes(respPacket.getSsrc());
        if (null != respPacket.getSubCallTypes()) {
            buf.writeShort(respPacket.getSubCallTypes().length * 4);
            for (int i = 0; i < respPacket.getSubCallTypes().length; i++) {
                buf.writeInt(respPacket.getSubCallTypes()[i]);
            }
        } else {
            buf.writeShort(0);
        }


        GameCallPacket.GameRoom[] gameRooms = respPacket.getGameRooms();
        if (gameRooms != null) {
            short dataLength1 = (short) (gameRooms.length * (2 + 64 + 4));
            //data length
            if (dataLength1 > 0) {
                buf.writeShort(dataLength1 + 2);
                buf.writeShort(dataLength1);
                for (GameCallPacket.GameRoom gameRoom : gameRooms) {
                    buf.writeShort(64);
                    buf.writeInt(gameRoom.getGameId());
                    buf.writeBytes(gameRoom.getRoomId().getBytes(Charset.forName("utf-8")));
                }
            } else {
                buf.writeShort(dataLength1);
            }
            //data length1
            //不需要向客户端发送dataLength2

        } else {
            buf.writeShort(0);
        }

        byte[] extraData = respPacket.getExtraData();
        if (null != extraData && extraData.length > 0) {
            buf.writeShort(extraData.length);
            buf.writeBytes(extraData);
        } else {
            buf.writeShort(0);
        }
    }

    private boolean isValidRoomId(byte roomIdPrefix) {
        return roomIdPrefix != 0;
    }
}

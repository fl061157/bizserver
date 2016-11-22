package com.handwin.server.controller;

import com.handwin.entity.ChannelInfo;
import com.handwin.entity.User;
import com.handwin.packet.*;
import com.handwin.packet.v5.*;
import com.handwin.protocal.v5.codec.V5GenericPacket;
import com.handwin.protocal.v5.codec.V5PacketHead;
import com.handwin.server.Channel;
import com.handwin.server.PseudoChannelImpl;
import com.handwin.server.handler.GenericMessageHandler;
import com.handwin.server.handler.Handler;
import com.handwin.server.handler.HandlerHolder;
import com.handwin.service.UserService;
import com.handwin.utils.V5MsgCustomHeaderUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by piguangtao on 16/3/21.
 * 特定的协议转换 转化为原始的二进制群组消息协议
 * <p>
 * <p>
 * 请求头字段
 * <p>
 * 群组Id
 * 是否指定接受者（如果指定了接受者 则只有特定的接受者才能接受到其它消息；否则 群组的其它人可以接受到消息）
 * 接受者列表
 * msg_type:0x01 (规则消息的类型 是文本 图片 视频等)
 * 消息内容
 * cmsgId
 * 群组所在的区域 以该字段判断消息是否转换
 * <p>
 * body直接是消息内容
 */
@Service
@Controller(value = "/v5/group/msg")
public class GroupMessageAdaptorController implements ServiceController {
    private static final Logger LOGGER = LoggerFactory.getLogger(GroupMessageAdaptorController.class);

    @Autowired
    private V5MsgCustomHeaderUtil headerUtil;

    @Autowired
    protected HandlerHolder hodler;

    @Autowired
    private UserService userService;

    private static final Logger logger = LoggerFactory.getLogger(GroupMessageAdaptorController.class);

    @Override
    public void handle(Channel channel, V5PacketHead packetHead, V5GenericPacket genericPacket) {
        String from = packetHead.getFrom();
        String groupId = headerUtil.getGroupId(packetHead);
//        String groupRegion = headerUtil.getGroupRegion(packetHead);
        Integer msgType = headerUtil.getMsgType(packetHead);
        if (null == msgType) {
            LOGGER.warn("[generic msg] invalid msgType. head:{}, packet:{}", packetHead, genericPacket);
            return;
        }

        SimpleMessagePacket simpleMessagePacket;


        String to = packetHead.getTo();
        String[] groupSpeUsers = null;
        if (StringUtils.isNotBlank(to)) {
            groupSpeUsers = to.split(",");
        }

        Handler simpleMessageHandler = null;

        switch (msgType.byteValue()) {
            case SimpleMessagePacket.TEXT_MESSAGE_TYPE: {
                //文本群组消息
                simpleMessagePacket = new V5TextMessagePacket();
                simpleMessageHandler = hodler.getHandler(TextMessagePacket.class);
                break;
            }
            case SimpleMessagePacket.IMAGE_URL_MESSAGE_TYPE: {
                //图片群组消息
                simpleMessagePacket = new V5ImageMessagePacket();
                simpleMessageHandler = hodler.getHandler(ImageMessagePacket.class);
                break;
            }
            case SimpleMessagePacket.VOICE_MESSAGE_TYPE: {
                //语音消息
                simpleMessagePacket = new V5VoiceMessagePacket();
                simpleMessageHandler = hodler.getHandler(VoiceMessagePacket.class);
                break;
            }
            case SimpleMessagePacket.VIDEO_MESSAGE_TYPE: {
                //短视频消息
                simpleMessagePacket = new V5VideoMessagePacket();
                simpleMessageHandler = hodler.getHandler(VideoMessagePacket.class);
                break;
            }
            default: {
                return;
            }
        }
        simpleMessagePacket.setSrcMsgBytes(genericPacket.getSrcMsgBytes());

        simpleMessagePacket.setFrom(from);
        simpleMessagePacket.setFromGroup(groupId);
        simpleMessagePacket.setCmsgid(packetHead.getMessageID());
        simpleMessagePacket.setMessageType(msgType.byteValue());
        simpleMessagePacket.setToGroup(groupId);
        simpleMessagePacket.setContent(genericPacket.getBodySrcBytes());
        simpleMessagePacket.setMessageServiceType(SimpleMessagePacket.TO_GROUP);

        ((V5SimpleMessagepacket) simpleMessagePacket).setGroupSpeUsers(groupSpeUsers);
        ((V5SimpleMessagepacket) simpleMessagePacket).setMessageSourceRegion(packetHead.getFromRegion());


        String traceId = packetHead.getTraceId();

        User user = userService.findById(from, channel.getChannelInfo().getAppID());

        PacketHead simplePacketHead = new PacketHead();
        simplePacketHead.setAppId(user.getAppId());
        simplePacketHead.setSecret((byte) 0x00);

        simpleMessagePacket.setPacketHead(simplePacketHead);

        ChannelInfo channelInfo = new ChannelInfo();
        channelInfo.setUserId(from);
        channelInfo.setAppID(user.getAppId());
        channelInfo.setUserZoneCode(user.getCountrycode());
        channelInfo.setTcpZoneCode(user.getCountrycode());
        channelInfo.setClientVersion(0x04);

        PseudoChannelImpl pseudoChannel = new PseudoChannelImpl(traceId, channelInfo);

        if (logger.isDebugEnabled()) {
            logger.debug("Group Controller Channel:", pseudoChannel.getChannelInfo());
        }
        simpleMessageHandler.handle(pseudoChannel, simpleMessagePacket);
    }
}

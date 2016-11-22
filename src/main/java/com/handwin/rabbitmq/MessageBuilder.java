package com.handwin.rabbitmq;

import com.chatgame.protobuf.TcpBiz;
import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors;
import com.google.protobuf.GeneratedMessage;
import com.handwin.entity.*;
import com.handwin.exception.ServerException;
import com.handwin.server.proto.BaseResponseMessage;
import com.handwin.service.UserService;
import com.handwin.utils.JsonUtil;
import com.handwin.utils.UserUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.Charset;

/**
 * @author fangliang
 */
@Service
public class MessageBuilder {

    public static final int ROUTE_KEY_CUT_WORLDS_COUNT = 33;
    @Value("#{configproperties['tcp_server.exchange']}")
    private String exchange;
    @Value("#{configproperties['push.msg.queue']}")
    private String pushQueueName;
    @Value("#{configproperties['biz2robot.queue']}")
    private String bizRobotQueueName;
    @Value("#{configproperties['biz_server.exchange']}")
    private String biz2bizExchange;
    @Value("#{configproperties['biz2biz.queue']}")
    private String biz2bizQueueName;
    @Value("#{configproperties['biz2biz.status.queue']}")
    private String biz2bizStatusQueueName;
    @Value("#{configproperties['default.country.code']}")
    private String defaultCountryCode;
    @Value("#{configproperties['country.codes']}")
    private String countryCode;
    @Value("#{configproperties['serverheart.queue']}")
    private String serverHeartBeatQueue;
    @Autowired
    private UserService userService;
    @Autowired
    private ObjectMapper objectMapper;


    private static final Logger logger = LoggerFactory.getLogger(MessageBuilder.class);

    @SuppressWarnings("rawtypes")
    protected static <T extends GeneratedMessage.Builder> T setProtoField(T protoObjectBuilder,
                                                                          int fieldNumber, Object value) {
        if (value != null) {
            Descriptors.Descriptor descriptor = protoObjectBuilder.getDescriptorForType();
            Descriptors.FieldDescriptor fieldDescriptor = descriptor.findFieldByNumber(fieldNumber);
            protoObjectBuilder.setField(fieldDescriptor, value);
        }
        return protoObjectBuilder;
    }

    public static String cutRouteKey(String routeKey, int backCutWorldsNum) {
        int length = 0;
        if (StringUtils.isBlank(routeKey) || (length = routeKey.length()) < backCutWorldsNum) {
            return routeKey;
        }
        return routeKey.substring(0, length - backCutWorldsNum);
    }

    public BizOutputMessage buildTcpMessage(String routingKey, BaseResponseMessage resp,
                                            byte[] packetBody) {
        return buildTcpMessage(routingKey, resp, packetBody, null);
    }

    public BizOutputMessage buildTcpMessage(String routingKey, BaseResponseMessage resp,
                                            byte[] packetBody, byte[] trackBackInfo) {
        routingKey = cutRouteKey(routingKey, ROUTE_KEY_CUT_WORLDS_COUNT);
        TcpBiz.Biz2TcpReps.Builder builder = TcpBiz.Biz2TcpReps.getDefaultInstance().newBuilderForType();
        setProtoField(builder, TcpBiz.Biz2TcpReps.TRACEID_FIELD_NUMBER, resp.getTraceId());
        setProtoField(builder, TcpBiz.Biz2TcpReps.TCPCHANNELID_FIELD_NUMBER, resp.getTcpChannelId());
        setProtoField(builder, TcpBiz.Biz2TcpReps.TCPCHANNELUUID_FIELD_NUMBER, resp.getTcpChannelUuid());
        setProtoField(builder, TcpBiz.Biz2TcpReps.ISLOCALUSER_FIELD_NUMBER, resp.isLocalUser());
        setProtoField(builder, TcpBiz.Biz2TcpReps.APPID_FIELD_NUMBER, resp.getAppId());
        setProtoField(builder, TcpBiz.Biz2TcpReps.ACTIONS_FIELD_NUMBER, resp.getActions());
        setProtoField(builder, TcpBiz.Biz2TcpReps.SESSIONID_FIELD_NUMBER, resp.getSessionId());
        setProtoField(builder, TcpBiz.Biz2TcpReps.USERZONECODE_FIELD_NUMBER, resp.getUserZonecode());
        setProtoField(builder, TcpBiz.Biz2TcpReps.MSGBODY_FIELD_NUMBER, ByteString.copyFrom(packetBody));
        setProtoField(builder, TcpBiz.Biz2TcpReps.USERID_FIELD_NUMBER, resp.getUserId());
        setProtoField(builder, TcpBiz.Biz2TcpReps.CHANNELMODE_FIELD_NUMBER, resp.getChannelMode());
        setProtoField(builder, TcpBiz.Biz2TcpReps.ROOMID_FIELD_NUMBER, resp.getRoomID());
        //setProtoField(builder, TcpBiz.Biz2TcpReps.PLATFORM_FIELD_NUMBER, resp.getPlatform());
        if (trackBackInfo != null && trackBackInfo.length > 0) {
            if (logger.isDebugEnabled()) {
                logger.debug("Add trackBackInfo");
            }
            try {
                setProtoField(builder, TcpBiz.Biz2TcpReps.BACKTRACKINFO_FIELD_NUMBER, new String(trackBackInfo));
            } catch (Exception e) {
                logger.error("", e);
            }
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("trackBackInfo Is Empty!");
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("BuildTcpMessage TraceId: {}", resp.getTraceId());
        }
        BizOutputMessage bizOutputMessage = new BizOutputMessage(exchange, routingKey, builder.build().toByteArray());
        return bizOutputMessage;
    }


    public BizOutputMessage buildTcp2bizReqMessage(String routingKey, BaseResponseMessage resp,
                                                   byte[] packetBody, byte[] trackBackInfo) {
        routingKey = cutRouteKey(routingKey, ROUTE_KEY_CUT_WORLDS_COUNT);
        TcpBiz.Tcp2BizReq.Builder builder = TcpBiz.Tcp2BizReq.getDefaultInstance().newBuilderForType();
        try {
            setProtoField(builder, TcpBiz.Tcp2BizReq.TRACEID_FIELD_NUMBER, resp.getTraceId());
            setProtoField(builder, TcpBiz.Tcp2BizReq.TCPCHANNELUUID_FIELD_NUMBER, resp.getTcpChannelUuid());
            setProtoField(builder, TcpBiz.Tcp2BizReq.ISLOCALUSER_FIELD_NUMBER, resp.isLocalUser());
            setProtoField(builder, TcpBiz.Tcp2BizReq.APPID_FIELD_NUMBER, resp.getAppId());
            setProtoField(builder, TcpBiz.Tcp2BizReq.SESSIONID_FIELD_NUMBER, resp.getSessionId());
            setProtoField(builder, TcpBiz.Tcp2BizReq.USERZONECODE_FIELD_NUMBER, resp.getUserZonecode());
            setProtoField(builder, TcpBiz.Tcp2BizReq.MSGBODY_FIELD_NUMBER, ByteString.copyFrom(packetBody));
            setProtoField(builder, TcpBiz.Tcp2BizReq.USERID_FIELD_NUMBER, resp.getUserId());
            setProtoField(builder, TcpBiz.Tcp2BizReq.CHANNELMODE_FIELD_NUMBER, resp.getChannelMode());
            //setProtoField(builder, TcpBiz.Tcp2BizReq.PLATFORM_FIELD_NUMBER, resp.getPlatform());
            if (trackBackInfo != null && trackBackInfo.length > 0) {
                setProtoField(builder, TcpBiz.Tcp2BizReq.BACKTRACKINFO_FIELD_NUMBER, ByteString.copyFrom(trackBackInfo));
            }
        } catch (Exception e) {
        }

        byte[] result = builder.build().toByteArray();
        result = ArrayUtils.isNotEmpty(result) ? result : new byte[]{};
        BizOutputMessage bizOutputMessage = new BizOutputMessage(exchange, routingKey, result);
        return bizOutputMessage;
    }

    public byte[] buildPushMsgMqBean(PushMsgMqBean bean) {
        if (null == bean.getNoticeType()) {
            PushMsgMqBean.NoticeType noticeType = getNotifyType(bean.getTo(), null != bean.getAppId() ? Integer.valueOf(bean.getAppId()) : 0);
            bean.setNoticeType(noticeType);
        }
        byte[] messageBody = JsonUtil.toJson(objectMapper, bean).getBytes(Charset.forName("utf-8"));
        //byte[] messageBody = JSON.toJSONString(bean).getBytes(Charset.forName("utf-8"));
        return messageBody;
    }

    public byte[] buildBrocastServerStatus(String tcpServerID) {
        TcpBiz.Tcp2BizReq.Builder tcp2BizReqBuilder = TcpBiz.Tcp2BizReq.getDefaultInstance().newBuilderForType();
        tcp2BizReqBuilder.setTcpServerId(tcpServerID);
        tcp2BizReqBuilder.setMessageType(ServerMessageType.ServerForwardHeartBeatMessage.getMessageType());
        TcpBiz.Tcp2BizReq tcp2BizReq = tcp2BizReqBuilder.build();
        byte[] message = tcp2BizReq.toByteArray();
        return message;
    }

    public PushMsgMqBean buildPushTextBean(PushTextBean pushTextBean) {
        final PushMsgMqBean mqBean = new PushMsgMqBean();
        buildPushMsgMqBean((PushBean) pushTextBean, mqBean);
        buildPushTextMqBean(pushTextBean, mqBean);
        return mqBean;
    }

    public PushMsgMqBean buildPushMsgMqBean(final PushCallBean pushCallBean) {
        final PushMsgMqBean mqBean = new PushMsgMqBean();
        buildPushMsgMqBean((PushBean) pushCallBean, mqBean);
        buildPushCallMqBean(pushCallBean, mqBean);
        return mqBean;
    }


    private void buildPushCallMqBean(PushCallBean pushCallBean, PushMsgMqBean mqBean) {
        mqBean.setType(PushMsgMqBean.TYPE_CALL);
        switch (pushCallBean.getCallType()) {
            case AUDIO: {
                mqBean.setSubType(PushMsgMqBean.SUBTYPE_CALL_AUDIO);
                break;
            }
            case VIDEO: {
                mqBean.setSubType(PushMsgMqBean.SUBTYPE_CALL_VIDEO);
                break;
            }
        }

        // games related
        if (pushCallBean instanceof PushGameCallBean) {
            mqBean.setSubType(PushMsgMqBean.SUBTYPE_GAME_CALL);
            mqBean.setMediaType(((PushGameCallBean) pushCallBean).getMediaType());
            mqBean.setGameIds(((PushGameCallBean) pushCallBean).getGameIds());
        }
        if (null != pushCallBean.getRoomId()) {
            mqBean.getAttrs().put("room_id", pushCallBean.getRoomId());
        }

    }

    protected void buildPushTextMqBean(PushTextBean pushTextBean, PushMsgMqBean mqBean) {
        mqBean.setType(PushMsgMqBean.TYPE_MESSAGE);
        mqBean.setReplyType(pushTextBean.isReplyType());
        mqBean.setSubType(pushTextBean.getSendType());
    }

    protected void buildPushMsgMqBean(PushBean pushBean, PushMsgMqBean mqBean) {
        mqBean.setExtraData(pushBean.getExtraData());
        mqBean.setContent(pushBean.getContent());
        mqBean.setAppId(String.valueOf(pushBean.getAppId()));
        mqBean.setProvider(pushBean.getProvider());
        mqBean.setDeviceToken(pushBean.getDeviceToken());

        if (DeviceType.IOS.getValue() == pushBean.getDeviceType().getValue()) {
            mqBean.setDeviceType(PushMsgMqBean.DEVICE_TYPE_IOS);
            mqBean.setUnreadCount(pushBean.getMsgNumber());
        } else if (DeviceType.ANDRIOD.getValue() == pushBean.getDeviceType().getValue()) {
            mqBean.setDeviceType(PushMsgMqBean.DEVICE_TYPE_ANDRIOD);
        }

        mqBean.setFrom(pushBean.getFrom());
        mqBean.setTo(pushBean.getTo());
        mqBean.setTraceId(pushBean.getTraceId());
        mqBean.setUnreadCount(pushBean.getMsgNumber());

        mqBean.setFromAvatarUrl(pushBean.getFromAvatarUrl());
        mqBean.setFromNickName(pushBean.getFromNickName());
        mqBean.setFromMobile(pushBean.getFromMobile());
        mqBean.setTime(pushBean.getTime());
        mqBean.setFromCountryCode(pushBean.getFromCountryCode());
        if (StringUtils.isNotBlank(pushBean.getTipType())) {
            mqBean.setTipType(pushBean.getTipType());
        }
        if (pushBean.getAttrs().size() > 0) {
            mqBean.getAttrs().putAll(pushBean.getAttrs());
        }

        if (pushBean.getMetaMap().size() > 0) {
            mqBean.getMetaMap().putAll(pushBean.getAttrs());
        }
    }

    protected PushMsgMqBean.NoticeType getNotifyType(String userID, Integer appId) {
        User user = null;
        try {
            user = userService.loadById(userID, appId);
        } catch (ServerException e) {
            return null;
        }
        boolean isHideTime = (null != user && UserUtils.isUserHideMessage(user.getHideTime(), user.getTimezone()));
        return isHideTime ? PushMsgMqBean.NoticeType.NO_ALERT : PushMsgMqBean.NoticeType.SOUND;
    }
}

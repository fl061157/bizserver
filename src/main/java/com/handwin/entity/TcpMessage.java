package com.handwin.entity;

import com.handwin.rabbitmq.MessageBuilder;
import com.handwin.server.proto.BaseResponseMessage;
import com.handwin.server.proto.ChannelAction;
import org.apache.commons.lang.ArrayUtils;

/**
 * Created by fangliang on 16/5/15.
 */
public class TcpMessage {

    private byte[] packetBody;

    private byte[] extraBody;

    private ChannelAction[] channelActions;

    private ChannelInfo channelInfo;

    private String traceID;

    private String nodeID;

    private byte[] messageBody;

    private BizOutputMessage bizOutputMessage;

    public TcpMessage(ChannelInfo channelInfo, byte[] packetBody, byte[] extraBody, ChannelAction[] channelActions, String traceID) {
        this.channelInfo = channelInfo;
        this.packetBody = packetBody;
        this.extraBody = extraBody;
        this.channelActions = channelActions;
        this.traceID = traceID;
        this.nodeID = channelInfo.getNodeId();
    }


    public TcpMessage(ChannelInfo channelInfo, byte[] packetBody, byte[] extraBody, ChannelAction[] channelActions) {
        this.channelInfo = channelInfo;
        this.packetBody = packetBody;
        this.extraBody = extraBody;
        this.channelActions = channelActions;
        this.nodeID = channelInfo.getNodeId();
    }

    public byte[] getExtraBody() {
        return extraBody;
    }

    public byte[] getPacketBody() {
        return packetBody;
    }


    public byte[] getMessageBody(MessageBuilder messageBuilder) {
        if (ArrayUtils.isEmpty(messageBody)) {
            getBizOutputMessage(messageBuilder);
            if (bizOutputMessage != null) {
                messageBody = bizOutputMessage.getMessageBody();
            }
        }
        return messageBody;
    }

    public BizOutputMessage getBizOutputMessage(MessageBuilder messageBuilder) {

        if (bizOutputMessage == null) {
            ChannelInfo channelInfo;
            if ((channelInfo = getChannelInfo()) == null) {
                return null;
            }

            String traceId = getTraceID();
            ChannelAction[] tcpActions = getChannelActions();
            BaseResponseMessage baseResponseMessage = BaseResponseMessage.formResponseMessage(channelInfo, traceId);
            if (tcpActions != null) {
                for (ChannelAction tcpAction : tcpActions) {
                    baseResponseMessage.addAction(tcpAction);
                }
            }
            byte[] packetBody = getPacketBody();
            byte[] extraBody = getExtraBody();

            BizOutputMessage bom = messageBuilder.buildTcpMessage(channelInfo.getNodeId(),
                    baseResponseMessage, packetBody, extraBody);
            bom.setTcpZoneCode(channelInfo.getTcpZoneCode());

            bizOutputMessage = bom;
        }

        return bizOutputMessage;
    }


    public ChannelAction[] getChannelActions() {
        return channelActions;
    }

    public void setChannelActions(ChannelAction[] channelActions) {
        this.channelActions = channelActions;
    }

    public void setExtraBody(byte[] extraBody) {
        this.extraBody = extraBody;
    }

    public void setPacketBody(byte[] packetBody) {
        this.packetBody = packetBody;
    }

    public ChannelInfo getChannelInfo() {
        return channelInfo;
    }

    public void setChannelInfo(ChannelInfo channelInfo) {
        this.channelInfo = channelInfo;
    }

    public String getTraceID() {
        return traceID;
    }

    public void setTraceID(String traceID) {
        this.traceID = traceID;
    }

    public String getNodeID() {
        return nodeID;
    }
}

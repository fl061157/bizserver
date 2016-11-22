package com.handwin.packet;

import com.handwin.utils.SystemUtils;
import com.handwin.utils.V5ProtoConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Created by piguangtao on 15/7/14.
 */
public class CommonMsgPackage {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommonMsgPackage.class);
    private int type;
    private SimpleMessagePacket simpleMessagePacket;
    private GenericPacket genericPacket;

    public CommonMsgPackage(SimpleMessagePacket simpleMessagePacket) {
        this.type = 1;
        this.simpleMessagePacket = simpleMessagePacket;
    }

    public CommonMsgPackage(GenericPacket genericPacket) {
        this.type = 2;
        this.genericPacket = genericPacket;
    }

    @Override
    public String toString() {
        String toString = "";
        switch (type) {
            case 1:
                toString = this.simpleMessagePacket.toString();
                break;

            case 2:
                toString = this.genericPacket.toString();
                break;

            default:
                break;
        }
        return toString;
    }

    public void setFrom(String from) {
        switch (type) {
            case 1:
                simpleMessagePacket.setFrom(from);
                break;
            case 2:
                genericPacket.getPacketHead().setFrom(from);
                break;
            default:
                break;
        }
    }

    public String getCmsgid() {
        String cmsgId = "";
        switch (type) {
            case 1:
                cmsgId = simpleMessagePacket.getCmsgid();
                break;
            case 2:
                cmsgId = genericPacket.getPacketHead().getMessageID();
                break;
            default:
                break;
        }
        return cmsgId;
    }

    public byte getMsgFlag() {
        byte msgFlag = 0;
        switch (type) {
            case 1:
                msgFlag = simpleMessagePacket.getMsgFlag();
                break;
            case 2:
                msgFlag = genericPacket.getPacketHead().getResend() ? (byte) 0x01 : 0x00;
                break;
            default:
                break;
        }
        return msgFlag;
    }

    public int getTempId() {
        int tempId = 0;
        switch (type) {
            case 1:
                tempId = simpleMessagePacket.getTempId();
                break;
            case 2:
                tempId = genericPacket.getPacketHead().getTempId();
                break;
            default:
                break;
        }
        return tempId;
    }

    public byte getMessageServiceType() {
        byte result = 0;
        switch (type) {
            case 1:
                result = simpleMessagePacket.getMessageServiceType();
                break;
            case 2:
                //目前只支持单发和群发
                result = genericPacket.getPacketHead().getMessageServiceType();
                break;
            default:
                break;
        }
        return result;
    }

    public String getToUser() {
        String toUser = "";
        switch (type) {
            case 1:
                toUser = simpleMessagePacket.getToUser();
                break;
            case 2:
                toUser = genericPacket.getPacketHead().getToUser();
                break;
            default:
                break;
        }
        return toUser;
    }

    public String getFrom() {
        String from = "";
        switch (type) {
            case 1:
                from = simpleMessagePacket.getFrom();
                break;
            case 2:
                from = genericPacket.getPacketHead().getFrom();
                break;
            default:
                break;
        }
        return from;
    }

    public String getFromGroup() {
        String fromGroup = null;
        switch (type) {
            case 1:
                fromGroup = simpleMessagePacket.getFromGroup();
                break;
            case 2:
                fromGroup = genericPacket.getPacketHead().getTo();
                break;
            default:
                break;
        }
        return fromGroup;
    }

    public byte getMessageType() {
        byte result = 0;
        switch (type) {
            case 1:
                result = simpleMessagePacket.getMessageType();
                break;
            case 2:
                result = genericPacket.getPacketHead().getMessageType();
                break;
            default:
                break;
        }
        return result;
    }

    public byte[] getSrcMsgBytes() {
        byte[] result = null;
        switch (type) {
            case 1:
                result = simpleMessagePacket.getSrcMsgBytes();
                break;
            case 2:
                result = genericPacket.getSrcMsgBytes();
                break;
            default:
                break;
        }
        return result;
    }

    public String getToGroup() {
        String result = null;
        switch (type) {
            case 1:
                result = simpleMessagePacket.getToGroup();
                break;
            case 2:
                result = genericPacket.getPacketHead().getTo();
                break;
            default:
                break;
        }
        return result;
    }

    public void setToUser(String toUser) {
        switch (type) {
            case 1:
                simpleMessagePacket.setToUser(toUser);
                break;
            case 2:
                genericPacket.getPacketHead().setToUser(toUser);
                break;
            default:
                break;
        }
    }

    public byte[] getContent() {
        byte[] result = null;
        switch (type) {
            case 1:
                result = simpleMessagePacket.getContent();
                break;
            case 2:
                switch (genericPacket.getBodyType()) {
                    case 1:
                        result = genericPacket.getBodySrcBytes();
                        break;
                    case 2:
                        //map转化为json
                        try {
                            result = SystemUtils.getJsonStr(genericPacket.getBodyMap()).getBytes(StandardCharsets.UTF_8);
                        } catch (IOException e) {
                            LOGGER.error("fails to parse body map to json", e);
                        }
                        break;
                    default:
                }

                break;
            default:
                break;
        }
        return result;
    }


    public byte getSecret() {
        byte result = 0x00;
        switch (type) {
            case 1:
                result = simpleMessagePacket.getPacketHead().getSecret();
                break;
            case 2:
                //新协议 不加密
                break;
            default:
                break;
        }
        return result;
    }

    public int getType() {
        return type;
    }

    public GenericPacket copyGenericPacket() {
        if (null == genericPacket) return null;
        PacketHead packetHead = genericPacket.getPacketHead().copy();
        return genericPacket.copy(packetHead);
    }

    public boolean isPush() {
        boolean result = true;
        switch (type) {
            case 1:
                //simpleMessagePacket 需要推送
                break;
            case 2:
                result = genericPacket.getPacketHead().getPush();
                break;
        }
        return result;
    }

    public GenericPacket getGenericPacket() {
        return genericPacket;
    }


    public boolean replyServerReceived() {
        boolean result = true;
        switch (type) {
            case 1:
                //simpleMessagePacket 需要回复
                break;
            case 2:
                result = genericPacket.getPacketHead().getServerReceivedConfirm();
                break;
        }
        return result;

    }

    public boolean needStore() {
        boolean result = true;
        switch (type) {
            case 1:
                //simpleMessagePacket 需要存储
                break;
            case 2:
                Boolean store = genericPacket.getPacketHead().getStore();
                //命令消息默认不存储
                if (null == store) {
                    String service = genericPacket.getPacketHead().getService();
                    if (V5ProtoConstant.SERVICE_SEND_SINGE_CMD.equals(service) || V5ProtoConstant.SERVICE_SEND_GROUP_CMD.equals(service) || V5ProtoConstant.SERVICE_SEND_CHATROOM_CMD.equals(service)) {
                        result = false;
                    }
                } else {
                    result = store;
                }
                break;
        }
        return result;
    }

    public int messagePacketType() {
        int result = 0;
        switch (type) {
            case 1:
                result = simpleMessagePacket.getPacketType();
                break;
            case 2:
                result = genericPacket.getPacketHead().getPacketType();
                break;
        }
        return result;

    }
}

package com.handwin.codec;

import com.handwin.utils.MessageUtils;
import com.handwin.utils.UserUtils;
import org.codehaus.jackson.map.ObjectMapper;
import com.handwin.packet.PacketHead;
import com.handwin.packet.SystemNotifyPacket;
import com.handwin.utils.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class SystemNotifyPackageCodec extends BasePacketDecodeAndEncoder<SystemNotifyPacket>
        implements InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(SystemNotifyPackageCodec.class);

    @Autowired
    private PacketCodecs packetCodecs;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MessageUtils messageUtils;

    public SystemNotifyPackageCodec() {
    }

    public void afterPropertiesSet() throws Exception {
        register();
    }

    @Override
    public void register() {
        packetCodecs.register((int) SystemNotifyPacket.SYSTEM_NOTIFY_PACKAGE_TYPE, this);
    }

    @Override
    public SystemNotifyPacket decode(ByteBuf buf, PacketHead head) {
        SystemNotifyPacket systemNotifyPackage = new SystemNotifyPacket();
        systemNotifyPackage.setPacketHead(head);

        if (!buf.isReadable()) {
            return null;
        }
        systemNotifyPackage.setMsgType(buf.readByte());

        if (!buf.isReadable()) {
            return null;
        }
        systemNotifyPackage.setServeType(buf.readByte());

        if (!buf.isReadable(32)) {
            return null;
        }
        //systemNotifyPackage.setFrom(ByteBufUtils.readUTF8String(buf, 32));

        systemNotifyPackage.setFrom(ByteBufUtils.readNewUTF8String(buf, 32));

        if (!buf.isReadable(32)) {
            return null;
        }
        //systemNotifyPackage.setTo(ByteBufUtils.readUTF8String(buf, 32));

        systemNotifyPackage.setTo(ByteBufUtils.readNewUTF8String(buf, 32));

        if (!buf.isReadable(SIZE_LONG)) {
            return null;
        }
        systemNotifyPackage.setExpired(buf.readLong());

        if (!buf.isReadable(SIZE_SHORT)) {
            return null;
        }
        int pushContentLength = buf.readUnsignedShort();

        systemNotifyPackage.setPushContentLength(pushContentLength);
        if (pushContentLength > 0) {
            if (!buf.isReadable(pushContentLength)) {
                return systemNotifyPackage;
            }
            systemNotifyPackage.setPushContentBody(ByteBufUtils.readUTF8String(buf, pushContentLength));
            systemNotifyPackage.setPushContentTemplate(messageUtils.generateRichMessage(systemNotifyPackage.getPushContentBody()));
        }

        if (!buf.isReadable(SIZE_SHORT)) {
            return systemNotifyPackage;
        }
        int messageLength = buf.readUnsignedShort();

        systemNotifyPackage.setMesssageLength(messageLength);
        if (messageLength > 0) {
            if (buf.isReadable(messageLength)) {
                systemNotifyPackage.setMessageBody(ByteBufUtils.readUTF8String(buf, messageLength));
            } else {
                logger.error("system notify package data error.");
                return systemNotifyPackage;
            }
        }

        if (buf.isReadable(SIZE_LONG)) {
            systemNotifyPackage.setMsgId(buf.readLong());
        } else {
            return systemNotifyPackage;
        }

        if (!buf.isReadable(SIZE_SHORT)) {
            return systemNotifyPackage;
        }
        int cmsgIdLength = buf.readUnsignedShort();

        if (cmsgIdLength > 0) {
            if (buf.isReadable(cmsgIdLength)) {
                systemNotifyPackage.setCmsgId(ByteBufUtils.readUTF8String(buf, cmsgIdLength));
            } else {
                logger.error("system notify package data error.");
                return systemNotifyPackage;
            }
        }
        try {
            if (buf.isReadable(SIZE_SHORT)) {
                int extraLen = buf.readUnsignedShort();
                if (extraLen > 0) {
                    if (buf.isReadable(extraLen)) {
                        byte[] extraBytes = ByteBufUtils.readByteArray(buf, extraLen);
                        systemNotifyPackage.setExtra(objectMapper.readValue(extraBytes, new TypeReference<Map<String, Object>>() {
                        }));
                    }
                }
            }
        } catch (Exception e) {
            logger.error("fails to parse system notify extra.");
        }

        return systemNotifyPackage;
    }


    @Override
    public void encode(SystemNotifyPacket msg, ByteBuf buf) {
        logger.debug("[encode notify package.]msg:{}", msg);
        buf.writeByte(msg.getMsgType());
        buf.writeByte(msg.getServeType());


        UserUtils.writeThirdUser(msg.getFrom(), msg.getPacketHead(), buf);
        UserUtils.writeThirdUser(msg.getTo(), msg.getPacketHead(), buf);

//        ByteBufUtils.writeUTF8String(buf, msg.getFrom());
//        ByteBufUtils.writeUTF8String(buf, msg.getTo());


        buf.writeLong(msg.getExpired());

        if (null == msg.getPushContentBody()) {
            buf.writeShort(0);
        } else {
            ByteBufUtils.writeUTF8StringPrefix2ByteLength(buf, msg.getPushContentBody());
        }

        if (null == msg.getMessageBody()) {
            buf.writeShort(0);
        } else {
            ByteBufUtils.writeUTF8StringPrefix2ByteLength(buf, msg.getMessageBody());
        }

        buf.writeLong(msg.getMsgId());
        if (null == msg.getCmsgId()) {
            buf.writeShort(0);
        } else {
            ByteBufUtils.writeUTF8StringPrefix2ByteLength(buf, msg.getCmsgId());
        }
    }

}

package com.handwin.codec;

import com.handwin.packet.*;
import com.handwin.utils.ByteBufUtils;
import com.handwin.utils.UserUtils;
import io.netty.buffer.ByteBuf;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;

@Component
public class SimpleMessageDecoderAndEncoder extends BasePacketDecodeAndEncoder<SimpleMessagePacket>
        implements InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(SimpleMessageDecoderAndEncoder.class);

    @Autowired
    private PacketCodecs packetCodecs;

    @Override
    public void register() {
        packetCodecs.register((int) SimpleMessagePacket.SIMPLE_MESSAGE_PACKET_TYPE, this);
    }

    public void afterPropertiesSet() throws Exception {
        register();
    }

    public SimpleMessageDecoderAndEncoder() {
    }

    @Override
    public void encode(SimpleMessagePacket msg, ByteBuf buf) {
        switch (msg.getPacketType()) {

            case TextMessagePacket.TEXT_MESSAGE_PACKET_TYPE: {
                buf.writeByte(TextMessagePacket.TEXT_MESSAGE_TYPE);

                //发给个人消息
                if (msg.getFromGroup() == null) {
                    buf.writeByte(TextMessagePacket.TO_USER | msg.getEntityType());
                    //ByteBufUtils.writeUTF8String(buf, msg.getFrom());

                    UserUtils.writeThirdUser(msg.getFrom(), msg.getPacketHead(), buf);

                } else {
                    //发送群组消息
                    buf.writeByte(TextMessagePacket.TO_GROUP | msg.getEntityType());

//                    ByteBufUtils.writeUTF8String(buf, msg.getFromGroup());
//                    ByteBufUtils.writeUTF8String(buf, msg.getFrom());

                    UserUtils.writeThirdUser(msg.getFromGroup(), msg.getPacketHead(), buf);
                    UserUtils.writeThirdUser(msg.getFrom(), msg.getPacketHead(), buf);


                }

                ByteBufUtils.writeByteArrayPrefix2ByteLength(buf, msg.getContent());

                if (null != msg.getMessageId()) {
                    buf.writeLong(msg.getMessageId());
                } else {
                    buf.writeLong(0l);
                }

                if (null != msg.getCmsgid()) {
                    ByteBufUtils.writeUTF8StringPrefix2ByteLength(buf, msg.getCmsgid());
                } else {
                    buf.writeShort(0);
                }

                break;
            }

            case ImageMessagePacket.IMAGE_MESSAGE_PACKET_TYPE: {
                buf.writeByte(ImageMessagePacket.IMAGE_URL_MESSAGE_TYPE);
                if (msg.getFromGroup() == null) {
                    buf.writeByte(ImageMessagePacket.TO_USER | msg.getEntityType());
                    //ByteBufUtils.writeUTF8String(buf, msg.getFrom());

                    UserUtils.writeThirdUser(msg.getFrom(), msg.getPacketHead(), buf);


                } else {
                    buf.writeByte(ImageMessagePacket.TO_GROUP | msg.getEntityType());

                    UserUtils.writeThirdUser(msg.getFromGroup(), msg.getPacketHead(), buf);
                    UserUtils.writeThirdUser(msg.getFrom(), msg.getPacketHead(), buf);

//                    ByteBufUtils.writeUTF8String(buf, msg.getFromGroup());
//                    ByteBufUtils.writeUTF8String(buf, msg.getFrom());
                }

                ByteBufUtils.writeByteArrayPrefix2ByteLength(buf, msg.getContent());

                if (null != msg.getMessageId()) {
                    buf.writeLong(msg.getMessageId());
                } else {
                    buf.writeLong(0l);
                }

                if (null != msg.getCmsgid()) {
                    ByteBufUtils.writeUTF8StringPrefix2ByteLength(buf, msg.getCmsgid());
                } else {
                    buf.writeShort(0);
                }

                break;
            }

            case VoiceMessagePacket.VOICE_MESSAGE_PACKET_TYPE: {
                buf.writeByte(VoiceMessagePacket.VOICE_MESSAGE_TYPE);
                if (msg.getFromGroup() == null) {
                    buf.writeByte(VoiceMessagePacket.TO_USER | msg.getEntityType());

                    UserUtils.writeThirdUser(msg.getFrom(), msg.getPacketHead(), buf);
                    //ByteBufUtils.writeUTF8String(buf, msg.getFrom());
                } else {
                    buf.writeByte(VoiceMessagePacket.TO_GROUP | msg.getEntityType());

                    UserUtils.writeThirdUser(msg.getFromGroup(), msg.getPacketHead(), buf);
                    UserUtils.writeThirdUser(msg.getFrom(), msg.getPacketHead(), buf);

//                    ByteBufUtils.writeUTF8String(buf, msg.getFromGroup());
//                    ByteBufUtils.writeUTF8String(buf, msg.getFrom());
                }

                ByteBufUtils.writeByteArrayPrefix2ByteLength(buf, msg.getContent());

                if (null != msg.getMessageId()) {
                    buf.writeLong(msg.getMessageId());
                } else {
                    buf.writeLong(0l);
                }

                if (null != msg.getCmsgid()) {
                    ByteBufUtils.writeUTF8StringPrefix2ByteLength(buf, msg.getCmsgid());
                } else {
                    buf.writeShort(0);
                }

                break;
            }

            case VideoMessagePacket.VIDEO_MESSAGE_PACKET_TYPE: {
                buf.writeByte(VideoMessagePacket.VIDEO_MESSAGE_TYPE);
                if (msg.getFromGroup() == null) {
                    buf.writeByte(VideoMessagePacket.TO_USER | msg.getEntityType());

                    UserUtils.writeThirdUser(msg.getFrom(), msg.getPacketHead(), buf);

                    //ByteBufUtils.writeUTF8String(buf, msg.getFrom());
                } else {
                    buf.writeByte(VideoMessagePacket.TO_GROUP | msg.getEntityType());

                    UserUtils.writeThirdUser(msg.getFromGroup(), msg.getPacketHead(), buf);
                    UserUtils.writeThirdUser(msg.getFrom(), msg.getPacketHead(), buf);


//                    ByteBufUtils.writeUTF8String(buf, msg.getFromGroup());
//                    ByteBufUtils.writeUTF8String(buf, msg.getFrom());
                }

                ByteBufUtils.writeByteArrayPrefix2ByteLength(buf, msg.getContent());
                if (null != msg.getMessageId()) {
                    buf.writeLong(msg.getMessageId());
                } else {
                    buf.writeLong(0l);
                }

                if (null != msg.getCmsgid()) {
                    ByteBufUtils.writeUTF8StringPrefix2ByteLength(buf, msg.getCmsgid());
                } else {
                    buf.writeShort(0);
                }

                break;
            }

            case MessageResponsePacket.MESSAGE_RESPONSE_PACKET_TYPE: {
                MessageResponsePacket messageResponsePacket = (MessageResponsePacket) msg;
                PacketHead head = new PacketHead();
                head.setTempId(messageResponsePacket.getTempId());
                messageResponsePacket.setPacketHead(head);
                buf.writeByte(MessageResponsePacket.STATUS_RESPONSE_MESSAGE_TYPE);
                buf.writeByte(messageResponsePacket.getMessageStatus().id());
                buf.writeLong(messageResponsePacket.getMessageId());
                String cmsgId = messageResponsePacket.getCmsgid();
                if (!StringUtils.isEmpty(cmsgId)) {
                    ByteBufUtils.writeByteArrayPrefix2ByteLength(buf, cmsgId.getBytes(Charset.forName("UTF-8")));
                }
                break;
            }

            case ForwardMessagePacket.FORWARD_MESSAGE_PACKET_TYPE: {
                ForwardMessagePacket forwardMessagePacket = (ForwardMessagePacket) msg;
                buf.writeByte(ForwardMessagePacket.FORWARD_MESSAGE_TYPE);
                buf.writeByte(forwardMessagePacket.isBoth() ? 0x02 : 0x01);
                //ByteBufUtils.writeUTF8String(buf, forwardMessagePacket.getFrom());

                UserUtils.writeThirdUser(forwardMessagePacket.getFrom(), msg.getPacketHead(), buf);


                ByteBufUtils.writeByteArrayPrefix2ByteLength(buf, forwardMessagePacket.getData());

                if (null != msg.getCmsgid()) {
                    ByteBufUtils.writeUTF8StringPrefix2ByteLength(buf, msg.getCmsgid());
                }
//                else {
//                    buf.writeShort(0);
//                }

                break;
            }

            default: {
                logger.debug("unknown simple message packet type {}", msg.getPacketType());
                break;
            }
        }
    }

    @Override
    public SimpleMessagePacket decode(ByteBuf buf, PacketHead head) {
        logger.debug("simple message packet encode.");
        SimpleMessagePacket result = null;
        if (!buf.isReadable(2)) {
            return result;
        }
        byte messageType = buf.readByte();
        byte serviceAndEntityType = buf.readByte();

        //取低4位
        byte messageServiceType = (byte) (serviceAndEntityType & 0x0F);
        byte entityType = (byte) (serviceAndEntityType & 0xF0);
        logger.debug("message type {}, message service type {}", messageType, messageServiceType);
        switch (messageType) {
            case TextMessagePacket.TEXT_MESSAGE_TYPE: {
                TextMessagePacket textMessagePacket = new TextMessagePacket();
                textMessagePacket.setMessageType(messageType);
                textMessagePacket.setMessageServiceType(messageServiceType);
                textMessagePacket.setEntityType(entityType);
                if (TextMessagePacket.TO_USER == messageServiceType) {
                    if (!buf.isReadable(32)) {
                        return null;
                    }
                    //String toUser = ByteBufUtils.readUTF8String(buf, 32);

                    String toUser = ByteBufUtils.readNewUTF8String(buf, 32);

                    textMessagePacket.setToUser(toUser);
                    if (buf.isReadable(SIZE_SHORT)) {
                        int len = buf.readUnsignedShort();
                        if (len > 0) {
                            if (buf.isReadable(len)) {
                                textMessagePacket.setContent(ByteBufUtils.readByteArray(buf, len));
                            } else {
                                logger.error("simple message packet data error.");
                                return null;
                            }
                        }
                    }
                } else if (TextMessagePacket.TO_GROUP == messageServiceType) {
                    if (!buf.isReadable(32)) {
                        return null;
                    }
                    //String toGroup = ByteBufUtils.readUTF8String(buf, 32);
                    String toGroup = ByteBufUtils.readNewUTF8String(buf, 32);

                    textMessagePacket.setFromGroup(toGroup);
                    textMessagePacket.setToGroup(toGroup);
                    if (buf.isReadable(SIZE_SHORT)) {
                        int len = buf.readUnsignedShort();
                        if (len > 0) {
                            if (buf.isReadable(len)) {
                                textMessagePacket.setContent(ByteBufUtils.readByteArray(buf, len));
                            } else {
                                logger.error("simple message packet data error.");
                                return null;
                            }
                        }
                    }
                } else {
                    logger.error("unknown message service type {}", messageServiceType);
                    break;
                }

                if (buf.isReadable()) {
                    textMessagePacket.setMsgFlag(buf.readByte());
                }

                if (buf.isReadable(SIZE_SHORT)) {
                    int len = buf.readUnsignedShort();
                    if (len > 0) {
                        if (buf.isReadable(len)) {
                            textMessagePacket.setCmsgid(ByteBufUtils.readUTF8String(buf, len));
                        } else {
                            logger.error("simple message packet data error.");
                            return null;
                        }
                    }
                }

                result = textMessagePacket;
                result.setPacketHead(head);
                result.setTempId(head.getTempId());
                break;
            }

            case ImageMessagePacket.IMAGE_URL_MESSAGE_TYPE: {
                logger.debug("encode image url message.");
                ImageMessagePacket imageMessagePacket = new ImageMessagePacket();
                imageMessagePacket.setMessageType(messageType);
                imageMessagePacket.setMessageServiceType(messageServiceType);
                imageMessagePacket.setEntityType(entityType);
                imageMessagePacket.setPacketHead(head);
                imageMessagePacket.setTempId(head.getTempId());

                if (ImageMessagePacket.TO_USER == messageServiceType) {
                    if (!buf.isReadable(32)) {
                        return null;
                    }
                    //String toUser = ByteBufUtils.readUTF8String(buf, 32);

                    String toUser = ByteBufUtils.readNewUTF8String(buf, 32);
                    imageMessagePacket.setToUser(toUser);
                    if (!buf.isReadable(SIZE_SHORT)) {
                        return imageMessagePacket;
                    }
                    int len = buf.readUnsignedShort();
                    if (len > 0) {
                        if (buf.isReadable(len)) {
                            imageMessagePacket.setContent(ByteBufUtils.readByteArray(buf, len));
                        } else {
                            logger.error("image message packet data error.");
                            return null;
                        }
                    }
                } else if (ImageMessagePacket.TO_GROUP == messageServiceType) {
                    if (!buf.isReadable(32)) {
                        return null;
                    }
                    //String toGroup = ByteBufUtils.readUTF8String(buf, 32);


                    String toGroup = ByteBufUtils.readNewUTF8String(buf, 32);
                    imageMessagePacket.setFromGroup(toGroup);
                    imageMessagePacket.setToGroup(toGroup);
                    if (!buf.isReadable(SIZE_SHORT)) {
                        return imageMessagePacket;
                    }
                    int len = buf.readUnsignedShort();
                    if (len > 0) {
                        if (buf.isReadable(len)) {
                            imageMessagePacket.setContent(ByteBufUtils.readByteArray(buf, len));
                        } else {
                            logger.error("group image message packet data error.");
                            return null;
                        }
                    }
                } else {
                    logger.error("unknown image url message service type {}", messageServiceType);
                    break;
                }

                if (!buf.isReadable()) {
                    return imageMessagePacket;
                }
                imageMessagePacket.setMsgFlag(buf.readByte());

                if (!buf.isReadable(SIZE_SHORT)) {
                    return imageMessagePacket;
                }
                int len = buf.readUnsignedShort();
                if (len > 0) {
                    if (buf.isReadable(len)) {
                        imageMessagePacket.setCmsgid(ByteBufUtils.readUTF8String(buf, len));
                    } else {
                        logger.error("image message packet data error.");
                        return null;
                    }
                }
                result = imageMessagePacket;
                break;
            }

            case VoiceMessagePacket.VOICE_MESSAGE_TYPE: {
                logger.debug("encode voice message");
                VoiceMessagePacket voicePacket = new VoiceMessagePacket();
                voicePacket.setMessageType(messageType);
                voicePacket.setMessageServiceType(messageServiceType);
                voicePacket.setEntityType(entityType);
                voicePacket.setPacketHead(head);
                voicePacket.setTempId(head.getTempId());

                if (VoiceMessagePacket.TO_USER == messageServiceType) {
                    if (!buf.isReadable(32)) {
                        return null;
                    }
                    //String toUser = ByteBufUtils.readUTF8String(buf, 32);

                    String toUser = ByteBufUtils.readNewUTF8String(buf, 32);
                    voicePacket.setToUser(toUser);
                    if (!buf.isReadable(SIZE_SHORT)) {
                        return voicePacket;
                    }
                    int len = buf.readUnsignedShort();
                    if (len > 0) {
                        if (buf.isReadable(len)) {
                            voicePacket.setContent(ByteBufUtils.readByteArray(buf, len));
                        } else {
                            logger.error("voice message packet data error.");
                            return null;
                        }
                    }
                } else if (VoiceMessagePacket.TO_GROUP == messageServiceType) {
                    if (!buf.isReadable(32)) {
                        return null;
                    }
                    //String toGroup = ByteBufUtils.readUTF8String(buf, 32);

                    String toGroup = ByteBufUtils.readNewUTF8String(buf, 32);
                    voicePacket.setFromGroup(toGroup);
                    voicePacket.setToGroup(toGroup);
                    if (!buf.isReadable(SIZE_SHORT)) {
                        return voicePacket;
                    }
                    int len = buf.readUnsignedShort();
                    if (len > 0) {
                        if (buf.isReadable(len)) {
                            voicePacket.setContent(ByteBufUtils.readByteArray(buf, len));
                        } else {
                            logger.error("group voice message packet data error.");
                            return null;
                        }
                    }
                } else {
                    logger.error("unknown voice message service type {}", messageServiceType);
                    break;
                }
                if (!buf.isReadable()) {
                    return voicePacket;
                }
                voicePacket.setMsgFlag(buf.readByte());
                if (!buf.isReadable(SIZE_SHORT)) {
                    return voicePacket;
                }
                int len = buf.readUnsignedShort();
                if (len > 0) {
                    if (buf.isReadable(len)) {
                        voicePacket.setCmsgid(ByteBufUtils.readUTF8String(buf, len));
                    } else {
                        logger.error("voice message packet data error.");
                        return null;
                    }
                }
                result = voicePacket;
                break;
            }

            case VideoMessagePacket.VIDEO_MESSAGE_TYPE: {
                logger.debug("encode voice message");
                VideoMessagePacket videoPacket = new VideoMessagePacket();
                videoPacket.setMessageType(messageType);
                videoPacket.setMessageServiceType(messageServiceType);
                videoPacket.setEntityType(entityType);
                videoPacket.setPacketHead(head);
                videoPacket.setTempId(head.getTempId());
                if (VoiceMessagePacket.TO_USER == messageServiceType) {
                    if (!buf.isReadable(32)) {
                        return null;
                    }
                    //String toUser = ByteBufUtils.readUTF8String(buf, 32);


                    String toUser = ByteBufUtils.readNewUTF8String(buf, 32);
                    videoPacket.setToUser(toUser);
                    if (!buf.isReadable(SIZE_SHORT)) {
                        return videoPacket;
                    }
                    int len = buf.readUnsignedShort();
                    if (len > 0) {
                        if (buf.isReadable(len)) {
                            videoPacket.setContent(ByteBufUtils.readByteArray(buf, len));
                        } else {
                            logger.error("video message packet data error.");
                            return null;
                        }
                    }
                } else if (VoiceMessagePacket.TO_GROUP == messageServiceType) {
                    if (!buf.isReadable(32)) {
                        return null;
                    }
                    //String toGroup = ByteBufUtils.readUTF8String(buf, 32);


                    String toGroup = ByteBufUtils.readNewUTF8String(buf, 32);
                    videoPacket.setFromGroup(toGroup);
                    videoPacket.setToGroup(toGroup);
                    if (!buf.isReadable(SIZE_SHORT)) {
                        return videoPacket;
                    }
                    int len = buf.readUnsignedShort();
                    if (len > 0) {
                        if (buf.isReadable(len)) {
                            videoPacket.setContent(ByteBufUtils.readByteArray(buf, len));
                        } else {
                            logger.error("group video message packet data error.");
                            return null;
                        }
                    }
                } else {
                    logger.error("unknown video url message service type {}", messageServiceType);
                    break;
                }

                if (!buf.isReadable()) {
                    return videoPacket;
                }
                videoPacket.setMsgFlag(buf.readByte());
                if (!buf.isReadable(SIZE_SHORT)) {
                    return videoPacket;
                }
                int len = buf.readUnsignedShort();
                if (len > 0) {
                    if (buf.isReadable(len)) {
                        videoPacket.setCmsgid(ByteBufUtils.readUTF8String(buf, len));
                    } else {
                        logger.error("video message packet data error.");
                        return null;
                    }
                }
                result = videoPacket;
                break;
            }

            case ForwardMessagePacket.FORWARD_MESSAGE_TYPE: {
                ForwardMessagePacket forwardMessagePacket = new ForwardMessagePacket();
                forwardMessagePacket.setMessageType(messageType);
                forwardMessagePacket.setMessageServiceType(messageServiceType);
                forwardMessagePacket.setEntityType(entityType);
                forwardMessagePacket.setPacketHead(head);
                forwardMessagePacket.setTempId(head.getTempId());
                if (!buf.isReadable(32)) {
                    return null;
                }
                //String toUser = ByteBufUtils.readUTF8String(buf, 32);


                String toUser = ByteBufUtils.readNewUTF8String(buf, 32);
                forwardMessagePacket.setToUser(toUser);
                forwardMessagePacket.setBoth(messageServiceType == 0x02);
                if (!buf.isReadable(SIZE_SHORT)) {
                    return forwardMessagePacket;
                }
                int len = buf.readUnsignedShort();
                if (len > 0) {
                    if (buf.isReadable(len)) {
                        forwardMessagePacket.setData(ByteBufUtils.readByteArray(buf, len));
                    } else {
                        logger.error("forward message packet data error.");
                        return null;
                    }
                }

                if (!buf.isReadable()) {
                    return forwardMessagePacket;
                }
                forwardMessagePacket.setMsgFlag(buf.readByte());
                if (!buf.isReadable(SIZE_SHORT)) {
                    return forwardMessagePacket;
                }
                len = buf.readUnsignedShort();
                if (len > 0) {
                    if (buf.isReadable(len)) {
                        forwardMessagePacket.setCmsgid(ByteBufUtils.readUTF8String(buf, len));
                    } else {
                        logger.error("forward message packet data error.");
                        return null;
                    }
                }
                result = forwardMessagePacket;
                break;
            }

            case StatusMessagePacket.STATUS_MESSAGE_TYPE: {
                StatusMessagePacket statusMessagePacket = new StatusMessagePacket();
                statusMessagePacket.setMessageType(messageType);
                statusMessagePacket.setMessageServiceType(messageServiceType);
                statusMessagePacket.setEntityType(entityType);
                statusMessagePacket.setMessageStatus(MessageStatus.getInstance(messageServiceType));
                if (!buf.isReadable(SIZE_LONG)) {
                    return null;
                }
                statusMessagePacket.setMessageId(buf.readLong());
                result = statusMessagePacket;
                result.setPacketHead(head);
                result.setTempId(head.getTempId());
                if (buf.isReadable(SIZE_SHORT)) {
                    int cmsgIdLength = buf.readUnsignedShort();
                    result.setCmsgid(ByteBufUtils.readUTF8String(buf, cmsgIdLength));
                }
                break;
            }


            case MessageResponsePacket.STATUS_RESPONSE_MESSAGE_TYPE: {
                MessageResponsePacket messageResponsePacket = new MessageResponsePacket();
                messageResponsePacket.setMessageType(messageType);
                messageResponsePacket.setMessageServiceType(messageServiceType);
                messageResponsePacket.setEntityType(entityType);
                messageResponsePacket.setMessageStatus(MessageStatus.getInstance(messageServiceType));
                if (!buf.isReadable(SIZE_LONG)) {
                    return null;
                }
                messageResponsePacket.setMessageId(buf.readLong());
                result = messageResponsePacket;
                result.setPacketHead(head);
                result.setTempId(head.getTempId());
                if (buf.isReadable(SIZE_SHORT)) {
                    int cmsgIdLength = buf.readUnsignedShort();
                    result.setCmsgid(ByteBufUtils.readUTF8String(buf, cmsgIdLength));
                }
                break;
            }

            default: {
                logger.error("unknown simple message packet, message type {},", messageType);
                break;
            }
        }

        if (null != result) result.setPacketHead(head);
        return result;
    }
}

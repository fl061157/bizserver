package com.handwin.packet;

import com.handwin.protocal.v5.codec.V5GenericPacket;
import com.handwin.protocal.v5.codec.V5PacketHead;
import com.handwin.utils.UserUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Map;

/**
 * Created by Danny on 2014-11-19.
 */
public class GenericPacket extends AbstractBasePacket {

    private V5GenericPacket v5GenericPacket;

    public final static byte GENERIC_PACKET_TYPE = 0x0F;
    public static final int BODY_TYPE_BYTES = 1;
    public static final int BODY_TYPE_GMAP = 2;


    public GenericPacket() {
        setPacketType(GENERIC_PACKET_TYPE);
    }

    public V5GenericPacket getV5GenericPacket() {
        return v5GenericPacket;
    }

    public void setV5GenericPacket(V5GenericPacket v5GenericPacket) {
        this.v5GenericPacket = v5GenericPacket;
    }

    @Override
    public void setSrcMsgBytes(byte[] srcMsgBytes) {
        super.setSrcMsgBytes(srcMsgBytes);
        if (null != v5GenericPacket) {
            v5GenericPacket.setSrcMsgBytes(srcMsgBytes);
        }
    }

    private byte[] bodySrcBytes;
    private Map bodyMap;

    private int bodyType;


    public GenericPacket copy(PacketHead packetHead) {
        GenericPacket genericPacket = new GenericPacket();
        genericPacket.setPacketHead(packetHead);
        genericPacket.setBodyType(this.bodyType);
        if (this.bodyType == BODY_TYPE_BYTES) {
            genericPacket.bodySrcBytes = this.bodySrcBytes; //TODO Copy
        } else if (this.bodyType == BODY_TYPE_GMAP) {
            genericPacket.bodySrcBytes = this.bodySrcBytes;
            genericPacket.bodyMap = this.bodyMap;//TODO Copy
        }
        return genericPacket;
    }


    public GenericPacket(int bodyType) {
        this.bodyType = bodyType;
    }

    public byte[] getBodySrcBytes() {
        return bodySrcBytes;
    }

    public void setBodySrcBytes(byte[] bodySrcBytes) {
        this.bodySrcBytes = bodySrcBytes;
    }

    public Map getBodyMap() {
        return bodyMap;
    }

    public void setBodyMap(Map bodyMap) {
        this.bodyMap = bodyMap;
    }

    public int getBodyType() {
        return bodyType;
    }

    public void setBodyType(int bodyType) {
        this.bodyType = bodyType;
    }

    @Override
    public void attachThirdUserId(Integer appID) {

        V5GenericPacket vp = this.getV5GenericPacket();

        if (vp == null) return;

        V5PacketHead vph = vp.getPacketHead();

        if (vph == null) return;

        if (StringUtils.isNotBlank(vph.getFrom())) {
            vph.setFrom(UserUtils.attachThirdUserID(vph.getFrom(), appID));
        }

        if (StringUtils.isNotBlank(vph.getTo())) {
            if (!vph.getService().contains("live")) {
                vph.setTo(UserUtils.attachThirdUserID(vph.getTo(), appID));
            }
        }

    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
    }

}

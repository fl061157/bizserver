package com.handwin.server;

import com.handwin.codec.CommandDecoderAndEncoder;
import com.handwin.packet.UDPServerPacket;
import com.handwin.utils.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.buffer.UnpooledDirectByteBuf;
import org.junit.Test;

import java.util.Arrays;

public class AmqpLocalChannelImplTest {

    CommandDecoderAndEncoder encoder = new CommandDecoderAndEncoder();

    @Test
    public void testWrite() throws Exception {
        UDPServerPacket udpServerPacket = new UDPServerPacket();
        udpServerPacket.setFlag((byte) 0x01);
        udpServerPacket.setRoomId("call_0bfa9a59f49b41789f83bf017210bfa9a59f49b41789f83bf01721b9ac1");

        udpServerPacket.setUdpInfo(new UDPServerPacket.UdpInfo[]{new UDPServerPacket.UdpInfo("192.168.1.1", 7101, "1001"), new UDPServerPacket.UdpInfo("192.168.1.1", 7101, "1002")});

        ByteBuf byteBuf = Unpooled.buffer();
        encoder.encode(udpServerPacket, byteBuf);

        byte[] result = new byte[byteBuf.writerIndex() - byteBuf.readerIndex()];
        byteBuf.readBytes(result);
        System.out.println(Arrays.toString(result));

    }

    @Test
    public void testByteBuffer() {
        byte[] bytes =new byte[]{
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
        } ;

        ByteBuf buf = Unpooled.wrappedBuffer(bytes);

        String roomId = ByteBufUtils.readUTF8String(buf, 64);
        System.out.println("roomId:"+roomId);
        System.out.println(null == roomId);
        System.out.println("".equals(roomId));


        final StringBuilder sb = new StringBuilder("GameRoom{");
        sb.append(", roomId='").append(roomId).append('\'');
        sb.append('}');
        sb.toString();

        System.out.println(sb.toString());
    }
}
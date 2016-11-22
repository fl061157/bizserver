package com.handwin.generic;

import com.handwin.codec.PacketCodecs;
import com.handwin.packet.BasePacket;
import com.handwin.packet.GenericPacket;
import com.handwin.packet.PacketHead;
import com.handwin.protocal.v5.codec.V5GenericPacket;
import com.handwin.protocal.v5.codec.V5PacketHead;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.junit.*;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Map;

import static com.handwin.packet.GenericPacket.GENERIC_PACKET_TYPE;

/**
 * Created by Danny on 2014-12-02.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/xmls/config-test-generic-packet.xml")
public class GenericPacketTest {

    private static final Logger logger = LoggerFactory.getLogger(GenericPacketTest.class);

    @Autowired
    PacketCodecs packetCodecs;

    @BeforeClass
    public static void beforeClass() {
    }

    @Before
    public void before() {
    }

    @After
    public void after() {
    }

    @Test
    public void encodeDecodeTest() {
        V5PacketHead head = new V5PacketHead();
        V5GenericPacket p = new V5GenericPacket();
        p.setPacketHead(head);

        head.setVersion((byte) 5);
        head.setHead((byte) 0xb7);
        head.setPacketType(GENERIC_PACKET_TYPE);
        head.setTimestamp(System.currentTimeMillis());

        head.setAppId(0);
        head.setFrom("from");
        head.setTo("to");
        head.setFromRegion("0086");
        head.setToRegion("0001");
        head.setMessageID("message-id");
        head.setClientReceivedConfirm(true);

        Map bodyMap = new ListOrderedMap();
        bodyMap.put("content", "content");

        p.setBodyMap(bodyMap);

        ByteBuf encodedBuf = Unpooled.buffer();
        GenericPacket packet = new GenericPacket();
        packet.setV5GenericPacket(p);
        packetCodecs.encode(5, packet, encodedBuf);

        logger.debug("encoded len {}, bytes : {}", encodedBuf.readableBytes(), ByteBufUtil.hexDump(encodedBuf));

        BasePacket basePacket = packetCodecs.decode(encodedBuf);

        Assert.assertNotNull(basePacket);
        Assert.assertTrue(basePacket instanceof GenericPacket);
        GenericPacket p2 = (GenericPacket) basePacket;
        PacketHead head2 = p2.getPacketHead();
        Assert.assertNotNull(head2);

        Assert.assertTrue(EqualsBuilder.reflectionEquals(head, head2, false));

        encodedBuf.release();
    }

}

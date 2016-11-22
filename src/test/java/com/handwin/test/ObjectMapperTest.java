package com.handwin.test;

import com.handwin.entity.UDPServerPacket2;
import com.handwin.packet.UDPServerPacket;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.PropertyNamingStrategy;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by piguangtao on 15/3/10.
 */
public class ObjectMapperTest {

    private static ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.setPropertyNamingStrategy(
                PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
        objectMapper.configure(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS,false);
        objectMapper.setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
    }

    @Test
    public void testUdpServerPacket2Json() throws IOException {
        String roomId = "call_981b5cafb5e04b74b5ea2f990ef981b5cafb5e04b74b5ea2f990ef7472a";
        UDPServerPacket2 udpServerPacket2 = new UDPServerPacket2();
        udpServerPacket2.setId1("c4ca4238a0b923820dcc509a6f75849b_0");
        UDPServerPacket udpServerPacket1 = new UDPServerPacket();
        udpServerPacket1.setFlag((byte)17);
        udpServerPacket1.setRoomId("call_981b5cafb5e04b74b5ea2f990ef981b5cafb5e04b74b5ea2f990ef7472a");

        UDPServerPacket.UdpInfo udpInfo = new UDPServerPacket.UdpInfo();
        udpInfo.setIp("114.215.193.49");
        udpInfo.setNodeId("udp_114215193049_7007");
        udpInfo.setPort(7007);
        udpServerPacket1.setUdpInfo(new UDPServerPacket.UdpInfo[]{udpInfo});

        udpServerPacket2.setUdpServerPacket1(udpServerPacket1);


        udpServerPacket2.setId2("c81e728d9d4c2f636f067f89cc14862c_0");
        UDPServerPacket udpServerPacket11 = new UDPServerPacket();
        udpServerPacket11.setFlag((byte)17);
        udpServerPacket11.setRoomId("call_981b5cafb5e04b74b5ea2f990ef981b5cafb5e04b74b5ea2f990ef7472a");
        udpServerPacket11.setUdpInfo(new UDPServerPacket.UdpInfo[]{udpInfo});
        udpServerPacket2.setUdpServerPacket2(udpServerPacket11);

        String json = objectMapper.writeValueAsString(udpServerPacket2);

        Map<String,String> hashTest = new HashMap<>();
        hashTest.put("1","a");
//        String json = objectMapper.writeValueAsString(hashTest);

        System.out.println(udpServerPacket2);
        System.out.println(json);
    }
}

package com.handwin.service;

import com.handwin.entity.UDPServerPacket2;
import com.handwin.packet.UDPServerPacket;
import com.handwin.utils.SystemConstant;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.GenericXmlApplicationContext;

import java.io.File;
import java.util.concurrent.CountDownLatch;

/**
 * Created by piguangtao on 15/3/10.
 */
public class CallServiceTest {

    @Autowired
    private CallService callService;

    CountDownLatch latch = null;

    @Before
    public void before() throws InterruptedException {
        System.setProperty("spring.profiles.active", "test-mock-cn");

        latch = new CountDownLatch(1);

        initSystem(latch);

    }


    @Test
    public void testStoreUdpServerInfo(){
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


        callService.storeUdpServerInfo(roomId,udpServerPacket2);

        UDPServerPacket2 restoreUdpServerPacket = callService.getUdpServerInfoFromCache(roomId);

        Assert.assertNotNull(restoreUdpServerPacket);
        Assert.assertEquals(restoreUdpServerPacket.getId1(),udpServerPacket2.getId1());
        Assert.assertEquals(restoreUdpServerPacket.getId2(),udpServerPacket2.getId2());
        Assert.assertNotNull(restoreUdpServerPacket.getUdpServerPacket1());
        Assert.assertNotNull(restoreUdpServerPacket.getUdpServerPacket2());

        Assert.assertEquals(restoreUdpServerPacket.getUdpServerPacket1().getRoomId(),udpServerPacket2.getUdpServerPacket1().getRoomId());
        Assert.assertEquals(restoreUdpServerPacket.getUdpServerPacket1().getFlag(),udpServerPacket2.getUdpServerPacket1().getFlag());
        Assert.assertNotNull(restoreUdpServerPacket.getUdpServerPacket1().getUdpInfo());
        Assert.assertEquals(restoreUdpServerPacket.getUdpServerPacket1().getUdpInfo().length,1);
        Assert.assertEquals(restoreUdpServerPacket.getUdpServerPacket1().getUdpInfo()[0].getIp(),udpServerPacket2.getUdpServerPacket1().getUdpInfo()[0].getIp());
        Assert.assertEquals(restoreUdpServerPacket.getUdpServerPacket1().getUdpInfo()[0].getNodeId(),udpServerPacket2.getUdpServerPacket1().getUdpInfo()[0].getNodeId());
        Assert.assertEquals(restoreUdpServerPacket.getUdpServerPacket1().getUdpInfo()[0].getPort(),udpServerPacket2.getUdpServerPacket1().getUdpInfo()[0].getPort());

        Assert.assertEquals(restoreUdpServerPacket.getUdpServerPacket2().getRoomId(),udpServerPacket2.getUdpServerPacket2().getRoomId());
        Assert.assertEquals(restoreUdpServerPacket.getUdpServerPacket2().getFlag(),udpServerPacket2.getUdpServerPacket2().getFlag());
        Assert.assertNotNull(restoreUdpServerPacket.getUdpServerPacket2().getUdpInfo());
        Assert.assertEquals(restoreUdpServerPacket.getUdpServerPacket2().getUdpInfo().length,1);
        Assert.assertEquals(restoreUdpServerPacket.getUdpServerPacket2().getUdpInfo()[0].getIp(),udpServerPacket2.getUdpServerPacket2().getUdpInfo()[0].getIp());
        Assert.assertEquals(restoreUdpServerPacket.getUdpServerPacket2().getUdpInfo()[0].getNodeId(),udpServerPacket2.getUdpServerPacket2().getUdpInfo()[0].getNodeId());
        Assert.assertEquals(restoreUdpServerPacket.getUdpServerPacket2().getUdpInfo()[0].getPort(),udpServerPacket2.getUdpServerPacket2().getUdpInfo()[0].getPort());


    }


    private void initSystem(CountDownLatch latch) throws InterruptedException {
        String nodeId = System.getProperty("node.id");
        if(null == nodeId) nodeId = String.valueOf(SystemConstant.NODE_ID_DEFAULT);
        String nodeIp = System.getProperty("node.ip");
        if(null == nodeIp) nodeIp = SystemConstant.NODE_IP_DEFAULT;
        System.setProperty("LOG_PREFIX", String.format("[biz:%s]", nodeId));
        System.setProperty("LOG_FLUME_IP", nodeIp);

        String prefix = "classpath:";
        String loading = "config-loading.xml";
        String dir = "xmls";

        System.setProperty("spring.profiles.active", "test-mock-cn");
        final GenericXmlApplicationContext context = new GenericXmlApplicationContext();

        context.load(prefix + dir + File.separatorChar + loading);
        context.refresh();

        new Thread(() -> {

            try {
                Thread.sleep(10 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            callService = (CallService) context.getBean("callService");

            latch.countDown();

        }).start();

//        new Thread(() -> context.refresh()).start();

        latch.await();

    }
}

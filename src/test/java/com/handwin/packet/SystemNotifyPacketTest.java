package com.handwin.packet;

import org.junit.Test;

/**
 * Created by piguangtao on 16/1/9.
 */
public class SystemNotifyPacketTest {

    @Test
    public void testIsGroup() throws Exception {
        SystemNotifyPacket systemNotifyPacket = new SystemNotifyPacket();
        systemNotifyPacket.setServeType((byte) 49);
        systemNotifyPacket.isGroup();
    }
}
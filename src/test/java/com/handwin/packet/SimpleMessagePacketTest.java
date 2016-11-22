package com.handwin.packet;

import junit.framework.Assert;
import org.junit.Test;

/**
 * Created by piguangtao on 16/3/25.
 */
public class SimpleMessagePacketTest {

    @Test
    public void testSetCmsgid() throws Exception {
        SimpleMessagePacket simpleMessagePacket = new SimpleMessagePacket();
        String cmsgId = "7eda6e5e98c3467ba7f2e3a43e46840f٠٠٠٣";
        String expectId = "7eda6e5e98c3467ba7f2e3a43e46840f9999";
        simpleMessagePacket.setCmsgid(cmsgId);
        Assert.assertEquals(expectId, simpleMessagePacket.getCmsgid());

    }
}
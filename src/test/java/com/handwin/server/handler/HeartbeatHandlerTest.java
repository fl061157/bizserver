package com.handwin.server.handler;

import com.handwin.entity.ChannelInfo;
import com.handwin.entity.User;
import com.handwin.packet.*;
import com.handwin.server.Channel;
import com.handwin.server.proto.ChannelAction;
import com.handwin.utils.SystemConstant;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.refEq;
import static org.mockito.Mockito.*;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/xmls/config-test-loading.xml")
public class HeartbeatHandlerTest extends TestCase {
    private static final Logger logger = LoggerFactory.getLogger(HeartbeatHandlerTest.class);

    @InjectMocks
    @Autowired
    private HeartbeatHandler heartbeatHandler;

    HeartbeatPacket heartbeatPacket;

    User user;

    @BeforeClass
    public static void beforeClass() {
        System.setProperty("spring.profiles.active", "dev");
    }

    @Before
    public void before() {
        heartbeatPacket = new HeartbeatPacket();
        heartbeatPacket.setChannelMode(ChannelMode.BACKGROUND);

        user = new User();
        user.setUserType(0);
        user.setId("cb5d4d14a83a87f6ef68118050ae0883");
    }

    @Test
    public void testLocalUserHeartbeat() throws Exception {
        logger.info(name.getMethodName() + " starting");
        Channel testChannel = mock(Channel.class);
        ChannelInfo chi = new ChannelInfo();
        chi.setUserId(user.getId());
        chi.setAppID(user.getAppId());
        when(testChannel.getChannelInfo()).thenReturn(chi);
        mockFindUser(user);
        mockLocalUser(true);
        // non local user

        heartbeatHandler.handle(testChannel, heartbeatPacket);
        verify(onlineStatusService).channelHeartBeat(chi, user.getId(), (short)user.getAppId(), 0);
    }

    /*
    @Test
    public void testNonlocalUserHeartbeat() throws Exception {
        logger.info(name.getMethodName() + " starting");
        Channel testChannel = mock(Channel.class);
        when(testChannel.getUser()).thenReturn(user);
        mockFindUser(user);
        // non local user
        mockLocalUser(false);

        heartbeatHandler.handle(testChannel, heartbeatPacket);
        verify(proxyMessageSender).write(user.getCountrycode(), heartbeatPacket);
    }
    */
}

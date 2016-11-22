package com.handwin.server.handler;

import com.handwin.entity.ChannelInfo;
import com.handwin.entity.User;
import com.handwin.packet.*;
import com.handwin.server.Channel;
import com.handwin.server.proto.ChannelAction;
import com.handwin.utils.SystemConstant;
import io.netty.channel.ChannelInitializer;
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

import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.refEq;
import static org.mockito.Mockito.*;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/xmls/config-test-loading.xml")
public class LogoutHandlerTest extends TestCase {
    private static final Logger logger = LoggerFactory.getLogger(LogoutHandlerTest.class);

    @InjectMocks
    @Autowired
    private LogoutHandler logoutHandler;

    LogoutPacket logoutPacket;

    User user;

    @BeforeClass
    public static void beforeClass() {
        System.setProperty("spring.profiles.active", "dev");
    }

    @Before
    public void before() {
        logoutPacket = new LogoutPacket();
        logoutPacket.setChannelMode(ChannelMode.UNKNOWN);

        user = new User();
        user.setUserType(0);
        user.setId("cb5d4d14a83a87f6ef68118050ae0883");
    }

    @Test
    public void testLogoutDelChannel() throws Exception {
        logger.info(name.getMethodName() + " starting");
        Channel testChannel = mock(Channel.class);
        mockLocalUser(true);
        ChannelInfo chi = new ChannelInfo();
        when(testChannel.getChannelInfo()).thenReturn(chi);

        logoutHandler.handle(testChannel, logoutPacket);
        verify(onlineStatusService).delChannel(anyString(), anyInt(), anyString(), anyString());
    }

    @Test
    public void testLogoutUpdateChannel() throws Exception {
        logger.info(name.getMethodName() + " starting");
        Channel testChannel = mock(Channel.class);
        mockLocalUser(true);
        ChannelInfo chi = new ChannelInfo();
        chi.setUserId(user.getId());
        chi.setAppID(user.getAppId());
        when(testChannel.getChannelInfo()).thenReturn(chi);
        logoutPacket.setChannelMode(ChannelMode.BACKGROUND);

        logoutHandler.handle(testChannel, logoutPacket);
        //assertThat(chi.getChannelMode(), eq(ChannelMode.BACKGROUND.getValue()));
        verify(onlineStatusService).changeChannelMode(chi, user.getId(), user.getAppId(), 0);
    }

    /*
    @Test
    public void testNonlocalUserLogout() throws Exception {
        logger.info(name.getMethodName() + " starting");
        Channel testChannel = mock(Channel.class);
        when(testChannel.getUser()).thenReturn(user);
        // non local user
        mockLocalUser(false);

        logoutHandler.handle(testChannel, logoutPacket);
        verify(proxyMessageSender).write(user.getCountrycode(), logoutPacket);
    }
    */
}

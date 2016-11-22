package com.handwin.server.handler;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.refEq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

import com.handwin.entity.ChannelInfo;
import com.handwin.entity.User;
import com.handwin.packet.ChannelMode;
import com.handwin.packet.LoginPacket;
import com.handwin.packet.LoginResponsePacket;
import com.handwin.packet.LoginStatus;
import com.handwin.packet.PacketHead;
import com.handwin.server.Channel;
import com.handwin.server.proto.ChannelAction;
import com.handwin.utils.SystemConstant;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/xmls/config-test-loading.xml")
public class LoginHandlerTest extends TestCase {
    private static final Logger logger = LoggerFactory.getLogger(LoginHandlerTest.class);

    @InjectMocks
    @Autowired
    private LoginHandler loginHandler;

    LoginPacket loginPacket;

    User user;

    @BeforeClass
    public static void beforeClass() {
        System.setProperty("spring.profiles.active", "dev");
    }

    @Before
    public void before() {
        loginPacket = new LoginPacket();
        loginPacket.setSessionId("a5eac9dc92b54b42989abbe00438209a");
        loginPacket.setChannelMode(ChannelMode.FOREGROUND);
        PacketHead packetHead = new PacketHead();
        packetHead.setAppId((short) 0);
        loginPacket.setPacketHead(packetHead);
        loginPacket.setLanguage("cn");
        loginPacket.setTimeZone("+08");

        user = new User();
        user.setUserType(0);
        user.setId("cb5d4d14a83a87f6ef68118050ae0883");
    }

    @Test
    public void testSystemUserLogin() throws Exception {
        logger.info(name.getMethodName() + " starting");
        Channel testChannel = mock(Channel.class);
        when(testChannel.getChannelInfo()).thenReturn(new ChannelInfo());

        user.setUserType(SystemConstant.USER_TYPE_SYSTEMACCOUNT);
        mockAuthorizeUser(user);

        loginHandler.handle(testChannel, loginPacket);
        verify(onlineStatusService).addChannel(user, testChannel);
        verify(testChannel).changeMode(loginPacket.getChannelMode());
        verify(testChannel).write(refEq(new LoginResponsePacket(LoginStatus.SUCCESS)), eq(ChannelAction.UDAPTE), eq(ChannelAction.SEND));
    }

    /*
    @Test
    public void testNonlocalUserLogin() throws Exception {
        logger.info(name.getMethodName() + " starting");
        Channel testChannel = mock(Channel.class);
        mockAuthorizeUser(user);
        // non local user
        mockLocalUser(false);

        loginHandler.handle(testChannel, loginPacket);
        verify(proxyMessageSender).write(user.getCountrycode(), loginPacket);
    }
    */

    @Test
    public void testLocalUserWithSameSessLogin() throws Exception {
        logger.info(name.getMethodName() + " starting");
        testLocalUserWithSessLogin(loginPacket.getSessionId());
    }

    @Test
    public void testLocalUserWithDiffSessLogin() throws Exception {
        logger.info(name.getMethodName() + " starting");
        testLocalUserWithSessLogin("");
    }

    private void testLocalUserWithSessLogin(String sessionId) throws Exception {
        mockAuthorizeUser(user);
        Channel newChannel = mock(Channel.class);
        Channel oldChannel = mock(Channel.class);
        ChannelInfo chi = new ChannelInfo();
        ChannelInfo oldChi = new ChannelInfo();

        chi.setSessonId(sessionId);
        chi.setNodeId("tcp_01");
        chi.setUuid("9338b44-787d-4bc5-9341-ab57d58896be");

        oldChi.setSessonId(sessionId);
        oldChi.setNodeId("tcp_01");
        oldChi.setUuid("9338b44-787d-4bc5-9341-ab57d58896be");

        when(newChannel.getChannelInfo()).thenReturn(chi);
        when(oldChannel.getChannelInfo()).thenReturn(oldChi);
        mockFindChannel(oldChannel);

        loginHandler.handle(newChannel, loginPacket);

        // new channel
        verify(newChannel).write(refEq(new LoginResponsePacket(LoginStatus.SUCCESS)), eq(ChannelAction.UDAPTE), eq(ChannelAction.SEND));

        // old channel
        if (sessionId.equalsIgnoreCase(loginPacket.getSessionId())) {
            verify(oldChannel).write(refEq(new LoginResponsePacket(LoginStatus.KICKOFF)), eq(ChannelAction.CLOSE));
        } else {
            verify(oldChannel).write(refEq(new LoginResponsePacket(LoginStatus.KICKOFF)), eq(ChannelAction.SEND), eq(ChannelAction.CLOSE));
        }
    }
}

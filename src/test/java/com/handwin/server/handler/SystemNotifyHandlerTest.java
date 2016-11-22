package com.handwin.server.handler;

import com.handwin.entity.ChannelInfo;
import com.handwin.entity.User;
import com.handwin.entity.UserToken;
import com.handwin.exception.ServerException;
import com.handwin.packet.*;
import com.handwin.server.Channel;
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

import static com.handwin.packet.SystemNotifyPacket.*;
import static org.mockito.Mockito.*;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/xmls/config-test-loading.xml")
public class SystemNotifyHandlerTest extends TestCase {
    private static final Logger logger = LoggerFactory.getLogger(SystemNotifyHandlerTest.class);

    @InjectMocks
    @Autowired
    private SystemNotifyHandler systemNotifyHandler;

    private SystemNotifyPacket sysNotifyPacket;

    User user;

    @BeforeClass
    public static void beforeClass() {
        System.setProperty("spring.profiles.active", "dev");
    }

    @Before
    public void before() throws ServerException {
        sysNotifyPacket = new SystemNotifyPacket();
        sysNotifyPacket.setTo("cb5d4d14a83a87f6ef68118050ae0883");
        sysNotifyPacket.setServeType((byte) (SERVICE_TYPE_NEED_SAVE | SERVICE_TYPE_NEED_PUSH));
        sysNotifyPacket.setFrom("me");
        sysNotifyPacket.setMessageBody("send system notification");
        sysNotifyPacket.setMsgId(1304294715165L);
        sysNotifyPacket.setPacketHead(new PacketHead());

        user = new User();
        user.setUserType(0);
        user.setId("cb5d4d14a83a87f6ef68118050ae0883");
        mockFindUser(user);
        mockLocalUser(true);
    }

    @Test
    public void testLogoutDelChannel() throws Exception {
        logger.info(name.getMethodName() + " starting");
    }

    @Test
    public void testLocalOnlineUser() throws Exception {
        logger.info(name.getMethodName() + " starting");
        Channel testChannel = mock(Channel.class);
        Channel toChannel = mock(Channel.class);
        mockFindChannel(toChannel);
        mockInBlackOrGreySheet(false);
        ChannelInfo chi = new ChannelInfo();
        chi.setChannelMode(ChannelMode.FOREGROUND);
        when(toChannel.getChannelInfo()).thenReturn(chi);

        systemNotifyHandler.handle(testChannel, sysNotifyPacket);
        verify(toChannel).write(sysNotifyPacket);
        MessageResponsePacket messageResponsePacket = new MessageResponsePacket();
        messageResponsePacket.setMessageType(SimpleMessagePacket.STATUS_RESPONSE_MESSAGE_TYPE);
        messageResponsePacket.setMessageStatus(MessageStatus.SERVER_RECEIVED);
        messageResponsePacket.setMessageId(sysNotifyPacket.getMsgId());
        messageResponsePacket.setTempId(sysNotifyPacket.getPacketHead().getTempId());
        verify(testChannel).write(refEq(messageResponsePacket));
    }

    @Test
    public void testLocalOfflineUser() throws Exception {
        logger.info(name.getMethodName() + " starting");
        Channel testChannel = mock(Channel.class);
        Channel toChannel = mock(Channel.class);
        mockFindChannel(toChannel);
        ChannelInfo chi = new ChannelInfo();
        chi.setChannelMode(ChannelMode.SUSPEND);
        when(toChannel.getChannelInfo()).thenReturn(chi);
        when(testChannel.getChannelInfo()).thenReturn(chi);
        UserToken userToken = new UserToken();
        userToken.setDeviceType(1);
        mockUserToken(userToken);

        systemNotifyHandler.handle(testChannel, sysNotifyPacket);
        verify(messageService).pushText(sysNotifyPacket, user, userToken, testChannel.getTraceId());
        MessageResponsePacket messageResponsePacket = new MessageResponsePacket();
        messageResponsePacket.setMessageType(SimpleMessagePacket.STATUS_RESPONSE_MESSAGE_TYPE);
        messageResponsePacket.setMessageStatus(MessageStatus.SERVER_RECEIVED);
        messageResponsePacket.setMessageId(sysNotifyPacket.getMsgId());
        messageResponsePacket.setTempId(sysNotifyPacket.getPacketHead().getTempId());
        verify(testChannel).write(refEq(messageResponsePacket));
    }

    @Test
    public void testNonlocalUserLogout() throws Exception {
        logger.info(name.getMethodName() + " starting");
        Channel testChannel = mock(Channel.class);
        mockLocalUser(false);

        systemNotifyHandler.handle(testChannel, sysNotifyPacket);
        verify(proxyMessageSender).write(user.getCountrycode(), sysNotifyPacket);
    }
}

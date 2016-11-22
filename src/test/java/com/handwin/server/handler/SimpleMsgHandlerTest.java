package com.handwin.server.handler;

import com.handwin.entity.ChannelInfo;
import com.handwin.entity.User;
import com.handwin.entity.UserToken;
import com.handwin.exception.ServerException;
import com.handwin.localentity.Message;
import com.handwin.packet.*;
import com.handwin.server.Channel;
import com.handwin.server.proto.ChannelAction;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.mockito.Mockito.*;


@RunWith(SpringJUnit4ClassRunner.class)
public class SimpleMsgHandlerTest extends TestCase {
    private static final Logger logger = LoggerFactory.getLogger(SimpleMsgHandlerTest.class);

    @InjectMocks
    @Autowired
    private TextMessageHandler textMessageHandler;

    TextMessagePacket textMessagePacket;

    User user;
    Channel testChannel;
    Channel toChannel;
    ChannelInfo chi;
    Message message;

    String userId = "c81e728d9d4c2f636f067f89cc14862c";

    @BeforeClass
    public static void beforeClass() {
        System.setProperty("spring.profiles.active", "dev");
    }

    @Before
    public void before() throws ServerException {
        textMessagePacket = new TextMessagePacket();
        textMessagePacket.setFrom("cb5d4d14a83a87f6ef68118050ae0883");
        textMessagePacket.setMessageServiceType(SimpleMessagePacket.TO_USER);
        textMessagePacket.setToUser("fajfpqrjqrpthqjtq");
        textMessagePacket.setMessageType((byte) 2);
        PacketHead packetHead = new PacketHead();
        packetHead.setSecret((byte) 1);
        textMessagePacket.setPacketHead(packetHead);

        user = new User();
        user.setId("cb5d4d14a83a87f6ef68118050ae0883");
        user.setUserType(0);

        testChannel = mock(Channel.class);
        toChannel = mock(Channel.class);
        ChannelInfo chi = new ChannelInfo();
        mockFindChannel(toChannel);
        chi.setChannelMode(ChannelMode.FOREGROUND);
        when(toChannel.getChannelInfo()).thenReturn(chi);
        when(testChannel.getChannelInfo()).thenReturn(chi);
        mockFindUser(user);
        mockLocalUser(true);
        mockServerReceived(null);
        mockInBlackSheet(false);
        message = textMessageHandler.createMessage(textMessagePacket, textMessagePacket.getToUser());
        message.setId(1045818485851L);
        mockCreateMessage(message);
    }

    @Test
    public void testOnlineUser() throws Exception {
        logger.info(name.getMethodName() + " starting");

        textMessageHandler.handle(testChannel, textMessagePacket);
        verify(messageService).addServerReceivedMessage(textMessagePacket.getCmsgid(), message.getId(), textMessageHandler.cmgIdttl, "c81e728d9d4c2f636f067f89cc14862c");
        verify(testChannel).write(refEq(textMessageHandler.createMessageResponsePacket(SimpleMessagePacket.STATUS_RESPONSE_MESSAGE_TYPE,
                message.getId(), textMessagePacket.getTempId(), MessageStatus.SERVER_RECEIVED, "cmsgId")));
        verify(toChannel).write(any(BasePacket.class), any(ChannelAction.class));

        /*
        verify(testChannel).write(Mockito.argThat(new ArgumentMatcher<BasePacket>() {
            @Override
            public boolean matches(Object argument) {
                ForwardMessagePacket msg = (ForwardMessagePacket)argument;
                return msg.getData() == null && msg.getFrom().compareTo(user.getId()) == 0 && msg.isBoth() &&
                        msg.getPacketHead().getSecret() == forwardMessagePacket.getPacketHead().getSecret();
            }
        }));
        */
    }

    @Test
    public void testUserInBlack() throws Exception {
        logger.info(name.getMethodName() + " starting");
        mockInBlackSheet(true);

        textMessageHandler.handle(testChannel, textMessagePacket);
        verify(messageService, never()).addServerReceivedMessage(anyString(), anyLong(), anyInt(), "c81e728d9d4c2f636f067f89cc14862c");
        verify(toChannel, never()).write(any(BasePacket.class), any(ChannelAction.class));
        verify(testChannel).write(refEq(textMessageHandler.createMessageResponsePacket(SimpleMessagePacket.STATUS_RESPONSE_MESSAGE_TYPE,
                0l, textMessagePacket.getTempId(), MessageStatus.SERVER_RECEIVED, "cmsgId")));
    }

    @Test
    public void testOfflineUser() throws Exception {
        logger.info(name.getMethodName() + " starting");
        when(toChannel.getChannelInfo()).thenReturn(null);
        mockInBlackOrGreySheet(false);
        UserToken userToken = new UserToken();
        userToken.setDeviceType(1);
        mockUserToken(userToken);

        textMessageHandler.handle(testChannel, textMessagePacket);
        verify(messageService).addServerReceivedMessage(textMessagePacket.getCmsgid(), message.getId(), textMessageHandler.cmgIdttl, userId);
        verify(testChannel).write(refEq(textMessageHandler.createMessageResponsePacket(SimpleMessagePacket.STATUS_RESPONSE_MESSAGE_TYPE,
                message.getId(), textMessagePacket.getTempId(), MessageStatus.SERVER_RECEIVED, "cmsgId")));
        verify(messageService).pushText(user, user, userToken, null, textMessagePacket, null);
        verify(toChannel, never()).write(any(BasePacket.class), any(ChannelAction.class));
    }

    @Test
    public void testUserDupMessage() throws Exception {
        logger.info(name.getMethodName() + " starting");
        mockServerReceived(14452552L);

        textMessageHandler.handle(testChannel, textMessagePacket);
        verify(messageService, never()).addServerReceivedMessage(anyString(), anyLong(), anyInt(), userId);
        verify(toChannel, never()).write(any(BasePacket.class), any(ChannelAction.class));
        verify(testChannel).write(refEq(textMessageHandler.createMessageResponsePacket(SimpleMessagePacket.STATUS_RESPONSE_MESSAGE_TYPE,
                14452552L, textMessagePacket.getTempId(),
                MessageStatus.SERVER_RECEIVED, "cmsgId")));
    }
}

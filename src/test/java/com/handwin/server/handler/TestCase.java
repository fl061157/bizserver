package com.handwin.server.handler;

import com.handwin.entity.ChannelInfo;
import com.handwin.entity.User;
import com.handwin.entity.UserToken;
import com.handwin.exception.ServerException;
import com.handwin.localentity.Message;
import com.handwin.packet.BasePacket;
import com.handwin.packet.ChannelMode;
import com.handwin.server.Channel;
import com.handwin.server.ProxyMessageSender;
import com.handwin.server.proto.ChannelAction;
import com.handwin.service.*;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

/**
 * Created by yangwei on 14-11-3.
 */
public abstract class TestCase {
    @Rule
    public TestName name = new TestName();

    @Mock
    protected UserService userService;

    @Mock
    protected ChannelService channelService;

    @Mock
    protected TcpSessionService onlineStatusService;

    @Mock
    protected ProxyMessageSender proxyMessageSender;

    @Mock
    protected MessageService messageService;

    @Mock
    private ConversationService conversationService;

    @org.junit.Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    protected void mockAuthorizeUser(User user) throws ServerException {
        when(userService.authorize(anyString(),0)).thenReturn(user);
        mockLocalUser(true);
    }

    protected void mockLocalUser(boolean local) {
        when(userService.isLocalUser(anyString())).thenReturn(local);
    }

    protected void mockFindUser(User user) throws ServerException {
        when(userService.findById(anyString(),0)).thenReturn(user);
    }

    protected void mockUserToken(UserToken token) {
        when(userService.getTokenInfo(anyString(), anyInt())).thenReturn(token);
    }

    protected void mockServerReceived(Long ret) {
        when(messageService.isServerReceived(anyString(), anyByte(), "c81e728d9d4c2f636f067f89cc14862c")).thenReturn(ret);
    }

    protected void mockInBlackOrGreySheet(boolean in) {
        when(conversationService.isInBlackOrInGreySheet(anyString(), anyString(),null)).thenReturn(in);
    }

    protected void mockInBlackSheet(boolean in) {
        when(conversationService.isInBlackSheet(anyString(), anyString(),null)).thenReturn(in);
    }

    protected void mockFindChannel(Channel channel) {
        when(channelService.findChannel(Matchers.any(User.class))).thenReturn(channel);
    }

    protected void mockOnlineStatusService() {
    }

    protected void mockCreateMessage(Message message) throws ServerException {
        when(messageService.createMessage(anyString(), anyString(), Matchers.any(Message.class), com.handwin.message.bean.MessageStatus.UNDEAL, Matchers.any(byte[].class))).thenReturn(message);
    }

    protected static class MockChannel implements Channel {
        BasePacket packet;
        ChannelAction[] actions;
        ChannelMode channelMode;
        ChannelInfo channelInfo;

        public void setChannelInfo(ChannelInfo channelInfo) {
            this.channelInfo = channelInfo;
        }

        public BasePacket getBasePacket() {
            return packet;
        }

        public ChannelAction[] getActions() {
            return actions;
        }

        public ChannelMode getChannelMode() {
            return channelMode;
        }

        @Override
        public void write(BasePacket packet) {
            this.packet = packet;
        }

        @Override
        public void write(BasePacket packet, ChannelAction... tcpActions) {
            this.packet = packet;
            this.actions = tcpActions;
        }

        @Override
        public void write(BasePacket packet, byte[] extraBody,
                          ChannelAction... tcpActions) {
            this.packet = packet;
            this.actions = tcpActions;
        }

        @Override
        public void write(byte[] packetBody, byte[] extraBody,
                          ChannelAction... tcpActions) {
        }

        @Override
        public void close() {

        }

        @Override
        public boolean isActive() {
            return true;
        }

        @Override
        public void changeMode(ChannelMode channelMode) {
            this.channelMode = channelMode;
        }

        @Override
        public String getIp() {
            return "";
        }

        @Override
        public int getPort() {
            return 0;
        }

        @Override
        public ChannelInfo getChannelInfo() {
            return channelInfo;
        }

        @Override
        public String getTraceId() {
            return null;
        }
    }
}

package com.handwin.server.handler;

import com.handwin.entity.ChannelInfo;
import com.handwin.entity.User;
import com.handwin.packet.*;
import com.handwin.server.Channel;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.mockito.Mockito.*;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/xmls/config-test-loading.xml")
public class ForwardMsgHandlerTest extends TestCase {
    private static final Logger logger = LoggerFactory.getLogger(ForwardMsgHandlerTest.class);

    @InjectMocks
    @Autowired
    private ForwardMessageHandler forwardMessageHandler;

    ForwardMessagePacket forwardMessagePacket;

    User user;

    @BeforeClass
    public static void beforeClass() {
        System.setProperty("spring.profiles.active", "dev");
    }

    @Before
    public void before() {
        MDC.put("TraceID", "3333333333");
        forwardMessagePacket = new ForwardMessagePacket();
        forwardMessagePacket.setBoth(true);
        PacketHead packetHead = new PacketHead();
        packetHead.setSecret((byte)1);
        forwardMessagePacket.setPacketHead(packetHead);
        forwardMessagePacket.setToUser("cb5d4d14a83a87f6ef68118050ae0883");

        user = new User();
        user.setId("cb5d4d14a83a87f6ef68118050ae0883");
        user.setUserType(0);
    }

    @After
    public void after(){
        //MDC.remove("TraceID");
    }

    @Test
    public void testLocalUserForward() throws Exception {
        logger.info(name.getMethodName() + " starting");
        Channel testChannel = mock(Channel.class);
        Channel toChannel = mock(Channel.class);
        ChannelInfo chi = new ChannelInfo();
        mockFindChannel(toChannel);
        chi.setChannelMode(ChannelMode.FOREGROUND);
        chi.setUserId(user.getId());
        when(toChannel.getChannelInfo()).thenReturn(chi);
        when(testChannel.getChannelInfo()).thenReturn(chi);
        mockFindUser(user);
        mockLocalUser(true);

        forwardMessageHandler.handle(testChannel, forwardMessagePacket);
        ForwardMessagePacket packet = new ForwardMessagePacket();
        packet.setData(null);
        packet.setFrom(user.getId());
        packet.setBoth(true);
        packet.setPacketHead(new PacketHead());
        verify(testChannel).write(Mockito.argThat(new ArgumentMatcher<BasePacket>() {
            @Override
            public boolean matches(Object argument) {
                ForwardMessagePacket msg = (ForwardMessagePacket)argument;
                return msg.getData() == null && msg.getFrom().compareTo(user.getId()) == 0 && msg.isBoth() &&
                        msg.getPacketHead().getSecret() == forwardMessagePacket.getPacketHead().getSecret();
            }
        }));
    }
}

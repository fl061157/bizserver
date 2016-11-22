package com.handwin.server.controller.livechat;

import com.alibaba.fastjson.JSON;
import com.handwin.admin.http.controller.LiveStopServerlet;
import com.handwin.audit.BehaviourLog;
import com.handwin.bean.LiveResponse;
import com.handwin.entity.ChannelInfo;
import com.handwin.entity.ServiceType;
import com.handwin.packet.GenericPacket;
import com.handwin.protocal.v5.codec.V5GenericPacket;
import com.handwin.protocal.v5.codec.V5PacketHead;
import com.handwin.server.AbstractChannelImpl;
import com.handwin.server.Channel;
import com.handwin.server.controller.livechat.cross.LiveChatCrossServiceAsync;
import com.handwin.server.controller.livechat.cross.LiveChatMessage;
import com.handwin.server.proto.ChannelAction;
import com.handwin.service.LiveChatService;
import com.handwin.service.UserService;
import com.handwin.service.impl.ChannelFactory;
import com.handwin.utils.SystemConstant;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;

/**
 * Created by fangliang on 16/7/14.
 */
@Service
public class LiveChatMessageService implements InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(LiveChatMessageService.class);

    @Autowired
    private UserService userService;

    @Autowired
    private LiveChatService liveChatService;

    @Autowired
    private ChannelFactory channelFactory;

    @Autowired
    private LiveStopServerlet liveStopServerlet;

    @Autowired
    private BehaviourLog behaviourLog;

    @Autowired
    @Qualifier(value = "rpcLiveChatCrossServiceAsync")
    private LiveChatCrossServiceAsync liveChatCrossService;


    private ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();

    private ReentrantReadWriteLock.ReadLock rl = rwl.readLock();

    private ReentrantReadWriteLock.WriteLock wl = rwl.writeLock();

    private ConcurrentHashMap<String, AtomicInteger> counterMap = new ConcurrentHashMap<>();

    private ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();

    @Override
    public void afterPropertiesSet() throws Exception {
        exec.scheduleAtFixedRate(() -> clearCouner(), 60, 60, TimeUnit.SECONDS);
    }

    public void handle(Channel channel, V5PacketHead packetHead, V5GenericPacket genericPacket) {

        if (logger.isDebugEnabled()) {

            logger.debug("[Living] Receive Content:{} ", new String(genericPacket.getBodySrcBytes()));

        }


        String from = packetHead.getFrom();
        String fromRegion = packetHead.getFromRegion();

        if (!userService.isLocalUser(fromRegion)) {
            logger.error("[LiveChat] message from:{} , fromRegion:{}", from, fromRegion);
            writeResponse(packetHead, new LiveResponse().buildResult(-1).buildAction(new int[]{ChannelAction.SEND.getAction()}), channel);
            return;
        }

        String roomID = packetHead.getTo();
        String[] toAndRegion = roomID.split("@");
        if (toAndRegion == null || toAndRegion.length != 2) {
            logger.error("[LiveChat] message to format error to:{}", roomID);
            writeResponse(packetHead, new LiveResponse().buildResult(-1).buildAction(new int[]{ChannelAction.SEND.getAction()}), channel);
            return;
        }

        String toRegion = toAndRegion[1];

        String hostess = null;
        if (userService.isLocalUser(toRegion)) {
            hostess = liveChatService.getHostessID(roomID);
            if (StringUtils.isBlank(hostess)) {
                logger.error("[LiveChat] message error for room not exists from:{} , to:{}", from, roomID);
                writeResponse(packetHead, new LiveResponse().buildResult(-1).buildAction(new int[]{ChannelAction.SEND.getAction()}), channel);
                return;
            }
        }

        Set<String> tcpNodeSet = liveChatService.findTcpNode(roomID);
        if (CollectionUtils.isEmpty(tcpNodeSet)) {
            logger.error("[LiveChat] message error for tcpNode empty from:{} , to:{}", from, roomID);
            writeResponse(packetHead, new LiveResponse().buildResult(-1).buildAction(new int[]{ChannelAction.SEND.getAction()}), channel);
            return;
        }

        final ChannelInfo fromChannelInfo = channel.getChannelInfo();
        final GenericPacket gPacket = build(packetHead, genericPacket);
        final AtomicBoolean forward = new AtomicBoolean(false);

        if (from.equals(hostess) || !liveStopServerlet.isStop(toAndRegion[0])) {

            tcpNodeSet.stream().forEach(tcpNode -> {
                if (userService.isLocal(tcpNode)) {
                    ChannelInfo cInfo = fromChannelInfo.copy();
                    Channel c = createChannel(cInfo, tcpNode, roomID, channel.getTraceId());
                    if (logger.isDebugEnabled()) {
                        logger.debug("[Living] Send tcpNode:{} ,  Content:{}", tcpNode, new String(genericPacket.getBodySrcBytes()));
                    }
                    c.write(gPacket, ChannelAction.MESSAGE_CHATROOM_ACTION);
                } else {
                    forward.set(true);
                }
            });


            if (!userService.isLocalUser(toRegion) || forward.get()) {
                LiveChatMessage liveChatMessage = new LiveChatMessage();
                liveChatMessage.setRoomID(packetHead.getTo());
                liveChatMessage.setFromRegion(packetHead.getFromRegion());
                liveChatMessage.setFrom(packetHead.getFrom());
                liveChatMessage.setAppID(packetHead.getAppId());
                liveChatMessage.setTraceID(packetHead.getTraceId());
                liveChatMessage.setContent(genericPacket.getBodySrcBytes());
                liveChatMessage.setService(packetHead.getService());
                liveChatCrossService.send(liveChatMessage);
            }

        }

        incCounter(roomID, packetHead.getAppId());

        writeResponse(packetHead, new LiveResponse().buildResult(0).buildAction(new int[]{ChannelAction.SEND.getAction()}), channel);

    }


    private void incCounter(String chatRoomID, Integer appID) {
        rl.lock();
        try {
            String key = String.format("%s_%d", chatRoomID, appID);
            AtomicInteger counter = counterMap.computeIfAbsent(key, s -> new AtomicInteger(0));
            counter.incrementAndGet();
        } finally {
            rl.unlock();
        }
    }


    private void clearCouner() {

        wl.lock();
        ConcurrentHashMap<String, AtomicInteger> tmpMap;
        try {
            tmpMap = counterMap;
            counterMap = new ConcurrentHashMap<>();
        } finally {
            wl.unlock();
        }

        String nodeId = System.getProperty("node.id");
        if (null == nodeId) nodeId = String.valueOf(SystemConstant.NODE_ID_DEFAULT);
        String nodeIp = System.getProperty("node.ip");
        if (null == nodeIp) nodeIp = SystemConstant.NODE_IP_DEFAULT;
        MDC.put("PREFIX", String.format("%s|%s|%s", nodeId, nodeIp, "BizServer"));

        for (Map.Entry<String, AtomicInteger> entry : tmpMap.entrySet()) {
            String[] ca = StringUtils.split(entry.getKey(), "_");
            String chatRoomID = ca[0];
            Integer appID = Integer.valueOf(ca[1]);
            AtomicInteger count = entry.getValue();
            behaviourLog.logChatMessageCount(appID, chatRoomID, count.get());
        }

    }


    private Channel createChannel(ChannelInfo channelInfo, String tcpNode, String roomID, String traceID) {
        ChannelInfo cInfo = channelInfo.copy();
        cInfo.setChatRoomID(roomID);
        cInfo.setNodeId(tcpNode);
        AbstractChannelImpl channel = (AbstractChannelImpl) channelFactory.createChannel(cInfo);
        channel.setTraceId(traceID);
        return channel;
    }


    private GenericPacket build(V5PacketHead packetHead, V5GenericPacket v5GenericPacket) {
        v5GenericPacket.setPacketHead(packetHead);
        v5GenericPacket.setBodyType(Byte.valueOf(packetHead.getContentType()));
        GenericPacket genericPacket = new GenericPacket();
        genericPacket.setV5GenericPacket(v5GenericPacket);
        genericPacket.setBodyType(GenericPacket.BODY_TYPE_BYTES);
        return genericPacket;
    }

    private void writeResponse(V5PacketHead packetHead, LiveResponse liveResponse, Channel channel) {
        V5PacketHead copyHead = packetHead.copy();
        copyHead.setService(ServiceType.ChatMessageResponse.getType());

        int[] actions = liveResponse.getAction();
        int length = actions.length;
        ChannelAction[] channelActions = new ChannelAction[length];
        for (int i = 0; i < length; i++) {
            channelActions[i] = ChannelAction.getChannelAction(actions[i]);
        }

        V5GenericPacket v5GenericPacket = new V5GenericPacket();
        v5GenericPacket.setPacketHead(copyHead);

        byte[] content = JSON.toJSONBytes(liveResponse);
        v5GenericPacket.setBodySrcBytes(content);

        GenericPacket genericPacket = new GenericPacket();
        genericPacket.setV5GenericPacket(v5GenericPacket);
        channel.write(genericPacket, channelActions);
    }

//    public static void main(String[] args) {
//        LiveChatMessageService ls = new LiveChatMessageService();
//        String chatRoomID = "123456";
//        Integer appID = 100;
//        ls.incCounter(chatRoomID, appID);
//        ls.incCounter(chatRoomID, appID);
//        ls.incCounter(chatRoomID, appID);
//
//
//        String chatRoomID1 = "1234567";
//        Integer appID1 = 200;
//        ls.incCounter(chatRoomID1, appID1);
//        ls.incCounter(chatRoomID1, appID1);
//        ls.incCounter(chatRoomID1, appID1);
//        ls.incCounter(chatRoomID1, appID1);
//        ls.incCounter(chatRoomID1, appID1);
//        ls.incCounter(chatRoomID1, appID1);
//
//        Map<String, AtomicInteger> m = ls.counterMap;
//        for (Map.Entry<String, AtomicInteger> e : m.entrySet()) {
//            System.out.println(e.getKey() + " : " + e.getValue());
//        }
//
//    }


}

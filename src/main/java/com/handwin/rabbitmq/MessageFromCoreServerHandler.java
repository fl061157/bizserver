package com.handwin.rabbitmq;

import com.handwin.packet.BasePacket;
import com.handwin.packet.SystemNotifyPacket;
import com.handwin.server.handler.Handler;
import com.handwin.server.handler.GroupSystemNotifyFromCoreServerHandler;
import com.handwin.server.proto.FullProtoRequestMessage;
import com.handwin.service.UserService;
import com.handwin.utils.SystemConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

/**
 * Created by piguangtao on 15/11/30.
 * 解决coreServer发送的系统通知如群组系统通知 需要做特殊处理
 */
public class MessageFromCoreServerHandler extends TcpMessageHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageFromCoreServerHandler.class);

    @Autowired
    private GroupSystemNotifyFromCoreServerHandler groupSystemNotifyFromCoreServerHandler;


    protected void handPacket(final FullProtoRequestMessage requestMessage) {
        Handler handler = hodler.getHandler(requestMessage.getPacket().getClass());
        if (handler != null) {
            com.handwin.server.Channel businessChannel = null;
            try {
                //对系统通知需要做特殊处理 使用专门处理从外部部件发送过来的系统通知
                boolean formChannel = true;
                if (requestMessage.getPacket() instanceof SystemNotifyPacket) {
                    SystemNotifyPacket systemNotifyPacket = (SystemNotifyPacket) requestMessage.getPacket();
                    Map<String, Object> extra = systemNotifyPacket.getExtra();
                    if (null != extra && SystemConstant.SYSTEM_NOTIFY_EXTRAT_TYPE_GROUP.equals(extra.get(SystemConstant.SYSTEM_NOTIFY_EXTRA_KEY_TYPE))) {
                        handler = groupSystemNotifyFromCoreServerHandler;
                        formChannel = false;
                    }
                }

                if (requestMessage.getBaseRequestMessage().getAppId() != 0) {
                    requestMessage.getPacket().getPacketHead().setAppId(requestMessage.getBaseRequestMessage().getAppId());
                }

                if (formChannel) {
                    businessChannel = channelService.buildChannel(requestMessage.getBaseRequestMessage());
                }
                handler.before(requestMessage.getBaseRequestMessage().getTraceId(), requestMessage.getPacket());
                requestMessage.getPacket().setTraceId(requestMessage.getBaseRequestMessage().getTraceId());
                try {
                    BasePacket packet = requestMessage.getPacket();
                    com.handwin.server.Channel tempChannel = businessChannel;
                    if (tempChannel == null) {
                        tempChannel = channelService.buildChannel(requestMessage.getBaseRequestMessage());
                    }
                    attachThirdUser(packet, tempChannel);

                    handler.handle(businessChannel, requestMessage.getPacket());
                } catch (Exception e) {
                    LOGGER.error("handPacket error: ", e);
                }
            } finally {
                handler.after(requestMessage.getBaseRequestMessage().getTraceId(), requestMessage.getPacket());

                try {
                    behaviourLog.thirdLoggerCommit(businessChannel, requestMessage);
                } catch (Exception e) {
                }

                behaviourLog.audit(businessChannel, requestMessage);
            }

        } else {
            LOGGER.error("No handler for {}", requestMessage.getPacket().getClass());
        }
    }


}

package com.handwin.server.handler;

import com.handwin.bean.Platform;
import com.handwin.entity.ChannelInfo;
import com.handwin.entity.TcpStrategyQuery;
import com.handwin.entity.TcpStrategyResult;
import com.handwin.entity.User;
import com.handwin.exception.ServerException;
import com.handwin.packet.LoginPacket;
import com.handwin.packet.LoginResponsePacket;
import com.handwin.packet.LoginStatus;
import com.handwin.server.Channel;
import com.handwin.server.proto.ChannelAction;
import com.handwin.service.ChannelService;
import com.handwin.service.IIpStrategyService;
import com.handwin.service.TcpSessionService;
import com.handwin.utils.SystemConstant;
import com.handwin.utils.SystemUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class LoginHandler extends AbstractHandler<LoginPacket>
        implements InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(LoginHandler.class);

    @Autowired
    private TcpSessionService onlineStatusService;

    @Autowired
    private ChannelService channelService;

    @Autowired
    @Qualifier("tcpStrategyServiceImpl")
    private IIpStrategyService ipStrategyService;

    private final static String DEFAULT_KICK_ID = "D_K_I";


    public void afterPropertiesSet() throws Exception {
        register(LoginPacket.class);
    }

    @Override
    public void handle(Channel channel, LoginPacket loginPacket) {
        try {

            preHandleThirdApp(loginPacket);


            if (channel != null && channel.getChannelInfo() != null && loginPacket.getPlatform() != Platform.Mobile) {

                if (logger.isInfoEnabled()) {
                    logger.info("[LoginHandler] Login Platform:{} , sessionID:{} ", loginPacket.getPlatform().getPlatform(), loginPacket.getSessionId());
                }

                String kickId = loginPacket.getKickId();

                if (StringUtils.isNotBlank(kickId)) {
                    channel.getChannelInfo().setKickId(kickId);
                }
            }
            channel.getChannelInfo().setPlatform(loginPacket.getPlatform().getPlatform());


            if (StringUtils.isNotBlank(loginPacket.getRegionCode()) && !userService.isLocalUser(loginPacket.getRegionCode())) {
                logger.error("[LoginHandler] , not login own idc  failure ! ",
                        loginPacket.getSessionId());
                channel.write(getRefreshTcpServersResp(channel, loginPacket), ChannelAction.SEND, ChannelAction.CLOSE);
                return;
            }

            if (logger.isInfoEnabled()) {
                logger.info("[LoginHandler],sessionID:{} , traceId:{}", loginPacket != null ? loginPacket.getSessionId() : "", channel.getTraceId());
            }
            User user;

            try {
                user = userService.authorize(loginPacket.getSessionId(), loginPacket.getAppKey());
            } catch (ServerException e) {
                logger.error("[LoginHandler], authorize sessionId:{} failure ! ", loginPacket.getSessionId());
                channel.write(new LoginResponsePacket(LoginStatus.REDIRECT), ChannelAction.SEND, ChannelAction.CLOSE);
                return;
            }

            if (user == null) {
                if (logger.isWarnEnabled()) {
                    logger.warn("[LoginHandler] , traceId:{} ,  authorize sessionId:{}  user null !",
                            channel.getTraceId(), loginPacket.getSessionId());
                }
                channel.write(new LoginResponsePacket(LoginStatus.FAIL), ChannelAction.SEND, ChannelAction.CLOSE);
                return;
            } else {
                //采用用户信息的国家码判断用户应该登录的区域
                String userCountryCode = user.getCountrycode();
                if (StringUtils.isNotBlank(userCountryCode) && !userService.isLocalUser(userCountryCode)) {
                    logger.error(String.format("[LoginHandler] , not login own idc  failure ! sessionId:%s,userContryCode:%s", loginPacket.getSessionId(), userCountryCode));
                    channel.write(getRefreshTcpServersResp(channel, loginPacket), ChannelAction.SEND, ChannelAction.CLOSE);
                    return;
                }
            }


            channel.getChannelInfo().setSessonId(loginPacket.getSessionId());
            channel.getChannelInfo().setUserId(user.getId());
            channel.getChannelInfo().setUserZoneCode(user.getCountrycode());
            channel.changeMode(loginPacket.getChannelMode());
            if (logger.isDebugEnabled()) {
                logger.debug("[Login] AppKey:{} ", loginPacket.getAppKey());
            }
            channel.getChannelInfo().setAppID(loginPacket.getAppKey());
            setChannelAttr(channel, loginPacket);


            //如果是系统账号，直接返回用户登录成功
            if (userService.isSystemAccount(user)) { //TODO 系统账号是否需要考虑Platform 问题
                //系统账号 允许多次登录，失效的通道 tcpServer会发送退出指令 因此不需要清理通道
//                onlineStatusService.cleanChannel(user.getId(), (short) user.getAppId());
                onlineStatusService.addChannel(user, channel);
                channel.getChannelInfo().setSessonId(loginPacket.getSessionId());
                channel.getChannelInfo().setUserId(user.getId());
                channel.write(new LoginResponsePacket(LoginStatus.SUCCESS), ChannelAction.UDAPTE, ChannelAction.SEND);
                return;
            }

            //用户一定是在出生地登录
            //用户剔人处理
            kickUserOtherChannel(user, loginPacket);
            //用户登录成功后，更新用户的信息
            userService.login(user, user.getAppId(), channel.getChannelInfo().getUuid(),
                    channel.getChannelInfo().getNodeId(), channel.getIp(),
                    loginPacket.getLanguage(), loginPacket.getTimeZone());

//            //TODO 异常处理器  处理各种异常 发送消息
//            onlineStatusService.cleanChannel(user.getId(), user.getAppId()); //TODO 多 Platform 下明显不适合使用


            onlineStatusService.addChannel(user, channel);
            if (logger.isDebugEnabled()) {
                logger.debug("[LoginHandler], traceId:{},  write loginResponse to user.id :{}. channelInfo:{} ", channel.getTraceId(), user.getId(), null != channel ? channel.getChannelInfo() : "");
            }
            LoginResponsePacket responsePacket = new LoginResponsePacket(LoginStatus.SUCCESS);
            responsePacket.setClientIP(channel.getChannelInfo().getIp());
            channel.write(responsePacket, ChannelAction.UDAPTE, ChannelAction.SEND);

        } catch (Throwable e) {
            logger.error(String.format("[LoginHandler] traceId:%s ", channel.getTraceId()), e);
            channel.write(new LoginResponsePacket(LoginStatus.REDIRECT), ChannelAction.SEND, ChannelAction.CLOSE);
        }
    }

    private void kickUserOtherChannel(final User user, final LoginPacket loginPacket) {

        try {

            Platform platform = loginPacket.getPlatform();

            Map<Platform, List<Channel>> channelMap = channelService.findChannelMap(user);

            if (MapUtils.isEmpty(channelMap)) return;

            List<Channel> channelList = channelMap.get(platform);

            if (CollectionUtils.isEmpty(channelList)) return;

            if (logger.isDebugEnabled()) {
                logger.debug("[Kick] user.id:{} , Platform:{} , keepCount:{} , channel.size:{} ",
                        user.getId(), platform.getPlatform(), platform.getCount(), channelList.size());
            }

            Map<String, List<Channel>> kcm = new HashedMap();
            for (Channel channel : channelList) {
                if (channel != null && channel.getChannelInfo() != null) {
                    String kickID = StringUtils.isBlank(channel.getChannelInfo().getKickId()) ? DEFAULT_KICK_ID : channel.getChannelInfo().getKickId();
                    List<Channel> channels = kcm.get(kickID);
                    if (channels == null) {
                        channels = new ArrayList<>();
                        kcm.put(kickID, channels);
                    }
                    channels.add(channel);
                }
            }

            int keepCount = platform.getCount() - 1;

            for (List<Channel> channels : kcm.values()) {
                if (channels.size() <= keepCount) {
                    continue;
                }
                Collections.sort(channels, (c1, c2) -> (int) (c2.getChannelInfo().getCreateTime() - c1.getChannelInfo().getCreateTime()));
                List<Channel> kcl = channels.subList(keepCount, channels.size());
                if (CollectionUtils.isNotEmpty(kcl)) {
                    for (Channel channel : kcl) {
                        if (logger.isInfoEnabled()) {
                            logger.info("[LoginHandler], traceId:{} , kickUser user.id:{} , oldChannel.channelUUID:{}",
                                    channel.getTraceId(), user.getId(), channel.getChannelInfo() != null ? channel.getChannelInfo().getUuid() : "");
                        }
                        ChannelInfo channelInfo = channel.getChannelInfo();
                        onlineStatusService.delChannel(user.getId(), loginPacket.getAppKey(),
                                channelInfo.getUuid(), channelInfo.getNodeId());
                        if (loginPacket.getSessionId().equalsIgnoreCase(channelInfo.getSessonId()) && platform.getCount() <= 1) {
                            channel.write(new LoginResponsePacket(LoginStatus.KICKOFF), ChannelAction.CLOSE);
                        } else {
                            channel.write(new LoginResponsePacket(LoginStatus.KICKOFF), ChannelAction.SEND, ChannelAction.CLOSE);
                        }
                    }
                }

            }
        } catch (Exception e) {
            logger.error("[LoginHandler] Kick User error ", e);
        }
    }


    private LoginResponsePacket getRefreshTcpServersResp(Channel channel, LoginPacket loginPacket) {
        String userIp = channel.getIp();
        String userCountryCode = loginPacket.getRegionCode();
        String tcpServers = null;
        if (StringUtils.isNotBlank(userIp) && StringUtils.isNotBlank(userCountryCode)) {
            TcpStrategyQuery query = new TcpStrategyQuery();
            query.buildIp(userIp).buildContryCode(userCountryCode);
            TcpStrategyResult result = ipStrategyService.getTcpServers(query);
            if (null != result) {
                tcpServers = result.getTcpServers();
            }
        }
        LoginResponsePacket result = new LoginResponsePacket(LoginStatus.REFRESH_TCPSERVER);

        if (StringUtils.isNotBlank(tcpServers)) {
            result.setTcpServersJson(tcpServers);
        }

        logger.debug("[tcpServers]ip:{},countryCode:{},tcpServers:{}", userIp, userCountryCode, tcpServers);
        return result;
    }

    private void setChannelAttr(Channel channel, LoginPacket loginPacket) {
        if (null == channel || null == channel.getChannelInfo() || null == loginPacket) return;
        byte[] netTypeBytes = loginPacket.getAttr(SystemConstant.CHANNEL_NETWORK_TYPE);
        if (null != netTypeBytes && netTypeBytes.length > 0) {
            channel.getChannelInfo().setNetworkType(new String(netTypeBytes, StandardCharsets.UTF_8));
        }
    }

    private void preHandleThirdApp(LoginPacket loginPacket) {
        if (null == loginPacket) return;
        byte[] appKeyBytes = loginPacket.getAttr(SystemConstant.LOGIN_APP_KEY);
        if (null != appKeyBytes && appKeyBytes.length == 4) {
            //解析整数

            int iAppKey = SystemUtils.readSimpleInt(appKeyBytes);

            if (iAppKey < 65535) {
                loginPacket.setAppKey(loginPacket.getPacketHead().getAppId());
            } else {
                loginPacket.setAppKey(SystemUtils.readSimpleInt(appKeyBytes));
            }
            String[] localIdcCountryCode = userService.getLocalIdcCountryCode();
            if (null != localIdcCountryCode && localIdcCountryCode.length > 0) {
                loginPacket.setRegionCode(userService.getLocalIdcCountryCode()[0]);
            } else {
                logger.warn("IDC no set local country code");
            }
        } else {
            loginPacket.setAppKey(loginPacket.getPacketHead().getAppId());
        }
    }


    public static Integer getAppID(LoginPacket loginPacket) {
        if (null == loginPacket) return 0;
        byte[] appKeyBytes = loginPacket.getAttr(SystemConstant.LOGIN_APP_KEY);
        if (null != appKeyBytes && appKeyBytes.length == 4) {
            return SystemUtils.readSimpleInt(appKeyBytes);
        } else {
            return loginPacket.getPacketHead().getAppId();
        }
    }


}

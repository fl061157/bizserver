package com.handwin.utils;

import com.handwin.bean.RichMessageInfo;
import com.handwin.entity.User;
import com.handwin.entity.UserSessionIndex;
import com.handwin.exception.ServerException;
import com.handwin.packet.SimpleMessagePacket;
import com.handwin.packet.VideoMessagePacket;
import com.handwin.packet.v5.V5SimpleMessagepacket;
import com.handwin.service.GroupService;
import com.handwin.service.UserService;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.fluent.Request;
import org.apache.http.message.BasicNameValuePair;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.dom4j.tree.DefaultElement;
import org.dom4j.tree.DefaultText;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Created by piguangtao on 15/11/24.
 */
@Service
public class MessageUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageUtils.class);

    @Autowired
    @Qualifier("messageSource")
    private ReloadableResourceBundleMessageSource messageSource;

    @Autowired
    @Qualifier("messageSource1")
    private ReloadableResourceBundleMessageSource messageSource1;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    private UserService userService;

    @Value("${http.serve.url}")
    protected String httpServerUrl;

    @Autowired
    private GroupService groupService;


    public RichMessageInfo generateRichMessage(byte[] content) {
        return generateRichMessage(new String(content, StandardCharsets.UTF_8));
    }

    /**
     * 获取@的人员 ie. hello<at id="" alt="@曹玲哥哥" name="曹玲哥哥" />富媒体测试
     * 接口性能 0.7ms
     *
     * @param content
     * @return
     */
    public RichMessageInfo generateRichMessage(String content) {

        if (StringUtils.isBlank(content)) {
            return null;
        }

        if (!(content.contains("<") && content.contains(">"))) {
            return null;
        }

        final RichMessageInfo result = new RichMessageInfo();
        try {
            String xmlContent = String.format("<msg>%s</msg>", content);

            SAXReader reader = new SAXReader();

            Document doc = reader.read(new ByteArrayInputStream(xmlContent.getBytes(StandardCharsets.UTF_8)));
            Element rootElement = doc.getRootElement();
//            StringBuilder textBuilder = new StringBuilder();
            StringBuilder atUsersBuilder = new StringBuilder();
            if (rootElement.hasContent()) {
                List list = rootElement.content();
                list.stream().forEach(element -> {
                    if (element instanceof DefaultText) {
                        DefaultText text = (DefaultText) element;
//                        textBuilder.append(text.getText());
                        result.getSubContents().add(new RichMessageInfo.TextSubContent(text.getText()));
                    } else if (element instanceof DefaultElement) {
                        //解析at自定义标签
                        DefaultElement defaultElement = (DefaultElement) element;
                        String name = defaultElement.getName();
                        if ("at".equalsIgnoreCase(name)) {
                            Attribute idAttri = defaultElement.attribute("id");
                            RichMessageInfo.AtSubContent atSubContent = new RichMessageInfo.AtSubContent();
                            if (null != idAttri) {
                                String value = idAttri.getValue();
                                if (StringUtils.isNotBlank(value)) {
                                    atUsersBuilder.append(value).append(",");
                                    atSubContent.withId(value);
                                }
                            }
                            Attribute altAttri = defaultElement.attribute("alt");
                            if (null != altAttri && StringUtils.isNotBlank(altAttri.getValue())) {
//                                textBuilder.append(" ")
//                                        .append(altAttri.getValue()).append(" ");
                                atSubContent.withAlt(altAttri.getValue());
                            } else {
                                Attribute nameAttri = defaultElement.attribute("name");
                                if (null != nameAttri) {
                                    String value = nameAttri.getValue();
                                    if (StringUtils.isNotBlank(value)) {
//                                        textBuilder.append(" @")
//                                                .append(value).append(" ");
                                        atSubContent.withName(value);
                                    }
                                }
                            }
                            result.getSubContents().add(atSubContent);
                        } else if ("user".equalsIgnoreCase(name)) {
                            Attribute idAttri = defaultElement.attribute("id");
                            RichMessageInfo.UserSubContent userSubContent = new RichMessageInfo.UserSubContent();
                            if (null != idAttri) {
                                String value = idAttri.getValue();
                                if (StringUtils.isNotBlank(value)) {
                                    userSubContent.withId(value);
                                }
                            }
                            result.getSubContents().add(userSubContent);
                        }
                    }
                });
            }
            result.setAtUsers(atUsersBuilder.toString());
//            result.setContentAfterReplaceTag(textBuilder.toString());
        } catch (Exception e) {
            LOGGER.error(String.format("fails to parse content.%s", content), e);

            //解析失败表示不是富媒体消息
            return null;
        }


        return result;
    }


    /**
     * 对应低版本的视频消息如果不是阅后即焚消息，在提示消息中需要包含分享的视频短链接
     * {"extra_data": {"animation": 0,"burned": false,"costume": "cat","duration": 3,"facelift": 0.05,"progress": 0,"read_status": 0,"type": 1},"local_video": "/storage/emulated/0/Android/data/me.chatgame.mobilecg/cache/.video/mv_1451960568473.ts","snap_url": "http://114.215.204.97:29102/api/file/download/2016/1/5/10/c03b09b6-3ef4-457b-be79-bae3c81e5b95.jpg","video_url": "http://114.215.204.97:29102/api/file/download/2016/1/5/10/6f3d1f58-fadc-4ffc-9c0f-dc63135d1cdb.ts"}
     *
     * @param simpleMessagePacket
     * @param fromUser
     * @param toUser
     * @return
     */
    public String getVideoMessageTipForUnsupportedVersion(VideoMessagePacket simpleMessagePacket, User fromUser, User toUser, Integer appID) {

        ReloadableResourceBundleMessageSource mSource = getMessageSource(appID);

        String tip = mSource.getMessage("video.version.support.tip1", null, toUser.getLocale());
        try {
            byte[] content = simpleMessagePacket.getContent();
            Map<String, Object> video = objectMapper.readValue(content, new TypeReference<Map<String, Object>>() {
            });
            if (null != video) {
                Object extraData = video.get("desc");
                if (null != extraData) {
                    Map<String, Object> descMap = objectMapper.readValue(((String) extraData).getBytes(StandardCharsets.UTF_8), new TypeReference<Map<String, Object>>() {
                    });

                    Object burned = descMap.get("burned");
                    if (null != burned) {
                        //表示阅后即焚
                        if ((Boolean) burned) {
                            return tip;
                        }
                    }
                }
                //获取视频的短链接
                UserSessionIndex userSessionIndex = userService.findUserSessionIndexByKey(fromUser.getId(), fromUser.getAppId());

                if (null != userSessionIndex) {
                    String shareUrl = getVideoShareUrl(userSessionIndex.getSessionId(), fromUser.getCountrycode(), new String(content, StandardCharsets.UTF_8));
                    if (null != shareUrl) {
                        tip = mSource.getMessage("video.version.support.tip2", new String[]{shareUrl}, toUser.getLocale());
                    }
                }
            }
        } catch (Exception e) {
            //抛错时 需要优雅降级
            LOGGER.error("fails to handle video tip", e);
        }
        return tip;
    }


    protected String getVideoShareUrl(String sessionId, String countryCode, String video) {
        String shareUrl = null;
        //调用AttachServer接口
        boolean isNeedResend = true;

        for (int i = 0; i < 3 && isNeedResend; i++) {
            try {
                Request request = Request.Post(String.format("%s/api/share/video2", httpServerUrl))
                        .bodyForm(((Supplier<List<BasicNameValuePair>>) () -> {
                            List<BasicNameValuePair> basicNameValuePairList = Arrays.asList(new BasicNameValuePair("video", video));
                            return basicNameValuePairList;
                        }).get(), Charset.forName("UTF-8"));
                request.addHeader("client-session", sessionId);
                request.addHeader("region-code", countryCode);
                String result = request.execute().returnContent().asString();
                if (StringUtils.isNotBlank(result)) {
                    Map<String, Object> videoShareMap = objectMapper.readValue(result, new TypeReference<Map<String, Object>>() {
                    });
                    if (null != videoShareMap) {
                        shareUrl = (String) videoShareMap.get("share_url");
                    }
                }
                isNeedResend = false;
            } catch (Throwable e) {
                LOGGER.error("[share video urls].fails to get share video urls.", e);
                isNeedResend = true;
            }
        }
        return shareUrl;
    }

    public String getRichMessageForPush(RichMessageInfo richMessageInfo, String receiver, Integer appId) {
        StringBuilder sb = new StringBuilder();
        if (null != richMessageInfo && null != richMessageInfo.getSubContents()) {
            for (RichMessageInfo.SubContent subContent : richMessageInfo.getSubContents()) {
                sb.append(getRichSubContentForPush(subContent, receiver, appId));
            }
        }
        return sb.toString();
    }

    protected String getRichSubContentForPush(RichMessageInfo.SubContent subContent, String receiver, Integer appId) {
        if (null == subContent) return "";
        StringBuilder sb = new StringBuilder();

        switch (subContent.getContentType()) {
            case RichMessageInfo.SubContent.CONTENT_TYPE_PLAIN: {
                sb.append(((RichMessageInfo.TextSubContent) subContent).getPlainContentForPush());
                break;
            }
            case RichMessageInfo.SubContent.CONTENT_TYPE_AT: {
                RichMessageInfo.AtSubContent atSubContent = (RichMessageInfo.AtSubContent) subContent;
                if (StringUtils.isNotBlank(receiver) && null != appId) {
                    String id = atSubContent.getId();
                    String alt = atSubContent.getAlt();
                    if (StringUtils.isNotBlank(id)) {
                        String[] atIds = atSubContent.getId().split(",");
                        for (String atId : atIds) {
                            User atUser = userService.findById(atId, appId);
                            String nickName = userService.getFriendNickname(atUser, receiver, appId);
                            sb.append("@").append(nickName);
                        }
                    } else {
                        sb.append(alt);
                    }
                }
                break;
            }
            case RichMessageInfo.SubContent.CONTENT_TYPE_USER: {
                RichMessageInfo.UserSubContent userSubContent = (RichMessageInfo.UserSubContent) subContent;
                String userId = userSubContent.getId();
                if (StringUtils.isNotBlank(userId)) {
                    User fromUser = userService.findById(userId, appId);
                    //发送方和接受方相同
                    if (userId.equalsIgnoreCase(receiver)) {
                        sb.append(fromUser.getNickname());
                    } else {
                        String nickName = userService.getFriendNickname(fromUser, receiver, appId);
                        sb.append(nickName);
                    }
                }
                break;
            }
            default: {
                break;
            }
        }
        return sb.toString();
    }

    public boolean isForward(User fromUser, SimpleMessagePacket simpleMessagePacket) {
        String sourceRegion = null;
        if (simpleMessagePacket instanceof V5SimpleMessagepacket) {
            sourceRegion = ((V5SimpleMessagepacket) simpleMessagePacket).getMessageSourceRegion();
        } else {
            sourceRegion = fromUser.getCountrycode();
        }

        boolean isForward =
                !(StringUtils.isBlank(sourceRegion) ||
                        userService.isLocalUser(sourceRegion));
        return isForward;
    }

    public List<User> getReceiversForGroupMessage(SimpleMessagePacket simpleMessagePacket, String excludeUserId) {
        List<User> toUsers = null;
        if (simpleMessagePacket instanceof V5SimpleMessagepacket) {
            String[] groupSpeUsers = ((V5SimpleMessagepacket) simpleMessagePacket).getGroupSpeUsers();
            if (null != groupSpeUsers && groupSpeUsers.length > 0) {
                List<String> toUserIds = Arrays.asList(groupSpeUsers);
                if (StringUtils.isNotBlank(excludeUserId)) {
                    toUserIds.remove(excludeUserId);
                }
                toUsers = userService.findByIdList(toUserIds);
            }
        }
        if (null == toUsers) {
            String fromGroup = simpleMessagePacket.getFromGroup();
            try {
                toUsers = groupService.findUserExcludeById(excludeUserId, fromGroup, simpleMessagePacket.getPacketHead().getAppId());
            } catch (ServerException e) {
                LOGGER.error("findGroup error", e);
            }
        }
        return toUsers;


    }


    private ReloadableResourceBundleMessageSource getMessageSource(Integer appID) {
        if (appID != null) {
            switch (appID) {
                case 0:
                    return messageSource;
                case 1:
                    return messageSource1;
            }
        }
        return messageSource;

    }

}

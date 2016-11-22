package com.handwin.entity;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by piguangtao on 14-3-10.
 */
public class SystemNotifyMsgBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(SystemNotifyMsgBean.class);

    private static ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    private String type;
    private String from;
    private String to;
    private String uuid;
    private Boolean ackFlag;
    private Boolean readFlag;
    private Boolean pushFlag;
    private String pushContent;
    private String expired;
    private  JsonNode data;
    private Short appId;

    public static ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public static void setObjectMapper(ObjectMapper objectMapper) {
        SystemNotifyMsgBean.objectMapper = objectMapper;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Boolean getAckFlag() {
        return ackFlag;
    }

    public void setAckFlag(Boolean ackFlag) {
        this.ackFlag = ackFlag;
    }

    public Boolean getReadFlag() {
        return readFlag;
    }

    public void setReadFlag(Boolean readFlag) {
        this.readFlag = readFlag;
    }

    public Boolean getPushFlag() {
        return pushFlag;
    }

    public void setPushFlag(Boolean pushFlag) {
        this.pushFlag = pushFlag;
    }

    public String getPushContent() {
        return pushContent;
    }

    public void setPushContent(String pushContent) {
        this.pushContent = pushContent;
    }

    public String getExpired() {
        return expired;
    }

    public void setExpired(String expired) {
        this.expired = expired;
    }

    public JsonNode getData() {
//        String result = null;
//        try {
//            return objectMapper.writeValueAsString(data);
//        } catch (JsonProcessingException e) {
//            LOGGER.error("fails to parse JsonObject.payload:"+data);
//        }
        return data;
    }

    public void setData(JsonNode data) {
        this.data = data;
    }

    public Short getAppId() {
        return appId;
    }

    public void setAppId(Short appId) {
        this.appId = appId;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SystemNotifyMsgBean{");
        sb.append("type='").append(type).append('\'');
        sb.append(", from='").append(from).append('\'');
        sb.append(", to='").append(to).append('\'');
        sb.append(", uuid='").append(uuid).append('\'');
        sb.append(", ackFlag=").append(ackFlag);
        sb.append(", readFlag=").append(readFlag);
        sb.append(", pushFlag=").append(pushFlag);
        sb.append(", pushContent='").append(pushContent).append('\'');
        sb.append(", expired='").append(expired).append('\'');
        sb.append(", data=").append(data);
        sb.append(", appId=").append(appId);
        sb.append('}');
        return sb.toString();
    }
}

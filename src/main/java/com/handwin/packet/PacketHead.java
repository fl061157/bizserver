package com.handwin.packet;

import com.handwin.genericmap.GMapUtils;
import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Map;

import static com.handwin.utils.V5ProtoConstant.*;

public class PacketHead {

    public static final String KEY_FROM = "1"; //string
    public static final String KEY_TO = "2"; //string
    public static final String KEY_FROM_REGION = "3"; //string
    public static final String KEY_TO_REGION = "4"; //string
    public static final String KEY_CONTENT_TYPE = "5"; //string
    public static final String KEY_CONTENT_LENGTH = "6"; //int
    public static final String KEY_TIMESTAMP = "7"; //long
    public static final String KEY_SERVICE = "8"; //string
    public static final String KEY_MESSAGE_ID = "9"; //string
    public static final String KEY_PUSH = "10"; //boolean
    public static final String KEY_PUSH_COUNT = "11"; //int
    public static final String KEY_PUSH_CONTENT = "12"; //string
    public static final String KEY_TEMP_ID = "13"; //int
    public static final String KEY_RESEND = "14"; //boolean
    public static final String KEY_SERVER_RECEIVED_CONFIRM = "15"; //boolean
    public static final String KEY_CLIENT_RECEIVED_CONFIRM = "16"; //boolean
    public static final String KEY_READ_CONFIRM = "17"; //boolean
    public static final String KEY_EXPIRE = "18"; //int
    public static final String KEY_BIDIRECTION = "19"; //boolean
    public static final String KEY_VERSION = "20"; //byte
    public static final String KEY_USER_AGENT = "21"; //string
    public static final String KEY_APP_ID = "22"; //string
    public static final String KEY_VIA = "23"; //string
    public static final String KEY_SERVER_MESSAGE_ID = "24"; //string
    /**
     * 是否需要存储
     */
    public static final String KEY_STORE = "25"; //boolean

    /**
     * 是否确保消息已达
     */
    public static final String KEY_ENSURE_ARRIVE = "26"; //boolean

    /**
     * 离线消息 push数目 是否增加1
     */
    public static final String KEY_PUSH_INCR = "27";//boolean

    /**
     * 用于跨部件消息跟踪
     */
    public static final String KEY_TRACE_ID = "28"; //string


    public static final String CONTENT_TYPE_BYTES = "1";
    public static final String CONTENT_TYPE_GMAP = "2";
    public static final String CONTENT_TYPE_JSON = "3";
    public static final String CONTENT_TYPE_XML = "4";

    private byte head;
    private byte version;
    private int packetType;

    private boolean zip;
    private byte secret;
    private long timestamp;
    private int tempId;
    private int packetSize;
    private int appId;

    private int headSize;
    private int bodySize;

    private String from;
    private String to;
    private String fromRegion;
    private String toRegion;
    private String via;
    private String userAgent;
    private Boolean resend = Boolean.FALSE;
    private Boolean serverReceivedConfirm = Boolean.FALSE;
    private Boolean clientReceivedConfirm = Boolean.FALSE;
    private Boolean readConfirm = Boolean.FALSE;
    private Boolean push = Boolean.FALSE;
    private Boolean ensureArrive = Boolean.FALSE;
    private Boolean pushIncr = Boolean.FALSE;
    private int pushCount;
    private String pushContent;
    private int expire;
    private Boolean bidirection = Boolean.FALSE;
    private String messageID;
    private String contentType;
    private int contentLength;
    private String service;
    private String serverMessageID;
    private String traceId;

    //群组消息时，识别消息接收方
    private String toUser;
    private Boolean store;

    private Map headMap;

    public PacketHead() {
    }

    public PacketHead copy() {
        PacketHead packetHead = new PacketHead();
        packetHead.setHead(this.head);
        packetHead.setVersion(this.version);
        packetHead.setPacketType(this.packetType);
        packetHead.setZip(this.zip);
        packetHead.setSecret(this.secret);
        packetHead.setTimestamp(this.timestamp);
        packetHead.setTempId(this.tempId);
        packetHead.setPacketSize(this.packetSize);
        packetHead.setAppId(this.appId);
        packetHead.setHeadSize(this.headSize);
        packetHead.setFrom(this.from);
        packetHead.setTo(this.to);
        packetHead.setFromRegion(this.fromRegion);
        packetHead.setToRegion(this.toRegion);
        packetHead.setVia(this.via);
        packetHead.setUserAgent(this.userAgent);
        if (this.resend) {
            packetHead.setResend(this.resend);
        }
        packetHead.setServerReceivedConfirm(this.serverReceivedConfirm);
        packetHead.setClientReceivedConfirm(this.clientReceivedConfirm);
        packetHead.setReadConfirm(this.readConfirm);
        packetHead.setPush(this.push);
        packetHead.setStore(this.store);
        packetHead.setEnsureArrive(this.ensureArrive);
        packetHead.setPushIncr(this.pushIncr);
        packetHead.setPushCount(this.pushCount);
        packetHead.setPushContent(this.pushContent);
        packetHead.setExpire(this.expire);
        if (this.bidirection) {
            packetHead.setBidirection(this.bidirection);
        }
        packetHead.setMessageID(this.messageID);
        packetHead.setContentType(this.contentType);
        packetHead.setContentLength(this.contentLength);
        packetHead.setService(this.service);
        packetHead.setServerMessageID(this.serverMessageID);
        packetHead.setToUser(this.toUser);
        packetHead.setStore(this.store);

        packetHead.buildToClientMap();
        packetHead.setTraceId(this.traceId);
        return packetHead;
    }


    public byte getHead() {
        return head;
    }

    public void setHead(byte head) {
        this.head = head;
    }

    public byte getVersion() {
        return version;
    }

    public void setVersion(byte version) {
        this.version = version;
    }

    public int getPacketType() {
        return packetType;
    }

    public void setPacketType(int packetType) {
        this.packetType = packetType;
    }

    public byte getSecret() {
        return secret;
    }

    public void setSecret(byte secret) {
        this.secret = secret;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getTempId() {
        return tempId;
    }

    public void setTempId(int tempId) {
        this.tempId = tempId;
    }

    public int getPacketSize() {
        return packetSize;
    }

    public void setPacketSize(int packetSize) {
        this.packetSize = packetSize;
    }

    public int getAppId() {
        return appId;
    }

    public void setAppId(int appId) {
        this.appId = appId;
    }

    public boolean isZip() {
        return zip;
    }

    public void setZip(boolean zip) {
        this.zip = zip;
    }

    public int getHeadSize() {
        return headSize;
    }

    public void setHeadSize(int headSize) {
        this.headSize = headSize;
    }

    public int getBodySize() {
        return bodySize;
    }

    public void setBodySize(int bodySize) {
        this.bodySize = bodySize;
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

    public String getFromRegion() {
        return fromRegion;
    }

    public void setFromRegion(String fromRegion) {
        this.fromRegion = fromRegion;
    }

    public String getToRegion() {
        return toRegion;
    }

    public void setToRegion(String toRegion) {
        this.toRegion = toRegion;
    }

    public String getVia() {
        return via;
    }

    public void setVia(String via) {
        this.via = via;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public Boolean getResend() {
        return resend;
    }

    public void setResend(Boolean resend) {
        this.resend = resend;
    }

    public Boolean getServerReceivedConfirm() {
        return serverReceivedConfirm;
    }

    public void setServerReceivedConfirm(Boolean serverReceivedConfirm) {
        this.serverReceivedConfirm = serverReceivedConfirm;
    }

    public Boolean getClientReceivedConfirm() {
        return clientReceivedConfirm;
    }

    public void setClientReceivedConfirm(Boolean clientReceivedConfirm) {
        this.clientReceivedConfirm = clientReceivedConfirm;
    }

    public Boolean getReadConfirm() {
        return readConfirm;
    }

    public void setReadConfirm(Boolean readConfirm) {
        this.readConfirm = readConfirm;
    }

    public Boolean getPush() {
        return push;
    }

    public void setPush(Boolean push) {
        this.push = push;
    }

    public int getPushCount() {
        return pushCount;
    }

    public void setPushCount(int pushCount) {
        this.pushCount = pushCount;
    }

    public String getPushContent() {
        return pushContent;
    }

    public void setPushContent(String pushContent) {
        this.pushContent = pushContent;
    }

    public int getExpire() {
        return expire;
    }

    public void setExpire(int expire) {
        this.expire = expire;
    }

    public Boolean getBidirection() {
        return bidirection;
    }

    public void setBidirection(Boolean bidirection) {
        this.bidirection = bidirection;
    }

    public String getMessageID() {
        return messageID;
    }

    public void setMessageID(String messageID) {
        this.messageID = messageID;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public int getContentLength() {
        return contentLength;
    }

    public void setContentLength(int contentLength) {
        this.contentLength = contentLength;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public Map getHeadMap() {
        return headMap;
    }

    public void setHeadMap(Map headMap) {
        this.headMap = headMap;
    }

    public String getServerMessageID() {
        return serverMessageID;
    }

    public void setServerMessageID(String serverMessageID) {
        this.serverMessageID = serverMessageID;
    }

    public Boolean getEnsureArrive() {
        return ensureArrive;
    }

    public void setEnsureArrive(Boolean ensureArrive) {
        this.ensureArrive = ensureArrive;
    }

    public Boolean getPushIncr() {
        return pushIncr;
    }

    public void setPushIncr(Boolean pushIncr) {
        this.pushIncr = pushIncr;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getToUser() {
        return toUser;
    }

    public void setToUser(String toUser) {
        this.toUser = toUser;
    }

    public Boolean getStore() {
        return store;
    }

    public void setStore(Boolean store) {
        this.store = store;
    }

    public void extractHeadMap() {
        if (headMap != null) {
            userAgent = GMapUtils.getString(headMap, KEY_USER_AGENT);
            try {
                appId = Integer.valueOf(GMapUtils.getString(headMap, KEY_APP_ID));
            } catch (NumberFormatException e) {
            }
            from = GMapUtils.getString(headMap, KEY_FROM);
            to = GMapUtils.getString(headMap, KEY_TO);

            //初始化二者设置一致
            toUser = to;
            fromRegion = GMapUtils.getString(headMap, KEY_FROM_REGION);
            toRegion = GMapUtils.getString(headMap, KEY_TO_REGION);
            timestamp = GMapUtils.getLong(headMap, KEY_TIMESTAMP);
            tempId = GMapUtils.getInt(headMap, KEY_TEMP_ID);
            if (headMap.containsKey(KEY_RESEND)) {
                resend = Boolean.valueOf(GMapUtils.getBoolean(headMap, KEY_RESEND));
            } else {
                resend = Boolean.FALSE;
            }

            if (headMap.containsKey(KEY_SERVER_RECEIVED_CONFIRM)) {
                serverReceivedConfirm = Boolean.valueOf(GMapUtils.getBoolean(headMap, KEY_SERVER_RECEIVED_CONFIRM));
            } else {
                serverReceivedConfirm = Boolean.FALSE;
            }

            if (headMap.containsKey(KEY_CLIENT_RECEIVED_CONFIRM)) {
                clientReceivedConfirm = Boolean.valueOf(GMapUtils.getBoolean(headMap, KEY_CLIENT_RECEIVED_CONFIRM));
            } else {
                clientReceivedConfirm = Boolean.FALSE;
            }

            if (headMap.containsKey(KEY_READ_CONFIRM)) {
                readConfirm = Boolean.valueOf(GMapUtils.getBoolean(headMap, KEY_READ_CONFIRM));
            } else {
                readConfirm = Boolean.FALSE;
            }

            if (headMap.containsKey(KEY_PUSH)) {
                push = Boolean.valueOf(GMapUtils.getBoolean(headMap, KEY_PUSH));
            } else {
                push = Boolean.FALSE;
            }

            if (headMap.containsKey(KEY_BIDIRECTION)) {
                bidirection = Boolean.valueOf(GMapUtils.getBoolean(headMap, KEY_BIDIRECTION));
            } else {
                bidirection = Boolean.FALSE;
            }

            if (headMap.containsKey(KEY_STORE)) {
                store = Boolean.valueOf(GMapUtils.getBoolean(headMap, KEY_STORE));
            } else {
                store = Boolean.FALSE;
            }

            if (headMap.containsKey(KEY_ENSURE_ARRIVE)) {
                ensureArrive = Boolean.valueOf(GMapUtils.getBoolean(headMap, KEY_ENSURE_ARRIVE));
            } else {
                ensureArrive = Boolean.FALSE;
            }

            if (headMap.containsKey(KEY_PUSH_INCR)) {
                pushIncr = Boolean.valueOf(GMapUtils.getBoolean(headMap, KEY_PUSH_INCR));
            } else {
                pushIncr = Boolean.FALSE;
            }


            pushCount = GMapUtils.getInt(headMap, KEY_PUSH_COUNT);
            pushContent = GMapUtils.getString(headMap, KEY_PUSH_CONTENT);
            expire = GMapUtils.getInt(headMap, KEY_EXPIRE);
            if (headMap.containsKey(KEY_BIDIRECTION)) {
                bidirection = Boolean.valueOf(GMapUtils.getBoolean(headMap, KEY_BIDIRECTION));
            } else {
                bidirection = Boolean.FALSE;
            }

            if (headMap.containsKey(KEY_SERVER_MESSAGE_ID)) {
                serverMessageID = GMapUtils.getString(headMap, KEY_SERVER_MESSAGE_ID);
            }

            messageID = GMapUtils.getString(headMap, KEY_MESSAGE_ID);
            contentType = GMapUtils.getString(headMap, KEY_CONTENT_TYPE);
            contentLength = GMapUtils.getInt(headMap, KEY_CONTENT_LENGTH);
            service = GMapUtils.getString(headMap, KEY_SERVICE);


            if (headMap.containsKey(KEY_STORE)) {
                store = Boolean.valueOf(GMapUtils.getBoolean(headMap, KEY_STORE));
            }

            traceId = GMapUtils.getString(headMap, KEY_TRACE_ID);
        }
    }

    public void buildToClientMap() {
        if (headMap == null) {
            headMap = new ListOrderedMap();
        }
        if (StringUtils.isNotBlank(via)) {
            headMap.put(KEY_VIA, via);
        }

        if (StringUtils.isNotBlank(from)) {
            headMap.put(KEY_FROM, from);
        }

        if (StringUtils.isNotBlank(to)) {
            headMap.put(KEY_TO, to);
        }

        if (StringUtils.isNotBlank(fromRegion)) {
            headMap.put(KEY_FROM_REGION, fromRegion);
        }

        if (StringUtils.isNotBlank(toRegion)) {
            headMap.put(KEY_TO_REGION, toRegion);
        }

        if (StringUtils.isNotBlank(messageID)) {
            headMap.put(KEY_MESSAGE_ID, messageID);
        }

        if (StringUtils.isNotBlank(contentType)) {
            headMap.put(KEY_CONTENT_TYPE, contentType);
        }

        if (StringUtils.isNotBlank(service)) {
            headMap.put(KEY_SERVICE, service);
        }

        if (timestamp > 0) {
            headMap.put(KEY_TIMESTAMP, timestamp);
        }

        if (tempId > 0) {
            headMap.put(KEY_TEMP_ID, tempId);
        }

        if (clientReceivedConfirm != null) {
            headMap.put(KEY_CLIENT_RECEIVED_CONFIRM, clientReceivedConfirm);
        }

        if (readConfirm != null) {
            headMap.put(KEY_READ_CONFIRM, readConfirm);
        }

        if (bidirection != null) {
            headMap.put(KEY_BIDIRECTION, bidirection);
        }

        if (StringUtils.isNotBlank(serverMessageID)) {
            headMap.put(KEY_SERVER_MESSAGE_ID, serverMessageID);
        }

        if (StringUtils.isNotBlank(traceId)) {
            headMap.put(KEY_TRACE_ID, traceId);
        }


        if (store != null) {
            headMap.put(KEY_STORE, store);
        }


    }


    public byte getMessageServiceType() {
        byte result = 0x01;
        switch (service) {
            case SERVICE_SEND_SINGE_TEXT:
            case SERVICE_SEND_SINGE_IMG:
            case SERVICE_SEND_SINGE_AUDIO:
            case SERVICE_SEND_SINGE_VIDEO:
            case SERVICE_SEND_SINGE_CMD:
                result = 0x01;
                break;
            case SERVICE_SEND_GROUP_TEXT:
            case SERVICE_SEND_GROUP_IMG:
            case SERVICE_SEND_GROUP_AUDIO:
            case SERVICE_SEND_GROUP_VIDEO:
            case SERVICE_SEND_GROUP_CMD:
                result = 0x02;
                break;
            case SERVICE_SEND_CHATROOM_TEXT:
            case SERVICE_SEND_CHATROOM_IMG:
            case SERVICE_SEND_CHATROOM_AUDIO:
            case SERVICE_SEND_CHATROOM_VIDEO:
            case SERVICE_SEND_CHATROOM_CMD:
                //聊天室
                result = 0x03;
                break;

            default:

                break;
        }
        return result;
    }

    public byte getMessageType(){
        byte result = 1;
        switch (service) {
            case SERVICE_SEND_SINGE_TEXT:
            case SERVICE_SEND_GROUP_TEXT:
            case SERVICE_SEND_CHATROOM_TEXT:
                result = 1;
                break;
            case SERVICE_SEND_SINGE_IMG:
            case SERVICE_SEND_GROUP_IMG:
            case SERVICE_SEND_CHATROOM_IMG:
                result = 3;
                break;
            case SERVICE_SEND_SINGE_AUDIO:
            case SERVICE_SEND_GROUP_AUDIO:
            case SERVICE_SEND_CHATROOM_AUDIO:
                result = 5;
                break;
            case SERVICE_SEND_SINGE_VIDEO:
            case SERVICE_SEND_GROUP_VIDEO:
            case SERVICE_SEND_CHATROOM_VIDEO:
                result = 6;
                break;
            case SERVICE_SEND_SINGE_CMD:
            case SERVICE_SEND_GROUP_CMD:
            case SERVICE_SEND_CHATROOM_CMD:
                result =11;
                break;
            default:
                break;
        }
        return result;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
    }

}

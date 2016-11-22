package com.handwin.bean;

/**
 * Created by piguangtao on 15/11/16.
 */
public class GroupCallMemberChangeInfo {
    private String type;

    private Data info;

    public GroupCallMemberChangeInfo(String type, Data info) {
        this.type = type;
        this.info = info;
    }


    public Data getInfo() {
        return info;
    }

    public void setInfo(Data info) {
        this.info = info;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public static class Data {
        private String groupId;
        private String userId;
        private Long timestamp;
        private String roomId;
        private Integer seq;

        public String getGroupId() {
            return groupId;
        }

        public void setGroupId(String groupId) {
            this.groupId = groupId;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public Long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(Long timestamp) {
            this.timestamp = timestamp;
        }

        public String getRoomId() {
            return roomId;
        }

        public void setRoomId(String roomId) {
            this.roomId = roomId;
        }

        public Integer getSeq() {
            return seq;
        }

        public void setSeq(Integer seq) {
            this.seq = seq;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("GroupCallAddMemberInfo{");
            sb.append("groupId='").append(groupId).append('\'');
            sb.append(", userId='").append(userId).append('\'');
            sb.append(", timestamp=").append(timestamp);
            sb.append(", roomId='").append(roomId).append('\'');
            sb.append('}');
            return sb.toString();
        }
    }


}

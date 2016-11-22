package com.handwin.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by piguangtao on 15/11/24.
 */
public class RichMessageInfo {

    /**
     * at的用户列表
     */
    private String atUsers;

    private List<SubContent> subContents = new ArrayList<>();

    public String getAtUsers() {
        return atUsers;
    }

    public void setAtUsers(String atUsers) {
        this.atUsers = atUsers;
    }

    public List<SubContent> getSubContents() {
        return subContents;
    }

    public void setSubContents(List<SubContent> subContents) {
        this.subContents = subContents;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("RichMessageInfo{");
        sb.append("atUsers='").append(atUsers).append('\'');
        sb.append(", subContents=").append(subContents);
        sb.append('}');
        return sb.toString();
    }

    public interface SubContent {
        String CONTENT_TYPE_PLAIN = "plain";

        String CONTENT_TYPE_AT = "at";

        String CONTENT_TYPE_USER = "user";

        String getContentType();

    }

    public static class TextSubContent implements SubContent {
        private String content;

        public TextSubContent(String content) {
            this.content = content;
        }

        @Override
        public String getContentType() {
            return CONTENT_TYPE_PLAIN;
        }

        public String getPlainContentForPush() {
            return content;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("TextSubContent{");
            sb.append("content='").append(content).append('\'');
            sb.append('}');
            return sb.toString();
        }
    }

    public static class AtSubContent implements SubContent {
        private String id;
        private String alt;
        private String name;

        @Override
        public String getContentType() {
            return CONTENT_TYPE_AT;
        }

        public AtSubContent withId(String id) {
            this.id = id;
            return this;
        }

        public AtSubContent withAlt(String alt) {
            this.alt = alt;
            return this;
        }

        public AtSubContent withName(String name) {
            this.name = name;
            return this;
        }


        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getAlt() {
            return alt;
        }

        public void setAlt(String alt) {
            this.alt = alt;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("AtSubContent{");
            sb.append("id='").append(id).append('\'');
            sb.append(", alt='").append(alt).append('\'');
            sb.append(", name='").append(name).append('\'');
            sb.append('}');
            return sb.toString();
        }
    }

    public static class UserSubContent implements SubContent {
        private String id;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public UserSubContent withId(String id) {
            this.id = id;
            return this;
        }

        @Override
        public String getContentType() {
            return CONTENT_TYPE_USER;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("UserSubContent{");
            sb.append("id='").append(id).append('\'');
            sb.append('}');
            return sb.toString();
        }
    }
}

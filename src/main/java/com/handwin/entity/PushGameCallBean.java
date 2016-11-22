package com.handwin.entity;

import java.util.List;

/**
 * Created by yangwei on 14-11-26.
 */
public class PushGameCallBean extends PushCallBean {
    private List<String> gameIds;
    private Integer mediaType;

    public List<String> getGameIds() {
        return gameIds;
    }

    public void setGameIds(List<String> gameIds) {
        this.gameIds = gameIds;
    }

    public Integer getMediaType() {
        return mediaType;
    }

    public void setMediaType(Integer mediaType) {
        this.mediaType = mediaType;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PushGameCallBean{");
        sb.append("gameIds=").append(gameIds);
        sb.append("mediaType=").append(mediaType);
        sb.append(super.toString());
        sb.append('}');
        return sb.toString();
    }
}

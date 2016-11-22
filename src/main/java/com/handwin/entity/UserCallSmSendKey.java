package com.handwin.entity;

import info.archinnov.achilles.annotations.ClusteringColumn;
import info.archinnov.achilles.annotations.Column;
import info.archinnov.achilles.annotations.PartitionKey;

/**
 * Created by piguangtao on 15/9/7.
 */
public class UserCallSmSendKey {
    @PartitionKey
    @Column(name = "receiver_id")
    private String receiverId;

    @ClusteringColumn
    @Column(name = "from_id")
    private String fromId;

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getFromId() {
        return fromId;
    }

    public void setFromId(String fromId) {
        this.fromId = fromId;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("UserCallSmSendKey{");
        sb.append("receiverId='").append(receiverId).append('\'');
        sb.append(", fromId='").append(fromId).append('\'');
        sb.append('}');
        return sb.toString();
    }
}

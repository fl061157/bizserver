package com.handwin.entity;

import info.archinnov.achilles.annotations.Column;
import info.archinnov.achilles.annotations.CompoundPrimaryKey;
import info.archinnov.achilles.annotations.Entity;
import info.archinnov.achilles.annotations.TypeTransformer;

import java.sql.Timestamp;

/**
 * Created by piguangtao on 15/9/7.
 */
@Entity(table = "user_call_sm_send")
public class UserCallSmSend {

    @CompoundPrimaryKey
    private UserCallSmSendKey key;

    @Column(name = "count")
    private Integer count;

    @Column(name = "update_time")
    @TypeTransformer(valueCodecClass = TimestampToString.class)
    private Timestamp updateTime;


    public UserCallSmSendKey getKey() {
        return key;
    }

    public void setKey(UserCallSmSendKey key) {
        this.key = key;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Timestamp getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("UserCallSmSend{");
        sb.append("key=").append(key);
        sb.append(", count=").append(count);
        sb.append(", updateTime=").append(updateTime);
        sb.append('}');
        return sb.toString();
    }
}

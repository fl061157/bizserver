package com.handwin.localentity;

import info.archinnov.achilles.annotations.Column;
import info.archinnov.achilles.annotations.Entity;
import info.archinnov.achilles.annotations.Id;
import info.archinnov.achilles.type.Counter;



/**
 * 用户本地未读取消息个数,推送时候带上
 */
@Entity(table = "user_localmsg_counter", comment = "用户本地未读取消息个数")
public class UserLocalMsgCounter {
    @Column(name = "user_id")
    @Id
    private String userId;

    @Column
    private Counter counter;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Counter getCounter() {
        return counter;
    }

    public void setCounter(Counter counter) {
        this.counter = counter;
    }

    public UserLocalMsgCounter() {

    }
}

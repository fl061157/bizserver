package com.handwin.entity;

import info.archinnov.achilles.annotations.Column;
import info.archinnov.achilles.annotations.EmbeddedId;
import info.archinnov.achilles.annotations.Entity;

@Entity(table = "phone_new_books", keyspace = "faceshow", comment = "手机联系人")
public class PhoneBook {
    @EmbeddedId
    private PhoneKey id;

    @Column( name = "create_time")
    private Long createTime;

    @Column
    private String name;

    public PhoneKey getId() {
        return id;
    }

    public void setId(PhoneKey id) {
        this.id = id;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PhoneBook() {

    }
}


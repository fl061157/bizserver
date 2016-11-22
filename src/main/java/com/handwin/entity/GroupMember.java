package com.handwin.entity;

import info.archinnov.achilles.annotations.Column;
import info.archinnov.achilles.annotations.EmbeddedId;
import info.archinnov.achilles.annotations.Entity;
import org.codehaus.jackson.annotate.JsonIgnore;

import java.util.Date;

@Entity(table = "group_members", keyspace = "faceshow", comment = "群组成员表")
public class GroupMember {
    @EmbeddedId
    private GroupKey id;

    @Column
    private Date createTime;

    @Column
    private Date updateTime;

    @Column
    private String name;

    @Column
    private Integer status;

    @Column
    /**
     * 成员在群组中的序号
     * 用于群组视频 网络层对用户的标示
     */
    private Integer seq;

    @JsonIgnore
    @Column
    /**
     * 增加该成员的数据中心
     */
    private String idc;

    public GroupKey getId() {
        return id;
    }

    public void setId(GroupKey id) {
        this.id = id;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getSeq() {
        return seq;
    }

    public void setSeq(Integer seq) {
        this.seq = seq;
    }

    public String getIdc() {
        return idc;
    }

    public void setIdc(String idc) {
        this.idc = idc;
    }

    public boolean equals(Object o) {
        if (!(o instanceof GroupMember)) return false;
        GroupMember mo = (GroupMember) o;

        return id.equals(mo.getId());
    }

    public GroupMember() {

    }


}

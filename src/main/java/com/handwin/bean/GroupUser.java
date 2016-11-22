package com.handwin.bean;

import com.handwin.entity.User;
import org.springframework.beans.BeanUtils;

/**
 * Created by piguangtao on 15/11/11.
 */
public class GroupUser extends User {
    /**
     * 成员在群组中的序号
     * 用于群组视频 网络层对用户的标示
     */
    private Integer seq;

    public Integer getSeq() {
        return seq;
    }

    public void setSeq(Integer seq) {
        this.seq = seq;
    }

    public static GroupUser createFromUser(User user) {
        GroupUser groupUser = new GroupUser();
        BeanUtils.copyProperties(user, groupUser);
        return groupUser;
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("GroupUser{");
        sb.append("seq=").append(seq);
        sb.append('}');
        return sb.toString();
    }
}

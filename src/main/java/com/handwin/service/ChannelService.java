package com.handwin.service;

import com.handwin.bean.Platform;
import com.handwin.entity.User;
import com.handwin.exception.ServerException;
import com.handwin.server.Channel;
import com.handwin.server.proto.BaseRequestMessage;

import java.util.List;
import java.util.Map;

/**
 * @author fangliang
 */
public interface ChannelService {

    public Channel buildChannel(BaseRequestMessage baseRequest);

    public Channel findChannel(User user) throws ServerException;

    public Map<Platform , List<Channel>> findChannelMap(User user) throws ServerException;

    public String findUserID(Channel channel);

}

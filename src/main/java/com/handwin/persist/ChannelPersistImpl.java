package com.handwin.persist;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Session;
import com.handwin.exception.ServerException;
import com.handwin.localentity.ChannelInfoBean;
import com.handwin.localentity.ChannelInfoKey;
import com.handwin.packet.ChannelMode;
import info.archinnov.achilles.persistence.PersistenceManager;
import info.archinnov.achilles.type.OptionsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by fangliang on 5/1/15.
 */
@Service
public class ChannelPersistImpl implements ChannelPersist, InitializingBean {

    @Autowired
    @Qualifier("bizEntityManager")
    private PersistenceManager manager;
    private Session session;
    private PreparedStatement statement;
    private PreparedStatement removeStatement;
    private PreparedStatement removeStatementOne;

    private static final Logger logger = LoggerFactory.getLogger(ChannelPersistImpl.class);

    @Override
    public void afterPropertiesSet() throws Exception {
        session = manager.getNativeSession();
        statement = session.prepare("INSERT INTO user_channel(" +
                "user_id," +
                "app_id," +
                "channel_uuid," +
                "channel_mode," +
                "client_version," +
                "id," +
                "ip," +
                "port," +
                "session_id," +
                "tcp_zone_code," +
                "user_zone_code," +
                "node_id" +
                ") values(?,?,?, ?,?,?, ?,?,?, ?,?,?)");
        removeStatement = session.prepare("DELETE FROM user_channel WHERE user_id = ? and app_id = ?");
        removeStatementOne = session.prepare("DELETE FROM user_channel WHERE user_id = ? and app_id = ? and channel_uuid =?");
    }


    @Override
    public void insert(ChannelInfoBean channelInfoBean, int expire) throws ServerException {
        manager.insert(channelInfoBean, OptionsBuilder.withTtl(expire));
    }

    @Override
    public ChannelInfoBean get(String userID, int appID, String channelUUID) throws ServerException {
        try {
            return manager.find(ChannelInfoBean.class, new ChannelInfoKey(userID, appID, channelUUID));
        } catch (Exception e) {
            logger.error(String.format("get channel error : userId:%s, channelUUID:%s", userID, channelUUID), e);
            throw new ServerException(ServerException.ErrorCode.ResourceNotFoundError, e);
        }
    }

    @Override
    public void delete(String userID, int appID, String channelUUID) throws ServerException {
        BoundStatement boundStatement = new BoundStatement(removeStatementOne);
        try {
            session.executeAsync(boundStatement.bind(userID, appID, channelUUID));
        } catch (Exception e) {
            logger.error(String.format("delete channel error : userID:%s, channelUUID:%s", userID, channelUUID), e);
            throw new ServerException(ServerException.ErrorCode.CanNotHandleIoError, e);
        }
    }

    @Override
    public void delete(String userID, int appID) throws ServerException {
        BoundStatement boundStatement = new BoundStatement(removeStatement);
        try {
            session.executeAsync(boundStatement.bind(userID, appID));
        } catch (Exception e) {
            logger.error(String.format("delete channel error : userID:%s, channelUUID:%s", userID), e);
            throw new ServerException(ServerException.ErrorCode.CanNotHandleIoError, e);
        }
    }

    @Override
    public List<ChannelInfoBean> find(String userID, int appID) {
        return manager.sliceQuery(ChannelInfoBean.class).forSelect().withPartitionComponents(userID).withClusterings(appID).get();
    }

    @Override
    public void updateChannelMode(String userID, int appID, String channelUUID, ChannelMode channelMode, int expire) throws ServerException {
        ChannelInfoBean channelInfoBean = get(userID, appID, channelUUID); //TODO TO BAD
        if (channelInfoBean != null) {
            channelInfoBean.setChannelMode(channelMode.getValue());
            channelInfoBean = manager.removeProxy(channelInfoBean);
            insert(channelInfoBean, expire);
        }
    }


    @Override
    public void expire(String userID, int appID, String channelUUID, int expire) throws ServerException {
        ChannelInfoBean channelInfoBean = get(userID, appID, channelUUID);
        if (channelInfoBean != null) {
            channelInfoBean = manager.removeProxy(channelInfoBean);
            insert(channelInfoBean, expire);
        }
    }


    @Override
    public void expire(ChannelInfoBean channelInfoBean, int expire) throws ServerException {
        if (channelInfoBean != null) {
            ChannelInfoBean bean = manager.removeProxy(channelInfoBean);
            insert(bean, expire);
        }
    }
}

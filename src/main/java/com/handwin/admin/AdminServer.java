package com.handwin.admin;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Created by fangliang on 19/12/14.
 */
@Service
public class AdminServer implements InitializingBean, DisposableBean {

    @Autowired
    private AdminChannelInitializer adminChannelInitializer;

    @Value("#{configproperties['admin.listen.port']}")
    private int adminListenPort;

    private EventLoopGroup bossGroup = new NioEventLoopGroup(1);
    private EventLoopGroup workerGroup = new NioEventLoopGroup(1);
    private ServerBootstrap serverBootstrap = new ServerBootstrap();
    private Channel channel;

    private static final Logger logger = LoggerFactory.getLogger(AdminServer.class);

    public void start() throws Exception {
        Thread thread = new Thread(() -> {
            serverBootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                    .childHandler(adminChannelInitializer);
            try {
                ChannelFuture channelFuture = serverBootstrap.bind(adminListenPort).sync();
                channelFuture.addListener(future -> logger.info("OperationComplete Success :" + future.isSuccess()));
                channel = channelFuture.channel();
                ChannelFuture closeFuture = channel.closeFuture();
                closeFuture.addListener(future -> logger.warn("Close AdminServer!"));
                closeFuture.sync();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                bossGroup.shutdownGracefully();
                workerGroup.shutdownGracefully();
            }
        });
        thread.setDaemon(true);
        thread.start();
    }


    public void stop() {
        if (channel != null) {
            channel.close();
        }
    }

    @Override
    public void destroy() throws Exception {
        if (logger.isInfoEnabled()) {
            logger.info("AdminServer Stop !");
        }
        stop();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (logger.isInfoEnabled()) {
            logger.info("AdminServer start !");
        }
        start();
    }
}

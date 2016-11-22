package com.handwin.service;

import com.handwin.entity.BizOutputMessage;
import com.handwin.entity.TcpMessage;
import com.handwin.exception.ServerException;
import com.handwin.rabbitmq.MessageBuilder;
import com.handwin.utils.RoundRobinList;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedTransferQueue;

/**
 * Created by fangliang on 16/5/15.
 */
public abstract class TransportExecutor {

    protected RoundRobinList<TransportExecutor> transportExecutorRound;

    protected MessageBuilder messageBuilder;

    protected FailingService failingService;

    protected BlockingQueue<TcpMessage> queue;

    protected volatile boolean active = false;

    protected volatile boolean stop = false;

    private int checkMillSeconds = 5 * 1000;


    private Thread thread;

    protected static final Logger logger = LoggerFactory.getLogger(TransportExecutor.class);

    protected final static byte[] HEARTBEAT_FRAME = new byte[]{1};

    public TransportExecutor(MessageBuilder messageBuilder,
                             RoundRobinList<TransportExecutor> transportExecutorRound,
                             FailingService failingService) {
        this.transportExecutorRound = transportExecutorRound;
        this.messageBuilder = messageBuilder;
        this.queue = new LinkedTransferQueue<>();
        this.failingService = failingService;
    }

    public void add(TcpMessage tcpMessage) {
        queue.add(tcpMessage);
    }

    protected void next(TcpMessage tcpMessage) {
        if (transportExecutorRound != null) {
            transportExecutorRound.get().add(tcpMessage);
        } else {
            failingService.handle(tcpMessage.getPacketBody(), tcpMessage.getExtraBody(), tcpMessage.getTraceID());
        }
    }


    public boolean write(TcpMessage tcpMessage) {

        BizOutputMessage bizOutputMessage = tcpMessage.getBizOutputMessage(messageBuilder);
        try {
            if (tcpMessage.getChannelInfo() == null || StringUtils.isBlank(tcpMessage.getChannelInfo().getTcpZoneCode())) {
                logger.warn("User RegionCode is empty user.id :{} ", tcpMessage.getChannelInfo().getUserId());
                return true;
            }
            return request(bizOutputMessage);
        } catch (ServerException e) {
            logger.error("mq send error : " + e.getMessage(), e);
            return reQueue(tcpMessage);
        }
    }


    protected abstract boolean request(BizOutputMessage bizOutputMessage);

    protected abstract boolean reQueue(TcpMessage tcpMessage);


    protected abstract boolean heartBeat(byte[] heartBeatFrame);

    public void start() {
        doStart();
        thread = new Thread(new Worker(this));
        thread.setDaemon(true);
        thread.start();
    }

    public void stop() {
        stop = true;
        active = false;
        thread.interrupt();
        doStop();
    }

    protected abstract void doStart();

    protected abstract void doStop();

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setStop(boolean stop) {
        this.stop = stop;
    }

    public boolean isActive() {
        return active;
    }

    public static class Worker implements Runnable {

        private TransportExecutor executor;

        public Worker(TransportExecutor executor) {
            this.executor = executor;
        }

        @Override
        public void run() {
            while (!executor.stop && !Thread.currentThread().isInterrupted()) {
                TcpMessage tcpMessage = null;
                if (executor.active) {
                    try {
                        tcpMessage = executor.queue.take();
                    } catch (InterruptedException e) {
                        executor.setStop(true);
                        Thread.currentThread().interrupt();
                    }
                    if (tcpMessage != null) {
                        boolean result;
                        try {

                            String traceID = tcpMessage.getTraceID();
                            MDC.remove("TraceID");
                            if (org.apache.commons.lang3.StringUtils.isNotBlank(traceID)) {
                                MDC.put("TraceID", traceID);
                            }

                            if (logger.isInfoEnabled()) {
                                try {
                                    logger.info("[WRITE] MQ to.userID:{} , traceID:{} , nodeID:{}  " , tcpMessage.getChannelInfo().getUserId() ,
                                            tcpMessage.getTraceID() , tcpMessage.getNodeID() );
                                } catch (Exception e) {
                                }
                            }

                            result = executor.write(tcpMessage);
                        } catch (Throwable e) {
                            result = false;
                            logger.error("Write message error ", e);
                        }
                        executor.setActive(result);
                        if (!result) {
                            try {
                                executor.next(tcpMessage);
                            } catch (Exception e) {
                                logger.error("handle next error ", e);
                            }
                        }
                    }
                } else {
                    boolean result;
                    try {
                        result = executor.heartBeat(TransportExecutor.HEARTBEAT_FRAME);
                    } catch (Throwable e) {
                        logger.error("HeatBeat error", e);
                        result = false;
                    }
                    executor.setActive(result);
                    if (!result) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("ZeroMq HeartBeat error !");
                        }
                        try {
                            Thread.sleep(executor.checkMillSeconds);
                        } catch (InterruptedException e) {
                        }
                    }
                }

            }
        }
    }


}

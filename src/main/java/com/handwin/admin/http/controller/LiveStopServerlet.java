package com.handwin.admin.http.controller;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by fangliang on 16/9/13.
 */

@Service
public class LiveStopServerlet extends HttpServlet {

    private Map<String, AtomicBoolean> STOP_CACHE = new ConcurrentHashMap<>();

    private final String parameterChatRoomID = "chatRoomID";
    private final String parameterStatus = "status";
    private final String parameterStatusValueStop = "stop";
    public final String myServletName = "LiveStopServerlet";

    private static final Logger logger = LoggerFactory.getLogger(LiveStopServerlet.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String chatRoomID = req.getParameter(parameterChatRoomID);
        if (StringUtils.isNotBlank(chatRoomID)) {
            String status = req.getParameter(parameterStatus);

            logger.info("[LiveStopSpeach] chatRoomID:{} , status:{}.", chatRoomID, status);

            if (StringUtils.isNotBlank(status) && status.toLowerCase().equals(parameterStatusValueStop)) {
                AtomicBoolean ab = STOP_CACHE.get(chatRoomID);
                if (ab != null) {
                    ab.set(true);
                } else {
                    ab = new AtomicBoolean(true);
                    STOP_CACHE.put(chatRoomID, ab);
                }
            } else {
                STOP_CACHE.remove(chatRoomID);
            }
        }
    }

    public boolean isStop(String chatRoomID) {
        AtomicBoolean stop = STOP_CACHE.get(chatRoomID);
        if (stop == null) return false;
        return stop.get();
    }


}

package com.handwin.admin.http.controller;

import com.alibaba.fastjson.JSON;
import com.handwin.exception.ServerException;
import com.handwin.rabbitmq.ProtocolOutptTemplate;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by fangliang on 21/1/15.
 */
@Service
public class RabbitmqCheckerServlet extends HttpServlet {

    @Autowired
    private ProtocolOutptTemplate protocolOutptTemplate;

    @Value("#{configproperties['checker.queue']}")
    private String checkerRouteKey;

    private static final String DEFAULT_EXCHANGE = "";

    private static final String MESSAGE = "message";

    public final String myServletName = "RabbitmqCheckerServlet";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String message = req.getParameter(MESSAGE);
        if (StringUtils.isBlank(message)) {
            message = "check rabbitmq";
        }
        String echo = null;
        String r;
        if (echo == null) {
            r = JSON.toJSONString(new RedisCheckerServlet.Result(0, "Error"));
        } else {
            r = JSON.toJSONString(new RedisCheckerServlet.Result(1, echo));
        }
        OutputStream out = resp.getOutputStream();
        try {
            out.write(r.getBytes());
        } finally {
            out.close();
        }
    }
}

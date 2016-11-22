package com.handwin.admin.http.controller;

import com.alibaba.fastjson.JSON;
import com.handwin.entity.User;
import com.handwin.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by fangliang on 22/1/15.
 */
@Service
public class CassendraCheckerServlet extends HttpServlet {

    @Autowired
    private UserService userService;

    protected final static String SECRETARY_ID = "88888888888888888888888888888888";

    public final String myServletName = "CassendraCheckerServlet";

    private static final Logger logger = LoggerFactory.getLogger(CassendraCheckerServlet.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        RedisCheckerServlet.Result result;
        try {
            User user = userService.findById(SECRETARY_ID, 0);
            if (user == null) {
                result = new RedisCheckerServlet.Result(0, "User empty !");
            } else {
                result = new RedisCheckerServlet.Result(1, "Nickname:" + user.getNickname());
            }
        } catch (Exception e) {
            logger.error("Cassendra check error", e);
            result = new RedisCheckerServlet.Result(0, e.getCause().toString());
        }
        OutputStream out = resp.getOutputStream();
        try {
            out.write(JSON.toJSONString(result).getBytes());
        } finally {
            out.close();
        }
    }
}

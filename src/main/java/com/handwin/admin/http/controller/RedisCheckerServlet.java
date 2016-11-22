package com.handwin.admin.http.controller;

import com.alibaba.fastjson.JSON;
import com.handwin.persist.StatusStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by fangliang on 21/1/15.
 */
@Service
public class RedisCheckerServlet extends HttpServlet {

    private final static Result OK = new Result(1, "OK");

    private final static String REDIS_KEY_PREFIX = "R_K_";

    private static final Logger logger = LoggerFactory.getLogger(RedisCheckerServlet.class);

    public final String myServletName = "RedisCheckerServlet";

    @Autowired
    @Qualifier(value = "statusClusterStoreImpl")
    private StatusStore statusStore;


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        int random = ThreadLocalRandom.current().nextInt();
        String key = String.format("%s%d", REDIS_KEY_PREFIX, random);
        String value = key;
        OutputStream out = resp.getOutputStream();
        try {
            statusStore.setEx(key.getBytes(), 30, value.getBytes());
            byte[] v = statusStore.get(key.getBytes());
            if (v == null || !value.equals(new String(v))) {
                logger.error("checkRedis get error key:{} value wrong !", key);
                out.write(JSON.toJSONString(new Result(0, String.format("get key error key: %s , value wrong !", key))).getBytes());
                return;
            }
            out.write(JSON.toJSONString(OK).getBytes());
        } catch (Throwable e) {
            logger.error("checkRedis error key:{} ", key, e);
            out.write(JSON.toJSONString(new Result(0, String.format("set key error key: %s , r: %s", key, e.getCause().toString()))).getBytes());
        } finally {
            out.close();
        }
    }


    public static class Result {

        public Result(int code, String message) {
            this.code = code;
            this.message = message;
        }

        public Result() {

        }

        private int code;

        private String message;

        public int getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }


}

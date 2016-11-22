package com.handwin.admin.http.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class MysqlCheckerServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(MysqlCheckerServlet.class);

    public final String myServletName = "MysqlCheckerServlet";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {


        OutputStream out = resp.getOutputStream();
        try {
            out.write("OK".getBytes());
        } finally {
            out.close();
        }

    }
}

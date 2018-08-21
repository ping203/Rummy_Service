/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vng.tfa.common;

import com.athena.services.impl.ServiceImpl;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

/**
 *
 * @author tunm
 */
public class StatsServlet extends HttpServlet
{

    private static final Logger logger_ = Logger.getLogger(StatsServlet.class);
    static final String authorize_key = "0@uth";
    static final int max_expires = (60 * 60 * 12); //12h

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        this.dumpStats(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        this.dumpStats(req, resp);
    }

    private void dumpStats(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        String key = req.getParameter("key");
        if (key == null)
        {
            key = "";
        }

        if (!key.equals(authorize_key))
        {
            return;
        }

        String type = req.getParameter("type");
        String auto = req.getParameter("auto");
        if (type == null)
        {
            type = "";
        }
        if (type.equals("selectG"))
        {
            String out = "";
            out += "<h1>SelectG</h1>";
//            out += ServiceImpl.reqStats.dumpHtmlStats();
            if (auto != null)
            {
                resp.setHeader("refresh", "1");
            }
            this.out(out, resp);
        }
        /*if (type.equals("loginStats"))
        {
            String out = "";
            out += "<h1>Stats Facebook Login</h1>";
            out += FacebookThaiNewLoginHandler.reqStats.dumpHtmlStats();
            if (auto != null)
            {
                resp.setHeader("refresh", "1");
            }
            this.out(out, resp);
        }
        /*else if (type.equals("dtStats"))
        {
        	String out = "";
            out += "<h1>Stats Facebook Login Dt</h1>";
            out += DautruongLoginHandler.reqStats.dumpHtmlStats();
            if (auto != null)
            {
                resp.setHeader("refresh", "1");
            }
            this.out(out, resp);
        }
        else if (type.equals("dtfaceStats"))
        {
        	String out = "";
            out += "<h1>Stats Facebook Login Dt</h1>";
            out += FacebookDTLoginHandler.reqStats.dumpHtmlStats();
            if (auto != null)
            {
                resp.setHeader("refresh", "1");
            }
            this.out(out, resp);
        }
        else if (type.equals("52Stats"))
        {
        	String out = "";
            out += "<h1>Stats Facebook Login 52</h1>";
            out += FacebookFun52LoginHandler.reqStats.dumpHtmlStats();
            if (auto != null)
            {
                resp.setHeader("refresh", "1");
            }
            this.out(out, resp);
        }
        else if (type.equals("ZingloginStats"))
        {
        	String out = "";
            out += "<h1>Stats Facebook Login 52</h1>";
            out += ZingLoginHandler.reqStats.dumpHtmlStats();
            if (auto != null)
            {
                resp.setHeader("refresh", "1");
            }
            this.out(out, resp);
        }*/
        else {
        	return ;
        }
    }

    private void out(String content, HttpServletResponse resp)
    {
        try
        {
            resp.setCharacterEncoding("utf-8");
            resp.addHeader("Content-Type", "text/html");
            resp.addHeader("Server", "OAuth2");
            PrintWriter out = resp.getWriter();
            out.println(content);
        }
        catch (Exception ex)
        {
            logger_.info(ex.getMessage(), ex);
        }

    }
}

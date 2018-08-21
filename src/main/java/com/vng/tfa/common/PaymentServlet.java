/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vng.tfa.common;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.athena.services.api.ServiceContract;
import com.athena.services.impl.ServiceImpl;
import com.cubeia.firebase.api.service.ServiceContext;
import com.dst.ServerSource;

/**
 *
 * @author tunm
 */
public class PaymentServlet extends HttpServlet
{

//    private static final Logger logger_ = Logger.getLogger(PaymentServlet.class);
    static final String authorize_key = "0@uth";
    static final int max_expires = (60 * 60 * 12); //12h
    private static Logger logger_ = Logger.getLogger("PaymentHandler");
    private ServiceContext context;
    public PaymentServlet(ServiceContext context) {
		// TODO Auto-generated constructor stub
    	this.context = context;
	}

	@Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
    	//ServiceImpl.
    	//ServiceImpl.getInstance().PaymentIndo(req.getParameter("TranID"), 9);
    	ServiceImpl serviceContract = (ServiceImpl) context.getParentRegistry().getServiceInstance(ServiceContract.class);
    	serviceContract.PaymentIndo(req.getParameter("TranID"), ServerSource.THAI_SOURCE);
        this.out("get ....", resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
    	//ServiceImpl.
    	//ServiceImpl.getInstance().PaymentSendToClientVN(req.getParameter("Username"), req.getParameter("PaySource"));
    	this.out("post ....", resp);
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

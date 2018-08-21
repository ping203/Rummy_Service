package com.vng.tfa.common;

import java.lang.management.ManagementFactory;

import javax.servlet.Filter;

import org.apache.log4j.Logger;
import org.eclipse.jetty.jmx.MBeanContainer;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.FilterMapping;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlets.GzipFilter;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import com.cubeia.firebase.api.service.ServiceContext;


public class WebServer extends Thread
{

    private static Logger logger_ = Logger.getLogger(WebServer.class);
    
    private ServiceContext context;
    public WebServer(ServiceContext con) {
		// TODO Auto-generated constructor stub
    	this.context = con;
	}

	@Override
    public void run()
    {
        try
        {
            this.startWebServer();
        }
        catch (Exception ex)
        {
            logger_.error("Webserver error", ex);
        }
    }

    public void startWebServer() throws Exception
    {
        int port_listen = 5000;//Integer.valueOf(System.getProperty("zport"));
        if (port_listen == 0)
        {
            logger_.error("zport not found");
            System.exit(-1);
        }
        logger_.info("get rest listen_port from zport=" + port_listen);

        //int acceptors = Integer.valueOf(Config.getParam("rest", "acceptors"));
        int min_threads = 10000;//Integer.valueOf(Config.getParam("jetty_threadpool", "minthread"));
        int max_threads = 500;//Integer.valueOf(Config.getParam("jetty_threadpool", "maxthread"));

        Server server = new Server();

        // Setup JMX
        MBeanContainer mbContainer = new MBeanContainer(ManagementFactory.getPlatformMBeanServer());
        mbContainer.addBean(Log.getLogger(this.getClass()));
        server.addBean(mbContainer);
        server.getContainer().addEventListener(mbContainer);

        QueuedThreadPool threadPool = new QueuedThreadPool();
        threadPool.setMinThreads(min_threads);
        threadPool.setMaxThreads(max_threads);
        server.setThreadPool(threadPool);

        SelectChannelConnector connector = new SelectChannelConnector();
        connector.setPort(port_listen);
        connector.setMaxIdleTime(60000);
        //connector.setConfidentialPort(8443);
        connector.setStatsOn(false);
        connector.setLowResourcesConnections(20000);
        connector.setLowResourcesMaxIdleTime(5000);
        //connector.setAcceptors(acceptors);	
        //connector.setAcceptQueueSize(acceptors*2);

        server.setConnectors(new Connector[]
                {
                    connector
                });
        ServletHandler handler = new ServletHandler();
        server.setHandler(handler);
        handler.addServletWithMapping(StatsServlet.class,"/stats/*");
        handler.addServletWithMapping(IndexServlet.class,"/*");
        //handler.addServletWithMapping(PaymentServlet.class,"/payment");
        handler.addServletWithMapping(new ServletHolder(new PaymentServlet(context)), "/payment");

        // Filter
        FilterHolder gzipFilterHolder = this.createGzipFilterHolder();
        handler.addFilter(gzipFilterHolder, this.createFilterMapping("/*", gzipFilterHolder));


        server.setStopAtShutdown(true);
        server.setGracefulShutdown(1000);//1 giay se dong
        server.setSendServerVersion(false);

        ShutdownThread obj = new ShutdownThread(server);
        Runtime.getRuntime().addShutdownHook(obj);

        try
        {
            server.start();
            server.join();
        }
        catch (Exception ex)
        {
            logger_.info(ex.getMessage(), ex);
        }
    }

    private FilterHolder createGzipFilterHolder()
    {
        Filter gzip = new GzipFilter();
        FilterHolder filterHolder = new FilterHolder(gzip);
        filterHolder.setName("gzip");
        return filterHolder;
    }

    private FilterMapping createFilterMapping(String pathSpec, FilterHolder filterHolder)
    {
        FilterMapping filterMapping = new FilterMapping();
        filterMapping.setPathSpec(pathSpec);
        filterMapping.setFilterName(filterHolder.getName());
        return filterMapping;
    }
}
class ShutdownThread extends Thread
{

    private Server server;
    private static Logger logger_ = Logger.getLogger(ShutdownThread.class);

    public ShutdownThread(Server server)
    {
        this.server = server;
    }

    @Override
    public void run()
    {
        logger_.info("Waiting for shut down!");
        try
        {
            //dump cache
            //InitAndMonitor.dumpCache();
            server.stop();
        }
        catch (Exception ex)
        {
            logger_.info(ex.getMessage(), ex);
        }
        logger_.info("Server shutted down!");
    }
}
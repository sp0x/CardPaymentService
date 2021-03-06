package com.cardService.network.http;


import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.servlet.ServletHandler;

import java.net.InetSocketAddress;
import java.util.Properties;

public class TransactionCallbackServer {

    private final InetSocketAddress _listenAddr;
    private final Server _server;

    public TransactionCallbackServer(HttpConfig config, Properties props){
        HandlerCollection handlers = new HandlerCollection();
        _listenAddr = new InetSocketAddress(config.getHost(), config.getPort());
        _server = new Server(_listenAddr);
        _server.setHandler(handlers);
        ServletHandler handler = new ServletHandler();
        handlers.addHandler(handler);
        _server.setErrorHandler(new CustomErrorHandler());
        handler.addServletWithMapping(TransactionCallbackServlet.class, "/");
    }

    public void listenAndBlock() throws Exception {
        _server.start();
        _server.join();
    }
}

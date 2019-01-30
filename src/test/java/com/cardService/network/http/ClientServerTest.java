package com.cardService.network.http;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.NetworkConnector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import javax.servlet.Servlet;
import java.net.InetSocketAddress;

public class ClientServerTest {
    private final SslContextFactory sslContextFactory;
    protected Server server;
    protected HttpClient client;
    protected NetworkConnector connector;

    public ClientServerTest(SslContextFactory sslContextFactory){
        this.sslContextFactory = sslContextFactory;
    }

    public void start(Handler handler) throws Exception
    {
        startServer(handler);
        startClient();
    }

    public void start(Class<? extends Servlet> servlet, String path) throws Exception {

        ServletHandler handler = new ServletHandler();
        handler.addServletWithMapping(servlet, path);
        startServer(handler);
        startClient();
    }

    protected void startServer(Handler handler) throws Exception
    {
        if (sslContextFactory != null)
        {
            sslContextFactory.setEndpointIdentificationAlgorithm("");
            sslContextFactory.setKeyStorePath("src/test/resources/keystore.jks");
            sslContextFactory.setKeyStorePassword("storepwd");
            sslContextFactory.setTrustStorePath("src/test/resources/truststore.jks");
            sslContextFactory.setTrustStorePassword("storepwd");
        }
        if (server == null)
        {
            QueuedThreadPool serverThreads = new QueuedThreadPool();
            serverThreads.setName("server");
            server = new Server(serverThreads);
        }
        connector = new ServerConnector(server, sslContextFactory);
        server.addConnector(connector);
        server.setHandler(handler);
        server.start();
    }

    protected void startClient() throws Exception
    {
        QueuedThreadPool clientThreads = new QueuedThreadPool();
        clientThreads.setName("client");
        client = new HttpClient(sslContextFactory);
        client.setExecutor(clientThreads);
        client.start();
    }

}

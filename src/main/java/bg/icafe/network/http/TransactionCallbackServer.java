package bg.icafe.network.http;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;

import java.net.InetSocketAddress;
import java.util.Properties;

public class TransactionCallbackServer {

    private final InetSocketAddress _listenAddr;
    private final Server _server;

    public TransactionCallbackServer(HttpConfig config, Properties props){
        _listenAddr = new InetSocketAddress(config.getHost(), config.getPort());
        _server = new Server(_listenAddr);
        ServletHandler handler = new ServletHandler();
        _server.setHandler(handler);
        handler.addServletWithMapping(TransactionCallbackServlet.class, "/*");
    }

    public void listenAndBlock() throws Exception {
        _server.start();
        _server.dumpStdErr();
        _server.join();
    }
}

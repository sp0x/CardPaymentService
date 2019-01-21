package bg.icafe.network.http;

import bg.icafe.Config;
import lv.tietoenator.cs.ecomm.merchant.ConfigurationException;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.*;

public class TransactionCallbackServletTest extends ClientServerTest {

    private TransactionCallbackServer _callbackListener;

    public TransactionCallbackServletTest() {
        super(null);
    }

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void doPost() {
        TransactionCallbackServlet servlet = new TransactionCallbackServlet();
        try {
            start(TransactionCallbackServlet.class, "/");
        } catch (Exception e) {
            e.printStackTrace();
        }
        URI uri = URI.create("http://localhost:" + connector.getLocalPort() + "/ok");
        String responseBody=null;
        try {
            ContentResponse response = client.newRequest(uri)
                    .method(HttpMethod.POST)
                    .param("error","system error: (unknown)")
                    .param("trans_id","3WarxxROHW8ZhSMeMZNKFtLbCSs=")
                    .timeout(5, TimeUnit.SECONDS)
                    .send();
            responseBody = new String(response.getContent(), StandardCharsets.UTF_8);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        System.out.println(responseBody);
        assertNotNull(responseBody);
        assertTrue(responseBody.trim().contains("Transaction failed"));
        assertTrue(responseBody.trim().contains("Declined"));
    }
}
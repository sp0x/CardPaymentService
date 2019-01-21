package bg.icafe.network.http;

public class HttpConfig {
    private final String _hostname;
    private final int _port;

    public HttpConfig(String hostname, int port) {
        _hostname = hostname;
        _port = port;
    }

    public String getHost() {
        return _hostname;
    }

    public int getPort() {
        return _port;
    }
}

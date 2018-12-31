package bg.icafe.network;

public class MqConfig
{
    private final String _pass;
    private final String _port;
    private final String _host;
    private final String _user;

    public MqConfig(String host, String port, String user, String pass){
        _host = host;
        _port = port;
        _user = user;
        _pass = pass;
    }

    public String getPass() {
        return _pass;
    }

    public String getPort() {
        return _port;
    }

    public String getHost() {
        return _host;
    }

    public String getUser() {
        return _user;
    }
}

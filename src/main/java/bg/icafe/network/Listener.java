package bg.icafe.network;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class Listener
{
    private final String _host;
    private final String _username;
    private final String _port;
    private Connection _connection;
    private Channel _channel;

    public Listener(String host, String port, String username){
        _host = host;
        _username = username;
        _port = port;
    }

    public Listener(MqConfig mqconfig) {
        this(mqconfig.getHost(), mqconfig.getPort(), mqconfig.getUser());
    }

    public void connect(String password) throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(_host);
        factory.setUsername(_username);
        if(_port!=null && _port.length()>0) factory.setPort(Integer.parseInt(_port));
        factory.setPassword(password);
        _connection = factory.newConnection();
        _channel = _connection.createChannel();
        System.out.println("Connected to mq host " + _host);
    }

    public Channel getChannel() {
        return _channel;
    }
}
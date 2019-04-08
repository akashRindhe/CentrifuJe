package centrifuje.registry;

import com.uber.tchannel.api.TChannel;
import com.uber.tchannel.api.handlers.RawRequestHandler;
import com.uber.tchannel.messages.RawRequest;
import com.uber.tchannel.messages.RawResponse;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class RegistrarImpl implements Registrar {
    private final TChannel _channel;
    private  InetAddress _serverHost;
    private int _serverPort;

    public RegistrarImpl(InetAddress serverHost, int serverPort) {
        _serverHost = serverHost;
        _serverPort = serverPort;
        _channel = new TChannel.Builder("server")
                         .setServerHost(_serverHost)
                         .setServerPort(_serverPort)
                         .build();
    }

    public RegistrarImpl(int serverPort) throws UnknownHostException {
        this(InetAddress.getLocalHost(), serverPort);
    }


    public void start() throws InterruptedException {
        _channel.makeSubChannel("server")
                .register("register", new RawRequestHandler() {
                    private int count = 0;

                    @Override
                    public RawResponse handleImpl(RawRequest request) {
                        System.out.println(String.format("Request received: header: %s, body: %s",
                                request.getHeader(),
                                request.getBody()));

                        return new RawResponse.Builder(request)
                                .setTransportHeaders(request.getTransportHeaders())
                                .setHeader("Polo")
                                .setBody("Pong!")
                                .build();
                    }
                });


        _channel.makeSubChannel("server")
                .register("heartbeat", new RawRequestHandler() {
                    private int count = 0;

                    @Override
                    public RawResponse handleImpl(RawRequest request) {
                        System.out.println(String.format("Request received: header: %s, body: %s",
                                request.getHeader(),
                                request.getBody()));

                        return new RawResponse.Builder(request)
                                .setTransportHeaders(request.getTransportHeaders())
                                .setHeader("Polo")
                                .setBody("Pong!")
                                .build();
                    }
                });
        _channel.listen();
    }

    public void stop() {
        _channel.shutdown();
    }
}

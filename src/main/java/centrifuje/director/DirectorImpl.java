package centrifuje.director;

import centrifuje.service.FileServiceProvider;
import centrifuje.service.ServiceProvider;
import com.uber.tchannel.api.SubChannel;
import com.uber.tchannel.api.TChannel;
import com.uber.tchannel.api.TFuture;
import com.uber.tchannel.messages.RawRequest;
import com.uber.tchannel.messages.RawResponse;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.*;

/**
 *
 */
public class DirectorImpl implements Director {
   private final TChannel _channel;
   private boolean _registered;
   private  InetAddress _registrarAddress;
   private int _registrarPort;
   private Map<String, URI> _serviceMappings;
   private ServiceProvider _serviceProvider;
   private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
   private ScheduledFuture<?> _heartbeatService;


    private DirectorImpl(String name, InetAddress address, int port, ServiceProvider serviceProvider) {
        _channel = new TChannel.Builder(name)
                                .setServerHost(address)
                                .setServerPort(port)
                                .build();
        _serviceProvider = serviceProvider;
    }

    private DirectorImpl(String directorName, int port, ServiceProvider serviceProvider) throws UnknownHostException {
        this(directorName, InetAddress.getLocalHost(), port, serviceProvider);
    }

    private DirectorImpl(int port, ServiceProvider serviceProvider) throws UnknownHostException {
        this("director", port, serviceProvider);
    }

    private void register(int port) throws UnknownHostException {
        register(InetAddress.getLocalHost(), port);
    }

    private void register(InetAddress address, int port) {
        boolean result = false;

        _registrarAddress = address;
        _registrarPort = port;
        System.out.println("Server: " + _registrarAddress + " Port: " + _registrarPort);

        try {
            SubChannel registerChannel = _channel.makeSubChannel("server");
            RawRequest request = new RawRequest.Builder("server", "register")
                    .setHeader("Meta-data")
                    .setBody("server:" + _channel.getHost().toString() + ";port:" + _channel.getPort() + ";services:")
                    .build();

            int attempts = 1;

            int _maxRetries = 5;
            while (attempts < _maxRetries) {
                TFuture<RawResponse> future = registerChannel.send(request, _registrarAddress, _registrarPort);
                try (RawResponse response = future.get()) {
                    if (!response.isError()) {
                        System.out.println(String.format("Response received: response code: %s, header: %s, body: %s",
                                response.getResponseCode(),
                                response.getHeader(),
                                response.getBody()));
                        postRegistrationActions();
                        _registered = true;

                        break;
                    } else {
                        attempts++;
                        System.out.println(String.format("Got error response: %s",
                                response.toString()));
                    }
                }
            }

            if (!_registered) {
                _channel.shutdown();
            }
        } catch(Exception ignored) {
        }
    }

    private void postRegistrationActions() {
        scheduleHeartbeats();
    }

    public boolean disconnect() throws Exception{
        if (!_registered) {
            throw new Exception("Not registered");
        }
        _heartbeatService.cancel(false);
        SubChannel disconnectChannel = _channel.makeSubChannel("server");
        RawRequest request = new RawRequest.Builder("server", "disconnect")
                .setHeader("Disconnect").build();
        TFuture<RawResponse> future = disconnectChannel.send(request, _registrarAddress, _registrarPort);
        try (RawResponse response = future.get()) {
            if (!response.isError()) {
                System.out.println(String.format("Response received: response code: %s, header: %s, body: %s",
                        response.getResponseCode(),
                        response.getHeader(),
                        response.getBody()));
                postRegistrationActions();
                _registered = false;
            } else {
                System.out.println(String.format("Got error response: %s",
                        response.toString()));
            }
        }
        return !_registered;
    }

    private void scheduleHeartbeats() {
        SubChannel heartbeatChannel = _channel.makeSubChannel("server");
        RawRequest request = new RawRequest.Builder("server", "register")
                .setHeader("Heartbeat")
                .setBody("server:" + _channel.getHost().toString() + ";port:" + _channel.getPort() + ";services:")
                .build();

        Runnable heartbeatTask = () -> {
            TFuture<RawResponse> future = heartbeatChannel.send(request, _registrarAddress, _registrarPort);
            try (RawResponse response = future.get()) {
                if (!response.isError()) {
                    System.out.println(String.format("Response received: response code: %s, header: %s, body: %s",
                            response.getResponseCode(),
                            response.getHeader(),
                            response.getBody()));
                    postRegistrationActions();
                    _registered = false;
                } else {
                    System.out.println(String.format("Got error response: %s",
                            response.toString()));
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        };
        _heartbeatService = scheduler.schedule(heartbeatTask, 8,  TimeUnit.SECONDS);
    }




    public static void main(String[] args) throws Exception {
        DirectorImpl d = new DirectorImpl(12312, new FileServiceProvider("asdsa"));
         d.register(8888);
    }
}

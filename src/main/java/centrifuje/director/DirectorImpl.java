package centrifuje.director;

import com.uber.tchannel.api.SubChannel;
import com.uber.tchannel.api.TChannel;
import com.uber.tchannel.api.TFuture;
import com.uber.tchannel.messages.RawRequest;
import com.uber.tchannel.messages.RawResponse;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public class DirectorImpl implements Director {
   private final TChannel _channel;
   private boolean _registered;
    private  InetAddress _registrarAddress;
   private int _registrarPort;
   private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);


    private DirectorImpl(String name, InetAddress address, int port) {
        _channel = new TChannel.Builder(name)
                                .setServerHost(address)
                                .setServerPort(port)
                                .build();
    }

    /**
     *
     * @param directorName
     * @param port
     * @throws UnknownHostException
     */
    private DirectorImpl(String directorName, int port) throws UnknownHostException {
        this(directorName, InetAddress.getLocalHost(), port);
    }


    private DirectorImpl(int port) throws UnknownHostException {
        this("director", port);
    }


    private void register(int port) throws UnknownHostException {
        register(InetAddress.getByName(null), port);
    }

    private boolean register(InetAddress address, int port) {
        boolean result = false;

        _registrarAddress = address;
        _registrarPort = port;

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
        } catch(Exception e) {
        }
        return result;
    }

    private void postRegistrationActions() {
        scheduleHeartbeats();
    }

    public boolean disconnect() throws Exception{
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
        SubChannel hearbeatChannel = _channel.makeSubChannel("server");
        RawRequest request = new RawRequest.Builder("server", "register")
                .setHeader("Heartbeat")
                .setBody("server:" + _channel.getHost().toString() + ";port:" + _channel.getPort() + ";services:")
                .build();

        Runnable hearbeatTask = () -> {
            TFuture<RawResponse> future = hearbeatChannel.send(request, _registrarAddress, _registrarPort);
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
        scheduler.schedule(hearbeatTask, 8,  TimeUnit.SECONDS);
    }



    public static void main(String[] args) throws Exception {
        DirectorImpl d = new DirectorImpl(5467);
         d.register(23123);
    }
}

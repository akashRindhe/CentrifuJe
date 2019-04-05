package centrifuje;

import com.uber.tchannel.api.TChannel;
import com.uber.tchannel.api.handlers.RawRequestHandler;
import com.uber.tchannel.messages.RawRequest;
import com.uber.tchannel.messages.RawResponse;

import java.net.InetAddress;

public class Main {
/*
    public static void main(String[] args) {
        try {
            TChannel ch = new TChannel.Builder("registrar")
                    .setServerHost(InetAddress.getByName(null))
                    .setServerPort(12333)
                    .build();

            ch.makeSubChannel("Register")
                    .register("register", new RawRequestHandler() {

                        private int count = 0;

                        @Override
                        public RawResponse handleImpl(RawRequest request) {
                            System.out.println(String.format("Request received: header: %s, body: %s",
                                    request.getHeader(),
                                    request.getBody()));

                            count++;
                            switch (count%2) {
                                case 1:
                                    return new RawResponse.Builder(request)
                                            .setTransportHeaders(request.getTransportHeaders())
                                            .setHeader("Polo")
                                            .setBody("Pong!")
                                            .build();
                                case 0:
                                    return new RawResponse.Builder(request)
                                            .setTransportHeaders(request.getTransportHeaders())
                                            .setResponseCode(ResponseCode.Error)
                                            .setHeader("Polo")
                                            .setBody("I feel bad ...")
                                            .build();
                                default:
                                    throw new UnsupportedOperationException("I feel very bad!");
                            }

                        }
                    });

            ch.listen();

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    */

 private static final boolean running = true;
    public static void main(String[] args) throws Exception {
        TChannel server = createServer();

        while(running){}

    }

    private static TChannel createServer() throws Exception {

        // create TChannel
        TChannel tchannel = new TChannel.Builder("registrar")
                .setServerHost(InetAddress.getByName(null))
                .setServerPort(23123)
                .build();

        // create sub channel to register the service and endpoint handler
        tchannel.makeSubChannel("server")
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


        tchannel.makeSubChannel("server")
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

        tchannel.listen();

        return tchannel;
    }
}

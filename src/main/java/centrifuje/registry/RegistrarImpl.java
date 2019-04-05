package centrifuje.registry;

import com.uber.tchannel.api.SubChannel;
import com.uber.tchannel.api.TChannel;
import com.uber.tchannel.messages.RawRequest;

import java.net.UnknownHostException;

class RegistrarImpl implements Registrar {



    @Override
    public void run() {
        TChannel client = new TChannel.Builder("client").build();
        SubChannel s = client.makeSubChannel("server");
        RawRequest rw = new RawRequest.Builder("server", "pong").build();
        //s.send(rw, InetAddress.getLocalHost(), )
    }
}

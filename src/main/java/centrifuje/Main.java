package centrifuje;

import centrifuje.registry.Registrar;
import centrifuje.registry.RegistrarImpl;
import com.uber.tchannel.api.TChannel;
import com.uber.tchannel.api.handlers.RawRequestHandler;
import com.uber.tchannel.messages.RawRequest;
import com.uber.tchannel.messages.RawResponse;

import java.net.InetAddress;

public class Main {
    public static void main(String[] args) throws Exception {
        Registrar registrar = new RegistrarImpl(8888);
        registrar.start();
    }
}

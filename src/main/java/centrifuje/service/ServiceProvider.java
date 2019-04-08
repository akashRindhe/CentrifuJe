package centrifuje.service;

public interface ServiceProvider {
    void initialize();

    String getService(String serviceName);
}

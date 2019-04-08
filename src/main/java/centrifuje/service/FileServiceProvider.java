package centrifuje.service;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class FileServiceProvider implements ServiceProvider {
    private final String _fileName;
    private final Map<String, String> _serviceEndPointMappings;

    public FileServiceProvider(String fileName) {
        _fileName = fileName;
        _serviceEndPointMappings = new HashMap<>();
    }


    /**
     * Reads the file to initialize mapping between service name and service endpoint
     * Expects the mapping to be of the form
     * serviceName:serviceEndpoint
     */
    @Override
    public void initialize() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(_fileName));
            String line;
            while ((line = reader.readLine()) != null)
            {
                String[] splitLine = line.split(":");
                if (splitLine.length != 2) {
                    // TODO implement warning/error loggin
                }
                _serviceEndPointMappings.put(splitLine[0].trim(), splitLine[1].trim());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getService(String serviceName) {
        return _serviceEndPointMappings.get(serviceName);
    }


}

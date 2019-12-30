package com.beumuth.math.core.internal.client;

import com.beumuth.math.MathClient;
import com.beumuth.math.client.Clients;
import com.beumuth.math.core.internal.environment.EnvironmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ClientService {
    @Autowired
    private EnvironmentService environmentService;

    public <T extends MathClient> T getClient(Class<T> clientClass) {
        return Clients.getClient(clientClass, environmentService.getActiveEnvironment().baseUrl);
    }
}

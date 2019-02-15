package com.beumuth.collections.core.client;

import com.beumuth.collections.client.Clients;
import com.beumuth.collections.client.application.ApplicationClient;
import com.beumuth.collections.client.environment.EnvironmentClient;
import com.beumuth.collections.core.environment.EnvironmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ClientService {
    @Autowired
    private EnvironmentService environmentService;

    public ApplicationClient getApplicationClient() {
        return Clients.getClient(ApplicationClient.class, environmentService.getActiveEnvironment().baseUrl);
    }

    public EnvironmentClient getEnvironmentClient() {
        return Clients.getClient(EnvironmentClient.class, environmentService.getActiveEnvironment().baseUrl);
    }
}

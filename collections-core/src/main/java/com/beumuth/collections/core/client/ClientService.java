package com.beumuth.collections.core.client;

import com.beumuth.collections.CollectionsClient;
import com.beumuth.collections.client.Clients;
import com.beumuth.collections.client.application.ApplicationClient;
import com.beumuth.collections.client.element.ElementClient;
import com.beumuth.collections.client.environment.EnvironmentClient;
import com.beumuth.collections.core.environment.EnvironmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ClientService {
    @Autowired
    private EnvironmentService environmentService;

    public <T extends CollectionsClient> T getClient(Class<T> clientClass) {
        return Clients.getClient(clientClass, environmentService.getActiveEnvironment().baseUrl);
    }
}

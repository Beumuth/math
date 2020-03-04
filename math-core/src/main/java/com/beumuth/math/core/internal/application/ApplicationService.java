package com.beumuth.math.core.internal.application;

import com.beumuth.math.client.external.gson.Gsons;
import com.beumuth.math.client.internal.application.ApplicationConfiguration;
import com.beumuth.math.client.internal.application.ApplicationMode;
import com.beumuth.math.core.internal.environment.EnvironmentService;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
public class ApplicationService {

    private EnvironmentService environmentService;
    private String configurationLocation;

    public ApplicationService(
        @Autowired EnvironmentService environmentService,
        @Value("${configurationLocation}") String configurationLocation
    ) {
        this.environmentService = environmentService;
        this.configurationLocation = configurationLocation;
    }

    public boolean doesConfigurationExist() {
        return new File(configurationLocation).exists();
    }

    public ApplicationConfiguration getApplicationConfiguration() {
        try {
            return Gsons
                .prettyPrintGson()
                .fromJson(
                    FileUtils.readFileToString(new File(configurationLocation), StandardCharsets.UTF_8),
                    ApplicationConfiguration.class
                );
        }
        catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setConfiguration(ApplicationConfiguration configuration) {
        try {
            FileUtils.writeStringToFile(
                new File(configurationLocation),
                Gsons
                    .prettyPrintGson()
                    .toJson(configuration),
                StandardCharsets.UTF_8
            );
        } catch(IOException e) {
            throw new RuntimeException("Could not save internal to file [" + configurationLocation + "]", e);
        }
    }

    public ApplicationMode getApplicationMode() {
        return getApplicationConfiguration().mode;
    }

    public void setApplicationMode(ApplicationMode mode) {
        ApplicationConfiguration configuration = getApplicationConfiguration();
        configuration.mode = mode;
        setConfiguration(configuration);
    }
}

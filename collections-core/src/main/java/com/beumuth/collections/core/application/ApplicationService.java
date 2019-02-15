package com.beumuth.collections.core.application;

import com.beumuth.collections.client.application.ApplicationConfiguration;
import com.beumuth.collections.client.application.ApplicationMode;
import com.beumuth.collections.client.environment.EnvironmentConfiguration;
import com.beumuth.collections.core.database.DatabaseService;
import com.beumuth.collections.core.environment.EnvironmentService;
import com.google.gson.Gson;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
public class ApplicationService {

    @Autowired
    private EnvironmentService environmentService;
    @Autowired
    private DatabaseService databaseService;

    @Value("${configurationLocation}")
    private String configurationLocation;

    @Autowired
    @Qualifier("prettyPrint")
    private Gson gson;

    public boolean doesConfigurationExist() {
        return new File(configurationLocation).exists();
    }

    public ApplicationConfiguration getApplicationConfiguration() {
        try {
            return gson.fromJson(
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
                gson.toJson(configuration),
                StandardCharsets.UTF_8
            );
        } catch(IOException e) {
            throw new RuntimeException("Could not save configuration to file [" + configurationLocation + "]", e);
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

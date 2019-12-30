package com.beumuth.math.core.internal.environment;

import com.beumuth.math.client.internal.application.ApplicationConfiguration;
import com.beumuth.math.client.internal.environment.EnvironmentConfiguration;
import com.beumuth.math.core.internal.application.ApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

@Service
public class EnvironmentService {

    @Autowired
    private ApplicationService applicationService;

    public Set<EnvironmentConfiguration> getEnvironments() {
        return applicationService.getApplicationConfiguration().environments;
    }

    public Optional<EnvironmentConfiguration> getEnvironment(String name) {
        Set<EnvironmentConfiguration> environments = applicationService.getApplicationConfiguration().environments;
        for(EnvironmentConfiguration environment : environments) {
            if(environment.name.equalsIgnoreCase(name)) {
                return Optional.of(environment);
            }
        }

        return Optional.empty();
    }

    public void updateEnvironment(String name, EnvironmentConfiguration newEnvironment) {
        ApplicationConfiguration application = applicationService.getApplicationConfiguration();
        for(EnvironmentConfiguration configuration : application.environments) {
            if(configuration.name.equals(name)) {
                application.environments.remove(configuration);
                application.environments.add(newEnvironment);
                break;
            }
        }

        //If the updated environment is the active environment, set the activeEnvironment to the newEnvironment's name
        //in case the name changed.
        if(application.activeEnvironment.equalsIgnoreCase(name)) {
            application.activeEnvironment = newEnvironment.name;
        }

        applicationService.setConfiguration(application);
    }

    public boolean doesEnvironmentExist(String name) {
        Set<EnvironmentConfiguration> environments = applicationService.getApplicationConfiguration().environments;
        for(EnvironmentConfiguration environment : environments) {
            if (environment.name.equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    public EnvironmentConfiguration getActiveEnvironment() {
        return getEnvironment(
            applicationService.getApplicationConfiguration().activeEnvironment
        ).get();
    }

    public void setActiveEnvironment(String environmentName) {
        ApplicationConfiguration application = applicationService.getApplicationConfiguration();
        application.activeEnvironment = environmentName;
        applicationService.setConfiguration(application);
    }

    public void addEnvironment(EnvironmentConfiguration environmentConfiguration) {
        ApplicationConfiguration application = applicationService.getApplicationConfiguration();
        application.environments.add(environmentConfiguration);
        applicationService.setConfiguration(application);
    }

    public void deleteEnvironment(String environmentName) {
        ApplicationConfiguration application = applicationService.getApplicationConfiguration();
        for(EnvironmentConfiguration environment : application.environments) {
            if(environment.name.equalsIgnoreCase(environmentName)) {
                application.environments.remove(environment);
                break;
            }
        }
        applicationService.setConfiguration(application);
    }
}

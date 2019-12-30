package com.beumuth.math.client.internal.application;

import com.beumuth.math.client.internal.environment.EnvironmentConfiguration;
import com.google.common.collect.Sets;
import lombok.AllArgsConstructor;

import java.util.Set;

@AllArgsConstructor
public class ApplicationConfiguration {
    public ApplicationMode mode;
    public Set<EnvironmentConfiguration> environments;
    public String activeEnvironment;

    /**
     * Copy constructor
     * @param other
     */
    public ApplicationConfiguration(ApplicationConfiguration other) {
        mode = other.mode;
        environments = Sets.newHashSet();
        for(EnvironmentConfiguration environment : other.environments) {
            environments.add(new EnvironmentConfiguration(environment));
        }
        activeEnvironment = other.activeEnvironment;
    }

    public boolean containsEnvironmentByName(String name) {
        for(EnvironmentConfiguration environment : environments) {
            if(environment.name.equals(name)) {
                return true;
            }
        }

        return false;
    }

    public static ApplicationModeStep newBuilder() {
        return new ApplicationConfigurationBuilder();
    }

    public static ApplicationConfiguration copy(ApplicationConfiguration configuration) {
        return new ApplicationConfiguration(configuration);
    }

    public interface ApplicationModeStep {
        AddFirstEnvironmentStep inLiveMode();
        AddFirstEnvironmentStep inTestMode();
    }

    public interface AddFirstEnvironmentStep {
        AddAnotherEnvironmentStep addEnvironment(EnvironmentConfiguration environment);
    }

    public interface AddAnotherEnvironmentStep {
        AddAnotherEnvironmentStep addAnotherEnvironment(EnvironmentConfiguration environment);
        BuildStep setActiveEnvironment(String name);
    }

    public interface BuildStep {
        ApplicationConfiguration build();
    }
}
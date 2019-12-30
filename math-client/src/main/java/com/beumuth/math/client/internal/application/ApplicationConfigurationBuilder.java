package com.beumuth.math.client.internal.application;

import com.beumuth.math.client.internal.environment.EnvironmentConfiguration;
import com.google.common.collect.Sets;

import java.util.Set;

public class ApplicationConfigurationBuilder implements
    ApplicationConfiguration.ApplicationModeStep,
    ApplicationConfiguration.AddFirstEnvironmentStep,
    ApplicationConfiguration.AddAnotherEnvironmentStep,
    ApplicationConfiguration.BuildStep {

    private ApplicationMode applicationMode;
    private Set<EnvironmentConfiguration> environments;
    private String activeEnvironment;

    public ApplicationConfigurationBuilder() {
        this.environments = Sets.newHashSet();
    }

    public ApplicationConfiguration.AddFirstEnvironmentStep inLiveMode() {
        this.applicationMode = ApplicationMode.LIVE;
        return this;
    }

    public ApplicationConfiguration.AddFirstEnvironmentStep inTestMode() {
        this.applicationMode = ApplicationMode.TEST;
        return this;
    }

    public ApplicationConfiguration.AddAnotherEnvironmentStep addEnvironment(EnvironmentConfiguration environment) {
        environments.add(environment);
        return this;
    }

    public ApplicationConfiguration.AddAnotherEnvironmentStep addAnotherEnvironment(EnvironmentConfiguration environment) {
        environments.add(environment);
        return this;
    }

    public ApplicationConfiguration.BuildStep setActiveEnvironment(String name) {
        activeEnvironment = name;
        return this;
    }

    public ApplicationConfiguration build() {
        return new ApplicationConfiguration(
            applicationMode,
            environments,
            activeEnvironment
        );
    }
}

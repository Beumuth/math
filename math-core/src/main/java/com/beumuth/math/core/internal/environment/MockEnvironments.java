package com.beumuth.math.core.internal.environment;

import com.beumuth.math.client.internal.application.ApplicationMode;
import com.beumuth.math.client.internal.environment.EnvironmentConfiguration;
import com.beumuth.math.core.internal.database.MockDatabaseConfigurations;
import com.google.common.collect.ImmutableMap;

public class MockEnvironments {
    public static EnvironmentConfiguration valid() {
        EnvironmentConfiguration environment = new EnvironmentConfiguration();
        environment.name = "mockEnvironment";
        environment.databaseConfigurations = ImmutableMap.of(
            ApplicationMode.LIVE, MockDatabaseConfigurations.withDatabaseName("mock-database"),
            ApplicationMode.TEST, MockDatabaseConfigurations.withDatabaseName("mock-database-test")
        );
        return environment;
    }
}

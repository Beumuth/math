package com.beumuth.collections.core.environment;

import com.beumuth.collections.client.environment.EnvironmentConfiguration;
import com.beumuth.collections.core.database.MockDatabaseConfigurations;

public class MockEnvironments {
    public static EnvironmentConfiguration validEnvironment() {
        EnvironmentConfiguration environment = new EnvironmentConfiguration();
        environment.name = "mockEnvironment";
        environment.baseUrl = "http://mock.com/collections";
        environment.databaseConfiguration = MockDatabaseConfigurations.validDatabaseConfigurations();
        return environment;
    }
}

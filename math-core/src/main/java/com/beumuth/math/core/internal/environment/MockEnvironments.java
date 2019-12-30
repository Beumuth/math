package com.beumuth.math.core.internal.environment;

import com.beumuth.math.client.internal.environment.EnvironmentConfiguration;
import com.beumuth.math.core.internal.database.MockDatabaseConfigurations;

public class MockEnvironments {
    public static EnvironmentConfiguration validEnvironment() {
        EnvironmentConfiguration environment = new EnvironmentConfiguration();
        environment.name = "mockEnvironment";
        environment.baseUrl = "http://mock.com/collections";
        environment.databaseConfiguration = MockDatabaseConfigurations.validDatabaseConfigurations();
        return environment;
    }
}

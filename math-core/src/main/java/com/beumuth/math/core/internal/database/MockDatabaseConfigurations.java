package com.beumuth.math.core.internal.database;

import com.beumuth.math.client.internal.database.DatabaseConfiguration;

public class MockDatabaseConfigurations {
    public static DatabaseConfiguration validDatabaseConfigurations() {
        return DatabaseConfiguration.newBuilder()
            .withGivenHost("http://mock.com")
            .withGivenPort(3306)
            .withGivenUsername("mockUser")
            .withGivenPassword("mockPassword")
            .withGivenDatabaseName("mock-math")
            .withGivenIntegrationTestDatabaseName("mock-math-test")
            .build();
    }
}

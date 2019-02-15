package com.beumuth.collections.core.database;

import com.beumuth.collections.client.database.DatabaseConfiguration;

public class MockDatabaseConfigurations {
    public static DatabaseConfiguration validDatabaseConfigurations() {
        return DatabaseConfiguration.newBuilder()
            .withGivenHost("http://mock.com")
            .withGivenPort(3306)
            .withGivenUsername("mockUser")
            .withGivenPassword("mockPassword")
            .withGivenDatabaseName("mock-collections")
            .withGivenIntegrationTestDatabaseName("mock-collections-test")
            .build();
    }
}

package com.beumuth.math.core.internal.database;

import com.beumuth.math.client.internal.database.DatabaseConfiguration;

public class MockDatabaseConfigurations {
    public static DatabaseConfiguration withDatabaseName(String databaseName) {
        return DatabaseConfiguration.newBuilder()
            .withHost("http://mock.com")
            .withPort(3306)
            .withUsername("mockUser")
            .withPassword("mockPassword")
            .withDatabase(databaseName)
            .build();
    }
}

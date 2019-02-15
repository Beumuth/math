package com.beumuth.collections.client.database;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class DatabaseConfiguration {
    public String host;
    public Integer port;
    public String username;
    public String password;
    public String database;
    public String integrationTestDatabase;

    /**
     * Copy constructor
     */
    public DatabaseConfiguration(DatabaseConfiguration other) {
        host = other.host;
        port = other.port;
        username = other.username;
        password = other.password;
        database = other.database;
        integrationTestDatabase = other.integrationTestDatabase;
    }

    public static WithHostStep newBuilder() {
        return new DatabaseConfigurationBuilder();
    }

    public static class DatabaseConfigurationBuilder implements
        WithHostStep,
        WithPortStep,
        WithUsernameStep,
        WithPasswordStep,
        WithDatabaseNameStep,
        WithIntegrationTestDatabaseNameStep,
        BuildStep {

        private DatabaseConfiguration databaseConfiguration;

        public WithPortStep withLocalhost() {
            databaseConfiguration = new DatabaseConfiguration();
            databaseConfiguration.host = "localhost";
            return this;
        }

        public WithPortStep withGivenHost(String host) {
            databaseConfiguration = new DatabaseConfiguration();
            databaseConfiguration.host = host;
            return this;
        }

        public WithUsernameStep withDefaultMySqlPort() {
            databaseConfiguration.port = 3306;
            return this;
        }

        public WithUsernameStep withGivenPort(int port) {
            databaseConfiguration.port = port;
            return this;
        }

        public WithPasswordStep withRootUsername() {
            databaseConfiguration.username = "root";
            return this;
        }

        public WithPasswordStep withGivenUsername(String username) {
            databaseConfiguration.username = username;
            return this;
        }

        public WithDatabaseNameStep withNoPassword() {
            databaseConfiguration.password = "";
            return this;
        }

        public WithDatabaseNameStep withGivenPassword(String password) {
            databaseConfiguration.password = password;
            return this;
        }

        public WithIntegrationTestDatabaseNameStep withGivenDatabaseName(String databaseName) {
            databaseConfiguration.database = databaseName;
            return this;
        }

        public BuildStep withGivenIntegrationTestDatabaseName(String databaseName) {
            databaseConfiguration.integrationTestDatabase = databaseName;
            return this;
        }

        public DatabaseConfiguration build() {
            return databaseConfiguration;
        }
    }

    public interface WithHostStep {
        WithPortStep withLocalhost();
        WithPortStep withGivenHost(String host);
    }

    public interface WithPortStep {
        WithUsernameStep withDefaultMySqlPort();
        WithUsernameStep withGivenPort(int port);
    }

    public interface WithUsernameStep {
        WithPasswordStep withRootUsername();
        WithPasswordStep withGivenUsername(String username);
    }

    public interface WithPasswordStep {
        WithDatabaseNameStep withNoPassword();
        WithDatabaseNameStep withGivenPassword(String password);
    }

    public interface WithDatabaseNameStep {
        WithIntegrationTestDatabaseNameStep withGivenDatabaseName(String databaseName);
    }

    public interface WithIntegrationTestDatabaseNameStep {
        BuildStep withGivenIntegrationTestDatabaseName(String databaseName);
    }

    public interface BuildStep {
        DatabaseConfiguration build();
    }
}
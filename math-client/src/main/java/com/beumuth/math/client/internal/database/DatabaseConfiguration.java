package com.beumuth.math.client.internal.database;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class DatabaseConfiguration {
    public String host;
    public Integer port;
    public String username;
    public String password;
    public String database;

    /**
     * Copy constructor
     */
    public DatabaseConfiguration(DatabaseConfiguration other) {
        host = other.host;
        port = other.port;
        username = other.username;
        password = other.password;
        database = other.database;
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
        BuildStep {

        private DatabaseConfiguration databaseConfiguration;

        public WithPortStep withLocalhost() {
            databaseConfiguration = new DatabaseConfiguration();
            databaseConfiguration.host = "localhost";
            return this;
        }

        public WithPortStep withHost(String host) {
            databaseConfiguration = new DatabaseConfiguration();
            databaseConfiguration.host = host;
            return this;
        }

        public WithUsernameStep withDefaultMySqlPort() {
            databaseConfiguration.port = 3306;
            return this;
        }

        public WithUsernameStep withPort(int port) {
            databaseConfiguration.port = port;
            return this;
        }

        public WithPasswordStep withRootUsername() {
            databaseConfiguration.username = "root";
            return this;
        }

        public WithPasswordStep withUsername(String username) {
            databaseConfiguration.username = username;
            return this;
        }

        public WithDatabaseNameStep withNoPassword() {
            databaseConfiguration.password = "";
            return this;
        }

        public WithDatabaseNameStep withPassword(String password) {
            databaseConfiguration.password = password;
            return this;
        }

        public BuildStep withDatabase(String databaseName) {
            databaseConfiguration.database = databaseName;
            return this;
        }

        public DatabaseConfiguration build() {
            return databaseConfiguration;
        }
    }

    public interface WithHostStep {
        WithPortStep withLocalhost();
        WithPortStep withHost(String host);
    }

    public interface WithPortStep {
        WithUsernameStep withDefaultMySqlPort();
        WithUsernameStep withPort(int port);
    }

    public interface WithUsernameStep {
        WithPasswordStep withRootUsername();
        WithPasswordStep withUsername(String username);
    }

    public interface WithPasswordStep {
        WithDatabaseNameStep withNoPassword();
        WithDatabaseNameStep withPassword(String password);
    }

    public interface WithDatabaseNameStep {
        BuildStep withDatabase(String databaseName);
    }

    public interface BuildStep {
        DatabaseConfiguration build();
    }
}
package com.beumuth.math.core.internal.database;

import com.beumuth.math.client.internal.database.DatabaseConfiguration;
import com.mysql.cj.jdbc.Driver;
import org.apache.commons.dbcp2.BasicDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Supplier;

/**
 * A DataSource that allows for multiple database connections.
 * The database name to use is based on a given supplier.
 */
public class MultiDataSource extends BasicDataSource {
    private final Supplier<DatabaseConfiguration> databaseConfigurationSupplier;

    public MultiDataSource(Supplier<DatabaseConfiguration> databaseConfigurationSupplier) {
        setDriverClassName(Driver.class.getCanonicalName());
        this.databaseConfigurationSupplier = databaseConfigurationSupplier;
    }

    @Override
    public Connection getConnection() throws SQLException {
        DatabaseConfiguration databaseConfiguration = databaseConfigurationSupplier.get();
        setUrl(
            "jdbc:mysql://" + databaseConfiguration.host + ":" + databaseConfiguration.port + "/" +
                databaseConfiguration.database + "?allowPublicKeyRetrieval=true&useSSL=false"
        );
        setUsername(databaseConfiguration.username);
        setPassword(databaseConfiguration.password);
        return super.getConnection();
    }
}
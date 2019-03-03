package com.beumuth.collections.core.database;

import com.beumuth.collections.client.application.ApplicationMode;
import com.beumuth.collections.client.database.DatabaseConfiguration;
import com.beumuth.collections.core.application.ApplicationService;
import com.beumuth.collections.core.environment.EnvironmentService;
import com.mysql.cj.jdbc.Driver;
import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.SQLException;

@Component
public class CollectionsDataSource extends BasicDataSource {
    @Autowired
    private ApplicationService applicationService;
    @Autowired
    private EnvironmentService environmentService;
    @Autowired
    private DatabaseService databaseService;

    public CollectionsDataSource() {
        setDriverClassName(Driver.class.getCanonicalName());
    }

    @Override
    public Connection getConnection() throws SQLException {

        ApplicationMode applicationMode = applicationService.getApplicationMode();
        DatabaseConfiguration databaseConfiguration = environmentService.getActiveEnvironment().databaseConfiguration;

        setUrl(
            "jdbc:mysql://" + databaseConfiguration.host + ":" + databaseConfiguration.port + "/" +
            databaseService.databaseConfigurationAndApplicationModeToDatabaseName(
                databaseConfiguration, applicationMode
            ) + "?sslMode=DISABLED"
        );
        setUsername(databaseConfiguration.username);
        setPassword(databaseConfiguration.password);
        return super.getConnection();
    }
}
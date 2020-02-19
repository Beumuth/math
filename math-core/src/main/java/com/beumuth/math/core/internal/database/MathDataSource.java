package com.beumuth.math.core.internal.database;

import com.beumuth.math.client.internal.application.ApplicationMode;
import com.beumuth.math.client.internal.database.DatabaseConfiguration;
import com.beumuth.math.core.internal.application.ApplicationService;
import com.beumuth.math.core.internal.environment.EnvironmentService;
import com.mysql.cj.jdbc.Driver;
import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.SQLException;

@Component
public class MathDataSource extends BasicDataSource {
    @Autowired
    private ApplicationService applicationService;
    @Autowired
    private EnvironmentService environmentService;
    @Autowired
    private DatabaseService databaseService;

    public MathDataSource() {
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
            ) + "?allowPublicKeyRetrieval=true&useSSL=false"
        );
        setUsername(databaseConfiguration.username);
        setPassword(databaseConfiguration.password);
        return super.getConnection();
    }
}
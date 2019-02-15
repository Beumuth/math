package com.beumuth.collections.core.database;

import com.beumuth.collections.client.application.ApplicationMode;
import com.beumuth.collections.client.database.DatabaseConfiguration;
import com.beumuth.collections.core.application.ApplicationService;
import com.beumuth.collections.core.environment.EnvironmentService;
import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;

@Service
public class DatabaseService {

    @Autowired
    private ApplicationService applicationService;
    @Autowired
    private EnvironmentService environmentService;

    private JdbcTemplate jdbcTemplate;
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public JdbcTemplate getJdbcTemplate() {
        return new JdbcTemplate(createDataSource());
    }

    public NamedParameterJdbcTemplate getNamedParameterJdbcTemplate() {
        return new NamedParameterJdbcTemplate(createDataSource());
    }

    private DataSource createDataSource() {
        DatabaseConfiguration databaseConfiguration = environmentService.getActiveEnvironment().databaseConfiguration;
        ApplicationMode applicationMode = applicationService.getApplicationMode();

        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName("com.mysql.jdbc.Driver");
        dataSource.setUrl(
            "jdbc:mysql://" + databaseConfiguration.host + ":" + databaseConfiguration.port + "/" +
                databaseConfigurationAndApplicationModeToDatabaseName(applicationMode, databaseConfiguration)
        );
        dataSource.setUsername(databaseConfiguration.username);
        dataSource.setPassword(databaseConfiguration.password);

        return dataSource;
    }

    private String databaseConfigurationAndApplicationModeToDatabaseName(
        ApplicationMode mode,
        DatabaseConfiguration configuration
    ) {
        switch(mode) {
            case LIVE:
                return configuration.database;
            case TEST:
                return configuration.integrationTestDatabase;
        }
        throw new RuntimeException("Current ApplicationMode [" + mode + "] not handled");
    }
}
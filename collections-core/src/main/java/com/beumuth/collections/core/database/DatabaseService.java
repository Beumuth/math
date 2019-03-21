package com.beumuth.collections.core.database;

import com.beumuth.collections.client.application.ApplicationMode;
import com.beumuth.collections.client.database.DatabaseConfiguration;
import com.beumuth.collections.core.application.ApplicationService;
import com.beumuth.collections.core.environment.EnvironmentService;
import com.mysql.cj.jdbc.Driver;
import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

@Service
public class DatabaseService {
    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private EnvironmentService environmentService;

    @Autowired
    private CollectionsDataSource collectionsDataSource;

    private JdbcTemplate jdbcTemplate;
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @PostConstruct
    public void initialize() {
        jdbcTemplate = new JdbcTemplate(collectionsDataSource);
        namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(collectionsDataSource);
    }

    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    public NamedParameterJdbcTemplate getNamedParameterJdbcTemplate() {
        return namedParameterJdbcTemplate;
    }

    /**
     * Get a JdbcTemplate that isn't connected to a particular database
     * @return
     */
    public JdbcTemplate getSchemalessJdbcTemplate() {
        return new JdbcTemplate(getSchemalessDataSource());
    }

    /**
     * Get a JdbcTemplate that isn't connected to a particular database
     * @return
     */
    public NamedParameterJdbcTemplate getSchemalessNamedParameterJdbcTemplate() {
        return new NamedParameterJdbcTemplate(getSchemalessDataSource());
    }

    private DataSource getSchemalessDataSource() {
        DatabaseConfiguration databaseConfiguration = environmentService.getActiveEnvironment().databaseConfiguration;
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName(Driver.class.getCanonicalName());
        dataSource.setUrl("jdbc:mysql://" + databaseConfiguration.host + ":" + databaseConfiguration.port + "?sslMode=DISABLED");
        dataSource.setUsername(databaseConfiguration.username);
        dataSource.setPassword(databaseConfiguration.password);
        return dataSource;
    }

    public String databaseConfigurationAndApplicationModeToDatabaseName(
        DatabaseConfiguration configuration,
        ApplicationMode mode
    ) {
        switch(mode) {
            case LIVE:
                return configuration.database;
            case TEST:
                return configuration.integrationTestDatabase;
        }
        throw new RuntimeException("Current ApplicationMode [" + mode + "] not handled");
    }

    public String getCurrentDatabaseName() {
        return databaseConfigurationAndApplicationModeToDatabaseName(
            environmentService.getActiveEnvironment().databaseConfiguration,
            applicationService.getApplicationMode()
        );
    }
}
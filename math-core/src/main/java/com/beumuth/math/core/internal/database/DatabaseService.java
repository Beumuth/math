package com.beumuth.math.core.internal.database;

import com.beumuth.math.client.internal.database.DatabaseConfiguration;
import com.beumuth.math.core.internal.application.ApplicationService;
import com.beumuth.math.core.internal.environment.EnvironmentService;
import com.mysql.cj.jdbc.Driver;
import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.function.Supplier;

@Service
public class DatabaseService {

    private final Supplier<DatabaseConfiguration> databaseConfigurationSupplier;
    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public DatabaseService(
        @Autowired ApplicationService applicationService,
        @Autowired EnvironmentService environmentService
    ) {
        this.databaseConfigurationSupplier = () -> environmentService
            .getActiveEnvironment()
            .databaseConfigurations
            .get(applicationService.getApplicationMode());
        MultiDataSource dataSource = new MultiDataSource(databaseConfigurationSupplier);
        jdbcTemplate = new JdbcTemplate(dataSource);
        namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
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

    private DataSource getSchemalessDataSource() {
        DatabaseConfiguration databaseConfiguration = databaseConfigurationSupplier.get();
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName(Driver.class.getCanonicalName());
        dataSource.setUrl(
            "jdbc:mysql://" + databaseConfiguration.host + ":" + databaseConfiguration.port + "?sslMode=DISABLED"
        );
        dataSource.setUsername(databaseConfiguration.username);
        dataSource.setPassword(databaseConfiguration.password);
        return dataSource;
    }

    public void createDatabase() {
        getSchemalessJdbcTemplate()
            .update("CREATE DATABASE `" + databaseConfigurationSupplier.get().database + "`");
    }

    public void dropDatabase() {
        getSchemalessJdbcTemplate()
            .update("DROP DATABASE `" + databaseConfigurationSupplier.get().database + "`");
    }
}
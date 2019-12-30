package com.beumuth.math.core.internal.databaseversion;

import com.beumuth.math.client.internal.application.ApplicationMode;
import com.beumuth.math.client.internal.databaseversion.CreateDatabaseVersionRequest;
import com.beumuth.math.client.internal.databaseversion.DatabaseVersion;
import com.beumuth.math.core.internal.application.ApplicationService;
import com.beumuth.math.core.internal.database.CollectionsBeanPropertyRowMapper;
import com.beumuth.math.core.internal.database.DatabaseService;
import com.beumuth.math.core.internal.environment.EnvironmentService;
import com.beumuth.math.core.internal.validation.InvalidResult;
import com.beumuth.math.core.internal.validation.ValidResult;
import com.beumuth.math.core.internal.validation.ValidationResult;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Service
public class DatabaseVersionService {

    private static final BeanPropertyRowMapper<DatabaseVersion> ROW_MAPPER =
        CollectionsBeanPropertyRowMapper.newInstance(DatabaseVersion.class);

    public static final String DATABASE_VERSION_SCRIPT_PATH = "/db/versions";

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private EnvironmentService environmentService;

    @Autowired
    private DatabaseService databaseService;

    @Autowired
    private ApplicationContext applicationContext;

    public List<DatabaseVersion> getAllDatabaseVersions() {
        return databaseService
            .getJdbcTemplate()
            .query(
            "SELECT " +
                    "id, " +
                    "majorVersion, " +
                    "minorVersion, " +
                    "patchVersion, " +
                    "datetimeCreated, " +
                    "description " +
                "FROM DatabaseVersion " +
                "ORDER BY datetimeCreated ASC",
                ROW_MAPPER
            );
    }

    public boolean doesDatabaseVersionExist(long id) {
        try {
            return databaseService
                .getNamedParameterJdbcTemplate()
                .queryForObject(
                    "SELECT 1 FROM DatabaseVersion WHERE id=:id",
                    ImmutableMap.of("id", id),
                    Boolean.class
                );
        } catch(EmptyResultDataAccessException e) {
            return false;
        }
    }

    public boolean doesDatabaseVersionWithSemanticVersionExist(int majorVersion, int minorVersion, int patchVersion) {
        try {
            return databaseService
                .getNamedParameterJdbcTemplate()
                .queryForObject(
                "SELECT 1 " +
                    "FROM DatabaseVersion " +
                    "WHERE " +
                        "majorVersion=:majorVersion AND " +
                        "minorVersion=:minorVersion AND " +
                        "patchVersion=:patchVersion",
                    ImmutableMap.
                        <String, Object> builder()
                        .put("majorVersion", majorVersion)
                        .put("minorVersion", minorVersion)
                        .put("patchVersion", patchVersion)
                        .build(),
                    Boolean.class
                );
        } catch(EmptyResultDataAccessException e) {
            return false;
        }
    }

    public boolean doesScriptExistForGivenSemanticVersion(int majorVersion, int minorVersion, int patchVersion) {
        return this
            .getClass()
            .getResource(
            DATABASE_VERSION_SCRIPT_PATH + "/" + majorVersion + "." + minorVersion + "." + patchVersion + ".sql"
            ) != null;
    }

    public Optional<DatabaseVersion> getDatabaseVersion(long id) {
        try {
            return Optional.ofNullable(
                databaseService
                    .getNamedParameterJdbcTemplate()
                    .queryForObject(
                    "SELECT " +
                            "id, " +
                            "majorVersion, " +
                            "minorVersion, " +
                            "patchVersion, " +
                            "datetimeCreated, " +
                            "description " +
                        "FROM " +
                            "DatabaseVersion " +
                        "WHERE " +
                            "id=:id",
                        ImmutableMap.of("id", id),
                        ROW_MAPPER
                    )
            );
        } catch(IncorrectResultSizeDataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<DatabaseVersion> getDatabaseVersionBySemanticVersion(
        int majorVersion,
        int minorVersion,
        int patchVersion
    ) {
        try {
            return Optional.ofNullable(
                databaseService
                    .getNamedParameterJdbcTemplate()
                    .queryForObject(
                    "SELECT " +
                            "id, " +
                            "majorVersion, " +
                            "minorVersion, " +
                            "patchVersion, " +
                            "datetimeCreated, " +
                            "description " +
                        "FROM DatabaseVersion " +
                        "WHERE " +
                            "majorVersion=:majorVersion AND " +
                            "minorVersion=:minorVersion AND " +
                            "patchVersion=:patchVersion",
                        ImmutableMap
                            .<String, Object>builder()
                            .put("majorVersion", majorVersion)
                            .put("minorVersion", minorVersion)
                            .put("patchVersion", patchVersion)
                            .build(),
                        ROW_MAPPER
                    )
            );
        } catch(IncorrectResultSizeDataAccessException e) {
            return Optional.empty();
        }
    }

    /**
     * Get the DatabaseVersion that the database is currently at
     * @return
     */
    public DatabaseVersion getCurrentVersion() {
        return databaseService
            .getJdbcTemplate()
            .queryForObject(
                "SELECT " +
                        "DatabaseVersion.id, " +
                        "majorVersion, " +
                        "minorVersion, " +
                        "patchVersion, " +
                        "datetimeCreated, " +
                        "description " +
                    "FROM " +
                        "DatabaseVersion JOIN DatabaseMetadata " +
                            "ON DatabaseVersion.id = DatabaseMetadata.idCurrentVersion ",
                ROW_MAPPER
            );
    }

    public DatabaseVersion getMostRecentVersion() {
        return databaseService
            .getJdbcTemplate()
            .queryForObject(
            "SELECT " +
                    "id, " +
                    "majorVersion, " +
                    "minorVersion, " +
                    "patchVersion, " +
                    "datetimeCreated, " +
                    "description " +
                "FROM DatabaseVersion " +
                "ORDER BY " +
                    "majorVersion DESC, " +
                    "minorVersion DESC, " +
                    "patchVersion DESC " +
                "LIMIT 1",
                ROW_MAPPER
            );
    }

    public void initialize() {
        initialize(Lists.newArrayList());
    }

    public boolean isInitialized() {
        try {
            return databaseService
                .getSchemalessNamedParameterJdbcTemplate()
                .queryForObject(
                    "SELECT 1 FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME=:databaseName",
                    ImmutableMap.of(
                        "databaseName",
                        databaseService.getCurrentDatabaseName()
                    ),
                    Boolean.class
                );
        } catch(EmptyResultDataAccessException e) {
            return false;
        }
    }

    public void initialize(List<DatabaseVersion> versions) {
        JdbcTemplate template = databaseService.getSchemalessJdbcTemplate();

        //Create the database
        String databaseName = databaseService.getCurrentDatabaseName();

        //@Transactional doesn't work because the DataSource bean is pointing to the active database, which may not
        //exist. This causes an exception to be thrown. The try/catch is to do manual transactions
        try {
            template.update("CREATE DATABASE `" + databaseName + "`");

            //Run the 0.0.0.sql script
            applyVersion(0, 0, 0);

            //If there are more versions to apply (the 0th is applied in the 0.0.0 script)
            if(versions.size() > 1) {
                //Reinsert all the database versions (except the 0th version, which is the initial one)
                versions
                    .subList(1, versions.size())
                    .stream()
                    .map(
                        databaseVersion -> new CreateDatabaseVersionRequest(
                            databaseVersion.getMajorVersion(),
                            databaseVersion.getMinorVersion(),
                            databaseVersion.getPatchVersion(),
                            databaseVersion.getDescription()
                        )
                    ).forEach(request -> createNewDatabaseVersion(request));
            }
        } catch(Exception e) {
            //Manually rollback
            template.update("DROP DATABASE `" + databaseName + "`");

            //Rethrow exception
            throw e;
        }
    }

    /**
     *
     * @param request
     * @return The id of the newly created DatabaseVersion
     */
    public long createNewDatabaseVersion(CreateDatabaseVersionRequest request) {

        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

        databaseService
            .getNamedParameterJdbcTemplate()
            .update(
            "INSERT INTO DatabaseVersion (" +
                    "majorVersion, " +
                    "minorVersion, " +
                    "patchVersion, " +
                    "datetimeCreated, " +
                    "description " +
                ") VALUES ( " +
                    ":majorVersion, " +
                    ":minorVersion, " +
                    ":patchVersion, " +
                    "NOW(), " +
                    ":description" +
                ")",
                new MapSqlParameterSource(
                    ImmutableMap
                        .<String, Object>builder()
                        .put("majorVersion", request.majorVersion)
                        .put("minorVersion", request.minorVersion)
                        .put("patchVersion", request.patchVersion)
                        .put("description", request.description)
                        .build()
                ),
                keyHolder
            );
        return keyHolder.getKey().longValue();
    }

    public void deleteVersion(int majorVersion, int minorVersion, int patchVersion) {
        databaseService
            .getNamedParameterJdbcTemplate()
            .update(
            "DELETE FROM DatabaseVersion " +
                "WHERE " +
                    "majorVersion=:majorVersion AND " +
                    "minorVersion=:minorVersion AND " +
                    "patchVersion=:patchVersion",
                ImmutableMap.of(
                    "majorVersion", majorVersion,
                    "minorVersion", minorVersion,
                    "patchVersion", patchVersion
                )
            );
    }

    public ValidationResult testVersionScript(CreateDatabaseVersionRequest request) {
        //The convolutedness of this function indicates this design is flawed

        //Keep track of the current application mode
        ApplicationMode startingMode = applicationService.getApplicationMode();
        DatabaseVersion startingVersion = getCurrentVersion();

        //Create the database version temporarily so the attempt to upgrade to it can be made
        createNewDatabaseVersion(request);

        try {
            //Go into test mode
            applicationService.setApplicationMode(ApplicationMode.TEST);

            //Clean the database
            cleanDatabase();

            //Upgrade to the version to test
            upgradeToVersion(request.majorVersion, request.minorVersion, request.patchVersion);

            //It worked
            return new ValidResult();
        } catch(Exception e) {
            //Test failed
            return new InvalidResult(
        "SQL script for version [" + request.majorVersion + "." + request.minorVersion + "." +
                    request.patchVersion + "] threw " + e.getClass().getName() + " with message " + e.getMessage()
            );
        } finally {
            cleanDatabase();

            //Delete the version since it was only temporary
            deleteVersion(request.majorVersion, request.minorVersion, request.patchVersion);

            //Go back to the starting version (if it wasn't the initial version)
            if(! getCurrentVersion().equals(startingVersion)) {
                upgradeToVersion(
                    startingVersion.getMajorVersion(),
                    startingVersion.getMinorVersion(),
                    startingVersion.getPatchVersion()
                );
            }

            //Switch back to the starting mode
            applicationService.setApplicationMode(startingMode);
        }
    }

    @Transactional
    public void upgradeToMostRecentVersion() {
        //Get unapplied versions
        List<DatabaseVersion> unappliedVersions = getUnappliedDatabaseVersions();

        //Apply each of them
        for (DatabaseVersion databaseVersion : unappliedVersions) {
            applyVersion(
                databaseVersion.getMajorVersion(),
                databaseVersion.getMinorVersion(),
                databaseVersion.getPatchVersion()
            );
        }
    }

    /**
     * Upgrade to a particular database version. If the database version is the current version or a previous one,
     * then nothing happens.
     */
    @Transactional
    public void upgradeToVersion(int majorVersion, int minorVersion, int patchVersion) {

        //Get all the unapplied versions
        List<DatabaseVersion> unappliedVersions = getUnappliedDatabaseVersions();
        //Are there no unapplied versions?
        if(unappliedVersions.size() == 0) {
            //Nothing to do
            return;
        }

        //Get the version to upgrade to
        DatabaseVersion versionToUpgradeTo = getDatabaseVersionBySemanticVersion(
            majorVersion, minorVersion, patchVersion
        ).get();
        //Is the versionToUpgradeTo not a part of the unapplied versions?
        if(! unappliedVersions.contains(versionToUpgradeTo)) {
            //Nothing to do
            return;
        }

        //Upgrade versions until the target version is reached
        for (DatabaseVersion databaseVersion : unappliedVersions) {
            //Apply the version
            applyVersion(
                databaseVersion.getMajorVersion(),
                databaseVersion.getMinorVersion(),
                databaseVersion.getPatchVersion()
            );

            //Check if we reached the target version
            if (databaseVersion.equals(versionToUpgradeTo)) {
                //Finished
                break;
            }
        }
    }

    /**
     * @throws IllegalStateException if the application is in LIVE mode.
     */
    public void cleanDatabase() {
        //Don't let this happen in live mode. Too risky.
        if(applicationService.getApplicationMode().equals(ApplicationMode.LIVE)) {
            throw new IllegalStateException(
                "DatabaseVersionService cleanDatabase cannot be called when the application is in LIVE mode. " +
                    "This is to prevent accidentally wiping the database. The live database must be manually cleaned " +
                    "if that's the intent."
            );
        }

        //Save all the DatabaseVersions to re-insert
        List<DatabaseVersion> databaseVersions = getAllDatabaseVersions();

        //Get integrationTestDatabaseName
        String integrationTestDatabaseName = environmentService
            .getActiveEnvironment()
            .databaseConfiguration
            .integrationTestDatabase;

        databaseService.getJdbcTemplate().batchUpdate(
        "DROP DATABASE `" + integrationTestDatabaseName + "`"
        );

        //Initialize the database with the same DatabaseVersions
        initialize(databaseVersions);
    }

    /**
     * Determines whether a DatabaseVersion has be upgraded to.
     * @param databaseVersion
     * @return
     */
    public boolean isDatabaseVersionApplied(DatabaseVersion databaseVersion) {
        DatabaseVersion currentVersion = getCurrentVersion();
        return
            databaseVersion.getMajorVersion() < currentVersion.getMajorVersion() || (
                databaseVersion.getMajorVersion() == currentVersion.getMajorVersion() &&
                databaseVersion.getMinorVersion() < currentVersion.getMinorVersion()
            ) || (
                databaseVersion.getMajorVersion() == currentVersion.getMajorVersion() &&
                databaseVersion.getMinorVersion() == currentVersion.getMinorVersion() &&
                databaseVersion.getPatchVersion() < currentVersion.getPatchVersion()
            ) ||
            databaseVersion.getId() == currentVersion.getId();
    }

    private List<DatabaseVersion> getUnappliedDatabaseVersions() {
        DatabaseVersion currentVersion = getCurrentVersion();
        return databaseService
            .getNamedParameterJdbcTemplate()
            .query(
            "SELECT " +
                    "id, " +
                    "majorVersion, " +
                    "minorVersion, " +
                    "patchVersion, " +
                    "datetimeCreated, " +
                    "description " +
                "FROM DatabaseVersion " +
                "WHERE " +
                    "majorVersion > :currentMajorVersion OR "  +
                    "( " +
                        "majorVersion = :currentMajorVersion AND " +
                        "minorVersion > :currentMinorVersion " +
                    ") OR " +
                    "( " +
                        "majorVersion = :currentMajorVersion AND " +
                        "minorVersion = :currentMinorVersion AND " +
                        "patchVersion > :currentPatchVersion " +
                    ")" +
                "ORDER BY " +
                    "majorVersion ASC, " +
                    "minorVersion ASC, " +
                    "patchVersion ASC ",
                ImmutableMap
                    .<String, Object>builder()
                    .put("currentMajorVersion", currentVersion.getMajorVersion())
                    .put("currentMinorVersion", currentVersion.getMinorVersion())
                    .put("currentPatchVersion", currentVersion.getPatchVersion())
                    .build(),
                ROW_MAPPER
            );
    }

    private void applyVersion(int majorVersion, int minorVersion, int patchVersion) {
        try {
            //Explicitly use the current database. This is to prevent a SQL exception being thrown saying that no
            //database is selected, which mysteriously happens in the midst of creating and dropping.
            databaseService
                .getJdbcTemplate()
                .update("USE `" + databaseService.getCurrentDatabaseName() + "`");

            //Run version script
            Connection connection = databaseService.getJdbcTemplate().getDataSource().getConnection();
            ScriptUtils.executeSqlScript(
                connection,
                applicationContext.getResource("classpath:db/versions/" + majorVersion + "." + minorVersion + "." +
                    patchVersion + ".sql")
            );
            connection.close();

            //Update DatabaseMetadata.currentVersion
            databaseService
                .getNamedParameterJdbcTemplate()
                .update(
                "UPDATE DatabaseMetadata " +
                    "SET idCurrentVersion = ( " +
                        "SELECT id " +
                        "FROM DatabaseVersion " +
                        "WHERE " +
                            "majorVersion=:majorVersion AND " +
                            "minorVersion=:minorVersion AND " +
                            "patchVersion=:patchVersion " +
                    ")",
                    ImmutableMap
                        .<String, Object>builder()
                        .put("majorVersion", majorVersion)
                        .put("minorVersion", minorVersion)
                        .put("patchVersion", patchVersion)
                        .build()
                    );
        }
        catch(SQLException e) {
            throw new RuntimeException(
                "Can't apply version [" + majorVersion + "." + minorVersion + "." + patchVersion + "]",
                e
            );
        }
    }
}
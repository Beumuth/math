package com.beumuth.collections.core.databaseversion;

import com.beumuth.collections.client.application.ApplicationMode;
import com.beumuth.collections.client.databaseversion.CreateDatabaseVersionRequest;
import com.beumuth.collections.client.databaseversion.DatabaseVersion;
import com.beumuth.collections.core.application.ApplicationService;
import com.beumuth.collections.core.validation.InvalidResult;
import com.beumuth.collections.core.validation.ValidationResult;
import com.github.instantpudd.validator.ClientErrorException;
import com.github.instantpudd.validator.ClientErrorStatusCode;
import com.github.instantpudd.validator.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/databaseVersions")
public class DatabaseVersionController {

    @Autowired
    private DatabaseVersionService databaseVersionService;

    @Autowired
    private ApplicationService applicationService;

    @GetMapping
    public List<DatabaseVersion> getAllDatabaseVersions() {
        return databaseVersionService.getAllDatabaseVersions();
    }

    @GetMapping(path="/databaseVersion/{id}/exists")
    public Boolean doesDatabaseVersionExist(@PathVariable(name="id") long id) {
        return databaseVersionService.doesDatabaseVersionExist(id);
    }

    @GetMapping(path="/databaseVersion/bySemanticVersion/exists")
    public Boolean doesDatabaseVersionWithSemanticVersionExist(
        @RequestParam(name="majorVersion") int majorVersion,
        @RequestParam(name="minorVersion") int minorVersion,
        @RequestParam(name="patchVersion") int patchVersion
    ) {
        return databaseVersionService.doesDatabaseVersionWithSemanticVersionExist(
            majorVersion,
            minorVersion,
            patchVersion
        );
    }

    @GetMapping(path="/databaseVersion/{id}")
    public DatabaseVersion getDatabaseVersion(@PathVariable(name="id") long id) throws ClientErrorException {
        Optional<DatabaseVersion> databaseVersion = databaseVersionService.getDatabaseVersion(id);
        Validator
            .returnStatus(ClientErrorStatusCode.NOT_FOUND)
            .ifEmpty(databaseVersion)
            .withErrorMessage("DatabaseVersion with given id [" + id + "] not found")
            .execute();
        return databaseVersion.get();
    }

    @GetMapping(path="/databaseVersion/bySemanticVersion")
    public DatabaseVersion getDatabaseVersionBySemanticVersion(
        @RequestParam(name="majorVersion") int majorVersion,
        @RequestParam(name="minorVersion") int minorVersion,
        @RequestParam(name="patchVersion") int patchVersion
    ) throws ClientErrorException {
        Optional<DatabaseVersion> databaseVersion = databaseVersionService.getDatabaseVersionBySemanticVersion(
            majorVersion, minorVersion, patchVersion
        );
        Validator
            .returnStatus(ClientErrorStatusCode.NOT_FOUND)
            .ifEmpty(databaseVersion)
            .withErrorMessage(
                "DatabaseVersion with given semantic version [" + majorVersion + "." + minorVersion + "." +
                    patchVersion + "] does not exist"
            ).execute();
        return databaseVersion.get();
    }

    @GetMapping(path="/databaseVersion/current")
    public DatabaseVersion getCurrentDatabaseVersion() {
        return databaseVersionService.getCurrentVersion();
    }

    @GetMapping(path="/databaseVersion/mostRecent")
    public DatabaseVersion getMostRecentVersion() {
        return databaseVersionService.getMostRecentVersion();
    }

    @PutMapping(path="/initialize")
    public void initialize() throws ClientErrorException {
        Validator
            .returnStatus(ClientErrorStatusCode.BAD_REQUEST)
            .ifTrue(databaseVersionService.isInitialized())
            .withErrorMessage("Database is already initialized. Use PUT /databaseVersions/clean instead.")
            .execute();
        databaseVersionService.initialize();
    }

    @RequestMapping(method=RequestMethod.POST, path="/databaseVersion")
    @ResponseStatus(HttpStatus.CREATED)
    public void createDatabaseVersion(@RequestBody CreateDatabaseVersionRequest request) throws ClientErrorException {
        //Ensure that the semantic version is unique
        Validator
            .returnStatus(ClientErrorStatusCode.CONFLICT)
            .ifTrue(
                databaseVersionService.doesDatabaseVersionWithSemanticVersionExist(
                    request.majorVersion,
                    request.minorVersion,
                    request.patchVersion
                )
            ).withErrorMessage(
                "DatabaseVersion with given semantic version [" + request.majorVersion + "." + request.minorVersion +
                    "." + request.patchVersion + "] already exists"
            ).execute();

        //Ensure that a sql script exists in the resources with the given semantic version
        Validator
            .returnStatus(ClientErrorStatusCode.BAD_REQUEST)
            .ifFalse(
                databaseVersionService.doesScriptExistForGivenSemanticVersion(
                    request.majorVersion,
                    request.minorVersion,
                    request.patchVersion
                )
            ).withErrorMessage(
            "The required sql script [" + DatabaseVersionService.DATABASE_VERSION_SCRIPT_PATH + "/" +
                request.majorVersion + "." + request.minorVersion + "." + request.patchVersion + ".sql] " +
                "does not exist"
            ).execute();

        //Test the sql script
        ValidationResult scriptTestValidationResult = databaseVersionService.testVersionScript(request);
        Validator
            .returnStatus(ClientErrorStatusCode.STATUS_400)
            .ifInstanceOf(scriptTestValidationResult, InvalidResult.class)
            .withErrorMessage(
                scriptTestValidationResult instanceof InvalidResult ?
                    ((InvalidResult)scriptTestValidationResult).getReason() :
                    ""
            ).execute();

        databaseVersionService.createNewDatabaseVersion(request);
    }

    @RequestMapping(method=RequestMethod.PUT, path="/upgrade/mostRecent")
    public void upgradeToMostRecentVersion() {
        databaseVersionService.upgradeToMostRecentVersion();
    }

    @RequestMapping(method=RequestMethod.PUT, path="/upgrade/version")
    public void upgradeToVersion(
        @RequestParam(name="majorVersion") int majorVersion,
        @RequestParam(name="minorVersion") int minorVersion,
        @RequestParam(name="patchVersion") int patchVersion
    ) throws ClientErrorException {
        //Get the DatabaseVersion
        Optional<DatabaseVersion> databaseVersion = databaseVersionService.getDatabaseVersionBySemanticVersion(
            majorVersion,
            minorVersion,
            patchVersion
        );
        //Ensure that the version exists
        Validator
            .returnStatus(ClientErrorStatusCode.NOT_FOUND)
            .ifEmpty(databaseVersion)
            .withErrorMessage(
                "DatabaseVersion with given semantic version [" + majorVersion + "." + minorVersion + "." +
                    patchVersion + "] does not exist"
            ).execute();

        //Check if the version is already applied
        Validator
            .returnStatus(ClientErrorStatusCode.BAD_REQUEST)
            .ifTrue(databaseVersionService.isDatabaseVersionApplied(databaseVersion.get()))
            .withErrorMessage(
            "DatabaseVersion with given semanticVersion [" + majorVersion + "." + minorVersion + "." + patchVersion +
                "] is already applied"
            ).execute();

        databaseVersionService.upgradeToVersion(majorVersion, minorVersion, patchVersion);
    }

    @RequestMapping(method=RequestMethod.PUT, path="/clean")
    public void cleanDatabase() throws ClientErrorException {
        //Validate that in TEST mode
        Validator
            .returnStatus(ClientErrorStatusCode.BAD_REQUEST)
            .ifFalse(applicationService.getApplicationMode().equals(ApplicationMode.TEST))
            .withErrorMessage("The database can only be cleaned through REST if the application is in test mode.")
            .execute();
        //Validate that the database is already initialized
        Validator
            .returnStatus(ClientErrorStatusCode.BAD_REQUEST)
            .ifFalse(databaseVersionService.isInitialized())
            .withErrorMessage("The database is not yet initialized. Use /initialize instead.")
            .execute();
        databaseVersionService.cleanDatabase();
    }
}

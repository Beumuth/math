package com.beumuth.math.core.internal.version.versiontransgrade;

import com.beumuth.math.client.internal.version.ontologyversion.SemanticVersion;
import com.beumuth.math.client.internal.version.ontologyversion.SemanticVersionFormatException;
import com.beumuth.math.core.internal.version.ontologyversion.OntologyVersionService;
import com.beumuth.math.core.internal.version.ontologyversion.OntologyVersionValidator;
import com.github.instantpudd.validator.ClientErrorException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import static com.github.instantpudd.validator.ClientErrorStatusCode.BAD_REQUEST;

@RestController
@RequestMapping("/api/versionTransgrades")
public class VersionTransgradeController {

    private VersionTransgradeService versionTransgradeService;
    private OntologyVersionService ontologyVersionService;
    private VersionTransgradeValidator versionTransgradeValidator;
    private OntologyVersionValidator ontologyVersionValidator;

    public VersionTransgradeController(
        @Autowired VersionTransgradeService versionTransgradeService,
        @Autowired OntologyVersionService ontologyVersionService,
        @Autowired VersionTransgradeValidator versionTransgradeValidator,
        @Autowired OntologyVersionValidator ontologyVersionValidator
    ) {
        this.versionTransgradeService = versionTransgradeService;
        this.ontologyVersionService = ontologyVersionService;
        this.versionTransgradeValidator = versionTransgradeValidator;
        this.ontologyVersionValidator = ontologyVersionValidator;
    }

    @PutMapping(path="/initialize")
    public void initialize() {
        versionTransgradeService.initialize();
    }

    @GetMapping(path="/versionTransgrade/from/{semanticVersionFrom}/to/{semanticVersionTo}/exists")
    @ResponseBody
    public boolean doesVersionTransgradeExistWithFromAndTo(
        @PathVariable("semanticVersionFrom") String semanticVersionFrom,
        @PathVariable("semanticVersionTo") String semanticVersionTo
    ) throws ClientErrorException {
        try {
            SemanticVersion from = SemanticVersion.fromString(semanticVersionFrom);
            SemanticVersion to = SemanticVersion.fromString(semanticVersionTo);
            ontologyVersionValidator.validateOntologyVersionExists(from);
            ontologyVersionValidator.validateOntologyVersionExists(to);
            return versionTransgradeService.doesVersionTransgradeExistWithFromAndTo(from, to);
        } catch(SemanticVersionFormatException e) {
            throw new ClientErrorException(BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping(path="/from/{semanticVersionFrom}/to/{semanticVersionTo}/isPossible")
    @ResponseBody
    public boolean isTransgradePossible(
        @PathVariable("semanticVersionFrom") String semanticVersionFrom,
        @PathVariable("semanticVersionTo") String semanticVersionTo
    ) throws ClientErrorException {
        try {
            SemanticVersion from = SemanticVersion.fromString(semanticVersionFrom);
            SemanticVersion to = SemanticVersion.fromString(semanticVersionTo);
            ontologyVersionValidator.validateOntologyVersionExists(from);
            ontologyVersionValidator.validateOntologyVersionExists(to);
            return versionTransgradeService.isTransgradePossible(from, to);
        } catch(SemanticVersionFormatException e) {
            throw new ClientErrorException(BAD_REQUEST, e.getMessage());
        }
    }

    @PutMapping(path="/versionTransgrade/to/mostRecentVersion")
    public void upgradeToMostRecentVersion() {
        try {
            versionTransgradeService.upgradeToMostRecentVersion();
        } catch(TransgradeNotPossibleException e) {
            throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, e.getMessage());
        }
    }

    @PutMapping(path="/transgrade/to/{semanticVersion}")
    public void transgrade(@PathVariable(name="semanticVersion") String semanticVersion) throws ClientErrorException {
        try {
            SemanticVersion to = SemanticVersion.fromString(semanticVersion);
            ontologyVersionValidator.validateOntologyVersionExists(to);

            //Is to the current version?
            if(
                ontologyVersionService
                    .getCurrentOntologyVersion()
                    .getSemanticVersion()
                    .equals(to)
            ) {
                //Nothing to do
                return;
            }

            //Validate that the transgrade is possible
            versionTransgradeValidator.validateVersionTransgradePossible(
                ontologyVersionService.getCurrentOntologyVersion().getSemanticVersion(),
                to
            );

            //Do the transgrade
            versionTransgradeService.transgrade(to);
        } catch(SemanticVersionFormatException e) {
            throw new ClientErrorException(BAD_REQUEST, e.getMessage());
        } catch(TransgradeNotPossibleException e) {
            throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, e.getMessage());
        }
    }
}

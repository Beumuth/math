package com.beumuth.math.core.internal.version.ontologyversion;

import com.beumuth.math.client.internal.version.ontologyversion.OntologyVersion;
import com.beumuth.math.client.internal.version.ontologyversion.OntologyVersions;
import com.beumuth.math.client.internal.version.ontologyversion.SemanticVersion;
import com.beumuth.math.client.internal.version.ontologyversion.SemanticVersionFormatException;
import com.beumuth.math.client.settheory.orderedset.OrderedSet;
import com.github.instantpudd.validator.ClientErrorException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import static com.github.instantpudd.validator.ClientErrorStatusCode.BAD_REQUEST;

@RestController
@RequestMapping("/api/ontologyVersions")
public class OntologyVersionController {

    private OntologyVersionService ontologyVersionService;
    private OntologyVersionValidator ontologyVersionValidator;

    public OntologyVersionController(
        @Autowired OntologyVersionService ontologyVersionService,
        @Autowired OntologyVersionValidator ontologyVersionValidator
    ) {
        this.ontologyVersionService = ontologyVersionService;
        this.ontologyVersionValidator = ontologyVersionValidator;
    }

    @GetMapping(path="/ontologyVersion/{semanticVersion}/exists")
    @ResponseBody
    public Boolean doesOntologyVersionExist(
        @PathVariable(name="semanticVersion") String semanticVersion
    ) throws ClientErrorException {
        try {
            return ontologyVersionService.doesOntologyVersionExist(
                SemanticVersion.fromString(semanticVersion)
            );
        } catch(SemanticVersionFormatException e) {
            throw new ClientErrorException(BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping(path="/ontologyVersion/{semanticVersion}")
    @ResponseBody
    public OntologyVersion getOntologyVersion(
        @PathVariable(name="semanticVersion") String semanticVersionString
    ) throws ClientErrorException {
        try {
            SemanticVersion semanticVersion = SemanticVersion.fromString(semanticVersionString);
            ontologyVersionValidator.validateOntologyVersionExists(semanticVersion);
            return ontologyVersionService.getOntologyVersion(semanticVersion);
        } catch(SemanticVersionFormatException e) {
            throw new ClientErrorException(BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping
    public OrderedSet<OntologyVersion> getAllOntologyVersions() {
        return OntologyVersions.ALL;
    }

    @GetMapping(path="/ontologyVersion/current")
    public OntologyVersion getCurrentOntologyVersion() {
        return ontologyVersionService.getCurrentOntologyVersion();
    }

    @GetMapping(path="/ontologyVersion/mostRecent")
    public OntologyVersion getMostRecentVersion() {
        return ontologyVersionService.getMostRecentOntologyVersion();
    }
}

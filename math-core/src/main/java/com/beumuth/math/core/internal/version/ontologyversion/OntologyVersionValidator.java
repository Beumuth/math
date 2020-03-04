package com.beumuth.math.core.internal.version.ontologyversion;

import com.beumuth.math.client.internal.version.ontologyversion.SemanticVersion;
import com.github.instantpudd.validator.ClientErrorException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.github.instantpudd.validator.ClientErrorStatusCode.NOT_FOUND;
import static com.github.instantpudd.validator.Validator.returnStatus;

@Component
public class OntologyVersionValidator {
    private OntologyVersionService ontologyVersionService;

    public OntologyVersionValidator(
        @Autowired OntologyVersionService ontologyVersionService
    ) {
        this.ontologyVersionService = ontologyVersionService;
    }
    public void validateOntologyVersionExists(SemanticVersion version) throws ClientErrorException {
        returnStatus(NOT_FOUND)
            .ifFalse(ontologyVersionService.doesOntologyVersionExist(version))
            .withErrorMessage("OntologyVersion with given SemanticVersion [" + version + "] does not exist")
            .execute();
    }
}

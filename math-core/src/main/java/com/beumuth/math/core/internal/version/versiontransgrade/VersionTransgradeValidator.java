package com.beumuth.math.core.internal.version.versiontransgrade;

import com.beumuth.math.client.internal.version.ontologyversion.SemanticVersion;
import com.beumuth.math.core.internal.version.ontologyversion.OntologyVersionValidator;
import com.github.instantpudd.validator.ClientErrorException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.github.instantpudd.validator.ClientErrorStatusCode.BAD_REQUEST;
import static com.github.instantpudd.validator.ClientErrorStatusCode.NOT_FOUND;
import static com.github.instantpudd.validator.Validator.returnStatus;

@Component
public class VersionTransgradeValidator {
    private VersionTransgradeService versionTransgradeService;
    private OntologyVersionValidator ontologyVersionValidator;

    public VersionTransgradeValidator(
        @Autowired VersionTransgradeService versionTransgradeService,
        @Autowired OntologyVersionValidator ontologyVersionValidator
    ) {
        this.versionTransgradeService = versionTransgradeService;
        this.ontologyVersionValidator = ontologyVersionValidator;
    }

    public void validateVersionTransgradeWithFromAndToExists(
        SemanticVersion from,
        SemanticVersion to
    ) throws ClientErrorException {
        ontologyVersionValidator.validateOntologyVersionExists(from);
        ontologyVersionValidator.validateOntologyVersionExists(to);
        returnStatus(NOT_FOUND)
            .ifFalse(versionTransgradeService.doesVersionTransgradeExistWithFromAndTo(from, to))
            .withErrorMessage("VersionTransgrade with given from [" + from + "] and to [" + to + "] does not exist")
            .execute();
    }

    public void validateVersionTransgradePossible(
        SemanticVersion from,
        SemanticVersion to
    ) throws ClientErrorException {
        returnStatus(BAD_REQUEST)
            .ifFalse(versionTransgradeService.isTransgradePossible(from, to))
            .withErrorMessage("Transgrade from version [" + from + "] to version [" + to + "] is not possible")
            .execute();
    }
}

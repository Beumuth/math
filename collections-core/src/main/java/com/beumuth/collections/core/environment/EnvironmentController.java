package com.beumuth.collections.core.environment;

import com.beumuth.collections.client.environment.EnvironmentConfiguration;
import com.beumuth.collections.core.validation.InvalidResult;
import com.beumuth.collections.core.validation.ValidationResult;
import com.github.instantpudd.validator.ClientErrorException;
import com.github.instantpudd.validator.ClientErrorStatusCode;
import com.github.instantpudd.validator.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping(path="/api/environments")
public class EnvironmentController {

    @Autowired
    private EnvironmentService environmentService;

    @Autowired
    private EnvironmentConfigurationValidator environmentConfigurationValidator;

    @GetMapping(path="")
    public Set<EnvironmentConfiguration> getEnvironments() {
        return environmentService.getEnvironments();
    }

    @GetMapping(path="/active")
    public EnvironmentConfiguration getActiveEnvironment() {
        return environmentService.getActiveEnvironment();
    }

    @PutMapping(path="/active")
    public void setActiveEnvironment(@RequestBody String environmentName) throws ClientErrorException {
        Validator
            .returnStatus(ClientErrorStatusCode.BAD_REQUEST)
            .ifFalse(environmentService.doesEnvironmentExist(environmentName))
            .withErrorMessage(
                "Given environmentName [" + environmentName+ "] is not recognized in the application configuration."
            ).execute();
        environmentService.setActiveEnvironment(environmentName);
    }

    @PostMapping(path="/environment")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addEnvironment(@RequestBody EnvironmentConfiguration environment) throws ClientErrorException {
        ValidationResult validationResult = environmentConfigurationValidator.validate(environment);
        if(validationResult instanceof InvalidResult) {
            throw new ClientErrorException(
                ClientErrorStatusCode.BAD_REQUEST,
                ((InvalidResult) validationResult).getReason()
            );
        }
        Validator
            .returnStatus(ClientErrorStatusCode.CONFLICT)
            .ifTrue(environmentService.doesEnvironmentExist(environment.name))
            .withErrorMessage("Environment already exists with name [" + environment.name + "]")
            .execute();

        environmentService.addEnvironment(environment);
    }

    @DeleteMapping(path="/environment/{name}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEnvironment(@PathVariable String name) throws ClientErrorException {
        Validator
            .returnStatus(ClientErrorStatusCode.BAD_REQUEST)
            .ifTrue(environmentService.getActiveEnvironment().name.equalsIgnoreCase(name))
            .withErrorMessage(
                "Environment [" + name + "] is currently the active environment. Switch the active environment to " +
                    "another to delete."
            ).execute();
        environmentService.deleteEnvironment(name);
    }

    @GetMapping(path="/environment/{name}")
    public EnvironmentConfiguration getEnvironment(@PathVariable String name) throws ClientErrorException {
        Optional<EnvironmentConfiguration> environment = environmentService.getEnvironment(name);
        if(environment.isEmpty()) {
            throw new ClientErrorException(
                ClientErrorStatusCode.NOT_FOUND,
                "Environment with name [" + name + "] does not exist"
            );
        }
        return environment.get();
    }

    @PutMapping(path="/environment/{name}")
    @ResponseStatus(value= HttpStatus.NO_CONTENT)
    public void updateEnvironment(
        @PathVariable String name,
        @RequestBody EnvironmentConfiguration newEnvironment
    ) throws ClientErrorException {
        Validator
            .returnStatus(ClientErrorStatusCode.NOT_FOUND)
            .ifFalse(environmentService.doesEnvironmentExist(name))
            .withErrorMessage("Environment with given name [" + name + "] not recognized")
            .execute();

        ValidationResult validationResult = environmentConfigurationValidator.validate(newEnvironment);
        Validator
            .returnStatus(ClientErrorStatusCode.BAD_REQUEST)
            .ifInstanceOf(validationResult, InvalidResult.class)
            .withErrorMessage(
                validationResult instanceof InvalidResult ?
                    "Given environment not valid. Reason: " + ((InvalidResult) validationResult).getReason() :
                    ""
            ).execute();

        environmentService.updateEnvironment(name, newEnvironment);
    }
}

package com.beumuth.collections.core.application;

import com.beumuth.collections.client.application.ApplicationConfiguration;
import com.beumuth.collections.client.application.ApplicationMode;
import com.beumuth.collections.client.environment.EnvironmentConfiguration;
import com.beumuth.collections.core.validation.InvalidResult;
import com.beumuth.collections.core.validation.ValidationResult;
import com.github.instantpudd.validator.ClientErrorException;
import com.github.instantpudd.validator.ClientErrorStatusCode;
import com.github.instantpudd.validator.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(
    path="/api/application",
    produces = {MediaType.APPLICATION_JSON_VALUE},
    consumes = {MediaType.APPLICATION_JSON_VALUE}
)
public class ApplicationController {
    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private ApplicationConfigurationValidator applicationConfigurationValidator;

    @GetMapping(path="/configuration")
    public ApplicationConfiguration getApplicationConfiguration() {
        return applicationService.getApplicationConfiguration();
    }

    @PutMapping(path="/configuration")
    public void setApplicationConfiguration(@RequestBody ApplicationConfiguration configuration) throws ClientErrorException {
        ValidationResult result = applicationConfigurationValidator.validate(configuration);
        Validator.returnStatus(ClientErrorStatusCode.BAD_REQUEST)
            .ifInstanceOf(result, InvalidResult.class)
            .withErrorMessage(result instanceof InvalidResult ? "configuration invalid: " + ((InvalidResult)result).getReason() : "")
            .execute();
        applicationService.setConfiguration(configuration);
    }

    @GetMapping(path="/mode")
    public ApplicationMode getApplicationMode() {
        return applicationService.getApplicationMode();
    }

    @PutMapping(path="/mode")
    public void setApplicationMode(@RequestBody String applicationMode) throws ClientErrorException {
        Validator
            .returnStatus(ClientErrorStatusCode.BAD_REQUEST)
            .ifFalse(ApplicationMode.doesApplicationModeExistByName(applicationMode))
            .withErrorMessage(
                "Given applicationMode [" + applicationMode + "] is not a valid ApplicationMode. Must be either " +
                    "'LIVE' or 'TEST'."
            ).execute();
        applicationService.setApplicationMode(ApplicationMode.valueOf(applicationMode));
    }
}

package com.beumuth.math.core.internal.application;

import com.beumuth.math.client.internal.application.ApplicationConfiguration;
import com.beumuth.math.client.internal.environment.EnvironmentConfiguration;
import com.beumuth.math.core.internal.environment.EnvironmentConfigurationValidator;
import com.beumuth.math.core.internal.validation.InvalidResult;
import com.beumuth.math.core.internal.validation.ValidResult;
import com.beumuth.math.core.internal.validation.ValidationResult;
import com.beumuth.math.core.internal.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ApplicationConfigurationValidator implements Validator<ApplicationConfiguration> {
    @Autowired
    private EnvironmentConfigurationValidator environmentConfigurationValidator;

    @Override
    public ValidationResult validate(ApplicationConfiguration instance) {
        if(instance == null) {
            return new InvalidResult("applicationConfiguration cannot be null");
        }
        if(instance.environments == null) {
            return new InvalidResult("environments must be set");
        }
        if(instance.environments.isEmpty()) {
            return new InvalidResult("environments cannot be empty");
        }
        //Validate each environment
        for(EnvironmentConfiguration environment : instance.environments) {
            ValidationResult result = environmentConfigurationValidator.validate(environment);
            if(result instanceof InvalidResult) {
                return new InvalidResult(
                    "There was a problem with the environment with name [" +  environment.name + "]. " +
                        ((InvalidResult) result).getReason()
                );
            }
        }
        if(instance.activeEnvironment == null || instance.activeEnvironment.isEmpty()) {
            return new InvalidResult("activeEnvironment must be set");
        }
        if(! instance.containsEnvironmentByName(instance.activeEnvironment)) {
            return new InvalidResult(
                "activeEnvironment [" + instance.activeEnvironment + "] not present in the environment list"
            );
        }
        if(instance.mode == null) {
            return new InvalidResult("mode must be either 'LIVE' or 'TEST'");
        }

        return new ValidResult();
    }
}
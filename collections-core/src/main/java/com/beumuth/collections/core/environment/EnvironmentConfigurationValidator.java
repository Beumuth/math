package com.beumuth.collections.core.environment;

import com.beumuth.collections.client.environment.EnvironmentConfiguration;
import com.beumuth.collections.core.database.DatabaseConfigurationValidator;
import com.beumuth.collections.core.validation.InvalidResult;
import com.beumuth.collections.core.validation.ValidationResult;
import com.beumuth.collections.core.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EnvironmentConfigurationValidator implements Validator<EnvironmentConfiguration> {

    @Autowired
    private DatabaseConfigurationValidator databaseConfigurationValidator;

    @Override
    public ValidationResult validate(EnvironmentConfiguration instance) {
        if(instance.name == null || instance.name.isEmpty()) {
            return new InvalidResult("name cannot be null or empty");
        }
        if(!instance.name.matches("[\\w\\-]+")) {
            return new InvalidResult(
                "environment name [" + instance.name + "] can only contain the following characters: a-z, " +
                    "A-Z, 0-9, -, _"
            );
        }

        if(instance.baseUrl == null || instance.baseUrl.isEmpty()) {
            return new InvalidResult(
                "baseUrl cannot be null or empty"
            );
        }

        if(instance.databaseConfiguration == null ) {
            return new InvalidResult(
                "databaseConfiguration for environment [" + instance.name + "] must be set"
            );
        }

        ValidationResult result = databaseConfigurationValidator.validate(instance.databaseConfiguration);
        if(result instanceof InvalidResult) {
            return new InvalidResult(
                "There is an error with the databaseConfiguration for environment with name [" + instance.name +
                    "]: " + ((InvalidResult) result).getReason()
            );
        }
        return result;
    }
}

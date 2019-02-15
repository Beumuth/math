package com.beumuth.collections.core.database;

import com.beumuth.collections.client.database.DatabaseConfiguration;
import com.beumuth.collections.core.validation.InvalidResult;
import com.beumuth.collections.core.validation.ValidResult;
import com.beumuth.collections.core.validation.ValidationResult;
import com.beumuth.collections.core.validation.Validator;
import org.springframework.stereotype.Component;

@Component
public class DatabaseConfigurationValidator implements Validator<DatabaseConfiguration> {
    @Override
    public ValidationResult validate(DatabaseConfiguration instance) {
        if(instance.host == null || instance.host.isEmpty()) {
            return new InvalidResult("host must be set");
        }
        if(instance.port == null) {
            return new InvalidResult("port must be set");
        }
        if(instance.username == null || instance.username.isEmpty()){
            return new InvalidResult("username must be set");
        }
        if(instance.password == null) {
            return new InvalidResult("password must be set");
        }
        if(instance.database == null || instance.database.isEmpty()) {
            return new InvalidResult("database must be set");
        }
        if(instance.integrationTestDatabase == null || instance.integrationTestDatabase.isEmpty()) {
            return new InvalidResult("integrationTestDatabase must be set");
        }

        return new ValidResult();
    }
}

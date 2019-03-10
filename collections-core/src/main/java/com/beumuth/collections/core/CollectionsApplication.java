package com.beumuth.collections.core;

import com.beumuth.collections.core.application.ApplicationConfigurationValidator;
import com.beumuth.collections.core.application.ApplicationService;
import com.beumuth.collections.core.application.InitializationException;
import com.beumuth.collections.core.validation.InvalidResult;
import com.beumuth.collections.core.validation.ValidationResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.test.context.ContextConfiguration;

@SpringBootApplication(
    exclude = {DataSourceAutoConfiguration.class}
)
public class CollectionsApplication {

    @Autowired
    private ApplicationConfigurationValidator applicationConfigurationValidator;

    @Value("${configurationLocation}")
    private String configurationLocation;

    @Autowired
    private ApplicationService applicationService;

    public static void main(String[] args) {
        SpringApplication.run(CollectionsApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void validateStartup() {
        //Validate the application configuration
        if(! applicationService.doesConfigurationExist()) {
            throw new InitializationException(
                "Given configurationLocation [" + configurationLocation + "] does not exist as a file"
            );
        }
        ValidationResult validationResult = applicationConfigurationValidator.validate(
            applicationService.getApplicationConfiguration()
        );
        if(validationResult instanceof InvalidResult) {
            throw new InitializationException(((InvalidResult) validationResult).getReason());
        }
    }
}
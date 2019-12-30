package com.beumuth.math.core.internal;

import com.beumuth.math.core.internal.application.ApplicationConfigurationValidator;
import com.beumuth.math.core.internal.application.ApplicationService;
import com.beumuth.math.core.internal.application.InitializationException;
import com.beumuth.math.core.internal.validation.InvalidResult;
import com.beumuth.math.core.internal.validation.ValidationResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

@SpringBootApplication(
    exclude = {DataSourceAutoConfiguration.class}
)
public class MathApplication {

    @Autowired
    private ApplicationConfigurationValidator applicationConfigurationValidator;

    @Value("${configurationLocation}")
    private String configurationLocation;

    @Autowired
    private ApplicationService applicationService;

    public static void main(String[] args) {
        SpringApplication.run(MathApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void validateStartup() {
        //Validate the application internal
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
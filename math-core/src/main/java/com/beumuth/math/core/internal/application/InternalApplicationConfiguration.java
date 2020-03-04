package com.beumuth.math.core.internal.application;

import com.github.instantpudd.validator.ClientErrorExceptionHandler;
import org.junit.internal.TextListener;
import org.junit.runner.JUnitCore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
public class InternalApplicationConfiguration {

    @Bean
    public JUnitCore jUnitCore() {
        JUnitCore core = new JUnitCore();
        core.addListener(new TextListener(System.out));
        return core;
    }

    @Bean
    public ClientErrorExceptionHandler clientErrorExceptionHandler() {
        return new ClientErrorExceptionHandler();
    }
}

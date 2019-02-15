package com.beumuth.collections.core.application;

import com.beumuth.collections.core.test.ResultJsonSerializer;
import com.github.instantpudd.validator.ClientErrorExceptionHandler;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.internal.TextListener;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class InternalApplicationConfiguration {

    @Autowired
    private ResultJsonSerializer resultJsonSerializer;

    @Primary
    @Bean(name="prettyPrint")
    public Gson prettyPrintGson() {
        return new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(Result.class, resultJsonSerializer)
            .create();
    }

    @Bean(name="minified")
    public Gson minifiedGson() {
        return new Gson();
    }

    @Bean
    public JUnitCore jUnitCore() {
        JUnitCore core = new JUnitCore();
        core.addListener(new TextListener(System.out));
        return core;
    }

    @Bean
    ClientErrorExceptionHandler clientErrorExceptionHandler() {
        return new ClientErrorExceptionHandler();
    }
}

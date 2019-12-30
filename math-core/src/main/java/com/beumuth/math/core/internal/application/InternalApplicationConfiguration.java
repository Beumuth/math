package com.beumuth.math.core.internal.application;

import com.beumuth.math.core.internal.database.CollectionsDataSource;
import com.beumuth.math.core.internal.test.ResultJsonSerializer;
import com.fatboyindustrial.gsonjodatime.Converters;
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
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
public class InternalApplicationConfiguration {

    @Autowired
    private ResultJsonSerializer resultJsonSerializer;

    @Autowired
    private CollectionsDataSource collectionsDataSource;

    @Primary
    @Bean(name="prettyPrint")
    public Gson prettyPrintGson() {
        return Converters.registerDateTime(
            new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(Result.class, resultJsonSerializer)
        ).create();
    }

    @Bean(name="minified")
    public Gson minifiedGson() {
        return Converters.registerDateTime(
            new GsonBuilder()
                .registerTypeAdapter(Result.class, resultJsonSerializer)
        ).create();
    }

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

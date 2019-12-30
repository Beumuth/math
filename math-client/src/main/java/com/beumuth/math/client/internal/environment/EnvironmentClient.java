package com.beumuth.math.client.internal.environment;

import com.beumuth.math.MathClient;
import feign.Param;
import feign.RequestLine;

import java.util.Set;

public interface EnvironmentClient extends MathClient {
    @RequestLine("GET api/environments")
    Set<EnvironmentConfiguration> getEnvironments();

    @RequestLine("GET api/environments/active")
    EnvironmentConfiguration getActiveEnvironment();

    @RequestLine("PUT api/environments/active")
    void setActiveEnvironment(String environmentName);

    @RequestLine("GET api/environments/environment/{name}")
    EnvironmentConfiguration getEnvironment(@Param("name") String name);

    @RequestLine("PUT api/environments/environment/{name}")
    Set<EnvironmentConfiguration> updateEnvironment(
        @Param("name") String name,
        EnvironmentConfiguration updatedEnvironment
    );

    @RequestLine("POST api/environments/environment")
    void createEnvironment(EnvironmentConfiguration environment);

    @RequestLine("DELETE api/environments/environment/{name}")
    void deleteEnvironment(@Param("name") String name);
}

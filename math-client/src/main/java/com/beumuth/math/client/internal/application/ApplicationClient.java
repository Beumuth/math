package com.beumuth.math.client.internal.application;

import com.beumuth.math.MathClient;
import feign.RequestLine;

public interface ApplicationClient extends MathClient {
    @RequestLine("GET api/application/mode")
    ApplicationMode getApplicationMode();

    @RequestLine("PUT api/application/mode")
    void setApplicationMode(ApplicationMode applicationMode);

    @RequestLine("GET api/application/internal")
    ApplicationConfiguration getApplicationConfiguration();

    @RequestLine("PUT api/application/internal")
    ApplicationConfiguration setApplicationConiguration(ApplicationConfiguration configuration);
}
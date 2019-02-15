package com.beumuth.collections.client.application;

import com.beumuth.collections.CollectionsClient;
import feign.RequestLine;

public interface ApplicationClient extends CollectionsClient {
    @RequestLine("GET api/application/mode")
    ApplicationMode getApplicationMode();

    @RequestLine("PUT api/application/mode")
    void setApplicationMode(ApplicationMode applicationMode);

    @RequestLine("GET api/application/configuration")
    ApplicationConfiguration getApplicationConfiguration();

    @RequestLine("PUT api/application/configuration")
    ApplicationConfiguration setApplicationConiguration(ApplicationConfiguration configuration);
}
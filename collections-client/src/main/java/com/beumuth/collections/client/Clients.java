package com.beumuth.collections.client;

import com.beumuth.collections.CollectionsClient;
import feign.Feign;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;

public class Clients {
    public static <T extends CollectionsClient> T getClient(Class<T> clientClass, String baseUrl) {
        return Feign
            .builder()
            .decoder(new GsonDecoder())
            .encoder(new GsonEncoder())
            .target(clientClass, baseUrl);
    }
}

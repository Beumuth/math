package com.beumuth.math.client;

import com.beumuth.math.MathClient;
import feign.Feign;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;

public class Clients {
    public static <T extends MathClient> T getClient(Class<T> clientClass, String baseUrl) {
        return Feign
            .builder()
            .decoder(new GsonDecoder())
            .encoder(new GsonEncoder())
            .target(clientClass, baseUrl);
    }
}

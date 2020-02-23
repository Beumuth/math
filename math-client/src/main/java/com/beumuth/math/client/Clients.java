package com.beumuth.math.client;

import com.beumuth.math.MathClient;
import com.beumuth.math.client.settheory.orderedset.OrderedSet;
import com.beumuth.math.client.settheory.orderedset.OrderedSetTypeAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import feign.Feign;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;

public class Clients {
    public static final Gson GSON = new GsonBuilder()
        .registerTypeAdapter(OrderedSet.class, new OrderedSetTypeAdapter())
        .create();

    public static <T extends MathClient> T getClient(Class<T> clientClass, String baseUrl) {
        return Feign
            .builder()
            .decoder(new GsonDecoder(GSON))
            .encoder(new GsonEncoder(GSON))
            .target(clientClass, baseUrl);
    }
}

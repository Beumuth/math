package com.beumuth.math.client.external.gson;

import com.beumuth.math.client.settheory.orderedset.OrderedSet;
import com.beumuth.math.client.settheory.orderedset.OrderedSetTypeAdapter;
import com.fatboyindustrial.gsonjodatime.Converters;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.runner.Result;

public class Gsons {
    private static GsonBuilder gsonBuilder() {
        return Converters.registerDateTime(
            new GsonBuilder()
                .registerTypeAdapter(Result.class, JsonSerializers.RESULT_JSON_SERIALIZER)
                .registerTypeAdapter(OrderedSet.class, new OrderedSetTypeAdapter())
        );
    }

    public static Gson prettyPrintGson() {
        return gsonBuilder()
            .setPrettyPrinting()
            .create();
    }

    public static Gson minifiedGson() {
        return gsonBuilder().create();
    }
}

package com.beumuth.math.client.settheory.orderedset;

import com.beumuth.math.client.Clients;
import com.google.gson.*;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class OrderedSetTypeAdapter<T> implements JsonSerializer<OrderedSet<T>>, JsonDeserializer<OrderedSet<T>> {
    @Override
    public OrderedSet deserialize(
        JsonElement json,
        Type typeOfT,
        JsonDeserializationContext context
    ) throws JsonParseException {
        if(json == null) {
            return null;
        }
        JsonArray jsonArray = json.getAsJsonArray();
        if(jsonArray.size() == 0) {
            return OrderedSets.empty();
        }
        return IntStream
            .range(0, jsonArray.size())
            .mapToObj(i ->
                Clients.GSON.fromJson(
                    jsonArray.get(i),
                    ((ParameterizedType) typeOfT).getActualTypeArguments()[0]
                )
            ).collect(Collectors.toCollection(OrderedSet::new));
    }

    @Override
    public JsonElement serialize(OrderedSet src, Type typeOfSrc, JsonSerializationContext context) {
        if(src == null) {
            return null;
        }

        JsonArray result = new JsonArray();
        src.forEach(o -> result.add(Clients.GSON.toJson(o)));
        return result;
    }
}

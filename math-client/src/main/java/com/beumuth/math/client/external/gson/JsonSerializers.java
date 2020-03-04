package com.beumuth.math.client.external.gson;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializer;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

public class JsonSerializers {
    public static final JsonSerializer<Result> RESULT_JSON_SERIALIZER = (result, type, jsonSerializationContext) -> {
        JsonObject resultJson = new JsonObject();
        resultJson.addProperty("numTests", result.getRunCount());
        resultJson.addProperty("numIgnored", result.getIgnoreCount());
        resultJson.addProperty("numFailed", result.getFailureCount());
        resultJson.addProperty("runTime", result.getRunTime());

        //Create and add failures
        JsonArray failuresJson = new JsonArray();
        for(Failure failure : result.getFailures()) {
            JsonObject failureElement = new JsonObject();
            failureElement.addProperty("description", failure.getDescription().getDisplayName());
            failureElement.addProperty("message", failure.getMessage());
            failureElement.addProperty("stackTrace", ExceptionUtils.getStackTrace(failure.getException()));
            failuresJson.add(failureElement);
        }
        resultJson.add("failures", failuresJson);

        return resultJson;
    };
}

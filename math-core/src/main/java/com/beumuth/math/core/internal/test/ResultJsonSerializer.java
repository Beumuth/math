package com.beumuth.math.core.internal.test;

import com.google.gson.*;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;

@Component
public class ResultJsonSerializer implements JsonSerializer<Result> {
    @Override
    public JsonElement serialize(Result result, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject resultJson = new JsonObject();
        resultJson.addProperty("numTests", result.getRunCount());
        resultJson.addProperty("numIgnored", result.getIgnoreCount());
        resultJson.addProperty("numFailed", result.getFailureCount());
        resultJson.addProperty("runTime", result.getRunTime());

        //Create and add failures
        JsonArray failuresJson = new JsonArray(result.getFailureCount());
        for(Failure failure : result.getFailures()) {
            JsonObject failureElement = new JsonObject();
            failureElement.addProperty("description", failure.getDescription().getDisplayName());
            failureElement.addProperty("message", failure.getMessage());
            failureElement.addProperty("stackTrace", ExceptionUtils.getStackTrace(failure.getException()));
            failuresJson.add(failureElement);
        }
        resultJson.add("failures", failuresJson);

        return resultJson;
    }
}

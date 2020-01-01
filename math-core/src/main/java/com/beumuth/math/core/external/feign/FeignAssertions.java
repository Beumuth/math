package com.beumuth.math.core.external.feign;

import feign.FeignException;
import org.junit.Assert;

import java.util.Set;

public class FeignAssertions {
    public static void assertExceptionLike(FeignException e, int statusCode) {
        Assert.assertEquals(statusCode, e.status());
    }

    /**
     *
     * @param statusCode
     * @param messageSnippet Ensure that the exception body includes this
     */
    public static void assertExceptionLike(FeignException e, int statusCode, String messageSnippet) {
        Assert.assertEquals(statusCode, e.status());
        Assert.assertTrue(
            "Response body [" + e.contentUTF8() + "] should contain [" + messageSnippet + "]",
            e.contentUTF8().contains(messageSnippet)
        );
    }

    /**
     *
     * @param statusCode
     * @param messageSnippets Ensure that the exception body includes all of these
     */
    public static void assertExceptionLike(FeignException e, int statusCode, Set<String> messageSnippets) {
        Assert.assertEquals(statusCode, e.status());
        messageSnippets
            .stream()
            .forEach(
                messageSnippet ->
                    Assert.assertTrue(
                        "Response body [" + e.contentUTF8() + "] should contain [" + messageSnippet + "]",
                        e.contentUTF8().contains(messageSnippet)
                    )
            );
    }
}

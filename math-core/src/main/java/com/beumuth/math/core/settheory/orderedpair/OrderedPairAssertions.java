package com.beumuth.math.core.settheory.orderedpair;

import com.beumuth.math.client.settheory.orderedpair.OrderedPair;
import org.junit.Assert;

public class OrderedPairAssertions {

    /**
     * Ensure that all their properties are the same.
     */
    public static void assertEquivalent(OrderedPair a, OrderedPair b) {
        Assert.assertTrue(
            (a.getId() == b.getId()) &&
            (a.getIdObject() == b.getIdObject()) &&
            (a.getIdLeft() == b.getIdLeft()) &&
            a.getIdRight() == b.getIdRight()
        );
    }
}

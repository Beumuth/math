package com.beumuth.math.core.jgraph.element;

import com.beumuth.math.client.jgraph.Element;
import org.junit.Assert;

public class ElementAssertions {
    public static void assertElementsSame(Element a, Element b) {
        Assert.assertEquals(a.getId(), b.getId());
        Assert.assertEquals(a.getA(), b.getA());
        Assert.assertEquals(a.getB(), b.getB());
    }
}

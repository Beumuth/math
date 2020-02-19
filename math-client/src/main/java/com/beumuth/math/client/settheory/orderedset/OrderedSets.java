package com.beumuth.math.client.settheory.orderedset;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class OrderedSets {
    public static <T> OrderedSet<T> empty() {
        return new OrderedSet<>();
    }

    public static <T> OrderedSet<T> singleton(T element) {
        return with(Collections.singleton(element));
    }

    public static <T> OrderedSet<T> with(Collection<? extends T> elements) {
        return new OrderedSet<>(elements);
    }

    public static <T> OrderedSet<T> with(T... elements) {
        OrderedSet<T> set = new OrderedSet<>(elements.length);
        Collections.addAll(set, elements);
        return set;
    }

    public static <T> OrderedSet<T> with(Collection<? extends T> ... elementCollections) {
        OrderedSet<T> set = empty();
        for(Collection<? extends T> elementCollection : elementCollections) {
            set.addAll(elementCollection);
        }
        return set;
    }

    public static <T> OrderedSet<T> nCopies(int n, T element) {
        return IntStream
            .range(0, n)
            .mapToObj(i -> element)
            .collect(Collectors.toCollection(OrderedSets::with));
    }
}

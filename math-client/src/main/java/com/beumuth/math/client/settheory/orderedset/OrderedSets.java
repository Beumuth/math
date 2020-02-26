package com.beumuth.math.client.settheory.orderedset;

import java.util.Collection;
import java.util.Collections;
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

    public static <T> OrderedSet<T> with(Collection<T>... collections) {
        OrderedSet<T> orderedSet = empty();
        IntStream
            .range(0, collections.length)
            .forEach(i -> orderedSet.addAll(collections[i]));
        return orderedSet;
    }

    public static <T> OrderedSet<T> merge(Collection<T> collection, T element) {
        OrderedSet<T> result = with(collection);
        result.add(element);
        return result;
    }

    public static <T> OrderedSet<T> merge(T element, Collection<T> collection) {
        OrderedSet<T> result = singleton(element);
        result.addAll(collection);
        return result;
    }

    public static <T> OrderedSet<T> merge(T element, Collection<T>... collections) {
        return merge(
            element,
            with(collections)
        );
    }
}

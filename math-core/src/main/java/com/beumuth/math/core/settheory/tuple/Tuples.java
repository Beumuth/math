package com.beumuth.math.core.settheory.tuple;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.stream.IntStream;

public class Tuples {
    public static <T> List<T> join(List<T>... lists) {
        List<T> combined = Lists.newArrayList();
        IntStream
            .range(0, lists.length)
            .forEach(i -> combined.addAll(lists[i]));
        return combined;
    }

    public static <T> List<T> join(T element, List<T> list) {
        List<T> combined = Lists.newArrayList(element);
        combined.addAll(list);
        return combined;
    }

    public static <T> List<T> join(T element, List<T>... lists) {
        List<T> combined = Lists.newArrayList(element);
        IntStream
            .range(0, lists.length)
            .forEach(i -> combined.addAll(lists[i]));
        return combined;
    }

    public static <T> List<T> join(List<T> list, T element) {
        List<T> combined = Lists.newArrayList(list);
        combined.add(element);
        return combined;
    }
}
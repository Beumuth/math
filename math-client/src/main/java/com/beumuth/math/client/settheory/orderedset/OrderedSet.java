package com.beumuth.math.client.settheory.orderedset;

import com.google.common.collect.Sets;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class OrderedSet<T> implements List<T>, Set<T> {

    private ArrayList<T> implementation;

    public OrderedSet() {
        implementation = new ArrayList<>();
    }

    public OrderedSet(int initialCapacity) {
        implementation = new ArrayList<>(initialCapacity);
    }

    public OrderedSet(Collection<? extends T> c) {
        implementation = new ArrayList<>(
            c instanceof Set ? c : Sets.newHashSet(c)
        );
    }

    @Override
    public int size() {
        return implementation.size();
    }

    @Override
    public boolean isEmpty() {
        return implementation.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return implementation.contains(o);
    }

    @Override
    public Iterator<T> iterator() {
        return implementation.iterator();
    }

    @Override
    public Object[] toArray() {
        return implementation.toArray();
    }

    @Override
    public <T1> T1[] toArray(T1[] a) {
        return implementation.toArray(a);
    }

    @Override
    public boolean add(T t) {
        return implementation.contains(t) || implementation.add(t);
    }

    @Override
    public boolean remove(Object o) {
        return implementation.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return implementation.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        return implementation.addAll(
            c
                .stream()
                .filter(Predicate.not(this::contains))
                .collect(Collectors.toSet())
        );
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        return implementation.addAll(
            index,
            c
                .stream()
                .filter(Predicate.not(this::contains))
                .collect(Collectors.toSet())
        );
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return implementation.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return implementation.retainAll(c);
    }

    @Override
    public void clear() {
        implementation.clear();
    }

    @Override
    public T get(int index) {
        return implementation.get(index);
    }

    @Override
    public T set(int index, T element) {
        return implementation.contains(element) ? implementation.get(index) : implementation.set(index, element);
    }

    @Override
    public void add(int index, T element) {
        if(! implementation.contains(element)){
            implementation.add(index, element);
        }
    }

    @Override
    public T remove(int index) {
        return implementation.remove(index);
    }

    @Override
    public int indexOf(Object o) {
        return implementation.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return implementation.lastIndexOf(o);
    }

    @Override
    public ListIterator<T> listIterator() {
        return implementation.listIterator();
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        return implementation.listIterator(index);
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        return implementation.subList(fromIndex, toIndex);
    }

    public OrderedSet<T> subOrderedSet(int fromIndex, int toIndex) {
        return new OrderedSet<>(implementation.subList(fromIndex, toIndex));
    }

    @Override
    public Spliterator<T> spliterator() {
        return List.super.spliterator();
    }

    @Override
    public int hashCode() {
        return implementation.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return implementation.equals(obj);
    }
}

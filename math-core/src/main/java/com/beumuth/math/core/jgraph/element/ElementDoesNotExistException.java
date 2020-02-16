package com.beumuth.math.core.jgraph.element;

public class ElementDoesNotExistException extends RuntimeException {
    public ElementDoesNotExistException(long id) {
        super("Element with id [" + id + "] does not exist");
    }
}

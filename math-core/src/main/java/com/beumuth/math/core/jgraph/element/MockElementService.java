package com.beumuth.math.core.jgraph.element;

import com.beumuth.math.client.jgraph.Element;
import com.beumuth.math.client.settheory.orderedset.OrderedSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class MockElementService {
    @Autowired
    private ElementService elementService;

    public long idNonexistent() {
        long id = elementService.createNode();
        elementService.deleteElement(id);
        return id;
    }

    public Set<Long> idNonexistentMultiple(int number) {
        Set<Long> ids = elementService.createNodes(number);
        elementService.deleteElements(ids);
        return ids;
    }

    public Element node() {
        return elementService.getElement(
            elementService.createNode()
        );
    }

    public Element edge(long idA, long idB) {
        return elementService.getElement(
            elementService.createElement(idA, idB)
        );
    }

    public Element pendantFrom(long idFrom) {
        return elementService.getElement(
            elementService.createPendantFrom(idFrom)
        );
    }

    public Element pendantTo(long idTo) {
        return elementService.getElement(
            elementService.createPendantTo(idTo)
        );
    }

    public Element loopOn(long idOn) {
        return elementService.getElement(
            elementService.createLoopOn(idOn)
        );
    }

    public Element nonexistent() {
        Element nonexistent = elementService.getElement(
            elementService.createNode()
        );
        elementService.deleteElement(nonexistent.getId());
        return nonexistent;
    }

    public OrderedSet<Element> nodes(int number) {
        return elementService.getElements(
            elementService.createNodes(number)
        );
    }

    public OrderedSet<Element> pendantsFrom(long idFrom, int number) {
        return elementService.getElements(
            elementService.createPendantsFrom(idFrom, number)
        );
    }

    public OrderedSet<Element> pendantsTo(long idTo, int number) {
        return elementService.getElements(
            elementService.createPendantsTo(idTo, number)
        );
    }

    public OrderedSet<Element> loopsOn(long idOn, int number) {
        return elementService.getElements(
            elementService.createLoopsOn(idOn, number)
        );
    }
}

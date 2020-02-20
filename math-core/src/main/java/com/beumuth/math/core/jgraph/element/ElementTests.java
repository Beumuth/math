package com.beumuth.math.core.jgraph.element;

import com.beumuth.math.client.jgraph.CreateElementRequest;
import com.beumuth.math.client.jgraph.Element;
import com.beumuth.math.client.jgraph.ElementClient;
import com.beumuth.math.client.jgraph.UpdateElementRequest;
import com.beumuth.math.client.settheory.orderedset.OrderedSet;
import com.beumuth.math.client.settheory.orderedset.OrderedSets;
import com.beumuth.math.core.internal.client.ClientService;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import feign.FeignException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.beumuth.math.core.external.feign.FeignAssertions.assertExceptionLike;
import static com.beumuth.math.core.jgraph.element.ElementAssertions.assertElementsSame;
import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ElementTests {
    @Autowired
    @Qualifier(value="JGraphElementService")
    private ElementService elementService;
    @Autowired
    private MockElementService mockElementService;
    @Autowired
    private ClientService clientService;

    private ElementClient elementClient;

    @Before
    public void setupTests() {
        elementClient = clientService.getClient(ElementClient.class);
        elementService.seed();
    }

    @Test
    public void doesElementExistTest_doesExist_shouldReturnTrue() {
        long idElement = elementService.createNode();
        try {
            assertTrue(elementClient.doesElementExist(idElement));
        } finally {
            elementService.deleteElement(idElement);
        }
    }

    @Test
    public void doesElementExistTest_doesNotExist_shouldReturnFalse() {
        assertFalse(
            elementClient.doesElementExist(
                mockElementService
                    .nonexistent()
                    .getId()
            )
        );
    }

    @Test
    public void doAnyElementsExist_noneExist_shouldReturnFalse() {
        assertFalse(
            elementClient.doAnyElementsExist(
               mockElementService.idNonexistentMultiple(10)
            )
        );
    }

    @Test
    public void doAnyElementsExist_oneExists_shouldReturnTrue() {
        long idElement = elementService.createNode();
        Set<Long> idElements = mockElementService.idNonexistentMultiple(9);
        idElements.add(idElement);
        try {
            assertTrue(
                elementClient.doAnyElementsExist(idElements)
            );
        } finally {
            elementService.deleteElement(idElement);
        }
    }

    @Test
    public void doAnyElementsExist_allExist_shouldReturnTrue() {
        Set<Long> idElements = elementService.createNodes(10);
        try {
            assertTrue(
                elementClient.doAnyElementsExist(idElements)
            );
        } finally {
            elementService.deleteElements(idElements);
        }
    }

    @Test
    public void doAnyElementsExist_emptyList_shouldReturn400() {
        Set<Long> idElements = elementService.createNodes(10);
        try {
            assertTrue(
                elementClient.doAnyElementsExist(idElements)
            );
        } catch(FeignException e) {
            assertExceptionLike(e, 400, "empty");
        } finally {
            elementService.deleteElements(idElements);
        }
    }
    
    @Test
    public void doAllElementsExist_noneExist_shouldReturnFalse() {
        assertFalse(
            elementClient.doAllElementsExist(
                mockElementService.idNonexistentMultiple(10)
            )
        );
    }

    @Test
    public void doAllElementsExist_oneExists_shouldReturnFalse() {
        long idElement = elementService.createNode();
        Set<Long> idElements = mockElementService.idNonexistentMultiple(9);
        idElements.add(idElement);
        try {
            assertFalse(
                elementClient.doAllElementsExist(idElements)
            );
        } finally {
            elementService.deleteElement(idElement);
        }
    }

    @Test
    public void doAllElementsExist_allExist_shouldReturnTrue() {
        Set<Long> idElements = elementService.createNodes(10);
        try {
            assertTrue(
                elementClient.doAllElementsExist(idElements)
            );
        } finally {
            elementService.deleteElements(idElements);
        }
    }

    @Test
    public void doAllElementsExist_emptyList_shouldReturn400() {
        Set<Long> idElements = elementService.createNodes(10);
        try {
            assertTrue(
                elementClient.doAnyElementsExist(idElements)
            );
        } catch(FeignException e) {
            assertExceptionLike(e, 400, "empty");
        } finally {
            elementService.deleteElements(idElements);
        }
    }
    
    @Test
    public void doesElementExistWithAOrBTest_existsWithAOnly_shouldReturnTrue() {
        long idNode = elementService.createNode();
        long idPendant = elementService.createPendantTo(idNode);
        long idNonexistent = mockElementService.idNonexistent();
        try {
            assertTrue(
                elementClient.doesElementExistWithAOrB(idPendant, idNonexistent)
            );
        } finally {
            elementService.deleteElement(idNode);
            elementService.deleteElement(idPendant);
        }
    }

    @Test
    public void doesElementExistWithAOrBTest_existsWithBOnly_shouldReturnTrue() {
        long idNode = elementService.createNode();
        long idPendant = elementService.createPendantFrom(idNode);
        long idNonexistent = mockElementService.idNonexistent();
        try {
            assertTrue(
                elementClient.doesElementExistWithAOrB(idNonexistent, idPendant)
            );
        } finally {
            elementService.deleteElement(idNode);
            elementService.deleteElement(idPendant);
        }
    }

    @Test
    public void doesElementExistWithAOrBTest_existsWithAAndB_shouldReturnTrue() {
        long idNode = elementService.createNode();
        long idPendantFrom = elementService.createPendantFrom(idNode);
        long idPendantTo = elementService.createPendantTo(idNode);
        try {
            assertTrue(
                elementClient.doesElementExistWithAOrB(idPendantTo, idPendantFrom)
            );
        } finally {
            elementService.deleteElements(Sets.newHashSet(idNode, idPendantFrom, idPendantTo));
        }
    }

    @Test
    public void doesElementExistWithAOrBTest_neither_shouldReturnFalse() {
        long idNonexistentA = mockElementService.idNonexistent();
        long idNonexistentB = mockElementService.idNonexistent();
        assertFalse(
            elementClient.doesElementExistWithAOrB(idNonexistentA, idNonexistentB)
        );
    }

    @Test
    public void doesElementExistWithAOrBTest_AAndBSame_exists_shouldReturnTrue() {
        long idNode = elementService.createNode();
        try {
            assertTrue(
                elementClient.doesElementExistWithAOrB(idNode, idNode)
            );
        } finally {
            elementService.deleteElement(idNode);
        }
    }
    
    @Test
    public void doesElementExistWithAOrBTest_AAndBSame_doesNotExist_shouldReturnTrue() {
        long idNonexistent = mockElementService.idNonexistent();
        assertTrue(
            elementClient.doesElementExistWithAOrB(idNonexistent, idNonexistent)
        );
    }

    @Test
    public void doesElementExistWithAAndBTest_existsWithAOnly_shouldReturnFalse() {
        long idNode = elementService.createNode();
        long idPendant = elementService.createPendantTo(idNode);
        long idNonexistent = mockElementService.idNonexistent();
        try {
            assertFalse(
                elementClient.doesElementExistWithAAndB(idPendant, idNonexistent)
            );
        } finally {
            elementService.deleteElement(idNode);
            elementService.deleteElement(idPendant);
        }
    }

    @Test
    public void doesElementExistWithAAndBTest_existsWithBOnly_shouldReturnFalse() {
        long idNode = elementService.createNode();
        long idPendant = elementService.createPendantFrom(idNode);
        long idNonexistent = mockElementService.idNonexistent();
        try {
            assertFalse(
                elementClient.doesElementExistWithAAndB(idNonexistent, idPendant)
            );
        } finally {
            elementService.deleteElement(idNode);
            elementService.deleteElement(idPendant);
        }
    }

    @Test
    public void doesElementExistWithAAndBTest_existsWithAAndB_shouldReturnTrue() {
        long idNode = elementService.createNode();
        long idPendantFrom = elementService.createPendantFrom(idNode);
        long idPendantTo = elementService.createPendantTo(idNode);
        try {
            assertTrue(
                elementClient.doesElementExistWithAAndB(idPendantTo, idPendantFrom)
            );
        } finally {
            elementService.deleteElements(Sets.newHashSet(idNode, idPendantFrom, idPendantTo));
        }
    }

    @Test
    public void doesElementExistWithAAndBTest_neither_shouldReturnFalse() {
        long idNonexistentA = mockElementService.idNonexistent();
        long idNonexistentB = mockElementService.idNonexistent();
        assertFalse(
            elementClient.doesElementExistWithAAndB(idNonexistentA, idNonexistentB)
        );
    }

    @Test
    public void doesElementExistWithAAndBTest_AAndBSame_exists_shouldReturnTrue() {
        long idNode = elementService.createNode();
        try {
            assertTrue(
                elementClient.doesElementExistWithAAndB(idNode, idNode)
            );
        } finally {
            elementService.deleteElement(idNode);
        }
    }

    @Test
    public void doesElementExistWithAAndBTest_AAndBSame_doesNotExist_shouldReturnTrue() {
        long idNonexistent = mockElementService.idNonexistent();
        assertTrue(
            elementClient.doesElementExistWithAAndB(idNonexistent, idNonexistent)
        );
    }

    @Test
    public void isElementNodeTest_isNode_shouldReturnTrue() {
        long idNode = elementService.createNode();
        try {
            assertTrue(elementClient.isElementNode(idNode));
        } finally {
            elementService.deleteElement(idNode);
        }
    }

    @Test
    public void isElementNodeTest_isNotNode_shouldReturnFalse() {
        long idNode = elementService.createNode();
        Set<Long> idNonNodes = Sets.newHashSet(
            elementService.createElement(idNode, idNode),
            elementService.createElement(0, idNode),
            elementService.createElement(0, idNode)
        );
        try {
            idNonNodes.forEach(idElement -> assertFalse(elementClient.isElementNode(idElement)));
        } finally {
            idNonNodes.add(idNode);
            elementService.deleteElements(idNonNodes);
        }
    }

    @Test
    public void isElementNodeTest_doesNotExist_shouldReturn404() {
        long idNonexistent = mockElementService.idNonexistent();
        try {
            elementClient.isElementNode(idNonexistent);
            fail();
        } catch(FeignException e) {
            assertExceptionLike(e, 404, idNonexistent + "");
        }
    }
    
    @Test
    public void getNumElementsWithAOrBTest_noneExist_shouldReturnZero() {
        long idNonexistentA = mockElementService.idNonexistent();
        long idNonexistentB = mockElementService.idNonexistent();
        assertEquals(0, elementClient.numElementsWithAOrB(idNonexistentA, idNonexistentB));
    }

    @Test
    public void areElementsNodesTest_emptyList_shouldReturnEmptyOrderedSet() {
        assertEquals(OrderedSets.empty(), elementClient.areElementsNodes(OrderedSets.empty()));
    }

    @Test
    public void areElementsNodesTest_oneElement_isNode_shouldReturnTrue() {
        long idNode = elementService.createNode();
        try {
            assertEquals(Lists.newArrayList(true), elementClient.areElementsNodes(OrderedSets.singleton(idNode)));
        } finally {
            elementService.deleteElement(idNode);
        }
    }

    @Test
    public void areElementsNodesTest_oneElement_isNotNode_shouldReturnFalse() {
        long idNode = elementService.createNode();
        Set<Long> idNonNodes = Sets.newHashSet(
            elementService.createElement(idNode, idNode),
            elementService.createElement(0, idNode),
            elementService.createElement(0, idNode)
        );
        try {
            idNonNodes.forEach(idElement ->
                assertEquals(
                    Collections.singletonList(false),
                    elementClient.areElementsNodes(OrderedSets.singleton(idElement))
                )
            );
        } finally {
            idNonNodes.add(idNode);
            elementService.deleteElements(idNonNodes);
        }
    }

    @Test
    public void areElementsNodesTest_oneElement_doesNotExist_shouldReturnFalse() {
        assertEquals(
            Collections.singletonList(false),
            elementClient.areElementsNodes(OrderedSets.singleton(mockElementService.idNonexistent()))
        );
    }

    @Test
    public void areElementsNodesTest_multipleElements() {
        long idNodeA = elementService.createNode();
        long idNodeB = elementService.createNode();
        OrderedSet<Long> idElements = OrderedSets.with(
            idNodeA,
            elementService.createElement(idNodeA, idNodeA),
            elementService.createElement(0, idNodeA),
            elementService.createElement(0, idNodeB),
            elementService.createElement(idNodeA, idNodeB),
            idNodeB
        );
        try {
            assertEquals(
                OrderedSets.with(true, false, false, false, false, true),
                elementClient.areElementsNodes(idElements)
            );
        } finally {
            elementService.deleteElements(Sets.newHashSet(idElements));
        }
    }

    @Test
    public void isElementPendantFromTest_itIs_shouldReturnTrue() {
        long idNode = elementService.createNode();
        long idPendantFrom = elementService.createPendantFrom(idNode);
        try {
            assertTrue(elementClient.isElementPendantFrom(idPendantFrom, idNode));
        } finally {
            elementService.deleteElements(Sets.newHashSet(idNode, idPendantFrom));
        }
    }

    @Test
    public void isElementPendantFromTest_itIsNot_shouldReturnFalse() {
        long idNode = elementService.createNode();
        long idLoopOn = elementService.createLoopOn(idNode);
        long idPendantTo = elementService.createPendantTo(idNode);
        try {
            assertFalse(elementClient.isElementPendantFrom(idLoopOn, idNode));
            assertFalse(elementClient.isElementPendantFrom(idPendantTo, idNode));
        } finally {
            elementService.deleteElements(Sets.newHashSet(idNode, idLoopOn, idPendantTo));
        }
    }

    @Test
    public void isElementPendantFromTest_elementDoesNotExist_shouldReturn404() {
        long idNode = elementService.createNode();
        long idNonexistent = mockElementService.idNonexistent();
        try {
            elementClient.isElementPendantFrom(idNonexistent, idNode);
            fail();
        } catch(FeignException e) {
            assertExceptionLike(e, 404, idNonexistent + "");
        } finally {
            elementService.deleteElement(idNode);
        }
    }

    @Test
    public void isElementPendantFromTest_pendantDoesNotExist_shouldReturn404() {
        long idNode = elementService.createNode();
        long idNonexistent = mockElementService.idNonexistent();
        try {
            elementClient.isElementPendantFrom(idNode, idNonexistent);
            fail();
        } catch(FeignException e) {
            assertExceptionLike(e, 404, idNonexistent + "");
        } finally {
            elementService.deleteElement(idNode);
        }
    }

    @Test
    public void areElementsPendantsFromTest_noElements_shouldReturnEmptyList() {
        long idNode = elementService.createNode();
        try {
            assertEquals(
                OrderedSets.empty(),
                elementClient.areElementsPendantsFrom(idNode, OrderedSets.empty())
            );
        } finally {
            elementService.deleteElement(idNode);
        }
    }

    @Test
    public void areElementsPendantsFromTest_oneElement_isPendantFrom_shouldReturnTrue() {
        long idNode = elementService.createNode();
        long idPendantFrom = elementService.createPendantFrom(idNode);
        try {
            assertEquals(
                Collections.singletonList(true),
                elementClient.areElementsPendantsFrom(idPendantFrom, OrderedSets.singleton(idNode))
            );
        } finally {
            elementService.deleteElement(idPendantFrom);
            elementService.deleteElement(idNode);
        }
    }

    @Test
    public void areElementsPendantsFromTest_oneElement_isNotPendantFrom_shouldReturnFalse() {
        long idNode = elementService.createNode();
        long idPendantTo = elementService.createPendantTo(idNode);
        try {
            assertEquals(
                Collections.singletonList(false),
                elementClient.areElementsPendantsFrom(idPendantTo, OrderedSets.singleton(idNode))
            );
        } finally {
            elementService.deleteElement(idPendantTo);
            elementService.deleteElement(idNode);
        }
    }

    @Test
    public void areElementsPendantsFromTest_oneElement_elementDoesNotExist_shouldReturnFalse() {
        long idNode = elementService.createNode();
        long idNonexistent = mockElementService.idNonexistent();
        try {
            assertEquals(
                Collections.singletonList(false),
                elementClient.areElementsPendantsFrom(idNode, OrderedSets.singleton(idNonexistent))
            );
        } finally {
            elementService.deleteElement(idNode);
        }
    }

    @Test
    public void areElementsPendantsFromTest_oneElement_pendantDoesNotExist_shouldReturn404() {
        long idNode = elementService.createNode();
        long idNonexistent = mockElementService.idNonexistent();
        try {
            assertEquals(
                Collections.singletonList(false),
                elementClient.areElementsPendantsFrom(idNonexistent, OrderedSets.singleton(idNode))
            );
        } catch(FeignException e) {
            assertExceptionLike(e, 404, idNonexistent + "");
        } finally {
            elementService.deleteElement(idNode);
        }
    }

    @Test
    public void areElementsPendantsFromTest_manyElements() {
        long idNode = elementService.createNode();
        OrderedSet<Long> idElements = OrderedSets.with(idNode);
        idElements.addAll(elementService.createPendantsFrom(idNode, 5));
        idElements.addAll(elementService.createLoopsOn(idNode, 2));
        idElements.add(elementService.createElement(idNode, idElements.get(1)));
        try {
            assertEquals(
                Lists.newArrayList(
                    false,
                    true, true, true, true, true,
                    false, false, false
                ),
                elementClient.areElementsPendantsFrom(idNode, idElements)
            );
        } finally {
            elementService.deleteElements(Sets.newHashSet(idElements));
        }
    }

    @Test
    public void isElementPendantToTest_itIs_shouldReturnTrue() {
        long idNode = elementService.createNode();
        long idPendantTo = elementService.createPendantTo(idNode);
        try {
            assertTrue(elementClient.isElementPendantTo(idPendantTo, idNode));
        } finally {
            elementService.deleteElements(Sets.newHashSet(idNode, idPendantTo));
        }
    }

    @Test
    public void isElementPendantToTest_itIsNot_shouldReturnFalse() {
        long idNode = elementService.createNode();
        long idLoopOn = elementService.createLoopOn(idNode);
        long idPendantFrom = elementService.createPendantFrom(idNode);
        try {
            assertFalse(elementClient.isElementPendantTo(idLoopOn, idNode));
            assertFalse(elementClient.isElementPendantTo(idPendantFrom, idNode));
        } finally {
            elementService.deleteElements(Sets.newHashSet(idNode, idLoopOn, idPendantFrom));
        }
    }

    @Test
    public void isElementPendantToTest_elementDoesNotExist_shouldReturn404() {
        long idNode = elementService.createNode();
        long idNonexistent = mockElementService.idNonexistent();
        try {
            elementClient.isElementPendantTo(idNonexistent, idNode);
            fail();
        } catch(FeignException e) {
            assertExceptionLike(e, 404, idNonexistent + "");
        } finally {
            elementService.deleteElement(idNode);
        }
    }

    @Test
    public void isElementPendantToTest_pendantDoesNotExist_shouldReturn404() {
        long idNode = elementService.createNode();
        long idNonexistent = mockElementService.idNonexistent();
        try {
            elementClient.isElementPendantTo(idNode, idNonexistent);
            fail();
        } catch(FeignException e) {
            assertExceptionLike(e, 404, idNonexistent + "");
        } finally {
            elementService.deleteElement(idNode);
        }
    }

    @Test
    public void areElementsPendantsToTest_noElements_shouldReturnEmptyOrderedSet() {
        long idNode = elementService.createNode();
        try {
            assertEquals(
                OrderedSets.empty(),
                elementClient.areElementsPendantsTo(idNode, OrderedSets.empty())
            );
        } finally {
            elementService.deleteElement(idNode);
        }
    }

    @Test
    public void areElementsPendantsToTest_oneElement_isPendantTo_shouldReturnTrue() {
        long idNode = elementService.createNode();
        long idPendantTo = elementService.createPendantTo(idNode);
        try {
            assertEquals(
                Collections.singletonList(true),
                elementClient.areElementsPendantsTo(idPendantTo, OrderedSets.singleton(idNode))
            );
        } finally {
            elementService.deleteElement(idPendantTo);
            elementService.deleteElement(idNode);
        }
    }

    @Test
    public void areElementsPendantsToTest_oneElement_isNotPendantTo_shouldReturnFalse() {
        long idNode = elementService.createNode();
        long idPendantTo = elementService.createPendantTo(idNode);
        try {
            assertEquals(
                Collections.singletonList(false),
                elementClient.areElementsPendantsTo(idPendantTo, OrderedSets.singleton(idNode))
            );
        } finally {
            elementService.deleteElement(idPendantTo);
            elementService.deleteElement(idNode);
        }
    }

    @Test
    public void areElementsPendantsToTest_oneElement_elementDoesNotExist_shouldReturnFalse() {
        long idNode = elementService.createNode();
        long idNonexistent = mockElementService.idNonexistent();
        try {
            assertEquals(
                Collections.singletonList(false),
                elementClient.areElementsPendantsTo(idNode, OrderedSets.singleton(idNonexistent))
            );
        } finally {
            elementService.deleteElement(idNode);
        }
    }

    @Test
    public void areElementsPendantsToTest_oneElement_pendantDoesNotExist_shouldReturn404() {
        long idNode = elementService.createNode();
        long idNonexistent = mockElementService.idNonexistent();
        try {
            assertEquals(
                Collections.singletonList(false),
                elementClient.areElementsPendantsTo(idNonexistent, OrderedSets.singleton(idNode))
            );
        } catch(FeignException e) {
            assertExceptionLike(e, 404, idNonexistent + "");
        } finally {
            elementService.deleteElement(idNode);
        }
    }

    @Test
    public void areElementsPendantsToTest_manyElements() {
        long idNode = elementService.createNode();
        OrderedSet<Long> idElements = OrderedSets.with(idNode);
        idElements.addAll(elementService.createPendantsTo(idNode, 5));
        idElements.addAll(elementService.createLoopsOn(idNode, 2));
        idElements.add(elementService.createElement(idNode, idElements.get(1)));
        try {
            assertEquals(
                Lists.newArrayList(
                    false,
                    true, true, true, true, true,
                    false, false, false
                ),
                elementClient.areElementsPendantsTo(idNode, idElements)
            );
        } finally {
            elementService.deleteElements(idElements);
        }
    }

    @Test
    public void isElementLoopOnTest_itIs_shouldReturnTrue() {
        long idNode = elementService.createNode();
        long idLoopOn = elementService.createLoopOn(idNode);
        try {
            assertTrue(elementClient.isElementLoopOn(idLoopOn, idNode));
        } finally {
            elementService.deleteElements(OrderedSets.with(idNode, idLoopOn));
        }
    }

    @Test
    public void isElementLoopOnTest_itIsNot_shouldReturnFalse() {
        long idNode = elementService.createNode();
        long idPendantTo = elementService.createPendantTo(idNode);
        long idEdge = elementService.createElement(idNode, idPendantTo);
        try {
            assertFalse(elementClient.isElementLoopOn(idEdge, idNode));
            assertFalse(elementClient.isElementLoopOn(idPendantTo, idNode));
        } finally {
            elementService.deleteElements(OrderedSets.with(idNode, idEdge, idPendantTo));
        }
    }

    @Test
    public void isElementLoopOnTest_elementDoesNotExist_shouldReturn404() {
        long idNode = elementService.createNode();
        long idNonexistent = mockElementService.idNonexistent();
        try {
            elementClient.isElementLoopOn(idNonexistent, idNode);
            fail();
        } catch(FeignException e) {
            assertExceptionLike(e, 404, idNonexistent + "");
        } finally {
            elementService.deleteElement(idNode);
        }
    }

    @Test
    public void isElementLoopOnTest_loopOnDoesNotExist_shouldReturn404() {
        long idNode = elementService.createNode();
        long idNonexistent = mockElementService.idNonexistent();
        try {
            elementClient.isElementLoopOn(idNode, idNonexistent);
            fail();
        } catch(FeignException e) {
            assertExceptionLike(e, 404, idNonexistent + "");
        } finally {
            elementService.deleteElement(idNode);
        }
    }

    @Test
    public void areElementsLoopsOnTest_noElements_shouldReturnEmptyOrderedSet() {
        long idNode = elementService.createNode();
        try {
            assertEquals(
                OrderedSets.empty(),
                elementClient.areElementsLoopsOn(idNode, OrderedSets.empty())
            );
        } finally {
            elementService.deleteElement(idNode);
        }
    }

    @Test
    public void areElementsLoopsOnTest_oneElement_isLoopOn_shouldReturnTrue() {
        long idNode = elementService.createNode();
        long idLoopOn = elementService.createLoopOn(idNode);
        try {
            assertEquals(
                Collections.singletonList(true),
                elementClient.areElementsLoopsOn(idLoopOn, OrderedSets.singleton(idNode))
            );
        } finally {
            elementService.deleteElement(idLoopOn);
            elementService.deleteElement(idNode);
        }
    }

    @Test
    public void areElementsLoopsOnTest_oneElement_isNotLoopOn_shouldReturnFalse() {
        long idNode = elementService.createNode();
        long idLoopOn = elementService.createLoopOn(idNode);
        try {
            assertEquals(
                Collections.singletonList(false),
                elementClient.areElementsLoopsOn(idLoopOn, OrderedSets.singleton(idNode))
            );
        } finally {
            elementService.deleteElement(idLoopOn);
            elementService.deleteElement(idNode);
        }
    }

    @Test
    public void areElementsLoopsOnTest_oneElement_elementDoesNotExist_shouldReturnFalse() {
        long idNode = elementService.createNode();
        long idNonexistent = mockElementService.idNonexistent();
        try {
            assertEquals(
                Collections.singletonList(false),
                elementClient.areElementsLoopsOn(idNode, OrderedSets.singleton(idNonexistent))
            );
        } finally {
            elementService.deleteElement(idNode);
        }
    }

    @Test
    public void areElementsLoopsOnTest_oneElement_pendantDoesNotExist_shouldReturn404() {
        long idNode = elementService.createNode();
        long idNonexistent = mockElementService.idNonexistent();
        try {
            assertEquals(
                Collections.singletonList(false),
                elementClient.areElementsLoopsOn(idNonexistent, OrderedSets.singleton(idNode))
            );
        } catch(FeignException e) {
            assertExceptionLike(e, 404, idNonexistent + "");
        } finally {
            elementService.deleteElement(idNode);
        }
    }

    @Test
    public void areElementsLoopsOnTest_manyElements() {
        long idNode = elementService.createNode();
        OrderedSet<Long> idElements = OrderedSets.with(idNode);
        idElements.addAll(elementService.createLoopsOn(idNode, 5));
        idElements.addAll(elementService.createPendantsTo(idNode, 2));
        idElements.add(elementService.createElement(idNode, idElements.get(1)));
        try {
            assertEquals(
                OrderedSets.with(
                    false,
                    true, true, true, true, true,
                    false, false, false
                ),
                elementClient.areElementsLoopsOn(idNode, idElements)
            );
        } finally {
            elementService.deleteElements(idElements);
        }
    }

    @Test
    public void isElementEndpointTest_isEndpoint_shouldReturnTrue() {
        long idNode = elementService.createNode();
        long idPendant = elementService.createPendantTo(idNode);
        try {
            assertTrue(elementClient.isElementEndpoint(idNode));
        } finally {
            elementService.deleteElements(
                OrderedSets.with(idNode, idPendant)
            );
        }
    }

    @Test
    public void isElementEndpointTest_isNotEndpoint_shouldReturnFalse() {
        long idNode = elementService.createNode();
        try {
            assertFalse(elementClient.isElementEndpoint(idNode));
        } finally {
            elementService.deleteElement(idNode);
        }
    }

    @Test
    public void isElementEndpointTest_doesNotExist_shouldReturn404() {
        long idNonexistent = mockElementService.idNonexistent();
        try {
            elementClient.isElementEndpoint(idNonexistent);
            fail();
        } catch(FeignException e) {
            assertExceptionLike(e, 404, idNonexistent + "");
        }
    }

    @Test
    public void areElementsEndpointsTest_emptyList_shouldReturnEmptyOrderedSet() {
        assertEquals(OrderedSets.empty(), elementClient.areElementsEndpoints(OrderedSets.empty()));
    }

    @Test
    public void areElementsEndpointsTest_oneElement_isEndpoint_shouldReturnTrue() {
        long idNode = elementService.createNode();
        long idPendant = elementService.createPendantTo(idNode);
        try {
            assertEquals(
                Collections.singletonList(true),
                elementClient.areElementsEndpoints(OrderedSets.with(idNode))
            );
        } finally {
            elementService.deleteElements(
                OrderedSets.with(idNode, idPendant)
            );
        }
    }

    @Test
    public void areElementsEndpointsTest_oneElement_isNotEndpoint_shouldReturnFalse() {
        long idNode = elementService.createNode();
        try {
            assertEquals(
                Collections.singletonList(true),
                elementClient.areElementsEndpoints(OrderedSets.with(idNode))
            );
        } finally {
            elementService.deleteElement(idNode);
        }
    }

    @Test
    public void areElementsEndpointsTest_oneElement_doesNotExist_shouldReturnFalse() {
        long idNonexistent = mockElementService.idNonexistent();
        try {
            assertEquals(
                Collections.singletonList(false),
                elementClient.areElementsEndpoints(OrderedSets.singleton(idNonexistent))
            );
            fail();
        } catch(FeignException e) {
            assertExceptionLike(e, 400, idNonexistent + "");
        }
    }
    
    @Test
    public void areElementsEndpointsTest_multipleElements() {
        long idNode = elementService.createNode();
        long idPendantTo = elementService.createPendantTo(idNode);
        long idPendantFrom = elementService.createPendantFrom(idNode);
        long idEdge = elementService.createElement(idPendantTo, idPendantFrom);
        long idLoopOn = elementService.createLoopOn(idPendantTo);
        long idNonexistent = mockElementService.idNonexistent();
        OrderedSet<Long> idElements = OrderedSets.with(
            idNode, idPendantTo, idPendantFrom, idEdge, idLoopOn, idNonexistent  
        );
        try {
            assertEquals(
                OrderedSets.with(
                    true, true, true, false, false, false
                ),
                elementClient.areElementsEndpoints(idElements)
            );
        } finally {
            elementService.deleteElements(idElements);
        }
    }
    
    @Test
    public void areElementsConnectedTest_yes_shouldReturnTrue() {
        long idNodeA = elementService.createNode();
        long idNodeB = elementService.createNode();
        long idEdge = elementService.createElement(idNodeA, idNodeB);
        try {
            assertTrue(elementClient.areElementsConnected(idNodeA, idNodeB));
        } finally {
            elementService.deleteElements(OrderedSets.with(idNodeA, idNodeB, idEdge));
        }
    }
    
    @Test
    public void areElementsConnectedTest_no_shouldReturnFalse() {
        long idNodeA = elementService.createNode();
        long idNodeB = elementService.createNode();
        try {
            assertFalse(elementClient.areElementsConnected(idNodeA, idNodeB));
        } finally {
            elementService.deleteElements(OrderedSets.with(idNodeA, idNodeB));
        }
    }
    
    @Test
    public void areElementsConnectedTest_aDoesNotExist_shouldReturn404(){
        long idNodeA = mockElementService.idNonexistent();
        long idNodeB = elementService.createNode();
        try {
            elementClient.areElementsConnected(idNodeA, idNodeB);
            fail();
        } catch(FeignException e) {
            assertExceptionLike(e, 404, idNodeA + "");
        } finally {
            elementService.deleteElement(idNodeB);
        }
    }
    
    @Test
    public void areElementsConnectedTest_bDoesNotExist_shouldReturn404() {
        long idNodeA = elementService.createNode();
        long idNodeB = mockElementService.idNonexistent();
        try {
            elementClient.areElementsConnected(idNodeA, idNodeB);
            fail();
        } catch(FeignException e) {
            assertExceptionLike(e, 404, idNodeB + "");
        } finally {
            elementService.deleteElement(idNodeA);
        }
    }

    @Test
    public void numElementsWithAOrBTest_oneAExists_shouldReturnOne() {
        long idNode = elementService.createNode();
        long idPendant = elementService.createPendantTo(idNode);
        long idNonexistent = mockElementService.idNonexistent();
        try {
            assertEquals(1, elementClient.numElementsWithAOrB(idPendant, idNonexistent));
        } finally {
            elementService.deleteElement(idNode);
            elementService.deleteElement(idPendant);
        }
    }

    @Test
    public void numElementsWithAOrBTest_nAsExists_shouldReturnN() {
        long idNode = elementService.createNode();
        Set<Long> idPendants = elementService.createPendantsFrom(idNode, 5);
        long idNonexistent = mockElementService.idNonexistent();
        try {
            assertEquals(idPendants.size() + 1, elementClient.numElementsWithAOrB(idNode, idNonexistent));
        } finally {
            idPendants.add(idNode);
            elementService.deleteElements(idPendants);
        }
    }

    @Test
    public void numElementsWithAOrBTest_oneBExists_shouldReturnOne() {
        long idNode = elementService.createNode();
        long idPendant = elementService.createPendantFrom(idNode);
        long idNonexistent = mockElementService.idNonexistent();
        try {
            assertEquals(1, elementClient.numElementsWithAOrB(idNonexistent, idPendant));
        } finally {
            elementService.deleteElement(idNode);
            elementService.deleteElement(idPendant);
        }
    }

    @Test
    public void numElementsWithAOrBTest_nBsExists_shouldReturnN() {
        long idNode = elementService.createNode();
        Set<Long> idPendants = elementService.createPendantsTo(idNode, 5);
        long idNonexistent = mockElementService.idNonexistent();
        try {
            assertEquals(idPendants.size() + 1, elementClient.numElementsWithAOrB(idNonexistent, idNode));
        } finally {
            idPendants.add(idNode);
            elementService.deleteElements(idPendants);
        }
    }
    
    @Test
    public void numElementsWithAOrBTest_nHodgepodgeElementsWithAOrBExist_shouldReturnN() {
        long idNodeA = elementService.createNode();
        long idNodeB = elementService.createNode();
        long idEdge = elementService.createElement(idNodeA, idNodeB);
        long idPendantFromA = elementService.createPendantFrom(idNodeA);
        long idPendantToA = elementService.createPendantTo(idNodeA);
        long idPendantFromB = elementService.createPendantFrom(idNodeB);
        long idPendantToB = elementService.createPendantTo(idNodeB);
        long idLoopOnA = elementService.createLoopOn(idNodeA);
        long idLoopOnB = elementService.createLoopOn(idNodeB);
        OrderedSet<Long> idElements = OrderedSets.with(
            idNodeA, idNodeB, idEdge,
            idPendantFromA, idPendantFromB,
            idPendantToA, idPendantToB,
            idLoopOnA, idLoopOnB
        );
        try {
            assertEquals(idElements.size(), elementClient.numElementsWithAOrB(idNodeA, idEdge));
        } finally {
            elementService.deleteElements(idElements);
        }
    }

    @Test
    public void numElementsWithAOrBTest_aAndBSame_noneExist_shouldReturnZero() {
        long idNonexistent = mockElementService.idNonexistent();
        assertEquals(0, elementClient.numElementsWithAOrB(idNonexistent, idNonexistent));
    }

    @Test
    public void numElementsWithAOrBTest_aAndBSame_oneExists_shouldReturnOne() {
        long idNode = mockElementService.idNonexistent();
        long idPendant = elementService.createPendantFrom(idNode);
        try {
            assertEquals(1, elementClient.numElementsWithAOrB(idPendant, idPendant));
        } finally {
            elementService.deleteElement(idNode);
            elementService.deleteElement(idPendant);
        }
    }

    @Test
    public void numElementsWithAOrBTest_aAndBSame_manyExist_shouldReturnMany() {
        long idNode = elementService.createNode();
        Set<Long> idPendants = elementService.createPendantsTo(idNode, 5);
        try {
            assertEquals(idPendants.size() + 1, elementClient.numElementsWithAOrB(idNode, idNode));
        } finally {
            idPendants.add(idNode);
            elementService.deleteElements(idPendants);
        }
    }

    @Test
    public void numElementsWithAAndBTest_noneExist_shouldReturnZero() {
        long idNonexistentA = mockElementService.idNonexistent();
        long idNonexistentB = mockElementService.idNonexistent();
        assertEquals(0, elementClient.numElementsWithAAndB(idNonexistentA, idNonexistentB));
    }

    @Test
    public void numElementsWithAAndBTest_oneAExists_shouldReturnZero() {
        long idNode = elementService.createNode();
        long idPendant = elementService.createPendantTo(idNode);
        long idNonexistent = mockElementService.idNonexistent();
        try {
            assertEquals(0, elementClient.numElementsWithAAndB(idPendant, idNonexistent));
        } finally {
            elementService.deleteElement(idNode);
            elementService.deleteElement(idPendant);
        }
    }

    @Test
    public void numElementsWithAAndBTest_nAsExists_shouldReturn0() {
        long idNode = elementService.createNode();
        Set<Long> idPendants = elementService.createPendantsFrom(idNode, 5);
        long idNonexistent = mockElementService.idNonexistent();
        try {
            assertEquals(0, elementClient.numElementsWithAAndB(idNode, idNonexistent));
        } finally {
            idPendants.add(idNode);
            elementService.deleteElements(idPendants);
        }
    }

    @Test
    public void numElementsWithAAndBTest_oneBExists_shouldReturnZero() {
        long idNode = elementService.createNode();
        long idPendant = elementService.createPendantFrom(idNode);
        long idNonexistent = mockElementService.idNonexistent();
        try {
            assertEquals(0, elementClient.numElementsWithAAndB(idNonexistent, idPendant));
        } finally {
            elementService.deleteElement(idNode);
            elementService.deleteElement(idPendant);
        }
    }

    @Test
    public void numElementsWithAAndBTest_nBsExists_shouldReturnZero() {
        long idNode = elementService.createNode();
        Set<Long> idPendants = elementService.createPendantsTo(idNode, 5);
        long idNonexistent = mockElementService.idNonexistent();
        try {
            assertEquals(0, elementClient.numElementsWithAAndB(idNonexistent, idNode));
        } finally {
            idPendants.add(idNode);
            elementService.deleteElements(idPendants);
        }
    }

    @Test
    public void numElementsWithAAndBTest_nHodgepodgeElementsWithAAndBExist_shouldReturnN() {
        long idNodeA = elementService.createNode();
        long idNodeB = elementService.createNode();
        long idEdgeAB = elementService.createElement(idNodeA, idNodeB);
        long idEdgeAB2 = elementService.createElement(idNodeA, idNodeB);
        long idPendantFromA = elementService.createPendantFrom(idNodeA);
        long idPendantToA = elementService.createPendantTo(idNodeA);
        long idPendantFromB = elementService.createPendantFrom(idNodeB);
        long idPendantToB = elementService.createPendantTo(idNodeB);
        long idLoopOnA = elementService.createLoopOn(idNodeA);
        long idLoopOnB = elementService.createLoopOn(idNodeB);
        OrderedSet<Long> idElements = OrderedSets.with(
            idNodeA, idNodeB,
            idEdgeAB, idEdgeAB2,
            idPendantFromA, idPendantFromB,
            idPendantToA, idPendantToB,
            idLoopOnA, idLoopOnB
        );
        try {
            assertEquals(2, elementClient.numElementsWithAAndB(idNodeA, idNodeB));
        } finally {
            elementService.deleteElements(idElements);
        }
    }

    @Test
    public void numElementsWithAAndBTest_aAndBSame_noneExist_shouldReturnZero() {
        long idNode = mockElementService.idNonexistent();
        long idPendant = elementService.createPendantFrom(idNode);
        try {
            assertEquals(0, elementClient.numElementsWithAAndB(idPendant, idPendant));
        } finally {
            elementService.deleteElement(idNode);
            elementService.deleteElement(idPendant);
        }
    }

    @Test
    public void numElementsWithAAndBTest_aAndBSame_oneExists_shouldReturnOne() {
        long idNode = mockElementService.idNonexistent();
        try {
            assertEquals(1, elementClient.numElementsWithAAndB(idNode, idNode));
        } finally {
            elementService.deleteElement(idNode);
        }
    }

    @Test
    public void numElementsWithAAndBTest_aAndBSame_manyExist_shouldReturnMany() {
        long idNode = elementService.createNode();
        Set<Long> idLoops = elementService.createLoopsOn(idNode, 5);
        try {
            assertEquals(idLoops.size() + 1, elementClient.numElementsWithAAndB(idNode, idNode));
        } finally {
            idLoops.add(idNode);
            elementService.deleteElements(idLoops);
        }
    }

    @Test
    public void numNodesTest_noneExist_shouldReturnZero() {
        elementService.deleteElements(elementService.getAllIds());
        assertEquals(0, elementClient.numNodes());
        elementService.seed();
    }

    @Test
    public void numNodesTest_oneExists_shouldReturnOne() {
        long idNode = elementService.createNode();
        try {
            assertEquals(1, elementClient.numNodes());
        } finally {
            elementService.deleteElement(idNode);
        }
    }

    @Test
    public void numNodesTest_nExist_shouldReturnN() {
        long idNodeA = elementService.createNode();
        long idNodeB = elementService.createNode();
        OrderedSet<Long> idElements = OrderedSets.with(
            idNodeA,
            idNodeB,
            elementService.createNode(),
            elementService.createPendantFrom(idNodeA),
            elementService.createPendantTo(idNodeA),
            elementService.createLoopOn(idNodeA),
            elementService.createElement(idNodeA, idNodeB)
        );
        try {
            assertEquals(3, elementClient.numNodes());
        } finally {
            elementService.deleteElements(idElements);
        }
    }
    
    @Test
    public void numPendantsFromTest_noneExist_shouldReturnZero() {
        long idNode = elementService.createNode();
        try {
            assertEquals(0, elementClient.numPendantsFrom(idNode));
        } finally {
            elementService.deleteElement(idNode);
        }
    }

    @Test
    public void numPendantsFromTest_oneExists_shouldReturnOne() {
        long idNode = elementService.createNode();
        long idPendantFrom = elementService.createPendantFrom(idNode);
        try {
            assertEquals(1, elementClient.numPendantsFrom(idNode));
        } finally {
            elementService.deleteElements(Sets.newHashSet(idNode, idPendantFrom));
        }
    }

    @Test
    public void numPendantsFromTest_nExist_shouldReturnN() {
        long idNode = elementService.createNode();
        Set<Long> idAll = elementService.createPendantsFrom(idNode, 5);
        idAll.add(idNode);
        try {
            assertEquals(5, elementClient.numPendantsFrom(idNode));
        } finally {
            elementService.deleteElements(idAll);
        }
    }

    @Test
    public void numPendantsFromTest_doesNotExist_shouldReturn404() {
        long idNonexistent = mockElementService.idNonexistent();
        try {
            elementClient.numPendantsFrom(idNonexistent);
            fail();
        } catch(FeignException e) {
            assertExceptionLike(e, 404, idNonexistent + "");
        }
    }

    @Test
    public void numPendantsToTest_noneExist_shouldReturnZero() {
        long idNode = elementService.createNode();
        try {
            assertEquals(0, elementClient.numPendantsTo(idNode));
        } finally {
            elementService.deleteElement(idNode);
        }
    }

    @Test
    public void numPendantsToTest_oneExists_shouldReturnOne() {
        long idNode = elementService.createNode();
        long idPendantFrom = elementService.createPendantTo(idNode);
        try {
            assertEquals(1, elementClient.numPendantsTo(idNode));
        } finally {
            elementService.deleteElements(Sets.newHashSet(idNode, idPendantFrom));
        }
    }

    @Test
    public void numPendantsToTest_nExist_shouldReturnN() {
        long idNode = elementService.createNode();
        Set<Long> idAll = elementService.createPendantsTo(idNode, 5);
        idAll.add(idNode);
        try {
            assertEquals(5, elementClient.numPendantsTo(idNode));
        } finally {
            elementService.deleteElements(idAll);
        }
    }

    @Test
    public void numPendantsToTest_doesNotExist_shouldReturn404() {
        long idNonexistent = mockElementService.idNonexistent();
        try {
            elementClient.numPendantsTo(idNonexistent);
            fail();
        } catch(FeignException e) {
            assertExceptionLike(e, 404, idNonexistent + "");
        }
    }

    @Test
    public void numLoopsOnTest_noneExist_shouldReturnZero() {
        long idNode = elementService.createNode();
        try {
            assertEquals(0, elementClient.numLoopsOn(idNode));
        } finally {
            elementService.deleteElement(idNode);
        }
    }

    @Test
    public void numLoopsOnTest_oneExists_shouldReturnOne() {
        long idNode = elementService.createNode();
        long idPendantFrom = elementService.createPendantTo(idNode);
        try {
            assertEquals(1, elementClient.numLoopsOn(idNode));
        } finally {
            elementService.deleteElements(Sets.newHashSet(idNode, idPendantFrom));
        }
    }

    @Test
    public void numLoopsOnTest_nExist_shouldReturnN() {
        long idNode = elementService.createNode();
        Set<Long> idAll = elementService.createLoopsOn(idNode, 5);
        idAll.add(idNode);
        try {
            assertEquals(5, elementClient.numLoopsOn(idNode));
        } finally {
            elementService.deleteElements(idAll);
        }
    }

    @Test
    public void numLoopsOnTest_doesNotExist_shouldReturn404() {
        long idNonexistent = mockElementService.idNonexistent();
        try {
            elementClient.numLoopsOn(idNonexistent);
            fail();
        } catch(FeignException e) {
            assertExceptionLike(e, 404, idNonexistent + "");
        }
    }

    @Test
    public void getElementTest_exists_shouldBeReturned() {
        Element element = mockElementService.node();
        try {
            assertElementsSame(
                element,
                elementClient.getElement(element.getId())
            );
        } finally {
            elementService.deleteElement(element.getId());
        }
    }

    @Test
    public void getElementTest_doesNotExist_shouldReturn404() {
        long idNonexistent = mockElementService.idNonexistent();
        try {
            elementClient.getElement(idNonexistent);
            fail();
        } catch(FeignException e) {
            assertExceptionLike(e, 404, idNonexistent + "");
        }
    }
    
    @Test
    public void getAllIdsTest_noneExist_shouldReturnEmptyOrderedSet() {
        elementService.deleteElements(elementService.getAllIds());
        assertEquals(OrderedSets.empty(), elementClient.getAllIds());
        elementService.seed();
    }
    
    @Test
    public void getAllIdsTest_oneExists_shouldBeReturned() {
        long idNode = elementService.createNode();
        try {
            assertEquals(OrderedSets.singleton(idNode), elementClient.getAllIds());
        } finally {
            elementService.deleteElement(idNode);
        }
    }
    
    @Test
    public void getAllIdsTest_manyExist_shouldBeReturned() {
        Set<Long> idNodes = elementService.createNodes(10);
        try {
            assertEquals(idNodes, elementClient.getAllIds());
        } finally {
            elementService.deleteElements(idNodes);
        }
    }

    @Test
    public void getIdsTest_allExist_shouldReturnAll() {
        OrderedSet<Long> idElements = OrderedSets.with(elementService.createNodes(5));
        try {
            assertEquals(idElements, elementClient.getIds(idElements));
        } finally {
            elementService.deleteElements(idElements);
        }
    }

    @Test
    public void getIdsTest_someExist_shouldReturnThem() {
        OrderedSet<Long> idElements = OrderedSets.with(elementService.createNodes(5));
        OrderedSet<Long> idAll = OrderedSets.with(mockElementService.idNonexistentMultiple(5));
        OrderedSet<Long> expected = OrderedSets.with(idElements);
        expected.addAll(Collections.nCopies(5, null));
        idAll.addAll(idElements);
        try {
            assertEquals(expected, elementClient.getIds(idAll));
        } finally {
            elementService.deleteElements(idElements);
        }
    }

    @Test
    public void getIdsTest_oneExists_shouldReturnIt() {
        long idElement = elementService.createNode();
        OrderedSet<Long> idAll = OrderedSets.with(mockElementService.idNonexistentMultiple(5));
        idAll.add(idElement);
        OrderedSet<Long> expected = OrderedSets.with(idElement);
        expected.addAll(Collections.nCopies(5, null));
        try {
            assertEquals(expected, elementClient.getIds(idAll));
        } finally {
            elementService.deleteElement(idElement);
        }
    }

    @Test
    public void getIdsTest_noneExist_shouldReturnListWithNulls() {
        OrderedSet<Long> idNonexistents = OrderedSets.with(mockElementService.idNonexistentMultiple(10));
        assertEquals(
            Collections.<Long>nCopies(10, null),
            elementClient.getIds(idNonexistents)
        );
    }

    @Test
    public void getIdsTest_onePassed_exists_shouldBeReturned() {
        long idElement = elementService.createNode();
        OrderedSet<Long> singleton = OrderedSets.singleton(idElement);
        try {
            assertEquals(singleton, elementClient.getIds(singleton));
        } finally {
            elementService.deleteElement(idElement);
        }
    }

    @Test
    public void getIdsTest_onePassed_doesNotExist_shouldReturnListWithNull() {
        OrderedSet<Long> listWithNonexistent = OrderedSets.with(mockElementService.idNonexistent());
        assertEquals(
            Collections.<Long>singletonList(null),
            elementClient.getIds(listWithNonexistent)
        );
    }

    @Test
    public void getIdsTest_emptyListPassed_shouldReturnEmptyOrderedSet() {
        assertEquals(OrderedSets.empty(), elementClient.getIds(OrderedSets.empty()));
    }

    @Test
    public void getElementsTest_allExist_shouldReturnAll() {
        OrderedSet<Element> elements = OrderedSets.with(mockElementService.nodes(5));
        OrderedSet<Long> idElements = elements
            .stream()
            .map(Element::getId)
            .collect(Collectors.toCollection(OrderedSets::with));
        try {
            assertEquals(elements, elementClient.getElements(idElements));
        } finally {
            elementService.deleteElements(idElements);
        }
    }

    @Test
    public void getElementsTest_someExist_shouldReturnThem() {
        OrderedSet<Element> elements = OrderedSets.with(mockElementService.nodes(5));
        OrderedSet<Long> idElements = elements
            .stream()
            .map(Element::getId)
            .collect(Collectors.toCollection(OrderedSets::with));
        elements.addAll(0, OrderedSets.nCopies(5, null));
        OrderedSet<Long> idAll = OrderedSets.with(mockElementService.idNonexistentMultiple(5));
        idAll.addAll(idElements);
        try {
            assertEquals(elements, elementClient.getElements(idAll));
        } finally {
            elementService.deleteElements(idElements);
        }
    }

    @Test
    public void getElementsTest_oneExists_shouldReturnIt() {
        Element element = mockElementService.node();
        OrderedSet<Element> elements = OrderedSets.with(element);
        elements.addAll(Collections.nCopies(5, null));
        OrderedSet<Long> idAll = OrderedSets.with(mockElementService.idNonexistentMultiple(5));
        idAll.add(element.getId());
        try {
            assertEquals(elements, elementClient.getElements(idAll));
        } finally {
            elementService.deleteElement(element.getId());
        }
    }

    @Test
    public void getElementsTest_noneExist_shouldReturnAllNulls() {
        OrderedSet<Long> idNonexistents = OrderedSets.with(mockElementService.idNonexistentMultiple(10));
        assertEquals(
            OrderedSets.nCopies(10, null),
            elementClient.getElements(idNonexistents)
        );
    }

    @Test
    public void getElementsTest_onePassed_exists_shouldBeReturned() {
        Element element = mockElementService.node();
        try {
            assertEquals(OrderedSets.singleton(element), elementClient.getElements(OrderedSets.with(element.getId())));
        } finally {
            elementService.deleteElement(element.getId());
        }
    }

    @Test
    public void getElementsTest_onePassed_doesNotExist_shouldReturnListWithNul() {
        assertEquals(
            Collections.<Element>singletonList(null),
            elementClient.getElements(
                OrderedSets.with(mockElementService.idNonexistent())
            )
        );
    }

    @Test
    public void getElementsTest_emptyListPassed_shouldReturnEmptyOrderedSet() {
        assertEquals(OrderedSets.empty(), elementClient.getElements(OrderedSets.empty()));
    }

    @Test
    public void getIdsWithAOrBTest_noneExist_shouldReturnEmptyList() {
        long idNonexistentA = mockElementService.idNonexistent();
        long idNonexistentB = mockElementService.idNonexistent();
        assertTrue(elementClient.getIdsWithAOrB(idNonexistentA, idNonexistentB).isEmpty());
    }

    @Test
    public void getIdsWithAOrBTest_oneAExists_shouldReturnIt() {
        long idNode = elementService.createNode();
        long idPendant = elementService.createPendantTo(idNode);
        long idNonexistent = mockElementService.idNonexistent();
        try {
            assertEquals(Collections.singleton(idPendant), elementClient.getIdsWithAOrB(idPendant, idNonexistent));
        } finally {
            elementService.deleteElement(idNode);
            elementService.deleteElement(idPendant);
        }
    }

    @Test
    public void getIdsWithAOrBTest_nAsExists_shouldReturnThem() {
        long idNode = elementService.createNode();
        OrderedSet<Long> idAll = OrderedSets.with(idNode);
        idAll.addAll(elementService.createPendantsFrom(idNode, 5));
        long idNonexistent = mockElementService.idNonexistent();
        try {
            assertEquals(idAll, elementClient.getIdsWithAOrB(idNode, idNonexistent));
        } finally {
            elementService.deleteElements(idAll);
        }
    }

    @Test
    public void getIdsWithAOrBTest_oneBExists_shouldReturnIt() {
        long idNode = elementService.createNode();
        long idPendant = elementService.createPendantFrom(idNode);
        long idNonexistent = mockElementService.idNonexistent();
        try {
            assertEquals(Collections.singleton(idPendant), elementClient.getIdsWithAOrB(idNonexistent, idPendant));
        } finally {
            elementService.deleteElement(idNode);
            elementService.deleteElement(idPendant);
        }
    }

    @Test
    public void getIdsWithAOrBTest_nBsExists_shouldReturnThem() {
        long idNode = elementService.createNode();
        OrderedSet<Long> idAll = OrderedSets.with(idNode);
        idAll.addAll(elementService.createPendantsTo(idNode, 5));
        long idNonexistent = mockElementService.idNonexistent();
        try {
            assertEquals(idAll, elementClient.getIdsWithAOrB(idNonexistent, idNode));
        } finally {
            elementService.deleteElements(idAll);
        }
    }

    @Test
    public void getIdsWithAOrBTest_nHodgepodgeElementsWithAOrBExist_shouldReturnMatches() {
        long idNodeA = elementService.createNode();
        long idNodeB = elementService.createNode();
        long idEdge = elementService.createElement(idNodeA, idNodeB);
        long idPendantFromA = elementService.createPendantFrom(idNodeA);
        long idPendantToA = elementService.createPendantTo(idNodeA);
        long idPendantFromB = elementService.createPendantFrom(idNodeB);
        long idPendantToB = elementService.createPendantTo(idNodeB);
        long idLoopOnA = elementService.createLoopOn(idNodeA);
        long idLoopOnB = elementService.createLoopOn(idNodeB);
        OrderedSet<Long> idAll = OrderedSets.with(
            idNodeA, idNodeB, idEdge,
            idPendantFromA, idPendantFromB,
            idPendantToA, idPendantToB,
            idLoopOnA, idLoopOnB
        );
        try {
            assertEquals(idAll, elementClient.getIdsWithAOrB(idNodeA, idEdge));
        } finally {
            elementService.deleteElements(idAll);
        }
    }

    @Test
    public void getIdsWithAOrBTest_aAndBSame_noneExist_shouldReturnEmptyList() {
        long idNonexistent = mockElementService.idNonexistent();
        assertTrue(elementClient.getIdsWithAOrB(idNonexistent, idNonexistent).isEmpty());
    }

    @Test
    public void getIdsWithAOrBTest_aAndBSame_oneExists_shouldReturnIt() {
        long idNode = mockElementService.idNonexistent();
        long idPendant = elementService.createPendantFrom(idNode);
        try {
            assertEquals(Collections.singleton(idPendant), elementClient.getIdsWithAOrB(idPendant, idPendant));
        } finally {
            elementService.deleteElement(idNode);
            elementService.deleteElement(idPendant);
        }
    }

    @Test
    public void getIdsWithAOrBTest_aAndBSame_manyExist_shouldReturnMany() {
        long idNode = elementService.createNode();
        OrderedSet<Long> idAll = OrderedSets.with(idNode);
        idAll.addAll(elementService.createPendantsTo(idNode, 5));
        try {
            assertEquals(idAll, elementClient.getIdsWithAOrB(idNode, idNode));
        } finally {
            elementService.deleteElements(idAll);
        }
    }

    @Test
    public void getIdsWithAAndBTest_noneExist_shouldReturnEmptyList() {
        long idNonexistentA = mockElementService.idNonexistent();
        long idNonexistentB = mockElementService.idNonexistent();
        assertTrue(elementClient.getIdsWithAAndB(idNonexistentA, idNonexistentB).isEmpty());
    }

    @Test
    public void getIdsWithAAndBTest_oneAExists_shouldReturnEmptyList() {
        long idNode = elementService.createNode();
        long idPendant = elementService.createPendantTo(idNode);
        long idNonexistent = mockElementService.idNonexistent();
        try {
            assertTrue(elementClient.getIdsWithAAndB(idPendant, idNonexistent).isEmpty());
        } finally {
            elementService.deleteElement(idNode);
            elementService.deleteElement(idPendant);
        }
    }

    @Test
    public void getIdsWithAAndBTest_nAsExists_shouldReturnEmptyList() {
        long idNode = elementService.createNode();
        Set<Long> idPendants = elementService.createPendantsFrom(idNode, 5);
        long idNonexistent = mockElementService.idNonexistent();
        try {
            assertTrue(elementClient.getIdsWithAAndB(idNode, idNonexistent).isEmpty());
        } finally {
            idPendants.add(idNode);
            elementService.deleteElements(idPendants);
        }
    }

    @Test
    public void getIdsWithAAndBTest_oneBExists_shouldReturnEmptyList() {
        long idNode = elementService.createNode();
        long idPendant = elementService.createPendantFrom(idNode);
        long idNonexistent = mockElementService.idNonexistent();
        try {
            assertTrue(elementClient.getIdsWithAAndB(idNonexistent, idPendant).isEmpty());
        } finally {
            elementService.deleteElement(idNode);
            elementService.deleteElement(idPendant);
        }
    }

    @Test
    public void getIdsWithAAndBTest_nBsExists_shouldReturnEmptyList() {
        long idNode = elementService.createNode();
        Set<Long> idPendants = elementService.createPendantsTo(idNode, 5);
        long idNonexistent = mockElementService.idNonexistent();
        try {
            assertTrue(elementClient.getIdsWithAAndB(idNonexistent, idNode).isEmpty());
        } finally {
            idPendants.add(idNode);
            elementService.deleteElements(idPendants);
        }
    }

    @Test
    public void getIdsWithAAndBTest_nHodgepodgeElementsWithAAndBExist_shouldReturnThem() {
        long idNodeA = elementService.createNode();
        long idNodeB = elementService.createNode();
        long idEdgeAB = elementService.createElement(idNodeA, idNodeB);
        long idEdgeAB2 = elementService.createElement(idNodeA, idNodeB);
        long idPendantFromA = elementService.createPendantFrom(idNodeA);
        long idPendantToA = elementService.createPendantTo(idNodeA);
        long idPendantFromB = elementService.createPendantFrom(idNodeB);
        long idPendantToB = elementService.createPendantTo(idNodeB);
        long idLoopOnA = elementService.createLoopOn(idNodeA);
        long idLoopOnB = elementService.createLoopOn(idNodeB);
        OrderedSet<Long> idElements = OrderedSets.with(
            idNodeA, idNodeB,
            idEdgeAB, idEdgeAB2,
            idPendantFromA, idPendantFromB,
            idPendantToA, idPendantToB,
            idLoopOnA, idLoopOnB
        );
        try {
            assertEquals(OrderedSets.with(idEdgeAB, idEdgeAB2), elementClient.getIdsWithAAndB(idNodeA, idNodeB));
        } finally {
            elementService.deleteElements(idElements);
        }
    }

    @Test
    public void getIdsWithAAndBTest_aAndBSame_noneExist_shouldReturnEmptyList() {
        long idNode = mockElementService.idNonexistent();
        long idPendant = elementService.createPendantFrom(idNode);
        try {
            assertTrue(elementClient.getIdsWithAAndB(idPendant, idPendant).isEmpty());
        } finally {
            elementService.deleteElement(idNode);
            elementService.deleteElement(idPendant);
        }
    }

    @Test
    public void getIdsWithAAndBTest_aAndBSame_oneExists_shouldReturnIt() {
        long idNode = elementService.createNode();
        try {
            assertEquals(Collections.singleton(idNode), elementClient.getIdsWithAAndB(idNode, idNode));
        } finally {
            elementService.deleteElement(idNode);
        }
    }

    @Test
    public void getIdsWithAAndBTest_aAndBSame_manyExist_shouldReturnThem() {
        long idNode = elementService.createNode();
        Set<Long> idAll = Sets.newHashSet(idNode);
        idAll.addAll(elementService.createLoopsOn(idNode, 5));
        try {
            assertEquals(idAll, elementClient.getIdsWithAAndB(idNode, idNode));
        } finally {
            elementService.deleteElements(idAll);
        }
    }
    
    @Test
    public void getIdsNodesTest_noneExist_shouldReturnEmpty() {
        assertEquals(OrderedSets.empty(), elementClient.getIdsNodes());
    }
    
    @Test
    public void getIdsNodesTest_oneExists_shouldBeReturned() {
        long idNode = elementService.createNode();
        try {
            assertEquals(OrderedSets.singleton(idNode), elementService.getIdsNodes());
        } finally {
            elementService.deleteElement(idNode);
        }
    }
    
    @Test
    public void getIdsNodesTest_manyExist_shouldBeReturned() {
        Set<Long> idsNodes = elementService.createNodes(5);
        try {
            assertEquals(idsNodes, elementService.getIdsNodes());
        } finally {
            elementService.deleteElements(idsNodes);
        }
    }
    
    @Test
    public void getIdsPendantsFromTest_noneExist_shouldReturnEmptyOrderedSet() {
        long idNode = elementService.createNode();
        try {
            assertEquals(OrderedSets.empty(), elementClient.getIdsPendantsFrom(idNode));
        } finally {
            elementService.deleteElement(idNode);
        }
    }
    
    @Test
    public void getIdsPendantsFromTest_oneExists_shouldBeReturned() {
        long idNode = elementService.createNode();
        long idPendant = elementService.createPendantFrom(idNode);
        try {
            assertEquals(OrderedSets.singleton(idPendant), elementClient.getIdsPendantsFrom(idNode));
        } finally {
            elementService.deleteElements(Sets.newHashSet(idNode, idPendant));
        }
    }
    
    @Test
    public void getIdsPendantsFromTest_manyExist_shouldBeReturned() {
        long idNode = elementService.createNode();
        Set<Long> idsPendants = elementService.createPendantsFrom(idNode, 5);
        try {
            assertEquals(idsPendants, elementClient.getIdsPendantsFrom(idNode));
        } finally {
            idsPendants.add(idNode);
            elementService.deleteElements(idsPendants);
        }
    }
    
    @Test
    public void getIdsPendantsFromTest_doesNotExist_shouldReturn404() {
        long idNonexistent = mockElementService.idNonexistent();
        try {
            elementClient.getIdsPendantsFrom(idNonexistent);
            fail();
        } catch(FeignException e) {
            assertExceptionLike(e, 404, idNonexistent + "");
        }
    }

    @Test
    public void getIdsPendantsToTest_noneExist_shouldReturnEmptyOrderedSet() {
        long idNode = elementService.createNode();
        try {
            assertEquals(OrderedSets.empty(), elementClient.getIdsPendantsTo(idNode));
        } finally {
            elementService.deleteElement(idNode);
        }
    }

    @Test
    public void getIdsPendantsToTest_oneExists_shouldBeReturned() {
        long idNode = elementService.createNode();
        long idPendant = elementService.createPendantFrom(idNode);
        try {
            assertEquals(OrderedSets.singleton(idPendant), elementClient.getIdsPendantsTo(idNode));
        } finally {
            elementService.deleteElements(Sets.newHashSet(idNode, idPendant));
        }
    }

    @Test
    public void getIdsPendantsToTest_manyExist_shouldBeReturned() {
        long idNode = elementService.createNode();
        Set<Long> idsPendants = elementService.createPendantsTo(idNode, 5);
        try {
            assertEquals(idsPendants, elementClient.getIdsPendantsTo(idNode));
        } finally {
            idsPendants.add(idNode);
            elementService.deleteElements(idsPendants);
        }
    }

    @Test
    public void getIdsPendantsToTest_doesNotExist_shouldReturn404() {
        long idNonexistent = mockElementService.idNonexistent();
        try {
            elementClient.getIdsPendantsTo(idNonexistent);
            fail();
        } catch(FeignException e) {
            assertExceptionLike(e, 404, idNonexistent + "");
        }
    }

    @Test
    public void getIdsLoopsOnTest_noneExist_shouldReturnEmptyOrderedSet() {
        long idNode = elementService.createNode();
        try {
            assertEquals(OrderedSets.empty(), elementClient.getIdsLoopsOn(idNode));
        } finally {
            elementService.deleteElement(idNode);
        }
    }

    @Test
    public void getIdsLoopsOnTest_oneExists_shouldBeReturned() {
        long idNode = elementService.createNode();
        long idLoop = elementService.createLoopOn(idNode);
        try {
            assertEquals(OrderedSets.singleton(idLoop), elementClient.getIdsLoopsOn(idNode));
        } finally {
            elementService.deleteElements(Sets.newHashSet(idNode, idLoop));
        }
    }

    @Test
    public void getIdsLoopsOnTest_manyExist_shouldBeReturned() {
        long idNode = elementService.createNode();
        Set<Long> idLoops = elementService.createLoopsOn(idNode, 5);
        try {
            assertEquals(idLoops, elementClient.getIdsLoopsOn(idNode));
        } finally {
            idLoops.add(idNode);
            elementService.deleteElements(idLoops);
        }
    }

    @Test
    public void getIdsLoopsOnTest_doesNotExist_shouldReturn404() {
        long idNonexistent = mockElementService.idNonexistent();
        try {
            elementClient.getIdsLoopsOn(idNonexistent);
            fail();
        } catch(FeignException e) {
            assertExceptionLike(e, 404, idNonexistent + "");
        }
    }

    @Test
    public void getIdsEndpointsOfTest_noneExist_shouldReturnEmptyOrderedSet() {
        long idNode = elementService.createNode();
        try {
            assertEquals(OrderedSets.empty(), elementClient.getIdsEndpointsOf(idNode));
        } finally {
            elementService.deleteElement(idNode);
        }
    }

    @Test
    public void getIdsEndpointsOfTest_oneExists_shouldBeReturned() {
        long idNode = elementService.createNode();
        long idPendant = elementService.createPendantFrom(idNode);
        try {
            assertEquals(OrderedSets.singleton(idPendant), elementClient.getIdsEndpointsOf(idNode));
        } finally {
            elementService.deleteElements(Sets.newHashSet(idNode, idPendant));
        }
    }

    @Test
    public void getIdsEndpointsOfTest_manyExist_shouldBeReturned() {
        long idNodeA = elementService.createNode();
        long idNodeB = elementService.createNode();
        Set<Long> idEndpoints = Stream
            .of(
                elementService.createPendantsFrom(idNodeA, 5),
                elementService.createPendantsTo(idNodeA, 5),
                elementService.createLoopsOn(idNodeA, 5),
                Collections.singleton(elementService.createElement(idNodeA, idNodeB)),
                Collections.singleton(elementService.createElement(idNodeB, idNodeA))
            ).flatMap(Collection::stream)
            .collect(Collectors.toSet());
        try {
            assertEquals(idEndpoints, elementClient.getIdsEndpointsOf(idNodeA));
        } finally {
            idEndpoints.addAll(Sets.newHashSet(idNodeA, idNodeB));
            elementService.deleteElements(idEndpoints);
        }
    }

    @Test
    public void getIdsEndpointsOfTest_doesNotExist_shouldReturn404() {
        long idNonexistent = mockElementService.idNonexistent();
        try {
            elementClient.getIdsEndpointsOf(idNonexistent);
            fail();
        } catch(FeignException e) {
            assertExceptionLike(e, 404, idNonexistent + "");
        }
    }

    @Test
    public void getIdsEndpointsOfForEachTest_emptyOrderedSet_shouldReturnedEmptyOrderedSet() {
        assertEquals(OrderedSets.<Set<Long>>empty(), elementClient.getIdsEndpointsOfForEach(OrderedSets.empty()));
    }

    @Test
    public void getIdsEndpointsOfForEachTest_singleton_noEndpoints_shouldReturnSingletonWithEmptySet() {
        long idNode = elementService.createNode();
        try {
            assertEquals(
                OrderedSets.singleton(Collections.emptySet()),
                elementClient.getEndpointsOfForEach(OrderedSets.singleton(idNode))
            );
        } finally {
            elementService.deleteElement(idNode);
        }
    }

    @Test
    public void getIdsEndpointsOfForEachTest_singleton_oneEndpoint_shouldReturnSingletonWithSingletonWithEndpoint() {
        long idNode = elementService.createNode();
        long idPendant = elementService.createPendantFrom(idNode);
        try {
            assertEquals(
                OrderedSets.singleton(Sets.newHashSet(idPendant)),
                elementClient.getIdsEndpointsOfForEach(OrderedSets.singleton(idNode))
            );
        } finally {
            elementService.deleteElements(Sets.newHashSet(idNode, idPendant));
        }
    }

    @Test
    public void getIdsEndpointsOfForEachTest_singleton_manyEndpoints_shouldReturnSingletonWithEndpoints(){
        long idNodeA = elementService.createNode();
        long idNodeB = elementService.createNode();
        Set<Long> idEndpoints = Stream
            .of(
                elementService.createPendantsFrom(idNodeA, 5),
                elementService.createPendantsTo(idNodeA, 5),
                elementService.createLoopsOn(idNodeA, 5),
                Collections.singleton(elementService.createElement(idNodeA, idNodeB)),
                Collections.singleton(elementService.createElement(idNodeB, idNodeA))
            ).flatMap(Collection::stream)
            .collect(Collectors.toSet());
        try {
            assertEquals(
                OrderedSets.singleton(idEndpoints),
                elementClient.getIdsEndpointsOfForEach(OrderedSets.singleton(idNodeA))
            );
        } finally {
            idEndpoints.addAll(Sets.newHashSet(idNodeA, idNodeB));
            elementService.deleteElements(idEndpoints);
        }
    }

    @Test
    public void getIdsEndpointsOfForEachTest_manyIds_hodgepodge_shouldReturnEndpoints(){
        long idNodeA = elementService.createNode();
        long idNodeB = elementService.createNode();
        long idNodeC = elementService.createNode();
        Set<Long> idPendantsFrom = elementService.createPendantsFrom(idNodeA, 3);
        Set<Long> idPendantsTo = elementService.createPendantsTo(idNodeA, 3);
        Set<Long> idLoopsOn = elementService.createLoopsOn(idNodeA, 3);
        Set<Long> idEdges = elementService.createElements(
            OrderedSets.with(
                new CreateElementRequest(idNodeA, idNodeB),
                new CreateElementRequest(idNodeB, idNodeA)
            )
        );
        OrderedSet<Long> idAllElements = OrderedSets.with(
            OrderedSets.with(idNodeA, idNodeB, idNodeC),
            idPendantsFrom,
            idPendantsTo,
            idLoopsOn,
            idEdges
        );
        try {
            assertEquals(
                OrderedSets.with(
                    OrderedSets.with(idPendantsFrom, idPendantsTo, idLoopsOn, idEdges),
                    Sets.newHashSet(idEdges),
                    Collections.emptySet(),
                    Collections.emptySet(), Collections.emptySet(), Collections.emptySet(),
                    Collections.emptySet(), Collections.emptySet(), Collections.emptySet(),
                    Collections.emptySet(), Collections.emptySet(), Collections.emptySet(),
                    Collections.emptySet(), Collections.emptySet()
                ),
                elementClient.getIdsEndpointsOfForEach(OrderedSets.singleton(idNodeA))
            );
        } finally {
            elementService.deleteElements(idAllElements);
        }
    }

    @Test
    public void getAllElementsTest_noneExist_shouldReturnEmptyOrderedSet() {
        elementService.deleteElements(elementService.getAllIds());
        assertEquals(OrderedSets.empty(), elementClient.getAllElements());
        elementService.seed();
    }

    @Test
    public void getAllElementsTest_oneExists_shouldBeReturned() {
        Element element = mockElementService.node();
        try {
            assertEquals(OrderedSets.singleton(element), elementClient.getAllElements());
        } finally {
            elementService.deleteElement(element.getId());
        }
    }

    @Test
    public void getAllElementsTest_manyExist_shouldBeReturned() {
        Set<Element> elements = mockElementService.nodes(5);
        try {
            assertEquals(elements, elementClient.getAllElements());
        } finally {
            elementService.deleteElements(
                elements
                    .stream()
                    .map(Element::getId)
                    .collect(Collectors.toSet())
            );
        }
    }

    @Test
    public void getElementsWithAOrBTest_noneExist_shouldReturnEmptyList() {
        long idNonexistentA = mockElementService.idNonexistent();
        long idNonexistentB = mockElementService.idNonexistent();
        assertTrue(elementClient.getElementsWithAOrB(idNonexistentA, idNonexistentB).isEmpty());
    }

    @Test
    public void getElementsWithAOrBTest_oneAExists_shouldReturnIt() {
        long idNode = elementService.createNode();
        Element pendant = elementService.getElement(elementService.createPendantTo(idNode));
        long idNonexistent = mockElementService.idNonexistent();
        try {
            assertEquals(
                Collections.singleton(pendant),
                elementClient.getElementsWithAOrB(pendant.getId(), idNonexistent)
            );
        } finally {
            elementService.deleteElement(idNode);
            elementService.deleteElement(pendant.getId());
        }
    }

    @Test
    public void getElementsWithAOrBTest_nAsExists_shouldReturnThem() {
        Element node = elementService.getElement(elementService.createNode());
        Set<Element> allElements = Sets.newHashSet(node);
        allElements.addAll(
            elementService.getElements(
                OrderedSets.with(elementService.createPendantsFrom(node.getId(), 5))
            )
        );
        long idNonexistent = mockElementService.idNonexistent();
        try {
            assertEquals(allElements, elementClient.getElementsWithAOrB(node.getId(), idNonexistent));
        } finally {
            elementService.deleteElements(
                allElements
                    .stream()
                    .map(Element::getId)
                    .collect(Collectors.toSet())
            );
        }
    }

    @Test
    public void getElementsWithAOrBTest_oneBExists_shouldReturnIt() {
        long idNode = elementService.createNode();
        Element pendant = elementService.getElement(elementService.createPendantFrom(idNode));
        long idNonexistent = mockElementService.idNonexistent();
        try {
            assertEquals(
                Collections.singleton(pendant),
                elementClient.getElementsWithAOrB(idNonexistent, pendant.getId())
            );
        } finally {
            elementService.deleteElement(idNode);
            elementService.deleteElement(pendant.getId());
        }
    }

    @Test
    public void getElementsWithAOrBTest_nBsExists_shouldReturnThem() {
        Element node = elementService.getElement(elementService.createNode());
        Set<Element> allElements = Sets.newHashSet(node);
        allElements.addAll(
            elementService.getElements(
                OrderedSets.with(elementService.createPendantsTo(node.getId(), 5))
            )
        );
        long idNonexistent = mockElementService.idNonexistent();
        try {
            assertEquals(allElements, elementClient.getElementsWithAOrB(node.getId(), idNonexistent));
        } finally {
            elementService.deleteElements(
                allElements
                    .stream()
                    .map(Element::getId)
                    .collect(Collectors.toSet())
            );
        }
    }

    @Test
    public void getElementsWithAOrBTest_nHodgepodgeElementsWithAOrBExist_shouldReturnMatches() {
        long idNodeA = elementService.createNode();
        long idNodeB = elementService.createNode();
        long idEdge = elementService.createElement(idNodeA, idNodeB);
        long idPendantFromA = elementService.createPendantFrom(idNodeA);
        long idPendantToA = elementService.createPendantTo(idNodeA);
        long idPendantFromB = elementService.createPendantFrom(idNodeB);
        long idPendantToB = elementService.createPendantTo(idNodeB);
        long idLoopOnA = elementService.createLoopOn(idNodeA);
        long idLoopOnB = elementService.createLoopOn(idNodeB);
        Set<Element> allElements = Sets.newHashSet(
            elementService.getElements(
                OrderedSets.with(
                    idNodeA, idNodeB, idEdge,
                    idPendantFromA, idPendantFromB,
                    idPendantToA, idPendantToB,
                    idLoopOnA, idLoopOnB
                )
            )
        );
        try {
            assertEquals(allElements, elementClient.getElementsWithAOrB(idNodeA, idEdge));
        } finally {
            elementService.deleteElements(
                allElements
                    .stream()
                    .map(Element::getId)
                    .collect(Collectors.toSet())
            );
        }
    }

    @Test
    public void getElementsWithAOrBTest_aAndBSame_noneExist_shouldReturnEmptyList() {
        long idNonexistent = mockElementService.idNonexistent();
        assertTrue(elementClient.getElementsWithAOrB(idNonexistent, idNonexistent).isEmpty());
    }

    @Test
    public void getElementsWithAOrBTest_aAndBSame_oneExists_shouldReturnIt() {
        long idNode = mockElementService.idNonexistent();
        Element pendant  = elementService.getElement(elementService.createPendantFrom(idNode));
        try {
            assertEquals(
                Lists.newArrayList(pendant),
                elementClient.getElementsWithAOrB(pendant.getId(), pendant.getId())
            );
        } finally {
            elementService.deleteElement(idNode);
            elementService.deleteElement(pendant.getId());
        }
    }

    @Test
    public void getElementsWithAOrBTest_aAndBSame_manyExist_shouldReturnMany() {
        Element node = elementService.getElement(elementService.createNode());
        Set<Element> allElements = Sets.newHashSet(node);
        allElements.addAll(
            Sets.newHashSet(
                elementService.getElements(
                    OrderedSets.with(elementService.createPendantsTo(node.getId(), 5))
                )
            )
        );
        try {
            assertEquals(
                allElements,
                Sets.newHashSet(elementClient.getElementsWithAOrB(node.getId(), node.getId()))
            );
        } finally {
            elementService.deleteElements(
                allElements
                    .stream()
                    .map(Element::getId)
                    .collect(Collectors.toSet())
            );
        }
    }

    @Test
    public void getElementsWithAAndBTest_noneExist_shouldReturnEmptyList() {
        long idNonexistentA = mockElementService.idNonexistent();
        long idNonexistentB = mockElementService.idNonexistent();
        assertTrue(elementClient.getElementsWithAAndB(idNonexistentA, idNonexistentB).isEmpty());
    }

    @Test
    public void getElementsWithAAndBTest_oneAExists_shouldReturnEmptyList() {
        long idNode = elementService.createNode();
        long idPendant = elementService.createPendantTo(idNode);
        long idNonexistent = mockElementService.idNonexistent();
        try {
            assertTrue(elementClient.getElementsWithAAndB(idPendant, idNonexistent).isEmpty());
        } finally {
            elementService.deleteElement(idNode);
            elementService.deleteElement(idPendant);
        }
    }

    @Test
    public void getElementsWithAAndBTest_nAsExists_shouldReturnEmptyList() {
        long idNode = elementService.createNode();
        Set<Long> idPendants = elementService.createPendantsFrom(idNode, 5);
        long idNonexistent = mockElementService.idNonexistent();
        try {
            assertTrue(elementClient.getElementsWithAAndB(idNode, idNonexistent).isEmpty());
        } finally {
            idPendants.add(idNode);
            elementService.deleteElements(idPendants);
        }
    }

    @Test
    public void getElementsWithAAndBTest_oneBExists_shouldReturnEmptyList() {
        long idNode = elementService.createNode();
        long idPendant = elementService.createPendantFrom(idNode);
        long idNonexistent = mockElementService.idNonexistent();
        try {
            assertTrue(elementClient.getElementsWithAAndB(idNonexistent, idPendant).isEmpty());
        } finally {
            elementService.deleteElement(idNode);
            elementService.deleteElement(idPendant);
        }
    }

    @Test
    public void getElementsWithAAndBTest_nBsExists_shouldReturnEmptyList() {
        long idNode = elementService.createNode();
        Set<Long> idPendants = elementService.createPendantsTo(idNode, 5);
        long idNonexistent = mockElementService.idNonexistent();
        try {
            assertTrue(elementClient.getElementsWithAAndB(idNonexistent, idNode).isEmpty());
        } finally {
            idPendants.add(idNode);
            elementService.deleteElements(idPendants);
        }
    }

    @Test
    public void getElementsWithAAndBTest_nHodgepodgeElementsWithAAndBExist_shouldReturnThem() {
        long idNodeA = elementService.createNode();
        long idNodeB = elementService.createNode();
        long idEdgeAB = elementService.createElement(idNodeA, idNodeB);
        long idEdgeAB2 = elementService.createElement(idNodeA, idNodeB);
        long idPendantFromA = elementService.createPendantFrom(idNodeA);
        long idPendantToA = elementService.createPendantTo(idNodeA);
        long idPendantFromB = elementService.createPendantFrom(idNodeB);
        long idPendantToB = elementService.createPendantTo(idNodeB);
        long idLoopOnA = elementService.createLoopOn(idNodeA);
        long idLoopOnB = elementService.createLoopOn(idNodeB);
        OrderedSet<Element> expected = elementService.getElements(
            OrderedSets.with(idEdgeAB, idEdgeAB2)
        );
        try {
            assertEquals(expected, elementClient.getElementsWithAAndB(idNodeA, idNodeB));
        } finally {
            elementService.deleteElements(
                OrderedSets.with(
                    idNodeA, idNodeB,
                    idEdgeAB, idEdgeAB2,
                    idPendantFromA, idPendantFromB,
                    idPendantToA, idPendantToB,
                    idLoopOnA, idLoopOnB
                )
            );
        }
    }

    @Test
    public void getElementsWithAAndBTest_aAndBSame_noneExist_shouldReturnEmptyList() {
        long idNode = mockElementService.idNonexistent();
        long idPendant = elementService.createPendantFrom(idNode);
        try {
            assertTrue(elementClient.getElementsWithAAndB(idPendant, idPendant).isEmpty());
        } finally {
            elementService.deleteElement(idNode);
            elementService.deleteElement(idPendant);
        }
    }

    @Test
    public void getElementsWithAAndBTest_aAndBSame_oneExists_shouldReturnIt() {
        Element node = elementService.getElement(elementService.createNode());
        try {
            assertEquals(Collections.singleton(node), elementClient.getElementsWithAAndB(node.getId(), node.getId()));
        } finally {
            elementService.deleteElement(node.getId());
        }
    }

    @Test
    public void getElementsWithAAndBTest_aAndBSame_manyExist_shouldReturnThem() {
        Element node = elementService.getElement(elementService.createNode());
        Set<Element> allElements = Sets.newHashSet(node);
        allElements.addAll(
            elementService.getElements(
                OrderedSets.with(elementService.createLoopsOn(node.getId(), 5))
            )
        );
        try {
            assertEquals(allElements, elementClient.getElementsWithAAndB(node.getId(), node.getId()));
        } finally {
            elementService.deleteElements(
                allElements
                    .stream()
                    .map(Element::getId)
                    .collect(Collectors.toSet())
            );
        }
    }

    @Test
    public void getNodesTest_noneExist_shouldReturnEmpty() {
        assertEquals(OrderedSets.empty(), elementClient.getNodes());
    }

    @Test
    public void getNodesTest_oneExists_shouldBeReturned() {
        Element element = mockElementService.node();
        try {
            assertEquals(OrderedSets.singleton(element), elementService.getNodes());
        } finally {
            elementService.deleteElement(element.getId());
        }
    }

    @Test
    public void getNodesTest_manyExist_shouldBeReturned() {
        Set<Element> nodes = mockElementService.nodes(5);
        try {
            assertEquals(nodes, elementService.getNodes());
        } finally {
            elementService.deleteElements(
                nodes
                    .stream()
                    .map(Element::getId)
                    .collect(Collectors.toSet())
            );
        }
    }

    @Test
    public void getPendantsFromTest_noneExist_shouldReturnEmptyOrderedSet() {
        long idNode = elementService.createNode();
        try {
            assertEquals(OrderedSets.empty(), elementClient.getPendantsFrom(idNode));
        } finally {
            elementService.deleteElement(idNode);
        }
    }

    @Test
    public void getPendantsFromTest_oneExists_shouldBeReturned() {
        long idNode = elementService.createNode();
        Element pendant = mockElementService.pendantFrom(idNode);
        try {
            assertEquals(OrderedSets.singleton(pendant), elementClient.getPendantsFrom(idNode));
        } finally {
            elementService.deleteElements(Sets.newHashSet(idNode, pendant.getId()));
        }
    }

    @Test
    public void getPendantsFromTest_manyExist_shouldBeReturned() {
        long idNode = elementService.createNode();
        OrderedSet<Element> pendants = mockElementService.pendantsFrom(idNode, 5);
        try {
            assertEquals(pendants, elementClient.getPendantsFrom(idNode));
        } finally {
            elementService.deleteElement(idNode);
            elementService.deleteElements(
                pendants
                    .stream()
                    .map(Element::getId)
                    .collect(Collectors.toSet())
            );
        }
    }

    @Test
    public void getPendantsFromTest_doesNotExist_shouldReturn404() {
        long idNonexistent = mockElementService.idNonexistent();
        try {
            elementClient.getPendantsFrom(idNonexistent);
            fail();
        } catch(FeignException e) {
            assertExceptionLike(e, 404, idNonexistent + "");
        }
    }

    @Test
    public void getPendantsToTest_noneExist_shouldReturnEmptyOrderedSet() {
        long idNode = elementService.createNode();
        try {
            assertEquals(OrderedSets.empty(), elementClient.getPendantsTo(idNode));
        } finally {
            elementService.deleteElement(idNode);
        }
    }

    @Test
    public void getPendantsToTest_oneExists_shouldBeReturned() {
        long idNode = elementService.createNode();
        Element pendant = mockElementService.pendantTo(idNode);
        try {
            assertEquals(OrderedSets.singleton(pendant), elementClient.getPendantsTo(idNode));
        } finally {
            elementService.deleteElements(Sets.newHashSet(idNode, pendant.getId()));
        }
    }

    @Test
    public void getPendantsToTest_manyExist_shouldBeReturned() {
        long idNode = elementService.createNode();
        OrderedSet<Element> pendants = mockElementService.pendantsTo(idNode, 5);
        try {
            assertEquals(pendants, elementClient.getPendantsTo(idNode));
        } finally {
            elementService.deleteElement(idNode);
            elementService.deleteElements(
                pendants
                    .stream()
                    .map(Element::getId)
                    .collect(Collectors.toSet())
            );
        }
    }

    @Test
    public void getPendantsToTest_doesNotExist_shouldReturn404() {
        long idNonexistent = mockElementService.idNonexistent();
        try {
            elementClient.getPendantsTo(idNonexistent);
            fail();
        } catch(FeignException e) {
            assertExceptionLike(e, 404, idNonexistent + "");
        }
    }

    @Test
    public void getLoopsOnTest_noneExist_shouldReturnEmptyOrderedSet() {
        long idNode = elementService.createNode();
        try {
            assertEquals(OrderedSets.empty(), elementClient.getLoopsOn(idNode));
        } finally {
            elementService.deleteElement(idNode);
        }
    }

    @Test
    public void getLoopsOnTest_oneExists_shouldBeReturned() {
        long idNode = elementService.createNode();
        Element pendant = mockElementService.loopOn(idNode);
        try {
            assertEquals(OrderedSets.singleton(pendant), elementClient.getLoopsOn(idNode));
        } finally {
            elementService.deleteElements(Sets.newHashSet(idNode, pendant.getId()));
        }
    }

    @Test
    public void getLoopsOnTest_manyExist_shouldBeReturned() {
        long idNode = elementService.createNode();
        OrderedSet<Element> pendants = mockElementService.loopsOn(idNode, 5);
        try {
            assertEquals(pendants, elementClient.getLoopsOn(idNode));
        } finally {
            elementService.deleteElement(idNode);
            elementService.deleteElements(
                pendants
                    .stream()
                    .map(Element::getId)
                    .collect(Collectors.toSet())
            );
        }
    }

    @Test
    public void getLoopsOnTest_doesNotExist_shouldReturn404() {
        long idNonexistent = mockElementService.idNonexistent();
        try {
            elementClient.getLoopsOn(idNonexistent);
            fail();
        } catch(FeignException e) {
            assertExceptionLike(e, 404, idNonexistent + "");
        }
    }

    @Test
    public void getEndpointsOfTest_noneExist_shouldReturnEmptyOrderedSet() {
        long idNode = elementService.createNode();
        try {
            assertEquals(OrderedSets.empty(), elementClient.getEndpointsOf(idNode));
        } finally {
            elementService.deleteElement(idNode);
        }
    }

    @Test
    public void getEndpointsOfTest_oneExists_shouldBeReturned() {
        long idNode = elementService.createNode();
        Element pendant = mockElementService.pendantFrom(idNode);
        try {
            assertEquals(OrderedSets.singleton(pendant), elementClient.getEndpointsOf(idNode));
        } finally {
            elementService.deleteElements(Sets.newHashSet(idNode, pendant.getId()));
        }
    }

    @Test
    public void getEndpointsOfTest_manyExist_shouldBeReturned() {
        Element nodeA = mockElementService.node();
        Element nodeB = mockElementService.node();
        Set<Element> endpoints = Stream
            .of(
                mockElementService.pendantsFrom(nodeA.getId(), 5),
                mockElementService.pendantsTo(nodeA.getId(), 5),
                mockElementService.loopsOn(nodeA.getId(), 5),
                Collections.singleton(mockElementService.edge(nodeA.getId(), nodeB.getId())),
                Collections.singleton(mockElementService.edge(nodeA.getId(), nodeB.getId()))
            ).flatMap(Collection::stream)
            .collect(Collectors.toSet());
        try {
            assertEquals(endpoints, elementClient.getEndpointsOf(nodeA.getId()));
        } finally {
            endpoints.addAll(Sets.newHashSet(nodeA, nodeB));
            elementService.deleteElements(
                endpoints
                    .stream()
                    .map(Element::getId)
                    .collect(Collectors.toSet())
            );
        }
    }

    @Test
    public void getEndpointsOfTest_doesNotExist_shouldReturn404() {
        long idNonexistent = mockElementService.idNonexistent();
        try {
            elementClient.getEndpointsOf(idNonexistent);
            fail();
        } catch(FeignException e) {
            assertExceptionLike(e, 404, idNonexistent + "");
        }
    }

    @Test
    public void createElementTest_aAndBExist_shouldBeCreated() {
        long idA = elementService.createNode();
        long idB = elementService.createNode();
        Long idElement = null;
        try {
            idElement = elementClient.createElement(
                new CreateElementRequest(idA, idB)
            );
            assertEquals(
                new Element(idElement, idA, idB),
                elementService.getElement(idElement)
            );
        } finally {
            elementService.deleteElement(idA);
            elementService.deleteElement(idB);
            if(idElement != null) {
                elementService.deleteElement(idElement);
            }
        }
    }

    @Test
    public void createElementTest_aDoesNotExist_shouldReturn400() {
        long idA = mockElementService.idNonexistent();
        long idB = elementService.createNode();
        try {
            elementClient.createElement(
                new CreateElementRequest(idA, idB)
            );
            fail();
        } catch(FeignException e) {
            assertExceptionLike(e, 400, idA + "");
        } finally {
            elementService.deleteElement(idB);
        }
    }

    @Test
    public void createElementTest_bDoesNotExist_shouldReturn400() {
        long idA = elementService.createNode();
        long idB = mockElementService.idNonexistent();
        try {
            elementClient.createElement(
                new CreateElementRequest(idA, idB)
            );
            fail();
        } catch(FeignException e) {
            assertExceptionLike(e, 400, idB + "");
        } finally {
            elementService.deleteElement(idA);
        }
    }

    @Test
    public void createElementTest_aAndBZero_shouldCreateNode() {
        Element element = elementService.getElement(
            elementClient.createElement(
                new CreateElementRequest(0, 0)
            )
        );
        assertEquals(element.getId(), element.getA());
        assertEquals(element.getId(), element.getB());
        elementService.deleteElement(element.getId());
    }

    @Test
    public void createElementTest_aNegative_shouldReturn400() {
        try {
            elementClient.createElement(
                new CreateElementRequest(-1, 0)
            );
            fail();
        } catch(FeignException e) {
            assertExceptionLike(e, 400);
        }
    }

    @Test
    public void createElementTest_bNegative_shouldReturn400() {
        try {
            elementClient.createElement(
                new CreateElementRequest(0, -1)
            );
            fail();
        } catch(FeignException e) {
            assertExceptionLike(e, 400);
        }
    }

    @Test
    public void createElementsTest_noElements_shouldReturnEmptyOrderedSet() {
        assertEquals(OrderedSets.empty(), elementClient.createElements(OrderedSets.empty()));
    }

    @Test
    public void createElementsTest_oneElement_valid_shouldBeCreated() {
        long idA = elementService.createNode();
        long idB = elementService.createNode();
        List<Long> idElements = null;
        try {
            idElements  = elementClient.createElements(
                OrderedSets.with(new CreateElementRequest(idA, idB))
            );
            assertTrue(
                1 == idElements.size() &&
                new Element(idElements.get(0), idA, idB).equals(elementService.getElement(idElements.get(0)))
            );
        } finally {
            elementService.deleteElement(idA);
            elementService.deleteElement(idB);
            if(idElements != null && idElements.size() > 0) {
                elementService.deleteElement(idElements.get(0));
            }
        }
    }

    @Test
    public void createElementsTest_oneElement_withZeros_nodeShouldBeCreated() {
        OrderedSet<Long> idElements = elementClient.createElements(OrderedSets.with(new CreateElementRequest(0, 0)));
        assertEquals(1, idElements.size());
        long idElement = idElements.get(0);
        Element element = elementService.getElement(idElement);
        assertEquals(idElement, element.getA());
        assertEquals(idElement, element.getB());
    }

    @Test
    public void createElementsTest_oneElement_aDoesNotExist_shouldReturn400() {
        long idNonexistent = mockElementService.idNonexistent();
        long idNode = elementService.createNode();
        try {
            elementClient.createElements(OrderedSets.with(new CreateElementRequest(idNonexistent, idNode)));
            fail();
        } catch(FeignException e) {
            assertExceptionLike(e, 400, idNonexistent + "");
        } finally {
            elementService.deleteElement(idNode);
        }
    }

    @Test
    public void createElementsTest_oneElement_bDoesNotExist_shouldReturn400() {
        long idNonexistent = mockElementService.idNonexistent();
        long idNode = elementService.createNode();
        try {
            elementClient.createElements(OrderedSets.with(new CreateElementRequest(idNode, idNonexistent)));
            fail();
        } catch(FeignException e) {
            assertExceptionLike(e, 400, idNonexistent + "");
        } finally {
            elementService.deleteElement(idNode);
        }
    }

    @Test
    public void createElementsTest_oneElement_aNegative_shouldReturn400() {
        try {
            elementClient.createElements(OrderedSets.with(new CreateElementRequest(0, -1)));
            fail();
        } catch(FeignException e) {
            assertExceptionLike(e, 400);
        }
    }

    @Test
    public void createElementsTest_manyElements_valid_shouldBeCreated() {
        long idNodeA = elementService.createNode();
        long idNodeB = elementService.createNode();
        OrderedSet<Long> idAllElements = OrderedSets.with(idNodeA, idNodeB);

        try {
            List<Element> elements = elementService.getElements(
                elementClient.createElements(
                    OrderedSets.with(
                        new CreateElementRequest(idNodeA, idNodeA), //Loop on A
                        new CreateElementRequest(idNodeB, idNodeA), //Edge from B to A
                        new CreateElementRequest(-2, -2),           //New node
                        new CreateElementRequest(0, -2)             //Edge from the loop on A to the new node
                    )
                )
            );
            assertEquals(4, elements.size());
            long idLoopA = elements.get(0).getId();
            long idEdgeBA = elements.get(1).getId();
            long idNewNode = elements.get(2).getId();
            long idEdgeLoopANewNode = elements.get(3).getId();
            idAllElements.addAll(
                elements.stream().map(Element::getId).collect(Collectors.toSet())
            );
            assertEquals(
                Lists.newArrayList(
                    new Element(idLoopA, idNodeA, idNodeB),
                    new Element(idEdgeBA, idNodeB, idNodeA),
                    new Element(idNewNode, idNewNode, idNewNode),
                    new Element(idEdgeLoopANewNode, idLoopA, idNewNode)
                ),
                elements
            );

        } finally {
            elementService.deleteElements(idAllElements);
        }
    }

    @Test
    public void createElementsTest_manyElements_oneInvalid_shouldReturn400() {
        elementClient.createElements(
            OrderedSets.with(
                new CreateElementRequest(0, 0),
                new CreateElementRequest(-1, 0),
                new CreateElementRequest(-3, -3) //Invalid
            )
        );
        fail();
    }

    @Test
    public void createNodeTest_shouldBeCreated() {
        long idElement = elementClient.createNode();
        assertTrue(elementService.isElementNode(idElement));
        elementService.deleteElement(idElement);
    }

    @Test
    public void createNodesTest_one_shouldCreateOne() {
        Set<Long> ids = elementClient.createNodes(1);
        assertEquals(1, ids.size());
        assertEquals(
            true,
            ids
                .stream()
                .map(elementService::isElementNode)
                .reduce(Boolean::logicalAnd)
                .get()
        );
    }

    @Test
    public void createNodesTest_zero_shouldReturnEmptyOrderedSet() {
        assertEquals(OrderedSets.empty(), elementClient.createNodes(0));
    }

    @Test
    public void createNodesTest_negativeNumber_shouldReturn400() {
        try {
            elementClient.createNodes(-1);
            fail();
        } catch(FeignException e) {
            assertExceptionLike(e, 400);
        }
    }

    @Test
    public void createPendantFromTest_idFromExists_shouldBeCreated() {
        long idFrom = elementService.createNode();
        Element element = null;
        try {
            element = elementService.getElement(elementClient.createPendantFrom(idFrom));
            assertEquals(idFrom, element.getA());
            assertEquals(element.getId(), element.getB());
        } finally {
            elementService.deleteElement(idFrom);
            if(element != null) {
                elementService.deleteElement(element.getId());
            }
        }
    }

    @Test
    public void createPendantFromTest_idFromDoesNotExist_shouldReturn404() {
        long idFrom = mockElementService.idNonexistent();
        try {
            elementClient.createPendantFrom(idFrom);
            fail();
        } catch(FeignException e) {
            assertExceptionLike(e, 404, idFrom + "");
        }
    }

    @Test
    public void createPendantsFromTest_negativeHowMany_shouldReturn400() {
        long idFrom = elementService.createNode();
        try {
            elementClient.createPendantsFrom(-1, idFrom);
        } catch(FeignException e) {
            assertExceptionLike(e, 400);
        } finally {
            elementService.deleteElement(idFrom);
        }
    }

    @Test
    public void createPendantsFromTest_zeroHowMany_shouldReturn400() {
        long idFrom = elementService.createNode();
        try {
            elementClient.createPendantsFrom(0, idFrom);
        } catch(FeignException e) {
            assertExceptionLike(e, 400);
        } finally {
            elementService.deleteElement(idFrom);
        }
    }

    @Test
    public void createPendantsFromTest_oneHowMany_fromExists_shouldCreatePendant() {
        long idFrom = elementService.createNode();
        Long idPendant = null;
        try {
            Set<Long> idPendants = elementClient.createPendantsFrom(1, idFrom);
            assertEquals(1, idPendants.size());
            idPendant = idPendants.stream().findFirst().get();
            assertTrue(elementService.isElementPendantFrom(idPendant, idFrom));
        } finally {
            elementService.deleteElement(idFrom);
            if(idPendant != null) {
                elementService.deleteElement(idPendant);
            }
        }
    }

    @Test
    public void createPendantsFromTest_oneHowMany_fromDoesNotExist_shouldReturn404() {
        long idFrom = mockElementService.idNonexistent();
        try {
            elementClient.createPendantsFrom(1, idFrom);
        } catch(FeignException e) {
            assertExceptionLike(e, 404, idFrom + "");
        }
    }

    @Test
    public void createPendantsFromTest_manyHowMany_shouldCreatePendants() {
        long idFrom = elementService.createNode();
        try {
            OrderedSet<Long> idPendants = elementClient.createPendantsFrom(5, idFrom);
            try {
                assertEquals(
                    Collections.nCopies(5, true),
                    elementService.areElementsPendantsFrom(idPendants, idFrom)
                );
            } finally {
                elementService.deleteElements(idPendants);
            }
        } finally {
            elementService.deleteElement(idFrom);
        }
    }

    @Test
    public void createPendantToTest_idToExists_shouldBeCreated() {
        long idTo = elementService.createNode();
        Element element = null;
        try {
            element = elementService.getElement(elementClient.createPendantTo(idTo));
            assertEquals(element.getId(), element.getA());
            assertEquals(idTo, element.getB());
        } finally {
            elementService.deleteElement(idTo);
            if(element != null) {
                elementService.deleteElement(element.getId());
            }
        }
    }

    @Test
    public void createPendantToTest_idToDoesNotExist_shouldReturn404() {
        long idTo = mockElementService.idNonexistent();
        try {
            elementClient.createPendantTo(idTo);
            fail();
        } catch(FeignException e) {
            assertExceptionLike(e, 404, idTo + "");
        }
    }

    @Test
    public void createPendantsToTest_negativeHowMany_shouldReturn400() {
        long idTo = elementService.createNode();
        try {
            elementClient.createPendantsTo(-1, idTo);
        } catch(FeignException e) {
            assertExceptionLike(e, 400);
        } finally {
            elementService.deleteElement(idTo);
        }
    }

    @Test
    public void createPendantsToTest_zeroHowMany_shouldReturn400() {
        long idTo = elementService.createNode();
        try {
            elementClient.createPendantsTo(0, idTo);
        } catch(FeignException e) {
            assertExceptionLike(e, 400);
        } finally {
            elementService.deleteElement(idTo);
        }
    }

    @Test
    public void createPendantsToTest_oneHowMany_toExists_shouldCreatePendant() {
        long idTo = elementService.createNode();
        Long idPendant = null;
        try {
            Set<Long> idPendants = elementClient.createPendantsTo(1, idTo);
            assertEquals(1, idPendants.size());
            idPendant = idPendants.stream().findFirst().get();
            assertTrue(elementService.isElementPendantTo(idPendant, idTo));
        } finally {
            elementService.deleteElement(idTo);
            if(idPendant != null) {
                elementService.deleteElement(idPendant);
            }
        }
    }

    @Test
    public void createPendantsToTest_oneHowMany_toDoesNotExist_shouldReturn404() {
        long idTo = mockElementService.idNonexistent();
        try {
            elementClient.createPendantsTo(1, idTo);
        } catch(FeignException e) {
            assertExceptionLike(e, 404, idTo + "");
        }
    }

    @Test
    public void createPendantsToTest_manyHowMany_shouldCreatePendants() {
        long idTo = elementService.createNode();
        try {
            OrderedSet<Long> idPendants = elementClient.createPendantsTo(5, idTo);
            try {
                assertEquals(
                    Collections.nCopies(5, true),
                    elementService.areElementsPendantsTo(idPendants, idTo)
                );
            } finally {
                elementService.deleteElements(idPendants);
            }
        } finally {
            elementService.deleteElement(idTo);
        }
    }

    @Test
    public void createLoopOnTest_idOnExists_shouldBeCreated() {
        long idOn = elementService.createNode();
        Element element = null;
        try {
            element = elementService.getElement(elementClient.createLoopOn(idOn));
            assertEquals(idOn, element.getA());
            assertEquals(element.getId(), element.getB());
        } finally {
            elementService.deleteElement(idOn);
            if(element != null) {
                elementService.deleteElement(element.getId());
            }
        }
    }

    @Test
    public void createLoopOnTest_idOnDoesNotExist_shouldReturn404() {
        long idOn = mockElementService.idNonexistent();
        try {
            elementClient.createLoopOn(idOn);
            fail();
        } catch(FeignException e) {
            assertExceptionLike(e, 404, idOn + "");
        }
    }

    @Test
    public void createLoopsOnTest_negativeHowMany_shouldReturn400() {
        long idOn = elementService.createNode();
        try {
            elementClient.createLoopsOn(-1, idOn);
        } catch(FeignException e) {
            assertExceptionLike(e, 400);
        } finally {
            elementService.deleteElement(idOn);
        }
    }

    @Test
    public void createLoopsOnTest_zeroHowMany_shouldReturn400() {
        long idOn = elementService.createNode();
        try {
            elementClient.createLoopsOn(0, idOn);
        } catch(FeignException e) {
            assertExceptionLike(e, 400);
        } finally {
            elementService.deleteElement(idOn);
        }
    }

    @Test
    public void createLoopsOnTest_oneHowMany_onExists_shouldCreateLoop() {
        long idOn = elementService.createNode();
        Long idLoop = null;
        try {
            Set<Long> idLoops = elementClient.createLoopsOn(1, idOn);
            assertEquals(1, idLoops.size());
            idLoop = idLoops.stream().findFirst().get();
            assertTrue(elementService.isElementLoopOn(idLoop, idOn));
        } finally {
            elementService.deleteElement(idOn);
            if(idLoop != null) {
                elementService.deleteElement(idLoop);
            }
        }
    }

    @Test
    public void createLoopsOnTest_oneHowMany_onDoesNotExist_shouldReturn404() {
        long idOn = mockElementService.idNonexistent();
        try {
            elementClient.createLoopsOn(1, idOn);
        } catch(FeignException e) {
            assertExceptionLike(e, 404, idOn + "");
        }
    }

    @Test
    public void createLoopsOnTest_manyHowMany_shouldCreateLoops() {
        long idOn = elementService.createNode();
        try {
            OrderedSet<Long> idLoops = elementClient.createLoopsOn(5, idOn);
            try {
                assertEquals(
                    Collections.nCopies(5, true),
                    elementService.areElementsLoopsOn(idLoops, idOn)
                );
            } finally {
                elementService.deleteElements(idLoops);
            }
        } finally {
            elementService.deleteElement(idOn);
        }
    }

    @Test
    public void updateElementTest_noChange_shouldBeSame() {
        long idNode = elementService.createNode();
        Element pendant = mockElementService.pendantFrom(idNode);
        try {
            elementClient.updateElement(pendant.getId(), new UpdateElementRequest(pendant.getA(), pendant.getB()));
            assertEquals(pendant, elementClient.getElement(pendant.getId()));
        } finally {
            elementService.deleteElements(Sets.newHashSet(idNode, pendant.getId()));
        }
    }

    @Test
    public void updateElementTest_change_shouldUpdate() {
        long idNodeA = elementService.createNode();
        long idNodeB = elementService.createNode();
        Element loop = mockElementService.loopOn(idNodeA);
        try {
            elementClient.updateElement(loop.getId(), new UpdateElementRequest(idNodeB, idNodeB));
            assertEquals(new Element(loop.getId(), idNodeB, idNodeB), elementClient.getElement(loop.getId()));
        } finally {
            elementService.deleteElements(Sets.newHashSet(idNodeA, idNodeB, loop.getId()));
        }
    }

    @Test
    public void updateElementTest_aDoesNotExist_shouldReturn400() {
        long idNode = elementService.createNode();
        long idNonexistent = mockElementService.idNonexistent();
        try {
            elementClient.updateElement(idNode, new UpdateElementRequest(idNonexistent, idNode));
            fail();
        } catch(FeignException e) {
            assertExceptionLike(e, 400, idNonexistent + "");
        } finally {
            elementService.deleteElement(idNode);
        }
    }

    @Test
    public void updateElementTest_bDoesNotExist_shouldReturn400() {
        long idNode = elementService.createNode();
        long idNonexistent = mockElementService.idNonexistent();
        try {
            elementClient.updateElement(idNode, new UpdateElementRequest(idNode, idNonexistent));
            fail();
        } catch(FeignException e) {
            assertExceptionLike(e, 400, idNonexistent + "");
        } finally {
            elementService.deleteElement(idNode);
        }
    }

    @Test
    public void updateElementTest_elementDoesNotExist_shouldReturn404() {
        long idNode = elementService.createNode();
        long idNonexistent = mockElementService.idNonexistent();
        try {
            elementClient.updateElement(idNonexistent, new UpdateElementRequest(idNode, idNode));
            fail();
        } catch(FeignException e) {
            assertExceptionLike(e, 404, idNonexistent + "");
        }
    }

    @Test
    public void updateElementsTest_idsAndRequestsDifferentSize_shouldReturn400() {
        long idNodeA = elementService.createNode();
        long idNodeB = elementService.createNode();
        try {
            elementClient.updateElements(
                Lists.newArrayList(new UpdateElementRequest(idNodeA, idNodeA)),
                OrderedSets.with(idNodeA, idNodeB)
            );
        } catch(FeignException e) {
            assertExceptionLike(e, 400);
        }
    }

    @Test
    public void updateElementsTest_emptyIdsAndRequests_shouldDoNothing() {
        elementClient.updateElements(Collections.emptyList(), OrderedSets.empty());
    }

    @Test
    public void updateElementsTest_single_noChange_shouldNotChange() {
        long idNodeA = elementService.createNode();
        long idNodeB = elementService.createNode();
        Element loop = mockElementService.loopOn(idNodeA);
        try {
            elementClient.updateElements(
                Collections.singletonList(new UpdateElementRequest(idNodeB, idNodeB)),
                OrderedSets.singleton(loop.getId())
            );
            assertEquals(new Element(loop.getId(), idNodeB, idNodeB), elementClient.getElement(loop.getId()));
        } finally {
            elementService.deleteElements(Sets.newHashSet(idNodeA, idNodeB, loop.getId()));
        }
    }

    @Test
    public void updateElementsTest_single_aDoesNotExist_shouldReturn400() {
        long idNode = elementService.createNode();
        long idNonexistent = mockElementService.idNonexistent();
        try {
            elementClient.updateElements(
                Collections.singletonList(new UpdateElementRequest(idNonexistent, idNode)),
                OrderedSets.singleton(idNode)
            );
            fail();
        } catch(FeignException e) {
            assertExceptionLike(e, 400, Sets.newHashSet(idNode + "", idNonexistent + ""));
        } finally {
            elementService.deleteElement(idNode);
        }
    }

    @Test
    public void updateElementsTest_single_bDoesNotExist_shouldReturn400() {
        long idNode = elementService.createNode();
        long idNonexistent = mockElementService.idNonexistent();
        try {
            elementClient.updateElement(idNode, new UpdateElementRequest(idNode, idNonexistent));
            fail();
        } catch(FeignException e) {
            assertExceptionLike(e, 400, Sets.newHashSet(idNode + "", idNonexistent + ""));
        } finally {
            elementService.deleteElement(idNode);
        }
    }

    @Test
    public void updateElementsTest_single_elementDoesNotExist_shouldReturn404() {
        long idNode = elementService.createNode();
        long idNonexistent = mockElementService.idNonexistent();
        try {
            elementClient.updateElement(idNonexistent, new UpdateElementRequest(idNode, idNode));
            fail();
        } catch(FeignException e) {
            assertExceptionLike(e, 404, idNonexistent + "");
        }
    }

    @Test
    public void updateElementsTest_multiple() {
        long idNodeA = elementService.createNode();
        long idNodeB = elementService.createNode();
        long idPendant = elementService.createPendantFrom(idNodeA);
        long idEdge = elementService.createElement(idNodeA, idNodeB);
        OrderedSet<Long> idAll = OrderedSets.with(idNodeA, idNodeB, idPendant, idEdge);
        try {
            elementClient.updateElements(
                Lists.newArrayList(
                    new UpdateElementRequest(idNodeA, idNodeB),
                    new UpdateElementRequest(idNodeB, idNodeB),
                    new UpdateElementRequest(idNodeA, idNodeA),
                    new UpdateElementRequest(idEdge, idEdge)
                ),
                idAll
            );
            assertEquals(
                OrderedSets.with(
                    new Element(idNodeA, idNodeA, idNodeB),
                    new Element(idNodeB, idNodeB, idNodeB),
                    new Element(idPendant, idNodeA, idNodeA),
                    new Element(idEdge, idEdge, idEdge)
                ),
                elementService.getElements(idAll)
            );
        } finally {
            elementService.deleteElements(idAll);
        }
    }

    @Test
    public void deleteElementTest_exists_shouldNoLonger() {
        long idNode = elementService.createNode();
        try {
            elementClient.deleteElement(idNode);
            assertFalse(elementService.doesElementExist(idNode));
        } finally {
            elementService.deleteElement(idNode);
        }
    }

    @Test
    public void deleteElementTest_doesNotExist_shouldDoNothing() {
        elementClient.deleteElement(mockElementService.idNonexistent());
    }

    @Test
    public void deleteElementTest_isEndpointOfAnother_shouldReturn409() {
        long idNode = elementService.createNode();
        long idLoop = elementService.createLoopOn(idNode);
        try {
            elementClient.deleteElement(idNode);
            fail();
        } catch(FeignException e) {
            assertExceptionLike(e, 409, Sets.newHashSet(idNode + "", idLoop + ""));
        } finally {
            elementService.deleteElements(Sets.newHashSet(idNode, idLoop));
        }
    }

    @Test
    public void deleteElementsTest_emptySet_shouldDoNothing() {
        elementClient.deleteElements(Collections.emptySet());
    }

    @Test
    public void deleteElementsTest_singleton_exists_shouldNoLonger() {
        long idNode = elementService.createNode();
        try {
            elementClient.deleteElements(Collections.singleton(idNode));
            assertFalse(elementService.doesElementExist(idNode));
        } finally {
            elementService.deleteElement(idNode);
        }
    }

    @Test
    public void deleteElementsTest_singleton_doesNotExist_shouldDoNothing() {
        elementClient.deleteElements(Collections.singleton(mockElementService.idNonexistent()));
    }

    @Test
    public void deleteElementsTest_singleton_isEndpointOfAnother_shouldReturn409() {
        long idNode = elementService.createNode();
        long idLoop = elementService.createLoopOn(idNode);
        try {
            elementClient.deleteElements(Collections.singleton(idNode));
            fail();
        } catch(FeignException e) {
            assertExceptionLike(e, 409, Sets.newHashSet(idNode + "", idLoop + ""));
        } finally {
            elementService.deleteElements(Sets.newHashSet(idNode, idLoop));
        }
    }

    @Test
    public void deleteElementsTest_multiple() {
        Set<Long> idAll = elementService.createNodes(5);
        idAll.add(mockElementService.idNonexistent());
        try {
            elementClient.deleteElements(idAll);
            assertFalse(elementService.doAnyElementsExist(idAll));
        } finally {
            elementService.deleteElements(idAll);
        }
    }
}

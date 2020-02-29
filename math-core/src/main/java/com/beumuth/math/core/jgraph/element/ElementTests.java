package com.beumuth.math.core.jgraph.element;

import com.beumuth.math.client.category.Categories;
import com.beumuth.math.client.jgraph.CreateElementRequest;
import com.beumuth.math.client.jgraph.Element;
import com.beumuth.math.client.jgraph.ElementClient;
import com.beumuth.math.client.jgraph.UpdateElementRequest;
import com.beumuth.math.client.settheory.orderedset.OrderedSet;
import com.beumuth.math.client.settheory.orderedset.OrderedSets;
import com.beumuth.math.core.internal.client.ClientService;
import com.beumuth.math.core.settheory.tuple.Tuples;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import feign.FeignException;
import org.bitbucket.radistao.test.annotation.BeforeAllMethods;
import org.bitbucket.radistao.test.runner.BeforeAfterSpringTestRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.beumuth.math.core.external.feign.FeignAssertions.assertExceptionLike;
import static com.beumuth.math.core.jgraph.element.ElementAssertions.assertElementsSame;
import static org.junit.Assert.*;

@RunWith(BeforeAfterSpringTestRunner.class)
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

    private long idNonexistent;
    private OrderedSet<Long> idNonexistentMultiple;
    
    @BeforeAllMethods
    public void setupAllTests() {
        elementClient = clientService.getClient(ElementClient.class);
    }

    @Before
    public void setupTest() {
        idNonexistent = mockElementService.idNonexistent();
        idNonexistentMultiple = mockElementService.idNonexistentMultiple(10);
    }

    @After
    public void cleanupTest() {
        elementService.reset();
    }

    @Test
    public void doesElementExistTest_doesExist_shouldReturnTrue() {
        assertTrue(elementClient.doesElementExist(elementService.createNode()));
    }

    @Test
    public void doesElementExistTest_doesNotExist_shouldReturnFalse() {
        assertFalse(elementClient.doesElementExist(idNonexistent));
    }

    @Test
    public void doAnyElementsExist_noneExist_shouldReturnFalse() {
        assertFalse(elementClient.doAnyElementsExist(idNonexistentMultiple));
    }

    @Test
    public void doAnyElementsExist_oneExists_shouldReturnTrue() {
        assertTrue(
            elementClient.doAnyElementsExist(
                OrderedSets.merge(idNonexistentMultiple, elementService.createNode())
            )
        );
    }

    @Test
    public void doAnyElementsExist_allExist_shouldReturnTrue() {
        assertTrue(elementClient.doAnyElementsExist(elementService.createNodes(10)));
    }

    @Test
    public void doAnyElementsExist_emptyList_shouldReturn400() {
        try {
            elementClient.doAnyElementsExist(OrderedSets.empty());
            fail();
        } catch(FeignException e) {
            assertExceptionLike(e, 400, "empty");
        }
    }
    
    @Test
    public void doAllElementsExist_noneExist_shouldReturnFalse() {
        assertFalse(elementClient.doAllElementsExist(idNonexistentMultiple));
    }

    @Test
    public void doAllElementsExist_oneExists_shouldReturnFalse() {
        assertFalse(
            elementClient.doAllElementsExist(
                OrderedSets.merge(idNonexistentMultiple, elementService.createNode())
            )
        );
    }

    @Test
    public void doAllElementsExist_allExist_shouldReturnTrue() {
        assertTrue(
            elementClient.doAllElementsExist(elementService.createNodes(10))
        );
    }

    @Test
    public void doAllElementsExist_emptyList_shouldReturn400() {
        try {
            elementClient.doAnyElementsExist(OrderedSets.empty());
            fail();
        } catch(FeignException e) {
            assertExceptionLike(e, 400, "empty");
        }
    }
    
    @Test
    public void doesElementExistWithATest_doesNot_shouldReturnFalse() {
        assertFalse(elementClient.doesElementExistWithA(idNonexistent));
    }
    
    @Test
    public void doesElementExistWithATest_does_shouldReturnTrue() {
        long idNode = elementService.createNode();
        assertTrue(elementClient.doesElementExistWithA(idNode));
    }

    @Test
    public void doesElementExistWithBTest_doesNot_shouldReturnFalse() {
        assertFalse(elementClient.doesElementExistWithB(idNonexistent));
    }

    @Test
    public void doesElementExistWithBTest_does_shouldReturnTrue() {
        long idNode = elementService.createNode();
        assertTrue(elementClient.doesElementExistWithB(idNode));
    }
    
    @Test
    public void doesElementExistWithAOrBTest_existsWithAOnly_shouldReturnTrue() {
        assertTrue(
            elementClient.doesElementExistWithAOrB(
                elementService.createPendantTo(elementService.createNode()),
                idNonexistent
            )
        );
    }

    @Test
    public void doesElementExistWithAOrBTest_existsWithBOnly_shouldReturnTrue() {
        assertTrue(
            elementClient.doesElementExistWithAOrB(
                idNonexistent,
                elementService.createPendantFrom(elementService.createNode())
            )
        );
    }

    @Test
    public void doesElementExistWithAOrBTest_existsWithAAndB_shouldReturnTrue() {
        long idNode = elementService.createNode();
        assertTrue(
            elementClient.doesElementExistWithAOrB(
                elementService.createPendantTo(idNode),
                elementService.createPendantFrom(idNode)
            )
        );
    }

    @Test
    public void doesElementExistWithAOrBTest_neither_shouldReturnFalse() {
        assertFalse(
            elementClient.doesElementExistWithAOrB(
                idNonexistent,
                idNonexistent
            )
        );
    }

    @Test
    public void doesElementExistWithAOrBTest_AAndBSame_exists_shouldReturnTrue() {
        long idNode = elementService.createNode();
        assertTrue(
            elementClient.doesElementExistWithAOrB(idNode, idNode)
        );
    }
    
    @Test
    public void doesElementExistWithAOrBTest_AAndBSame_doesNotExist_shouldReturnFalse() {
        assertFalse(
            elementClient.doesElementExistWithAOrB(idNonexistent, idNonexistent)
        );
    }

    @Test
    public void doesElementExistWithAAndBTest_existsWithAOnly_shouldReturnFalse() {
        assertFalse(
            elementClient.doesElementExistWithAAndB(
                elementService.createPendantTo(elementService.createNode()),
                idNonexistent
            )
        );
    }

    @Test
    public void doesElementExistWithAAndBTest_existsWithBOnly_shouldReturnFalse() {
        assertFalse(
            elementClient.doesElementExistWithAAndB(
                idNonexistent,
                elementService.createPendantFrom(elementService.createNode())
            )
        );
    }

    @Test
    public void doesElementExistWithAAndBTest_existsWithAAndB_shouldReturnTrue() {
        long idNode = elementService.createNode();
        elementService.createElement(idNode, idNode);
        assertTrue(
            elementClient.doesElementExistWithAAndB(idNode, idNode)
        );
    }

    @Test
    public void doesElementExistWithAAndBTest_neither_shouldReturnFalse() {
        assertFalse(
            elementClient.doesElementExistWithAAndB(
                idNonexistent,
                idNonexistent
            )
        );
    }

    @Test
    public void doesElementExistWithAAndBTest_AAndBSame_exists_shouldReturnTrue() {
        long idNode = elementService.createNode();
        assertTrue(
            elementClient.doesElementExistWithAAndB(idNode, idNode)
        );
    }

    @Test
    public void doesElementExistWithAAndBTest_AAndBSame_doesNotExist_shouldReturnFalse() {
        assertFalse(
            elementClient.doesElementExistWithAAndB(idNonexistent, idNonexistent)
        );
    }
    
    @Test
    public void doesElementHaveATest_does_shouldReturnTrue() {
        long idNode = elementService.createNode();
        assertTrue(elementClient.doesElementHaveA(idNode, idNode));
        assertTrue(
            elementClient.doesElementHaveA(
                elementService.createPendantFrom(idNode),
                idNode
            )
        );
    }
    
    @Test
    public void doesElementHaveATest_doesNot_shouldReturnFalse() {
        long idNode = elementService.createNode();
        assertFalse(
            elementClient.doesElementHaveA(
                elementService.createPendantTo(idNode),
                idNode
            )
        );
    }
    
    @Test
    public void doesElementHaveATest_doesNotExist_shouldReturn404() {
        try {
            elementClient.doesElementHaveA(idNonexistent, elementService.createNode());
            fail();
        } catch(FeignException e) {
            assertExceptionLike(e, 404, idNonexistent + "");
        }
    }
    
    @Test
    public void doElementsHaveATest_emptySet_shouldReturnEmptyList() {
        assertEquals(
            Collections.emptyList(),
            elementClient.doElementsHaveA(elementService.createNode(), OrderedSets.empty())
        );
    }
    
    @Test
    public void doElementsHaveATest_single_doesNot_shouldReturnFalse() {
        long idNode = elementService.createNode();
        assertEquals(
            Collections.singletonList(false),
            elementClient.doElementsHaveA(
                idNode,
                OrderedSets.singleton(elementService.createPendantTo(idNode))
            )
        );
    }

    @Test
    public void doElementsHaveATest_single_does_shouldReturnTrue() {
        long idNode = elementService.createNode();
        assertEquals(
            Collections.singletonList(true),
            elementClient.doElementsHaveA(
                idNode,
                OrderedSets.singleton(elementService.createPendantFrom(idNode))
            )
        );
    }

    @Test
    public void doElementsHaveATest_single_doesNotExist_shouldReturnFalse() {
        assertEquals(
            Collections.singletonList(false),
            elementClient.doElementsHaveA(
                elementService.createNode(),
                OrderedSets.singleton(idNonexistent)
            )
        );
    }

    @Test
    public void doElementsHaveATest_multiple() {
        long idNodeA = elementService.createNode();
        long idNodeB = elementService.createNode();
        assertEquals(
            Lists.newArrayList(
                true, false,
                true, true, true,
                false, false, false,
                true, true,
                true, false,
                false, false,
                false, false
            ),
            elementClient.doElementsHaveA(
                idNodeA,
                OrderedSets.with(
                    OrderedSets.with(idNodeA, idNodeB),
                    elementService.createPendantsFrom(idNodeA, 3),
                    elementService.createPendantsTo(idNodeA, 3),
                    elementService.createLoopsOn(idNodeA, 2),
                    OrderedSets.with(
                        elementService.createElement(idNodeA, idNodeB),
                        elementService.createElement(idNodeB, idNodeA),
                        elementService.createLoopOn(idNodeB),
                        elementService.createPendantFrom(idNodeB),
                        idNonexistentMultiple.get(0),
                        idNonexistentMultiple.get(1)
                    )
                )
            )
        );
    }

    @Test
    public void doesElementHaveBTest_does_shouldReturnTrue() {
        long idNode = elementService.createNode();
        assertTrue(elementClient.doesElementHaveB(idNode, idNode));
        assertTrue(
            elementClient.doesElementHaveB(
                elementService.createPendantTo(idNode),
                idNode
            )
        );
    }

    @Test
    public void doesElementHaveBTest_doesNot_shouldReturnFalse() {
        long idNode = elementService.createNode();
        assertFalse(
            elementClient.doesElementHaveB(
                elementService.createPendantFrom(idNode),
                idNode
            )
        );
    }

    @Test
    public void doesElementHaveBTest_doesNotExist_shouldReturn404() {
        try {
            elementClient.doesElementHaveB(idNonexistent, elementService.createNode());
            fail();
        } catch(FeignException e) {
            assertExceptionLike(e, 404, idNonexistent + "");
        }
    }

    @Test
    public void doElementsHaveBTest_emptySet_shouldReturnEmptyList() {
        assertEquals(
            Collections.emptyList(),
            elementClient.doElementsHaveA(elementService.createNode(), OrderedSets.empty())
        );
    }

    @Test
    public void doElementsHaveBTest_single_doesNot_shouldReturnFalse() {
        long idNode = elementService.createNode();
        assertEquals(
            Collections.singletonList(false),
            elementClient.doElementsHaveB(
                idNode,
                OrderedSets.singleton(elementService.createPendantFrom(idNode))
            )
        );
    }

    @Test
    public void doElementsHaveBTest_single_does_shouldReturnTrue() {
        long idNode = elementService.createNode();
        assertEquals(
            Collections.singletonList(true),
            elementClient.doElementsHaveB(
                idNode,
                OrderedSets.singleton(elementService.createPendantTo(idNode))
            )
        );
    }

    @Test
    public void doElementsHaveBTest_single_doesNotExist_shouldReturnFalse() {
        assertEquals(
            Collections.singletonList(false),
            elementClient.doElementsHaveB(
                elementService.createNode(),
                OrderedSets.singleton(idNonexistent)
            )
        );
    }

    @Test
    public void doElementsHaveBTest_multiple() {
        long idNodeA = elementService.createNode();
        long idNodeB = elementService.createNode();
        assertEquals(
            Lists.newArrayList(
                true, false,
                true, true, true,
                false, false, false,
                true, true,
                false, true,
                false, false,
                false, false
            ),
            elementClient.doElementsHaveB(
                idNodeA,
                OrderedSets.with(
                    OrderedSets.with(idNodeA, idNodeB),
                    elementService.createPendantsTo(idNodeA, 3),
                    elementService.createPendantsFrom(idNodeA, 3),
                    elementService.createLoopsOn(idNodeA, 2),
                    OrderedSets.with(
                        elementService.createElement(idNodeA, idNodeB),
                        elementService.createElement(idNodeB, idNodeA),
                        elementService.createLoopOn(idNodeB),
                        elementService.createPendantFrom(idNodeB),
                        idNonexistentMultiple.get(0),
                        idNonexistentMultiple.get(1)
                    )
                )
            )
        );
    }

    @Test
    public void isElementNodeTest_isNode_shouldReturnTrue() {
        assertTrue(elementClient.isElementNode(elementService.createNode()));
    }

    @Test
    public void isElementNodeTest_isNotNode_shouldReturnFalse() {
        long idNode = elementService.createNode();
        Sets.newHashSet(
            elementService.createElement(idNode, idNode),
            elementService.createElement(0, idNode),
            elementService.createElement(0, idNode)
        ).forEach(idElement -> assertFalse(elementClient.isElementNode(idElement)));
    }

    @Test
    public void isElementNodeTest_doesNotExist_shouldReturn404() {
        try {
            elementClient.isElementNode(idNonexistent);
            fail();
        } catch(FeignException e) {
            assertExceptionLike(e, 404, idNonexistent + "");
        }
    }
    
    @Test
    public void getNumElementsWithAOrBTest_noneExist_shouldReturnZero() {
        assertEquals(
            0,
            elementClient.numElementsWithAOrB(idNonexistent, idNonexistent)
        );
    }

    @Test
    public void areElementsNodesTest_emptyList_shouldReturnEmptyOrderedSet() {
        assertEquals(Collections.emptyList(), elementClient.areElementsNodes(OrderedSets.empty()));
    }

    @Test
    public void areElementsNodesTest_oneElement_isNode_shouldReturnTrue() {
        assertEquals(
            Lists.newArrayList(true),
            elementClient.areElementsNodes(
                OrderedSets.singleton(elementService.createNode())
            )
        );
    }

    @Test
    public void areElementsNodesTest_oneElement_isNotNode_shouldReturnFalse() {
        long idNode = elementService.createNode();
        Sets.newHashSet(
            elementService.createElement(idNode, idNode),
            elementService.createElement(0, idNode),
            elementService.createElement(0, idNode)
        ).forEach(idElement ->
            assertEquals(
                Collections.singletonList(false),
                elementClient.areElementsNodes(OrderedSets.singleton(idElement))
            )
        );
    }

    @Test
    public void areElementsNodesTest_oneElement_doesNotExist_shouldReturnFalse() {
        assertEquals(
            Collections.singletonList(false),
            elementClient.areElementsNodes(OrderedSets.singleton(idNonexistent))
        );
    }

    @Test
    public void areElementsNodesTest_multipleElements() {
        long idNode1 = elementService.createNode();
        long idNode2 = elementService.createNode();
        assertEquals(
            Lists.newArrayList(true, false, false, false, false, true),
            elementClient.areElementsNodes(
                OrderedSets.with(
                    idNode1,
                    elementService.createElement(idNode1, idNode2),
                    elementService.createElement(0, idNode1),
                    elementService.createElement(0, idNode2),
                    elementService.createElement(idNode1, idNode1),
                    idNode2
                )
            )
        );
    }

    @Test
    public void isElementPendantFromTest_itIs_shouldReturnTrue() {
        long idNode = elementService.createNode();
        assertTrue(
            elementClient.isElementPendantFrom(
                elementService.createPendantFrom(idNode),
                idNode
            )
        );
    }

    @Test
    public void isElementPendantFromTest_itIsNot_shouldReturnFalse() {
        long idNode = elementService.createNode();
        assertFalse(
            elementClient.isElementPendantFrom(
                elementService.createLoopOn(idNode),
                idNode
            )
        );
        assertFalse(
            elementClient.isElementPendantFrom(
                elementService.createPendantTo(
                    idNode
                ),
                idNode
            )
        );
    }

    @Test
    public void isElementPendantFromTest_elementDoesNotExist_shouldReturn404() {
        try {
            elementClient.isElementPendantFrom(idNonexistent, elementService.createNode());
            fail();
        } catch(FeignException e) {
            assertExceptionLike(e, 404, idNonexistent + "");
        }
    }

    @Test
    public void isElementPendantFromTest_pendantDoesNotExist_shouldReturn404() {
        try {
            elementClient.isElementPendantFrom(elementService.createNode(), idNonexistent);
            fail();
        } catch(FeignException e) {
            assertExceptionLike(e, 404, idNonexistent + "");
        }
    }

    @Test
    public void areElementsPendantsFromTest_noElements_shouldReturnEmptyList() {
        assertEquals(
            Collections.emptyList(),
            elementClient.areElementsPendantsFrom(elementService.createNode(), OrderedSets.empty())
        );
    }

    @Test
    public void areElementsPendantsFromTest_oneElement_isPendantFrom_shouldReturnTrue() {
        long idNode = elementService.createNode();
        assertEquals(
            Collections.singletonList(true),
            elementClient.areElementsPendantsFrom(
                idNode,
                OrderedSets.singleton(elementService.createPendantFrom(idNode))
            )
        );
    }

    @Test
    public void areElementsPendantsFromTest_oneElement_isNotPendantFrom_shouldReturnFalse() {
        long idNode = elementService.createNode();
        assertEquals(
            Collections.singletonList(false),
            elementClient.areElementsPendantsFrom(
                elementService.createPendantTo(idNode),
                OrderedSets.singleton(idNode)
            )
        );
    }

    @Test
    public void areElementsPendantsFromTest_oneElement_elementDoesNotExist_shouldReturnFalse() {
        assertEquals(
            Collections.singletonList(false),
            elementClient.areElementsPendantsFrom(
                elementService.createNode(),
                OrderedSets.singleton(idNonexistent)
            )
        );
    }

    @Test
    public void areElementsPendantsFromTest_oneElement_pendantDoesNotExist_shouldReturn404() {
        try {
            assertEquals(
                Collections.singletonList(false),
                elementClient.areElementsPendantsFrom(
                    idNonexistent,
                    OrderedSets.singleton(elementService.createNode())
                )
            );
        } catch(FeignException e) {
            assertExceptionLike(e, 404, idNonexistent + "");
        }
    }

    @Test
    public void areElementsPendantsFromTest_manyElements() {
        long idNode = elementService.createNode();
        assertEquals(
            Lists.newArrayList(
                false,
                true, true, true, true, true,
                false, false, false
            ),
            elementClient.areElementsPendantsFrom(
                idNode,
                OrderedSets.merge(
                    idNode,
                    elementService.createPendantsFrom(idNode, 5),
                    OrderedSets.with(
                        elementService.createPendantTo(idNode),
                        elementService.createLoopOn(idNode),
                        elementService.createNode()
                    )
                )
            )
        );
    }

    @Test
    public void isElementPendantToTest_itIs_shouldReturnTrue() {
        long idNode = elementService.createNode();
        assertTrue(
            elementClient.isElementPendantTo(
                elementService.createPendantTo(idNode),
                idNode
            )
        );
    }

    @Test
    public void isElementPendantToTest_itIsNot_shouldReturnFalse() {
        long idNode = elementService.createNode();
        assertFalse(
            elementClient.isElementPendantTo(
                elementService.createLoopOn(idNode),
                idNode
            )
        );
        assertFalse(
            elementClient.isElementPendantTo(
                elementService.createPendantFrom(idNode),
                idNode
            )
        );
    }

    @Test
    public void isElementPendantToTest_elementDoesNotExist_shouldReturn404() {
        try {
            elementClient.isElementPendantTo(idNonexistent, elementService.createNode());
            fail();
        } catch(FeignException e) {
            assertExceptionLike(e, 404, idNonexistent + "");
        }
    }

    @Test
    public void isElementPendantToTest_elementToDoesNotExist_shouldReturn404() {
        try {
            elementClient.isElementPendantTo(elementService.createNode(), idNonexistent);
            fail();
        } catch(FeignException e) {
            assertExceptionLike(e, 404, idNonexistent + "");
        }
    }

    @Test
    public void areElementsPendantsToTest_noElements_shouldReturnEmptyOrderedSet() {
        assertEquals(
            Collections.emptyList(),
            elementClient.areElementsPendantsTo(elementService.createNode(), OrderedSets.empty())
        );
    }

    @Test
    public void areElementsPendantsToTest_oneElement_isPendantTo_shouldReturnTrue() {
        long idNode = elementService.createNode();
        assertEquals(
            Collections.singletonList(true),
            elementClient.areElementsPendantsTo(
                idNode,
                OrderedSets.singleton(elementService.createPendantTo(idNode))
            )
        );
    }

    @Test
    public void areElementsPendantsToTest_oneElement_isNotPendantTo_shouldReturnFalse() {
        assertEquals(
            Collections.singletonList(false),
            elementClient.areElementsPendantsTo(
                elementService.createPendantTo(elementService.createNode()),
                OrderedSets.singleton(elementService.createNode())
            )
        );
    }

    @Test
    public void areElementsPendantsToTest_oneElement_elementDoesNotExist_shouldReturnFalse() {
        assertEquals(
            Collections.singletonList(false),
            elementClient.areElementsPendantsTo(
                elementService.createNode(),
                OrderedSets.singleton(idNonexistent)
            )
        );
    }

    @Test
    public void areElementsPendantsToTest_oneElement_pendantDoesNotExist_shouldReturn404() {
        try {
            assertEquals(
                Collections.singletonList(false),
                elementClient.areElementsPendantsTo(
                    idNonexistent,
                    OrderedSets.singleton(elementService.createNode())
                )
            );
        } catch(FeignException e) {
            assertExceptionLike(e, 404, idNonexistent + "");
        }
    }

    @Test
    public void areElementsPendantsToTest_manyElements() {
        long idNode = elementService.createNode();
        assertEquals(
            Lists.newArrayList(
                false, false,
                true, true, true, true, true,
                false, false
            ),
            elementClient.areElementsPendantsTo(
                idNode,
                OrderedSets.with(
                    OrderedSets.with(
                        idNode,
                        elementService.createPendantFrom(idNode)
                    ),
                    elementService.createPendantsTo(idNode, 5),
                    elementService.createLoopsOn(idNode, 2)
                )
            )
        );
    }

    @Test
    public void isElementLoopOnTest_itIs_shouldReturnTrue() {
        long idNode = elementService.createNode();
        assertTrue(
            elementClient.isElementLoopOn(
                elementService.createLoopOn(idNode),
                idNode
            )
        );
    }

    @Test
    public void isElementLoopOnTest_itIsNot_shouldReturnFalse() {
        long idNode = elementService.createNode();
        long idPendantTo = elementService.createPendantTo(idNode);
        assertFalse(
            elementClient.isElementLoopOn(
                elementService.createElement(idNode, idPendantTo),
                idNode
            )
        );
        assertFalse(
            elementClient.isElementLoopOn(
                idPendantTo,
                idNode
            )
        );
    }

    @Test
    public void isElementLoopOnTest_elementDoesNotExist_shouldReturn404() {
        try {
            elementClient.isElementLoopOn(idNonexistent, elementService.createNode());
            fail();
        } catch(FeignException e) {
            assertExceptionLike(e, 404, idNonexistent + "");
        }
    }

    @Test
    public void isElementLoopOnTest_elementOnDoesNotExist_shouldReturn404() {
        try {
            elementClient.isElementLoopOn(elementService.createNode(), idNonexistent);
            fail();
        } catch(FeignException e) {
            assertExceptionLike(e, 404, idNonexistent + "");
        }
    }

    @Test
    public void areElementsLoopsOnTest_noElements_shouldReturnEmptyOrderedSet() {
        assertEquals(
            Collections.emptyList(),
            elementClient.areElementsLoopsOn(elementService.createNode(), OrderedSets.empty())
        );
    }

    @Test
    public void areElementsLoopsOnTest_oneElement_isLoopOn_shouldReturnTrue() {
        long idNode = elementService.createNode();
        assertEquals(
            Collections.singletonList(true),
            elementClient.areElementsLoopsOn(
                idNode,
                OrderedSets.singleton(elementService.createLoopOn(idNode))
            )
        );
    }

    @Test
    public void areElementsLoopsOnTest_oneElement_isNotLoopOn_shouldReturnFalse() {
        long idNode = elementService.createNode();
        assertEquals(
            Collections.singletonList(false),
            elementClient.areElementsLoopsOn(
                elementService.createLoopOn(idNode),
                OrderedSets.singleton(idNode)
            )
        );
    }

    @Test
    public void areElementsLoopsOnTest_oneElement_elementDoesNotExist_shouldReturnFalse() {
        assertEquals(
            Collections.singletonList(false),
            elementClient.areElementsLoopsOn(
                elementService.createNode(),
                OrderedSets.singleton(idNonexistent)
            )
        );
    }

    @Test
    public void areElementsLoopsOnTest_oneElement_pendantDoesNotExist_shouldReturn404() {
        try {
            assertEquals(
                Collections.singletonList(false),
                elementClient.areElementsLoopsOn(idNonexistent, OrderedSets.singleton(elementService.createNode()))
            );
        } catch(FeignException e) {
            assertExceptionLike(e, 404, idNonexistent + "");
        }
    }

    @Test
    public void areElementsLoopsOnTest_manyElements() {
        long idNode = elementService.createNode();
        assertEquals(
            Lists.newArrayList(
                false,
                true, true, true, true, true,
                false, false
            ),
            elementClient.areElementsLoopsOn(
                idNode,
                OrderedSets.merge(
                    idNode,
                    elementService.createLoopsOn(idNode, 5),
                    elementService.createPendantsTo(idNode, 2)
                )
            )
        );
    }

    @Test
    public void isElementEndpointTest_isEndpoint_shouldReturnTrue() {
        long idNode = elementService.createNode();
        elementService.createPendantTo(idNode);
        assertTrue(elementClient.isElementEndpoint(idNode));
    }

    @Test
    public void isElementEndpointTest_isNotEndpoint_shouldReturnFalse() {
        assertFalse(elementClient.isElementEndpoint(elementService.createNode()));
    }

    @Test
    public void isElementEndpointTest_doesNotExist_shouldReturnFalse() {
        assertFalse(elementClient.isElementEndpoint(idNonexistent));
    }

    @Test
    public void areElementsEndpointsTest_emptyList_shouldReturnEmptyOrderedSet() {
        assertEquals(Collections.emptyList(), elementClient.areElementsEndpoints(OrderedSets.empty()));
    }

    @Test
    public void areElementsEndpointsTest_oneElement_isEndpoint_shouldReturnTrue() {
        long idNode = elementService.createNode();
        elementService.createPendantTo(idNode);
        assertEquals(
            Collections.singletonList(true),
            elementClient.areElementsEndpoints(OrderedSets.with(idNode))
        );
    }

    @Test
    public void areElementsEndpointsTest_oneElement_isNotEndpoint_shouldReturnFalse() {
        assertEquals(
            Collections.singletonList(false),
            elementClient.areElementsEndpoints(OrderedSets.with(elementService.createNode()))
        );
    }

    @Test
    public void areElementsEndpointsTest_oneElement_doesNotExist_shouldReturnFalse() {
        assertEquals(
            Collections.singletonList(false),
            elementClient.areElementsEndpoints(OrderedSets.singleton(idNonexistent))
        );
    }
    
    @Test
    public void areElementsEndpointsTest_multipleElements() {
        long idNode = elementService.createNode();
        long idPendantTo = elementService.createPendantTo(idNode);
        long idPendantFrom = elementService.createPendantFrom(idNode);
        assertEquals(
            Lists.newArrayList(true, true, true, false, false, false),
            elementClient.areElementsEndpoints(
                OrderedSets.with(
                    idNode,
                    idPendantTo,
                    idPendantFrom,
                    elementService.createElement(idPendantTo, idNode),
                    elementService.createLoopOn(idPendantFrom),
                    idNonexistent
                )
            )
        );
    }

    @Test
    public void areElementsConnectedTest_yes_bothSame_shouldReturnTrue() {
        long idNode1 = elementService.createNode();
        assertTrue(elementClient.areElementsConnected(idNode1, idNode1));
        long idNode2 = elementService.createNode();
        long idEdge = elementService.createElement(idNode2, idNode2);
        assertTrue(elementService.areElementsConnected(idEdge, idEdge));
    }
    
    @Test
    public void areElementsConnectedTest_yes_notSame_shouldReturnTrue() {
        long idNode = elementService.createNode();
        elementService.createElement(idNode, idNode);
        assertTrue(elementClient.areElementsConnected(idNode, idNode));
        assertTrue(
            elementClient.areElementsConnected(
                idNode,
                elementService.createLoopOn(idNode)
            )
        );
    }
    
    @Test
    public void areElementsConnectedTest_no_shouldReturnFalse() {
        assertFalse(
            elementClient.areElementsConnected(
                elementService.createNode(),
                elementService.createNode()
            )
        );
    }
    
    @Test
    public void areElementsConnectedTest_aDoesNotExist_shouldReturn404(){
        try {
            elementClient.areElementsConnected(idNonexistent, elementService.createNode());
            fail();
        } catch(FeignException e) {
            assertExceptionLike(e, 404, idNonexistent + "");
        }
    }
    
    @Test
    public void areElementsConnectedTest_bDoesNotExist_shouldReturn404() {
        try {
            elementClient.areElementsConnected(elementService.createNode(), idNonexistent);
            fail();
        } catch(FeignException e) {
            assertExceptionLike(e, 404, idNonexistent + "");
        }
    }
    
    @Test
    public void numElementsWithATest_noneExist_shouldReturnZero() {
        long idNode = elementService.createNode();
        assertEquals(
            0,
            elementClient.numElementsWithA(elementService.createPendantFrom(idNode))
        );
    }
    
    @Test
    public void numElementsWithATest_oneExists_shouldReturnOne() {
        assertEquals(1, elementClient.numElementsWithA(elementService.createNode()));
    }
    
    @Test
    public void numElementsWithATest_nExist_shouldReturnN() {
        long idNode = elementService.createNode();
        assertEquals(
            1 + elementService.createPendantsFrom(idNode, 5).size(),
            elementClient.numElementsWithA(idNode)
        );
    }
    
    @Test
    public void numElementsWithATest_aDoesNotExist_shouldReturn0() {
        assertEquals(0, elementClient.numElementsWithA(idNonexistent));
    }

    @Test
    public void numElementsWithBTest_noneExist_shouldReturnZero() {
        long idNode = elementService.createNode();
        assertEquals(
            0,
            elementClient.numElementsWithB(elementService.createPendantTo(idNode))
        );
    }

    @Test
    public void numElementsWitBTest_oneExists_shouldReturnOne() {
        assertEquals(1, elementClient.numElementsWithB(elementService.createNode()));
    }

    @Test
    public void numElementsWithBTest_nExist_shouldReturnN() {
        long idNode = elementService.createNode();
        assertEquals(
            1 + elementService.createPendantsTo(idNode, 5).size(),
            elementClient.numElementsWithB(idNode)
        );
    }

    @Test
    public void numElementsWithBTest_aDoesNotExist_shouldReturn0() {
        assertEquals(0, elementClient.numElementsWithB(idNonexistent));
    }

    @Test
    public void numElementsWithAOrBTest_oneAExists_shouldReturnOne() {
        assertEquals(
            1,
            elementClient.numElementsWithAOrB(
                elementService.createPendantTo(elementService.createNode()),
                idNonexistent
            )
        );
    }

    @Test
    public void numElementsWithAOrBTest_nAsExists_shouldReturnN() {
        long idNode = elementService.createNode();
        assertEquals(
            elementService.createPendantsFrom(idNode, 5).size() + 1,
            elementClient.numElementsWithAOrB(idNode, idNonexistent)
        );
    }

    @Test
    public void numElementsWithAOrBTest_oneBExists_shouldReturnOne() {
        assertEquals(
            1,
            elementClient.numElementsWithAOrB(
                idNonexistent,
                elementService.createPendantFrom(elementService.createNode())
            )
        );
    }

    @Test
    public void numElementsWithAOrBTest_nBsExists_shouldReturnN() {
        long idNode = elementService.createNode();
        assertEquals(
            elementService.createPendantsTo(idNode, 5).size() + 1,
            elementClient.numElementsWithAOrB(idNonexistent, idNode)
        );
    }
    
    @Test
    public void numElementsWithAOrBTest_nHodgepodgeElementsWithAOrBExist_shouldReturnN() {
        long idNode1 = elementService.createNode();
        long idNode2 = elementService.createNode();
        elementService.createPendantFrom(idNode1);
        elementService.createPendantFrom(idNode2);
        elementService.createPendantTo(idNode1);
        elementService.createPendantTo(idNode2);
        elementService.createLoopOn(idNode1);
        elementService.createLoopOn(idNode2);
        assertEquals(
            4,
            elementClient.numElementsWithAOrB(
                idNode1,
                elementService.createElement(idNode1, idNode2)
            )
        );
    }

    @Test
    public void numElementsWithAOrBTest_aAndBSame_noneExist_shouldReturnZero() {
        assertEquals(0, elementClient.numElementsWithAOrB(idNonexistent, idNonexistent));
    }

    @Test
    public void numElementsWithAOrBTest_aAndBSame_oneExists_shouldReturnOne() {
        long idPendant = elementService.createPendantFrom(elementService.createNode());
        assertEquals(1, elementClient.numElementsWithAOrB(idPendant, idPendant));
    }

    @Test
    public void numElementsWithAOrBTest_aAndBSame_manyExist_shouldReturnMany() {
        long idNode = elementService.createNode();
        assertEquals(
            1 + elementService.createPendantsTo(idNode, 5).size(),
            elementClient.numElementsWithAOrB(idNode, idNode)
        );
    }

    @Test
    public void numElementsWithAAndBTest_noneExist_shouldReturnZero() {
        assertEquals(
            0,
            elementClient.numElementsWithAAndB(idNonexistentMultiple.get(0), idNonexistentMultiple.get(1))
        );
    }

    @Test
    public void numElementsWithAAndBTest_oneAExists_shouldReturnZero() {
        assertEquals(
            0,
            elementClient.numElementsWithAAndB(
                elementService.createPendantTo(elementService.createNode()),
                idNonexistent
            )
        );
    }

    @Test
    public void numElementsWithAAndBTest_nAsExists__bDoesNotExist_shouldReturn0() {
        long idNode = elementService.createNode();
        elementService.createPendantsFrom(idNode, 5);
        assertEquals(0, elementClient.numElementsWithAAndB(idNode, idNonexistent));
    }

    @Test
    public void numElementsWithAAndBTest_aDoesNotExist_oneBExists_shouldReturnZero() {
        assertEquals(
            0,
            elementClient.numElementsWithAAndB(
                idNonexistent,
                elementService.createPendantFrom(elementService.createNode())
            )
        );
    }

    @Test
    public void numElementsWithAAndBTest_aDoesNotExist_nBsExists_shouldReturnZero() {
        long idNode = elementService.createNode();
        elementService.createPendantsTo(idNode, 5);
        assertEquals(0, elementClient.numElementsWithAAndB(idNonexistent, idNode));
    }

    @Test
    public void numElementsWithAAndBTest_nHodgepodgeElementsWithAAndBExist_shouldReturnN() {
        long idNode1 = elementService.createNode();
        long idNode2 = elementService.createNode();
        elementService.createElement(idNode1, idNode2);
        elementService.createElement(idNode1, idNode2);
        elementService.createPendantFrom(idNode1);
        elementService.createPendantFrom(idNode2);
        elementService.createPendantTo(idNode1);
        elementService.createPendantTo(idNode2);
        elementService.createLoopOn(idNode1);
        elementService.createLoopOn(idNode2);
        assertEquals(2, elementClient.numElementsWithAAndB(idNode1, idNode2));
    }

    @Test
    public void numElementsWithAAndBTest_aAndBSame_noneExist_shouldReturnZero() {
        long idPendant = elementService.createPendantFrom(elementService.createNode());
        assertEquals(0, elementClient.numElementsWithAAndB(idPendant, idPendant));
    }

    @Test
    public void numElementsWithAAndBTest_aAndBSame_oneExists_shouldReturnOne() {
        long idNode = elementService.createNode();
        assertEquals(1, elementClient.numElementsWithAAndB(idNode, idNode));
    }

    @Test
    public void numElementsWithAAndBTest_aAndBSame_manyExist_shouldReturnMany() {
        long idNode = elementService.createNode();
        assertEquals(
            1 + elementService.createLoopsOn(idNode, 5).size(),
            elementClient.numElementsWithAAndB(idNode, idNode)
        );
    }

    @Test
    public void numNodesTest_noneAdded_shouldReturnZero() {
        assertEquals(0, elementClient.numNodes());
    }

    @Test
    public void numNodesTest_oneAdded_shouldReturnOne() {
        elementService.createNode();
        assertEquals(1, elementClient.numNodes());
    }

    @Test
    public void numNodesTest_nAdded_shouldReturnN() {
        long idNode = elementService.createNode();
        elementService.createNodes(3);
        elementService.createPendantFrom(idNode);
        elementService.createPendantTo(idNode);
        elementService.createLoopOn(idNode);
        elementService.createElement(idNode, idNode);
        assertEquals(4, elementClient.numNodes());
    }
    
    @Test
    public void numPendantsFromTest_noneExist_shouldReturnZero() {
        assertEquals(0, elementClient.numPendantsFrom(elementService.createNode()));
    }

    @Test
    public void numPendantsFromTest_oneExists_shouldReturnOne() {
        long idNode = elementService.createNode();
        elementService.createPendantFrom(idNode);
        assertEquals(1, elementClient.numPendantsFrom(idNode));
    }

    @Test
    public void numPendantsFromTest_nExist_shouldReturnN() {
        long idNode = elementService.createNode();
        assertEquals(
            elementService.createPendantsFrom(idNode, 5).size(),
            elementClient.numPendantsFrom(idNode)
        );
    }

    @Test
    public void numPendantsFromTest_doesNotExist_shouldReturn404() {
        try {
            elementClient.numPendantsFrom(idNonexistent);
            fail();
        } catch(FeignException e) {
            assertExceptionLike(e, 404, idNonexistent + "");
        }
    }

    @Test
    public void numPendantsToTest_noneExist_shouldReturnZero() {
        assertEquals(0, elementClient.numPendantsTo(elementService.createNode()));
    }

    @Test
    public void numPendantsToTest_oneExists_shouldReturnOne() {
        long idNode = elementService.createNode();
        elementService.createPendantTo(idNode);
        assertEquals(1, elementClient.numPendantsTo(idNode));
    }

    @Test
    public void numPendantsToTest_nExist_shouldReturnN() {
        long idNode = elementService.createNode();
        assertEquals(
            elementService.createPendantsTo(idNode, 5).size(),
            elementClient.numPendantsTo(idNode)
        );
    }

    @Test
    public void numPendantsToTest_doesNotExist_shouldReturn404() {
        try {
            elementClient.numPendantsTo(idNonexistent);
            fail();
        } catch(FeignException e) {
            assertExceptionLike(e, 404, idNonexistent + "");
        }
    }

    @Test
    public void numLoopsOnTest_noneExist_shouldReturnZero() {
        assertEquals(0, elementClient.numLoopsOn(elementService.createNode()));
    }

    @Test
    public void numLoopsOnTest_oneExists_shouldReturnOne() {
        long idNode = elementService.createNode();
        elementService.createLoopOn(idNode);
        assertEquals(1, elementClient.numLoopsOn(idNode));
    }

    @Test
    public void numLoopsOnTest_nExist_shouldReturnN() {
        long idNode = elementService.createNode();
        assertEquals(
            elementService.createLoopsOn(idNode, 5).size(),
            elementClient.numLoopsOn(idNode)
        );
    }

    @Test
    public void numLoopsOnTest_doesNotExist_shouldReturn404() {
        try {
            elementClient.numLoopsOn(idNonexistent);
            fail();
        } catch(FeignException e) {
            assertExceptionLike(e, 404, idNonexistent + "");
        }
    }

    @Test
    public void getElementTest_exists_shouldBeReturned() {
        Element node = mockElementService.node();
        assertElementsSame(
            node,
            elementClient.getElement(node.getId())
        );
    }

    @Test
    public void getElementTest_doesNotExist_shouldReturn404() {
        try {
            elementClient.getElement(idNonexistent);
            fail();
        } catch(FeignException e) {
            assertExceptionLike(e, 404, idNonexistent + "");
        }
    }
    
    @Test
    public void getAllIdsTest_noneAdded_shouldReturnEmptySet() {
        assertEquals(
            OrderedSets.empty(),
            elementClient.getAllIds()
        );
    }
    
    @Test
    public void getAllIdsTest_oneExists_shouldBeReturned() {
        assertEquals(
            elementService.createNodes(1),
            elementClient.getAllIds()
        );
    }
    
    @Test
    public void getAllIdsTest_manyExist_shouldBeReturned() {
        assertEquals(
            elementService.createNodes(10),
            elementClient.getAllIds()
        );
    }

    @Test
    public void getIdsTest_allExist_shouldReturnAll() {
        OrderedSet<Long> idElements = OrderedSets.with(elementService.createNodes(5));
        assertEquals(idElements, elementClient.getIds(idElements));
    }

    @Test
    public void getIdsTest_someExist_shouldReturnThem() {
        OrderedSet<Long> idNodes = elementService.createNodes(5);
        assertEquals(
            Tuples.join(idNodes, Collections.<Long>nCopies(idNonexistentMultiple.size(), null)),
            elementClient.getIds(
                OrderedSets.with(idNodes, idNonexistentMultiple)
            )
        );
    }

    @Test
    public void getIdsTest_oneExists_shouldReturnIt() {
        long idNode = elementService.createNode();
        assertEquals(
            Tuples.join(
                Collections.nCopies(idNonexistentMultiple.size(), null),
                idNode
            ),
            elementClient.getIds(
                OrderedSets.merge(idNonexistentMultiple, idNode)
            )
        );
    }

    @Test
    public void getIdsTest_noneExist_shouldReturnListWithNulls() {
        assertEquals(
            Collections.<Long>nCopies(idNonexistentMultiple.size(), null),
            elementClient.getIds(idNonexistentMultiple)
        );
    }

    @Test
    public void getIdsTest_onePassed_exists_shouldBeReturned() {
        long idNode = elementService.createNode();
        assertEquals(
            OrderedSets.singleton(idNode),
            elementClient.getIds(OrderedSets.singleton(idNode))
        );
    }

    @Test
    public void getIdsTest_onePassed_doesNotExist_shouldReturnListWithNull() {
        assertEquals(
            Collections.<Long>singletonList(null),
            elementClient.getIds(OrderedSets.singleton(idNonexistent))
        );
    }

    @Test
    public void getIdsTest_emptyListPassed_shouldReturnEmptyOrderedSet() {
        assertEquals(OrderedSets.empty(), elementClient.getIds(OrderedSets.empty()));
    }

    @Test
    public void getElementsTest_allExist_shouldReturnAll() {
        OrderedSet<Element> elements = mockElementService.nodes(5);
        assertEquals(
            elements,
            elementClient.getElements(
                elements
                    .stream()
                    .map(Element::getId)
                    .collect(Collectors.toCollection(OrderedSets::with))
            )
        );
    }

    @Test
    public void getElementsTest_someExist_shouldReturnThem() {
        OrderedSet<Element> nodes = mockElementService.nodes(5);
        assertEquals(
            Tuples.join(nodes, Collections.<Element>nCopies(idNonexistentMultiple.size(), null)),
            elementClient.getElements(
                OrderedSets.with(
                    nodes
                        .stream()
                        .map(Element::getId)
                        .collect(Collectors.toCollection(OrderedSet::new)),
                    idNonexistentMultiple
                )
            )
        );
    }

    @Test
    public void getElementsTest_oneExists_shouldReturnIt() {
        Element node = mockElementService.node();
        assertEquals(
            Tuples.join(node, Collections.nCopies(idNonexistentMultiple.size(), null)),
            elementClient.getElements(
                OrderedSets.merge(node.getId(), idNonexistentMultiple)
            )
        );
    }

    @Test
    public void getElementsTest_noneExist_shouldReturnAllNulls() {
        assertEquals(
            Collections.nCopies(idNonexistentMultiple.size(), null),
            elementClient.getElements(idNonexistentMultiple)
        );
    }

    @Test
    public void getElementsTest_onePassed_exists_shouldBeReturned() {
        Element node = mockElementService.node();
        assertEquals(
            OrderedSets.singleton(node),
            elementClient.getElements(OrderedSets.with(node.getId()))
        );
    }

    @Test
    public void getElementsTest_onePassed_doesNotExist_shouldReturnListWithNull() {
        assertEquals(
            Collections.<Element>singletonList(null),
            elementClient.getElements(
                OrderedSets.with(idNonexistent)
            )
        );
    }

    @Test
    public void getElementsTest_emptySetPassed_shouldReturnEmptySet() {
        assertEquals(
            OrderedSets.empty(),
            elementClient.getElements(OrderedSets.empty())
        );
    }
    
    @Test
    public void getIdsWithATest_noneExist_shouldReturnEmptyList() {
        long idNode = elementService.createNode();
        assertEquals(
            OrderedSets.empty(),
            elementClient.getIdsWithA(elementService.createPendantFrom(idNode))
        );
    }
    
    @Test
    public void getIdsWithATest_oneExists_shouldBeReturned() {
        long idNode = elementService.createNode();
        assertEquals(
            OrderedSets.singleton(idNode),
            elementClient.getIdsWithA(idNode)
        );
    }
    
    @Test
    public void getIdsWithATest_manyExist_shouldBeReturned() {
        long idNode = elementService.createNode();
        assertEquals(
            OrderedSets.with(
                OrderedSets.singleton(idNode),
                elementService.createPendantsFrom(idNode, 5),
                elementService.createLoopsOn(idNode, 5)
            ),
            elementClient.getIdsWithA(idNode)
        );
    }
    
    @Test
    public void getIdsWithATest_aDoesNotExist_shouldReturn404() {
        try {
            elementClient.getIdsWithA(idNonexistent);
            fail();
        } catch(FeignException e) {
            assertExceptionLike(e, 404,idNonexistent + "");
        }
    }

    @Test
    public void getIdsWithBTest_noneExist_shouldReturnEmptyList() {
        long idNode = elementService.createNode();
        assertEquals(
            OrderedSets.empty(),
            elementClient.getIdsWithB(elementService.createPendantTo(idNode))
        );
    }

    @Test
    public void getIdsWithBTest_oneExists_shouldBeReturned() {
        long idNode = elementService.createNode();
        assertEquals(
            OrderedSets.singleton(idNode),
            elementClient.getIdsWithB(idNode)
        );
    }

    @Test
    public void getIdsWithBTest_manyExist_shouldBeReturned() {
        long idNode = elementService.createNode();
        assertEquals(
            OrderedSets.with(
                OrderedSets.singleton(idNode),
                elementService.createPendantsTo(idNode, 5),
                elementService.createLoopsOn(idNode, 5)
            ),
            elementClient.getIdsWithB(idNode)
        );
    }

    @Test
    public void getIdsWithBTest_doesNotExist_shouldReturn404() {
        try {
            elementClient.getIdsWithB(idNonexistent);
            fail();
        } catch(FeignException e) {
            assertExceptionLike(e, 404,idNonexistent + "");
        }
    }

    @Test
    public void getIdsWithAOrBTest_noneExist_shouldReturnEmptyList() {
        assertTrue(
            elementClient.getIdsWithAOrB(
                idNonexistentMultiple.get(0),
                idNonexistentMultiple.get(1)
            ).isEmpty()
        );
    }

    @Test
    public void getIdsWithAOrBTest_oneAExists_bDoesNotExist_shouldReturnA() {
        long idPendant = elementService.createPendantTo(elementService.createNode());
        assertEquals(
            Collections.singleton(idPendant),
            elementClient.getIdsWithAOrB(idPendant, idNonexistent)
        );
    }

    @Test
    public void getIdsWithAOrBTest_nAsExists_bDoesNotExist_shouldReturnAs() {
        long idNode = elementService.createNode();
        assertEquals(
            OrderedSets.merge(
                idNode,
                elementService.createPendantsFrom(idNode, 5)
            ),
            elementClient.getIdsWithAOrB(idNode, idNonexistent)
        );
    }

    @Test
    public void getIdsWithAOrBTest_aDoesNotExist_oneBExists_shouldReturnIt() {
        long idPendant = elementService.createPendantFrom(elementService.createNode());
        assertEquals(
            Collections.singleton(idPendant),
            elementClient.getIdsWithAOrB(idNonexistent, idPendant)
        );
    }

    @Test
    public void getIdsWithAOrBTest_aDoesNotExist_nBsExists_shouldReturnThem() {
        long idNode = elementService.createNode();
        assertEquals(
            OrderedSets.merge(
                idNode,
                elementService.createPendantsTo(idNode, 5)
            ),
            elementClient.getIdsWithAOrB(idNonexistent, idNode)
        );
    }

    @Test
    public void getIdsWithAOrBTest_nHodgepodgeElementsWithAOrBExist_shouldReturnMatches() {
        long idNode1 = elementService.createNode();
        long idNode2 = elementService.createNode();
        long idEdge = elementService.createElement(idNode1, idNode2);
        long idPendantFrom1 = elementService.createPendantFrom(idNode1);
        long idLoopOn1 = elementService.createLoopOn(idNode1);
        elementService.createPendantTo(idNode2);
        elementService.createPendantFrom(idNode2);
        elementService.createPendantTo(idNode2);
        elementService.createLoopOn(idNode2);
        assertEquals(
            OrderedSets.with(idNode1, idEdge, idPendantFrom1, idLoopOn1),
            elementClient.getIdsWithAOrB(idNode1, idEdge)
        );
    }

    @Test
    public void getIdsWithAOrBTest_aAndBSame_noneExist_shouldReturnEmptyList() {
        assertTrue(elementClient.getIdsWithAOrB(idNonexistent, idNonexistent).isEmpty());
    }

    @Test
    public void getIdsWithAOrBTest_aAndBSame_oneExists_shouldReturnIt() {
        long idPendant = elementService.createPendantFrom(elementService.createNode());
        assertEquals(Collections.singleton(idPendant), elementClient.getIdsWithAOrB(idPendant, idPendant));
    }

    @Test
    public void getIdsWithAOrBTest_aAndBSame_manyExist_shouldReturnMany() {
        long idNode = elementService.createNode();
        assertEquals(
            OrderedSets.merge(
                idNode,
                elementService.createPendantsTo(idNode, 5)
            ),
            elementClient.getIdsWithAOrB(idNode, idNode)
        );
    }

    @Test
    public void getIdsWithAAndBTest_noneExist_shouldReturnEmptyList() {
        assertTrue(
            elementClient.getIdsWithAAndB(
                idNonexistentMultiple.get(0),
                idNonexistentMultiple.get(1)
            ).isEmpty()
        );
    }

    @Test
    public void getIdsWithAAndBTest_oneAExists_bDoesNotExist_shouldReturnEmptyList() {
        assertTrue(
            elementClient.getIdsWithAAndB(
                elementService.createPendantTo(elementService.createNode()),
                idNonexistent
            ).isEmpty()
        );
    }

    @Test
    public void getIdsWithAAndBTest_nAsExists_bDoesNotExist_shouldReturnEmptyList() {
        long idNode = elementService.createNode();
        elementService.createPendantsFrom(idNode, 5);
        assertTrue(
            elementClient.getIdsWithAAndB(idNode, idNonexistent).isEmpty()
        );
    }

    @Test
    public void getIdsWithAAndBTest_aDoesNotExist_oneBExists_shouldReturnEmptyList() {
        assertTrue(
            elementClient.getIdsWithAAndB(
                idNonexistent,
                elementService.createPendantFrom(elementService.createNode())
            ).isEmpty()
        );
    }

    @Test
    public void getIdsWithAAndBTest_aDoesNotExist_nBsExists_shouldReturnEmptyList() {
        long idNode = elementService.createNode();
        elementService.createPendantsTo(idNode, 5);
        assertTrue(elementClient.getIdsWithAAndB(idNonexistent, idNode).isEmpty());
    }

    @Test
    public void getIdsWithAAndBTest_nHodgepodgeElementsWithAAndBExist_shouldReturnThem() {
        long idNode1 = elementService.createNode();
        long idNode2 = elementService.createNode();
        long idEdge1 = elementService.createElement(idNode1, idNode2);
        long idEdge2 = elementService.createElement(idNode1, idNode2);
        elementService.createPendantFrom(idNode1);
        elementService.createPendantFrom(idNode2);
        elementService.createPendantTo(idNode1);
        elementService.createPendantTo(idNode2);
        elementService.createLoopOn(idNode1);
        elementService.createLoopOn(idNode2);
        assertEquals(
            OrderedSets.with(idEdge1, idEdge2),
            elementClient.getIdsWithAAndB(idNode1, idNode2)
        );
    }

    @Test
    public void getIdsWithAAndBTest_aAndBSame_noneExist_shouldReturnEmptyList() {
        long idPendant = elementService.createPendantFrom(elementService.createNode());
        assertTrue(elementClient.getIdsWithAAndB(idPendant, idPendant).isEmpty());
    }

    @Test
    public void getIdsWithAAndBTest_aAndBSame_oneExists_shouldReturnIt() {
        long idNode = elementService.createNode();
        assertEquals(
            Collections.singleton(idNode),
            elementClient.getIdsWithAAndB(idNode, idNode)
        );
    }

    @Test
    public void getIdsWithAAndBTest_aAndBSame_manyExist_shouldReturnThem() {
        long idNode = elementService.createNode();
        assertEquals(
            OrderedSets.merge(
                idNode,
                elementService.createLoopsOn(idNode, 5)
            ),
            elementClient.getIdsWithAAndB(idNode, idNode)
        );
    }
    
    @Test
    public void getIdsNodesTest_noneAdded_shouldEmptySet() {
        assertEquals(
            OrderedSets.empty(),
            elementClient.getIdsNodes()
        );
    }
    
    @Test
    public void getIdsNodesTest_oneAdded_shouldBeReturned() {
        long idNode = elementService.createNode();
        assertEquals(
            OrderedSets.singleton(idNode),
            elementService.getIdsNodes()
        );
    }
    
    @Test
    public void getIdsNodesTest_manyAdded_shouldBeReturned() {
        assertEquals(
            elementService.createNodes(5),
            elementService.getIdsNodes()
        );
    }
    
    @Test
    public void getIdsPendantsFromTest_noneAdded_shouldReturnEmptySet() {
        assertEquals(
            OrderedSets.empty(),
            elementClient.getIdsPendantsFrom(elementService.createNode())
        );
    }
    
    @Test
    public void getIdsPendantsFromTest_oneExists_shouldBeReturned() {
        long idNode = elementService.createNode();
        assertEquals(
            OrderedSets.singleton(elementService.createPendantFrom(idNode)),
            elementClient.getIdsPendantsFrom(idNode)
        );
    }
    
    @Test
    public void getIdsPendantsFromTest_manyExist_shouldBeReturned() {
        long idNode = elementService.createNode();
        assertEquals(
            elementService.createPendantsFrom(idNode, 5),
            elementClient.getIdsPendantsFrom(idNode)
        );
    }
    
    @Test
    public void getIdsPendantsFromTest_doesNotExist_shouldReturn404() {
        try {
            elementClient.getIdsPendantsFrom(idNonexistent);
            fail();
        } catch(FeignException e) {
            assertExceptionLike(e, 404, idNonexistent + "");
        }
    }

    @Test
    public void getIdsPendantsToTest_noneExist_shouldReturnEmptyOrderedSet() {
        assertEquals(
            OrderedSets.empty(),
            elementClient.getIdsPendantsTo(elementService.createNode())
        );
    }

    @Test
    public void getIdsPendantsToTest_oneExists_shouldBeReturned() {
        long idNode = elementService.createNode();
        assertEquals(
            OrderedSets.singleton(elementService.createPendantTo(idNode)),
            elementClient.getIdsPendantsTo(idNode)
        );
    }

    @Test
    public void getIdsPendantsToTest_manyExist_shouldBeReturned() {
        long idNode = elementService.createNode();
        assertEquals(
            elementService.createPendantsTo(idNode, 5),
            elementClient.getIdsPendantsTo(idNode)
        );
    }

    @Test
    public void getIdsPendantsToTest_doesNotExist_shouldReturn404() {
        try {
            elementClient.getIdsPendantsTo(idNonexistent);
            fail();
        } catch(FeignException e) {
            assertExceptionLike(e, 404, idNonexistent + "");
        }
    }

    @Test
    public void getIdsLoopsOnTest_noneExist_shouldReturnEmptyOrderedSet() {
        assertEquals(
            OrderedSets.empty(),
            elementClient.getIdsLoopsOn(elementService.createNode())
        );
    }

    @Test
    public void getIdsLoopsOnTest_oneExists_shouldBeReturned() {
        long idNode = elementService.createNode();
        assertEquals(
            elementService.createLoopsOn(idNode, 1),
            elementClient.getIdsLoopsOn(idNode)
        );
    }

    @Test
    public void getIdsLoopsOnTest_manyExist_shouldBeReturned() {
        long idNode = elementService.createNode();
        assertEquals(
            elementService.createLoopsOn(idNode, 5),
            elementClient.getIdsLoopsOn(idNode)
        );
    }

    @Test
    public void getIdsLoopsOnTest_doesNotExist_shouldReturn404() {
        try {
            elementClient.getIdsLoopsOn(idNonexistent);
            fail();
        } catch(FeignException e) {
            assertExceptionLike(e, 404, idNonexistent + "");
        }
    }

    @Test
    public void getIdsEndpointsOfTest_noneExist_shouldReturnEmptyOrderedSet() {
        assertEquals(
            OrderedSets.empty(),
            elementClient.getIdsEndpointsOf(elementService.createNode())
        );
    }

    @Test
    public void getIdsEndpointsOfTest_oneExists_shouldBeReturned() {
        long idNode = elementService.createNode();
        assertEquals(
            elementService.createPendantsFrom(idNode, 1),
            elementClient.getIdsEndpointsOf(idNode)
        );
    }

    @Test
    public void getIdsEndpointsOfTest_manyExist_shouldBeReturned() {
        long idNode1 = elementService.createNode();
        long idNode2 = elementService.createNode();
        assertEquals(
            OrderedSets.with(
                elementService.createPendantsFrom(idNode1, 5),
                elementService.createPendantsTo(idNode1, 5),
                elementService.createLoopsOn(idNode1, 5),
                OrderedSets.with(
                    elementService.createElement(idNode1, idNode2),
                    elementService.createElement(idNode2, idNode1)
                )
            ),
            elementClient.getIdsEndpointsOf(idNode1)
        );
    }

    @Test
    public void getIdsEndpointsOfTest_doesNotExist_shouldReturn404() {
        try {
            elementClient.getIdsEndpointsOf(idNonexistent);
            fail();
        } catch(FeignException e) {
            assertExceptionLike(e, 404, idNonexistent + "");
        }
    }

    @Test
    public void getIdsEndpointsOfForEachTest_emptyOrderedSet_shouldReturnedEmptyOrderedSet() {
        assertEquals(
            Collections.<OrderedSet<Long>>emptyList(),
            elementClient.getIdsEndpointsOfForEach(OrderedSets.empty())
        );
    }

    @Test
    public void getIdsEndpointsOfForEachTest_singleton_noEndpoints_shouldReturnSingletonWithEmptySet() {
        assertEquals(
            OrderedSets.singleton(Collections.emptySet()),
            elementClient.getIdsEndpointsOfForEach(elementService.createNodes(1))
        );
    }

    @Test
    public void getIdsEndpointsOfForEachTest_singleton_oneEndpoint_shouldReturnSingletonWithSingletonWithEndpoint() {
        long idNode = elementService.createNode();
        assertEquals(
            OrderedSets.singleton(elementService.createPendantsFrom(idNode, 1)),
            elementClient.getIdsEndpointsOfForEach(OrderedSets.singleton(idNode))
        );
    }

    @Test
    public void getIdsEndpointsOfForEachTest_singleton_manyEndpoints_shouldReturnSingletonWithEndpoints(){
        long idNode = elementService.createNode();
        assertEquals(
            OrderedSets.singleton(
                Stream
                    .of(
                        elementService.createPendantsFrom(idNode, 5),
                        elementService.createPendantsTo(idNode, 5),
                        elementService.createLoopsOn(idNode, 5),
                        Collections.singleton(elementService.createElement(idNode, idNode)),
                        Collections.singleton(elementService.createElement(idNode, idNode))
                    ).flatMap(Collection::stream)
                    .collect(Collectors.toCollection(OrderedSet::new))
            ),
            elementClient.getIdsEndpointsOfForEach(OrderedSets.singleton(idNode))
        );
    }

    @Test
    public void getIdsEndpointsOfForEachTest_manyIds_hodgepodge_shouldReturnEndpoints(){
        long idNode1 = elementService.createNode();
        long idNode2 = elementService.createNode();
        long idNode3 = elementService.createNode();
        Set<Long> idPendantsFrom = elementService.createPendantsFrom(idNode1, 3);
        Set<Long> idPendantsTo = elementService.createPendantsTo(idNode1, 3);
        Set<Long> idLoopsOn = elementService.createLoopsOn(idNode1, 3);
        Set<Long> idEdges = elementService.createElements(
            OrderedSets.with(
                new CreateElementRequest(idNode1, idNode2),
                new CreateElementRequest(idNode2, idNode1)
            )
        );
        assertEquals(
            Lists.newArrayList(
                OrderedSets.with(idPendantsFrom, idPendantsTo, idLoopsOn, idEdges),
                idEdges,
                OrderedSets.empty(),
                OrderedSets.empty(), OrderedSets.empty(), OrderedSets.empty(),
                OrderedSets.empty(), OrderedSets.empty(), OrderedSets.empty(),
                OrderedSets.empty(), OrderedSets.empty(), OrderedSets.empty(),
                OrderedSets.empty(), OrderedSets.empty()
            ),
            elementClient.getIdsEndpointsOfForEach(
                OrderedSets.with(
                    OrderedSets.with(idNode1, idNode2, idNode3),
                    idPendantsFrom,
                    idPendantsTo,
                    idLoopsOn,
                    idEdges
                )
            )
        );
    }

    @Test
    public void getAllElementsTest_noneAdded_shouldEmptySet() {
        assertEquals(OrderedSets.empty(), elementClient.getAllElements());
    }

    @Test
    public void getAllElementsTest_oneExists_shouldBeReturned() {
        assertEquals(
            mockElementService.nodes(1),
            elementClient.getAllElements()
        );
    }

    @Test
    public void getAllElementsTest_manyExist_shouldBeReturned() {
        assertEquals(
            mockElementService.nodes(5),
            elementClient.getAllElements()
        );
    }

    @Test
    public void getElementsWithAOrBTest_noneExist_shouldReturnEmptyList() {
        assertTrue(
            elementClient.getElementsWithAOrB(
                idNonexistentMultiple.get(0),
                idNonexistentMultiple.get(1)
            ).isEmpty()
        );
    }


    @Test
    public void getElementsWithATest_noneExist_shouldReturnEmptyList() {
        long idNode = elementService.createNode();
        assertEquals(
            OrderedSets.empty(),
            elementClient.getElementsWithA(elementService.createPendantFrom(idNode))
        );
    }

    @Test
    public void getElementsWithATest_oneExists_shouldBeReturned() {
        Element node = mockElementService.node();
        assertEquals(
            OrderedSets.singleton(node),
            elementClient.getElementsWithA(node.getId())
        );
    }

    @Test
    public void getElementsWithATest_manyExist_shouldBeReturned() {
        Element node = mockElementService.node();
        assertEquals(
            OrderedSets.with(
                OrderedSets.singleton(node),
                mockElementService.pendantsFrom(node.getId(), 5),
                mockElementService.loopsOn(node.getId(), 5)
            ),
            elementClient.getElementsWithA(node.getId())
        );
    }

    @Test
    public void getElementsWithATest_aDoesNotExist_shouldReturn404() {
        try {
            elementClient.getElementsWithA(idNonexistent);
            fail();
        } catch(FeignException e) {
            assertExceptionLike(e, 404,idNonexistent + "");
        }
    }

    @Test
    public void getElementsWithBTest_noneExist_shouldReturnEmptyList() {
        long idNode = elementService.createNode();
        assertEquals(
            OrderedSets.empty(),
            elementClient.getElementsWithB(elementService.createPendantTo(idNode))
        );
    }

    @Test
    public void getElementsWithBTest_oneExists_shouldBeReturned() {
        Element node = mockElementService.node();
        assertEquals(
            OrderedSets.singleton(node),
            elementClient.getElementsWithB(node.getId())
        );
    }

    @Test
    public void getElementsWithBTest_manyExist_shouldBeReturned() {
        Element node = mockElementService.node();
        assertEquals(
            OrderedSets.with(
                OrderedSets.singleton(node),
                mockElementService.pendantsTo(node.getId(), 5),
                mockElementService.loopsOn(node.getId(), 5)
            ),
            elementClient.getElementsWithB(node.getId())
        );
    }

    @Test
    public void getElementsWithBTest_aDoesNotExist_shouldReturn404() {
        try {
            elementClient.getElementsWithB(idNonexistent);
            fail();
        } catch(FeignException e) {
            assertExceptionLike(e, 404,idNonexistent + "");
        }
    }

    @Test
    public void getElementsWithAOrBTest_oneAExists_shouldReturnIt() {
        Element pendant = mockElementService.pendantTo(elementService.createNode());
        assertEquals(
            OrderedSets.singleton(pendant),
            elementClient.getElementsWithAOrB(pendant.getId(), idNonexistent)
        );
    }

    @Test
    public void getElementsWithAOrBTest_nAsExists_shouldReturnThem() {
        Element node = mockElementService.node();
        assertEquals(
            OrderedSets.merge(
                node,
                mockElementService.pendantsFrom(node.getId(), 5)
            ),
            elementClient.getElementsWithAOrB(node.getId(), idNonexistent)
        );
    }

    @Test
    public void getElementsWithAOrBTest_oneBExists_shouldReturnIt() {
        Element pendant = mockElementService.pendantFrom(elementService.createNode());
        assertEquals(
            Collections.singleton(pendant),
            elementClient.getElementsWithAOrB(idNonexistent, pendant.getId())
        );
    }

    @Test
    public void getElementsWithAOrBTest_nBsExists_shouldReturnThem() {
        Element node = mockElementService.node();
        assertEquals(
            OrderedSets.merge(
                node,
                mockElementService.pendantsTo(node.getId(), 5)
            ),
            elementClient.getElementsWithAOrB(idNonexistent, node.getId())
        );
    }

    @Test
    public void getElementsWithAOrBTest_nHodgepodgeElementsWithAOrBExist_shouldReturnMatches() {
        long idNode1 = elementService.createNode();
        long idNode2 = elementService.createNode();
        long idEdge = elementService.createElement(idNode1, idNode2);
        long idPendantFrom1 = elementService.createPendantFrom(idNode1);
        long idLoopOn1 = elementService.createLoopOn(idNode1);
        elementService.createPendantTo(idNode1);
        elementService.createPendantFrom(idNode2);
        elementService.createPendantTo(idNode2);
        elementService.createLoopOn(idNode2);
        assertEquals(
            elementService.getElements(
                OrderedSets.with(idNode1, idEdge, idPendantFrom1, idLoopOn1)
            ),
            elementClient.getElementsWithAOrB(idNode1, idEdge)
        );
    }

    @Test
    public void getElementsWithAOrBTest_aAndBSame_noneExist_shouldReturnEmptyList() {
        assertTrue(elementClient.getElementsWithAOrB(idNonexistent, idNonexistent).isEmpty());
    }

    @Test
    public void getElementsWithAOrBTest_aAndBSame_oneExists_shouldReturnIt() {
        Element pendant = mockElementService.pendantFrom(elementService.createNode());
        assertEquals(
            OrderedSets.singleton(pendant),
            elementClient.getElementsWithAOrB(pendant.getId(), pendant.getId())
        );
    }

    @Test
    public void getElementsWithAOrBTest_aAndBSame_manyExist_shouldReturnMany() {
        Element node = mockElementService.node();
        assertEquals(
            OrderedSets.merge(
                node,
                mockElementService.pendantsTo(node.getId(), 5)
            ),
            elementClient.getElementsWithAOrB(node.getId(), node.getId())
        );
    }

    @Test
    public void getElementsWithAAndBTest_noneExist_shouldReturnEmptyList() {
        assertTrue(
            elementClient.getElementsWithAAndB(
                idNonexistentMultiple.get(0),
                idNonexistentMultiple.get(1)
            ).isEmpty()
        );
    }

    @Test
    public void getElementsWithAAndBTest_oneAExists_bDoesNotExist_shouldReturnEmptyList() {
        assertTrue(
            elementClient.getElementsWithAAndB(
                elementService.createPendantTo(elementService.createNode()),
                idNonexistent
            ).isEmpty()
        );
    }

    @Test
    public void getElementsWithAAndBTest_nAsExists_bDoesNotExist_shouldReturnEmptyList() {
        long idNode = elementService.createNode();
        elementService.createPendantsFrom(idNode, 5);
        assertTrue(
            elementClient.getElementsWithAAndB(idNode, idNonexistent).isEmpty()
        );
    }

    @Test
    public void getElementsWithAAndBTest_aDoesNotExist_oneBExists_shouldReturnEmptyList() {
        assertTrue(
            elementClient.getElementsWithAAndB(
                idNonexistent,
                elementService.createPendantFrom(elementService.createNode())
            ).isEmpty()
        );
    }

    @Test
    public void getElementsWithAAndBTest_aDoesNotExist_nBsExists_shouldReturnEmptyList() {
        long idNode = elementService.createNode();
        elementService.createPendantsTo(idNode, 5);
        assertTrue(
            elementClient.getElementsWithAAndB(idNonexistent, idNode).isEmpty()
        );
    }

    @Test
    public void getElementsWithAAndBTest_nHodgepodgeElementsWithAAndBExist_shouldReturnThem() {
        long idNode1 = elementService.createNode();
        long idNode2 = elementService.createNode();
        long idEdge1 = elementService.createElement(idNode1, idNode2);
        long idEdge2 = elementService.createElement(idNode1, idNode2);
        elementService.createPendantFrom(idNode1);
        elementService.createPendantFrom(idNode2);
        elementService.createPendantTo(idNode1);
        elementService.createPendantTo(idNode2);
        elementService.createLoopOn(idNode1);
        elementService.createLoopOn(idNode2);
        assertEquals(
            OrderedSets.with(
                elementService.getElements(
                    OrderedSets.with(idEdge1, idEdge2)
                )
            ),
            elementClient.getElementsWithAAndB(idNode1, idNode2)
        );
    }

    @Test
    public void getElementsWithAAndBTest_aAndBSame_noneExist_shouldReturnEmptyList() {
        long idPendant = elementService.createPendantFrom(elementService.createNode());
        assertTrue(elementClient.getElementsWithAAndB(idPendant, idPendant).isEmpty());
    }

    @Test
    public void getElementsWithAAndBTest_aAndBSame_oneExists_shouldReturnIt() {
        Element node = mockElementService.node();
        assertEquals(
            OrderedSets.singleton(node),
            elementClient.getElementsWithAAndB(node.getId(), node.getId())
        );
    }

    @Test
    public void getElementsWithAAndBTest_aAndBSame_manyExist_shouldReturnThem() {
        Element node = mockElementService.node();
        assertEquals(
            OrderedSets.merge(
                node,
                mockElementService.loopsOn(node.getId(), 5)
            ),
            elementClient.getElementsWithAAndB(node.getId(), node.getId())
        );
    }

    @Test
    public void getNodesTest_noneExist_shouldReturnEmptySet() {
        assertEquals(OrderedSets.empty(), elementClient.getNodes());
    }

    @Test
    public void getNodesTest_oneExists_shouldBeReturned() {
        assertEquals(
            mockElementService.nodes(1),
            elementService.getNodes()
        );
    }

    @Test
    public void getNodesTest_manyExist_shouldBeReturned() {
        assertEquals(
            mockElementService.nodes(5),
            elementService.getNodes()
        );
    }

    @Test
    public void getPendantsFromTest_noneExist_shouldReturnEmptyOrderedSet() {
        assertEquals(
            OrderedSets.empty(),
            elementClient.getPendantsFrom(elementService.createNode())
        );
    }

    @Test
    public void getPendantsFromTest_oneExists_shouldBeReturned() {
        long idNode = elementService.createNode();
        assertEquals(
            mockElementService.pendantsFrom(idNode, 1),
            elementClient.getPendantsFrom(idNode)
        );
    }

    @Test
    public void getPendantsFromTest_manyExist_shouldBeReturned() {
        long idNode = elementService.createNode();
        assertEquals(
            mockElementService.pendantsFrom(idNode, 5),
            elementClient.getPendantsFrom(idNode)
        );
    }

    @Test
    public void getPendantsFromTest_doesNotExist_shouldReturn404() {
        try {
            elementClient.getPendantsFrom(idNonexistent);
            fail();
        } catch(FeignException e) {
            assertExceptionLike(e, 404, idNonexistent + "");
        }
    }

    @Test
    public void getPendantsToTest_noneExist_shouldReturnEmptyOrderedSet() {
        assertEquals(OrderedSets.empty(), elementClient.getPendantsTo(elementService.createNode()));
    }

    @Test
    public void getPendantsToTest_oneExists_shouldBeReturned() {
        long idNode = elementService.createNode();
        assertEquals(
            mockElementService.pendantsTo(idNode, 1),
            elementClient.getPendantsTo(idNode)
        );
    }

    @Test
    public void getPendantsToTest_manyExist_shouldBeReturned() {
        long idNode = elementService.createNode();
        assertEquals(
            mockElementService.pendantsTo(idNode, 5),
            elementClient.getPendantsTo(idNode)
        );
    }

    @Test
    public void getPendantsToTest_doesNotExist_shouldReturn404() {
        try {
            elementClient.getPendantsTo(idNonexistent);
            fail();
        } catch(FeignException e) {
            assertExceptionLike(e, 404, idNonexistent + "");
        }
    }

    @Test
    public void getLoopsOnTest_noneExist_shouldReturnEmptyOrderedSet() {
        assertEquals(
            OrderedSets.empty(),
            elementClient.getLoopsOn(elementService.createNode())
        );
    }

    @Test
    public void getLoopsOnTest_oneExists_shouldBeReturned() {
        long idNode = elementService.createNode();
        assertEquals(
            mockElementService.loopsOn(idNode, 1),
            elementClient.getLoopsOn(idNode)
        );
    }

    @Test
    public void getLoopsOnTest_manyExist_shouldBeReturned() {
        long idNode = elementService.createNode();
        assertEquals(
            mockElementService.loopsOn(idNode, 5),
            elementClient.getLoopsOn(idNode)
        );
    }

    @Test
    public void getLoopsOnTest_doesNotExist_shouldReturn404() {
        try {
            elementClient.getLoopsOn(idNonexistent);
            fail();
        } catch(FeignException e) {
            assertExceptionLike(e, 404, idNonexistent + "");
        }
    }

    @Test
    public void getEndpointsOfTest_noneExist_shouldReturnEmptyOrderedSet() {
        assertEquals(
            OrderedSets.empty(),
            elementClient.getEndpointsOf(elementService.createNode())
        );
    }

    @Test
    public void getEndpointsOfTest_oneExists_shouldBeReturned() {
        long idNode = elementService.createNode();
        assertEquals(
            mockElementService.pendantsFrom(idNode, 1),
            elementClient.getEndpointsOf(idNode)
        );
    }

    @Test
    public void getEndpointsOfTest_manyExist_shouldBeReturned() {
        long idNode1 = elementService.createNode();
        long idNode2 = elementService.createNode();
        assertEquals(
            OrderedSets
                .with(
                    mockElementService.pendantsFrom(idNode1, 5),
                    mockElementService.pendantsTo(idNode1, 5),
                    mockElementService.loopsOn(idNode1, 5),
                    mockElementService.edges(idNode1, idNode2, 2)
                ),
            elementClient.getEndpointsOf(idNode1)
        );
    }

    @Test
    public void getEndpointsOfTest_doesNotExist_shouldReturn404() {
        try {
            elementClient.getEndpointsOf(idNonexistent);
            fail();
        } catch(FeignException e) {
            assertExceptionLike(e, 404, idNonexistent + "");
        }
    }

    @Test
    public void getEndpointsOfForEachTest_emptyOrderedSet_shouldReturnedEmptyOrderedSet() {
        assertTrue(
            elementClient.getEndpointsOfForEach(OrderedSets.empty()).isEmpty()
        );
    }

    @Test
    public void getEndpointsOfForEachTest_singleton_noEndpoints_shouldReturnSingletonWithEmptySet() {
        assertEquals(
            OrderedSets.singleton(Collections.emptySet()),
            elementClient.getEndpointsOfForEach(elementService.createNodes(1))
        );
    }

    @Test
    public void getEndpointsOfForEachTest_singleton_oneEndpoint_shouldReturnSingletonWithSingletonWithEndpoint() {
        long idNode = elementService.createNode();
        assertEquals(
            Collections.singletonList(mockElementService.pendantsFrom(idNode, 1)),
            elementClient.getEndpointsOfForEach(OrderedSets.singleton(idNode))
        );
    }

    @Test
    public void getEndpointsOfForEachTest_singleton_manyEndpoints_shouldReturnSingletonWithEndpoints(){
        long idNode = elementService.createNode();
        assertEquals(
            OrderedSets.singleton(
                OrderedSets.with(
                    mockElementService.pendantsFrom(idNode, 5),
                    mockElementService.pendantsTo(idNode, 5),
                    mockElementService.loopsOn(idNode, 5),
                    OrderedSets.with(
                        mockElementService.edge(idNode, idNode),
                        mockElementService.edge(idNode, idNode)
                    )
                )
            ),
            elementClient.getEndpointsOfForEach(
                OrderedSets.singleton(idNode)
            )
        );
    }

    @Test
    public void getEndpointsOfForEachTest_many_hodgepodge_shouldReturnEndpoints(){
        Element node1 = mockElementService.node();
        Element node2 = mockElementService.node();
        Element node3 = mockElementService.node();
        OrderedSet<Element> pendantsFrom = mockElementService.pendantsFrom(node1.getId(), 3);
        OrderedSet<Element> pendantsTo = mockElementService.pendantsTo(node1.getId(), 3);
        OrderedSet<Element> loopsOn = mockElementService.pendantsTo(node1.getId(), 3);
        OrderedSet<Element> edges = OrderedSets.with(
            mockElementService.edge(node1.getId(), node2.getId()),
            mockElementService.edge(node2.getId(), node1.getId())
        );
        assertEquals(
            Lists.newArrayList(
                OrderedSets.with(pendantsFrom, pendantsTo, loopsOn, edges),
                OrderedSets.with(edges),
                OrderedSets.empty(),
                OrderedSets.empty(), OrderedSets.empty(), OrderedSets.empty(),
                OrderedSets.empty(), OrderedSets.empty(), OrderedSets.empty(),
                OrderedSets.empty(), OrderedSets.empty(), OrderedSets.empty(),
                OrderedSets.empty(), OrderedSets.empty()
            ),
            elementClient.getEndpointsOfForEach(
                OrderedSets
                    .with(
                        OrderedSets.with(node1, node2, node3),
                        pendantsFrom,
                        pendantsTo,
                        loopsOn,
                        edges
                    ).stream()
                    .map(Element::getId)
                    .collect(Collectors.toCollection(OrderedSet::new))
            )
        );
    }

    @Test
    public void createElementTest_aAndBExist_shouldBeCreated() {
        long idNode = elementService.createNode();
        long idElement = elementClient.createElement(
            new CreateElementRequest(idNode, idNode)
        );
        assertEquals(
            new Element(idElement, idNode, idNode),
            elementService.getElement(idElement)
        );
    }

    @Test
    public void createElementTest_aDoesNotExist_shouldReturn400() {
        try {
            elementClient.createElement(
                new CreateElementRequest(idNonexistent, elementService.createNode())
            );
            fail();
        } catch(FeignException e) {
            assertExceptionLike(e, 400, idNonexistent + "");
        }
    }

    @Test
    public void createElementTest_bDoesNotExist_shouldReturn400() {
        try {
            elementClient.createElement(
                new CreateElementRequest(elementService.createNode(), idNonexistent)
            );
            fail();
        } catch(FeignException e) {
            assertExceptionLike(e, 400, idNonexistent + "");
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
        assertEquals(OrderedSets.empty(), elementClient.createElements(Collections.emptyList()));
    }

    @Test
    public void createElementsTest_oneElement_valid_shouldBeCreated() {
        long idNode1 = elementService.createNode();
        long idNode2 = elementService.createNode();
        List<Long> idElements = elementClient.createElements(
            Collections.singletonList(new CreateElementRequest(idNode1, idNode2))
        );
        assertTrue(
            1 == idElements.size() &&
                new Element(
                    idElements.get(0),
                    idNode1,
                    idNode2
                ).equals(
                    elementService.getElement(idElements.get(0))
                )
        );
    }

    @Test
    public void createElementsTest_oneElement_withZeros_nodeShouldBeCreated() {
        OrderedSet<Long> idElements = elementClient.createElements(
            Collections.singletonList(new CreateElementRequest(0, 0))
        );
        assertEquals(1, idElements.size());
        long idElement = idElements.get(0);
        Element element = elementService.getElement(idElement);
        assertEquals(idElement, element.getA());
        assertEquals(idElement, element.getB());
    }

    @Test
    public void createElementsTest_oneElement_aDoesNotExist_shouldReturn400() {
        try {
            elementClient.createElements(
                Collections.singletonList(
                    new CreateElementRequest(idNonexistent, elementService.createNode())
                )
            );
            fail();
        } catch(FeignException e) {
            assertExceptionLike(e, 400, idNonexistent + "");
        }
    }

    @Test
    public void createElementsTest_oneElement_bDoesNotExist_shouldReturn400() {
        try {
            elementClient.createElements(
                Collections.singletonList(
                    new CreateElementRequest(elementService.createNode(), idNonexistent)
                )
            );
            fail();
        } catch(FeignException e) {
            assertExceptionLike(e, 400, idNonexistent + "");
        }
    }

    @Test
    public void createElementsTest_oneElement_aNegative_shouldReturn400() {
        try {
            elementClient.createElements(Collections.singletonList(new CreateElementRequest(0, -1)));
            fail();
        } catch(FeignException e) {
            assertExceptionLike(e, 400);
        }
    }

    @Test
    public void createElementsTest_manyElements_valid_shouldBeCreated() {
        long idNode1 = elementService.createNode();
        long idNode2 = elementService.createNode();
        List<Element> elements = elementService.getElements(
            elementClient.createElements(
                Lists.newArrayList(
                    new CreateElementRequest(idNode1, idNode1),
                    new CreateElementRequest(idNode2, idNode1),
                    new CreateElementRequest(-2, -2),
                    new CreateElementRequest(0, -2)
                )
            )
        );
        assertEquals(4, elements.size());
        long idLoop1 = elements.get(0).getId();
        long idEdge21 = elements.get(1).getId();
        long idNewNode = elements.get(2).getId();
        long idEdgeLoop1NewNode = elements.get(3).getId();
        assertEquals(
            Lists.newArrayList(
                new Element(idLoop1, idNode1, idNode1),
                new Element(idEdge21, idNode2, idNode1),
                new Element(idNewNode, idNewNode, idNewNode),
                new Element(idEdgeLoop1NewNode, idLoop1, idNewNode)
            ),
            elements
        );
    }

    @Test
    public void createElementsTest_manyElements_oneInvalid_shouldReturn400() {
        try {
            elementClient.createElements(
                Lists.newArrayList(
                    new CreateElementRequest(0, 0),
                    new CreateElementRequest(-1, 0),
                    new CreateElementRequest(-3, -3) //Invalid
                )
            );
            fail();
        } catch(FeignException e) {
            assertExceptionLike(e, 400, "-3");
        }
    }

    @Test
    public void createNodeTest_shouldBeCreated() {
        assertTrue(
            elementService.isElementNode(elementClient.createNode())
        );
    }

    @Test
    public void createNodesTest_one_shouldCreateOne() {
        OrderedSet<Long> ids = elementClient.createNodes(1);
        assertEquals(1, ids.size());
        assertTrue(elementService.isElementNode(ids.get(0)));
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
        long idNode = elementService.createNode();
        assertTrue(
            elementService.isElementPendantFrom(
                elementClient.createPendantFrom(idNode),
                idNode
            )
        );
    }

    @Test
    public void createPendantFromTest_idFromDoesNotExist_shouldReturn404() {
        try {
            elementClient.createPendantFrom(idNonexistent);
            fail();
        } catch(FeignException e) {
            assertExceptionLike(e, 404, idNonexistent + "");
        }
    }

    @Test
    public void createPendantsFromTest_negativeHowMany_shouldReturn400() {
        try {
            elementClient.createPendantsFrom(-1, elementService.createNode());
            fail();
        } catch(FeignException e) {
            assertExceptionLike(e, 400);
        }
    }

    @Test
    public void createPendantsFromTest_zeroHowMany_shouldReturn400() {
        try {
            elementClient.createPendantsFrom(0, elementService.createNode());
            fail();
        } catch(FeignException e) {
            assertExceptionLike(e, 400);
        }
    }

    @Test
    public void createPendantsFromTest_oneHowMany_fromExists_shouldCreatePendant() {
        long idNode = elementService.createNode();
        assertEquals(
            Collections.singletonList(true),
            elementService.areElementsPendantsFrom(
                elementClient.createPendantsFrom(1, idNode),
                idNode
            )
        );
    }

    @Test
    public void createPendantsFromTest_oneHowMany_fromDoesNotExist_shouldReturn404() {
        try {
            elementClient.createPendantsFrom(1, idNonexistent);
        } catch(FeignException e) {
            assertExceptionLike(e, 404, idNonexistent + "");
        }
    }

    @Test
    public void createPendantsFromTest_manyHowMany_shouldCreatePendants() {
        long idNode = elementService.createNode();
        assertEquals(
            Collections.nCopies(5, true),
            elementService.areElementsPendantsFrom(
                elementClient.createPendantsFrom(5, idNode),
                idNode
            )
        );
    }

    @Test
    public void createPendantToTest_idToExists_shouldBeCreated() {
        long idNode = elementService.createNode();
        assertTrue(
            elementService.isElementPendantTo(
                elementClient.createPendantTo(idNode),
                idNode
            )
        );
    }

    @Test
    public void createPendantToTest_idToDoesNotExist_shouldReturn404() {
        try {
            elementClient.createPendantTo(idNonexistent);
            fail();
        } catch(FeignException e) {
            assertExceptionLike(e, 404, idNonexistent + "");
        }
    }

    @Test
    public void createPendantsToTest_negativeHowMany_shouldReturn400() {
        try {
            elementClient.createPendantsTo(-1, elementService.createNode());
            fail();
        } catch(FeignException e) {
            assertExceptionLike(e, 400);
        }
    }

    @Test
    public void createPendantsToTest_zeroHowMany_shouldReturn400() {
        try {
            elementClient.createPendantsTo(0, elementService.createNode());
            fail();
        } catch(FeignException e) {
            assertExceptionLike(e, 400);
        }
    }

    @Test
    public void createPendantsToTest_oneHowMany_toExists_shouldCreatePendant() {
        long idNode = elementService.createNode();
        assertEquals(
            Collections.singletonList(true),
            elementService.areElementsPendantsTo(
                elementClient.createPendantsTo(1, idNode),
                idNode
            )
        );
    }

    @Test
    public void createPendantsToTest_oneHowMany_toDoesNotExist_shouldReturn404() {
        try {
            elementClient.createPendantsTo(1, idNonexistent);
        } catch(FeignException e) {
            assertExceptionLike(e, 404, idNonexistent + "");
        }
    }

    @Test
    public void createPendantsToTest_manyHowMany_shouldCreatePendants() {
        long idNode = elementService.createNode();
        assertEquals(
            Collections.nCopies(5, true),
            elementService.areElementsPendantsTo(
                elementClient.createPendantsTo(5, idNode),
                idNode
            )
        );
    }

    @Test
    public void createLoopOnTest_idOnExists_shouldBeCreated() {
        long idNode = elementService.createNode();
        assertTrue(
            elementService.isElementLoopOn(
                elementClient.createLoopOn(idNode),
                idNode
            )
        );
    }

    @Test
    public void createLoopOnTest_idOnDoesNotExist_shouldReturn404() {
        try {
            elementClient.createLoopOn(idNonexistent);
            fail();
        } catch(FeignException e) {
            assertExceptionLike(e, 404, idNonexistent + "");
        }
    }

    @Test
    public void createLoopsOnTest_negativeHowMany_shouldReturn400() {
        try {
            elementClient.createLoopsOn(-1, elementService.createNode());
            fail();
        } catch(FeignException e) {
            assertExceptionLike(e, 400);
        }
    }

    @Test
    public void createLoopsOnTest_zeroHowMany_shouldReturn400() {
        try {
            elementClient.createLoopsOn(0, elementService.createNode());
        } catch(FeignException e) {
            assertExceptionLike(e, 400);
        }
    }

    @Test
    public void createLoopsOnTest_oneHowMany_onExists_shouldCreateLoop() {
        long idNode = elementService.createNode();
        assertEquals(
            Collections.singletonList(true),
            elementService.areElementsLoopsOn(
                elementClient.createLoopsOn(1, idNode),
                idNode
            )
        );
    }

    @Test
    public void createLoopsOnTest_oneHowMany_onDoesNotExist_shouldReturn404() {
        try {
            elementClient.createLoopsOn(1, idNonexistent);
        } catch(FeignException e) {
            assertExceptionLike(e, 404, idNonexistent + "");
        }
    }

    @Test
    public void createLoopsOnTest_manyHowMany_shouldCreateLoops() {
        long idNode = elementService.createNode();
        assertEquals(
            Collections.nCopies(5, true),
            elementService.areElementsLoopsOn(
                elementClient.createLoopsOn(5, idNode),
                idNode
            )
        );
    }

    @Test
    public void updateElementTest_noChange_shouldBeSame() {
        Element pendant = mockElementService.pendantFrom(elementService.createNode());
        elementClient.updateElement(pendant.getId(), new UpdateElementRequest(pendant.getA(), pendant.getB()));
        assertEquals(pendant, elementClient.getElement(pendant.getId()));
    }

    @Test
    public void updateElementTest_change_shouldUpdate() {
        long idNode = elementService.createNode();
        Element loop = mockElementService.loopOn(idNode);
        elementClient.updateElement(loop.getId(), new UpdateElementRequest(idNode, idNode));
        assertEquals(
            new Element(loop.getId(), idNode, idNode),
            elementClient.getElement(loop.getId())
        );
    }

    @Test
    public void updateElementTest_aDoesNotExist_shouldReturn400() {
        long idNode = elementService.createNode();
        try {
            elementClient.updateElement(
                idNode,
                new UpdateElementRequest(idNonexistent, idNode)
            );
            fail();
        } catch(FeignException e) {
            assertExceptionLike(e, 400, idNonexistent + "");
        }
    }

    @Test
    public void updateElementTest_bDoesNotExist_shouldReturn400() {
        long idNode = elementService.createNode();
        try {
            elementClient.updateElement(
                idNode,
                new UpdateElementRequest(idNode, idNonexistent)
            );
            fail();
        } catch(FeignException e) {
            assertExceptionLike(e, 400, idNonexistent + "");
        }
    }

    @Test
    public void updateElementTest_elementDoesNotExist_shouldReturn404() {
        long idNode = elementService.createNode();
        try {
            elementClient.updateElement(
                idNonexistent,
                new UpdateElementRequest(idNode, idNode)
            );
            fail();
        } catch(FeignException e) {
            assertExceptionLike(e, 404, idNonexistent + "");
        }
    }

    @Test
    public void updateElementsTest_idsAndRequestsDifferentSize_shouldReturn400() {
        long idNode1 = elementService.createNode();
        long idNode2 = elementService.createNode();
        try {
            elementClient.updateElements(
                Collections.singletonList(new UpdateElementRequest(idNode1, idNode1)),
                OrderedSets.with(idNode1, idNode2)
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
        long idNode = elementService.createNode();
        Element loop = mockElementService.loopOn(idNode);
        elementClient.updateElements(
            Collections.singletonList(new UpdateElementRequest(idNode, idNode)),
            OrderedSets.singleton(loop.getId())
        );
        assertEquals(
            new Element(loop.getId(), idNode, idNode),
            elementClient.getElement(loop.getId())
        );
    }

    @Test
    public void updateElementsTest_single_aDoesNotExist_shouldReturn400() {
        long idNode = elementService.createNode();
        try {
            elementClient.updateElements(
                Collections.singletonList(new UpdateElementRequest(idNonexistent, idNode)),
                OrderedSets.singleton(idNode)
            );
            fail();
        } catch(FeignException e) {
            assertExceptionLike(e, 400, idNonexistent + "");
        }
    }

    @Test
    public void updateElementsTest_single_bDoesNotExist_shouldReturn400() {
        long idNode = elementService.createNode();
        try {
            elementClient.updateElements(
                Collections.singletonList(new UpdateElementRequest(idNode, idNonexistent)),
                OrderedSets.singleton(idNode)
            );
            fail();
        } catch(FeignException e) {
            assertExceptionLike(e, 400, idNonexistent + "");
        }
    }

    @Test
    public void updateElementsTest_single_elementDoesNotExist_shouldReturn404() {
        long idNode = elementService.createNode();
        try {
            elementClient.updateElement(
                idNonexistent,
                new UpdateElementRequest(idNode, idNode)
            );
            fail();
        } catch(FeignException e) {
            assertExceptionLike(e, 404, idNonexistent + "");
        }
    }

    @Test
    public void updateElementsTest_multiple() {
        long idNode1 = elementService.createNode();
        long idNode2 = elementService.createNode();
        long idPendant = elementService.createPendantFrom(idNode1);
        long idEdge = elementService.createElement(idNode1, idNode2);
        OrderedSet<Long> ids = OrderedSets.with(idNode1, idNode2, idPendant, idEdge);
        elementClient.updateElements(
            Lists.newArrayList(
                new UpdateElementRequest(idNode1, idNode1),
                new UpdateElementRequest(idNode2, idNode2),
                new UpdateElementRequest(idNode2, idNode1),
                new UpdateElementRequest(idEdge, idEdge)
            ),
            ids
        );
        assertEquals(
            OrderedSets.with(
                new Element(idNode1, idNode1, idNode1),
                new Element(idNode2, idNode2, idNode2),
                new Element(idPendant, idNode2, idNode1),
                new Element(idEdge, idEdge, idEdge)
            ),
            elementService.getElements(ids)
        );
    }

    @Test
    public void deleteElementTest_exists_shouldNoLonger() {
        long idNode = elementService.createNode();
        elementClient.deleteElement(idNode);
        assertFalse(elementService.doesElementExist(idNode));
    }

    @Test
    public void deleteElementTest_doesNotExist_shouldDoNothing() {
        elementClient.deleteElement(idNonexistent);
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
        }
    }

    @Test
    public void deleteElementTest_isStandardCategory_shouldReturn400() {
        try {
            elementClient.deleteElement(Categories.CATEGORY);
            fail();
        } catch(FeignException e) {
            assertExceptionLike(e, 400, Categories.CATEGORY + "");
        }
    }

    @Test
    public void deleteElementsTest_emptySet_shouldDoNothing() {
        elementClient.deleteElements(Collections.emptySet());
    }

    @Test
    public void deleteElementsTest_singleton_exists_shouldNoLonger() {
        long idNode = elementService.createNode();
        elementClient.deleteElements(Collections.singleton(idNode));
        assertFalse(elementService.doesElementExist(idNode));
    }

    @Test
    public void deleteElementsTest_singleton_doesNotExist_shouldDoNothing() {
        elementClient.deleteElements(Collections.singleton(idNonexistent));
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
        }
    }

    @Test
    public void deleteElementsTest_singleton_isStandardCategory_shouldReturn400() {
        try {
            elementClient.deleteElements(Collections.singleton(Categories.CATEGORY));
            fail();
        } catch(FeignException e) {
            assertExceptionLike(e, 400, Categories.CATEGORY + "");
        }
    }

    @Test
    public void deleteElementsTest_multiple() {
        Set<Long> ids = OrderedSets.with(idNonexistentMultiple, elementService.createNodes(5));
        elementClient.deleteElements(ids);
        assertFalse(elementService.doAnyElementsExist(ids));
    }
}

package com.beumuth.collections.core.set;

import com.beumuth.collections.client.set.SetClient;
import com.beumuth.collections.core.client.ClientService;
import com.beumuth.collections.core.element.ElementService;
import com.beumuth.collections.core.setelement.SetElementService;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import feign.FeignException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SetTests {

    @Autowired
    private SetService setService;
    @Autowired
    private SetElementService setElementService;
    @Autowired
    private ElementService elementService;
    @Autowired
    private ClientService clientService;

    private SetClient setClient;

    @Before
    public void setupTests() {
        setClient = clientService.getClient(SetClient.class);
    }

    @Test
    public void doesSetExistTest_shouldReturnTrue() {
        long idSet = setService.createSet();
        try {
            Assert.assertTrue(setClient.doesSetExist(idSet));
        } finally {
            setService.deleteSet(idSet);
        }
    }

    @Test
    public void doesSetExistTest_shouldReturnFalse() {
        long idSet = setService.createSet();
        setService.deleteSet(idSet);
        Assert.assertFalse(setClient.doesSetExist(idSet));
    }

    @Test
    public void getSetTest_shouldReturnSet() {
        long idSet = setService.createSet();
        try {
            Assert.assertEquals(idSet, setClient.getSet(idSet).getId());
        } finally {
            setService.deleteSet(idSet);
        }
    }

    @Test
    public void getSetTest_shouldReturn404() {
        long idSet = setService.createSet();
        setService.deleteSet(idSet);
        try {
            setClient.getSet(idSet);
            Assert.fail();
        } catch(FeignException e) {
            Assert.assertEquals(404, e.status());
        }
    }

    @Test
    public void getSetElements_nonEmptySet_shouldReturnElements() {
        Set<Long> idSetElements = Sets.newHashSet(elementService.createMultipleElements(3));
        long idSet = setService.createSetWithElements(idSetElements);
        try {
            Assert.assertEquals(idSetElements, setClient.getSetElements(idSet));
        } finally {
            elementService.deleteMultipleElements(idSetElements);
            setService.deleteSet(idSet);
        }
    }

    @Test
    public void getSetElements_emptySet_shouldReturnNoElements() {
        long idSet = setService.createEmptySet();
        try {
            Assert.assertEquals(Collections.EMPTY_SET, setClient.getSetElements(idSet));
        } finally {
            setService.deleteSet(idSet);
        }
    }

    @Test
    public void createSetTest_shouldCreateEmptySet() {
        long idSet = setClient.createSet();
        try {
            Assert.assertEquals(Collections.EMPTY_SET, setService.getSetElements(idSet));
        } finally {
            setService.deleteSet(idSet);
        }
    }

    @Test
    public void  createSetWithElementsTest_shouldCreateSetWithElements() {
        Set<Long> idElements = Sets.newHashSet(elementService.createMultipleElements(3));
        long idSet = setClient.createSetWithElements(idElements);
        try {
            Assert.assertEquals(idElements, setService.getSetElements(idSet));
        } finally {
            setService.deleteSet(idSet);
            elementService.deleteMultipleElements(idElements);
        }
    }

    @Test
    public void createSetWithElementsTest_noElements_shouldCreateEmptySet() {
        long idSet = setClient.createSetWithElements(Collections.emptySet());
        try {
            Assert.assertEquals(setService.getSetElements(idSet), Collections.EMPTY_SET);
        } finally {
            setService.deleteSet(idSet);
        }
    }

    @Test
    public void createSetWithElementsTest_oneElementDoesNotExist_shouldReturn400() {
        long idNonExistentElement = elementService.createElement();
        elementService.deleteElement(idNonExistentElement);
        Set<Long> idExistingElements = Sets.newHashSet(elementService.createMultipleElements(3));
        Set<Long> idElements = Sets.newHashSet(idExistingElements);
        idElements.add(idNonExistentElement);
        try {
            setClient.createSetWithElements(idElements);
            Assert.fail();
        } catch(FeignException e) {
            Assert.assertEquals(400, e.status());
            Assert.assertTrue(
        "Response body [" + e.contentUTF8() + "] should contain the ids of the nonexistent Element [" +
                    idNonExistentElement + "]",
                e.contentUTF8().contains(idNonExistentElement + "")
            );
        } finally {
            elementService.deleteMultipleElements(idExistingElements);
        }
    }

    @Test
    public void createSetWithElementsTest_multipleElementsDoNotExist_shouldReturn400() {
        Set<Long> idNonExistentElements = Sets.newHashSet(elementService.createMultipleElements(3));
        elementService.deleteMultipleElements(idNonExistentElements);
        Set<Long> idExistingElements = Sets.newHashSet(elementService.createMultipleElements(3));
        Set<Long> idElements = Sets.union(idNonExistentElements, idExistingElements);
        try {
            setClient.createSetWithElements(idElements);
            Assert.fail();
        } catch(FeignException e) {
            Assert.assertEquals(400, e.status());
            for(long idElement : idNonExistentElements) {
                Assert.assertTrue(
            "Response body [" + e.contentUTF8() + "] should contain the id of the nonexistent Element [" +
                        idElement + "]",
                    e.contentUTF8().contains(idElement + "")
                );
            }
        } finally {
            elementService.deleteMultipleElements(idExistingElements);
        }
    }

    @Test
    public void copySetTest_setWithElements_shouldBeCopied() {
        Set<Long> idElements = Sets.newHashSet(elementService.createMultipleElements(3));
        long idSet = setService.createSetWithElements(idElements);
        try {
            long idCopiedSet = setClient.copySet(idSet);
            Assert.assertTrue(setService.areEqual(idSet, idCopiedSet));
            Assert.assertNotEquals(idSet, idCopiedSet);
            setService.deleteSet(idCopiedSet);
        } finally {
            setService.deleteSet(idSet);
            elementService.deleteMultipleElements(idElements);
        }
    }

    @Test
    public void copySetTest_emptySet_shouldBeCopied() {
        long idSet = setService.createEmptySet();
        try {
            long idCopiedSet = setClient.copySet(idSet);
            Assert.assertTrue(setService.areEqual(idSet, idCopiedSet));
            Assert.assertNotEquals(idSet, idCopiedSet);
            setService.deleteSet(idCopiedSet);
        } finally {
            setService.deleteSet(idSet);
        }
    }

    @Test
    public void copySetTest_nonExistentSet_shouldReturn404() {
        long idSet = setService.createSet();
        setService.deleteSet(idSet);
        try {
            setClient.copySet(idSet);
        } catch(FeignException e) {
            Assert.assertEquals(404, e.status());
            Assert.assertTrue(
        "Response body [" + e.contentUTF8() + "] should contain the id of the nonexistent Set [" +
                    idSet + "]",
                e.contentUTF8().contains(idSet + "")
            );
        }
    }

    @Test
    public void deleteSetTest_setSetElementAndElement_shouldBeDeleted() {
        Set<Long> idElements = Sets.newHashSet(elementService.createMultipleElements(3));
        long idSet = setService.createSetWithElements(idElements);
        try {
            com.beumuth.collections.client.set.Set set = setService.getSet(idSet).get();
            Set<Long> idSetElements = Sets.newHashSet();
            for(Long idElement : idElements) {
                idSetElements.add(setElementService.getSetElementByNaturalKey(idSet, idElement).get().getId());
            }

            setClient.deleteSet(set.getId());

            Assert.assertFalse(setService.doesSetExist(set.getId()));
            Assert.assertFalse(elementService.doesElementExist(set.getIdElement()));
            for(Long idSetElement : idSetElements) {
                Assert.assertFalse(setElementService.doesSetElementExist(idSetElement));
            }
        } finally {
            setService.deleteSet(idSet);
            elementService.deleteMultipleElements(idElements);
        }
    }

    @Test
    public void deleteSetTest_withNonExistingSet_shouldDoNothing() {
        long idSet = setService.createSet();
        setService.deleteSet(idSet);
        setClient.deleteSet(idSet); //This should not throw an exception
    }

    @Test
    public void doesSetContainElementTest_doesContainElement_shouldReturnTrue() {
        long idElement = elementService.createElement();
        long idSet = setService.createSetWithElements(Sets.newHashSet(idElement));
        try {
            Assert.assertTrue(setClient.doesSetContainElement(idSet, idElement));
        } finally {
            elementService.deleteElement(idElement);
            setService.deleteSet(idSet);
        }
    }

    @Test
    public void doesSetContainElementTest_doesNotContainElement_shouldReturnFalse() {
        long idElement = elementService.createElement();
        long idSet = setService.createSetWithElements(Sets.newHashSet(idElement));
        setService.removeElementFromSet(idSet, idElement);
        try {
            Assert.assertFalse(setClient.doesSetContainElement(idSet, idElement));
        } finally {
            elementService.deleteElement(idElement);
            setService.deleteSet(idSet);
        }
    }

    @Test
    public void doesSetContainElementTest_setDoesNotExist_shouldReturn404() {
        long idElement = elementService.createElement();
        long idSet = setService.createSet();
        setService.deleteSet(idSet);
        try {
            setClient.doesSetContainElement(idSet, idElement);
            Assert.fail();
        } catch(FeignException e) {
            Assert.assertEquals(404, e.status());
            Assert.assertTrue(
                "Response body [" + e.contentUTF8() + "] should contain the id of the nonexistent Set [" +
                    idSet + "]",
                e.contentUTF8().contains(idSet + "")
            );
        } finally {
            elementService.deleteElement(idElement);
        }
    }

    @Test
    public void doesSetContainElementTest_elementDoesNotExist_shouldReturn404() {
        long idElement = elementService.createElement();
        long idSet = setService.createSet();
        elementService.deleteElement(idElement);
        try {
            setClient.doesSetContainElement(idSet, idElement);
            Assert.fail();
        } catch(FeignException e) {
            Assert.assertEquals(404, e.status());
            Assert.assertTrue(
                "Response body [" + e.contentUTF8() + "] should contain the id of the nonexistent Element [" +
                    idElement + "]",
                e.contentUTF8().contains(idElement + "")
            );
        } finally {
            setService.deleteSet(idSet);
        }
    }

    @Test
    public void doesSetContainElements_doesContainElements_shouldReturnTrue() {
        Set<Long> idElements = Sets.newHashSet(elementService.createMultipleElements(3));
        long idSet = setService.createSetWithElements(idElements);
        try {
            Assert.assertTrue(setClient.doesSetContainElements(idSet, idElements));
        } finally {
            setService.deleteSet(idSet);
            elementService.deleteMultipleElements(idElements);
        }
    }

    @Test
    public void doesSetContainElements_doesNotContainAnElement_shouldReturnFalse() {
        Set<Long> idElementsInSet = Sets.newHashSet(elementService.createMultipleElements(3));
        long idElementNotInSet = elementService.createElement();
        long idSet = setService.createSetWithElements(idElementsInSet);
        Set<Long> idAllElements = Sets.newHashSet(idElementsInSet);
        idAllElements.add(idElementNotInSet);
        try {
            Assert.assertFalse(setClient.doesSetContainElements(idSet, idAllElements));
        } finally {
            setService.deleteSet(idSet);
            elementService.deleteMultipleElements(idAllElements);
        }
    }

    @Test
    public void doesSetContainElements_setDoesNotExist_shouldReturn404() {
        Set<Long> idElements = Sets.newHashSet(elementService.createMultipleElements(3));
        long idSet = setService.createSet();
        setService.deleteSet(idSet);
        try {
            setClient.doesSetContainElements(idSet, idElements);
        } catch(FeignException e) {
            Assert.assertEquals(404, e.status());
            Assert.assertTrue(
            "Response body [" + e.contentUTF8() + "] should contain the id of the nonexistent Set [" +
                    idSet + "]",
                e.contentUTF8().contains(idSet + "")
            );
        } finally {
            elementService.deleteMultipleElements(idElements);
        }
    }

    @Test
    public void doesSetContainElements_elementsDoNotExist_shouldReturn404() {
        Set<Long> idElements = Sets.newHashSet(elementService.createMultipleElements(3));
        Set<Long> idNonExistingElements = Sets.newHashSet(elementService.createMultipleElements(2));
        elementService.deleteMultipleElements(idNonExistingElements);
        Set<Long> idAllElements = Sets.union(idElements, idNonExistingElements);
        long idSet = setService.createSetWithElements(idElements);
        try {
            setClient.doesSetContainElements(idSet, idAllElements);
        } catch(FeignException e) {
            Assert.assertEquals(404, e.status());
            for(Long idElement : idNonExistingElements) {
                Assert.assertTrue(
            "Response body [" + e.contentUTF8() + "] should contain the id of the nonexistent Element [" +
                        idElement + "]",
                    e.contentUTF8().contains(idElement + "")
                );
            }
        } finally {
            elementService.deleteMultipleElements(idElements);
        }
    }

    @Test
    public void addElementToSetTest_shouldBeAdded() {
        long idSet = setService.createSet();
        long idElement = elementService.createElement();
        try {
            setClient.addElementToSet(idSet, idElement);
            Assert.assertTrue(setService.containsElement(idSet, idElement));
        } finally {
            setService.deleteSet(idSet);
            elementService.deleteElement(idElement);
        }
    }

    @Test
    public void addElementToSetTest_setDoesNotExist_shouldReturn404() {
        long idSet = setService.createSet();
        setService.deleteSet(idSet);
        long idElement = elementService.createElement();
        try {
            setClient.addElementToSet(idSet, idElement);
            Assert.fail();
        } catch(FeignException e) {
            Assert.assertEquals(404, e.status());
            Assert.assertTrue(
        "Response body [" + e.contentUTF8() + "] should contain the id of the nonexistent Set [" + idSet + "]",
                e.contentUTF8().contains(idSet + "")
            );
        } finally {
            elementService.deleteElement(idElement);
        }
    }

    @Test
    public void addElementToSetTest_elementDoesNotExist_shouldReturn404() {
        long idSet = setService.createSet();
        long idElement = elementService.createElement();
        elementService.deleteElement(idElement);
        try {
            setClient.addElementToSet(idSet, idElement);
            Assert.fail();
        } catch(FeignException e) {
            Assert.assertEquals(404, e.status());
            Assert.assertTrue(
        "Response body [" + e.contentUTF8() + "] should contain the id of the nonexistent Element [" +
                    idElement + "]",
                e.contentUTF8().contains(idElement + "")
            );
        } finally {
            setService.deleteSet(idSet);
        }
    }

    @Test
    public void createAndAddElementToSetTest_shouldSucceed() {
        long idSet = setService.createSet();
        try {
            long idElement = setClient.createAndAddElementToSet(idSet);
            try {
                Assert.assertTrue(setService.containsElement(idSet, idElement));
            } finally {
                elementService.deleteElement(idElement);
            }
        } finally {
            setService.deleteSet(idSet);
        }
    }

    @Test
    public void createAndAddElementToSetTest_setDoesNotExist_shouldReturn404() {
        long idSet = setService.createSet();
        setService.deleteSet(idSet);
        try {
            setClient.createAndAddElementToSet(idSet);
            Assert.fail();
        } catch(FeignException e) {
            Assert.assertEquals(404, e.status());
            Assert.assertTrue(
        "Response body [" + e.contentUTF8() + "] should contain the id of the nonexistent set [" + idSet + "]",
                e.contentUTF8().contains(idSet + "")
            );
        }
    }

    @Test
    public void removeElementFromSetTest_elementInSet_shouldRemoveElement() {
        long idElement = elementService.createElement();
        long idSet = setService.createSetWithElements(Sets.newHashSet(idElement));
        try {
            setClient.removeElementFromSet(idSet, idElement);
            Assert.assertFalse(setService.containsElement(idSet, idElement));
        } finally {
            elementService.deleteElement(idElement);
            setService.deleteSet(idSet);
        }
    }

    @Test
    public void removeElementFromSetTest_elementNotInSet_shouldDoNothing() {
        long idElement = elementService.createElement();
        long idSet = setService.createSet();
        try {
            setClient.removeElementFromSet(idSet, idElement);
        } finally {
            elementService.deleteElement(idElement);
            setService.deleteSet(idSet);
        }
    }

    @Test
    public void removeElementFromSetTest_setDoesNotExist_shouldReturn404() {
        long idElement = elementService.createElement();
        long idSet = setService.createSet();
        setService.deleteSet(idSet);
        try {
            setClient.removeElementFromSet(idSet, idElement);
            Assert.fail();
        } catch(FeignException e) {
            Assert.assertEquals(404, e.status());
            Assert.assertTrue(
        "Response body [" + e.contentUTF8() + "] should contain the id of the nonexistent Set [" + idSet + "]",
                e.contentUTF8().contains(idSet + "")
            );
        }
    }

    @Test
    public void removeElementFromSetTest_elementDoesNotExist_shouldReturn404() {
        long idElement = elementService.createElement();
        long idSet = setService.createSet();
        elementService.deleteElement(idElement);
        try {
            setClient.removeElementFromSet(idSet, idElement);
            Assert.fail();
        } catch(FeignException e) {
            Assert.assertEquals(404, e.status());
            Assert.assertTrue(
        "Response body [" + e.contentUTF8() + "] should contain the id of the nonexistent Element [" +
                    idElement + "]",
                e.contentUTF8().contains(idElement + "")
            );
        }
    }

    @Test
    public void equalsTest_setsContainSameElements_shouldReturnTrue() {
        Set<Long> idElements = Sets.newHashSet(elementService.createMultipleElements(3));
        long idSetA = setService.createSetWithElements(idElements);
        long idSetB = setService.copySet(idSetA);
        try {
            Assert.assertTrue(setClient.areEqual(idSetA, idSetB));
        } finally {
            setService.deleteSet(idSetA);
            setService.deleteSet(idSetB);
            elementService.deleteMultipleElements(idElements);
        }
    }

    @Test
    public void equalsTest_setsDoNotContainSameElements_shouldReturnFalse() {
        Set<Long> idElementsA = Sets.newHashSet(elementService.createMultipleElements(3));
        Set<Long> idElementsB = Sets.newHashSet(elementService.createMultipleElements(4));
        long idSetA = setService.createSetWithElements(idElementsA);
        long idSetB = setService.createSetWithElements(idElementsB);
        try {
            Assert.assertFalse(setClient.areEqual(idSetA, idSetB));
        } finally {
            setService.deleteSet(idSetA);
            setService.deleteSet(idSetB);
            elementService.deleteMultipleElements(idElementsA);
            elementService.deleteMultipleElements(idElementsB);
        }
    }

    @Test
    public void equalsTest_sameSetId_shouldReturnTrue() {
        Set<Long> idElements = Sets.newHashSet(elementService.createMultipleElements(3));
        long idSet = setService.createSetWithElements(idElements);
        try {
            Assert.assertTrue(setClient.areEqual(idSet, idSet));
        } finally {
            setService.deleteSet(idSet);
            elementService.deleteMultipleElements(idElements);
        }
    }

    @Test
    public void equalsTest_twoEmptySets_shouldReturnTrue() {
        long idSetA = setService.createEmptySet();
        long idSetB = setService.createEmptySet();
        try {
            Assert.assertTrue(setClient.areEqual(idSetA, idSetB));
        } finally {
            setService.deleteSet(idSetA);
            setService.deleteSet(idSetB);
        }
    }

    @Test
    public void equalsTest_firstSetDoesNotExist_shouldReturn404() {
        long idSetA = setService.createSet();
        setService.deleteSet(idSetA);
        long idSetB = setService.createSet();
        try {
            setClient.areEqual(idSetA, idSetB);
        } catch(FeignException e) {
            Assert.assertEquals(404, e.status());
            Assert.assertTrue(
        "Response body [" + e.contentUTF8() + "] should contain the id of the nonexistent Set [" + idSetA + "]",
                e.contentUTF8().contains(idSetA + "")
            );
        } finally {
            setService.deleteSet(idSetB);
        }
    }

    @Test
    public void equalsTest_secondSetDoesNotExist_shouldReturn404() {
        long idSetA = setService.createSet();
        long idSetB = setService.createSet();
        setService.deleteSet(idSetB);
        try {
            setClient.areEqual(idSetA, idSetB);
        } catch(FeignException e) {
            Assert.assertEquals(404, e.status());
            Assert.assertTrue(
        "Response body [" + e.contentUTF8() + "] should contain the id of the nonexistent Set [" + idSetB + "]",
                e.contentUTF8().contains(idSetB + "")
            );
        } finally {
            setService.deleteSet(idSetA);
        }
    }

    @Test
    public void isSubsetTest_setAIsSubsetOfSetB_shouldReturnTrue() {
        Set<Long> idElementsA = Sets.newHashSet(elementService.createMultipleElements(3));
        Set<Long> idElementsB = Sets.union(
            Sets.newHashSet(elementService.createMultipleElements(2)),
            idElementsA
        );
        long idSetA = setService.createSetWithElements(idElementsA);
        long idSetB = setService.createSetWithElements(idElementsB);
        try {
            Assert.assertTrue(setClient.isSubset(idSetA, idSetB));
        } finally {
            setService.deleteSet(idSetA);
            setService.deleteSet(idSetB);
            elementService.deleteMultipleElements(Sets.union(idElementsA, idElementsB));
        }
    }

    @Test
    public void isSubsetTest_setAIsNotSubsetOfSetB_shouldReturnTrue() {
        Set<Long> idElementsB = Sets.newHashSet(elementService.createMultipleElements(3));
        Set<Long> idElementsA = Sets.union(
            Sets.newHashSet(elementService.createMultipleElements(2)),
            idElementsB
        );
        long idSetA = setService.createSetWithElements(idElementsA);
        long idSetB = setService.createSetWithElements(idElementsB);
        try {
            Assert.assertFalse(setClient.isSubset(idSetA, idSetB));
        } finally {
            setService.deleteSet(idSetA);
            setService.deleteSet(idSetB);
            elementService.deleteMultipleElements(Sets.union(idElementsA, idElementsB));
        }
    }

    @Test
    public void isSubsetTest_setAIsEmptySet_shouldReturnTrue() {
        long idSetA = setService.createEmptySet();
        Set<Long> idElementsB = Sets.newHashSet(elementService.createMultipleElements(3));
        long idSetB = setService.createSetWithElements(idElementsB);
        try {
            Assert.assertTrue(setClient.isSubset(idSetA, idSetB));
        } finally {
            setService.deleteSet(idSetA);
            setService.deleteSet(idSetB);
            elementService.deleteMultipleElements(idElementsB);
        }
    }

    @Test
    public void isSubsetTest_setAIsNonEmptySetBIsEmpty_shouldReturnFalse() {
        Set<Long> idElementsA = Sets.newHashSet(elementService.createMultipleElements(3));
        long idSetA = setService.createSetWithElements(idElementsA);
        long idSetB = setService.createEmptySet();
        try {
            Assert.assertFalse(setClient.isSubset(idSetA, idSetB));
        } finally {
            setService.deleteSet(idSetA);
            setService.deleteSet(idSetB);
            elementService.deleteMultipleElements(idElementsA);
        }
    }

    @Test
    public void isSubsetTest_setAAndSetBAreEmpty_shouldReturnTrue() {
        long idSetA = setService.createEmptySet();
        long idSetB = setService.createEmptySet();
        try {
            Assert.assertTrue(setClient.isSubset(idSetA, idSetB));
        } finally {
            setService.deleteSet(idSetA);
            setService.deleteSet(idSetB);
        }
    }

    @Test
    public void isSubsetTest_setAEqualsSetB_shouldReturnTrue() {
        Set<Long> idElementsA = Sets.newHashSet(elementService.createMultipleElements(3));
        long idSetA = setService.createSetWithElements(idElementsA);
        long idSetB = setService.copySet(idSetA);
        try {
            Assert.assertTrue(setClient.isSubset(idSetA, idSetB));
        } finally {
            setService.deleteSet(idSetA);
            setService.deleteSet(idSetB);
            elementService.deleteMultipleElements(idElementsA);
        }
    }

    @Test
    public void isSubsetTest_setADoesNotExist_shouldReturn404() {
        long idSetA = setService.createSet();
        setService.deleteSet(idSetA);
        long idSetB = setService.createSet();
        try {
            setClient.isSubset(idSetA, idSetB);
        } catch(FeignException e) {
            Assert.assertEquals(404, e.status());
            Assert.assertTrue(
        "Response body [" + e.contentUTF8() + "] should contain the id of the nonexistent Set [" + idSetA + "]",
                e.contentUTF8().contains(idSetA + "")
            );
        } finally {
            setService.deleteSet(idSetB);
        }
    }

    @Test
    public void isSubsetTest_setBDoesNotExist_shouldReturn404() {
        long idSetA = setService.createSet();
        long idSetB = setService.createSet();
        setService.deleteSet(idSetB);
        try {
            setClient.isSubset(idSetA, idSetB);
        } catch(FeignException e) {
            Assert.assertEquals(404, e.status());
            Assert.assertTrue(
        "Response body [" + e.contentUTF8() + "] should contain the id of the nonexistent Set [" + idSetB + "]",
                e.contentUTF8().contains(idSetB + "")
            );
        } finally {
            setService.deleteSet(idSetA);
        }
    }

    @Test
    public void setCardinalityTest_nonEmptySet_shouldReturnSize() {
        int numElements = 5;
        Set<Long> idElements = Sets.newHashSet(elementService.createMultipleElements(numElements));
        long idSet = setService.createSetWithElements(idElements);
        try {
            Assert.assertEquals(numElements, setClient.setCardinality(idSet));
        } finally {
            setService.deleteSet(idSet);
            elementService.deleteMultipleElements(idElements);
        }
    }

    @Test
    public void setCardinalityTest_emptySet_shouldReturnZero() {
        long idSet = setService.createEmptySet();
        try {
            Assert.assertEquals(0, setClient.setCardinality(idSet));
        } finally {
            setService.deleteSet(idSet);
        }
    }

    @Test
    public void setCardinalityTest_nonExistentSet_shouldReturn404() {
        long idSet = setService.createSet();
        setService.deleteSet(idSet);
        try {
            setClient.setCardinality(idSet);
            Assert.fail();
        } catch(FeignException e) {
            Assert.assertEquals(404, e.status());
            Assert.assertTrue(
        "Response body [" + e.contentUTF8() + "] should contain the id of the nonexistent Set [" + idSet + "]",
                e.contentUTF8().contains(idSet + "")
            );
        }
    }

    @Test
    public void isEmptySetTest_setIsEmpty_shouldReturnTrue() {
        long idSet = setService.createEmptySet();
        try {
            Assert.assertTrue(setClient.isEmptySet(idSet));
        } finally {
            setService.deleteSet(idSet);
        }
    }

    @Test
    public void isEmptySetTest_setIsNotEmpty_shouldReturnFalse() {
        Set<Long> idElements = Sets.newHashSet(elementService.createMultipleElements(3));
        long idSet = setService.createSetWithElements(idElements);
        try {
            Assert.assertFalse(setClient.isEmptySet(idSet));
        } finally {
            setService.deleteSet(idSet);
            elementService.deleteMultipleElements(idElements);
        }
    }

    @Test
    public void isEmptySetTest_setDoesNotExist_shouldReturn404() {
        long idSet = setService.createSet();
        setService.deleteSet(idSet);
        try {
            setClient.isEmptySet(idSet);
            Assert.fail();
        } catch(FeignException e) {
            Assert.assertEquals(404, e.status());
            Assert.assertTrue(
        "Response body [" + e.contentUTF8() + "] should contain the id of the nonexistent Set [" + idSet + "]",
                e.contentUTF8().contains(idSet + "")
            );
        }
    }

    @Test
    public void intersectionTest_setAAndSetBShareSomeCommonElements_shouldReturnIntersection() {
        List<Long> idAllElements = elementService.createMultipleElements(12);
        Set<Long> idCommonElements = Sets.newHashSet(idAllElements.subList(3, 6));
        long idSetA = setService.createSetWithElements(
            Sets.newHashSet(idAllElements.subList(0, 6))
        );
        long idSetB = setService.createSetWithElements(
            Sets.newHashSet(idAllElements.subList(3, idAllElements.size()))
        );
        try {
            long idIntersectionSet = setClient.intersection(idSetA, idSetB);
            Assert.assertNotEquals(idIntersectionSet, idSetA);
            Assert.assertNotEquals(idIntersectionSet, idSetB);
            Assert.assertEquals(idCommonElements, setService.getSetElements(idIntersectionSet));
            setService.deleteSet(idIntersectionSet);
        } finally {
            setService.deleteSet(idSetA);
            setService.deleteSet(idSetB);
            elementService.deleteMultipleElements(Sets.newHashSet(idAllElements));
        }
    }

    @Test
    public void intersectionTest_setAAndSetBShareNoCommonElements_shouldReturnEmptySet() {
        Set<Long> idElementsA = Sets.newHashSet(elementService.createMultipleElements(5));
        Set<Long> idElementsB = Sets.newHashSet(elementService.createMultipleElements(6));
        long idSetA = setService.createSetWithElements(idElementsA);
        long idSetB = setService.createSetWithElements(idElementsB);
        try {
            long idIntersectionSet = setClient.intersection(idSetA, idSetB);
            Assert.assertNotEquals(idIntersectionSet, idSetA);
            Assert.assertNotEquals(idIntersectionSet, idSetB);
            Assert.assertTrue(setService.isEmpty(idIntersectionSet));
            setService.deleteSet(idIntersectionSet);
        } finally {
            setService.deleteSet(idSetA);
            setService.deleteSet(idSetB);
            elementService.deleteMultipleElements(idElementsA);
            elementService.deleteMultipleElements(idElementsB);
        }
    }

    @Test
    public void intersectionTest_setAAndSetBShareSameElements_shouldReturnSameElements() {
        Set<Long> idElements = Sets.newHashSet(elementService.createMultipleElements(5));
        long idSetA = setService.createSetWithElements(idElements);
        long idSetB = setService.createSetWithElements(idElements);
        try {
            long idIntersectionSet = setClient.intersection(idSetA, idSetB);
            Assert.assertNotEquals(idIntersectionSet, idSetA);
            Assert.assertNotEquals(idIntersectionSet, idSetB);
            Assert.assertTrue(setService.areEqual(idSetA, idIntersectionSet));
            Assert.assertTrue(setService.areEqual(idSetB, idIntersectionSet));
            setService.deleteSet(idIntersectionSet);
        } finally {
            setService.deleteSet(idSetA);
            setService.deleteSet(idSetB);
            elementService.deleteMultipleElements(idElements);
        }
    }

    @Test
    public void intersectionTest_setAIsSetB_shouldReturnCopiedSet() {
        Set<Long> idElements = Sets.newHashSet(elementService.createMultipleElements(5));
        long idSet = setService.createSetWithElements(idElements);
        try {
            long idIntersectionSet = setClient.intersection(idSet, idSet);
            Assert.assertNotEquals(idIntersectionSet, idSet);
            Assert.assertTrue(setService.areEqual(idSet, idIntersectionSet));
            setService.deleteSet(idIntersectionSet);
        } finally {
            setService.deleteSet(idSet);
            elementService.deleteMultipleElements(idElements);
        }
    }

    @Test
    public void intersectionTest_setAIsEmptySet_shouldReturnEmptySet() {
        Set<Long> idElementsA = Sets.newHashSet(elementService.createMultipleElements(5));
        long idSetA = setService.createSetWithElements(idElementsA);
        long idSetB = setService.createEmptySet();
        try {
            long idIntersectionSet = setClient.intersection(idSetA, idSetB);
            Assert.assertNotEquals(idIntersectionSet, idSetA);
            Assert.assertNotEquals(idIntersectionSet, idSetB);
            Assert.assertTrue(setService.isEmpty(idIntersectionSet));
            setService.deleteSet(idIntersectionSet);
        } finally {
            setService.deleteSet(idSetA);
            setService.deleteSet(idSetB);
            elementService.deleteMultipleElements(idElementsA);
        }
    }

    @Test
    public void intersectionTest_setBIsEmptySet_shouldReturnEmptySet() {
        Set<Long> idElementsB = Sets.newHashSet(elementService.createMultipleElements(5));
        long idSetA = setService.createEmptySet();
        long idSetB = setService.createSetWithElements(idElementsB);
        try {
            long idIntersectionSet = setClient.intersection(idSetA, idSetB);
            Assert.assertNotEquals(idIntersectionSet, idSetA);
            Assert.assertNotEquals(idIntersectionSet, idSetB);
            Assert.assertTrue(setService.isEmpty(idIntersectionSet));
            setService.deleteSet(idIntersectionSet);
        } finally {
            setService.deleteSet(idSetA);
            setService.deleteSet(idSetB);
            elementService.deleteMultipleElements(idElementsB);
        }
    }

    @Test
    public void intersectionTest_setAAndSetBAreEmptySet_shouldReturnEmptySet() {
        long idSetA = setService.createEmptySet();
        long idSetB = setService.createEmptySet();
        try {
            long idIntersectionSet = setClient.intersection(idSetA, idSetB);
            Assert.assertNotEquals(idIntersectionSet, idSetA);
            Assert.assertNotEquals(idIntersectionSet, idSetB);
            Assert.assertTrue(setService.isEmpty(idIntersectionSet));
            setService.deleteSet(idIntersectionSet);
        } finally {
            setService.deleteSet(idSetA);
            setService.deleteSet(idSetB);
        }
    }

    @Test
    public void intersectionTest_setADoesNotExist_shouldReturn404() {
        long idSetA = setService.createEmptySet();
        setService.deleteSet(idSetA);
        long idSetB = setService.createEmptySet();
        try {
            setClient.intersection(idSetA, idSetB);
            Assert.fail();
        } catch(FeignException e) {
            Assert.assertEquals(404, e.status());
            Assert.assertTrue(
        "Response body [" + e.contentUTF8() + "] should contain the id of the nonexistent Set [" + idSetA + "]",
                e.contentUTF8().contains(idSetA + "")
            );
        } finally {
            setService.deleteSet(idSetB);
        }
    }

    @Test
    public void intersectionTest_setBDoesNotExist_shouldReturn404() {
        long idSetA = setService.createEmptySet();
        long idSetB = setService.createEmptySet();
        setService.deleteSet(idSetB);
        try {
            setClient.intersection(idSetA, idSetB);
            Assert.fail();
        } catch(FeignException e) {
            Assert.assertEquals(404, e.status());
            Assert.assertTrue(
        "Response body [" + e.contentUTF8() + "] should contain the id of the nonexistent Set [" + idSetB + "]",
                e.contentUTF8().contains(idSetB + "")
            );
        } finally {
            setService.deleteSet(idSetA);
        }
    }

    @Test
    public void intersectionMultipleSetsTest_threeSetsWithSomeCommonElements_shouldReturnCommonElements() {
        List<Long> idAllElements = elementService.createMultipleElements(15);
        long idSetA = setService.createSetWithElements(
            Sets.newHashSet(idAllElements.subList(0, 8))
        );
        long idSetB = setService.createSetWithElements(
            Sets.newHashSet(idAllElements.subList(4, 12))
        );
        long idSetC = setService.createSetWithElements(
            Sets.newHashSet(
                Iterables.concat(
                    idAllElements.subList(4, 7),
                    idAllElements.subList(9, idAllElements.size())
                )
            )
        );
        try {
            long idIntersectionSet = setClient.intersectMultipleSets(Sets.newHashSet(idSetA, idSetB, idSetC));
            Assert.assertNotEquals(idSetA, idIntersectionSet);
            Assert.assertNotEquals(idSetB, idIntersectionSet);
            Assert.assertNotEquals(idSetC, idIntersectionSet);
            Assert.assertEquals(
                Sets.newHashSet(idAllElements.subList(4, 7)),
                setService.getSetElements(idIntersectionSet)
            );
            setService.deleteSet(idIntersectionSet);
        } finally {
            setService.deleteSet(idSetA);
            setService.deleteSet(idSetB);
            setService.deleteSet(idSetC);
            elementService.deleteMultipleElements(Sets.newHashSet(idAllElements));
        }
    }

    @Test
    public void intersectionMultipleSetsTest_threeSetsWhereOneHasNoCommonElements_shouldReturnEmptySet() {
        List<Long> idAllElements = elementService.createMultipleElements(15);
        long idSetA = setService.createSetWithElements(
            Sets.newHashSet(idAllElements.subList(0, 3))
        );
        long idSetB = setService.createSetWithElements(
            Sets.newHashSet(idAllElements.subList(3, 12))
        );
        long idSetC = setService.createSetWithElements(
            Sets.newHashSet(idAllElements.subList(12, idAllElements.size()))
        );
        try {
            long idIntersectionSet = setClient.intersectMultipleSets(Sets.newHashSet(idSetA, idSetB, idSetC));
            Assert.assertNotEquals(idSetA, idIntersectionSet);
            Assert.assertNotEquals(idSetB, idIntersectionSet);
            Assert.assertNotEquals(idSetC, idIntersectionSet);
            Assert.assertTrue(setService.isEmpty(idIntersectionSet));
            setService.deleteSet(idIntersectionSet);
        } finally {
            setService.deleteSet(idSetA);
            setService.deleteSet(idSetB);
            setService.deleteSet(idSetC);
            elementService.deleteMultipleElements(Sets.newHashSet(idAllElements));
        }
    }

    @Test
    public void intersectionMultipleSetsTest_threeSetsThatAreEqual_shouldReturnEqualSet() {
        Set<Long> idElements = Sets.newHashSet(elementService.createMultipleElements(5));
        long idSetA = setService.createSetWithElements(idElements);
        long idSetB = setService.createSetWithElements(idElements);
        long idSetC = setService.createSetWithElements(idElements);
        try {
            long idIntersectionSet = setClient.intersectMultipleSets(Sets.newHashSet(idSetA, idSetB, idSetC));
            Assert.assertNotEquals(idSetA, idIntersectionSet);
            Assert.assertNotEquals(idSetB, idIntersectionSet);
            Assert.assertNotEquals(idSetC, idIntersectionSet);
            Assert.assertEquals(idElements, setService.getSetElements(idIntersectionSet));
            setService.deleteSet(idIntersectionSet);
        } finally {
            setService.deleteSet(idSetA);
            setService.deleteSet(idSetB);
            setService.deleteSet(idSetC);
            elementService.deleteMultipleElements(idElements);
        }
    }

    @Test
    public void intersectionMultipleSetsTest_threeSetsThatAreTheSame_shouldReturnEqualSet() {
        Set<Long> idElements = Sets.newHashSet(elementService.createMultipleElements(5));
        long idSet = setService.createSetWithElements(idElements);
        try {
            long idIntersectionSet = setClient.intersectMultipleSets(Sets.newHashSet(idSet, idSet, idSet));
            Assert.assertNotEquals(idSet, idIntersectionSet);
            Assert.assertEquals(idElements, setService.getSetElements(idIntersectionSet));
            setService.deleteSet(idIntersectionSet);
        } finally {
            setService.deleteSet(idSet);
            elementService.deleteMultipleElements(idElements);
        }
    }

    @Test
    public void intersectionMultipleSetsTest_threeSetsWhereOneIsEmptySet_shouldReturnEmptySet() {
        Set<Long> idElements = Sets.newHashSet(elementService.createMultipleElements(5));
        long idSetA = setService.createEmptySet();
        long idSetB = setService.createSetWithElements(idElements);
        long idSetC = setService.createSetWithElements(idElements);
        try {
            long idIntersectionSet = setClient.intersectMultipleSets(Sets.newHashSet(idSetA, idSetB, idSetC));
            Assert.assertNotEquals(idSetA, idIntersectionSet);
            Assert.assertNotEquals(idSetB, idIntersectionSet);
            Assert.assertNotEquals(idSetC, idIntersectionSet);
            Assert.assertTrue(setService.isEmpty(idIntersectionSet));
            setService.deleteSet(idIntersectionSet);
        } finally {
            setService.deleteSet(idSetA);
            setService.deleteSet(idSetB);
            setService.deleteSet(idSetC);
            elementService.deleteMultipleElements(idElements);
        }
    }

    @Test
    public void intersectionMultipleSetsTest_threeSetsThatAreEmptySets_shouldReturnEmptySet() {
        long idSetA = setService.createEmptySet();
        long idSetB = setService.createEmptySet();
        long idSetC = setService.createEmptySet();
        try {
            long idIntersectionSet = setClient.intersectMultipleSets(Sets.newHashSet(idSetA, idSetB, idSetC));
            Assert.assertNotEquals(idSetA, idIntersectionSet);
            Assert.assertNotEquals(idSetB, idIntersectionSet);
            Assert.assertNotEquals(idSetC, idIntersectionSet);
            Assert.assertTrue(setService.isEmpty(idIntersectionSet));
            setService.deleteSet(idIntersectionSet);
        } finally {
            setService.deleteSet(idSetA);
            setService.deleteSet(idSetB);
            setService.deleteSet(idSetC);
        }
    }

    @Test
    public void intersectionMultipleSetsTest_threeSetsWhereOneDoesNotExist_shouldReturn400() {
        long idSetA = setService.createEmptySet();
        setService.deleteSet(idSetA);
        long idSetB = setService.createEmptySet();
        long idSetC = setService.createEmptySet();
        try {
            setClient.intersectMultipleSets(Sets.newHashSet(idSetA, idSetB, idSetC));
            Assert.fail();
        } catch(FeignException e) {
            Assert.assertEquals(400, e.status());
            Assert.assertTrue(
        "Response body [" + e.contentUTF8() + "] should contain the id of the nonexistent Set [" + idSetA + "]",
                e.contentUTF8().contains(idSetA + "")
            );
        } finally {
            setService.deleteSet(idSetB);
            setService.deleteSet(idSetC);
        }
    }

    @Test
    public void unionTest_setsDisjoint_shouldReturnUnion() {
        Set<Long> idElementsA = Sets.newHashSet(elementService.createMultipleElements(5));
        Set<Long> idElementsB = Sets.newHashSet(elementService.createMultipleElements(6));
        long idSetA = setService.createSetWithElements(idElementsA);
        long idSetB = setService.createSetWithElements(idElementsB);
        try {
            long idUnion = setClient.union(idSetA, idSetB);
            Assert.assertNotEquals(idSetA, idUnion);
            Assert.assertNotEquals(idSetB, idUnion);
            Assert.assertEquals(
                Sets.union(idElementsA, idElementsB),
                setService.getSetElements(idUnion)
            );
            setService.deleteSet(idUnion);
        } finally {
            elementService.deleteMultipleElements(idElementsA);
            elementService.deleteMultipleElements(idElementsB);
            setService.deleteSet(idSetA);
            setService.deleteSet(idSetB);
        }
    }

    @Test
    public void unionTest_setsNonDisjoint_shouldReturnUnion() {
        List<Long> idAllElements = elementService.createMultipleElements(6);
        long idSetA = setService.createSetWithElements(
            Sets.newHashSet(idAllElements.subList(0, 4))
        );
        long idSetB = setService.createSetWithElements(
            Sets.newHashSet(idAllElements.subList(2, idAllElements.size()))
        );
        try {
            long idUnion = setClient.union(idSetA, idSetB);
            Assert.assertNotEquals(idSetA, idUnion);
            Assert.assertNotEquals(idSetB, idUnion);
            Assert.assertEquals(
                Sets.newHashSet(idAllElements),
                setService.getSetElements(idUnion)
            );
            setService.deleteSet(idUnion);
        } finally {
            elementService.deleteMultipleElements(Sets.newHashSet(idAllElements));
            setService.deleteSet(idSetA);
            setService.deleteSet(idSetB);
        }
    }

    @Test
    public void unionTest_twoEqualSets_shouldReturnEqualSet() {
        Set<Long> idElements = Sets.newHashSet(elementService.createMultipleElements(5));
        long idSetA = setService.createSetWithElements(idElements);
        long idSetB = setService.createSetWithElements(idElements);
        try {
            long idUnion = setClient.union(idSetA, idSetB);
            Assert.assertNotEquals(idSetA, idUnion);
            Assert.assertNotEquals(idSetB, idUnion);
            Assert.assertEquals(idElements, setService.getSetElements(idUnion));
            setService.deleteSet(idUnion);
        } finally {
            elementService.deleteMultipleElements(idElements);
            setService.deleteSet(idSetA);
            setService.deleteSet(idSetB);
        }
    }

    @Test
    public void unionTest_setsAreSame_shouldReturnEqualSet() {
        Set<Long> idElements = Sets.newHashSet(elementService.createMultipleElements(5));
        long idSet = setService.createSetWithElements(idElements);
        try {
            long idUnion = setClient.union(idSet, idSet);
            Assert.assertNotEquals(idSet, idUnion);
            Assert.assertEquals(idElements, setService.getSetElements(idUnion));
            setService.deleteSet(idUnion);
        } finally {
            elementService.deleteMultipleElements(idElements);
            setService.deleteSet(idSet);
        }
    }

    @Test
    public void unionTest_setAEmptySetBNonEmpty_shouldReturnSetB() {
        Set<Long> idElementsB = Sets.newHashSet(elementService.createMultipleElements(5));
        long idSetA = setService.createEmptySet();
        long idSetB = setService.createSetWithElements(idElementsB);
        try {
            long idUnion = setClient.union(idSetA, idSetB);
            Assert.assertNotEquals(idSetA, idUnion);
            Assert.assertNotEquals(idSetB, idUnion);
            Assert.assertEquals(idElementsB, setService.getSetElements(idUnion));
            setService.deleteSet(idUnion);
        } finally {
            elementService.deleteMultipleElements(idElementsB);
            setService.deleteSet(idSetA);
            setService.deleteSet(idSetB);
        }
    }

    @Test
    public void unionTest_setANonEmptySetBEmpty_shouldReturnSetA() {
        Set<Long> idElementsA = Sets.newHashSet(elementService.createMultipleElements(5));
        long idSetA = setService.createSetWithElements(idElementsA);
        long idSetB = setService.createEmptySet();
        try {
            long idUnion = setClient.union(idSetA, idSetB);
            Assert.assertNotEquals(idSetA, idUnion);
            Assert.assertNotEquals(idSetB, idUnion);
            Assert.assertEquals(idElementsA, setService.getSetElements(idUnion));
            setService.deleteSet(idUnion);
        } finally {
            elementService.deleteMultipleElements(idElementsA);
            setService.deleteSet(idSetA);
            setService.deleteSet(idSetB);
        }
    }

    @Test
    public void unionTest_bothSetsEmpty_shouldReturnEmptySet() {
        long idSetA = setService.createEmptySet();
        long idSetB = setService.createEmptySet();
        try {
            long idUnion = setClient.union(idSetA, idSetB);
            Assert.assertNotEquals(idSetA, idUnion);
            Assert.assertNotEquals(idSetB, idUnion);
            Assert.assertTrue(setService.isEmpty(idUnion));
            setService.deleteSet(idUnion);
        } finally {
            setService.deleteSet(idSetA);
            setService.deleteSet(idSetB);
        }
    }

    @Test
    public void unionTest_setADoesNotExist_shouldReturn404() {
        long idSetA = setService.createSet();
        setService.deleteSet(idSetA);
        long idSetB = setService.createSet();
        try {
            setClient.union(idSetA, idSetB);
            Assert.fail();
        } catch(FeignException e) {
            Assert.assertEquals(404, e.status());
            Assert.assertTrue(
        "Response body [" + e.contentUTF8() + "] should contain the id of the nonexistent Set [" + idSetA + "]",
                e.contentUTF8().contains(idSetA + "")
            );
        } finally {
            setService.deleteSet(idSetB);
        }
    }

    @Test
    public void unionTest_setBDoesNotExist_shouldReturn404() {
        long idSetA = setService.createSet();
        long idSetB = setService.createSet();
        setService.deleteSet(idSetB);
        try {
            setClient.union(idSetA, idSetB);
            Assert.fail();
        } catch(FeignException e) {
            Assert.assertEquals(404, e.status());
            Assert.assertTrue(
        "Response body [" + e.contentUTF8() + "] should contain the id of the nonexistent Set [" + idSetB + "]",
                e.contentUTF8().contains(idSetB + "")
            );
        } finally {
            setService.deleteSet(idSetA);
        }
    }

    @Test
    public void unionMultipleSetsTest_threeNonDisjointSets_shouldReturnUnion() {
        List<Long> idAllElements = elementService.createMultipleElements(10);
        Set<Long> idElementsA = Sets.newHashSet(idAllElements.subList(0, 6));
        Set<Long> idElementsB = Sets.newHashSet(idAllElements.subList(3, 9));
        Set<Long> idElementsC = Sets.newHashSet(idAllElements.subList(4, idAllElements.size()));
        long idSetA = setService.createSetWithElements(idElementsA);
        long idSetB = setService.createSetWithElements(idElementsB);
        long idSetC = setService.createSetWithElements(idElementsC);
        try {
            long idUnion = setClient.unionMultipleSets(Sets.newHashSet(idSetA, idSetB, idSetC));
            Assert.assertNotEquals(idSetA, idUnion);
            Assert.assertNotEquals(idSetB, idUnion);
            Assert.assertNotEquals(idSetC, idUnion);
            Assert.assertEquals(Sets.newHashSet(idAllElements), setService.getSetElements(idUnion));
            setService.deleteSet(idUnion);
        } finally {
            elementService.deleteMultipleElements(Sets.newHashSet(idAllElements));
            setService.deleteSet(idSetA);
            setService.deleteSet(idSetB);
            setService.deleteSet(idSetC);
        }
    }

    @Test
    public void unionMultipleSetsTest_threeDisjointSets_shouldReturnUnion() {
        Set<Long> idElementsA = Sets.newHashSet(elementService.createMultipleElements(3));
        Set<Long> idElementsB = Sets.newHashSet(elementService.createMultipleElements(4));
        Set<Long> idElementsC = Sets.newHashSet(elementService.createMultipleElements(5));
        long idSetA = setService.createSetWithElements(idElementsA);
        long idSetB = setService.createSetWithElements(idElementsB);
        long idSetC = setService.createSetWithElements(idElementsC);
        try {
            long idUnion = setClient.unionMultipleSets(Sets.newHashSet(idSetA, idSetB, idSetC));
            Assert.assertNotEquals(idSetA, idUnion);
            Assert.assertNotEquals(idSetB, idUnion);
            Assert.assertNotEquals(idSetC, idUnion);
            Assert.assertEquals(
                Sets.union(
                    idElementsA,
                    Sets.union(idElementsB, idElementsC)
                ),
                setService.getSetElements(idUnion)
            );
            setService.deleteSet(idUnion);
        } finally {
            elementService.deleteMultipleElements(idElementsA);
            elementService.deleteMultipleElements(idElementsB);
            elementService.deleteMultipleElements(idElementsC);
            setService.deleteSet(idSetA);
            setService.deleteSet(idSetB);
            setService.deleteSet(idSetC);
        }
    }

    @Test
    public void unionMultipleSetsTest_threeEqualSets_shouldReturnEqualSet() {
        Set<Long> idElements = Sets.newHashSet(elementService.createMultipleElements(5));
        long idSetA = setService.createSetWithElements(idElements);
        long idSetB = setService.createSetWithElements(idElements);
        long idSetC = setService.createSetWithElements(idElements);
        try {
            long idUnion = setClient.unionMultipleSets(Sets.newHashSet(idSetA, idSetB, idSetC));
            Assert.assertNotEquals(idSetA, idUnion);
            Assert.assertNotEquals(idSetB, idUnion);
            Assert.assertNotEquals(idSetC, idUnion);
            Assert.assertEquals(idElements, setService.getSetElements(idUnion));
            setService.deleteSet(idUnion);
        } finally {
            elementService.deleteMultipleElements(idElements);
            setService.deleteSet(idSetA);
            setService.deleteSet(idSetB);
            setService.deleteSet(idSetC);
        }
    }

    @Test
    public void unionMultipleSetsTest_threeSetsThatAreTheSame_shouldReturnEqualSet() {
        Set<Long> idElements = Sets.newHashSet(elementService.createMultipleElements(5));
        long idSet = setService.createSetWithElements(idElements);
        try {
            long idUnion = setClient.unionMultipleSets(Sets.newHashSet(idSet, idSet, idSet));
            Assert.assertNotEquals(idSet, idUnion);
            Assert.assertEquals(idElements, setService.getSetElements(idUnion));
            setService.deleteSet(idUnion);
        } finally {
            elementService.deleteMultipleElements(idElements);
            setService.deleteSet(idSet);
        }
    }

    @Test
    public void unionMultipleSetsTest_threeSetsWhereOneIsEmpty_shouldReturnUnion() {
        Set<Long> idElementsB = Sets.newHashSet(elementService.createMultipleElements(5));
        Set<Long> idElementsC = Sets.newHashSet(elementService.createMultipleElements(5));
        long idSetA = setService.createEmptySet();
        long idSetB = setService.createSetWithElements(idElementsB);
        long idSetC = setService.createSetWithElements(idElementsC);
        try {
            long idUnion = setClient.unionMultipleSets(Sets.newHashSet(idSetA, idSetB, idSetC));
            Assert.assertNotEquals(idSetA, idUnion);
            Assert.assertNotEquals(idSetB, idUnion);
            Assert.assertNotEquals(idSetC, idUnion);
            Assert.assertEquals(
                Sets.union(idElementsB, idElementsC),
                setService.getSetElements(idUnion)
            );
            setService.deleteSet(idUnion);
        } finally {
            elementService.deleteMultipleElements(idElementsB);
            elementService.deleteMultipleElements(idElementsC);
            setService.deleteSet(idSetA);
            setService.deleteSet(idSetB);
            setService.deleteSet(idSetC);
        }
    }

    @Test
    public void unionMultipleSetsTest_threeEmptySets_shouldReturnUnion() {
        long idSetA = setService.createEmptySet();
        long idSetB = setService.createEmptySet();
        long idSetC = setService.createEmptySet();
        try {
            long idUnion = setClient.unionMultipleSets(Sets.newHashSet(idSetA, idSetB, idSetC));
            Assert.assertNotEquals(idSetA, idUnion);
            Assert.assertNotEquals(idSetB, idUnion);
            Assert.assertNotEquals(idSetC, idUnion);
            Assert.assertTrue(setService.isEmpty(idUnion));
            setService.deleteSet(idUnion);
        } finally {
            setService.deleteSet(idSetA);
            setService.deleteSet(idSetB);
            setService.deleteSet(idSetC);
        }
    }

    @Test
    public void unionMultipleSetsTest_threeSetsWhereOneDoesNotExist_shouldReturn400() {
        long idSetA = setService.createEmptySet();
        setService.deleteSet(idSetA);
        long idSetB = setService.createEmptySet();
        long idSetC = setService.createEmptySet();
        try {
            setClient.unionMultipleSets(Sets.newHashSet(idSetA, idSetB, idSetC));
        } catch(FeignException e) {
            Assert.assertEquals(400, e.status());
            Assert.assertTrue(
        "Response body [" + e.contentUTF8() + "] should contain the id of the nonexistent Set [" + idSetA + "]",
                e.contentUTF8().contains(idSetA + "")
            );
        } finally {
            setService.deleteSet(idSetA);
            setService.deleteSet(idSetB);
            setService.deleteSet(idSetC);
        }
    }

    @Test
    public void differenceTest_intersectingNonSubsetSets_shouldReturnDifference() {
        List<Long> idAllElements = elementService.createMultipleElements(10);
        long idSetA = setService.createSetWithElements(Sets.newHashSet(idAllElements.subList(0, 7)));
        long idSetB = setService.createSetWithElements(Sets.newHashSet(idAllElements.subList(4, idAllElements.size())));
        try {
            long idDifference = setClient.difference(idSetA, idSetB);
            Assert.assertNotEquals(idDifference, idSetA);
            Assert.assertNotEquals(idDifference, idSetB);
            Assert.assertEquals(
                Sets.newHashSet(idAllElements.subList(0, 4)),
                setService.getSetElements(idDifference)
            );
            setService.deleteSet(idDifference);
        } finally {
            setService.deleteSet(idSetA);
            setService.deleteSet(idSetB);
            elementService.deleteMultipleElements(Sets.newHashSet(idAllElements));
        }
    }

    @Test
    public void differenceTest_setAAndSetBDisjoint_shouldReturnSetA() {
        List<Long> idAllElements = elementService.createMultipleElements(10);
        Set<Long> idElementsA = Sets.newHashSet(idAllElements.subList(0, 5));
        long idSetA = setService.createSetWithElements(idElementsA);
        long idSetB = setService.createSetWithElements(Sets.newHashSet(idAllElements.subList(5, idAllElements.size())));
        try {
            long idDifference = setClient.difference(idSetA, idSetB);
            Assert.assertNotEquals(idDifference, idSetA);
            Assert.assertNotEquals(idDifference, idSetB);
            Assert.assertEquals(idElementsA, setService.getSetElements(idDifference));
            setService.deleteSet(idDifference);
        } finally {
            setService.deleteSet(idSetA);
            setService.deleteSet(idSetB);
            elementService.deleteMultipleElements(Sets.newHashSet(idAllElements));
        }
    }

    @Test
    public void differenceTest_setASubsetOfSetB_shouldReturnEmptySet() {
        List<Long> idElementsB = elementService.createMultipleElements(10);
        Set<Long> idElementsA = Sets.newHashSet(idElementsB.subList(0, 5));
        long idSetA = setService.createSetWithElements(idElementsA);
        long idSetB = setService.createSetWithElements(Sets.newHashSet(idElementsB));
        try {
            long idDifference = setClient.difference(idSetA, idSetB);
            Assert.assertNotEquals(idDifference, idSetA);
            Assert.assertNotEquals(idDifference, idSetB);
            Assert.assertTrue(setService.isEmpty(idDifference));
            setService.deleteSet(idDifference);
        } finally {
            setService.deleteSet(idSetA);
            setService.deleteSet(idSetB);
            elementService.deleteMultipleElements(Sets.newHashSet(idElementsB));
        }
    }

    @Test
    public void differenceTest_setBSubsetOfSetA_shouldReturnDifference() {
        List<Long> idElementsA = elementService.createMultipleElements(10);
        Set<Long> idElementsB = Sets.newHashSet(idElementsA.subList(0, 5));
        long idSetA = setService.createSetWithElements(Sets.newHashSet(idElementsA));
        long idSetB = setService.createSetWithElements(idElementsB);
        try {
            long idDifference = setClient.difference(idSetA, idSetB);
            Assert.assertNotEquals(idDifference, idSetA);
            Assert.assertNotEquals(idDifference, idSetB);
            Assert.assertEquals(
                Sets.difference(Sets.newHashSet(idElementsA), idElementsB),
                setService.getSetElements(idDifference)
            );
            setService.deleteSet(idDifference);
        } finally {
            setService.deleteSet(idSetA);
            setService.deleteSet(idSetB);
            elementService.deleteMultipleElements(Sets.newHashSet(idElementsA));
        }
    }

    @Test
    public void differenceTest_setAEmptySetBNotEmpty_shouldReturnEmptySet() {
        long idSetA = setService.createEmptySet();
        Set<Long> idElementsB = Sets.newHashSet(elementService.createMultipleElements(5));
        long idSetB = setService.createSetWithElements(idElementsB);
        try {
            long idDifference = setClient.difference(idSetA, idSetB);
            Assert.assertNotEquals(idDifference, idSetA);
            Assert.assertNotEquals(idDifference, idSetB);
            Assert.assertTrue(setService.isEmpty(idDifference));
            setService.deleteSet(idDifference);
        } finally {
            setService.deleteSet(idSetA);
            setService.deleteSet(idSetB);
            elementService.deleteMultipleElements(idElementsB);
        }
    }

    @Test
    public void differenceTest_setANotEmptySetBEmpty_shouldReturnSetA() {
        Set<Long> idElementsA = Sets.newHashSet(elementService.createMultipleElements(5));
        long idSetA = setService.createSetWithElements(idElementsA);
        long idSetB = setService.createEmptySet();
        try {
            long idDifference = setClient.difference(idSetA, idSetB);
            Assert.assertNotEquals(idDifference, idSetA);
            Assert.assertNotEquals(idDifference, idSetB);
            Assert.assertEquals(idElementsA, setService.getSetElements(idDifference));
            setService.deleteSet(idDifference);
        } finally {
            setService.deleteSet(idSetA);
            setService.deleteSet(idSetB);
            elementService.deleteMultipleElements(idElementsA);
        }
    }

    @Test
    public void differenceTest_setAAndSetBEmpty_shouldReturnEmptySet() {
        long idSetA = setService.createEmptySet();
        long idSetB = setService.createEmptySet();
        try {
            long idDifference = setClient.difference(idSetA, idSetB);
            Assert.assertNotEquals(idDifference, idSetA);
            Assert.assertNotEquals(idDifference, idSetB);
            Assert.assertTrue(setService.isEmpty(idDifference));
            setService.deleteSet(idDifference);
        } finally {
            setService.deleteSet(idSetA);
            setService.deleteSet(idSetB);
        }
    }

    @Test
    public void differenceTest_setADoesNotExist_shouldReturn404() {
        long idSetA = setService.createSet();
        setService.deleteSet(idSetA);
        long idSetB = setService.createSet();
        try {
            setClient.difference(idSetA, idSetB);
            Assert.fail();
        } catch(FeignException e) {
            Assert.assertEquals(404, e.status());
            Assert.assertTrue(
        "Response body [" + e.contentUTF8() + "] should contain the id of the nonexistent Set [" + idSetA + "]",
                e.contentUTF8().contains(idSetA + "")
            );
        } finally {
            setService.deleteSet(idSetB);
        }
    }

    @Test
    public void differenceTest_setBDoesNotExist_shouldReturn404() {
        long idSetA = setService.createSet();
        long idSetB = setService.createSet();
        setService.deleteSet(idSetB);
        try {
            setClient.difference(idSetA, idSetB);
            Assert.fail();
        } catch(FeignException e) {
            Assert.assertEquals(404, e.status());
            Assert.assertTrue(
        "Response body [" + e.contentUTF8() + "] should contain the id of the nonexistent Set [" + idSetB + "]",
                e.contentUTF8().contains(idSetB + "")
            );
        } finally {
            setService.deleteSet(idSetA);
        }
    }

    @Test
    public void symmetricDifferenceTest_setAAndSetBIntersectButAreNotSubsets_shouldReturnSymmetricDifference() {
        List<Long> idAllElements = elementService.createMultipleElements(10);
        Set<Long> idElementsA = Sets.newHashSet(idAllElements.subList(0, 5));
        Set<Long> idElementsB = Sets.newHashSet(idAllElements.subList(3, idAllElements.size()));
        long idSetA = setService.createSetWithElements(idElementsA);
        long idSetB = setService.createSetWithElements(idElementsB);
        try {
            long idSymmetricDifference = setClient.symmetricDifference(idSetA, idSetB);
            Assert.assertNotEquals(idSetA, idSymmetricDifference);
            Assert.assertNotEquals(idSetB, idSymmetricDifference);
            Assert.assertEquals(
                Sets.symmetricDifference(idElementsA, idElementsB),
                setService.getSetElements(idSymmetricDifference)
            );
            setService.deleteSet(idSymmetricDifference);
        } finally {
            setService.deleteSet(idSetA);
            setService.deleteSet(idSetB);
            elementService.deleteMultipleElements(Sets.newHashSet(idAllElements));
        }
    }

    @Test
    public void symmetricDifferenceTest_setAIsSubsetOfSetB_shouldReturnSymmetricDifference() {
        List<Long> idElementsBList = elementService.createMultipleElements(10);
        Set<Long> idElementsA = Sets.newHashSet(idElementsBList.subList(0, 5));
        Set<Long> idElementsB = Sets.newHashSet(idElementsBList);
        long idSetA = setService.createSetWithElements(idElementsA);
        long idSetB = setService.createSetWithElements(idElementsB);
        try {
            long idSymmetricDifference = setClient.symmetricDifference(idSetA, idSetB);
            Assert.assertNotEquals(idSetA, idSymmetricDifference);
            Assert.assertNotEquals(idSetB, idSymmetricDifference);
            Assert.assertEquals(
                Sets.symmetricDifference(idElementsA, idElementsB),
                setService.getSetElements(idSymmetricDifference)
            );
            setService.deleteSet(idSymmetricDifference);
        } finally {
            setService.deleteSet(idSetA);
            setService.deleteSet(idSetB);
            elementService.deleteMultipleElements(Sets.newHashSet(idElementsB));
        }
    }

    @Test
    public void symmetricDifferenceTest_setBIsSubsetOfSetA_shouldReturnSymmetricDifference() {
        List<Long> idElementsAList = elementService.createMultipleElements(10);
        Set<Long> idElementsA = Sets.newHashSet(idElementsAList);
        Set<Long> idElementsB = Sets.newHashSet(idElementsAList.subList(0, 5));
        long idSetA = setService.createSetWithElements(idElementsA);
        long idSetB = setService.createSetWithElements(idElementsB);
        try {
            long idSymmetricDifference = setClient.symmetricDifference(idSetA, idSetB);
            Assert.assertNotEquals(idSetA, idSymmetricDifference);
            Assert.assertNotEquals(idSetB, idSymmetricDifference);
            Assert.assertEquals(
                Sets.symmetricDifference(idElementsA, idElementsB),
                setService.getSetElements(idSymmetricDifference)
            );
            setService.deleteSet(idSymmetricDifference);
        } finally {
            setService.deleteSet(idSetA);
            setService.deleteSet(idSetB);
            elementService.deleteMultipleElements(Sets.newHashSet(idElementsA));
        }
    }

    @Test
    public void symmetricDifferenceTest_setsAreDisjoint_shouldReturnUnion() {
        Set<Long> idElementsA = Sets.newHashSet(elementService.createMultipleElements(5));
        Set<Long> idElementsB = Sets.newHashSet(elementService.createMultipleElements(5));
        long idSetA = setService.createSetWithElements(idElementsA);
        long idSetB = setService.createSetWithElements(idElementsB);
        try {
            long idSymmetricDifference = setClient.symmetricDifference(idSetA, idSetB);
            Assert.assertNotEquals(idSetA, idSymmetricDifference);
            Assert.assertNotEquals(idSetB, idSymmetricDifference);
            Assert.assertEquals(
                Sets.union(idElementsA, idElementsB),
                setService.getSetElements(idSymmetricDifference)
            );
            setService.deleteSet(idSymmetricDifference);
        } finally {
            setService.deleteSet(idSetA);
            setService.deleteSet(idSetB);
            elementService.deleteMultipleElements(Sets.union(idElementsA, idElementsB));
        }
    }

    @Test
    public void symmetricDifferenceTest_setAIsEmpty_shouldReturnSetB() {
        long idSetA = setService.createEmptySet();
        Set<Long> idElementsB = Sets.newHashSet(elementService.createMultipleElements(5));
        long idSetB = setService.createSetWithElements(idElementsB);
        try {
            long idSymmetricDifference = setClient.symmetricDifference(idSetA, idSetB);
            Assert.assertNotEquals(idSetA, idSymmetricDifference);
            Assert.assertNotEquals(idSetB, idSymmetricDifference);
            Assert.assertEquals(idElementsB, setService.getSetElements(idSymmetricDifference));
            setService.deleteSet(idSymmetricDifference);
        } finally {
            setService.deleteSet(idSetA);
            setService.deleteSet(idSetB);
            elementService.deleteMultipleElements(idElementsB);
        }
    }

    @Test
    public void symmetricDifferenceTest_setBIsEmpty_shouldReturnSetA() {
        Set<Long> idElementsA = Sets.newHashSet(elementService.createMultipleElements(5));
        long idSetA = setService.createSetWithElements(idElementsA);
        long idSetB = setService.createEmptySet();
        try {
            long idSymmetricDifference = setClient.symmetricDifference(idSetA, idSetB);
            Assert.assertNotEquals(idSetA, idSymmetricDifference);
            Assert.assertNotEquals(idSetB, idSymmetricDifference);
            Assert.assertEquals(idElementsA, setService.getSetElements(idSymmetricDifference));
            setService.deleteSet(idSymmetricDifference);
        } finally {
            setService.deleteSet(idSetA);
            setService.deleteSet(idSetB);
            elementService.deleteMultipleElements(idElementsA);
        }
    }

    @Test
    public void symmetricDifferenceTest_bothSetsAreEmpty_shouldReturnEmptySet() {
        long idSetA = setService.createEmptySet();
        long idSetB = setService.createEmptySet();
        try {
            long idSymmetricDifference = setClient.symmetricDifference(idSetA, idSetB);
            Assert.assertNotEquals(idSetA, idSymmetricDifference);
            Assert.assertNotEquals(idSetB, idSymmetricDifference);
            Assert.assertTrue(setService.isEmpty(idSymmetricDifference));
            setService.deleteSet(idSymmetricDifference);
        } finally {
            setService.deleteSet(idSetA);
            setService.deleteSet(idSetB);
        }
    }

    @Test
    public void symmetricDifferenceTest_setADoesNotExist_shouldReturn404() {
        long idSetA = setService.createSet();
        setService.deleteSet(idSetA);
        long idSetB = setService.createSet();
        try {
            setClient.symmetricDifference(idSetA, idSetB);
            Assert.fail();
        } catch(FeignException e) {
            Assert.assertEquals(404, e.status());
            Assert.assertTrue(
        "Response body [" + e.contentUTF8() + "] should contain the id of the nonexistent Set [" + idSetA + "]",
                e.contentUTF8().contains(idSetA + "")
            );
        } finally {
            setService.deleteSet(idSetB);
        }
    }

    @Test
    public void symmetricDifferenceTest_setBDoesNotExist_shouldReturn404() {
        long idSetA = setService.createSet();
        long idSetB = setService.createSet();
        setService.deleteSet(idSetB);
        try {
            setClient.symmetricDifference(idSetA, idSetB);
            Assert.fail();
        } catch(FeignException e) {
            Assert.assertEquals(404, e.status());
            Assert.assertTrue(
        "Response body [" + e.contentUTF8() + "] should contain the id of the nonexistent Set [" + idSetB + "]",
                e.contentUTF8().contains(idSetB + "")
            );
        } finally {
            setService.deleteSet(idSetA);
        }
    }
}

package com.beumuth.math.core.settheory.set;

import com.beumuth.math.client.Clients;
import com.beumuth.math.client.settheory.set.SetClient;
import com.beumuth.math.core.internal.client.ClientConfigurations;
import com.beumuth.math.core.settheory.element.ElementService;
import com.beumuth.math.core.settheory.object.ObjectService;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
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
    private ElementService elementService;
    @Autowired
    private ObjectService objectService;

    private SetClient setClient;

    @Before
    public void setupTests() {
        setClient = Clients.getClient(SetClient.class, ClientConfigurations.LOCAL);
    }

    @Test
    public void doesSetExistTest_shouldReturnTrue() {
        long idSet = setService.createSet();
        try {
            Assert.assertTrue(setClient.exists(idSet));
        } finally {
            setService.deleteSet(idSet);
        }
    }

    @Test
    public void doesSetExistTest_shouldReturnFalse() {
        long idSet = setService.createSet();
        setService.deleteSet(idSet);
        Assert.assertFalse(setClient.exists(idSet));
    }

    @Test
    public void getSetTest_shouldReturnSet() {
        long idSet = setService.createSet();
        try {
            Assert.assertEquals(idSet, setClient.get(idSet).getId());
        } finally {
            setService.deleteSet(idSet);
        }
    }

    @Test
    public void getSetTest_shouldReturn404() {
        long idSet = setService.createSet();
        setService.deleteSet(idSet);
        try {
            setClient.get(idSet);
            Assert.fail();
        } catch(FeignException e) {
            Assert.assertEquals(404, e.status());
        }
    }

    @Test
    public void getSetElements_nonEmptySet_shouldReturnElements() {
        Set<Long> idElements = com.google.common.collect.Sets.newHashSet(objectService.createMultipleObjects(3));
        long idSet = setService.createSetWithElements(idElements);
        try {
            Assert.assertEquals(idElements, setClient.getElements(idSet));
        } finally {
            objectService.deleteMultipleObjects(idElements);
            setService.deleteSet(idSet);
        }
    }

    @Test
    public void getSetElements_emptySet_shouldReturnNoElements() {
        long idSet = setService.createEmptySet();
        try {
            Assert.assertEquals(Collections.EMPTY_SET, setClient.getElements(idSet));
        } finally {
            setService.deleteSet(idSet);
        }
    }

    @Test
    public void createSetTest_shouldCreateEmptySet() {
        long idSet = setClient.create();
        try {
            Assert.assertEquals(Collections.EMPTY_SET, setService.getSetElements(idSet));
        } finally {
            setService.deleteSet(idSet);
        }
    }

    @Test
    public void  createSetWithElementsTest_shouldCreateSetWithElements() {
        Set<Long> idObjects = com.google.common.collect.Sets.newHashSet(objectService.createMultipleObjects(3));
        long idSet = setClient.createWithElements(idObjects);
        try {
            Assert.assertEquals(idObjects, setService.getSetElements(idSet));
        } finally {
            setService.deleteSet(idSet);
            objectService.deleteMultipleObjects(idObjects);
        }
    }

    @Test
    public void createSetWithElementsTest_noElements_shouldCreateEmptySet() {
        long idSet = setClient.createWithElements(Collections.emptySet());
        try {
            Assert.assertEquals(setService.getSetElements(idSet), Collections.EMPTY_SET);
        } finally {
            setService.deleteSet(idSet);
        }
    }

    @Test
    public void createSetWithElementsTest_oneElementDoesNotExist_shouldReturn400() {
        long idNonExistingObject = objectService.createObject();
        objectService.deleteObject(idNonExistingObject);
        Set<Long> idExistingObjects = com.google.common.collect.Sets.newHashSet(objectService.createMultipleObjects(3));
        Set<Long> idObjects = com.google.common.collect.Sets.newHashSet(idExistingObjects);
        idObjects.add(idNonExistingObject);
        try {
            setClient.createWithElements(idObjects);
            Assert.fail();
        } catch(FeignException e) {
            Assert.assertEquals(400, e.status());
            Assert.assertTrue(
                "Response body [" + e.contentUTF8() + "] should contain the ids of the nonexistent Object [" +
                    idNonExistingObject + "]",
                e.contentUTF8().contains(idNonExistingObject + "")
            );
        } finally {
            objectService.deleteMultipleObjects(idExistingObjects);
        }
    }

    @Test
    public void createSetWithElementsTest_multipleElementsDoNotExist_shouldReturn400() {
        Set<Long> idNonExistentObjects = com.google.common.collect.Sets.newHashSet(objectService.createMultipleObjects(3));
        objectService.deleteMultipleObjects(idNonExistentObjects);
        Set<Long> idExistingObjects = com.google.common.collect.Sets.newHashSet(objectService.createMultipleObjects(3));
        Set<Long> idObjects = com.google.common.collect.Sets.union(idNonExistentObjects, idExistingObjects);
        try {
            setClient.createWithElements(idObjects);
            Assert.fail();
        } catch(FeignException e) {
            Assert.assertEquals(400, e.status());
            for(long idObject : idNonExistentObjects) {
                Assert.assertTrue(
                    "Response body [" + e.contentUTF8() + "] should contain the id of the nonexistent Object [" +
                        idObject + "]",
                    e.contentUTF8().contains(idObject + "")
                );
            }
        } finally {
            objectService.deleteMultipleObjects(idExistingObjects);
        }
    }

    @Test
    public void copySetTest_setWithElements_shouldBeCopied() {
        Set<Long> idElements = com.google.common.collect.Sets.newHashSet(objectService.createMultipleObjects(3));
        long idSet = setService.createSetWithElements(idElements);
        try {
            long idCopiedSet = setClient.copy(idSet);
            Assert.assertTrue(setService.areEqual(idSet, idCopiedSet));
            Assert.assertNotEquals(idSet, idCopiedSet);
            setService.deleteSet(idCopiedSet);
        } finally {
            setService.deleteSet(idSet);
            objectService.deleteMultipleObjects(idElements);
        }
    }

    @Test
    public void copySetTest_emptySet_shouldBeCopied() {
        long idSet = setService.createEmptySet();
        try {
            long idCopiedSet = setClient.copy(idSet);
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
            setClient.copy(idSet);
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
        Set<Long> idObjects = com.google.common.collect.Sets.newHashSet(objectService.createMultipleObjects(3));
        long idSet = setService.createSetWithElements(idObjects);
        try {
            com.beumuth.math.client.settheory.set.Set set = setService.getSet(idSet).get();
            Set<Long> idElements = com.google.common.collect.Sets.newHashSet();
            for(Long idObject : idObjects) {
                idElements.add(elementService.getElementByNaturalKey(idSet, idObject).get().getId());
            }

            setClient.delete(set.getId());

            Assert.assertFalse(setService.doesSetExist(set.getId()));
            Assert.assertFalse(objectService.doesObjectExist(set.getIdObject()));
            for(Long idElement : idElements) {
                Assert.assertFalse(elementService.doesElementExist(idElement));
            }
        } finally {
            setService.deleteSet(idSet);
            objectService.deleteMultipleObjects(idObjects);
        }
    }

    @Test
    public void deleteSetTest_withNonExistingSet_shouldDoNothing() {
        long idSet = setService.createSet();
        setService.deleteSet(idSet);
        setClient.delete(idSet); //This should not throw an exception
    }

    @Test
    public void doesSetContainObjectTest_doesContainElement_shouldReturnTrue() {
        long idObject = objectService.createObject();
        long idSet = setService.createSetWithElements(com.google.common.collect.Sets.newHashSet(idObject));
        try {
            Assert.assertTrue(setClient.contains(idSet, idObject));
        } finally {
            objectService.deleteObject(idObject);
            setService.deleteSet(idSet);
        }
    }

    @Test
    public void doesSetContainObjectTest_doesNotContainElement_shouldReturnFalse() {
        long idObject = objectService.createObject();
        long idSet = setService.createSetWithElements(com.google.common.collect.Sets.newHashSet(idObject));
        setService.removeElementFromSet(idSet, idObject);
        try {
            Assert.assertFalse(setClient.contains(idSet, idObject));
        } finally {
            objectService.deleteObject(idObject);
            setService.deleteSet(idSet);
        }
    }

    @Test
    public void doesSetContainObjectTest_setDoesNotExist_shouldReturn404() {
        long idObject = objectService.createObject();
        long idSet = setService.createSet();
        setService.deleteSet(idSet);
        try {
            setClient.contains(idSet, idObject);
            Assert.fail();
        } catch(FeignException e) {
            Assert.assertEquals(404, e.status());
            Assert.assertTrue(
                "Response body [" + e.contentUTF8() + "] should contain the id of the nonexistent Set [" +
                    idSet + "]",
                e.contentUTF8().contains(idSet + "")
            );
        } finally {
            objectService.deleteObject(idObject);
        }
    }

    @Test
    public void doesSetContainObjectTest_elementDoesNotExist_shouldReturn404() {
        long idObject = objectService.createObject();
        long idSet = setService.createSet();
        objectService.deleteObject(idObject);
        try {
            setClient.contains(idSet, idObject);
            Assert.fail();
        } catch(FeignException e) {
            Assert.assertEquals(404, e.status());
            Assert.assertTrue(
                "Response body [" + e.contentUTF8() + "] should contain the id of the nonexistent Object [" +
                    idObject + "]",
                e.contentUTF8().contains(idObject + "")
            );
        } finally {
            setService.deleteSet(idSet);
        }
    }

    @Test
    public void doesSetContainObjects_doesContainElements_shouldReturnTrue() {
        Set<Long> idObjects = com.google.common.collect.Sets.newHashSet(objectService.createMultipleObjects(3));
        long idSet = setService.createSetWithElements(idObjects);
        try {
            Assert.assertTrue(setClient.containsAll(idSet, idObjects));
        } finally {
            setService.deleteSet(idSet);
            objectService.deleteMultipleObjects(idObjects);
        }
    }

    @Test
    public void doesSetContainObjects_doesNotContainAnElement_shouldReturnFalse() {
        Set<Long> idObjectsInSet = com.google.common.collect.Sets.newHashSet(objectService.createMultipleObjects(3));
        long idObjectsNotInSet = objectService.createObject();
        long idSet = setService.createSetWithElements(idObjectsInSet);
        Set<Long> idAllObjects = com.google.common.collect.Sets.newHashSet(idObjectsInSet);
        idAllObjects.add(idObjectsNotInSet);
        try {
            Assert.assertFalse(setClient.containsAll(idSet, idAllObjects));
        } finally {
            setService.deleteSet(idSet);
            objectService.deleteMultipleObjects(idAllObjects);
        }
    }

    @Test
    public void doesSetContainObjects_setDoesNotExist_shouldReturn404() {
        Set<Long> idObjects = com.google.common.collect.Sets.newHashSet(objectService.createMultipleObjects(3));
        long idSet = setService.createSet();
        setService.deleteSet(idSet);
        try {
            setClient.containsAll(idSet, idObjects);
        } catch(FeignException e) {
            Assert.assertEquals(404, e.status());
            Assert.assertTrue(
                "Response body [" + e.contentUTF8() + "] should contain the id of the nonexistent Set [" +
                    idSet + "]",
                e.contentUTF8().contains(idSet + "")
            );
        } finally {
            objectService.deleteMultipleObjects(idObjects);
        }
    }

    @Test
    public void doesSetContainObjects_objectsDoNotExist_shouldReturn404() {
        Set<Long> idObjects = com.google.common.collect.Sets.newHashSet(objectService.createMultipleObjects(3));
        Set<Long> idNonExistingObjects = com.google.common.collect.Sets.newHashSet(objectService.createMultipleObjects(2));
        objectService.deleteMultipleObjects(idNonExistingObjects);
        Set<Long> idAllObjects = com.google.common.collect.Sets.union(idObjects, idNonExistingObjects);
        long idSet = setService.createSetWithElements(idObjects);
        try {
            setClient.containsAll(idSet, idAllObjects);
        } catch(FeignException e) {
            Assert.assertEquals(404, e.status());
            for(Long idObject : idNonExistingObjects) {
                Assert.assertTrue(
                    "Response body [" + e.contentUTF8() + "] should contain the id of the nonexistent Object [" +
                        idObject + "]",
                    e.contentUTF8().contains(idObject + "")
                );
            }
        } finally {
            objectService.deleteMultipleObjects(idObjects);
            setService.deleteSet(idSet);
        }
    }

    @Test
    public void addObjectToSetTest_shouldBeAdded() {
        long idSet = setService.createSet();
        long idObject = objectService.createObject();
        try {
            setClient.addElement(idSet, idObject);
            Assert.assertTrue(setService.containsObject(idSet, idObject));
        } finally {
            setService.deleteSet(idSet);
            objectService.deleteObject(idObject);
        }
    }

    @Test
    public void addObjectToSetTest_setContainsObject_shouldDoNothing() {
        long idSet = setService.createSet();
        long idObject = objectService.createObject();
        setService.addObjectToSet(idSet, idObject);
        try {
            setClient.addElement(idSet, idObject);
            Assert.assertTrue(setService.containsObject(idSet, idObject));
        } finally {
            setService.deleteSet(idSet);
            objectService.deleteObject(idObject);
        }
    }

    @Test
    public void addObjectToSetTest_setDoesNotExist_shouldReturn404() {
        long idSet = setService.createSet();
        setService.deleteSet(idSet);
        long idObject = objectService.createObject();
        try {
            setClient.addElement(idSet, idObject);
            Assert.fail();
        } catch(FeignException e) {
            Assert.assertEquals(404, e.status());
            Assert.assertTrue(
                "Response body [" + e.contentUTF8() + "] should contain the id of the nonexistent Set [" + idSet + "]",
                e.contentUTF8().contains(idSet + "")
            );
        } finally {
            objectService.deleteObject(idObject);
        }
    }

    @Test
    public void addObjectToSetTest_objectDoesNotExist_shouldReturn404() {
        long idSet = setService.createSet();
        long idObject = objectService.createObject();
        objectService.deleteObject(idObject);
        try {
            setClient.addElement(idSet, idObject);
            Assert.fail();
        } catch(FeignException e) {
            Assert.assertEquals(404, e.status());
            Assert.assertTrue(
                "Response body [" + e.contentUTF8() + "] should contain the id of the nonexistent Object [" +
                    idObject + "]",
                e.contentUTF8().contains(idObject + "")
            );
        } finally {
            setService.deleteSet(idSet);
        }
    }

    @Test
    public void createAndAddObjectToSetTest_shouldSucceed() {
        long idSet = setService.createSet();
        try {
            long idElement = setClient.createAndAddElement(idSet);
            try {
                Assert.assertTrue(setService.containsObject(idSet, idElement));
            } finally {
                objectService.deleteObject(idElement);
            }
        } finally {
            setService.deleteSet(idSet);
        }
    }

    @Test
    public void createAndAddObjectToSetTest_setDoesNotExist_shouldReturn404() {
        long idSet = setService.createSet();
        setService.deleteSet(idSet);
        try {
            setClient.createAndAddElement(idSet);
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
    public void removeElementFromSetTest_objectInSet_shouldRemoveElement() {
        long idObject = objectService.createObject();
        long idSet = setService.createSetWithElements(com.google.common.collect.Sets.newHashSet(idObject));
        try {
            setClient.removeElement(idSet, idObject);
            Assert.assertFalse(setService.containsObject(idSet, idObject));
        } finally {
            objectService.deleteObject(idObject);
            setService.deleteSet(idSet);
        }
    }

    @Test
    public void removeElementFromSetTest_objectNotInSet_shouldDoNothing() {
        long idObject = objectService.createObject();
        long idSet = setService.createSet();
        try {
            setClient.removeElement(idSet, idObject);
        } finally {
            objectService.deleteObject(idObject);
            setService.deleteSet(idSet);
        }
    }

    @Test
    public void removeElementFromSetTest_setDoesNotExist_shouldReturn404() {
        long idObject = objectService.createObject();
        long idSet = setService.createSet();
        setService.deleteSet(idSet);
        try {
            setClient.removeElement(idSet, idObject);
            Assert.fail();
        } catch(FeignException e) {
            Assert.assertEquals(404, e.status());
            Assert.assertTrue(
                "Response body [" + e.contentUTF8() + "] should contain the id of the nonexistent Set [" + idSet + "]",
                e.contentUTF8().contains(idSet + "")
            );
        } finally {
            objectService.deleteObject(idObject);
        }
    }

    @Test
    public void removeElementFromSetTest_elementDoesNotExist_shouldReturn404() {
        long idObject = objectService.createObject();
        long idSet = setService.createSet();
        objectService.deleteObject(idObject);
        try {
            setClient.removeElement(idSet, idObject);
            Assert.fail();
        } catch(FeignException e) {
            Assert.assertEquals(404, e.status());
            Assert.assertTrue(
                "Response body [" + e.contentUTF8() + "] should contain the id of the nonexistent Object [" +
                    idObject + "]",
                e.contentUTF8().contains(idObject + "")
            );
        } finally {
            setService.deleteSet(idSet);
        }
    }

    @Test
    public void equalsTest_setsContainSameElements_shouldReturnTrue() {
        Set<Long> idElements = com.google.common.collect.Sets.newHashSet(objectService.createMultipleObjects(3));
        long idSetA = setService.createSetWithElements(idElements);
        long idSetB = setService.copySet(idSetA);
        try {
            Assert.assertTrue(setClient.areEqual(idSetA, idSetB));
        } finally {
            setService.deleteSet(idSetA);
            setService.deleteSet(idSetB);
            objectService.deleteMultipleObjects(idElements);
        }
    }

    @Test
    public void equalsTest_setsDoNotContainSameElements_shouldReturnFalse() {
        Set<Long> idElementsA = com.google.common.collect.Sets.newHashSet(objectService.createMultipleObjects(3));
        Set<Long> idElementsB = com.google.common.collect.Sets.newHashSet(objectService.createMultipleObjects(4));
        long idSetA = setService.createSetWithElements(idElementsA);
        long idSetB = setService.createSetWithElements(idElementsB);
        try {
            Assert.assertFalse(setClient.areEqual(idSetA, idSetB));
        } finally {
            setService.deleteSet(idSetA);
            setService.deleteSet(idSetB);
            objectService.deleteMultipleObjects(idElementsA);
            objectService.deleteMultipleObjects(idElementsB);
        }
    }

    @Test
    public void equalsTest_sameSetId_shouldReturnTrue() {
        Set<Long> idElements = com.google.common.collect.Sets.newHashSet(objectService.createMultipleObjects(3));
        long idSet = setService.createSetWithElements(idElements);
        try {
            Assert.assertTrue(setClient.areEqual(idSet, idSet));
        } finally {
            setService.deleteSet(idSet);
            objectService.deleteMultipleObjects(idElements);
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
        Set<Long> idElementsA = com.google.common.collect.Sets.newHashSet(objectService.createMultipleObjects(3));
        Set<Long> idElementsB = com.google.common.collect.Sets.union(
            com.google.common.collect.Sets.newHashSet(objectService.createMultipleObjects(2)),
            idElementsA
        );
        long idSetA = setService.createSetWithElements(idElementsA);
        long idSetB = setService.createSetWithElements(idElementsB);
        try {
            Assert.assertTrue(setClient.isSubset(idSetA, idSetB));
        } finally {
            setService.deleteSet(idSetA);
            setService.deleteSet(idSetB);
            objectService.deleteMultipleObjects(com.google.common.collect.Sets.union(idElementsA, idElementsB));
        }
    }

    @Test
    public void isSubsetTest_setAIsNotSubsetOfSetB_shouldReturnTrue() {
        Set<Long> idElementsB = com.google.common.collect.Sets.newHashSet(objectService.createMultipleObjects(3));
        Set<Long> idElementsA = com.google.common.collect.Sets.union(
            com.google.common.collect.Sets.newHashSet(objectService.createMultipleObjects(2)),
            idElementsB
        );
        long idSetA = setService.createSetWithElements(idElementsA);
        long idSetB = setService.createSetWithElements(idElementsB);
        try {
            Assert.assertFalse(setClient.isSubset(idSetA, idSetB));
        } finally {
            setService.deleteSet(idSetA);
            setService.deleteSet(idSetB);
            objectService.deleteMultipleObjects(com.google.common.collect.Sets.union(idElementsA, idElementsB));
        }
    }

    @Test
    public void isSubsetTest_setAIsEmptySet_shouldReturnTrue() {
        long idSetA = setService.createEmptySet();
        Set<Long> idElementsB = com.google.common.collect.Sets.newHashSet(objectService.createMultipleObjects(3));
        long idSetB = setService.createSetWithElements(idElementsB);
        try {
            Assert.assertTrue(setClient.isSubset(idSetA, idSetB));
        } finally {
            setService.deleteSet(idSetA);
            setService.deleteSet(idSetB);
            objectService.deleteMultipleObjects(idElementsB);
        }
    }

    @Test
    public void isSubsetTest_setAIsNonEmptySetBIsEmpty_shouldReturnFalse() {
        Set<Long> idElementsA = com.google.common.collect.Sets.newHashSet(objectService.createMultipleObjects(3));
        long idSetA = setService.createSetWithElements(idElementsA);
        long idSetB = setService.createEmptySet();
        try {
            Assert.assertFalse(setClient.isSubset(idSetA, idSetB));
        } finally {
            setService.deleteSet(idSetA);
            setService.deleteSet(idSetB);
            objectService.deleteMultipleObjects(idElementsA);
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
        Set<Long> idElementsA = com.google.common.collect.Sets.newHashSet(objectService.createMultipleObjects(3));
        long idSetA = setService.createSetWithElements(idElementsA);
        long idSetB = setService.copySet(idSetA);
        try {
            Assert.assertTrue(setClient.isSubset(idSetA, idSetB));
        } finally {
            setService.deleteSet(idSetA);
            setService.deleteSet(idSetB);
            objectService.deleteMultipleObjects(idElementsA);
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
    public void areDisjointTest_areDisjointAndNonempty_shouldReturnTrue() {
        Set<Long> idElementsA = com.google.common.collect.Sets.newHashSet(objectService.createMultipleObjects(4));
        Set<Long> idElementsB = com.google.common.collect.Sets.newHashSet(objectService.createMultipleObjects(5));
        long idSetA = setService.createSetWithElements(idElementsA);
        long idSetB = setService.createSetWithElements(idElementsB);
        try {
            Assert.assertTrue(setClient.areDisjoint(idSetA, idSetB));
        } finally {
            setService.deleteSet(idSetA);
            setService.deleteSet(idSetB);
            objectService.deleteMultipleObjects(idElementsA);
            objectService.deleteMultipleObjects(idElementsB);
        }
    }

    @Test
    public void areDisjointTest_AIsEmpty_shouldReturnTrue() {
        Set<Long> idElementsB = com.google.common.collect.Sets.newHashSet(objectService.createMultipleObjects(5));
        long idSetA = setService.createEmptySet();
        long idSetB = setService.createSetWithElements(idElementsB);
        try {
            Assert.assertTrue(setClient.areDisjoint(idSetA, idSetB));
        } finally {
            setService.deleteSet(idSetA);
            setService.deleteSet(idSetB);
            objectService.deleteMultipleObjects(idElementsB);
        }
    }

    @Test
    public void areDisjointTest_BIsEmpty_shouldReturnTrue() {
        Set<Long> idElementsA = com.google.common.collect.Sets.newHashSet(objectService.createMultipleObjects(5));
        long idSetA = setService.createSetWithElements(idElementsA);
        long idSetB = setService.createEmptySet();
        try {
            Assert.assertTrue(setClient.areDisjoint(idSetA, idSetB));
        } finally {
            setService.deleteSet(idSetA);
            setService.deleteSet(idSetB);
            objectService.deleteMultipleObjects(idElementsA);
        }
    }

    @Test
    public void areDisjointTest_AAndBAreEmpty_shouldReturnTrue() {
        long idSetA = setService.createEmptySet();
        long idSetB = setService.createEmptySet();
        try {
            Assert.assertTrue(setClient.areDisjoint(idSetA, idSetB));
        } finally {
            setService.deleteSet(idSetA);
            setService.deleteSet(idSetB);
        }
    }

    @Test
    public void areDisjointTest_areNotDisjoint_shouldReturnFalse() {
        long idSharedElement = objectService.createObject();
        Set<Long> idElementsA = com.google.common.collect.Sets.newHashSet(objectService.createMultipleObjects(4));
        idElementsA.add(idSharedElement);
        Set<Long> idElementsB = com.google.common.collect.Sets.newHashSet(objectService.createMultipleObjects(5));
        idElementsB.add(idSharedElement);
        long idSetA = setService.createSetWithElements(idElementsA);
        long idSetB = setService.createSetWithElements(idElementsB);
        try {
            Assert.assertFalse(setClient.areDisjoint(idSetA, idSetB));
        } finally {
            setService.deleteSet(idSetA);
            setService.deleteSet(idSetB);
            objectService.deleteMultipleObjects(idElementsA);
            objectService.deleteMultipleObjects(idElementsB);
        }
    }

    @Test
    public void areDisjointTest_aDoesNotExist_shouldReturn404() {
        Set<Long> idElementsB = com.google.common.collect.Sets.newHashSet(objectService.createMultipleObjects(5));
        long idSetA = setService.createSet();
        setService.deleteSet(idSetA);
        long idSetB = setService.createSetWithElements(idElementsB);
        try {
            setClient.areDisjoint(idSetA, idSetB);
            Assert.fail();
        } catch(FeignException e) {
            Assert.assertEquals(404, e.status());
            Assert.assertTrue(
                "Response body [" + e.contentUTF8() + "] should contain the id of the nonexistent Set [" + idSetA + "]",
                e.contentUTF8().contains(idSetA + "")
            );
        } finally {
            setService.deleteSet(idSetB);
            objectService.deleteMultipleObjects(idElementsB);
        }
    }

    @Test
    public void areDisjointTest_bDoesNotExist_shouldReturn404() {
        Set<Long> idElementsA = com.google.common.collect.Sets.newHashSet(objectService.createMultipleObjects(5));
        long idSetA = setService.createSetWithElements(idElementsA);
        long idSetB = setService.createSet();
        setService.deleteSet(idSetB);
        try {
            setClient.areDisjoint(idSetA, idSetB);
            Assert.fail();
        } catch(FeignException e) {
            Assert.assertEquals(404, e.status());
            Assert.assertTrue(
                "Response body [" + e.contentUTF8() + "] should contain the id of the nonexistent Set [" + idSetB + "]",
                e.contentUTF8().contains(idSetB + "")
            );
        } finally {
            setService.deleteSet(idSetA);
            objectService.deleteMultipleObjects(idElementsA);
        }
    }

    @Test
    public void areDisjointMultipleTest_areDisjoint_shouldReturnTrue() {
        Set<Long> idElementsA = com.google.common.collect.Sets.newHashSet(objectService.createMultipleObjects(5));
        Set<Long> idElementsB = com.google.common.collect.Sets.newHashSet(objectService.createMultipleObjects(5));
        Set<Long> idElementsC = com.google.common.collect.Sets.newHashSet(objectService.createMultipleObjects(5));
        Set<Long> idElementsD = com.google.common.collect.Sets.newHashSet(objectService.createMultipleObjects(5));
        long idSetA = setService.createSetWithElements(idElementsA);
        long idSetB = setService.createSetWithElements(idElementsB);
        long idSetC = setService.createSetWithElements(idElementsC);
        long idSetD = setService.createSetWithElements(idElementsD);
        try {
            Assert.assertTrue(
                setClient.areDisjoint(
                    com.google.common.collect.Sets.newHashSet(idSetA, idSetB, idSetC, idSetD)
                )
            );
        } finally {
            setService.deleteSet(idSetA);
            setService.deleteSet(idSetB);
            setService.deleteSet(idSetC);
            setService.deleteSet(idSetD);
            objectService.deleteMultipleObjects(idElementsA);
            objectService.deleteMultipleObjects(idElementsB);
            objectService.deleteMultipleObjects(idElementsC);
            objectService.deleteMultipleObjects(idElementsD);
        }
    }

    @Test
    public void areDisjointMultipleTest_areNotDisjoint_shouldReturnTrue() {
        long idSharedElement = objectService.createObject();  //Shared by A and B
        Set<Long> idElementsA = com.google.common.collect.Sets.newHashSet(objectService.createMultipleObjects(5));
        idElementsA.add(idSharedElement);
        Set<Long> idElementsB = com.google.common.collect.Sets.newHashSet(objectService.createMultipleObjects(5));
        idElementsB.add(idSharedElement);
        Set<Long> idElementsC = com.google.common.collect.Sets.newHashSet(objectService.createMultipleObjects(5));
        Set<Long> idElementsD = com.google.common.collect.Sets.newHashSet(objectService.createMultipleObjects(5));
        long idSetA = setService.createSetWithElements(idElementsA);
        long idSetB = setService.createSetWithElements(idElementsB);
        long idSetC = setService.createSetWithElements(idElementsC);
        long idSetD = setService.createSetWithElements(idElementsD);
        try {
            Assert.assertFalse(
                setClient.areDisjoint(
                    com.google.common.collect.Sets.newHashSet(idSetA, idSetB, idSetC, idSetD)
                )
            );
        } finally {
            setService.deleteSet(idSetA);
            setService.deleteSet(idSetB);
            setService.deleteSet(idSetC);
            setService.deleteSet(idSetD);
            objectService.deleteMultipleObjects(idElementsA);
            objectService.deleteMultipleObjects(idElementsB);
            objectService.deleteMultipleObjects(idElementsC);
            objectService.deleteMultipleObjects(idElementsD);
        }
    }

    @Test
    public void areDisjointMultipleTest_areAllEmpty_shouldReturnTrue() {
        long idSetA = setService.createEmptySet();
        long idSetB = setService.createEmptySet();
        long idSetC = setService.createEmptySet();
        long idSetD = setService.createEmptySet();
        try {
            Assert.assertTrue(
                setClient.areDisjoint(
                    com.google.common.collect.Sets.newHashSet(idSetA, idSetB, idSetC, idSetD)
                )
            );
        } finally {
            setService.deleteSet(idSetA);
            setService.deleteSet(idSetB);
            setService.deleteSet(idSetC);
            setService.deleteSet(idSetD);
        }
    }

    @Test
    public void areDisjointMultipleTest_areAllEqual_shouldReturnFalse() {
        Set<Long> idElements = com.google.common.collect.Sets.newHashSet(objectService.createMultipleObjects(5));
        long idSetA = setService.createSetWithElements(idElements);
        long idSetB = setService.createSetWithElements(idElements);
        long idSetC = setService.createSetWithElements(idElements);
        long idSetD = setService.createSetWithElements(idElements);
        try {
            Assert.assertFalse(
                setClient.areDisjoint(
                    com.google.common.collect.Sets.newHashSet(idSetA, idSetB, idSetC, idSetD)
                )
            );
        } finally {
            setService.deleteSet(idSetA);
            setService.deleteSet(idSetB);
            setService.deleteSet(idSetC);
            setService.deleteSet(idSetD);
            objectService.deleteMultipleObjects(idElements);
        }
    }

    @Test
    public void areDisjointMultipleTest_emptySet_shouldReturn400() {
        try {
            setClient.areDisjoint(com.google.common.collect.Sets.newHashSet());
            Assert.fail();
        } catch(FeignException e) {
            Assert.assertEquals(400, e.status());
        }
    }

    @Test
    public void areDisjointMultipleTest_oneSet_shouldReturn400() {
        Set<Long> idElements = com.google.common.collect.Sets.newHashSet(objectService.createMultipleObjects(5));
        long idSet = setService.createSetWithElements(idElements);
        try {
            setClient.areDisjoint(com.google.common.collect.Sets.newHashSet(idSet));
            Assert.fail();
        } catch(FeignException e) {
            Assert.assertEquals(400, e.status());
        } finally {
            setService.deleteSet(idSet);
            objectService.deleteMultipleObjects(idElements);
        }
    }

    @Test
    public void areDisjointMultipleTest_setDoesNotExist_shouldReturn400() {
        Set<Long> idElementsB = com.google.common.collect.Sets.newHashSet(objectService.createMultipleObjects(5));
        Set<Long> idElementsC = com.google.common.collect.Sets.newHashSet(objectService.createMultipleObjects(5));
        Set<Long> idElementsD = com.google.common.collect.Sets.newHashSet(objectService.createMultipleObjects(5));
        long idSetA = setService.createSet();
        setService.deleteSet(idSetA);
        long idSetB = setService.createSetWithElements(idElementsB);
        long idSetC = setService.createSetWithElements(idElementsC);
        long idSetD = setService.createSetWithElements(idElementsD);
        try {
            setClient.areDisjoint(
                com.google.common.collect.Sets.newHashSet(idSetA, idSetB, idSetC, idSetD)
            );
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
            setService.deleteSet(idSetD);
            objectService.deleteMultipleObjects(idElementsB);
            objectService.deleteMultipleObjects(idElementsC);
            objectService.deleteMultipleObjects(idElementsD);
        }
    }

    @Test
    public void isPartitionTest_isPartitionWithMultipleElements_shouldReturnTrue() {
        Set<Long> idElementsSubsetA = com.google.common.collect.Sets.newHashSet(objectService.createMultipleObjects(3));
        Set<Long> idElementsSubsetB = com.google.common.collect.Sets.newHashSet(objectService.createMultipleObjects(5));
        Set<Long> idElementsSubsetC = com.google.common.collect.Sets.newHashSet(objectService.createMultipleObjects(2));
        Set<Long> idElementsSet = com.google.common.collect.Sets.union(com.google.common.collect.Sets.union(idElementsSubsetA, idElementsSubsetB), idElementsSubsetC);
        long idSubsetA = setService.createSetWithElements(idElementsSubsetA);
        long idSubsetB = setService.createSetWithElements(idElementsSubsetB);
        long idSubsetC = setService.createSetWithElements(idElementsSubsetC);
        long idSet = setService.createSetWithElements(idElementsSet);
        try {
            Assert.assertTrue(
                setClient.isPartition(
                    com.google.common.collect.Sets.newHashSet(idSubsetA, idSubsetB, idSubsetC),
                    idSet
                )
            );
        } finally {
            setService.deleteSet(idSubsetA);
            setService.deleteSet(idSubsetB);
            setService.deleteSet(idSubsetC);
            setService.deleteSet(idSet);
            objectService.deleteMultipleObjects(idElementsSet);
        }
    }

    @Test
    public void isPartitionTest_partitionIsSingletonWithSet_shouldReturnTrue() {
        Set<Long> idElements = com.google.common.collect.Sets.newHashSet(objectService.createMultipleObjects(5));
        long idSet = setService.createSetWithElements(idElements);
        try {
            //If A is a set, then {A} should be a partition of A
            Assert.assertTrue(setClient.isPartition(com.google.common.collect.Sets.newHashSet(idSet), idSet));
        } finally {
            setService.deleteSet(idSet);
            objectService.deleteMultipleObjects(idElements);
        }
    }

    @Test
    public void isPartitionTest_unionDoesNotEqualSet_shouldReturnFalse() {
        Set<Long> idElementsSubsetA = com.google.common.collect.Sets.newHashSet(objectService.createMultipleObjects(3));
        Set<Long> idElementsSubsetB = com.google.common.collect.Sets.newHashSet(objectService.createMultipleObjects(5));
        Set<Long> idElementsSubsetC = com.google.common.collect.Sets.newHashSet(objectService.createMultipleObjects(2));
        Long idBonusElement = objectService.createObject();
        Set<Long> idElementsSet = com.google.common.collect.Sets.newHashSet(idBonusElement);
        idElementsSet.addAll(idElementsSubsetA);
        idElementsSet.addAll(idElementsSubsetB);
        idElementsSet.addAll(idElementsSubsetC);
        long idSubsetA = setService.createSetWithElements(idElementsSubsetA);
        long idSubsetB = setService.createSetWithElements(idElementsSubsetB);
        long idSubsetC = setService.createSetWithElements(idElementsSubsetC);
        long idSet = setService.createSetWithElements(idElementsSet);
        try {
            Assert.assertFalse(
                setClient.isPartition(
                    com.google.common.collect.Sets.newHashSet(idSubsetA, idSubsetB, idSubsetC),
                    idSet
                )
            );
        } finally {
            setService.deleteSet(idSubsetA);
            setService.deleteSet(idSubsetB);
            setService.deleteSet(idSubsetC);
            setService.deleteSet(idSet);
            objectService.deleteMultipleObjects(idElementsSet);
        }
    }

    @Test
    public void isPartitionTest_partitionNotDisjoint_shouldReturnFalse() {
        long idSharedElement = objectService.createObject(); //Shared by A and B
        Set<Long> idElementsSubsetA = com.google.common.collect.Sets.newHashSet(objectService.createMultipleObjects(3));
        idElementsSubsetA.add(idSharedElement);
        Set<Long> idElementsSubsetB = com.google.common.collect.Sets.newHashSet(objectService.createMultipleObjects(5));
        idElementsSubsetB.add(idSharedElement);
        Set<Long> idElementsSubsetC = com.google.common.collect.Sets.newHashSet(objectService.createMultipleObjects(2));
        Set<Long> idElementsSet = com.google.common.collect.Sets.union(com.google.common.collect.Sets.union(idElementsSubsetA, idElementsSubsetB), idElementsSubsetC);
        long idSubsetA = setService.createSetWithElements(idElementsSubsetA);
        long idSubsetB = setService.createSetWithElements(idElementsSubsetB);
        long idSubsetC = setService.createSetWithElements(idElementsSubsetC);
        long idSet = setService.createSetWithElements(idElementsSet);
        try {
            Assert.assertFalse(
                setClient.isPartition(
                    com.google.common.collect.Sets.newHashSet(idSubsetA, idSubsetB, idSubsetC),
                    idSet
                )
            );
        } finally {
            setService.deleteSet(idSubsetA);
            setService.deleteSet(idSubsetB);
            setService.deleteSet(idSubsetC);
            setService.deleteSet(idSet);
            objectService.deleteMultipleObjects(idElementsSet);
        }
    }

    @Test
    public void isPartitionTest_setIsEmpty_shouldReturnFalse() {
        long idEmptySet = setService.createEmptySet();
        Set<Long> idElements = com.google.common.collect.Sets.newHashSet(objectService.createMultipleObjects(5));
        long idNonEmptySet = setService.createSetWithElements(idElements);
        try {
            Assert.assertFalse(
                setClient.isPartition(com.google.common.collect.Sets.newHashSet(idNonEmptySet), idEmptySet)
            );
        } finally {
            setService.deleteSet(idEmptySet);
            setService.deleteSet(idNonEmptySet);
            objectService.deleteMultipleObjects(idElements);
        }
    }

    @Test
    public void isPartitionTest_partitionEmptySets_setEmptySet_shouldReturnTrue() {
        long idSet = setService.createEmptySet();
        long idPartitionSetA = setService.createEmptySet();
        long idPartitionSetB = setService.createEmptySet();
        long idPartitionSetC = setService.createEmptySet();
        try {
            Assert.assertTrue(
                setClient.isPartition(
                    com.google.common.collect.Sets.newHashSet(idPartitionSetA, idPartitionSetB, idPartitionSetC),
                    idSet
                )
            );
        } finally {
            setService.deleteSet(idSet);
            setService.deleteSet(idPartitionSetA);
            setService.deleteSet(idPartitionSetB);
            setService.deleteSet(idPartitionSetC);
        }
    }

    @Test
    public void isPartitionTest_partitionEmptySet_setIsNonEmpty_shouldReturnFalse() {
        long idElement = objectService.createObject();
        long idSet = setService.createSetWithElements(com.google.common.collect.Sets.newHashSet(idElement));
        long idPartitionSet = setService.createEmptySet();
        try {
            Assert.assertFalse(
                setClient.isPartition(
                    com.google.common.collect.Sets.newHashSet(idPartitionSet),
                    idSet
                )
            );
        } finally {
            setService.deleteSet(idSet);
            setService.deleteSet(idPartitionSet);
            objectService.deleteObject(idElement);
        }
    }

    @Test
    public void isPartitionSet_partitionSetIsEmpty_shouldReturn400() {
        long idElement = objectService.createObject();
        long idSet = setService.createSetWithElements(com.google.common.collect.Sets.newHashSet(idElement));
        try {
            setClient.isPartition(
                com.google.common.collect.Sets.newHashSet(),
                idSet
            );
            Assert.fail();
        } catch(FeignException e) {
            Assert.assertEquals(400, e.status());
        } finally {
            setService.deleteSet(idSet);
            objectService.deleteObject(idElement);
        }
    }

    @Test
    public void isPartitionTest_setDoesNotExist_shouldReturn404() {
        long idSet = setService.createSet();
        setService.deleteSet(idSet);
        long idPartitionSet = setService.createEmptySet();
        try {
            setClient.isPartition(
                com.google.common.collect.Sets.newHashSet(idPartitionSet),
                idSet
            );
            Assert.fail();
        } catch(FeignException e) {
            Assert.assertEquals(404, e.status());
            Assert.assertTrue(
                "Response body [" + e.contentUTF8() + "] should contain the id of the nonexistent Set [" + idSet + "]",
                e.contentUTF8().contains(idSet + "")
            );
        } finally {
            setService.deleteSet(idPartitionSet);
        }
    }

    @Test
    public void isPartitionTest_setInPartitionDoesNotExist_shouldReturn400() {
        Set<Long> idElementsSubsetA = com.google.common.collect.Sets.newHashSet(objectService.createMultipleObjects(3));
        Set<Long> idElementsSubsetB = com.google.common.collect.Sets.newHashSet(objectService.createMultipleObjects(5));
        Set<Long> idElementsSet = com.google.common.collect.Sets.union(idElementsSubsetA, idElementsSubsetB);
        long idSubsetA = setService.createSetWithElements(idElementsSubsetA);
        long idSubsetB = setService.createSetWithElements(idElementsSubsetB);
        long idSubsetC = setService.createSet();
        setService.deleteSet(idSubsetC);
        long idSet = setService.createSetWithElements(idElementsSet);
        try {
            setClient.isPartition(
                com.google.common.collect.Sets.newHashSet(idSubsetA, idSubsetB, idSubsetC),
                idSet
            );
            Assert.fail();
        } catch(FeignException e) {
            Assert.assertEquals(400, e.status());
            Assert.assertTrue(
                "Response body [" + e.contentUTF8() + "] should contain the id of the nonexistent Set [" + idSubsetC +
                    "]",
                e.contentUTF8().contains(idSubsetC + "")
            );
        } finally {
            setService.deleteSet(idSubsetA);
            setService.deleteSet(idSubsetB);
            setService.deleteSet(idSet);
            objectService.deleteMultipleObjects(idElementsSet);
        }
    }

    @Test
    public void setCardinalityTest_nonEmptySet_shouldReturnSize() {
        int numElements = 5;
        Set<Long> idElements = com.google.common.collect.Sets.newHashSet(objectService.createMultipleObjects(numElements));
        long idSet = setService.createSetWithElements(idElements);
        try {
            Assert.assertEquals(numElements, setClient.cardinality(idSet));
        } finally {
            setService.deleteSet(idSet);
            objectService.deleteMultipleObjects(idElements);
        }
    }

    @Test
    public void setCardinalityTest_emptySet_shouldReturnZero() {
        long idSet = setService.createEmptySet();
        try {
            Assert.assertEquals(0, setClient.cardinality(idSet));
        } finally {
            setService.deleteSet(idSet);
        }
    }

    @Test
    public void setCardinalityTest_nonExistentSet_shouldReturn404() {
        long idSet = setService.createSet();
        setService.deleteSet(idSet);
        try {
            setClient.cardinality(idSet);
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
            Assert.assertTrue(setClient.isEmpty(idSet));
        } finally {
            setService.deleteSet(idSet);
        }
    }

    @Test
    public void isEmptySetTest_setIsNotEmpty_shouldReturnFalse() {
        Set<Long> idElements = com.google.common.collect.Sets.newHashSet(objectService.createMultipleObjects(3));
        long idSet = setService.createSetWithElements(idElements);
        try {
            Assert.assertFalse(setClient.isEmpty(idSet));
        } finally {
            setService.deleteSet(idSet);
            objectService.deleteMultipleObjects(idElements);
        }
    }

    @Test
    public void isEmptySetTest_setDoesNotExist_shouldReturn404() {
        long idSet = setService.createSet();
        setService.deleteSet(idSet);
        try {
            setClient.isEmpty(idSet);
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
        List<Long> idAllElements = objectService.createMultipleObjects(12);
        Set<Long> idCommonElements = com.google.common.collect.Sets.newHashSet(idAllElements.subList(3, 6));
        long idSetA = setService.createSetWithElements(
            com.google.common.collect.Sets.newHashSet(idAllElements.subList(0, 6))
        );
        long idSetB = setService.createSetWithElements(
            com.google.common.collect.Sets.newHashSet(idAllElements.subList(3, idAllElements.size()))
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
            objectService.deleteMultipleObjects(com.google.common.collect.Sets.newHashSet(idAllElements));
        }
    }

    @Test
    public void intersectionTest_setAAndSetBShareNoCommonElements_shouldReturnEmptySet() {
        Set<Long> idElementsA = com.google.common.collect.Sets.newHashSet(objectService.createMultipleObjects(5));
        Set<Long> idElementsB = com.google.common.collect.Sets.newHashSet(objectService.createMultipleObjects(6));
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
            objectService.deleteMultipleObjects(idElementsA);
            objectService.deleteMultipleObjects(idElementsB);
        }
    }

    @Test
    public void intersectionTest_setAAndSetBShareSameElements_shouldReturnSameElements() {
        Set<Long> idElements = com.google.common.collect.Sets.newHashSet(objectService.createMultipleObjects(5));
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
            objectService.deleteMultipleObjects(idElements);
        }
    }

    @Test
    public void intersectionTest_setAIsSetB_shouldReturnCopiedSet() {
        Set<Long> idElements = com.google.common.collect.Sets.newHashSet(objectService.createMultipleObjects(5));
        long idSet = setService.createSetWithElements(idElements);
        try {
            long idIntersectionSet = setClient.intersection(idSet, idSet);
            Assert.assertNotEquals(idIntersectionSet, idSet);
            Assert.assertTrue(setService.areEqual(idSet, idIntersectionSet));
            setService.deleteSet(idIntersectionSet);
        } finally {
            setService.deleteSet(idSet);
            objectService.deleteMultipleObjects(idElements);
        }
    }

    @Test
    public void intersectionTest_setAIsEmptySet_shouldReturnEmptySet() {
        Set<Long> idElementsA = com.google.common.collect.Sets.newHashSet(objectService.createMultipleObjects(5));
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
            objectService.deleteMultipleObjects(idElementsA);
        }
    }

    @Test
    public void intersectionTest_setBIsEmptySet_shouldReturnEmptySet() {
        Set<Long> idElementsB = com.google.common.collect.Sets.newHashSet(objectService.createMultipleObjects(5));
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
            objectService.deleteMultipleObjects(idElementsB);
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
        List<Long> idAllElements = objectService.createMultipleObjects(15);
        long idSetA = setService.createSetWithElements(
            com.google.common.collect.Sets.newHashSet(idAllElements.subList(0, 8))
        );
        long idSetB = setService.createSetWithElements(
            com.google.common.collect.Sets.newHashSet(idAllElements.subList(4, 12))
        );
        long idSetC = setService.createSetWithElements(
            com.google.common.collect.Sets.newHashSet(
                Iterables.concat(
                    idAllElements.subList(4, 7),
                    idAllElements.subList(9, idAllElements.size())
                )
            )
        );
        try {
            long idIntersectionSet = setClient.intersection(com.google.common.collect.Sets.newHashSet(idSetA, idSetB, idSetC));
            Assert.assertNotEquals(idSetA, idIntersectionSet);
            Assert.assertNotEquals(idSetB, idIntersectionSet);
            Assert.assertNotEquals(idSetC, idIntersectionSet);
            Assert.assertEquals(
                com.google.common.collect.Sets.newHashSet(idAllElements.subList(4, 7)),
                setService.getSetElements(idIntersectionSet)
            );
            setService.deleteSet(idIntersectionSet);
        } finally {
            setService.deleteSet(idSetA);
            setService.deleteSet(idSetB);
            setService.deleteSet(idSetC);
            objectService.deleteMultipleObjects(com.google.common.collect.Sets.newHashSet(idAllElements));
        }
    }

    @Test
    public void intersectionMultipleSetsTest_threeSetsWhereOneHasNoCommonElements_shouldReturnEmptySet() {
        List<Long> idAllElements = objectService.createMultipleObjects(15);
        long idSetA = setService.createSetWithElements(
            com.google.common.collect.Sets.newHashSet(idAllElements.subList(0, 3))
        );
        long idSetB = setService.createSetWithElements(
            com.google.common.collect.Sets.newHashSet(idAllElements.subList(3, 12))
        );
        long idSetC = setService.createSetWithElements(
            com.google.common.collect.Sets.newHashSet(idAllElements.subList(12, idAllElements.size()))
        );
        try {
            long idIntersectionSet = setClient.intersection(com.google.common.collect.Sets.newHashSet(idSetA, idSetB, idSetC));
            Assert.assertNotEquals(idSetA, idIntersectionSet);
            Assert.assertNotEquals(idSetB, idIntersectionSet);
            Assert.assertNotEquals(idSetC, idIntersectionSet);
            Assert.assertTrue(setService.isEmpty(idIntersectionSet));
            setService.deleteSet(idIntersectionSet);
        } finally {
            setService.deleteSet(idSetA);
            setService.deleteSet(idSetB);
            setService.deleteSet(idSetC);
            objectService.deleteMultipleObjects(com.google.common.collect.Sets.newHashSet(idAllElements));
        }
    }

    @Test
    public void intersectionMultipleSetsTest_threeSetsThatAreEqual_shouldReturnEqualSet() {
        Set<Long> idElements = com.google.common.collect.Sets.newHashSet(objectService.createMultipleObjects(5));
        long idSetA = setService.createSetWithElements(idElements);
        long idSetB = setService.createSetWithElements(idElements);
        long idSetC = setService.createSetWithElements(idElements);
        try {
            long idIntersectionSet = setClient.intersection(com.google.common.collect.Sets.newHashSet(idSetA, idSetB, idSetC));
            Assert.assertNotEquals(idSetA, idIntersectionSet);
            Assert.assertNotEquals(idSetB, idIntersectionSet);
            Assert.assertNotEquals(idSetC, idIntersectionSet);
            Assert.assertEquals(idElements, setService.getSetElements(idIntersectionSet));
            setService.deleteSet(idIntersectionSet);
        } finally {
            setService.deleteSet(idSetA);
            setService.deleteSet(idSetB);
            setService.deleteSet(idSetC);
            objectService.deleteMultipleObjects(idElements);
        }
    }

    @Test
    public void intersectionMultipleSetsTest_threeSetsThatAreTheSame_shouldReturnEqualSet() {
        Set<Long> idElements = com.google.common.collect.Sets.newHashSet(objectService.createMultipleObjects(5));
        long idSet = setService.createSetWithElements(idElements);
        try {
            long idIntersectionSet = setClient.intersection(com.google.common.collect.Sets.newHashSet(idSet, idSet, idSet));
            Assert.assertNotEquals(idSet, idIntersectionSet);
            Assert.assertEquals(idElements, setService.getSetElements(idIntersectionSet));
            setService.deleteSet(idIntersectionSet);
        } finally {
            setService.deleteSet(idSet);
            objectService.deleteMultipleObjects(idElements);
        }
    }

    @Test
    public void intersectionMultipleSetsTest_threeSetsWhereOneIsEmptySet_shouldReturnEmptySet() {
        Set<Long> idElements = com.google.common.collect.Sets.newHashSet(objectService.createMultipleObjects(5));
        long idSetA = setService.createEmptySet();
        long idSetB = setService.createSetWithElements(idElements);
        long idSetC = setService.createSetWithElements(idElements);
        try {
            long idIntersectionSet = setClient.intersection(com.google.common.collect.Sets.newHashSet(idSetA, idSetB, idSetC));
            Assert.assertNotEquals(idSetA, idIntersectionSet);
            Assert.assertNotEquals(idSetB, idIntersectionSet);
            Assert.assertNotEquals(idSetC, idIntersectionSet);
            Assert.assertTrue(setService.isEmpty(idIntersectionSet));
            setService.deleteSet(idIntersectionSet);
        } finally {
            setService.deleteSet(idSetA);
            setService.deleteSet(idSetB);
            setService.deleteSet(idSetC);
            objectService.deleteMultipleObjects(idElements);
        }
    }

    @Test
    public void intersectionMultipleSetsTest_threeSetsThatAreEmptySets_shouldReturnEmptySet() {
        long idSetA = setService.createEmptySet();
        long idSetB = setService.createEmptySet();
        long idSetC = setService.createEmptySet();
        try {
            long idIntersectionSet = setClient.intersection(com.google.common.collect.Sets.newHashSet(idSetA, idSetB, idSetC));
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
            setClient.intersection(com.google.common.collect.Sets.newHashSet(idSetA, idSetB, idSetC));
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
        Set<Long> idElementsA = com.google.common.collect.Sets.newHashSet(objectService.createMultipleObjects(5));
        Set<Long> idElementsB = com.google.common.collect.Sets.newHashSet(objectService.createMultipleObjects(6));
        long idSetA = setService.createSetWithElements(idElementsA);
        long idSetB = setService.createSetWithElements(idElementsB);
        try {
            long idUnion = setClient.union(idSetA, idSetB);
            Assert.assertNotEquals(idSetA, idUnion);
            Assert.assertNotEquals(idSetB, idUnion);
            Assert.assertEquals(
                com.google.common.collect.Sets.union(idElementsA, idElementsB),
                setService.getSetElements(idUnion)
            );
            setService.deleteSet(idUnion);
        } finally {
            objectService.deleteMultipleObjects(idElementsA);
            objectService.deleteMultipleObjects(idElementsB);
            setService.deleteSet(idSetA);
            setService.deleteSet(idSetB);
        }
    }

    @Test
    public void unionTest_setsNonDisjoint_shouldReturnUnion() {
        List<Long> idAllElements = objectService.createMultipleObjects(6);
        long idSetA = setService.createSetWithElements(
            com.google.common.collect.Sets.newHashSet(idAllElements.subList(0, 4))
        );
        long idSetB = setService.createSetWithElements(
            com.google.common.collect.Sets.newHashSet(idAllElements.subList(2, idAllElements.size()))
        );
        try {
            long idUnion = setClient.union(idSetA, idSetB);
            Assert.assertNotEquals(idSetA, idUnion);
            Assert.assertNotEquals(idSetB, idUnion);
            Assert.assertEquals(
                com.google.common.collect.Sets.newHashSet(idAllElements),
                setService.getSetElements(idUnion)
            );
            setService.deleteSet(idUnion);
        } finally {
            objectService.deleteMultipleObjects(com.google.common.collect.Sets.newHashSet(idAllElements));
            setService.deleteSet(idSetA);
            setService.deleteSet(idSetB);
        }
    }

    @Test
    public void unionTest_twoEqualSets_shouldReturnEqualSet() {
        Set<Long> idElements = com.google.common.collect.Sets.newHashSet(objectService.createMultipleObjects(5));
        long idSetA = setService.createSetWithElements(idElements);
        long idSetB = setService.createSetWithElements(idElements);
        try {
            long idUnion = setClient.union(idSetA, idSetB);
            Assert.assertNotEquals(idSetA, idUnion);
            Assert.assertNotEquals(idSetB, idUnion);
            Assert.assertEquals(idElements, setService.getSetElements(idUnion));
            setService.deleteSet(idUnion);
        } finally {
            objectService.deleteMultipleObjects(idElements);
            setService.deleteSet(idSetA);
            setService.deleteSet(idSetB);
        }
    }

    @Test
    public void unionTest_setsAreSame_shouldReturnEqualSet() {
        Set<Long> idElements = com.google.common.collect.Sets.newHashSet(objectService.createMultipleObjects(5));
        long idSet = setService.createSetWithElements(idElements);
        try {
            long idUnion = setClient.union(idSet, idSet);
            Assert.assertNotEquals(idSet, idUnion);
            Assert.assertEquals(idElements, setService.getSetElements(idUnion));
            setService.deleteSet(idUnion);
        } finally {
            objectService.deleteMultipleObjects(idElements);
            setService.deleteSet(idSet);
        }
    }

    @Test
    public void unionTest_setAEmptySetBNonEmpty_shouldReturnSetB() {
        Set<Long> idElementsB = com.google.common.collect.Sets.newHashSet(objectService.createMultipleObjects(5));
        long idSetA = setService.createEmptySet();
        long idSetB = setService.createSetWithElements(idElementsB);
        try {
            long idUnion = setClient.union(idSetA, idSetB);
            Assert.assertNotEquals(idSetA, idUnion);
            Assert.assertNotEquals(idSetB, idUnion);
            Assert.assertEquals(idElementsB, setService.getSetElements(idUnion));
            setService.deleteSet(idUnion);
        } finally {
            objectService.deleteMultipleObjects(idElementsB);
            setService.deleteSet(idSetA);
            setService.deleteSet(idSetB);
        }
    }

    @Test
    public void unionTest_setANonEmptySetBEmpty_shouldReturnSetA() {
        Set<Long> idElementsA = com.google.common.collect.Sets.newHashSet(objectService.createMultipleObjects(5));
        long idSetA = setService.createSetWithElements(idElementsA);
        long idSetB = setService.createEmptySet();
        try {
            long idUnion = setClient.union(idSetA, idSetB);
            Assert.assertNotEquals(idSetA, idUnion);
            Assert.assertNotEquals(idSetB, idUnion);
            Assert.assertEquals(idElementsA, setService.getSetElements(idUnion));
            setService.deleteSet(idUnion);
        } finally {
            objectService.deleteMultipleObjects(idElementsA);
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
        List<Long> idAllElements = objectService.createMultipleObjects(10);
        Set<Long> idElementsA = com.google.common.collect.Sets.newHashSet(idAllElements.subList(0, 6));
        Set<Long> idElementsB = com.google.common.collect.Sets.newHashSet(idAllElements.subList(3, 9));
        Set<Long> idElementsC = com.google.common.collect.Sets.newHashSet(idAllElements.subList(4, idAllElements.size()));
        long idSetA = setService.createSetWithElements(idElementsA);
        long idSetB = setService.createSetWithElements(idElementsB);
        long idSetC = setService.createSetWithElements(idElementsC);
        try {
            long idUnion = setClient.union(com.google.common.collect.Sets.newHashSet(idSetA, idSetB, idSetC));
            Assert.assertNotEquals(idSetA, idUnion);
            Assert.assertNotEquals(idSetB, idUnion);
            Assert.assertNotEquals(idSetC, idUnion);
            Assert.assertEquals(com.google.common.collect.Sets.newHashSet(idAllElements), setService.getSetElements(idUnion));
            setService.deleteSet(idUnion);
        } finally {
            objectService.deleteMultipleObjects(com.google.common.collect.Sets.newHashSet(idAllElements));
            setService.deleteSet(idSetA);
            setService.deleteSet(idSetB);
            setService.deleteSet(idSetC);
        }
    }

    @Test
    public void unionMultipleSetsTest_threeDisjointSets_shouldReturnUnion() {
        Set<Long> idElementsA = com.google.common.collect.Sets.newHashSet(objectService.createMultipleObjects(3));
        Set<Long> idElementsB = com.google.common.collect.Sets.newHashSet(objectService.createMultipleObjects(4));
        Set<Long> idElementsC = com.google.common.collect.Sets.newHashSet(objectService.createMultipleObjects(5));
        long idSetA = setService.createSetWithElements(idElementsA);
        long idSetB = setService.createSetWithElements(idElementsB);
        long idSetC = setService.createSetWithElements(idElementsC);
        try {
            long idUnion = setClient.union(com.google.common.collect.Sets.newHashSet(idSetA, idSetB, idSetC));
            Assert.assertNotEquals(idSetA, idUnion);
            Assert.assertNotEquals(idSetB, idUnion);
            Assert.assertNotEquals(idSetC, idUnion);
            Assert.assertEquals(
                com.google.common.collect.Sets.union(
                    idElementsA,
                    com.google.common.collect.Sets.union(idElementsB, idElementsC)
                ),
                setService.getSetElements(idUnion)
            );
            setService.deleteSet(idUnion);
        } finally {
            objectService.deleteMultipleObjects(idElementsA);
            objectService.deleteMultipleObjects(idElementsB);
            objectService.deleteMultipleObjects(idElementsC);
            setService.deleteSet(idSetA);
            setService.deleteSet(idSetB);
            setService.deleteSet(idSetC);
        }
    }

    @Test
    public void unionMultipleSetsTest_threeEqualSets_shouldReturnEqualSet() {
        Set<Long> idElements = com.google.common.collect.Sets.newHashSet(objectService.createMultipleObjects(5));
        long idSetA = setService.createSetWithElements(idElements);
        long idSetB = setService.createSetWithElements(idElements);
        long idSetC = setService.createSetWithElements(idElements);
        try {
            long idUnion = setClient.union(com.google.common.collect.Sets.newHashSet(idSetA, idSetB, idSetC));
            Assert.assertNotEquals(idSetA, idUnion);
            Assert.assertNotEquals(idSetB, idUnion);
            Assert.assertNotEquals(idSetC, idUnion);
            Assert.assertEquals(idElements, setService.getSetElements(idUnion));
            setService.deleteSet(idUnion);
        } finally {
            objectService.deleteMultipleObjects(idElements);
            setService.deleteSet(idSetA);
            setService.deleteSet(idSetB);
            setService.deleteSet(idSetC);
        }
    }

    @Test
    public void unionMultipleSetsTest_threeSetsThatAreTheSame_shouldReturnEqualSet() {
        Set<Long> idElements = com.google.common.collect.Sets.newHashSet(objectService.createMultipleObjects(5));
        long idSet = setService.createSetWithElements(idElements);
        try {
            long idUnion = setClient.union(com.google.common.collect.Sets.newHashSet(idSet, idSet, idSet));
            Assert.assertNotEquals(idSet, idUnion);
            Assert.assertEquals(idElements, setService.getSetElements(idUnion));
            setService.deleteSet(idUnion);
        } finally {
            objectService.deleteMultipleObjects(idElements);
            setService.deleteSet(idSet);
        }
    }

    @Test
    public void unionMultipleSetsTest_threeSetsWhereOneIsEmpty_shouldReturnUnion() {
        Set<Long> idElementsB = com.google.common.collect.Sets.newHashSet(objectService.createMultipleObjects(5));
        Set<Long> idElementsC = com.google.common.collect.Sets.newHashSet(objectService.createMultipleObjects(5));
        long idSetA = setService.createEmptySet();
        long idSetB = setService.createSetWithElements(idElementsB);
        long idSetC = setService.createSetWithElements(idElementsC);
        try {
            long idUnion = setClient.union(com.google.common.collect.Sets.newHashSet(idSetA, idSetB, idSetC));
            Assert.assertNotEquals(idSetA, idUnion);
            Assert.assertNotEquals(idSetB, idUnion);
            Assert.assertNotEquals(idSetC, idUnion);
            Assert.assertEquals(
                com.google.common.collect.Sets.union(idElementsB, idElementsC),
                setService.getSetElements(idUnion)
            );
            setService.deleteSet(idUnion);
        } finally {
            objectService.deleteMultipleObjects(idElementsB);
            objectService.deleteMultipleObjects(idElementsC);
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
            long idUnion = setClient.union(com.google.common.collect.Sets.newHashSet(idSetA, idSetB, idSetC));
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
            setClient.union(com.google.common.collect.Sets.newHashSet(idSetA, idSetB, idSetC));
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
        List<Long> idAllElements = objectService.createMultipleObjects(10);
        long idSetA = setService.createSetWithElements(com.google.common.collect.Sets.newHashSet(idAllElements.subList(0, 7)));
        long idSetB = setService.createSetWithElements(com.google.common.collect.Sets.newHashSet(idAllElements.subList(4, idAllElements.size())));
        try {
            long idDifference = setClient.difference(idSetA, idSetB);
            Assert.assertNotEquals(idDifference, idSetA);
            Assert.assertNotEquals(idDifference, idSetB);
            Assert.assertEquals(
                com.google.common.collect.Sets.newHashSet(idAllElements.subList(0, 4)),
                setService.getSetElements(idDifference)
            );
            setService.deleteSet(idDifference);
        } finally {
            setService.deleteSet(idSetA);
            setService.deleteSet(idSetB);
            objectService.deleteMultipleObjects(com.google.common.collect.Sets.newHashSet(idAllElements));
        }
    }

    @Test
    public void differenceTest_setAAndSetBDisjoint_shouldReturnSetA() {
        List<Long> idAllElements = objectService.createMultipleObjects(10);
        Set<Long> idElementsA = com.google.common.collect.Sets.newHashSet(idAllElements.subList(0, 5));
        long idSetA = setService.createSetWithElements(idElementsA);
        long idSetB = setService.createSetWithElements(com.google.common.collect.Sets.newHashSet(idAllElements.subList(5, idAllElements.size())));
        try {
            long idDifference = setClient.difference(idSetA, idSetB);
            Assert.assertNotEquals(idDifference, idSetA);
            Assert.assertNotEquals(idDifference, idSetB);
            Assert.assertEquals(idElementsA, setService.getSetElements(idDifference));
            setService.deleteSet(idDifference);
        } finally {
            setService.deleteSet(idSetA);
            setService.deleteSet(idSetB);
            objectService.deleteMultipleObjects(com.google.common.collect.Sets.newHashSet(idAllElements));
        }
    }

    @Test
    public void differenceTest_setASubsetOfSetB_shouldReturnEmptySet() {
        List<Long> idElementsB = objectService.createMultipleObjects(10);
        Set<Long> idElementsA = com.google.common.collect.Sets.newHashSet(idElementsB.subList(0, 5));
        long idSetA = setService.createSetWithElements(idElementsA);
        long idSetB = setService.createSetWithElements(com.google.common.collect.Sets.newHashSet(idElementsB));
        try {
            long idDifference = setClient.difference(idSetA, idSetB);
            Assert.assertNotEquals(idDifference, idSetA);
            Assert.assertNotEquals(idDifference, idSetB);
            Assert.assertTrue(setService.isEmpty(idDifference));
            setService.deleteSet(idDifference);
        } finally {
            setService.deleteSet(idSetA);
            setService.deleteSet(idSetB);
            objectService.deleteMultipleObjects(com.google.common.collect.Sets.newHashSet(idElementsB));
        }
    }

    @Test
    public void differenceTest_setBSubsetOfSetA_shouldReturnDifference() {
        List<Long> idElementsA = objectService.createMultipleObjects(10);
        Set<Long> idElementsB = com.google.common.collect.Sets.newHashSet(idElementsA.subList(0, 5));
        long idSetA = setService.createSetWithElements(com.google.common.collect.Sets.newHashSet(idElementsA));
        long idSetB = setService.createSetWithElements(idElementsB);
        try {
            long idDifference = setClient.difference(idSetA, idSetB);
            Assert.assertNotEquals(idDifference, idSetA);
            Assert.assertNotEquals(idDifference, idSetB);
            Assert.assertEquals(
                com.google.common.collect.Sets.difference(com.google.common.collect.Sets.newHashSet(idElementsA), idElementsB),
                setService.getSetElements(idDifference)
            );
            setService.deleteSet(idDifference);
        } finally {
            setService.deleteSet(idSetA);
            setService.deleteSet(idSetB);
            objectService.deleteMultipleObjects(com.google.common.collect.Sets.newHashSet(idElementsA));
        }
    }

    @Test
    public void differenceTest_setAEmptySetBNotEmpty_shouldReturnEmptySet() {
        long idSetA = setService.createEmptySet();
        Set<Long> idElementsB = com.google.common.collect.Sets.newHashSet(objectService.createMultipleObjects(5));
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
            objectService.deleteMultipleObjects(idElementsB);
        }
    }

    @Test
    public void differenceTest_setANotEmptySetBEmpty_shouldReturnSetA() {
        Set<Long> idElementsA = com.google.common.collect.Sets.newHashSet(objectService.createMultipleObjects(5));
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
            objectService.deleteMultipleObjects(idElementsA);
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
    public void complementTest_setSubsetOfUniverse_shouldReturnUniverseMinusSet() {
        List<Long> idElementsUniverse = objectService.createMultipleObjects(10);
        Set<Long> idElementsSet = com.google.common.collect.Sets.newHashSet(idElementsUniverse.subList(0, 5));
        long idUniverse = setService.createSetWithElements(com.google.common.collect.Sets.newHashSet(idElementsUniverse));
        long idSet = setService.createSetWithElements(idElementsSet);
        try {
            long idComplement = setClient.complement(idSet, idUniverse);
            Assert.assertNotEquals(idComplement, idUniverse);
            Assert.assertNotEquals(idComplement, idSet);
            Assert.assertEquals(
                com.google.common.collect.Sets.difference(com.google.common.collect.Sets.newHashSet(idElementsUniverse), idElementsSet),
                setService.getSetElements(idComplement)
            );
            setService.deleteSet(idComplement);
        } finally {
            setService.deleteSet(idUniverse);
            setService.deleteSet(idSet);
            objectService.deleteMultipleObjects(ImmutableSet.copyOf(idElementsUniverse));
        }
    }

    @Test
    public void complementTest_setEmptyUniverseNotEmpty_shouldReturnUniverse() {
        Set<Long> idElementsUniverse = com.google.common.collect.Sets.newHashSet(objectService.createMultipleObjects(5));
        long idUniverse = setService.createSetWithElements(idElementsUniverse);
        long idSet = setService.createEmptySet();
        try {
            long idComplement = setClient.complement(idSet, idUniverse);
            Assert.assertNotEquals(idComplement, idUniverse);
            Assert.assertNotEquals(idComplement, idSet);
            Assert.assertEquals(idElementsUniverse, setService.getSetElements(idComplement));
            setService.deleteSet(idComplement);
        } finally {
            setService.deleteSet(idUniverse);
            setService.deleteSet(idSet);
            objectService.deleteMultipleObjects(idElementsUniverse);
        }
    }

    @Test
    public void complementTest_setEqualsUniverse_shouldReturnEmptySet() {
        Set<Long> idElements = com.google.common.collect.Sets.newHashSet(objectService.createMultipleObjects(10));
        long idUniverse = setService.createSetWithElements(idElements);
        long idSet = setService.copySet(idUniverse);
        try {
            long idComplement = setClient.complement(idSet, idUniverse);
            Assert.assertTrue(setService.isEmpty(idComplement));
            setService.deleteSet(idComplement);
        } finally {
            setService.deleteSet(idUniverse);
            setService.deleteSet(idSet);
            objectService.deleteMultipleObjects(idElements);
        }
    }

    @Test
    public void complementTest_universeAndSetBothEmpty_shouldReturnEmptySet() {
        long idUniverse = setService.createEmptySet();
        long idSet = setService.createEmptySet();
        try {
            long idComplement = setClient.complement(idSet, idUniverse);
            Assert.assertNotEquals(idComplement, idUniverse);
            Assert.assertNotEquals(idComplement, idSet);
            Assert.assertTrue(setService.isEmpty(idComplement));
            setService.deleteSet(idComplement);
        } finally {
            setService.deleteSet(idUniverse);
            setService.deleteSet(idSet);
        }
    }

    @Test
    public void complementTest_setIsNotASubsetOfUniverse_shouldReturn400() {
        List<Long> idAllElements = objectService.createMultipleObjects(10);
        long idSet = setService.createSetWithElements(com.google.common.collect.Sets.newHashSet(idAllElements.subList(0, 7)));
        long idUniverse = setService.createSetWithElements(
            com.google.common.collect.Sets.newHashSet(idAllElements.subList(4, idAllElements.size()))
        );
        try {
            setClient.complement(idSet, idUniverse);
            Assert.fail();
        } catch(FeignException e) {
            Assert.assertEquals(400, e.status());
            Assert.assertTrue(
                "Response body [" + e.contentUTF8() + "] should contain the id of the set [" + idSet + "]",
                e.contentUTF8().contains(idSet + "")
            );
            Assert.assertTrue(
                "Response body [" + e.contentUTF8() + "] should contain the id of the universal set [" + idUniverse +
                    "]",
                e.contentUTF8().contains(idUniverse + "")
            );
        } finally {
            setService.deleteSet(idSet);
            setService.deleteSet(idUniverse);
            objectService.deleteMultipleObjects(ImmutableSet.copyOf(idAllElements));
        }
    }

    @Test
    public void complementTest_universeEmpty_shouldReturn400() {
        long idUniverse = setService.createEmptySet();
        Set<Long> idElementsSet = com.google.common.collect.Sets.newHashSet(objectService.createMultipleObjects(5));
        long idSet = setService.createSetWithElements(idElementsSet);
        try {
            setClient.complement(idSet, idUniverse);
            Assert.fail();
        } catch(FeignException e) {
            Assert.assertEquals(400, e.status());
            Assert.assertTrue(
                "Response body [" + e.contentUTF8() + "] should contain the id of the set [" + idSet + "]",
                e.contentUTF8().contains(idSet + "")
            );
            Assert.assertTrue(
                "Response body [" + e.contentUTF8() + "] should contain the id of the universal set [" + idUniverse +
                    "]",
                e.contentUTF8().contains(idUniverse + "")
            );
        } finally {
            setService.deleteSet(idSet);
            setService.deleteSet(idUniverse);
            objectService.deleteMultipleObjects(idElementsSet);
        }
    }

    @Test
    public void complementTest_universeDoesNotExist_shouldReturn404() {
        long idUniverse = setService.createSet();
        setService.deleteSet(idUniverse);
        long idSet = setService.createSet();
        try {
            setClient.complement(idSet, idUniverse);
            Assert.fail();
        } catch(FeignException e) {
            Assert.assertEquals(404, e.status());
            Assert.assertTrue(
                "Response body [" + e.contentUTF8() + "] should contain the id of the nonexistent Set [" + idUniverse +
                    "]",
                e.contentUTF8().contains(idUniverse + "")
            );
        } finally {
            setService.deleteSet(idSet);
        }
    }

    @Test
    public void complementTest_setDoesNotExist_shouldReturn404() {
        long idUniverse = setService.createSet();
        long idSet = setService.createSet();
        setService.deleteSet(idSet);
        try {
            setClient.complement(idSet, idUniverse);
            Assert.fail();
        } catch(FeignException e) {
            Assert.assertEquals(404, e.status());
            Assert.assertTrue(
                "Response body [" + e.contentUTF8() + "] should contain the id of the nonexistent Set [" + idSet + "]",
                e.contentUTF8().contains(idSet + "")
            );
        } finally {
            setService.deleteSet(idUniverse);
        }
    }

    @Test
    public void symmetricDifferenceTest_setAAndSetBIntersectButAreNotSubsets_shouldReturnSymmetricDifference() {
        List<Long> idAllElements = objectService.createMultipleObjects(10);
        Set<Long> idElementsA = com.google.common.collect.Sets.newHashSet(idAllElements.subList(0, 5));
        Set<Long> idElementsB = com.google.common.collect.Sets.newHashSet(idAllElements.subList(3, idAllElements.size()));
        long idSetA = setService.createSetWithElements(idElementsA);
        long idSetB = setService.createSetWithElements(idElementsB);
        try {
            long idSymmetricDifference = setClient.symmetricDifference(idSetA, idSetB);
            Assert.assertNotEquals(idSetA, idSymmetricDifference);
            Assert.assertNotEquals(idSetB, idSymmetricDifference);
            Assert.assertEquals(
                com.google.common.collect.Sets.symmetricDifference(idElementsA, idElementsB),
                setService.getSetElements(idSymmetricDifference)
            );
            setService.deleteSet(idSymmetricDifference);
        } finally {
            setService.deleteSet(idSetA);
            setService.deleteSet(idSetB);
            objectService.deleteMultipleObjects(com.google.common.collect.Sets.newHashSet(idAllElements));
        }
    }

    @Test
    public void symmetricDifferenceTest_setAIsSubsetOfSetB_shouldReturnSymmetricDifference() {
        List<Long> idElementsBList = objectService.createMultipleObjects(10);
        Set<Long> idElementsA = com.google.common.collect.Sets.newHashSet(idElementsBList.subList(0, 5));
        Set<Long> idElementsB = com.google.common.collect.Sets.newHashSet(idElementsBList);
        long idSetA = setService.createSetWithElements(idElementsA);
        long idSetB = setService.createSetWithElements(idElementsB);
        try {
            long idSymmetricDifference = setClient.symmetricDifference(idSetA, idSetB);
            Assert.assertNotEquals(idSetA, idSymmetricDifference);
            Assert.assertNotEquals(idSetB, idSymmetricDifference);
            Assert.assertEquals(
                com.google.common.collect.Sets.symmetricDifference(idElementsA, idElementsB),
                setService.getSetElements(idSymmetricDifference)
            );
            setService.deleteSet(idSymmetricDifference);
        } finally {
            setService.deleteSet(idSetA);
            setService.deleteSet(idSetB);
            objectService.deleteMultipleObjects(com.google.common.collect.Sets.newHashSet(idElementsB));
        }
    }

    @Test
    public void symmetricDifferenceTest_setBIsSubsetOfSetA_shouldReturnSymmetricDifference() {
        List<Long> idElementsAList = objectService.createMultipleObjects(10);
        Set<Long> idElementsA = com.google.common.collect.Sets.newHashSet(idElementsAList);
        Set<Long> idElementsB = com.google.common.collect.Sets.newHashSet(idElementsAList.subList(0, 5));
        long idSetA = setService.createSetWithElements(idElementsA);
        long idSetB = setService.createSetWithElements(idElementsB);
        try {
            long idSymmetricDifference = setClient.symmetricDifference(idSetA, idSetB);
            Assert.assertNotEquals(idSetA, idSymmetricDifference);
            Assert.assertNotEquals(idSetB, idSymmetricDifference);
            Assert.assertEquals(
                com.google.common.collect.Sets.symmetricDifference(idElementsA, idElementsB),
                setService.getSetElements(idSymmetricDifference)
            );
            setService.deleteSet(idSymmetricDifference);
        } finally {
            setService.deleteSet(idSetA);
            setService.deleteSet(idSetB);
            objectService.deleteMultipleObjects(com.google.common.collect.Sets.newHashSet(idElementsA));
        }
    }

    @Test
    public void symmetricDifferenceTest_setsAreDisjoint_shouldReturnUnion() {
        Set<Long> idElementsA = com.google.common.collect.Sets.newHashSet(objectService.createMultipleObjects(5));
        Set<Long> idElementsB = com.google.common.collect.Sets.newHashSet(objectService.createMultipleObjects(5));
        long idSetA = setService.createSetWithElements(idElementsA);
        long idSetB = setService.createSetWithElements(idElementsB);
        try {
            long idSymmetricDifference = setClient.symmetricDifference(idSetA, idSetB);
            Assert.assertNotEquals(idSetA, idSymmetricDifference);
            Assert.assertNotEquals(idSetB, idSymmetricDifference);
            Assert.assertEquals(
                com.google.common.collect.Sets.union(idElementsA, idElementsB),
                setService.getSetElements(idSymmetricDifference)
            );
            setService.deleteSet(idSymmetricDifference);
        } finally {
            setService.deleteSet(idSetA);
            setService.deleteSet(idSetB);
            objectService.deleteMultipleObjects(com.google.common.collect.Sets.union(idElementsA, idElementsB));
        }
    }

    @Test
    public void symmetricDifferenceTest_setAIsEmpty_shouldReturnSetB() {
        long idSetA = setService.createEmptySet();
        Set<Long> idElementsB = com.google.common.collect.Sets.newHashSet(objectService.createMultipleObjects(5));
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
            objectService.deleteMultipleObjects(idElementsB);
        }
    }

    @Test
    public void symmetricDifferenceTest_setBIsEmpty_shouldReturnSetA() {
        Set<Long> idElementsA = com.google.common.collect.Sets.newHashSet(objectService.createMultipleObjects(5));
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
            objectService.deleteMultipleObjects(idElementsA);
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

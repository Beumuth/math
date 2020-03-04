package com.beumuth.math.core.settheory.orderedpair;

import com.beumuth.math.client.Clients;
import com.beumuth.math.client.settheory.orderedpair.CrupdateOrderedPairRequest;
import com.beumuth.math.client.settheory.orderedpair.OrderedPair;
import com.beumuth.math.client.settheory.orderedpair.OrderedPairClient;
import com.beumuth.math.core.external.feign.FeignAssertions;
import com.beumuth.math.core.internal.client.ClientConfigurations;
import com.beumuth.math.core.settheory.object.MockObjectService;
import com.beumuth.math.core.settheory.object.ObjectService;
import feign.FeignException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;

@RunWith(SpringRunner.class)
@SpringBootTest
public class OrderedPairTests {

    @Autowired
    private OrderedPairService orderedPairService;
    @Autowired
    private MockOrderedPairService mockOrderedPairService;
    @Autowired
    private ObjectService objectService;
    @Autowired
    private MockObjectService mockObjectService;

    private OrderedPairClient orderedPairClient;

    @Before
    public void setupTests() {
        orderedPairClient = Clients.getClient(OrderedPairClient.class, ClientConfigurations.LOCAL);
    }

    @Test
    public void existsTest_doesExist_shouldReturnTrue() {
        OrderedPair validMock = mockOrderedPairService.valid();
        try {
            Assert.assertTrue(orderedPairClient.exists(validMock.getId()));
        } finally {
            mockOrderedPairService.deleteMock(validMock);
        }
    }

    @Test
    public void existsTest_doesNotExist_shouldReturnFalse() {
        OrderedPair nonexistentMock = mockOrderedPairService.nonexistent();
        Assert.assertFalse(orderedPairClient.exists(nonexistentMock.getId()));
    }

    @Test
    public void getTest_doesExist_shouldBeReturned() {
        OrderedPair validMock = mockOrderedPairService.valid();
        try {
            OrderedPairAssertions.assertEquivalent(
                validMock,
                orderedPairClient.get(validMock.getId())
            );
        } finally {
            mockOrderedPairService.deleteMock(validMock);
        }
    }

    @Test
    public void getTest_doesNotExist_shouldReturn404() {
        OrderedPair nonexistentMock = mockOrderedPairService.nonexistent();
        try {
            orderedPairClient.get(nonexistentMock.getId());
            Assert.fail();
        } catch(FeignException e) {
            FeignAssertions.assertExceptionLike(e, 404, nonexistentMock.getId() + "");
        }
    }

    @Test
    public void createTest_shouldBeCreated() {
        long idLeft = mockObjectService.valid();
        long idRight = mockObjectService.valid();
        try {
            Optional<OrderedPair> orderedPair = orderedPairService.getOrderedPair(
                orderedPairClient.create(new CrupdateOrderedPairRequest(idLeft, idRight))
            );
            Assert.assertTrue(orderedPair.isPresent());
            Assert.assertEquals(idLeft, orderedPair.get().getIdLeft());
            Assert.assertEquals(idRight, orderedPair.get().getIdRight());
            mockOrderedPairService.deleteMock(orderedPair.get());
        } finally {
            mockObjectService.deleteMock(idLeft);
            mockObjectService.deleteMock(idRight);
        }
    }

    @Test
    public void createTest_objectsExistAndSame_shouldBeCreated() {
        long idLeftAndRight = mockObjectService.valid();
        try {
            Optional<OrderedPair> orderedPair = orderedPairService.getOrderedPair(
                orderedPairClient.create(new CrupdateOrderedPairRequest(idLeftAndRight, idLeftAndRight))
            );
            Assert.assertTrue(orderedPair.isPresent());
            Assert.assertEquals(idLeftAndRight, orderedPair.get().getIdLeft());
            Assert.assertEquals(idLeftAndRight, orderedPair.get().getIdRight());
            mockOrderedPairService.deleteMock(orderedPair.get());
        } finally {
            mockObjectService.deleteMock(idLeftAndRight);
        }
    }

    @Test
    public void createTest_leftObjectDoesNotExist_shouldReturn400() {
        long idLeft = mockObjectService.nonexistent();
        long idRight = mockObjectService.valid();
        try {
            orderedPairClient.create(new CrupdateOrderedPairRequest(idLeft, idRight));
            Assert.fail();
        } catch(FeignException e) {
            FeignAssertions.assertExceptionLike(e, 400, idLeft + "");
        } finally {
            mockObjectService.deleteMock(idRight);
        }
    }

    @Test
    public void createTest_rightObjectDoesNotExist_shouldReturn400() {
        long idLeft = mockObjectService.valid();
        long idRight = mockObjectService.nonexistent();
        try {
            orderedPairClient.create(new CrupdateOrderedPairRequest(idLeft, idRight));
            Assert.fail();
        } catch(FeignException e) {
            FeignAssertions.assertExceptionLike(e, 400, idRight + "");
        } finally {
            mockObjectService.deleteMock(idLeft);
        }
    }

    @Test
    public void updateTest_valid_shouldUpdate() {
        OrderedPair mockOrderedPair = mockOrderedPairService.valid();
        long idNewLeft = mockObjectService.valid();
        long idNewRight = mockObjectService.valid();
        try {
            orderedPairClient.update(mockOrderedPair.getId(), new CrupdateOrderedPairRequest(idNewLeft, idNewRight));
            Optional<OrderedPair> newOrderedPair = orderedPairService.getOrderedPair(mockOrderedPair.getId());
            Assert.assertTrue(newOrderedPair.isPresent());
            Assert.assertEquals(mockOrderedPair.getId(), newOrderedPair.get().getId());
            Assert.assertEquals(mockOrderedPair.getIdObject(), newOrderedPair.get().getIdObject());
            Assert.assertEquals(idNewLeft, newOrderedPair.get().getIdLeft());
            Assert.assertEquals(idNewRight, newOrderedPair.get().getIdRight());
            mockOrderedPairService.deleteMock(newOrderedPair.get());
        } finally {
            mockObjectService.deleteMock(idNewLeft);
            mockObjectService.deleteMock(idNewRight);
            mockOrderedPairService.deleteMock(mockOrderedPair);
        }
    }

    @Test
    public void updateTest_orderedPairDoesNotExist_shouldReturn404() {
        OrderedPair nonexistent = mockOrderedPairService.nonexistent();
        long idNewLeft = mockObjectService.valid();
        long idNewRight = mockObjectService.valid();
        try {
            orderedPairClient.update(
                nonexistent.getId(),
                new CrupdateOrderedPairRequest(idNewLeft, idNewRight)
            );
            Assert.fail();
        } catch(FeignException e) {
            FeignAssertions.assertExceptionLike(e, 404, nonexistent.getId() + "");
        } finally {
            mockObjectService.deleteMock(idNewLeft);
            mockObjectService.deleteMock(idNewRight);
        }
    }

    @Test
    public void updateTest_leftObjectDoesNotExist_shouldReturn400() {
        OrderedPair mockOrderedPair = mockOrderedPairService.valid();
        long idNewLeft = mockObjectService.nonexistent();
        long idNewRight = mockObjectService.valid();
        try {
            orderedPairClient.update(
                mockOrderedPair.getId(),
                new CrupdateOrderedPairRequest(idNewLeft, idNewRight)
            );
            Assert.fail();
        } catch(FeignException e) {
            FeignAssertions.assertExceptionLike(e, 400, idNewLeft + "");
        } finally {
            mockObjectService.deleteMock(idNewRight);
            mockOrderedPairService.deleteMock(mockOrderedPair);
        }
    }

    @Test
    public void updateTest_rightObjectDoesNotExist_shouldReturn400() {
        OrderedPair mockOrderedPair = mockOrderedPairService.valid();
        long idNewLeft = mockObjectService.valid();
        long idNewRight = mockObjectService.nonexistent();
        try {
            orderedPairClient.update(
                mockOrderedPair.getId(),
                new CrupdateOrderedPairRequest(idNewLeft, idNewRight)
            );
            Assert.fail();
        } catch(FeignException e) {
            FeignAssertions.assertExceptionLike(e, 400, idNewRight + "");
        } finally {
            mockObjectService.deleteMock(idNewLeft);
            mockOrderedPairService.deleteMock(mockOrderedPair);
        }
    }

    @Test
    public void deleteTest_shouldBeDeleted() {
        OrderedPair mockOrderedPair = mockOrderedPairService.valid();
        try {
            orderedPairClient.delete(mockOrderedPair.getId());
            Assert.assertFalse(orderedPairService.doesOrderedPairExist(mockOrderedPair.getId()));

            //The left and right objects should still exist though
            Assert.assertTrue(objectService.doesObjectExist(mockOrderedPair.getIdLeft()));
            Assert.assertTrue(objectService.doesObjectExist(mockOrderedPair.getIdRight()));
        } finally {
            mockOrderedPairService.deleteMock(mockOrderedPair);
        }
    }

    @Test
    public void swapTest_shouldBeSwapped() {
        OrderedPair mockOrderedPair = mockOrderedPairService.valid();
        try {
            orderedPairClient.swap(mockOrderedPair.getId());
            Optional<OrderedPair> updatedOrderedPairOptional =
                orderedPairService.getOrderedPair(mockOrderedPair.getId());
            Assert.assertTrue(updatedOrderedPairOptional.isPresent());
            OrderedPair updatedOrderedPair = updatedOrderedPairOptional.get();
            Assert.assertEquals(mockOrderedPair.getId(), updatedOrderedPair.getId());
            Assert.assertEquals(mockOrderedPair.getIdObject(), updatedOrderedPair.getIdObject());
            Assert.assertEquals(mockOrderedPair.getIdRight(), updatedOrderedPair.getIdLeft());
            Assert.assertEquals(mockOrderedPair.getIdLeft(), updatedOrderedPair.getIdRight());
        } finally {
            mockOrderedPairService.deleteMock(mockOrderedPair);
        }
    }

    @Test
    public void swapTest_orderedPairDoesNotExist_shouldReturn404() {
        OrderedPair mockOrderedPair = mockOrderedPairService.nonexistent();
        try {
            orderedPairClient.swap(mockOrderedPair.getId());
            Assert.fail();
        } catch(FeignException e) {
            FeignAssertions.assertExceptionLike(e, 404, mockOrderedPair.getId() + "");
        }
    }
}

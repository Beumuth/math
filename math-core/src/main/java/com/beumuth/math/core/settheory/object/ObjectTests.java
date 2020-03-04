package com.beumuth.math.core.settheory.object;

import com.beumuth.math.client.Clients;
import com.beumuth.math.client.settheory.object.ObjectClient;
import com.beumuth.math.core.internal.client.ClientConfigurations;
import com.google.common.collect.Sets;
import feign.FeignException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Set;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ObjectTests {

    @Autowired
    private ObjectService objectService;

    private ObjectClient objectClient;

    @Before
    public void setupTests() {
        objectClient = Clients.getClient(ObjectClient.class, ClientConfigurations.LOCAL);
    }

    @Test
    public void doesObjectExistTest() {
        //Create an object
        long id = objectService.createObject();

        //It should exist
        Assert.assertTrue(objectClient.exists(id));

        //Delete the object
        objectService.deleteObject(id);

        //It should not exist
        Assert.assertFalse(objectClient.exists(id));
    }

    @Test
    public void getObjectTest() {
        //Create an object
        long id = objectService.createObject();

        //It should be gettable
        try {
            objectClient.get(id);
        } catch(FeignException e) {
            Assert.fail(); //Oh no
        }

        //Delete the object
        objectService.deleteObject(id);

        //It should return a 404
        try {
            objectClient.get(id);
            Assert.fail(); //Oh no
        } catch(FeignException e) {
            Assert.assertEquals(404, e.status());
        }
    }

    @Test
    public void createObjectTest() {
        //Create an object, the clean it up
        objectService.deleteObject(
            objectClient.create()
        );
    }

    @Test
    public void createMultipleObjectsTest() {
        //Create multiple objects
        List<Long> idObjects = objectClient.createMultiple(10);

        Assert.assertEquals(10, idObjects.size());

        //Clean up
        objectService.deleteMultipleObjects(Sets.newHashSet(idObjects));
    }

    @Test
    public void deleteObjectTest() {
        //Create an object
        long idObject = objectService.createObject();

        //Delete it
        objectClient.delete(idObject);

        //Ensure that it doesnt exist
        Assert.assertFalse(objectService.doesObjectExist(idObject));
    }

    @Test
    public void deleteMultipleObjectsTest() {
        //Create multiple objects
        Set<Long> idObjects = Sets.newHashSet(objectService.createMultipleObjects(10));

        //Delete them
        objectClient.deleteAll(idObjects);

        //Ensure that they don't exist
        for(Long idObject : idObjects) {
            Assert.assertFalse(objectService.doesObjectExist(idObject));
        }
    }
}

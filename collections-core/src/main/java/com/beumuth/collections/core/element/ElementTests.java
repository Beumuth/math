package com.beumuth.collections.core.element;

import com.beumuth.collections.client.element.ElementClient;
import com.beumuth.collections.core.client.ClientService;
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
public class ElementTests {
    @Autowired
    private ClientService clientService;

    @Autowired
    private ElementService elementService;

    private ElementClient elementClient;

    @Before
    public void setupTests() {
        elementClient = clientService.getClient(ElementClient.class);
    }

    @Test
    public void doesElementExistTest() {
        //Create an element
        long id = elementService.createElement();

        //It should exist
        Assert.assertTrue(elementClient.doesElementExist(id));

        //Delete the element
        elementService.deleteElement(id);

        //It should not exist
        Assert.assertFalse(elementClient.doesElementExist(id));
    }

    @Test
    public void getElementTest() {
        //Create an element
        long id = elementService.createElement();

        //It should be gettable
        try {
            elementClient.getElement(id);
        } catch(FeignException e) {
            Assert.fail(); //Oh no
        }

        //Delete the element
        elementService.deleteElement(id);

        //It should return a 404
        try {
            elementClient.getElement(id);
            Assert.fail(); //Oh no
        } catch(FeignException e) {
            Assert.assertEquals(404, e.status());
        }
    }

    @Test
    public void createElementTest() {
        //Create an element, the clean it up
        elementService.deleteElement(
            elementClient.createElement()
        );
    }

    @Test
    public void createMultipleElementsTest() {
        //Create multiple elements
        List<Long> idElements = elementClient.createMultipleElements(10);

        Assert.assertEquals(10, idElements.size());

        //Clean up
        elementService.deleteMultipleElements(Sets.newHashSet(idElements));
    }

    @Test
    public void deleteElementTest() {
        //Create an element
        long idElement = elementService.createElement();

        //Delete it
        elementClient.deleteElement(idElement);

        //Ensure that it doesnt exist
        Assert.assertFalse(elementService.doesElementExist(idElement));
    }

    @Test
    public void deleteMultipleElementsTest() {
        //Create multiple elements
        Set<Long> idElements = Sets.newHashSet(elementService.createMultipleElements(10));

        //Delete them
        elementClient.deleteMultipleElements(idElements);

        //Ensure that they don't exist
        for(Long idElement : idElements) {
            Assert.assertFalse(elementService.doesElementExist(idElement));
        }
    }
}

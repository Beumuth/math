package com.beumuth.collections.core.element;

import com.beumuth.collections.client.element.Element;
import com.github.instantpudd.validator.ClientErrorException;
import com.github.instantpudd.validator.ClientErrorStatusCode;
import com.github.instantpudd.validator.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Controller
@RequestMapping(path="/api/elements")
public class ElementController {

    @Autowired
    private ElementService elementService;

    @GetMapping(path="/element/{id}/exists")
    @ResponseBody
    public Boolean doesElementExist(@PathVariable long id) {
        return elementService.doesElementExist(id);
    }

    @GetMapping(path="/element/{id}")
    @ResponseBody
    public Element getElement(@PathVariable long id) throws ClientErrorException {
        Optional<Element> element = elementService.getElement(id);
        Validator
            .returnStatus(ClientErrorStatusCode.BAD_REQUEST)
            .ifEmpty(element)
            .withErrorMessage("No element exists with given id [" + id + "]")
            .execute();
        return element.get();
    }

    @PostMapping(path="/element")
    @ResponseBody
    public Long createElement() {
        return elementService.createElement();
    }

    @PostMapping
    @ResponseBody
    public List<Long> createMultipleElements(@RequestBody int numToCreate) {
        return elementService.createMultipleElements(numToCreate);
    }

    @DeleteMapping(path="/element/{id}")
    public void deleteElement(@PathVariable long id) {
        elementService.deleteElement(id);
    }

    @DeleteMapping
    public void deleteElements(@RequestBody Set<Long> ids) {
        elementService.deleteMultipleElements(ids);
    }
}
package com.beumuth.math.core.settheory.object;

import com.beumuth.math.client.settheory.object.Object;
import com.github.instantpudd.validator.ClientErrorException;
import com.github.instantpudd.validator.ClientErrorStatusCode;
import com.github.instantpudd.validator.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Controller
@RequestMapping(path="/api/objects")
public class ObjectController {

    @Autowired
    private ObjectService objectService;

    @GetMapping(path="/object/{id}/exists")
    @ResponseBody
    public Boolean doesObjectExist(@PathVariable long id) {
        return objectService.doesObjectExist(id);
    }

    @GetMapping(path="/object/{id}")
    @ResponseBody
    public Object getObject(@PathVariable long id) throws ClientErrorException {
        Optional<Object> object = objectService.getObject(id);
        Validator
            .returnStatus(ClientErrorStatusCode.NOT_FOUND)
            .ifEmpty(object)
            .withErrorMessage("No object exists with given id [" + id + "]")
            .execute();
        return object.get();
    }

    @PostMapping(path="/object")
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public Long createObject() {
        return objectService.createObject();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public List<Long> createMultipleObjects(@RequestBody int numToCreate) {
        return objectService.createMultipleObjects(numToCreate);
    }

    @DeleteMapping(path="/object/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteObject(@PathVariable long id) {
        objectService.deleteObject(id);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteObjects(@RequestBody Set<Long> ids) {
        objectService.deleteMultipleObjects(ids);
    }
}
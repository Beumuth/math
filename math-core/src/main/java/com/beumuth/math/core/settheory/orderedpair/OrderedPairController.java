package com.beumuth.math.core.settheory.orderedpair;

import com.beumuth.math.client.settheory.orderedpair.CrupdateOrderedPairRequest;
import com.beumuth.math.client.settheory.orderedpair.OrderedPair;
import com.beumuth.math.core.settheory.object.ObjectValidationService;
import com.github.instantpudd.validator.ClientErrorException;
import com.github.instantpudd.validator.ClientErrorStatusCode;
import com.github.instantpudd.validator.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequestMapping(path="/api/orderedPairs")
public class OrderedPairController {

    @Autowired
    private OrderedPairService orderedPairService;

    @Autowired
    private OrderedPairValidationService orderedPairValidationService;
    @Autowired
    private ObjectValidationService objectValidationService;

    @GetMapping(path="/orderedPair/{id}/exists")
    @ResponseBody
    public Boolean doesOrderedPairExist(@PathVariable long id) {
        return orderedPairService.doesOrderedPairExist(id);
    }

    @GetMapping(path="/orderedPair/{id}")
    @ResponseBody
    public OrderedPair getOrderedPair(@PathVariable long id) throws ClientErrorException {
        Optional<OrderedPair> orderedPair = orderedPairService.getOrderedPair(id);
        Validator
            .returnStatus(ClientErrorStatusCode.NOT_FOUND)
            .ifEmpty(orderedPair)
            .withErrorMessage("No OrderedPair exists with given id [" + id + "]")
            .execute();
        return orderedPair.get();
    }

    @PostMapping(path="/orderedPair")
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public Long createOrderedPair(@RequestBody CrupdateOrderedPairRequest request) throws ClientErrorException {
        objectValidationService.validateObjectExists(
            request.getIdLeft(),
            ClientErrorStatusCode.BAD_REQUEST,
            "Object with given idLeft [" + request.getIdLeft() + "] does not exist"
        );
        objectValidationService.validateObjectExists(
            request.getIdRight(),
            ClientErrorStatusCode.BAD_REQUEST,
            "Object with given idRight [" + request.getIdRight() + "] does not exist"
        );
        return orderedPairService.createOrderedPair(request);
    }

    @PutMapping(path="/orderedPair/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ResponseBody
    public void updateOrderedPair(@PathVariable long id, @RequestBody CrupdateOrderedPairRequest request)
        throws ClientErrorException {
        orderedPairValidationService.validateOrderedPairExists(
            id,
            ClientErrorStatusCode.NOT_FOUND,
            "OrderedPair with given id [" + id + "] does not exist"
        );
        objectValidationService.validateObjectExists(
            request.getIdLeft(),
            ClientErrorStatusCode.BAD_REQUEST,
            "Object with given idLeft [" + request.getIdLeft() + "] does not exist"
        );
        objectValidationService.validateObjectExists(
            request.getIdRight(),
            ClientErrorStatusCode.BAD_REQUEST,
            "Object with given idRight [" + request.getIdRight() + "] does not exist"
        );
        orderedPairService.updateOrderedPair(id, request);
    }

    @DeleteMapping(path="/orderedPair/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOrderedPair(@PathVariable long id) {
        orderedPairService.deleteOrderedPair(id);
    }

    @PutMapping(path="/orderedPair/{id}/swap")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void swap(@PathVariable long id) throws ClientErrorException {
        orderedPairValidationService.validateOrderedPairExists(
            id,
            ClientErrorStatusCode.NOT_FOUND,
            "OrderedPair with given id [" + id + "] does not exist"
        );
        orderedPairService.swap(id);
    }
}

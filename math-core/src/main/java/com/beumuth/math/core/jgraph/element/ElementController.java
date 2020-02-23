package com.beumuth.math.core.jgraph.element;

import com.beumuth.math.client.jgraph.CreateElementRequest;
import com.beumuth.math.client.jgraph.Element;
import com.beumuth.math.client.jgraph.UpdateElementRequest;
import com.beumuth.math.client.settheory.orderedset.OrderedSet;
import com.beumuth.math.client.settheory.orderedset.OrderedSets;
import com.github.instantpudd.validator.ClientErrorException;
import com.github.instantpudd.validator.Validator;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.github.instantpudd.validator.ClientErrorStatusCode.*;

@Controller
@RequestMapping("/api/jgraph")
public class ElementController {

    @Autowired
    @Qualifier(value="JGraphElementService")
    private ElementService elementService;

    @RequestMapping(method=RequestMethod.GET, value="/elements/element/{id}/exists")
    @ResponseBody
    public boolean doesElementExist(@PathVariable("id") long id) {
        return elementService.doesElementExist(id);
    }

    @RequestMapping(method=RequestMethod.GET, value="/elements/exist/any")
    @ResponseBody
    public boolean doAnyElementsExist(@RequestParam(value="ids") Set<Long> ids) throws ClientErrorException {
        Validator
            .returnStatus(BAD_REQUEST)
            .ifTrue(ids.isEmpty())
            .withErrorMessage("ids cannot be empty")
            .execute();
        return elementService.doAnyElementsExist(ids);
    }

    @RequestMapping(method=RequestMethod.GET, value="/elements/exist/all")
    @ResponseBody
    public boolean doAllElementsExist(@RequestParam(value="ids") Set<Long> ids) throws ClientErrorException {
        Validator
            .returnStatus(BAD_REQUEST)
            .ifTrue(ids.isEmpty())
            .withErrorMessage("ids cannot be empty")
            .execute();
        return elementService.doAllElementsExist(ids);
    }

    @RequestMapping(method=RequestMethod.GET, value="/elements/element/with/{a}/or/{b}/exists")
    @ResponseBody
    public boolean doesElementExistWithAOrB(@PathVariable("a") long a, @PathVariable("b") long b) {
        return elementService.doesElementWithAOrBExist(a, b);
    }

    @RequestMapping(method=RequestMethod.GET, value="/elements/element/with/{a}/and/{b}/exists")
    @ResponseBody
    public boolean doesElementExistWithAAndB(@PathVariable("a") long a, @PathVariable("b") long b) {
        return elementService.doesElementExistWithAAndB(a, b);
    }

    @RequestMapping(method=RequestMethod.GET, path="/elements/element/{id}/isNode")
    @ResponseBody
    public boolean isElementNode(@PathVariable("id") long id) throws ClientErrorException {
        try {
            return elementService.isElementNode(id);
        } catch(ElementDoesNotExistException e) {
            throw new ClientErrorException(NOT_FOUND, e.getMessage());
        }
    }

    @RequestMapping(method=RequestMethod.GET, path="/elements/areNodes")
    @ResponseBody
    public List<Boolean> areElementsNodes(@RequestParam(value="ids", required=false) OrderedSet<Long> ids) {
        return ids == null || ids.isEmpty() ? OrderedSets.empty() : elementService.areElementsNodes(ids);
    }

    @RequestMapping(method=RequestMethod.GET, path="/elements/element/{id}/isPendantFrom/{idFrom}")
    @ResponseBody
    public boolean isElementPendantFrom(@PathVariable("id") long id, @PathVariable("idFrom") long idFrom)
        throws ClientErrorException {
        try {
            return elementService.isElementPendantFrom(id, idFrom);
        } catch(ElementDoesNotExistException e) {
            throw new ClientErrorException(NOT_FOUND, e.getMessage());
        }
    }

    @RequestMapping(method=RequestMethod.GET, path="/elements/arePendantsFrom/{idFrom}")
    @ResponseBody
    public List<Boolean> areElementsPendantsFrom(
        @RequestParam(value="ids") OrderedSet<Long> ids,
        @PathVariable("idFrom") long idFrom
    ) throws ClientErrorException {
        Validator
            .returnStatus(NOT_FOUND)
            .ifFalse(elementService.doesElementExist(idFrom))
            .withErrorMessage("Element with given id [" + idFrom + "] does not exist")
            .execute();
        return ids.isEmpty() ? OrderedSets.empty() : elementService.areElementsPendantsFrom(ids, idFrom);
    }

    @RequestMapping(method=RequestMethod.GET, path="/elements/element/{id}/isPendantTo/{idTo}")
    @ResponseBody
    public boolean isElementPendantTo(@PathVariable("id") long id, @PathVariable("idTo") long idTo)
        throws ClientErrorException {
        try {
            return elementService.isElementPendantTo(id, idTo);
        } catch(ElementDoesNotExistException e) {
            throw new ClientErrorException(NOT_FOUND, e.getMessage());
        }
    }

    @RequestMapping(method=RequestMethod.GET, path="/elements/arePendantsTo/{idTo}")
    @ResponseBody
    public List<Boolean> areElementsPendantsTo(
        @RequestParam(value="ids", required=false) OrderedSet<Long> ids,
        @PathVariable("idTo") long idTo
    ) throws ClientErrorException {
        Validator
            .returnStatus(NOT_FOUND)
            .ifFalse(elementService.doesElementExist(idTo))
            .withErrorMessage("Element with given id [" + idTo + "] does not exist")
            .execute();
        return ids == null || ids.isEmpty() ? OrderedSets.empty() : elementService.areElementsPendantsTo(ids, idTo);
    }

    @RequestMapping(method=RequestMethod.GET, path="/elements/element/{id}/isLoopOn/{idOn}")
    @ResponseBody
    public boolean isElementLoopOn(@PathVariable("id") long id, @PathVariable("idOn") long idOn)
        throws ClientErrorException {
        try {
            return elementService.isElementLoopOn(id, idOn);
        } catch(ElementDoesNotExistException e) {
            throw new ClientErrorException(NOT_FOUND, e.getMessage());
        }
    }

    @RequestMapping(method=RequestMethod.GET, path="/elements/areLoopsOn/{idOn}")
    @ResponseBody
    public List<Boolean> areElementsLoopsOn(
        @RequestParam(value="ids", required=false) OrderedSet<Long> ids,
        @PathVariable("idOn") long idOn
    ) throws ClientErrorException {
        Validator
            .returnStatus(NOT_FOUND)
            .ifFalse(elementService.doesElementExist(idOn))
            .withErrorMessage("Element with given id [" + idOn + "] does not exist")
            .execute();
        return ids == null || ids.isEmpty() ? OrderedSets.empty() : elementService.areElementsLoopsOn(ids, idOn);
    }

    @RequestMapping(method=RequestMethod.GET, path="/elements/element/{id}/isEndpoint")
    @ResponseBody
    public boolean isElementEndpoint(@PathVariable("id") long id) {
        return elementService.isElementEndpoint(id);
    }

    @RequestMapping(method=RequestMethod.GET, path="/elements/areEndpoints")
    @ResponseBody
    public List<Boolean> areElementsEndpoints(@RequestParam(value="ids", required=false) OrderedSet<Long> ids) {
        return ids == null || ids.isEmpty() ? OrderedSets.empty() : elementService.areElementsEndpoints(ids);
    }


    @RequestMapping(method=RequestMethod.GET, path="/elements/connected/{x}/{y}")
    @ResponseBody
    public boolean areElementsConnected(
        @PathVariable("x") long x,
        @PathVariable("y") long y
    ) throws ClientErrorException {
        Validator
            .returnStatus(NOT_FOUND)
            .ifFalse(doesElementExist(x))
            .withErrorMessage("Element with given id [" + x + "] does not exist")
            .execute();
        Validator
            .returnStatus(NOT_FOUND)
            .ifFalse(doesElementExist(y))
            .withErrorMessage("Element with given id [" + y + "] does not exist")
            .execute();
        return elementService.areElementsConnected(x, y);
    }

    @RequestMapping(method=RequestMethod.GET, value="/elements/with/{a}/or/{b}/count")
    @ResponseBody
    public int getNumElementsWithAOrB(@PathVariable("a") long a, @PathVariable("b") long b) {
        return elementService.numElementsWithAOrB(a, b);
    }

    @RequestMapping(method=RequestMethod.GET, value="/elements/with/{a}/and/{b}/count")
    @ResponseBody
    public int numElementsWithAAndB(@PathVariable("a") long a, @PathVariable("b") long b) {
        return elementService.numElementsWithAAndB(a, b);
    }

    @RequestMapping(method=RequestMethod.GET, value="/elements/nodes/count")
    @ResponseBody
    public int numNodes() {
        return elementService.numNodes();
    }

    @RequestMapping(method=RequestMethod.GET, value="/elements/pendants/from/{idFrom}/count")
    @ResponseBody
    public int numPendantsFrom(@PathVariable("idFrom") long idFrom) throws ClientErrorException {
        Validator
            .returnStatus(NOT_FOUND)
            .ifFalse(elementService.doesElementExist(idFrom))
            .withErrorMessage("Element with given id  [" + idFrom + "] does not exist")
            .execute();
        return elementService.numPendantsFrom(idFrom);
    }

    @RequestMapping(method=RequestMethod.GET, value="/elements/pendants/to/{idTo}/count")
    @ResponseBody
    public int numPendantsTo(@PathVariable("idTo") long idTo) throws ClientErrorException {
        Validator
            .returnStatus(NOT_FOUND)
            .ifFalse(elementService.doesElementExist(idTo))
            .withErrorMessage("Element with given id  [" + idTo + "] does not exist")
            .execute();
        return elementService.numPendantsTo(idTo);
    }

    @RequestMapping(method=RequestMethod.GET, value="/elements/loops/on/{idOn}/count")
    @ResponseBody
    public int numLoopsOn(@PathVariable("idOn") long idOn) throws ClientErrorException {
        Validator
            .returnStatus(NOT_FOUND)
            .ifFalse(elementService.doesElementExist(idOn))
            .withErrorMessage("Element with given id  [" + idOn + "] does not exist")
            .execute();
        return elementService.numLoopsOn(idOn);
    }

    @RequestMapping(method=RequestMethod.GET, value="/elements/element/{id}")
    @ResponseBody
    public Element getElement(@PathVariable("id") long id) throws ClientErrorException {
        try {
            return elementService.getElement(id);
        } catch(ElementDoesNotExistException e) {
            throw new ClientErrorException(NOT_FOUND, e.getMessage());
        }
    }

    @RequestMapping(method=RequestMethod.GET, value="/elements/ids")
    @ResponseBody
    public List<Long> getIds(@RequestParam(value="ids", required=false) OrderedSet<Long> ids) {
        return ids == null || ids.isEmpty() ? Collections.emptyList() : elementService.getIds(ids);
    }

    @RequestMapping(method=RequestMethod.GET, value="/elements/ids/all")
    @ResponseBody
    public OrderedSet<Long> getAllIds() {
        return elementService.getAllIds();
    }

    @RequestMapping(method=RequestMethod.GET, value="/elements/ids/with/{a}/or/{b}")
    @ResponseBody
    public OrderedSet<Long> getIdsWithAOrB(@PathVariable("a") long a, @PathVariable("b") long b) {
        return elementService.getIdsWithAOrB(a, b);
    }

    @RequestMapping(method=RequestMethod.GET, value="/elements/ids/with/{a}/and/{b}")
    @ResponseBody
    public OrderedSet<Long> getIdsWithAAndB(@PathVariable("a") long a, @PathVariable("b") long b) {
        return elementService.getIdsWithAAndB(a, b);
    }

    @RequestMapping(method=RequestMethod.GET, value="/elements/nodes/ids")
    @ResponseBody
    public OrderedSet<Long> getIdsNodes() {
        return elementService.getIdsNodes();
    }

    @RequestMapping(method=RequestMethod.GET, value="/elements/element/{idFrom}/pendants/from/ids")
    @ResponseBody
    public OrderedSet<Long> getIdsPendantsFrom(@PathVariable("idFrom") long idFrom) throws ClientErrorException {
        Validator
            .returnStatus(NOT_FOUND)
            .ifFalse(elementService.doesElementExist(idFrom))
            .withErrorMessage("Element with given id [" + idFrom + "] does not exist")
            .execute();
        return elementService.getIdsPendantsFrom(idFrom);
    }

    @RequestMapping(method=RequestMethod.GET, value="/elements/element/{idTo}/pendants/to/ids")
    @ResponseBody
    public OrderedSet<Long> getIdsPendantsTo(@PathVariable("idTo") long idTo)
        throws ClientErrorException {
        Validator
            .returnStatus(NOT_FOUND)
            .ifFalse(elementService.doesElementExist(idTo))
            .withErrorMessage("Element with given id [" + idTo + "] does not exist")
            .execute();
        return elementService.getIdsPendantsTo(idTo);
    }

    @RequestMapping(method=RequestMethod.GET, value="/elements/element/{idOn}/loops/on/ids")
    @ResponseBody
    public OrderedSet<Long> getIdsLoopsOn(@PathVariable("idOn") long idOn)
        throws ClientErrorException {
        Validator
            .returnStatus(NOT_FOUND)
            .ifFalse(elementService.doesElementExist(idOn))
            .withErrorMessage("Element with given id [" + idOn + "] does not exist")
            .execute();
        return elementService.getIdsLoopsOn(idOn);
    }

    @RequestMapping(method=RequestMethod.GET, value="/elements/element/{id}/endpoints/of/ids")
    @ResponseBody
    public OrderedSet<Long> getIdsEndpointsOf(@PathVariable("id") long id) throws ClientErrorException {
        Validator
            .returnStatus(NOT_FOUND)
            .ifFalse(elementService.doesElementExist(id))
            .withErrorMessage("Element with given id [" + id + "] does not exist")
            .execute();
        return elementService.getIdsEndpointsOf(id);
    }

    @RequestMapping(method=RequestMethod.GET, value="/elements/endpoints/of/ids")
    @ResponseBody
    public List<OrderedSet<Long>> getIdsEndpointsOfForEach(@RequestParam(value="ids") OrderedSet<Long> ids) {
        return ids.isEmpty() ? Collections.emptyList() : elementService.getIdsEndpointsOfForEach(ids);
    }

    @RequestMapping(method=RequestMethod.GET, value="/elements")
    @ResponseBody
    public List<Element> getElements(@RequestParam(value="ids", required=false) OrderedSet<Long> ids) {
        return ids == null || ids.isEmpty() ? elementService.getAllElements() : elementService.getElements(ids);
    }

    @RequestMapping(method=RequestMethod.GET, value="/elements/with/{a}/or/{b}")
    @ResponseBody
    public OrderedSet<Element> getElementsWithAOrB(@PathVariable("a") long a, @PathVariable("b") long b) {
        return elementService.getElementsWithAOrB(a, b);
    }

    @RequestMapping(method=RequestMethod.GET, value="/elements/with/{a}/and/{b}")
    @ResponseBody
    public OrderedSet<Element> getElementsWithAAndB(@PathVariable("a") long a, @PathVariable("b") long b) {
        return elementService.getElementsWithAAndB(a, b);
    }

    @RequestMapping(method=RequestMethod.GET, value="/elements/nodes")
    @ResponseBody
    public OrderedSet<Element> getNodes() {
        return elementService.getNodes();
    }

    @RequestMapping(method=RequestMethod.GET, value="/elements/element/{idFrom}/pendants/from")
    @ResponseBody
    public OrderedSet<Element> getPendantsFrom(@PathVariable("idFrom") long idFrom) throws ClientErrorException {
        Validator
            .returnStatus(NOT_FOUND)
            .ifFalse(elementService.doesElementExist(idFrom))
            .withErrorMessage("Element with given id [" + idFrom + "] does not exist")
            .execute();
        return elementService.getPendantsFrom(idFrom);
    }

    @RequestMapping(method=RequestMethod.GET, value="/elements/element/{idTo}/pendants/to")
    @ResponseBody
    public OrderedSet<Element> getPendantsTo(@PathVariable("idTo") long idTo)
        throws ClientErrorException {
        Validator
            .returnStatus(NOT_FOUND)
            .ifFalse(elementService.doesElementExist(idTo))
            .withErrorMessage("Element with given id [" + idTo + "] does not exist")
            .execute();
        return elementService.getPendantsTo(idTo);
    }

    @RequestMapping(method=RequestMethod.GET, value="/elements/element/{idOn}/loops/on")
    @ResponseBody
    public OrderedSet<Element> getLoopsOn(@PathVariable("idOn") long idOn)
        throws ClientErrorException {
        Validator
            .returnStatus(NOT_FOUND)
            .ifFalse(elementService.doesElementExist(idOn))
            .withErrorMessage("Element with given id [" + idOn + "] does not exist")
            .execute();
        return elementService.getLoopsOn(idOn);
    }

    @RequestMapping(method=RequestMethod.GET, value="/elements/element/{id}/endpoints/of")
    @ResponseBody
    public OrderedSet<Element> getEndpointsOf(@PathVariable("id") long id) throws ClientErrorException {
        Validator
            .returnStatus(NOT_FOUND)
            .ifFalse(elementService.doesElementExist(id))
            .withErrorMessage("Element with given id [" + id + "] does not exist")
            .execute();
        return elementService.getEndpointsOf(id);
    }

    @RequestMapping(method=RequestMethod.GET, value="/elements/endpoints/of")
    @ResponseBody
    public List<OrderedSet<Element>> getEndpointsOfForEach(@RequestParam(value="ids") OrderedSet<Long> ids) {
        return ids.isEmpty() ? Collections.emptyList() : elementService.getEndpointsOfForEach(ids);
    }

    @RequestMapping(method=RequestMethod.POST, value="/elements/element")
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    public long createElement(CreateElementRequest request) throws ClientErrorException {
        validateCreateElementRequest(request, 1);
        return elementService.createElement(request.getA(), request.getB());
    }

    @RequestMapping(method=RequestMethod.POST, value="/elements")
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    public OrderedSet<Long> createElements(@RequestBody List<CreateElementRequest> requests) throws ClientErrorException {
        for(CreateElementRequest request : requests) {
           validateCreateElementRequest(request, requests.size());
        }
        return requests.isEmpty() ? OrderedSets.empty() : elementService.createElements(requests);
    }

    @RequestMapping(method=RequestMethod.POST, value="/elements/nodes/node")
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    public long createNode() {
        return elementService.createNode();
    }

    @RequestMapping(method=RequestMethod.POST, value="/elements/nodes")
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    public OrderedSet<Long> createNodes(@RequestBody int number) throws ClientErrorException {
        Validator
            .returnStatus(BAD_REQUEST)
            .ifTrue(number < 0)
            .withErrorMessage("Given request body [" + number + "] must be >= 0")
            .execute();
        return number == 0 ? OrderedSets.empty() : elementService.createNodes(number);
    }

    @RequestMapping(method=RequestMethod.POST, value="/elements/element/{idFrom}/pendant/from")
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    public long createPendantFrom(@PathVariable("idFrom") long idFrom) throws ClientErrorException {
        Validator
            .returnStatus(NOT_FOUND)
            .ifFalse(elementService.doesElementExist(idFrom))
            .withErrorMessage("Element with given from [" + idFrom + "] does not exist.")
            .execute();
        return elementService.createPendantFrom(idFrom);
    }

    @RequestMapping(method=RequestMethod.POST, value="/elements/element/{idFrom}/pendants/from")
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    public OrderedSet<Long> createPendantsFrom(@PathVariable("idFrom") long idFrom, @RequestBody int howMany)
        throws ClientErrorException {
        Validator
            .returnStatus(NOT_FOUND)
            .ifFalse(elementService.doesElementExist(idFrom))
            .withErrorMessage("Element with given from [" + idFrom + "] does not exist.")
            .execute();
        Validator
            .returnStatus(BAD_REQUEST)
            .ifFalse(howMany > 0)
            .withErrorMessage("The request body indicating how many to create must be > 0")
            .execute();
        return elementService.createPendantsFrom(idFrom, howMany);
    }

    @RequestMapping(method=RequestMethod.POST, value="/elements/element/{idTo}/pendant/to")
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    public long createPendantTo(@PathVariable("idTo") long idTo) throws ClientErrorException {
        Validator
            .returnStatus(NOT_FOUND)
            .ifFalse(elementService.doesElementExist(idTo))
            .withErrorMessage("Element with given to [" + idTo + "] does not exist.")
            .execute();
        return elementService.createPendantTo(idTo);
    }

    @RequestMapping(method=RequestMethod.POST, value="/elements/element/{idTo}/pendants/to")
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    public OrderedSet<Long> createPendantsTo(@PathVariable("idTo") long idTo, @RequestBody int howMany)
        throws ClientErrorException {
        Validator
            .returnStatus(NOT_FOUND)
            .ifFalse(elementService.doesElementExist(idTo))
            .withErrorMessage("Element with given from [" + idTo + "] does not exist.")
            .execute();
        Validator
            .returnStatus(BAD_REQUEST)
            .ifFalse(howMany > 0)
            .withErrorMessage("The request body indicating how many to create must be > 0")
            .execute();
        return elementService.createPendantsTo(idTo, howMany);
    }

    @RequestMapping(method=RequestMethod.POST, value="/elements/element/{idOn}/loop")
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    public long createLoopOn(@PathVariable("idOn") long idOn) throws ClientErrorException {
        Validator
            .returnStatus(NOT_FOUND)
            .ifFalse(elementService.doesElementExist(idOn))
            .withErrorMessage("Element with given on [" + idOn + "] does not exist.")
            .execute();
        return elementService.createLoopOn(idOn);
    }

    @RequestMapping(method=RequestMethod.POST, value="/elements/element/{idOn}/loops")
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    public OrderedSet<Long> createLoopsOn(@PathVariable("idOn") long idOn, @RequestBody int howMany)
        throws ClientErrorException {
        Validator
            .returnStatus(NOT_FOUND)
            .ifFalse(elementService.doesElementExist(idOn))
            .withErrorMessage("Element with given on [" + idOn + "] does not exist.")
            .execute();
        Validator
            .returnStatus(BAD_REQUEST)
            .ifFalse(howMany > 0)
            .withErrorMessage("The request body indicating how many to create must be > 0")
            .execute();
        return elementService.createLoopsOn(idOn, howMany);
    }

    @RequestMapping(method=RequestMethod.PUT, value="/elements/element/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateElement(@PathVariable long id, @RequestBody UpdateElementRequest request)
        throws ClientErrorException {
        Validator
            .returnStatus(NOT_FOUND)
            .ifFalse(elementService.doesElementExist(id))
            .withErrorMessage("Element with given id [" + id + "] does not exist")
            .execute();
        Validator
            .returnStatus(BAD_REQUEST)
            .ifFalse(elementService.doesElementExist(request.getA()))
            .withErrorMessage("Element with given a [" + request.getA() + "] does not exist")
            .execute();
        Validator
            .returnStatus(BAD_REQUEST)
            .ifFalse(elementService.doesElementExist(request.getB()))
            .withErrorMessage("Element with given b [" + request.getB() + "] does not exist")
            .execute();
        elementService.updateElement(id, request);
    }

    @RequestMapping(method=RequestMethod.PUT, value="/elements")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateElements(
        @RequestParam(value="ids", required=false) OrderedSet<Long> ids,
        @RequestBody List<UpdateElementRequest> requests
    ) throws ClientErrorException {
        //No ids given?
        if(ids == null || ids.isEmpty()) {
            //Nothing to update.
            return;
        }

        //Ensure that the ids are the same size as the requests
        Validator
            .returnStatus(BAD_REQUEST)
            .ifFalse(ids.size() == requests.size())
            .withErrorMessage(
                "The number of ids must match the number of UpdateElementRequests in the body. " +
                ids.size() + " ids and " + requests.size() + " requests were given."
            ).execute();

        //Ensure that all of the ids in the request exist
        OrderedSet<Long> idsThatDoNotExist = elementService.getIdsThatDoNotExist(ids);
        Validator
            .returnStatus(NOT_FOUND)
            .ifFalse(idsThatDoNotExist.isEmpty())
            .withErrorMessage("Elements with the following ids do not exist: " + idsThatDoNotExist)
            .execute();

        //Ensure that Elements with the given a's and b's exist
        OrderedSet<Long> idAsThatDoNotExist = elementService.getIdsThatDoNotExist(
            requests //Read as a's
                .stream()
                .mapToLong(UpdateElementRequest::getA)
                .boxed()
                .collect(Collectors.toCollection(OrderedSet::new))
        );
        OrderedSet<Long> idBsThatDoNotExist = elementService.getIdsThatDoNotExist(
            requests //Read as a's
                .stream()
                .mapToLong(UpdateElementRequest::getB)
                .boxed()
                .collect(Collectors.toCollection(OrderedSet::new))
        );
        if(! (idAsThatDoNotExist.isEmpty() && idBsThatDoNotExist.isEmpty())) {
            Validator
                .returnStatus(BAD_REQUEST)
                .always()
                .withErrorMessage(
                    "Elements with the given a elements do not exist " + idAsThatDoNotExist.toString() + ". " +
                    "Elements with the given b elements do not exist " + idBsThatDoNotExist.toString() + "."
                ).execute();
        }

        elementService.updateElements(ids, requests);
    }

    @RequestMapping(method=RequestMethod.DELETE, path="/elements/element/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteElement(@PathVariable long id) throws ClientErrorException {
        //Get the full element
        try {
            elementService.getElement(id);
        } catch(ElementDoesNotExistException e) {
            //Does not exist, nothing to do
            return;
        }
        //Ensure that no element is connected to or from the element
        OrderedSet<Element> endpoints = elementService.getEndpointsOf(id);
        Validator
            .returnStatus(CONFLICT)
            .ifFalse(endpoints.isEmpty())
            .withErrorMessage(
                "The Element with id [" + id + "] has the following elements connected to it and cannot be deleted: " +
                    endpoints
            ).execute();
        elementService.deleteElement(id);
    }

    @RequestMapping(method=RequestMethod.DELETE, path="/elements")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteElements(@RequestParam(value="ids", required=false) Set<Long> ids) throws ClientErrorException {
        if(ids == null || ids.isEmpty()) {
            return;
        }

        //Get the ids of elements whose endpoint list is not a subset of the given ids
        OrderedSet<Long> orderedIds = OrderedSets.with(ids);
        List<OrderedSet<Long>> idsEndpointsOfForEach = elementService.getIdsEndpointsOfForEach(orderedIds);
        Map<Long, Set> invalidIds = Maps.newHashMap();
        IntStream
            .range(0, ids.size())
            .forEach(i -> {
                Set<Long> endpointsNotBeingDeleted = Sets.difference(idsEndpointsOfForEach.get(i), ids).immutableCopy();
                if (! endpointsNotBeingDeleted.isEmpty()) {
                    invalidIds.put(orderedIds.get(i), endpointsNotBeingDeleted);
                }
            });
        Validator
            .returnStatus(CONFLICT)
            .ifFalse(invalidIds.isEmpty())
            .withErrorMessage(
                "There were elements that could not be deleted due to containing endpoints not being deleted." +
                " Here is a map from the invalid ids to its endpoints that are not being deleted: " + invalidIds
            ).execute();

        elementService.deleteElements(ids);
    }

    private void validateCreateElementRequest(CreateElementRequest request, int numRequests)
        throws ClientErrorException {
        Validator
            .returnStatus(BAD_REQUEST)
            .ifTrue(request.getA() < 0 && -1 * request.getA() >= numRequests)
            .withErrorMessage(
                "Was given a [" + request.getA() + "], which indicates that a is to reference the id of the " +
                    request.getA() + "th element of this request; but the request only contains " + numRequests +
                    " elements."
            ).execute();
        Validator
            .returnStatus(BAD_REQUEST)
            .ifTrue(request.getB() < 0 && -1 * request.getB() >= numRequests)
            .withErrorMessage(
                "Was given b [" + request.getB() + "], which indicates that a is to reference the id of the " +
                    request.getB() + "th element of this request; but the request only contains " + numRequests +
                    " elements."
            ).execute();
        Validator
            .returnStatus(BAD_REQUEST)
            .ifFalse(request.getA() <= 0 || elementService.doesElementExist(request.getA()))
            .withErrorMessage("Given a [" + request.getA() + "] does not exist")
            .execute();
        Validator
            .returnStatus(BAD_REQUEST)
            .ifFalse(request.getB() <= 0 || elementService.doesElementExist(request.getB()))
            .withErrorMessage("Given b [" + request.getB() + "] does not exist")
            .execute();
    }
}
package com.beumuth.math.core.jgraph.element;

import com.beumuth.math.client.category.Categories;
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
    public boolean doAnyElementsExist(
        @RequestParam(value="ids", required=false) Set<Long> ids
    ) throws ClientErrorException {
        Validator
            .returnStatus(BAD_REQUEST)
            .ifTrue(ids == null || ids.isEmpty())
            .withErrorMessage("ids cannot be empty")
            .execute();
        return elementService.doAnyElementsExist(ids);
    }

    @RequestMapping(method=RequestMethod.GET, value="/elements/exist/all")
    @ResponseBody
    public boolean doAllElementsExist(
        @RequestParam(value="ids", required=false) Set<Long> ids
    ) throws ClientErrorException {
        Validator
            .returnStatus(BAD_REQUEST)
            .ifTrue(ids == null || ids.isEmpty())
            .withErrorMessage("ids cannot be empty")
            .execute();
        return elementService.doAllElementsExist(ids);
    }

    @RequestMapping(method=RequestMethod.GET, value="/elements/element/with/a/{a}/exists")
    @ResponseBody
    public boolean doesElementExistWithA(@PathVariable("a") long a) {
        return elementService.doesElementExistWithA(a);
    }

    @RequestMapping(method=RequestMethod.GET, value="/elements/element/with/b/{b}/exists")
    @ResponseBody
    public boolean doesElementExistWithB(@PathVariable("b") long b) {
        return elementService.doesElementExistWithB(b);
    }

    @RequestMapping(method=RequestMethod.GET, value="/elements/element/with/a/{a}/or/b/{b}/exists")
    @ResponseBody
    public boolean doesElementExistWithAOrB(@PathVariable("a") long a, @PathVariable("b") long b) {
        return elementService.doesElementWithAOrBExist(a, b);
    }

    @RequestMapping(method=RequestMethod.GET, value="/elements/element/with/a/{a}/and/b/{b}/exists")
    @ResponseBody
    public boolean doesElementExistWithAAndB(@PathVariable("a") long a, @PathVariable("b") long b) {
        return elementService.doesElementExistWithAAndB(a, b);
    }

    @RequestMapping(method=RequestMethod.GET, value="/elements/element/{id}/has/a/{a}")
    @ResponseBody
    public boolean doesElementHaveA(
        @PathVariable("id") long id,
        @PathVariable("a") long a
    ) throws ClientErrorException {
        Validator
            .returnStatus(NOT_FOUND)
            .ifFalse(elementService.doesElementExist(id))
            .withErrorMessage("Element with given id [" + id + "] does not exist")
            .execute();
        return elementService.doesElementHaveA(id, a);
    }

    @RequestMapping(method=RequestMethod.GET, value="/elements/have/a/{a}")
    @ResponseBody
    public List<Boolean> doElementsHaveA(
        @PathVariable("a") long a,
        @RequestParam(value="ids", required=false) OrderedSet<Long> ids
    ) {
        return ids == null || ids.isEmpty() ?
            Collections.emptyList() :
            elementService.doElementsHaveA(ids, a);
    }

    @RequestMapping(method=RequestMethod.GET, value="/elements/element/{id}/has/b/{b}")
    @ResponseBody
    public boolean doesElementHaveB(
        @PathVariable("id") long id,
        @PathVariable("b") long b
    ) throws ClientErrorException {
        Validator
            .returnStatus(NOT_FOUND)
            .ifFalse(elementService.doesElementExist(id))
            .withErrorMessage("Element with given id [" + id + "] does not exist")
            .execute();
        return elementService.doesElementHaveB(id, b);
    }

    @RequestMapping(method=RequestMethod.GET, value="/elements/have/b/{b}")
    @ResponseBody
    public List<Boolean> doElementsHaveB(
        @PathVariable("b") long b,
        @RequestParam(value="ids", required=false) OrderedSet<Long> ids
    ) {
        return ids == null || ids.isEmpty() ?
            Collections.emptyList() :
            elementService.doElementsHaveB(ids, b);
    }

    @RequestMapping(method=RequestMethod.GET, path="/elements/element/{id}/isNode")
    @ResponseBody
    public boolean isElementNode(@PathVariable("id") long id) throws ClientErrorException {
        Validator
            .returnStatus(NOT_FOUND)
            .ifFalse(elementService.doesElementExist(id))
            .withErrorMessage("Element with given id [" + id + "] does not exist")
            .execute();
        return elementService.isElementNode(id);
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
        Validator
            .returnStatus(NOT_FOUND)
            .ifFalse(elementService.doAllElementsExist(Sets.newHashSet(id, idFrom)))
            .withErrorMessage("One of the given ids [" + id + ", " + idFrom + " ] does not exist as an Element.")
            .execute();
        return elementService.isElementPendantFrom(id, idFrom);
    }

    @RequestMapping(method=RequestMethod.GET, path="/elements/arePendantsFrom/{idFrom}")
    @ResponseBody
    public List<Boolean> areElementsPendantsFrom(
        @RequestParam(value="ids", required=false) OrderedSet<Long> ids,
        @PathVariable("idFrom") long idFrom
    ) throws ClientErrorException {
        Validator
            .returnStatus(NOT_FOUND)
            .ifFalse(elementService.doesElementExist(idFrom))
            .withErrorMessage("Element with given id [" + idFrom + "] does not exist")
            .execute();
        return ids == null || ids.isEmpty() ?
            OrderedSets.empty() :
            elementService.areElementsPendantsFrom(ids, idFrom);
    }

    @RequestMapping(method=RequestMethod.GET, path="/elements/element/{id}/isPendantTo/{idTo}")
    @ResponseBody
    public boolean isElementPendantTo(@PathVariable("id") long id, @PathVariable("idTo") long idTo)
        throws ClientErrorException {
        Validator
            .returnStatus(NOT_FOUND)
            .ifFalse(elementService.doAllElementsExist(Sets.newHashSet(id, idTo)))
            .withErrorMessage("One of the given ids [" + id + ", " + idTo + " ] does not exist as an Element.")
            .execute();
        return elementService.isElementPendantTo(id, idTo);
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
        return ids == null || ids.isEmpty() ?
            OrderedSets.empty() :
            elementService.areElementsPendantsTo(ids, idTo);
    }

    @RequestMapping(method=RequestMethod.GET, path="/elements/element/{id}/isLoopOn/{idOn}")
    @ResponseBody
    public boolean isElementLoopOn(@PathVariable("id") long id, @PathVariable("idOn") long idOn)
        throws ClientErrorException {
        Validator
            .returnStatus(NOT_FOUND)
            .ifFalse(elementService.doAllElementsExist(Sets.newHashSet(id, idOn)))
            .withErrorMessage("One of the given ids [" + id + ", " + idOn + " ] does not exist as an Element.")
            .execute();
        return elementService.isElementLoopOn(id, idOn);
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
        return ids == null || ids.isEmpty() ?
            OrderedSets.empty() :
            elementService.areElementsLoopsOn(ids, idOn);
    }

    @RequestMapping(method=RequestMethod.GET, path="/elements/element/{id}/isEndpoint")
    @ResponseBody
    public boolean isElementEndpoint(@PathVariable("id") long id) {
        return elementService.isElementEndpoint(id);
    }

    @RequestMapping(method=RequestMethod.GET, path="/elements/areEndpoints")
    @ResponseBody
    public List<Boolean> areElementsEndpoints(@RequestParam(value="ids", required=false) OrderedSet<Long> ids) {
        return ids == null || ids.isEmpty() ?
            OrderedSets.empty() :
            elementService.areElementsEndpoints(ids);
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

    @RequestMapping(method=RequestMethod.GET, value="/elements/with/a/{a}/count")
    @ResponseBody
    public int numElementsWithA(@PathVariable("a") long a) {
        return elementService.numElementsWithA(a);
    }

    @RequestMapping(method=RequestMethod.GET, value="/elements/with/b/{b}/count")
    @ResponseBody
    public int numElementsWithB(@PathVariable("b") long b) {
        return elementService.numElementsWithB(b);
    }

    @RequestMapping(method=RequestMethod.GET, value="/elements/with/a/{a}/or/b/{b}/count")
    @ResponseBody
    public int getNumElementsWithAOrB(@PathVariable("a") long a, @PathVariable("b") long b) {
        return elementService.numElementsWithAOrB(a, b);
    }

    @RequestMapping(method=RequestMethod.GET, value="/elements/with/a/{a}/and/b/{b}/count")
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
        Validator
            .returnStatus(NOT_FOUND)
            .ifFalse(elementService.doesElementExist(id))
            .withErrorMessage("Element with given idTo [" + id + "] does not exist")
            .execute();
        return elementService.getElement(id);
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

    @RequestMapping(method=RequestMethod.GET, value="/elements/ids/with/a/{a}")
    @ResponseBody
    public OrderedSet<Long> getIdsWithA(@PathVariable("a") long a) throws ClientErrorException {
        Validator
            .returnStatus(NOT_FOUND)
            .ifFalse(elementService.doesElementExist(a))
            .withErrorMessage("Element with given a [ " + a + "] does not exist")
            .execute();
        return elementService.getIdsWithA(a);
    }

    @RequestMapping(method=RequestMethod.GET, value="/elements/ids/with/b/{b}")
    @ResponseBody
    public OrderedSet<Long> getIdsWithB(@PathVariable("b") long b) throws ClientErrorException {
        Validator
            .returnStatus(NOT_FOUND)
            .ifFalse(elementService.doesElementExist(b))
            .withErrorMessage("Element with given a [ " + b + "] does not exist")
            .execute();
        return elementService.getIdsWithB(b);
    }

    @RequestMapping(method=RequestMethod.GET, value="/elements/ids/with/a/{a}/or/b/{b}")
    @ResponseBody
    public OrderedSet<Long> getIdsWithAOrB(@PathVariable("a") long a, @PathVariable("b") long b) {
        return elementService.getIdsWithAOrB(a, b);
    }

    @RequestMapping(method=RequestMethod.GET, value="/elements/ids/with/a/{a}/and/b/{b}")
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
    public List<OrderedSet<Long>> getIdsEndpointsOfForEach(
        @RequestParam(value="ids", required=false) OrderedSet<Long> ids
    ) {
        return ids == null || ids.isEmpty() ?
            Collections.emptyList() :
            elementService.getIdsEndpointsOfForEach(ids);
    }

    @RequestMapping(method=RequestMethod.GET, value="/elements")
    @ResponseBody
    public List<Element> getElements(@RequestParam(value="ids", required=false) OrderedSet<Long> ids) {
        return ids == null || ids.isEmpty() ? elementService.getAllElements() : elementService.getElements(ids);
    }

    @RequestMapping(method=RequestMethod.GET, value="/elements/with/a/{a}")
    @ResponseBody
    public OrderedSet<Element> getElementsWithA(@PathVariable("a") long a) throws ClientErrorException {
        Validator
            .returnStatus(NOT_FOUND)
            .ifFalse(elementService.doesElementExist(a))
            .withErrorMessage("Element with given a [" + a + "] does not exist")
            .execute();
        return elementService.getElementsWithA(a);
    }

    @RequestMapping(method=RequestMethod.GET, value="/elements/with/b/{b}")
    @ResponseBody
    public OrderedSet<Element> getElementsWithB(@PathVariable("b") long b) throws ClientErrorException {
        Validator
            .returnStatus(NOT_FOUND)
            .ifFalse(elementService.doesElementExist(b))
            .withErrorMessage("Element with given b [" + b + "] does not exist")
            .execute();
        return elementService.getElementsWithB(b);
    }

    @RequestMapping(method=RequestMethod.GET, value="/elements/with/a/{a}/or/b/{b}")
    @ResponseBody
    public OrderedSet<Element> getElementsWithAOrB(@PathVariable("a") long a, @PathVariable("b") long b) {
        return elementService.getElementsWithAOrB(a, b);
    }

    @RequestMapping(method=RequestMethod.GET, value="/elements/with/a/{a}/and/b/{b}")
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
    public List<OrderedSet<Element>> getEndpointsOfForEach(
        @RequestParam(value="ids", required=false) OrderedSet<Long> ids
    ) {
        return ids == null || ids.isEmpty() ?
            Collections.emptyList() :
            elementService.getEndpointsOfForEach(ids);
    }

    @RequestMapping(method=RequestMethod.POST, value="/elements/element")
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    public long createElement(@RequestBody CreateElementRequest request) throws ClientErrorException {
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
            .withErrorMessage("Element with given b [" + request.getA() + "] does not exist")
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

        //Ensure that Elements with the given b's and b's exist
        OrderedSet<Long> idAsThatDoNotExist = elementService.getIdsThatDoNotExist(
            requests //Read as b's
                .stream()
                .mapToLong(UpdateElementRequest::getA)
                .boxed()
                .collect(Collectors.toCollection(OrderedSet::new))
        );
        OrderedSet<Long> idBsThatDoNotExist = elementService.getIdsThatDoNotExist(
            requests //Read as b's
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
                    "Elements with the given b elements do not exist " + idAsThatDoNotExist.toString() + ". " +
                    "Elements with the given b elements do not exist " + idBsThatDoNotExist.toString() + "."
                ).execute();
        }

        elementService.updateElements(ids, requests);
    }

    @RequestMapping(method=RequestMethod.DELETE, path="/elements/element/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteElement(@PathVariable long id) throws ClientErrorException {
        //Does the element already not exist?
        if(! elementService.doesElementExist(id)) {
            //Yes. Nothing to do.
            return;
        }

        //Ensure it is not a standard category
        Validator
            .returnStatus(BAD_REQUEST)
            .ifTrue(Categories.ALL_STANDARD.contains(id))
            .withErrorMessage(
                "The given id [" + id + "] is a standard Category. Standard Categories cannot be deleted."
            ).execute();

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

        //Ensure none of the Elements are standard categories
        Set<Long> idsStandardCatgoriesBeingDeleted = Sets.intersection(ids, Categories.ALL_STANDARD);
        Validator
            .returnStatus(BAD_REQUEST)
            .ifFalse(idsStandardCatgoriesBeingDeleted.isEmpty())
            .withErrorMessage(
                "The following standard categories are included for deletion: [" +
                    idsStandardCatgoriesBeingDeleted + "]. Standard Categories cannot be deleted"
            ).execute();

        //Get the ids of elements whose endpoint list is not b subset of the given ids
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
                " Here is b map from the invalid ids to its endpoints that are not being deleted: " + invalidIds
            ).execute();

        elementService.deleteElements(ids);
    }

    private void validateCreateElementRequest(CreateElementRequest request, int numRequests)
        throws ClientErrorException {
        Validator
            .returnStatus(BAD_REQUEST)
            .ifTrue(request.getA() < 0 && -1 * request.getA() >= numRequests)
            .withErrorMessage(
                "Was given b [" + request.getA() + "], which indicates that b is to reference the id of the " +
                    request.getA() + "th element of this request; but the request only contains " + numRequests +
                    " elements."
            ).execute();
        Validator
            .returnStatus(BAD_REQUEST)
            .ifTrue(request.getB() < 0 && -1 * request.getB() >= numRequests)
            .withErrorMessage(
                "Was given b [" + request.getB() + "], which indicates that b is to reference the id of the " +
                    request.getB() + "th element of this request; but the request only contains " + numRequests +
                    " elements."
            ).execute();
        Validator
            .returnStatus(BAD_REQUEST)
            .ifFalse(request.getA() <= 0 || elementService.doesElementExist(request.getA()))
            .withErrorMessage("Given b [" + request.getA() + "] does not exist")
            .execute();
        Validator
            .returnStatus(BAD_REQUEST)
            .ifFalse(request.getB() <= 0 || elementService.doesElementExist(request.getB()))
            .withErrorMessage("Given b [" + request.getB() + "] does not exist")
            .execute();
    }
}
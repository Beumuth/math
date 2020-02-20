package com.beumuth.math.client.jgraph;

import com.beumuth.math.MathClient;
import com.beumuth.math.client.settheory.orderedset.OrderedSet;
import feign.Param;
import feign.RequestLine;

import java.util.List;
import java.util.Set;

public interface ElementClient extends MathClient {
    @RequestLine("GET api/jgraph/elements/element/{id}/exists")
    boolean doesElementExist(@Param("id") long id);

    @RequestLine("GET api/jgraph/elements/exist/any?ids={ids}")
    boolean doAnyElementsExist(@Param("ids") Set<Long> ids);

    @RequestLine("GET api/jgraph/elements/exist/all?ids={ids}")
    boolean doAllElementsExist(@Param("ids") Set<Long> ids);

    @RequestLine("GET api/jgraph/elements/element/with/{a}/or/{b}/exists")
    boolean doesElementExistWithAOrB(@Param("a") long a, @Param("b") long b);

    @RequestLine("GET api/jgraph/elements/element/with/{a}/and/{b}/exists")
    boolean doesElementExistWithAAndB(@Param("a") long a, @Param("b") long b);

    @RequestLine("GET api/jgraph/elements/element/{id}/isNode")
    boolean isElementNode(@Param("id") long id);

    @RequestLine("GET api/jgraph/elements/areNodes?ids={ids}")
    OrderedSet<Boolean> areElementsNodes(@Param("ids") OrderedSet<Long> ids);

    @RequestLine("GET api/jgraph/elements/element/{id}/isPendantFrom/{idFrom}")
    boolean isElementPendantFrom(@Param("id") long id, @Param("idFrom") long idFrom);

    @RequestLine("GET api/jgraph/elements/arePendantsFrom/{idFrom}?ids={ids}")
    OrderedSet<Boolean> areElementsPendantsFrom(@Param("idFrom") long idFrom, @Param("ids") OrderedSet<Long> ids);

    @RequestLine("GET api/jgraph/elements/element/{id}/isPendantTo/{idTo}")
    boolean isElementPendantTo(@Param("id") long id, @Param("idTo") long idTo);

    @RequestLine("GET api/jgraph/elements/arePendantsTo/{idTo}?ids={ids}")
    OrderedSet<Boolean> areElementsPendantsTo(@Param("idTo") long idTo, @Param("ids") OrderedSet<Long> ids);

    @RequestLine("GET api/jgraph/elements/element/{id}/isLoopOn/{idOn}")
    boolean isElementLoopOn(@Param("id") long id, @Param("idOn") long idOn);

    @RequestLine("GET api/jgraph/elements/areLoopsOn/{idOn}?ids={ids}")
    OrderedSet<Boolean> areElementsLoopsOn(@Param("idOn") long idOn, @Param("ids") OrderedSet<Long> ids);

    @RequestLine("GET api/jgraph/elements/element/{id}/isEndpoint")
    boolean isElementEndpoint(@Param("id") long id);

    @RequestLine("GET api/jgraph/elements/areEndpoints?ids={ids}")
    OrderedSet<Boolean> areElementsEndpoints(@Param("ids") OrderedSet<Long> ids);

    @RequestLine("GET api/jgraph/elements/connected/{a}/{b}")
    boolean areElementsConnected(@Param("a") long a, @Param("b") long b);

    @RequestLine("GET api/jgraph/elements/with/{a}/or/{b}/count")
    int numElementsWithAOrB(@Param("a") long a, @Param("b") long b);

    @RequestLine("GET api/jgraph/elements/with/{a}/and/{b}/count")
    int numElementsWithAAndB(@Param("a") long a, @Param("b") long b);

    @RequestLine("GET api/jgraph/elements/nodes/count")
    int numNodes();

    @RequestLine("GET api/jgraph/elements/pendants/from/{idFrom}/count")
    int numPendantsFrom(@Param("idFrom") long idFrom);

    @RequestLine("GET api/jgraph/elements/pendants/to/{idTo}/count")
    int numPendantsTo(@Param("idTo") long idTo);

    @RequestLine("GET api/jgraph/elements/loops/on/{idOn}/count")
    int numLoopsOn(@Param("idOn") long idOn);

    @RequestLine("GET api/jgraph/elements/element/{id}")
    Element getElement(@Param("id") long id);

    @RequestLine("GET api/jgraph/elements/ids")
    OrderedSet<Long> getAllIds();

    @RequestLine("GET api/jgraph/elements/ids?ids={ids}")
    OrderedSet<Long> getIds(@Param("ids") OrderedSet<Long> ids);

    @RequestLine("GET api/jgraph/elements/ids/with/{a}/or/{b}")
    OrderedSet<Long> getIdsWithAOrB(@Param("a") long a, @Param("b") long b);

    @RequestLine("GET api/jgraph/elements/ids/with/{a}/and/{b}")
    OrderedSet<Long> getIdsWithAAndB(@Param("a") long a, @Param("b") long b);

    @RequestLine("GET api/jgraph/elements/nodes/ids")
    OrderedSet<Long> getIdsNodes();

    @RequestLine("GET api/jgraph/elements/element/{idFrom}/pendants/from/ids")
    OrderedSet<Long> getIdsPendantsFrom(@Param("idFrom") long idFrom);

    @RequestLine("GET api/jgraph/elements/element/{idTo}/pendants/to/ids")
    OrderedSet<Long> getIdsPendantsTo(@Param("idTo") long idTo);

    @RequestLine("GET api/jgraph/elements/element/{idOn}/loops/on/ids")
    OrderedSet<Long> getIdsLoopsOn(@Param("idOn") long iOn);

    @RequestLine("GET api/jgraph/elements/element/{id}/endpoints/of/ids")
    OrderedSet<Long> getIdsEndpointsOf(@Param("id") long id);

    @RequestLine("GET api/jgraph/elements/endpoints/of/ids?ids={ids}")
    OrderedSet<Set<Long>> getIdsEndpointsOfForEach(@Param("ids") OrderedSet<Long> ids);

    @RequestLine("GET api/jgraph/elements")
    OrderedSet<Element> getAllElements();

    @RequestLine("GET api/jgraph/elements?ids={ids}")
    OrderedSet<Element> getElements(@Param("ids") OrderedSet<Long> ids);

    @RequestLine("GET api/jgraph/elements/with/{a}/or/{b}")
    OrderedSet<Element> getElementsWithAOrB(@Param("a") long a, @Param("b") long b);

    @RequestLine("GET api/jgraph/elements/with/{a}/and/{b}")
    OrderedSet<Element> getElementsWithAAndB(@Param("a") long a, @Param("b") long b);

    @RequestLine("GET api/jgraph/elements/nodes")
    OrderedSet<Element> getNodes();

    @RequestLine("GET api/jgraph/elements/element/{idFrom}/pendants/from")
    OrderedSet<Element> getPendantsFrom(@Param("idFrom") long idFrom);

    @RequestLine("GET api/jgraph/elements/element/{idTo}/pendants/to")
    OrderedSet<Element> getPendantsTo(@Param("idTo") long idTo);

    @RequestLine("GET api/jgraph/elements/element/{idOn}/loops/on")
    OrderedSet<Element> getLoopsOn(@Param("idOn") long iOn);

    @RequestLine("GET api/jgraph/elements/element/{id}/endpoints/of")
    OrderedSet<Element> getEndpointsOf(@Param("id") long id);

    @RequestLine("GET api/jgraph/elements/endpoints/of?ids={ids}")
    OrderedSet<OrderedSet<Element>> getEndpointsOfForEach(@Param("ids") OrderedSet<Long> ids);

    @RequestLine("POST api/jgraph/elements/element")
    long createElement(CreateElementRequest request);

    @RequestLine("POST api/jgraph/elements/element")
    OrderedSet<Long> createElements(OrderedSet<CreateElementRequest> requests);

    @RequestLine("POST api/jgraph/elements/nodes/node")
    long createNode();

    @RequestLine("POST api/jgraph/elements/nodes")
    OrderedSet<Long> createNodes(int howMany);

    @RequestLine("POST api/jgraph/elements/element/{idFrom}/pendant/from")
    long createPendantFrom(@Param("idFrom") long from);

    @RequestLine("POST api/jgraph/elements/element/{idFrom}/pendants")
    OrderedSet<Long> createPendantsFrom(int howMany, @Param("idFrom") long idFrom);

    @RequestLine("POST api/jgraph/elements/element/{idTo}/pendant/to")
    long createPendantTo(@Param("idTo") long idTo);

    @RequestLine("POST api/jgraph/elements/element/{idTo}/pendants/to")
    OrderedSet<Long> createPendantsTo(int howMany, @Param("idTo") long idTo);

    @RequestLine("POST api/jgraph/elements/element/{idOn}/loop")
    long createLoopOn(@Param("idOn") long idOn);

    @RequestLine("POST api/jgraph/elements/element/{idOn}/loops")
    OrderedSet<Long> createLoopsOn(int howMany, @Param("idOn") long idOn);

    @RequestLine("PUT api/jgraph/elements/element/{id}")
    void updateElement(@Param("id") long id, UpdateElementRequest request);

    @RequestLine("PUT api/jgraph/elements?ids={ids}")
    void updateElements(List<UpdateElementRequest> requests, @Param("ids") OrderedSet<Long> ids);

    @RequestLine("DELETE api/jgraph/elements/element/{id}")
    void deleteElement(@Param("id") long id);

    @RequestLine("DELETE api/jgraph/elements?ids={ids}")
    void deleteElements(@Param("ids") Set<Long> ids);
}

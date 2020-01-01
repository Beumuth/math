package com.beumuth.math.client.settheory.orderedpair;

import com.beumuth.math.MathClient;
import feign.Param;
import feign.RequestLine;

public interface OrderedPairClient extends MathClient {
    @RequestLine("GET api/orderedPairs/orderedPair/{id}/exists")
    boolean exists(@Param("id") long id);

    @RequestLine("GET api/orderedPairs/orderedPair/{id}")
    OrderedPair get(@Param("id") long id);

    @RequestLine("POST api/orderedPairs/orderedPair")
    long create(CrupdateOrderedPairRequest request);

    @RequestLine("PUT api/orderedPairs/orderedPair/{id}")
    void update(@Param("id") long id, CrupdateOrderedPairRequest request);

    @RequestLine("DELETE api/orderedPairs/orderedPair/{id}")
    void delete(@Param("id") long id);

    @RequestLine("PUT api/orderedPairs/orderedPair/{id}/swap")
    void swap(@Param("id") long id);
}

package com.beumuth.collections.client.element;

import com.beumuth.collections.CollectionsClient;
import feign.Param;
import feign.RequestLine;

import java.util.List;
import java.util.Set;

public interface ElementClient extends CollectionsClient {
    @RequestLine("GET api/elements/element/{id}/exists")
    boolean doesElementExist(@Param("id") long id);

    @RequestLine("GET api/elements/element/{id}")
    Element getElement(@Param("id") long id);

    @RequestLine("POST api/elements/element")
    long createElement();

    @RequestLine("POST api/elements")
    List<Long> createMultipleElements(int numToCreate);

    @RequestLine("DELETE api/elements/element/{id}")
    void deleteElement(@Param("id") long id);

    @RequestLine("DELETE api/elements")
    void deleteMultipleElements(Set<Long> ids);
}

package com.beumuth.math.client.settheory.object;

import com.beumuth.math.MathClient;
import feign.Param;
import feign.RequestLine;

import java.util.List;
import java.util.Set;

public interface ObjectClient extends MathClient {
    @RequestLine("GET api/objects/object/{id}/exists")
    boolean exists(@Param("id") long id);

    @RequestLine("GET api/objects/object/{id}")
    Object get(@Param("id") long id);

    @RequestLine("POST api/objects/object")
    long create();

    @RequestLine("POST api/objects")
    List<Long> createMultiple(int numToCreate);

    @RequestLine("DELETE api/objects/object/{id}")
    void delete(@Param("id") long id);

    @RequestLine("DELETE api/objects")
    void deleteAll(Set<Long> ids);
}

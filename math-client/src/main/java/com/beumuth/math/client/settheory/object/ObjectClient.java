package com.beumuth.math.client.settheory.object;

import com.beumuth.math.MathClient;
import feign.Param;
import feign.RequestLine;

import java.util.List;
import java.util.Set;

public interface ObjectClient extends MathClient {
    @RequestLine("GET api/objects/object/{id}/exists")
    boolean doesObjectExist(@Param("id") long id);

    @RequestLine("GET api/objects/object/{id}")
    Object getObject(@Param("id") long id);

    @RequestLine("POST api/objects/object")
    long createObject();

    @RequestLine("POST api/objects")
    List<Long> createMultipleObjects(int numToCreate);

    @RequestLine("DELETE api/objects/object/{id}")
    void deleteObject(@Param("id") long id);

    @RequestLine("DELETE api/objects")
    void deleteMultipleObjects(Set<Long> ids);
}

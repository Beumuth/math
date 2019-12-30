package com.beumuth.math.client.settheory.set;

import com.beumuth.math.MathClient;
import feign.Param;
import feign.RequestLine;

public interface SetClient extends MathClient {
    @RequestLine("GET api/sets/set/{id}/exists")
    boolean doesSetExist(@Param("id") long id);

    @RequestLine("GET api/sets/set/{id}")
    Set getSet(@Param("id") long id);

    @RequestLine("GET api/sets/set/{id}/elements")
    java.util.Set<Long> getSetElements(@Param("id") long id);

    @RequestLine("POST api/sets/set")
    long createSet();

    @RequestLine("POST api/sets/set/withElements")
    long createSetWithElements(java.util.Set<Long> idElements);

    @RequestLine("POST api/sets/set/{id}/copy")
    long copySet(@Param("id") long id);

    @RequestLine("DELETE api/sets/set/{id}")
    void deleteSet(@Param("id") long id);

    @RequestLine("GET api/sets/set/{idSet}/containsElement/{idElement}")
    boolean doesSetContainElement(@Param("idSet") long idSet, @Param("idElement") long idElement);

    @RequestLine("GET api/sets/set/{idSet}/containsElements?idElements={idElements}")
    boolean doesSetContainElements(@Param("idSet") long idSet, @Param("idElements") java.util.Set<Long> idElements);

    @RequestLine("PUT api/sets/set/{idSet}/element/{idElement}")
    void addElementToSet(@Param("idSet") long idSet, @Param("idElement") long idElement);

    @RequestLine("POST api/sets/set/{idSet}/element")
    long createAndAddElementToSet(@Param("idSet") long idSet);

    @RequestLine("DELETE api/sets/set/{idSet}/element/{idElement}")
    void removeElementFromSet(@Param("idSet") long idSet, @Param("idElement") long idElement);

    @RequestLine("GET api/sets/set/{idSetA}/equals/{idSetB}")
    boolean areEqual(@Param("idSetA") long idSetA, @Param("idSetB") long idSetB);

    @RequestLine("GET api/sets/set/{idSetA}/isSubset/{idSetB}")
    boolean isSubset(@Param("idSetA") long idPossibleSubset, @Param("idSetB") long idPossibleSuperset);

    @RequestLine("GET api/sets/set/{id}/size")
    int setCardinality(@Param("id") long id);

    @RequestLine("GET api/sets/set/{id}/isEmpty")
    boolean isEmptySet(@Param("id") long id);

    @RequestLine("POST api/sets/set/{idSetA}/intersect/{idSetB}")
    long intersection(@Param("idSetA") long idSetA, @Param("idSetB") long idSetB);

    @RequestLine("POST api/sets/intersect")
    long intersectMultipleSets(java.util.Set<Long> idSets);

    @RequestLine("POST api/sets/set/{idSetA}/union/{idSetB}")
    long union(@Param("idSetA") long idSetA, @Param("idSetB") long idSetB);

    @RequestLine("POST api/sets/union")
    long unionMultipleSets(java.util.Set<Long> idSets);

    @RequestLine("POST api/sets/set/{idSetA}/subtract/{idSetB}")
    long difference(@Param("idSetA") long idSetA, @Param("idSetB") long idSetB);

    @RequestLine("POST api/sets/set/{idSetA}/symmetricDifference/{idSetB}")
    long symmetricDifference(@Param("idSetA") long idSetA, @Param("idSetB") long idSetB);
}

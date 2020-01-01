package com.beumuth.math.client.settheory.set;

import com.beumuth.math.MathClient;
import feign.Param;
import feign.RequestLine;

public interface SetClient extends MathClient {
    @RequestLine("GET api/sets/set/{id}/exists")
    boolean exists(@Param("id") long id);

    @RequestLine("GET api/sets/set/{id}")
    Set get(@Param("id") long id);

    @RequestLine("GET api/sets/set/{id}/elements")
    java.util.Set<Long> getElements(@Param("id") long id);

    @RequestLine("POST api/sets/set")
    long create();

    @RequestLine("POST api/sets/set/withElements")
    long createWithElements(java.util.Set<Long> idObjects);

    @RequestLine("POST api/sets/set/{id}/copy")
    long copy(@Param("id") long id);

    @RequestLine("DELETE api/sets/set/{id}")
    void delete(@Param("id") long id);

    @RequestLine("GET api/sets/set/{idSet}/contains/{idObject}")
    boolean contains(@Param("idSet") long idSet, @Param("idObject") long idObject);

    @RequestLine("GET api/sets/set/{idSet}/contains?idObjects={idObjects}")
    boolean containsAll(@Param("idSet") long idSet, @Param("idObjects") java.util.Set<Long> idObjects);

    @RequestLine("PUT api/sets/set/{idSet}/element/{idObject}")
    void addElement(@Param("idSet") long idSet, @Param("idObject") long idObject);

    @RequestLine("POST api/sets/set/{idSet}/element")
    long createAndAddElement(@Param("idSet") long idSet);

    @RequestLine("DELETE api/sets/set/{idSet}/element/{idObject}")
    void removeElement(@Param("idSet") long idSet, @Param("idObject") long idElement);

    @RequestLine("GET api/sets/set/{idSetA}/equals/{idSetB}")
    boolean areEqual(@Param("idSetA") long idSetA, @Param("idSetB") long idSetB);

    @RequestLine("GET api/sets/set/{idSetA}/isSubset/{idSetB}")
    boolean isSubset(@Param("idSetA") long idPossibleSubset, @Param("idSetB") long idPossibleSuperset);

    @RequestLine("GET api/sets/set/{idSetA}/isDisjoint/{idSetB}")
    boolean areDisjoint(@Param("idSetA") long idSetA, @Param("idSetB") long idSetB);

    @RequestLine("GET api/sets/areDisjoint?idSets={idSets}")
    boolean areDisjoint(@Param("idSets") java.util.Set<Long> idSets);

    @RequestLine("GET api/sets/set/{idSet}/isPartition?candidatePartition={candidatePartition}")
    boolean isPartition(
        @Param("candidatePartition") java.util.Set<Long> candidatePartition,
        @Param("idSet") long idSet
    );

    @RequestLine("GET api/sets/set/{id}/size")
    int cardinality(@Param("id") long id);

    @RequestLine("GET api/sets/set/{id}/isEmpty")
    boolean isEmpty(@Param("id") long id);

    @RequestLine("POST api/sets/set/{idSetA}/intersect/{idSetB}")
    long intersection(@Param("idSetA") long idSetA, @Param("idSetB") long idSetB);

    @RequestLine("POST api/sets/intersect")
    long intersection(java.util.Set<Long> idSets);

    @RequestLine("POST api/sets/set/{idSetA}/union/{idSetB}")
    long union(@Param("idSetA") long idSetA, @Param("idSetB") long idSetB);

    @RequestLine("POST api/sets/union")
    long union(java.util.Set<Long> idSets);

    @RequestLine("POST api/sets/set/{idSetA}/subtract/{idSetB}")
    long difference(@Param("idSetA") long idSetA, @Param("idSetB") long idSetB);

    @RequestLine("POST api/sets/set/{idSet}/complement/{idUniversalSet}")
    long complement(@Param("idSet") long idSet, @Param("idUniversalSet") long idUniversalSet);

    @RequestLine("POST api/sets/set/{idSetA}/symmetricDifference/{idSetB}")
    long symmetricDifference(@Param("idSetA") long idSetA, @Param("idSetB") long idSetB);
}

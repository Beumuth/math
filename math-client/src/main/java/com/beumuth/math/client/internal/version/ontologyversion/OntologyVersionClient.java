package com.beumuth.math.client.internal.version.ontologyversion;

import com.beumuth.math.MathClient;
import com.beumuth.math.client.settheory.orderedset.OrderedSet;
import feign.Param;
import feign.RequestLine;

public interface OntologyVersionClient extends MathClient {
    @RequestLine("GET api/ontologyVersions/ontologyVersion/{semanticVersion}/exists")
    boolean doesOntologyVersionExist(@Param("ontologyVersion") String semanticVersion);

    @RequestLine("GET api/ontologyVersions/ontologyVersion/{semanticVersion}")
    OntologyVersion getOntologyVersion(@Param("semanticVersion") String semanticVersion);

    @RequestLine("GET api/ontologyVersions")
    OrderedSet<OntologyVersion> getAllOntologyVersions();

    @RequestLine("GET api/ontologyVersions/ontologyVersion/current")
    OntologyVersion getCurrentOntologyVersion();

    @RequestLine("GET api/ontologyVersions/ontologyVersion/mostRecent")
    OntologyVersion getMostRecentOntologyVersion();
}

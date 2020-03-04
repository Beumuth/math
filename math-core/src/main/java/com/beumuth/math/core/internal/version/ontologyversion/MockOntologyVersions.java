package com.beumuth.math.core.internal.version.ontologyversion;

import com.beumuth.math.client.internal.version.ontologyversion.OntologyVersion;
import com.beumuth.math.client.internal.version.ontologyversion.SemanticVersion;
import com.google.common.collect.Lists;

import java.util.Collections;

public class MockOntologyVersions {
    public static final OntologyVersion NONEXISTENT_VERSION = new OntologyVersion(
        new SemanticVersion(
            0,
            0,
            404,
            Lists.newArrayList("does", "not", "exist"),
            Collections.emptyList()
        ),
        "Mock nonexistent OntologyVersion"
    );
}

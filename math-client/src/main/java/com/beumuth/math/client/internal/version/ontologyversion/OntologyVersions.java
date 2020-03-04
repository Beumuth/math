package com.beumuth.math.client.internal.version.ontologyversion;

import com.beumuth.math.client.settheory.orderedset.OrderedSet;
import com.beumuth.math.client.settheory.orderedset.OrderedSets;

public class OntologyVersions {
    public static final OntologyVersion VERSION_0_0_0 = new OntologyVersion(
        new SemanticVersion(0, 0, 0),
        "Start"
    );
    public static final OntologyVersion VERSION_0_1_0 = new OntologyVersion(
        new SemanticVersion(0, 1, 0),
        "Element"
    );
    public static final OrderedSet<OntologyVersion> ALL = OrderedSets.with(
        VERSION_0_0_0,
        VERSION_0_1_0
    );
}

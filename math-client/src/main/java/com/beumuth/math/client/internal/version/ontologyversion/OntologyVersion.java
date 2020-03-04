package com.beumuth.math.client.internal.version.ontologyversion;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(of="semanticVersion")
public class OntologyVersion {
    private SemanticVersion semanticVersion;
    private String description;
}

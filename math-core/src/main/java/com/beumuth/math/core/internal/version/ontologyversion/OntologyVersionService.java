package com.beumuth.math.core.internal.version.ontologyversion;

import com.beumuth.math.client.internal.version.ontologyversion.OntologyVersion;
import com.beumuth.math.client.internal.version.ontologyversion.OntologyVersions;
import com.beumuth.math.client.internal.version.ontologyversion.SemanticVersion;
import com.beumuth.math.core.internal.metaontology.MetaontologyService;
import com.google.common.collect.Iterables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

@Service
@DependsOn("metaontologyService")
public class OntologyVersionService {
    private MetaontologyService metaontologyService;


    public OntologyVersionService(@Autowired MetaontologyService metaontologyService) {
        this.metaontologyService = metaontologyService;
    }

    public boolean doesOntologyVersionExist(SemanticVersion semanticVersion) {
        return OntologyVersions
            .ALL
            .stream()
            .anyMatch(ontologyVersion -> ontologyVersion.getSemanticVersion().equals(semanticVersion));
    }

    public OntologyVersion getOntologyVersion(SemanticVersion semanticVersion) {
        return OntologyVersions
            .ALL
            .stream()
            .filter(ontologyVersion -> ontologyVersion.getSemanticVersion().equals(semanticVersion))
            .findFirst()
            .get();
    }

    public OntologyVersion getCurrentOntologyVersion() {
        return OntologyVersions
            .ALL
            .stream()
            .filter(ontologyVersion ->
                ontologyVersion
                    .getSemanticVersion()
                    .equals(
                        metaontologyService
                            .getEnvironmentService()
                            .getCurrentVersion()
                    )
            ).findFirst()
            .get();
    }

    public OntologyVersion getMostRecentOntologyVersion() {
        return Iterables.getLast(OntologyVersions.ALL);
    }
}
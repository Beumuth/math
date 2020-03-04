package com.beumuth.math.core.internal.metaontology;

import com.beumuth.math.core.internal.application.ApplicationService;
import com.beumuth.math.core.internal.database.DatabaseService;
import com.beumuth.math.core.internal.environment.EnvironmentService;
import com.beumuth.math.core.internal.version.ontologyversion.OntologyVersionService;
import com.beumuth.math.core.internal.version.versiontransgrade.VersionTransgradeService;
import com.beumuth.math.core.jgraph.element.ElementService;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Getter
public class MetaontologyService {

    private DatabaseService databaseService;
    private ApplicationService applicationService;
    private EnvironmentService environmentService;
    private ElementService elementService;
    private OntologyVersionService ontologyVersionService;
    private VersionTransgradeService versionTransgradeService;

    public MetaontologyService(
        @Autowired DatabaseService databaseService,
        @Autowired ApplicationService applicationService,
        @Autowired EnvironmentService environmentService,
        @Autowired ElementService elementService,
        @Autowired OntologyVersionService ontologyVersionService,
        @Autowired VersionTransgradeService versionTransgradeService
    ) {
        this.databaseService = databaseService;
        this.applicationService = applicationService;
        this.environmentService = environmentService;
        this.elementService = elementService;
        this.ontologyVersionService = ontologyVersionService;
        this.versionTransgradeService = versionTransgradeService;
    }
}

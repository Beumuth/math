package com.beumuth.math.core.internal.version.versiontransgrade;

import com.beumuth.math.client.internal.version.ontologyversion.OntologyVersion;
import com.beumuth.math.client.internal.version.ontologyversion.SemanticVersion;
import com.beumuth.math.client.settheory.orderedset.OrderedSet;
import com.beumuth.math.client.settheory.orderedset.OrderedSets;
import com.beumuth.math.core.internal.metaontology.MetaontologyService;
import com.beumuth.math.client.internal.version.ontologyversion.OntologyVersions;
import com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class VersionTransgradeService {

    private MetaontologyService metaontologyService;

    public VersionTransgradeService(@Autowired MetaontologyService metaontologyService) {
        this.metaontologyService = metaontologyService;
    }

    /**
     * Brings the ontology to its initial version.
     */
    public void initialize() {
        try {
            transgrade(OntologyVersions.VERSION_0_0_0.getSemanticVersion());
        } catch(TransgradeNotPossibleException e) {
            //Should not be possible
            throw new RuntimeException("No transgrade to the initial version found", e);
        }
    }

    public boolean doesVersionTransgradeExistWithFromAndTo(SemanticVersion from, SemanticVersion to) {
        return VersionTransgrades
            .ALL
            .stream()
            .anyMatch(versionTransgrade ->
                versionTransgrade.getFrom().equals(from) &&
                    versionTransgrade.getTo().equals(to)
            );
    }

    public boolean isTransgradePossible(SemanticVersion from, SemanticVersion to) {
        try {
            return from.equals(to) || getTransgradePath(from, to).isEmpty();
        } catch(TransgradeNotPossibleException e) {
            return false;
        }
    }

    public VersionTransgrade getVersionTransgradeWithFromAndTo(SemanticVersion from, SemanticVersion to) {
        return VersionTransgrades
            .ALL
            .stream()
            .filter(versionTransgrade ->
                versionTransgrade.getFrom().equals(from) &&
                    versionTransgrade.getTo().equals(to)
            ).findFirst()
            .get();
    }

    @Transactional
    public void upgradeToMostRecentVersion() throws TransgradeNotPossibleException {
        transgrade(
            metaontologyService
                .getOntologyVersionService()
                .getMostRecentOntologyVersion()
                .getSemanticVersion()
        );
    }

    /**
     * Transgrades (upgrades or downgrades) from the current version to the given version.
     */
    @Transactional
    public void transgrade(SemanticVersion to) throws TransgradeNotPossibleException {
        getTransgradePath(
            metaontologyService
                .getOntologyVersionService()
                .getCurrentOntologyVersion()
                .getSemanticVersion(),
            to
        ).forEach(versionTransgrade ->
            versionTransgrade
                .getScript()
                .accept(metaontologyService)
        );
        metaontologyService
            .getEnvironmentService()
            .setCurrentVersion(to);
    }

    private OrderedSet<VersionTransgrade> getTransgradePath(
        SemanticVersion from,
        SemanticVersion to
    ) throws TransgradeNotPossibleException {
        //Get all ontology versions
        OrderedSet<SemanticVersion> ontologyVersions =
            OntologyVersions
                .ALL
                .stream()
                .map(OntologyVersion::getSemanticVersion)
                .collect(Collectors.toCollection(OrderedSet::new));

        //Initialize distances
        List<Integer> distances = ontologyVersions
            .stream()
            .map(ontologyVersion -> Integer.MAX_VALUE)
            .collect(Collectors.toList());
        distances.set(ontologyVersions.indexOf(from), 0);

        //Initialize previous versions
        List<SemanticVersion> previousVersions = ontologyVersions
            .stream()
            .<SemanticVersion>map(ontologyVersion -> null)
            .collect(Collectors.toList());
        OrderedSet<SemanticVersion> queue = OrderedSets.with(ontologyVersions);
        while(! queue.isEmpty()) {
            //The next version is the first in the queue (i.e. the one with the minimum distance)
            SemanticVersion curVersion = queue.remove(0);
            int indexCurVersion = ontologyVersions.indexOf(curVersion);

            //Have we found the destination version?
            if(curVersion.equals(to)) {
                //Yes. Now calculate the path.
                OrderedSet<VersionTransgrade> path = OrderedSets.empty();
                SemanticVersion curVersionInPath = curVersion;
                SemanticVersion nextVersionInPath = previousVersions.get(indexCurVersion);

                //While the current version does not equal from
                while(nextVersionInPath != null) {

                    //Add the current version to the path
                    path.add(
                        0,
                        getVersionTransgradeWithFromAndTo(curVersionInPath, nextVersionInPath)
                    );

                    //Set the new current version in the path to the version previous to the current version.
                    curVersionInPath = nextVersionInPath;
                    nextVersionInPath = previousVersions.get(
                        ontologyVersions.indexOf(curVersionInPath)
                    );
                }
                //Path calculated, return
                return path;
            }

            //Destination version not yet reached.
            //Add the neighbors of the current version to the queue.
            Set<SemanticVersion> neighbors = Sets.intersection(queue, getVersionNeighbors(curVersion));
            for(SemanticVersion neighbor : neighbors) {
                int indexNeighbor = ontologyVersions.indexOf(neighbor);
                int distanceToNeighbor = distances.get(indexCurVersion) + Math.abs(indexCurVersion - indexNeighbor);
                if(distanceToNeighbor < distances.get(indexNeighbor)) {
                    distances.set(indexNeighbor, distanceToNeighbor);
                    previousVersions.set(indexNeighbor, curVersion);
                }
            }

            //Sort the queue by minimum distance
            queue.sort(
                Comparator.comparingInt(version ->
                    distances.get(ontologyVersions.indexOf(version))
                )
            );
        }

        //No path found
        throw new TransgradeNotPossibleException(from, to);
    }

    private Set<SemanticVersion> getVersionNeighbors(SemanticVersion version) {
        Set<SemanticVersion> neighbors = Sets.newHashSet();
        VersionTransgrades
            .ALL
            .forEach(versionTransgrade -> {
                if(versionTransgrade.getFrom().matches(version)) {
                    neighbors.add(versionTransgrade.getTo());
                } else if(versionTransgrade.getTo().matches(version)) {
                    neighbors.add(versionTransgrade.getFrom());
                }
            });
        return neighbors;
    }
}

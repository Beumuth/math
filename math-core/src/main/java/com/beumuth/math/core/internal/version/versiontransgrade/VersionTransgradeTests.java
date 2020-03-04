package com.beumuth.math.core.internal.version.versiontransgrade;


import com.beumuth.math.client.Clients;
import com.beumuth.math.client.internal.version.ontologyversion.OntologyVersion;
import com.beumuth.math.client.internal.version.ontologyversion.OntologyVersions;
import com.beumuth.math.client.internal.version.versiontransgrade.VersionTransgradeClient;
import com.beumuth.math.core.internal.client.ClientConfigurations;
import com.beumuth.math.core.internal.version.ontologyversion.MockOntologyVersions;
import com.beumuth.math.core.internal.version.ontologyversion.OntologyVersionService;
import feign.FeignException;
import org.bitbucket.radistao.test.annotation.AfterAllMethods;
import org.bitbucket.radistao.test.runner.BeforeAfterSpringTestRunner;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static com.beumuth.math.core.external.feign.FeignAssertions.assertExceptionLike;
import static org.junit.Assert.*;

@RunWith(BeforeAfterSpringTestRunner.class)
@SpringBootTest
public class VersionTransgradeTests {

    private VersionTransgradeService versionTransgradeService;
    private OntologyVersionService ontologyVersionService;
    private VersionTransgradeClient versionTransgradeClient;

    public VersionTransgradeTests(
        @Autowired VersionTransgradeService versionTransgradeService,
        @Autowired OntologyVersionService ontologyVersionService
        ) {
        this.versionTransgradeService = versionTransgradeService;
        this.ontologyVersionService = ontologyVersionService;
        this.versionTransgradeClient = Clients.getClient(VersionTransgradeClient.class, ClientConfigurations.LOCAL);
    }

    @AfterAllMethods
    public void cleanupTest() {
        versionTransgradeService.initialize();
    }

    public void initializeTest_fromInitializedState_shouldDoNothing() {
        versionTransgradeClient.initialize();
        assertInitialized();
    }

    public void initializeTest_fromUninitializedState_shouldInitialize() throws TransgradeNotPossibleException {
        //This test only applies if there is an non-initial version
        if(OntologyVersions.ALL.size() <= 1) {
            return;
        }
        versionTransgradeService.transgrade(
            OntologyVersions
                .ALL
                .get(1)
                .getSemanticVersion()
        );
        versionTransgradeClient.initialize();
        assertInitialized();
    }

    public void doesVersionTransgradeExistWithFromAndToTest_exists_shouldReturnTrue() {
        VersionTransgrades
            .ALL
            .forEach(versionTransgrade ->
                assertTrue(
                    versionTransgradeClient.doesVersionTransgradeExistWithFromAndTo(
                        versionTransgrade.getFrom().toString(),
                        versionTransgrade.getTo().toString()
                    )
                )
            );
    }

    public void doesVersionTransgradeExistWithFromAndToTest_doesNot_shouldReturnFalse() {
        //TODO - Test nonexistent version transgrade once it's possible to add programmatically
    }

    public void doesVersionTransgradeExistWithFromAndToTest_fromNotOntologyVersion_shouldReturn404() {
        try {
            versionTransgradeClient.doesVersionTransgradeExistWithFromAndTo(
                MockOntologyVersions.NONEXISTENT_VERSION.getSemanticVersion().toString(),
                VersionTransgrades.ALL.last().get().getTo().toString()
            );
            fail();
        } catch(FeignException e) {
            assertExceptionLike(
                e,
                404,
                MockOntologyVersions.NONEXISTENT_VERSION.getSemanticVersion().toString()
            );
        }
    }

    public void isTransgradePossibleTest_is_shouldReturnTrue() {
        //Ensure that the transgrade is possible for all transgrades
        VersionTransgrades
            .ALL
            .forEach(versionTransgrade ->
                assertTrue(
                    versionTransgradeClient.isTransgradePossible(
                        versionTransgrade.getFrom().toString(),
                        versionTransgrade.getTo().toString()
                    )
                )
            );
    }

    public void isTransgradePossibleTest_sameFromAndTo_shouldReturnTrue() {
        OntologyVersions
            .ALL
            .forEach(ontologyVersion ->
                assertTrue(
                    versionTransgradeClient.isTransgradePossible(
                        ontologyVersion.getSemanticVersion().toString(),
                        ontologyVersion.getSemanticVersion().toString()
                    )
                )
            );
    }

    //Test that a transgrade path exists from every version to the initial version
    public void isTransgradePossibleTest_everyVersion_toInitialVersion_shouldReturnTrue() {
        OntologyVersions
            .ALL
            .subList(
                1,
                OntologyVersions.ALL.size()
            ).forEach(ontologyVersion ->
                assertTrue(
                    versionTransgradeClient.isTransgradePossible(
                        ontologyVersion.getSemanticVersion().toString(),
                        OntologyVersions.VERSION_0_0_0.toString()
                    )
                )
            );
    }


    public void isTransgradePossibleTest_isNot_shouldReturnFalse() {
        //TODO - Test not possible version transgrade once it's possible to add programmatically
    }

    public void isTransgradePossibleTest_fromIsNotOntologyVersion_shouldReturn404() {
        try {
            versionTransgradeClient.isTransgradePossible(
                MockOntologyVersions.NONEXISTENT_VERSION.toString(),
                VersionTransgrades.ALL.last().get().getTo().toString()
            );
            fail();
        } catch(FeignException e) {
            assertExceptionLike(e, 404, MockOntologyVersions.NONEXISTENT_VERSION.toString());
        }
    }

    public void isTransgradePossibleTest_toIsNotOntologyVersion_shouldReturn404() {
        try {
            versionTransgradeClient.isTransgradePossible(
                VersionTransgrades.ALL.last().get().getTo().toString(),
                MockOntologyVersions.NONEXISTENT_VERSION.toString()
            );
            fail();
        } catch(FeignException e) {
            assertExceptionLike(e, 404, MockOntologyVersions.NONEXISTENT_VERSION.toString());
        }
    }

    public void upgradeToMostRecentVersionTest_fromEveryVersion_shouldHappen() {
        OntologyVersions
            .ALL
            .forEach(ontologyVersion ->{
                try {
                    versionTransgradeService.initialize();
                    versionTransgradeService.transgrade(ontologyVersion.getSemanticVersion());
                    OntologyVersion mostRecentOntologyVersion = ontologyVersionService.getMostRecentOntologyVersion();
                    versionTransgradeClient.transgrade(mostRecentOntologyVersion.getSemanticVersion().toString());
                    assertEquals(
                        mostRecentOntologyVersion,
                        ontologyVersionService.getCurrentOntologyVersion()
                    );
                } catch(TransgradeNotPossibleException e){
                    fail("Could not transgrade to version [" + ontologyVersion + "]");
                }
            });
        versionTransgradeClient.upgradeToMostRecentVersion();
    }

    public void transgradeTest_testAllTransgrades() {
        VersionTransgrades
            .ALL
            .forEach(versionTransgrade -> {
                try {
                    versionTransgradeService.initialize();
                    versionTransgradeService.transgrade(versionTransgrade.getFrom());
                    versionTransgradeClient.transgrade(versionTransgrade.getTo().toString());
                    assertEquals(
                        versionTransgrade.getTo(),
                        ontologyVersionService.getCurrentOntologyVersion().getSemanticVersion()
                    );
                } catch(TransgradeNotPossibleException e){
                    fail("Could not transgrade to version [" + versionTransgrade.getFrom().toString() + "]");
                }
            });
    }

    public void transgradeTest_toIsNotOntologyVersion_shouldReturn404(){
        try {
            versionTransgradeClient.transgrade(
                MockOntologyVersions
                    .NONEXISTENT_VERSION
                    .getSemanticVersion()
                    .toString()
            );
            fail();
        } catch(FeignException e) {
            assertExceptionLike(
                e,
                404,
                MockOntologyVersions
                    .NONEXISTENT_VERSION
                    .getSemanticVersion()
                    .toString()
            );
        }
    }

    private void assertInitialized() {
        assertEquals(
            OntologyVersions.VERSION_0_0_0,
            ontologyVersionService.getCurrentOntologyVersion()
        );
    }
}

package com.beumuth.math.core.internal.version.ontologyversion;

import com.beumuth.math.client.Clients;
import com.beumuth.math.client.internal.version.ontologyversion.OntologyVersionClient;
import com.beumuth.math.client.internal.version.ontologyversion.OntologyVersions;
import com.beumuth.math.core.internal.client.ClientConfigurations;
import com.beumuth.math.core.internal.environment.EnvironmentService;
import feign.FeignException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static com.beumuth.math.core.external.feign.FeignAssertions.assertExceptionLike;
import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class OntologyVersionTests {

    private OntologyVersionService ontologyVersionService;
    private EnvironmentService environmentService;
    private OntologyVersionClient ontologyVersionClient;

    public OntologyVersionTests(
        @Autowired OntologyVersionService ontologyVersionService,
        @Autowired EnvironmentService environmentService
    ) {
        this.ontologyVersionService = ontologyVersionService;
        this.environmentService = environmentService;
        ontologyVersionClient = Clients.getClient(OntologyVersionClient.class, ClientConfigurations.LOCAL);
    }

    @Test
    public void doesOntologyVersionExistTest_forEachExistent_shouldReturnTrue() {
        OntologyVersions
            .ALL
            .forEach(ontologyVersion ->
                assertTrue(
                    ontologyVersionClient.doesOntologyVersionExist(
                        ontologyVersion
                            .getSemanticVersion()
                            .toString()
                    )
                )
            );
    }

    @Test
    public void doesOntologyVersionExistTest_doesNotExist_shouldReturnFalse() {
        assertFalse(
            ontologyVersionClient.doesOntologyVersionExist(
                MockOntologyVersions
                    .NONEXISTENT_VERSION
                    .getSemanticVersion()
                    .toString()
            )
        );
    }

    @Test
    public void getOntologyVersionTest_forEachExistent_shouldBeReturned() {
        OntologyVersions
            .ALL
            .forEach(ontologyVersion ->
                assertEquals(
                    ontologyVersion,
                    ontologyVersionClient.getOntologyVersion(
                        ontologyVersion
                            .getSemanticVersion()
                            .toString()
                    )
                )
            );
    }

    @Test
    public void getOntologyVersionTest_doesNotExist_shouldReturn404() {
        try {
            ontologyVersionClient.getOntologyVersion(
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

    @Test
    public void getAllOntologyVersionsTest_shouldReturnAll() {
        assertEquals(
            OntologyVersions.ALL,
            ontologyVersionClient.getAllOntologyVersions()
        );
    }

    @Test
    public void getCurrentOntologyVersionTest_shouldReturnCurrent() {
        assertEquals(
            ontologyVersionService.getOntologyVersion(
                environmentService.getCurrentVersion()
            ),
            ontologyVersionClient.getCurrentOntologyVersion()
        );
    }

    @Test
    public void getMostRecentVersionTest_shouldReturnMostRecent() {
        assertEquals(
            OntologyVersions.ALL.last().get(),
            ontologyVersionClient.getMostRecentOntologyVersion()
        );
    }
}
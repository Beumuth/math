package com.beumuth.collections.core.environment;

import com.beumuth.collections.client.application.ApplicationConfiguration;
import com.beumuth.collections.client.environment.EnvironmentClient;
import com.beumuth.collections.client.environment.EnvironmentConfiguration;
import com.beumuth.collections.core.application.ApplicationService;
import com.beumuth.collections.core.application.ApplicationTests;
import com.beumuth.collections.core.client.ClientService;
import feign.FeignException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.lang.reflect.Method;

@RunWith(SpringRunner.class)
@SpringBootTest
public class EnvironmentTests {
    private static boolean initialized = false;
    private static int testsRan;
    private static int totalTests;
    private static ApplicationConfiguration startingConfiguration;
    private static EnvironmentClient client;

    @Autowired
    private ClientService clientService;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private EnvironmentService environmentService;

    @Before
    public void setupTests() {
        if(! initialized) {
            client = clientService.getEnvironmentClient();
            startingConfiguration = applicationService.getApplicationConfiguration();

            //Set total tests - credit https://stackoverflow.com/a/48981027/3816779
            testsRan = 0;
            totalTests = 0;
            Method[] methods = ApplicationTests.class.getMethods();
            for (Method method : methods) {
                if (method.getAnnotation(Test.class) != null) {
                    totalTests++;
                }
            }

            initialized = true;
        }
    }

    @After
    public void cleanupTests() {
        testsRan++;
        if(testsRan == totalTests)  {
            applicationService.setConfiguration(startingConfiguration);
            initialized = false;
        }
    }

    @Test
    public void getAllEnvironmentsTest_shouldSucceed() {
        //Just make sure it runs
        client.getEnvironments();
    }

    @Test
    public void getActiveEnvironmentTest_shouldSucceed() {
        //Just make sure it runs
        client.getActiveEnvironment();
    }

    @Test
    public void updateActiveEnvironmentTest_shouldSucceed() {
        try {
            //Add mock environment
            EnvironmentConfiguration mockEnvironment = MockEnvironments.validEnvironment();
            environmentService.addEnvironment(MockEnvironments.validEnvironment());

            //Set active environment to the mock
            client.setActiveEnvironment(mockEnvironment.name);

            //Ensure that the mock environment is the active environment
            Assert.assertTrue(mockEnvironment.name.equalsIgnoreCase(environmentService.getActiveEnvironment().name));
        } catch(Exception e) {
            throw e;
        } finally{

            //It's important the application configuration environment gets set back to what it was for the next tests
            applicationService.setConfiguration(startingConfiguration);
        }
    }

    @Test
    public void getEnvironmentTest_shouldSucceed() {
        //Just make sure it runs
        client.getEnvironment(startingConfiguration.activeEnvironment);
    }

    @Test
    public void updateEnvironmentTest_shouldSucceed() {
        //Create and add a mock environment
        EnvironmentConfiguration mockEnvironment = MockEnvironments.validEnvironment();
        environmentService.addEnvironment(mockEnvironment);
        try {

            //Update the mock environment's base url
            String newBaseUrl = "http://newMock.com";
            mockEnvironment.baseUrl = newBaseUrl;
            client.updateEnvironment(mockEnvironment.name, mockEnvironment);

            //Ensure that the update applied
            Assert.assertEquals(newBaseUrl, environmentService.getEnvironment(mockEnvironment.name).get().baseUrl);
        } catch(Exception e) {
            throw e;
        } finally {
            //It's important the mockEnvironment is removed for future tests
            environmentService.deleteEnvironment(mockEnvironment.name);
        }
    }

    @Test
    public void updateActiveEnvironmentName_shouldUpdateActiveEnvironment() {
        //Get the active environment and change its name
        EnvironmentConfiguration activeEnvironment = environmentService.getActiveEnvironment();
        String curName = activeEnvironment.name;
        String newName = "someNewNameThatDoesNotExist";
        activeEnvironment.name = newName;
        try {
            client.updateEnvironment(curName, activeEnvironment);
            Assert.assertEquals(newName, environmentService.getActiveEnvironment().name);
        } finally {
            //Ensure the configuration gets reverted for future tests
            applicationService.setConfiguration(startingConfiguration);
        }
    }

    @Test
    public void updateEnvironmentThatDoesNotExistTest_shouldReturn404() {
        try {
            client.updateEnvironment(
                "thisEnvironmentDoesNotExist",
                MockEnvironments.validEnvironment()
            );
        } catch (FeignException e) {
            Assert.assertEquals(404, e.status());
        }
    }

    @Test
    public void updateEnvironmentWithNullName_shouldReturn400() {
        //Create and add a mock environment
        EnvironmentConfiguration mockEnvironment = MockEnvironments.validEnvironment();
        environmentService.addEnvironment(mockEnvironment);
        try {
            //Add new environment with null name
            String curName = mockEnvironment.name;
            mockEnvironment.name = null;
            client.updateEnvironment(curName, mockEnvironment);
        } catch (FeignException e) {
            //Should throw 400
            Assert.assertEquals(400, e.status());
            Assert.assertTrue(e.contentUTF8().contains("name"));
        } finally {
            //Remove the mock environment
            environmentService.deleteEnvironment(mockEnvironment.name);
        }
    }

    @Test
    public void updateEnvironmentWithNullBaseUrl_shouldReturn400() {
        //Create and add a mock environment
        EnvironmentConfiguration mockEnvironment = MockEnvironments.validEnvironment();
        environmentService.addEnvironment(mockEnvironment);
        try {
            //Update the mock environment's base url to null
            mockEnvironment.baseUrl = null;
            client.updateEnvironment(mockEnvironment.name, mockEnvironment);
        } catch (FeignException e) {
            //Should throw 400
            Assert.assertEquals(400, e.status());
            Assert.assertTrue(e.contentUTF8().contains("baseUrl"));
        } finally {
            //Remove the mock environment
            environmentService.deleteEnvironment(mockEnvironment.name);
        }
    }

    @Test
    public void updateEnvironmentWithNullDatabaseConfiguration_shouldReturn400() {
        //Create and add a mock environment
        EnvironmentConfiguration mockEnvironment = MockEnvironments.validEnvironment();
        environmentService.addEnvironment(mockEnvironment);
        try {
            //Update the mock environment's base url to null
            mockEnvironment.databaseConfiguration = null;
            client.updateEnvironment(mockEnvironment.name, mockEnvironment);
        } catch (FeignException e) {
            //Should throw 400
            Assert.assertEquals(400, e.status());
            Assert.assertTrue(e.contentUTF8().contains("databaseConfiguration"));
        } finally {
            //Remove the mock environment
            environmentService.deleteEnvironment(mockEnvironment.name);
        }
    }

    @Test
    public void updateEnvironmentWithNullDatabaseConfigurationHost_shouldReturn400() {
        //Create and add a mock environment
        EnvironmentConfiguration mockEnvironment = MockEnvironments.validEnvironment();
        environmentService.addEnvironment(mockEnvironment);
        try {
            //Update the mock databaseConfiguration's host to null
            mockEnvironment.databaseConfiguration.host = null;
            client.updateEnvironment(mockEnvironment.name, mockEnvironment);
        } catch (FeignException e) {
            //Should throw 400
            Assert.assertEquals(400, e.status());
            Assert.assertTrue(
                e.contentUTF8().contains("databaseConfiguration") && e.contentUTF8().contains("name")
            );
        } finally {
            //Remove the mock environment
            environmentService.deleteEnvironment(mockEnvironment.name);
        }
    }

    @Test
    public void updateEnvironmentWithNullDatabaseConfigurationPort_shouldReturn400() {
        //Create and add a mock environment
        EnvironmentConfiguration mockEnvironment = MockEnvironments.validEnvironment();
        environmentService.addEnvironment(mockEnvironment);
        try {
            //Update the mock databaseConfiguration's port to null
            mockEnvironment.databaseConfiguration.port = null;
            client.updateEnvironment(mockEnvironment.name, mockEnvironment);
        } catch (FeignException e) {
            //Should throw 400
            Assert.assertEquals(400, e.status());
            Assert.assertTrue(
                e.contentUTF8().contains("databaseConfiguration") && e.contentUTF8().contains("port")
            );
        } finally {
            //Remove the mock environment
            environmentService.deleteEnvironment(mockEnvironment.name);
        }
    }

    @Test
    public void updateEnvironmentWithNullDatabaseConfigurationUsername_shouldReturn400() {
        //Create and add a mock environment
        EnvironmentConfiguration mockEnvironment = MockEnvironments.validEnvironment();
        environmentService.addEnvironment(mockEnvironment);
        try {
            //Update the mock databaseConfiguration's username to null
            mockEnvironment.databaseConfiguration.username = null;
            client.updateEnvironment(mockEnvironment.name, mockEnvironment);
        } catch (FeignException e) {
            //Should throw 400
            Assert.assertEquals(400, e.status());
            Assert.assertTrue(
                e.contentUTF8().contains("databaseConfiguration") && e.contentUTF8().contains("username")
            );
        } finally {
            //Remove the mock environment
            environmentService.deleteEnvironment(mockEnvironment.name);
        }
    }

    @Test
    public void updateEnvironmentWithNullDatabaseConfigurationPassword_shouldReturn400() {
        //Create and add a mock environment
        EnvironmentConfiguration mockEnvironment = MockEnvironments.validEnvironment();
        environmentService.addEnvironment(mockEnvironment);
        try {
            //Update the mock databaseConfiguration's password to null
            mockEnvironment.databaseConfiguration.password = null;
            client.updateEnvironment(mockEnvironment.name, mockEnvironment);
        } catch (FeignException e) {
            //Should throw 400
            Assert.assertEquals(400, e.status());
            Assert.assertTrue(
                e.contentUTF8().contains("databaseConfiguration") && e.contentUTF8().contains("password")
            );
        } finally {
            //Remove the mock environment
            environmentService.deleteEnvironment(mockEnvironment.name);
        }
    }

    @Test
    public void updateEnvironmentWithNullDatabaseConfigurationDatabase_shouldReturn400() {
        //Create and add a mock environment
        EnvironmentConfiguration mockEnvironment = MockEnvironments.validEnvironment();
        environmentService.addEnvironment(mockEnvironment);
        try {
            //Update the mock databaseConfiguration's database to null
            mockEnvironment.databaseConfiguration.database = null;
            client.updateEnvironment(mockEnvironment.name, mockEnvironment);
        } catch (FeignException e) {
            //Should throw 400
            Assert.assertEquals(400, e.status());
            Assert.assertTrue(
                e.contentUTF8().contains("databaseConfiguration") && e.contentUTF8().contains("database")
            );
        } finally {
            //Remove the mock environment
            environmentService.deleteEnvironment(mockEnvironment.name);
        }
    }

    @Test
    public void updateEnvironmentWithNullDatabaseConfigurationIntegrationTestDatabase_shouldReturn400() {
        //Create and add a mock environment
        EnvironmentConfiguration mockEnvironment = MockEnvironments.validEnvironment();
        environmentService.addEnvironment(mockEnvironment);
        try {
            //Update the mock databaseConfiguration's integrationTestDatabase to null
            mockEnvironment.databaseConfiguration.integrationTestDatabase = null;
            client.updateEnvironment(mockEnvironment.name, mockEnvironment);
        } catch (FeignException e) {
            //Should throw 400
            Assert.assertEquals(400, e.status());
            Assert.assertTrue(
                e.contentUTF8().contains("databaseConfiguration") &&
                    e.contentUTF8().contains("integrationTestDatabase")
            );
        } finally {
            //Remove the mock environment
            environmentService.deleteEnvironment(mockEnvironment.name);
        }
    }

    @Test
    public void createValidEnvironment_shouldSucceed() {
        EnvironmentConfiguration validEnvironment = MockEnvironments.validEnvironment();
        try {
            //Create
            client.createEnvironment(validEnvironment);

            //Ensure it was added
            Assert.assertTrue(environmentService.getEnvironment(validEnvironment.name).isPresent());
        } finally {
            //Ensure the environment that got created is removed for future tests
            environmentService.deleteEnvironment(validEnvironment.name);
        }
    }

    @Test
    public void createEnvironmentWithDuplicateName_shouldReturn409() {
        //Create a mock environment with the same name as the active environment
        EnvironmentConfiguration mockEnvironment = MockEnvironments.validEnvironment();
        mockEnvironment.name = environmentService.getActiveEnvironment().name;
        try {
            //Attempt to create the mock environment
            client.createEnvironment(mockEnvironment);
        } catch(FeignException e) {
            Assert.assertEquals(409, e.status());
            Assert.assertTrue(e.contentUTF8().contains("name"));
        } finally {
            //Ensure to reset the application configuration for future tests
            applicationService.setConfiguration(startingConfiguration);
        }
    }

    @Test
    public void createEnvironmentWithNullName_shouldReturn400() {
        //Create a mock environment with a null name
        EnvironmentConfiguration mockEnvironment = MockEnvironments.validEnvironment();
        mockEnvironment.name = null;
        try {
            client.createEnvironment(mockEnvironment);
        } catch (FeignException e) {
            //Should throw 400
            Assert.assertEquals(400, e.status());
            Assert.assertTrue(e.contentUTF8().contains("name"));
        } finally {
            //Remove the mock environment
            environmentService.deleteEnvironment(mockEnvironment.name);
        }
    }

    @Test
    public void createEnvironmentWithNullBaseUrl_shouldReturn400() {
        //Create a mock environment with a null baseUrl
        EnvironmentConfiguration mockEnvironment = MockEnvironments.validEnvironment();
        mockEnvironment.baseUrl = null;
        try {
            client.createEnvironment(mockEnvironment);
        } catch (FeignException e) {
            //Should throw 400
            Assert.assertEquals(400, e.status());
            Assert.assertTrue(e.contentUTF8().contains("baseUrl"));
        } finally {
            //Remove the mock environment
            environmentService.deleteEnvironment(mockEnvironment.name);
        }
    }

    @Test
    public void createEnvironmentWithNullDatabaseConfiguration_shouldReturn400() {
        //Create and a mock environment with a null databaseConfiguration
        EnvironmentConfiguration mockEnvironment = MockEnvironments.validEnvironment();
        mockEnvironment.databaseConfiguration = null;
        try {
            //Update the mock environment's base url to null
            client.createEnvironment(mockEnvironment);
        } catch (FeignException e) {
            //Should throw 400
            Assert.assertEquals(400, e.status());
            Assert.assertTrue(e.contentUTF8().contains("databaseConfiguration"));
        } finally {
            //Remove the mock environment
            environmentService.deleteEnvironment(mockEnvironment.name);
        }
    }

    @Test
    public void createEnvironmentWithNullDatabaseConfigurationHost_shouldReturn400() {
        //Create a mock environment with a null databaseConfiguration host
        EnvironmentConfiguration mockEnvironment = MockEnvironments.validEnvironment();
        mockEnvironment.databaseConfiguration.host = null;
        try {
            client.createEnvironment(mockEnvironment);
        } catch (FeignException e) {
            //Should throw 400
            Assert.assertEquals(400, e.status());
            Assert.assertTrue(
                e.contentUTF8().contains("databaseConfiguration") && e.contentUTF8().contains("name")
            );
        } finally {
            //Remove the mock environment
            environmentService.deleteEnvironment(mockEnvironment.name);
        }
    }

    @Test
    public void createEnvironmentWithNullDatabaseConfigurationPort_shouldReturn400() {
        //Create a mock environment with a null databaseConfiguration port
        EnvironmentConfiguration mockEnvironment = MockEnvironments.validEnvironment();
        mockEnvironment.databaseConfiguration.port = null;
        try {
            //Update the mock databaseConfiguration's port to null
            client.createEnvironment(mockEnvironment);
        } catch (FeignException e) {
            //Should throw 400
            Assert.assertEquals(400, e.status());
            Assert.assertTrue(
                e.contentUTF8().contains("databaseConfiguration") && e.contentUTF8().contains("port")
            );
        } finally {
            //Remove the mock environment
            environmentService.deleteEnvironment(mockEnvironment.name);
        }
    }

    @Test
    public void createEnvironmentWithNullDatabaseConfigurationUsername_shouldReturn400() {
        //Create a mock environment with a null databaseConfiguration username
        EnvironmentConfiguration mockEnvironment = MockEnvironments.validEnvironment();
        mockEnvironment.databaseConfiguration.username = null;
        try {
            client.createEnvironment(mockEnvironment);
        } catch (FeignException e) {
            //Should throw 400
            Assert.assertEquals(400, e.status());
            Assert.assertTrue(
                e.contentUTF8().contains("databaseConfiguration") && e.contentUTF8().contains("username")
            );
        } finally {
            //Remove the mock environment
            environmentService.deleteEnvironment(mockEnvironment.name);
        }
    }

    @Test
    public void createEnvironmentWithNullDatabaseConfigurationPassword_shouldReturn400() {
        //Create a mock environment with a null databaseConfiguration password
        EnvironmentConfiguration mockEnvironment = MockEnvironments.validEnvironment();
        mockEnvironment.databaseConfiguration.password = null;
        try {
            client.createEnvironment(mockEnvironment);
        } catch (FeignException e) {
            //Should throw 400
            Assert.assertEquals(400, e.status());
            Assert.assertTrue(
                e.contentUTF8().contains("databaseConfiguration") && e.contentUTF8().contains("password")
            );
        } finally {
            //Remove the mock environment
            environmentService.deleteEnvironment(mockEnvironment.name);
        }
    }

    @Test
    public void createEnvironmentWithNullDatabaseConfigurationDatabase_shouldReturn400() {
        //Create a mock environment with a null databaseConfiguration database
        EnvironmentConfiguration mockEnvironment = MockEnvironments.validEnvironment();
        mockEnvironment.databaseConfiguration.database = null;
        try {
            client.createEnvironment(mockEnvironment);
        } catch (FeignException e) {
            //Should throw 400
            Assert.assertEquals(400, e.status());
            Assert.assertTrue(
                e.contentUTF8().contains("databaseConfiguration") && e.contentUTF8().contains("database")
            );
        } finally {
            //Remove the mock environment
            environmentService.deleteEnvironment(mockEnvironment.name);
        }
    }

    @Test
    public void createEnvironmentWithNullDatabaseConfigurationIntegrationTestDatabase_shouldReturn400() {
        //Create a mock environment with a null databaseConfiguration integrationTestDatabase
        EnvironmentConfiguration mockEnvironment = MockEnvironments.validEnvironment();
        mockEnvironment.databaseConfiguration.integrationTestDatabase = null;
        try {
            client.createEnvironment(mockEnvironment);
        } catch (FeignException e) {
            //Should throw 400
            Assert.assertEquals(400, e.status());
            Assert.assertTrue(
                e.contentUTF8().contains("databaseConfiguration") &&
                    e.contentUTF8().contains("integrationTestDatabase")
            );
        } finally {
            //Remove the mock environment
            environmentService.deleteEnvironment(mockEnvironment.name);
        }
    }
}

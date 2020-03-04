package com.beumuth.math.core.internal.application;


import com.beumuth.math.client.Clients;
import com.beumuth.math.client.internal.application.ApplicationClient;
import com.beumuth.math.client.internal.application.ApplicationConfiguration;
import com.beumuth.math.client.internal.application.ApplicationMode;
import com.beumuth.math.client.internal.environment.EnvironmentConfiguration;
import com.beumuth.math.core.internal.client.ClientConfigurations;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
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
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ApplicationTests {
    private static boolean initialized = false;
    private static int testsRan;
    private static int totalTests;
    private static ApplicationConfiguration startingConfiguration;
    private static ApplicationClient client;

    @Autowired
    private ApplicationService applicationService;

    @Before
    public void setupTests() {
        if(! initialized) {
            client = Clients.getClient(ApplicationClient.class, ClientConfigurations.LOCAL);
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
    public void getAndSetApplicationModeSuccessTests() {
        try {
            //Get the current application mode
            ApplicationMode current = client.getApplicationMode();

            //Set the current application mode to something different
            ApplicationMode next = ApplicationMode.LIVE;
            if (current == ApplicationMode.LIVE) {  //This shouldn't be the case
                next = ApplicationMode.TEST;
            }
            client.setApplicationMode(next);

            //Get the current application mode and ensure that it's correct
            Assert.assertEquals(next, client.getApplicationMode());

            //Set back to TEST mode in finally
        } catch(Exception e) {
            throw(e);
        } finally {
            //It's critical this gets called so that other tests don't get executed in LIVE mode
            applicationService.setApplicationMode(ApplicationMode.TEST);
        }
    }

    @Test
    public void getApplicationConfigurationTest() {
        //Just make sure it runs without exception
        client.getApplicationConfiguration();
    }

    @Test
    public void setApplicationConfigurationNullMode_shouldReturn400() {
        try {
            ApplicationConfiguration configuration = ApplicationConfiguration.copy(startingConfiguration);
            configuration.mode = null;
            client.setApplicationConiguration(configuration);
        } catch(FeignException e){
            Assert.assertEquals(400, e.status());
            Assert.assertTrue(
                "Response body [" + e.contentUTF8() + "] should contain the word 'mode'" ,
                e.contentUTF8().contains("mode")
            );
        }
    }

    @Test
    public void setApplicationConfigurationNullEnvironments_shouldReturn400() {
        try {
            ApplicationConfiguration configuration = ApplicationConfiguration.copy(startingConfiguration);
            configuration.environments = null;
            client.setApplicationConiguration(configuration);
        } catch(FeignException e){
            Assert.assertEquals(400, e.status());
            Assert.assertTrue(
                "Response body [" + e.contentUTF8() + "]  should contain the word 'environments'" ,
                e.contentUTF8().contains("environments")
            );
        }
    }

    @Test
    public void setApplicationConfigurationEmptyEnvironments_shouldReturn400() {
        try {
            ApplicationConfiguration configuration = ApplicationConfiguration.copy(startingConfiguration);
            configuration.environments = Sets.newHashSet();
            client.setApplicationConiguration(configuration);
        } catch(FeignException e){
            Assert.assertEquals(400, e.status());
            Assert.assertTrue(
                "Response body [" + e.contentUTF8() + "]  should contain the words 'environments' and 'empty''" ,
                e.contentUTF8().contains("environments") && e.contentUTF8().contains("empty")
            );
        }
    }

    @Test
    public void setApplicationConfigurationNoActiveEnvironment_shouldReturn400() {
        try {
            ApplicationConfiguration configuration = ApplicationConfiguration.copy(startingConfiguration);
            configuration.activeEnvironment = null;
            client.setApplicationConiguration(configuration);
        } catch(FeignException e){
            Assert.assertEquals(400, e.status());
            Assert.assertTrue(
                "Response body [" + e.contentUTF8() + "]  should contain the word 'activeEnvironment'" ,
                e.contentUTF8().contains("activeEnvironment")
            );
        }
    }

    @Test
    public void setApplicationConfigurationUnrecognizedActiveEnvironment_shouldReturn400() {
        try {
            ApplicationConfiguration configuration = ApplicationConfiguration.copy(startingConfiguration);
            configuration.activeEnvironment = "ThereShouldBeNoEnvironmentWithThisName";
            client.setApplicationConiguration(configuration);
        } catch(FeignException e){
            Assert.assertEquals(400, e.status());
            Assert.assertTrue(
                "Given response body [" + e.contentUTF8() + "] should contain the word 'activeEnvironment'" ,
                e.contentUTF8().contains("activeEnvironment")
            );
        }
    }

    @Test
    public void setApplicationConfigurationEnvironmentNullName_shouldReturn400() {
        try {
            ApplicationConfiguration configuration = ApplicationConfiguration.copy(startingConfiguration);
            List<EnvironmentConfiguration> environmentList = Lists.newArrayList(configuration.environments);
            environmentList.get(0).name = null;
            configuration.environments = Sets.newHashSet(environmentList);
            client.setApplicationConiguration(configuration);
        } catch(FeignException e){
            Assert.assertEquals(400, e.status());
            Assert.assertTrue(
                "Response body [" + e.contentUTF8() + "] should contain the words 'environment' and 'name'",
                e.contentUTF8().contains("environment") && e.contentUTF8().contains("name")
            );
        }
    }

    @Test
    public void setApplicationConfigurationEnvironmentNullBaseUrl_shouldReturn400() {
        try {
            ApplicationConfiguration configuration = ApplicationConfiguration.copy(startingConfiguration);
            List<EnvironmentConfiguration> environmentList = Lists.newArrayList(configuration.environments);
            environmentList.get(0).baseUrl = null;
            configuration.environments = Sets.newHashSet(environmentList);client.setApplicationConiguration(configuration);

        } catch(FeignException e){
            Assert.assertEquals(400, e.status());
            Assert.assertTrue(
                "Response body [" + e.contentUTF8() + "]  should contain the word 'baseUrl'" ,
                e.contentUTF8().contains("baseUrl")
            );
        }
    }

    @Test
    public void setApplicationConfigurationEnvironmentNullDatabaseConfiguration_shouldReturn400() {
        try {
            ApplicationConfiguration configuration = ApplicationConfiguration.copy(startingConfiguration);
            List<EnvironmentConfiguration> environmentList = Lists.newArrayList(configuration.environments);
            environmentList.get(0).databaseConfiguration = null;
            configuration.environments = Sets.newHashSet(environmentList);
            client.setApplicationConiguration(configuration);
        } catch(FeignException e){
            Assert.assertEquals(400, e.status());
            Assert.assertTrue(
                "Response body [" + e.contentUTF8() + "]  should contain the word 'databaseConfiguration'" ,
                e.contentUTF8().contains("databaseConfiguration")
            );
        }
    }

    @Test
    public void setApplicationConfigurationEnvironmentDatabaseConfigurationNullHost_shouldReturn400() {
        try {
            ApplicationConfiguration configuration = ApplicationConfiguration.copy(startingConfiguration);
            List<EnvironmentConfiguration> environmentList = Lists.newArrayList(configuration.environments);
            environmentList.get(0).databaseConfiguration.host = null;
            configuration.environments = Sets.newHashSet(environmentList);
            client.setApplicationConiguration(configuration);
        } catch(FeignException e){
            Assert.assertEquals(400, e.status());
            Assert.assertTrue(
                "Response body [" + e.contentUTF8() + "]  should contain the words 'databaseConfiguration' and 'host'" ,
                e.contentUTF8().contains("databaseConfiguration") && e.contentUTF8().contains("host")
            );
        }
    }

    @Test
    public void setApplicationConfigurationEnvironmentDatabaseConfigurationNullPort_shouldReturn400() {
        try {
            ApplicationConfiguration configuration = ApplicationConfiguration.copy(startingConfiguration);
            List<EnvironmentConfiguration> environmentList = Lists.newArrayList(configuration.environments);
            environmentList.get(0).databaseConfiguration.port = null;
            configuration.environments = Sets.newHashSet(environmentList);
            client.setApplicationConiguration(configuration);
        } catch(FeignException e){
            Assert.assertEquals(400, e.status());
            Assert.assertTrue(
                "Response body [" + e.contentUTF8() + "]  should contain the words 'databaseConfiguration' and 'port'" ,
                e.contentUTF8().contains("databaseConfiguration") && e.contentUTF8().contains("port")
            );
        }
    }

    @Test
    public void setApplicationConfigurationEnvironmentDatabaseConfigurationNullUsername_shouldReturn400() {
        try {
            ApplicationConfiguration configuration = ApplicationConfiguration.copy(startingConfiguration);
            List<EnvironmentConfiguration> environmentList = Lists.newArrayList(configuration.environments);
            environmentList.get(0).databaseConfiguration.username = null;
            configuration.environments = Sets.newHashSet(environmentList);
            client.setApplicationConiguration(configuration);
        } catch(FeignException e){
            Assert.assertEquals(400, e.status());
            Assert.assertTrue(
                "Response body [" + e.contentUTF8() + "]  should contain the words 'databaseConfiguration' and 'username'" ,
                e.contentUTF8().contains("databaseConfiguration") && e.contentUTF8().contains("username")
            );
        }
    }

    @Test
    public void setApplicationConfigurationEnvironmentDatabaseConfigurationNullPassword_shouldReturn400() {
        try {
            ApplicationConfiguration configuration = ApplicationConfiguration.copy(startingConfiguration);
            List<EnvironmentConfiguration> environmentList = Lists.newArrayList(configuration.environments);
            environmentList.get(0).databaseConfiguration.password = null;
            configuration.environments = Sets.newHashSet(environmentList);
            client.setApplicationConiguration(configuration);
        } catch(FeignException e){
            Assert.assertEquals(400, e.status());
            Assert.assertTrue(
                "Response body [" + e.contentUTF8() + "]  should contain the words 'databaseConfiguration' and 'password'" ,
                e.contentUTF8().contains("databaseConfiguration") && e.contentUTF8().contains("password")
            );
        }
    }

    @Test
    public void setApplicationConfigurationEnvironmentDatabaseConfigurationNullDatabase_shouldReturn400() {
        try {
            ApplicationConfiguration configuration = ApplicationConfiguration.copy(startingConfiguration);
            List<EnvironmentConfiguration> environmentList = Lists.newArrayList(configuration.environments);
            environmentList.get(0).databaseConfiguration.database = null;
            configuration.environments = Sets.newHashSet(environmentList);
            client.setApplicationConiguration(configuration);
        } catch(FeignException e){
            Assert.assertEquals(400, e.status());
            Assert.assertTrue(
                "Response body [" + e.contentUTF8() + "]  should contain the words 'databaseConfiguration' and 'database'" ,
                e.contentUTF8().contains("databaseConfiguration") && e.contentUTF8().contains("database")
            );
        }
    }

    @Test
    public void setApplicationConfigurationEnvironmentDatabaseConfigurationNullIntegrationTestDatabase_shouldReturn400() {
        try {
            ApplicationConfiguration configuration = ApplicationConfiguration.copy(startingConfiguration);
            List<EnvironmentConfiguration> environmentList = Lists.newArrayList(configuration.environments);
            environmentList.get(0).databaseConfiguration.integrationTestDatabase = null;
            configuration.environments = Sets.newHashSet(environmentList);
            client.setApplicationConiguration(configuration);
        } catch(FeignException e){
            Assert.assertEquals(400, e.status());
            Assert.assertTrue(
                "Response body [" + e.contentUTF8() + "]  should contain the words 'databaseConfiguration' and 'integrationTestDatabase'" ,
                e.contentUTF8().contains("databaseConfiguration") && e.contentUTF8().contains("integrationTestDatabase")
            );
        }
    }
}

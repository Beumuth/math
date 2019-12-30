package com.beumuth.math.core.internal.test;

import com.github.instantpudd.validator.ClientErrorException;
import com.github.instantpudd.validator.ClientErrorStatusCode;
import com.github.instantpudd.validator.Validator;
import org.junit.runner.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping(
    path="/api/tests"
)
public class TestController {
    @Autowired
    TestService testService;

    @RequestMapping(method=RequestMethod.PUT, path="/run/all")
    public RunTestsResponse runAllTests() {
        return testService.runAllTests();
    }

    @RequestMapping(method=RequestMethod.PUT, path="/run/inClass/{testClassName}")
    public RunTestsResponse runTestsInClass(@PathVariable(value="testClassName") String testClassName)
        throws ClientErrorException {

        //Ensure that the class is a part of the BalloonHQTestSuite, otherwise return 400
        Optional<Class> testClass = MathTestSuite.getTestClassByName(testClassName);
        Validator
            .returnStatus(ClientErrorStatusCode.BAD_REQUEST)
            .ifEmpty(testClass)
            .withErrorMessage("The class [" + testClassName + "] does not belong to the BalloonHQTestSuite")
            .execute();

        //Run the tests in the class
        return testService.runTestsInClass(testClass.get());
    }
}
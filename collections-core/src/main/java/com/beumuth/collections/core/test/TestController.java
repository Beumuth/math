package com.beumuth.collections.core.test;

import org.junit.runner.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(
    path="/api/tests"
)
public class TestController {
    @Autowired
    TestService testService;

    @RequestMapping(method=RequestMethod.PUT, path="/run/all")
    public Result runAllTests() {
        return testService.runAllTests();
    }
}
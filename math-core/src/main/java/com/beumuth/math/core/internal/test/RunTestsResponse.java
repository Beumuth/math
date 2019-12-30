package com.beumuth.math.core.internal.test;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RunTestsResponse {
    private int numTests;
    private int numIgnored;
    private int numFailed;
    private long runTime;
    private List<TestFailure> testFailures;

    public static RunTestsResponse fromResult(Result result) {
        RunTestsResponse runTestsResponse = new RunTestsResponse();
        runTestsResponse.setNumTests(result.getRunCount());
        runTestsResponse.setNumIgnored(result.getIgnoreCount());
        runTestsResponse.setNumFailed(result.getFailureCount());
        runTestsResponse.setRunTime(result.getRunTime());
        List<TestFailure> testFailures = Lists.newArrayList();
        for(Failure failure : result.getFailures()) {
            testFailures.add(new TestFailure(failure.getDescription().getMethodName(), failure.getMessage()));
        }
        runTestsResponse.setTestFailures(testFailures);
        return runTestsResponse;
    }
}

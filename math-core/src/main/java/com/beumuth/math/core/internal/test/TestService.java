package com.beumuth.math.core.internal.test;

import com.beumuth.math.client.internal.application.ApplicationMode;
import com.beumuth.math.core.internal.application.ApplicationService;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TestService {

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private JUnitCore jUnitCore;

    public Result runAllTests() {
        ApplicationMode startMode = applicationService.getApplicationMode();
        applicationService.setApplicationMode(ApplicationMode.TEST);
        Result result = jUnitCore.run(MathTestSuite.class);
        applicationService.setApplicationMode(startMode);
        return result;
    }
}

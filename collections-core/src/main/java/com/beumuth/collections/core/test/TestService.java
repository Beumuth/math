package com.beumuth.collections.core.test;

import com.beumuth.collections.client.application.ApplicationMode;
import com.beumuth.collections.core.application.ApplicationService;
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
        Result result = jUnitCore.run(CollectionsSuite.class);
        applicationService.setApplicationMode(startMode);
        return result;
    }
}

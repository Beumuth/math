package com.beumuth.math.core.internal.test;

import com.beumuth.math.core.internal.application.ApplicationTests;
import com.beumuth.math.core.internal.environment.EnvironmentTests;
import com.beumuth.math.core.settheory.object.ObjectTests;
import com.beumuth.math.core.settheory.set.SetTests;
import com.google.common.collect.Lists;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import java.util.List;
import java.util.Optional;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    ApplicationTests.class,
    EnvironmentTests.class,
    ObjectTests.class,
    SetTests.class
})
public class MathTestSuite {
    public static Optional<Class> getTestClassByName(String name) {
        List<Class> classesInSuite = Lists.newArrayList(
            MathTestSuite
                .class
                .getAnnotation(Suite.SuiteClasses.class)
                .value()
        );

        for(Class testClass : classesInSuite) {
            if(testClass.getSimpleName().equals(name)) {
                return Optional.of(testClass);
            }
        }

        return Optional.empty();
    }
}

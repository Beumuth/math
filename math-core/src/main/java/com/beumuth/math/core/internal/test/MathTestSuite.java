package com.beumuth.math.core.internal.test;

import com.beumuth.math.core.internal.application.ApplicationTests;
import com.beumuth.math.core.internal.environment.EnvironmentTests;
import com.beumuth.math.core.settheory.element.ElementTests;
import com.beumuth.math.core.settheory.set.SetTests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    ApplicationTests.class,
    EnvironmentTests.class,
    ElementTests.class,
    SetTests.class
})
public class MathTestSuite {
}

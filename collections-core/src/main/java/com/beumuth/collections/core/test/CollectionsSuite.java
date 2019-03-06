package com.beumuth.collections.core.test;

import com.beumuth.collections.core.application.ApplicationTests;
import com.beumuth.collections.core.element.ElementTests;
import com.beumuth.collections.core.environment.EnvironmentTests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    ApplicationTests.class,
    EnvironmentTests.class,
    ElementTests.class
})
public class CollectionsSuite {
}

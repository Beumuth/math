package com.beumuth.collections.core.test;

import com.beumuth.collections.core.application.ApplicationTests;
import com.beumuth.collections.core.element.ElementTests;
import com.beumuth.collections.core.environment.EnvironmentTests;
import com.beumuth.collections.core.set.SetTests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    ApplicationTests.class,
    EnvironmentTests.class,
    ElementTests.class,
    SetTests.class
})
public class CollectionsSuite {
}

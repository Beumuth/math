package com.beumuth.math.core.internal.test;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TestFailure {
    private String testClass;
    private String testMethod;
    private String message;
}

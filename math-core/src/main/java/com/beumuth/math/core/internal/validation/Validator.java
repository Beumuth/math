package com.beumuth.math.core.internal.validation;

public interface Validator<T> {
    ValidationResult validate(T instance);
}

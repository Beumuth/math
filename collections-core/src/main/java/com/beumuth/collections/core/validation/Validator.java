package com.beumuth.collections.core.validation;

public interface Validator<T> {
    ValidationResult validate(T instance);
}

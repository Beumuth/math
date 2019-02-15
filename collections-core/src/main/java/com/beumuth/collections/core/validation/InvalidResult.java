package com.beumuth.collections.core.validation;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class InvalidResult implements ValidationResult {
    @Getter
    private String reason;
}

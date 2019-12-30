package com.beumuth.math.core.internal.validation;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class InvalidResult implements ValidationResult {
    @Getter
    private String reason;
}

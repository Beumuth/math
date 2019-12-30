package com.beumuth.math;

import feign.Headers;

@Headers({
    "Accept: application/json",
    "Content-Type: application/json"
})
public interface MathClient {
}

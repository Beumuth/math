package com.beumuth.collections;

import feign.Headers;

@Headers({
    "Accept: application/json",
    "Content-Type: application/json"
})
public interface CollectionsClient {
}

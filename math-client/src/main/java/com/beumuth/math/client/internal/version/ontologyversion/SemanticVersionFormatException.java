package com.beumuth.math.client.internal.version.ontologyversion;

public class SemanticVersionFormatException extends Exception {
    public SemanticVersionFormatException(String semanticVersionString, String reason) {
        super("Could not parse String [" + semanticVersionString + "] to a SemanticVersion: " + reason);
    }
}

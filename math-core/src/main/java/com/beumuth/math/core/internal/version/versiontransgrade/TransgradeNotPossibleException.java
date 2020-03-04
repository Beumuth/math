package com.beumuth.math.core.internal.version.versiontransgrade;

import com.beumuth.math.client.internal.version.ontologyversion.SemanticVersion;

public class TransgradeNotPossibleException extends Exception {
    public TransgradeNotPossibleException(SemanticVersion from, SemanticVersion to) {
        super(
            "It is not possible to transgrade from the current version [" + from + "] to version [" + to + "]: " +
                "no path of transgrades exist."
        );
    }
}

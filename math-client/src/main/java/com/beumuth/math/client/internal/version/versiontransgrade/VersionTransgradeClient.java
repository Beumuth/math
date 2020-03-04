package com.beumuth.math.client.internal.version.versiontransgrade;

import com.beumuth.math.MathClient;
import feign.Param;
import feign.RequestLine;

public interface VersionTransgradeClient extends MathClient {
    @RequestLine("GET api/versionTransgrades/initialize")
    void initialize();

    @RequestLine(
        "GET api/versionTransgrades/versionTransgrade/from/{semanticVersionFrom}/to/{semanticVersionTo}/exists"
    )
    boolean doesVersionTransgradeExistWithFromAndTo(
        @Param("semanticVersionFrom") String fromVersion,
        @Param("semanticVersionTo") String toVersion
    );

    @RequestLine("GET api/versionTransgrades/from/{semanticVersionFrom}/to/{semanticVersionTo}/isPossible")
    boolean isTransgradePossible(
        @Param("semanticVersionFrom") String fromVersion,
        @Param("semanticVersionTo") String toVersion
    );

    @RequestLine("PUT api/versionTransgrades/to/mostRecentVersion")
    void upgradeToMostRecentVersion();

    @RequestLine("PUT api/versionTransgrades/to/{semanticVersion}")
    void transgrade(@Param("semanticVersion") String toVersion);
}

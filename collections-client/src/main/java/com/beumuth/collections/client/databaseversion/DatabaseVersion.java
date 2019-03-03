package com.beumuth.collections.client.databaseversion;

import lombok.*;
import org.joda.time.DateTime;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of="id")
public class DatabaseVersion {
    @Getter
    @Setter
    private long id;

    @Getter
    @Setter
    private int majorVersion;

    @Getter
    @Setter
    private int minorVersion;

    @Getter
    @Setter
    private int patchVersion;

    @Getter
    @Setter
    private DateTime datetimeCreated;

    @Getter
    @Setter
    private String description;
}
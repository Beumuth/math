package com.beumuth.math.client.internal.databaseversion;

import lombok.*;
import org.joda.time.DateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(of="id")
public class DatabaseVersion {
    private long id;
    private int majorVersion;
    private int minorVersion;
    private int patchVersion;
    private DateTime datetimeCreated;
    private String description;
}
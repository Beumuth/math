package com.beumuth.collections.core.databaseversion;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.joda.time.DateTime;

@AllArgsConstructor
@NoArgsConstructor
public class DatabaseVersion {
    public int id;
    public int majorVersion;
    public int minorVersion;
    public int patchVersion;
    public DateTime datetimeCreated;
    public String description;
}
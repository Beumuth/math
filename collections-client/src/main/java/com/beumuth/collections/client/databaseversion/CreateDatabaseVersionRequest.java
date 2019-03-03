package com.beumuth.collections.client.databaseversion;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class CreateDatabaseVersionRequest {
    public int majorVersion;
    public int minorVersion;
    public int patchVersion;
    public String description;
}

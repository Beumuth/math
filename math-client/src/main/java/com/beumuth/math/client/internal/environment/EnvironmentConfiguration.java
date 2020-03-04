package com.beumuth.math.client.internal.environment;

import com.beumuth.math.client.internal.application.ApplicationMode;
import com.beumuth.math.client.internal.database.DatabaseConfiguration;
import com.beumuth.math.client.internal.version.ontologyversion.SemanticVersion;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.Objects;

@AllArgsConstructor
@NoArgsConstructor
public class EnvironmentConfiguration {
    public String name;
    public Map<ApplicationMode, DatabaseConfiguration> databaseConfigurations;
    public Map<ApplicationMode, SemanticVersion> currentVersions;

    /**
     * Copy constructor
     */
    public EnvironmentConfiguration(EnvironmentConfiguration other) {
        this.name = other.name;
        this.databaseConfigurations = other.databaseConfigurations;
        this.currentVersions = other.currentVersions;
    }

    public static EnvironmentConfiguration copy(EnvironmentConfiguration other) {
        return new EnvironmentConfiguration(other);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EnvironmentConfiguration that = (EnvironmentConfiguration) o;
        return name.equalsIgnoreCase(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}

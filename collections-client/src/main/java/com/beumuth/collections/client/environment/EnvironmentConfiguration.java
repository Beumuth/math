package com.beumuth.collections.client.environment;

import com.beumuth.collections.client.database.DatabaseConfiguration;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Objects;

@AllArgsConstructor
@NoArgsConstructor
public class EnvironmentConfiguration {
    public String name;
    public String baseUrl;
    public DatabaseConfiguration databaseConfiguration;

    /**
     * Copy constructor
     */
    public EnvironmentConfiguration(EnvironmentConfiguration other) {
        this.name = other.name;
        this.baseUrl = other.baseUrl;
        this.databaseConfiguration = new DatabaseConfiguration(other.databaseConfiguration);
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

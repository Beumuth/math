package com.beumuth.math.core.internal.version.versiontransgrade;

import com.beumuth.math.client.internal.version.ontologyversion.SemanticVersion;
import com.beumuth.math.core.internal.metaontology.MetaontologyService;
import lombok.*;

import java.util.Comparator;
import java.util.function.Consumer;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(of={"from", "to"})
public class VersionTransgrade implements Comparable<VersionTransgrade> {
    private SemanticVersion from;
    private SemanticVersion to;
    private Consumer<MetaontologyService> script;

    @Override
    public int compareTo(VersionTransgrade other) {
        return Comparator
            .comparing(VersionTransgrade::getFrom)
            .thenComparing(VersionTransgrade::getTo)
            .compare(this, other);
    }
}

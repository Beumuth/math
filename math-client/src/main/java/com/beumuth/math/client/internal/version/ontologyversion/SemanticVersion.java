package com.beumuth.math.client.internal.version.ontologyversion;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@AllArgsConstructor
@Getter
@EqualsAndHashCode
public class SemanticVersion implements Comparable<SemanticVersion> {
    //This constant allows matching any major, minor, or patch version.
    public static final int ANY = -1;
    //This constant allows matching any prereleaseIdentifiers or buildMetadataIdentifiers.
    public static final List<String> ANY_IDENTIFIERS = null;

    private int major;
    private int minor;
    private int patch;
    private List<String> prereleaseIdentifiers;
    private List<String> buildMetadataIdentifiers;

    public SemanticVersion(int major, int minor, int patch) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
        prereleaseIdentifiers = Lists.newArrayList();
        buildMetadataIdentifiers = Lists.newArrayList();
    }

    @Override
    public int compareTo(SemanticVersion other) {
        return Comparator
            .comparing(SemanticVersion::getMajor)
            .thenComparing(SemanticVersion::getMinor)
            .thenComparing(SemanticVersion::getPatch)
            .thenComparing((o1, o2) ->
                compareIdentifiers(o1.getPrereleaseIdentifiers(), o2.getPrereleaseIdentifiers())
            ).thenComparing((o1, o2) ->
                compareIdentifiers(o1.getBuildMetadataIdentifiers(), o2.getBuildMetadataIdentifiers())
            ).compare(this, other);
    }

    private int compareIdentifiers(List<String> identifiers1, List<String> identifiers2) {
        if(identifiers1.size() > identifiers2.size()) {
            return 1;
        }
        if(identifiers2.size() > identifiers1.size()) {
            return -1;
        }
        for(int i = 0; i < Math.max(identifiers1.size(), identifiers2.size()); ++i) {
            String curIdentifier1 = identifiers1.get(i);
            String curIdentifier2 = identifiers1.get(i);

            //Both integers?
            if(StringUtils.isNumeric(curIdentifier1) && StringUtils.isNumeric(curIdentifier2)) {
                long curIdentifier1Numeric = Long.parseLong(curIdentifier1);
                long curIdentifier2Numeric = Long.parseLong(curIdentifier2);

                //The first > the second?
                if(curIdentifier1Numeric > curIdentifier2Numeric) {
                    return 1;
                }

                //The second > first?
                if(curIdentifier2Numeric > curIdentifier1Numeric) {
                    return -1;
                }

                //Equal.
                continue;
            }

            //First is integer?
            if(StringUtils.isNumeric(curIdentifier1)) {
                return 1;
            }

            //Second is integer?
            if(StringUtils.isNumeric(curIdentifier2)) {
                return -1;
            }

            //Both Strings.

            //Equal?
            if(curIdentifier1.equals(curIdentifier2)) {
                continue;
            }

            //Not equal.
            return curIdentifier1.compareTo(curIdentifier2);
        }

        //The identifier Lists are identical
        return 0;
    }

    @Override
    public String toString() {
        return major + "." + minor + "." + patch + (
            prereleaseIdentifiers.isEmpty() ?
                "" :
                "-" + String.join(".", prereleaseIdentifiers)
        ) + (
            buildMetadataIdentifiers.isEmpty() ?
                "" :
                "+" + String.join(".", buildMetadataIdentifiers)
        );
    }

    public static SemanticVersion fromString(String semanticVersionString) throws SemanticVersionFormatException {
        //Get the build metadata identifers if there are any (anything after the '+" character)
        String[] components = semanticVersionString.split("\\+");
        if(components.length > 2) {
            throw new SemanticVersionFormatException(
                semanticVersionString,
                "must not contain more than one '+' character"
            );
        }
        List<String> buildMetadataIdentifiers = components.length == 2 ?
            Lists.newArrayList(components[1].split("\\.")) :
            Collections.emptyList();

        //Get the prerelease identifers if there are any (anything after the '-" character)
        components = semanticVersionString.split("-");
        if(components.length > 2) {
            throw new SemanticVersionFormatException(
                semanticVersionString,
                "must not contain more than one '-' character"
            );
        }
        List<String> prereleaseIdentifiers = components.length == 2 ?
            Lists.newArrayList(components[1].split("\\.")) :
            Collections.emptyList();


        //Get major, minor, and patch versions
        components = semanticVersionString.split("\\.");
        if(components.length < 3) {
            throw new SemanticVersionFormatException(
                semanticVersionString,
                "must contain at least three dot-separated numbers represeting major, minor, and patch versions"
            );
        }

        try {
            int major = Integer.parseInt(components[0]);
            int minor = Integer.parseInt(components[1]);
            int patch = Integer.parseInt(components[2]);

            if(major < 0 || minor < 0 || patch < 0) {
                throw new SemanticVersionFormatException(
                    semanticVersionString,
                    "the major, minor, and patch versions must all be > 0"
                );
            }

            return new SemanticVersion(major, minor, patch, prereleaseIdentifiers, buildMetadataIdentifiers);
        } catch(NumberFormatException e) {
            throw new SemanticVersionFormatException(
                semanticVersionString,
                "The major, minor, and patch versions must be integers"
            );
        }
    }

    /**
     * This determines whether this SemanticVersion matches some other. That is, if this version or the other contains
     * an ANY major, minor, or patch version, or an ANY_IDENTIFIERS prereleaseIdentifier or buildMetadataIdentifiers.
     */
    public boolean matches(SemanticVersion other) {
        return
            (major == ANY || other.getMajor() == ANY || major == other.getMajor()) &&
            (minor == ANY || other.getMinor() == ANY || minor == other.getMinor()) &&
            (patch == ANY || other.getPatch() == ANY || patch == other.getPatch()) && (
                prereleaseIdentifiers == ANY_IDENTIFIERS ||
                other.getPrereleaseIdentifiers() == ANY_IDENTIFIERS ||
                prereleaseIdentifiers.equals(other.getPrereleaseIdentifiers())
            ) && (
                buildMetadataIdentifiers == ANY_IDENTIFIERS ||
                other.getBuildMetadataIdentifiers() == ANY_IDENTIFIERS ||
                buildMetadataIdentifiers.equals(other.getBuildMetadataIdentifiers())
            );
    }
}

package com.beumuth.math.core.internal.version.versiontransgrade;

import com.beumuth.math.client.internal.version.ontologyversion.SemanticVersion;
import com.beumuth.math.client.settheory.orderedset.OrderedSet;
import com.beumuth.math.client.settheory.orderedset.OrderedSets;
import com.beumuth.math.core.internal.metaontology.MetaontologyService;

import static com.beumuth.math.client.internal.version.ontologyversion.SemanticVersion.ANY;
import static com.beumuth.math.client.internal.version.ontologyversion.SemanticVersion.ANY_IDENTIFIERS;

public class VersionTransgrades {
    public static OrderedSet<VersionTransgrade> ALL = OrderedSets.with(
        new VersionTransgrade(
            new SemanticVersion(ANY, ANY, ANY, ANY_IDENTIFIERS, ANY_IDENTIFIERS),
            new SemanticVersion(0, 0, 0),
            VersionTransgrades::from_any_to_0_0_0
        ),
            new VersionTransgrade(
            new SemanticVersion(0, 0, 0),
            new SemanticVersion(0, 1, 0),
            VersionTransgrades::from_0_0_0_to_0_1_0
        )
    );

    private static void from_any_to_0_0_0(MetaontologyService metaontologyService) {
        metaontologyService
            .getDatabaseService()
            .dropDatabase();
    }

    private static void from_0_0_0_to_0_1_0(MetaontologyService metaontologyService) {
        metaontologyService
            .getDatabaseService()
            .createDatabase();

        //Create Element table
        metaontologyService
            .getDatabaseService()
            .getJdbcTemplate()
            .update(
                "CREATE TABLE IF NOT EXISTS `Element` ( " +
                    "`id` INT(11) NOT NULL AUTO_INCREMENT, " +
                    "`a` INT(11) NOT NULL, " +
                    "`b` INT(11) NOT NULL, " +
                    "PRIMARY KEY (`id`), " +
                    "INDEX `fk_jGraphElement_a_idx` (`a` ASC), " +
                    "INDEX `fk_jGraphElement_b_idx` (`b` ASC), " +
                    "CONSTRAINT `fk_element_a` " +
                    "FOREIGN KEY (`a`) " +
                        "REFERENCES `Element` (`id`) " +
                            "ON DELETE CASCADE " +
                            "ON UPDATE NO ACTION, " +
                    "CONSTRAINT `fk_element_b` " +
                    "FOREIGN KEY (`b`) " +
                        "REFERENCES `Element` (`id`) " +
                            "ON DELETE CASCADE " +
                            "ON UPDATE NO ACTION " +
                ") "
            );
    }
}

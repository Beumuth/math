/*
 * Drop all tables. Create 'Element', which was previous 'JGraphElement'.
 */
 DROP TABLE Element, OrderedPair, Sset, JGraphElement, Object;

CREATE TABLE IF NOT EXISTS `Element` (
    `id` INT(11) NOT NULL AUTO_INCREMENT,
    `a` INT(11) NOT NULL,
    `b` INT(11) NOT NULL,
    PRIMARY KEY (`id`),
    INDEX `fk_jGraphElement_a_idx` (`a` ASC),
    INDEX `fk_jGraphElement_b_idx` (`b` ASC),
    CONSTRAINT `fk_element_a`
        FOREIGN KEY (`a`)
             REFERENCES `Element` (`id`)
                 ON DELETE CASCADE
                 ON UPDATE NO ACTION,
    CONSTRAINT `fk_element_b`
        FOREIGN KEY (`b`)
            REFERENCES `Element` (`id`)
                ON DELETE CASCADE
                ON UPDATE NO ACTION
)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;

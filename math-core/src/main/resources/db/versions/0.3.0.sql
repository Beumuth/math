/*JGraphElement*/
CREATE TABLE IF NOT EXISTS JGraphElement (
  `id` INT(11) NOT NULL,
  `a` INT(11) NOT NULL,
  `b` INT(11) NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_jgraphElement_x_idx` (`a` ASC),
  INDEX `fk_jgraphElement_y_idx` (`b` ASC),
  CONSTRAINT `fk_jgraphElement_object`
    FOREIGN KEY (`id`)
    REFERENCES `Object` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_jgraphElement_x`
    FOREIGN KEY (`a`)
    REFERENCES `JGraphElement` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_jgraphElement_y`
    FOREIGN KEY (`b`)
    REFERENCES `JGraphElement` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;

/*This allows LAST_INSERT_ID() to work on the first query*/
INSERT INTO jgraphelement (a, b) values (1, 1);
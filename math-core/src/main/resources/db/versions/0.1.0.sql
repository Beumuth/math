CREATE TABLE IF NOT EXISTS `Element` (
	`id` INT(11) NOT NULL AUTO_INCREMENT,
	PRIMARY KEY (`id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;

CREATE TABLE IF NOT EXISTS `Sset` (
	`id` INT(11) NOT NULL AUTO_INCREMENT,
	`idElement` INT(11) NOT NULL,
	PRIMARY KEY (`id`),
	INDEX `fk_set_element_idx` (`idElement` ASC),
	CONSTRAINT `fk_set_element`
		FOREIGN KEY (`idElement`)
		REFERENCES `Element` (`id`)
		ON DELETE NO ACTION
		ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;

CREATE TABLE IF NOT EXISTS `SetElement` (
	`id` INT(11) NOT NULL AUTO_INCREMENT,
	`idSet` INT(11) NOT NULL,
	`idElement` INT(11) NOT NULL,
	PRIMARY KEY (`id`),
	INDEX `fk_setItem_set_idx` (`idSet` ASC),
	INDEX `fk_set_element_idx` (`idElement` ASC),
	UNIQUE INDEX `unique_setElement` (`idSet` ASC, `idElement` ASC),
	CONSTRAINT `fk_setElement_set`
		FOREIGN KEY (`idSet`)
		REFERENCES `Sset` (`id`)
		ON DELETE CASCADE
		ON UPDATE NO ACTION,
	CONSTRAINT `fk_setElement_element`
		FOREIGN KEY (`idElement`)
		REFERENCES `Element` (`id`)
		ON DELETE CASCADE
		ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;
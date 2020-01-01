/*Objects, Sets, Elements*/

CREATE TABLE IF NOT EXISTS `Object` (
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`id`));

CREATE TABLE IF NOT EXISTS `Sset` (
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `idObject` INT(11) NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_set_object_idx` (`idObject` ASC),
  UNIQUE INDEX `unique_set` (`idObject` ASC),
  CONSTRAINT `fk_set_object`
    FOREIGN KEY (`idObject`)
    REFERENCES `Object` (`id`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION);

CREATE TABLE IF NOT EXISTS `Element` (
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `idObject` INT(11) NOT NULL,
  `idSet` INT(11) NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_element_set_idx` (`idSet` ASC),
  INDEX `fk_element_object_idx` (`idObject` ASC),
  UNIQUE INDEX `unique_setElement` (`idSet` ASC, `idObject` ASC),
  CONSTRAINT `fk_element_set`
    FOREIGN KEY (`idSet`)
    REFERENCES `Sset` (`id`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_element_object`
    FOREIGN KEY (`idObject`)
    REFERENCES `Object` (`id`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION);
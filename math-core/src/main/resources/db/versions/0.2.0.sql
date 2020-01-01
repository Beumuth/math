/*OrderedPair*/

CREATE TABLE IF NOT EXISTS `OrderedPair` (
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `idObject` INT(11) NOT NULL,
  `idLeft` INT(11) NOT NULL,
  `idRight` INT(11) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `idObject_UNIQUE` (`idObject` ASC),
  CONSTRAINT `fk_orderedPair_object`
    FOREIGN KEY (`idObject`)
    REFERENCES `Object` (`id`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION);
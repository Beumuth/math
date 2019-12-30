/*
 * Update the foreign key from Set to Element so that it cascades on delete.
 * Add unique index on id_element
 */
ALTER TABLE `Sset` 
DROP FOREIGN KEY `fk_set_element`;

ALTER TABLE `Sset`  
ADD CONSTRAINT `fk_set_element` 
	FOREIGN KEY (`idElement`)
	REFERENCES `Element` (`id`)
	ON DELETE CASCADE
	ON UPDATE NO ACTION,
ADD UNIQUE INDEX unique_set (idElement ASC);
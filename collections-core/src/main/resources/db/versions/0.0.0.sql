CREATE TABLE `DatabaseVersion` (
	`id` INT(11) NOT NULL AUTO_INCREMENT PRIMARY KEY,
	`majorVersion` INT(11) NOT NULL,
	`minorVersion` INT(11) NOT NULL,
	`patchVersion` INT(11) NOT NULL,
	`datetimeCreated` DATETIME NOT NULL,
	`description` VARCHAR(2000) NOT NULL,
	INDEX `naturalKey_databaseVersion` (`majorVersion` ASC, `minorVersion` ASC, `patchVersion` ASC)
) ENGINE=InnoDB;

CREATE TABLE `DatabaseMetadata` (
	`idCurrentVersion` INT(11) NOT NULL,
	FOREIGN KEY `fk_databaseMetadata_dv`(`idCurrentVersion`)
		REFERENCES `DatabaseVersion` (`id`)
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
) ENGINE=InnoDB;

INSERT INTO DatabaseVersion(
	majorVersion,
	minorVersion,
	patchVersion,
	datetimeCreated,
	description
) VALUES (
	0,
	0,
	0,
	NOW(),
	"Initial version"
);

/*Set the current version to the initial version 0.0.0*/
INSERT INTO DatabaseMetadata(
	idCurrentVersion
) VALUES(
	(SELECT id FROM DatabaseVersion WHERE majorVersion=0 AND minorVersion=0 AND patchVersion=0)
);
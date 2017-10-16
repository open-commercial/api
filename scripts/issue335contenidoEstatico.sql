ALTER TABLE `sic`.`empresa` DROP COLUMN `logo`;
ALTER TABLE `sic`.`empresa` ADD COLUMN `logo` VARCHAR(255) NOT NULL AFTER `lema`;
DROP SCHEMA IF EXISTS `alarma`;
CREATE SCHEMA IF NOT EXISTS `alarma` DEFAULT CHARACTER SET utf8;
USE `alarma`;

DROP TABLE IF EXISTS `alarma`.`Dispara`;
DROP TABLE IF EXISTS `alarma`.`Usuario`;
DROP TABLE IF EXISTS `alarma`.`Evento`;
DROP TABLE IF EXISTS `alarma`.`Sensor`;
DROP TABLE IF EXISTS `alarma`.`Camara`;

CREATE TABLE IF NOT EXISTS `Usuario`(
  `Email`            VARCHAR(100) NOT NULL PRIMARY KEY,
  `Alias`            VARCHAR(20) NOT NULL UNIQUE,
  `Es_administrador` BOOLEAN DEFAULT FALSE,
  `Token`            VARCHAR(255),
  `Tag_RFID`         INT UNSIGNED,
  `Codigo`           VARCHAR(20)
);

CREATE TABLE IF NOT EXISTS `Evento`(
  `Id`               INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `Timestamp`        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `Descripcion`      VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS `Sensor`(
  `Id`               INT NOT NULL PRIMARY KEY,
  `Alias`            VARCHAR(20) NOT NULL UNIQUE,
  `Habilitado`       BOOLEAN DEFAULT FALSE,
  `Retardado`        BOOLEAN DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS `Camara`(
  `Alias`            VARCHAR(20) NOT NULL PRIMARY KEY,
  `Uri`              VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS `Dispara`(
  `Id_sensor_dispara`      INT NOT NULL,
  `Alias_camara_disparada` VARCHAR(20) NOT NULL,
  PRIMARY KEY (`Id_sensor_dispara`,`Alias_camara_disparada`)
);

ALTER TABLE `alarma`.`Dispara`
  ADD FOREIGN KEY (`Id_sensor_dispara`) REFERENCES `alarma`.`Sensor` (`Id`),
  ADD FOREIGN KEY (`Alias_camara_disparada`) REFERENCES `alarma`.`Camara` (`Alias`) ON UPDATE CASCADE;

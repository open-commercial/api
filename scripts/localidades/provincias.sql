INSERT INTO provincia (idProvincia, nombre) VALUES
    (14,'Córdoba'),
    (22,'Chaco'),
    (26,'Chubut'),
    (06,'Buenos Aires'),
    (10,'Catamarca'),
    (30,'Entre Ríos'),
    (34,'Formosa'),
    (42,'La Pampa'),
    (62,'Río Negro'),
    (70,'San Juan'),
    (78,'Santa Cruz'),
    (82,'Santa Fe'),
    (94,'Tierra del Fuego, Antártida e Islas del Atlántico Sur'),
    (38,'Jujuy'),
    (54,'Misiones'),
    (02,'Ciudad Autónoma de Buenos Aires'),
    (18,'Corrientes'),
    (46,'La Rioja'),
    (66,'Salta'),
    (86,'Santiago del Estero'),
    (50,'Mendoza'),
    (58,'Neuquén'),
    (74,'San Luis'),
    (90,'Tucumán');

--Default values para codigoPostal, costoEnvo y envioGratuito
ALTER TABLE localidad MODIFY COLUMN codigoPostal varchar(255) NOT NULL DEFAULT 0;
ALTER TABLE localidad MODIFY COLUMN costoEnvio DECIMAL(25,15) NOT NULL DEFAULT 0;
ALTER TABLE localidad MODIFY COLUMN envioGratuito BIT(1) NOT NULL DEFAULT 0;
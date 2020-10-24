ALTER TABLE sucursal
add column idConfiguracionSucursal bigint(20) not null;

UPDATE sucursal
set sucursal.idConfiguracionSucursal = 1
where sucursal.idSucursal = 1;

UPDATE sucursal
set sucursal.idConfiguracionSucursal = 2
where sucursal.idSucursal = 2;

UPDATE sucursal
set sucursal.idConfiguracionSucursal = 5
where sucursal.idSucursal = 5;

ALTER TABLE configuracionsucursal
DROP FOREIGN KEY `FKayhqfqt2o07rn0utsh6h057xe`;

ALTER TABLE configuracionsucursal
drop column idSucursal;

ALTER TABLE sucursal
ADD CONSTRAINT `Fkelflp79kg0kw83fiwybcwcvku` FOREIGN KEY (idConfiguracionSucursal)
REFERENCES configuracionsucursal(idConfiguracionSucursal);

alter table configuracionsucursal
add column comparteStock bit(1) default false;



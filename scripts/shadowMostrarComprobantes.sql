SET SQL_SAFE_UPDATES=0;

update factura 
set
factura.eliminada = factura.shadow
where date(factura.fecha) < "2018-10-23" ;
update nota
set
nota.eliminada = nota.shadow
where date(nota.fecha) < "2018-10-23";
update pedido
set
pedido.eliminado = pedido.shadow
where date(pedido.fecha) < "2018-10-23";

update pedido
set pedido.eliminado = pedido.shadow
where date(pedido.fecha) < "2018-10-01"; 

alter table factura drop column factura.shadow;
alter table nota drop column nota.shadow;
alter table pedido drop column pedido.shadow;

SET SQL_SAFE_UPDATES=1;

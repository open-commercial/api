
alter table formadepago add column paymentMethodId varchar(255) after predeterminado;
UPDATE formadepago SET 
nombre="Tarjeta Debito Mastercard" WHERE id_FormaDePago=57;
UPDATE formadepago SET
 nombre="Tarjeta Nativa" WHERE id_FormaDePago=54;
UPDATE formadepago SET
 nombre="Tarjeta Mastercard" WHERE id_FormaDePago=40;
 
Insert into formadepago(afectaCaja, eliminada, nombre, predeterminado, paymentMethodId) values (false, false, "Tarjeta Cordobesa", false, "cordobesa");
Insert into formadepago(afectaCaja, eliminada, nombre, predeterminado, paymentMethodId) values (false, false, "Tarjeta Debito Cabal", false, "debcabal");
Insert into formadepago(afectaCaja, eliminada, nombre, predeterminado, paymentMethodId) values (false, false, "Tarjeta Argencard", false, "argencard");
Insert into formadepago(afectaCaja, eliminada, nombre, predeterminado, paymentMethodId) values (false, false, "Tarjeta CMR", false, "cmr");
Insert into formadepago(afectaCaja, eliminada, nombre, predeterminado, paymentMethodId) values (false, false, "Tarjeta Amex", false, "amex");
Insert into formadepago(afectaCaja, eliminada, nombre, predeterminado, paymentMethodId) values (false, false, "Tarjeta Shoping", false, "tarshop");
Insert into formadepago(afectaCaja, eliminada, nombre, predeterminado, paymentMethodId) values (false, false, "Tarjeta Diners", false, "diners");
Insert into formadepago(afectaCaja, eliminada, nombre, predeterminado, paymentMethodId) values (false, false, "Tarjeta Cencosud", false, "cencosud");
Insert into formadepago(afectaCaja, eliminada, nombre, predeterminado, paymentMethodId) values (false, false, "Tarjeta Cordial", false, "cordial");
Insert into formadepago(afectaCaja, eliminada, nombre, predeterminado, paymentMethodId) values (false, false, "Pago Fácil", false, "pagofacil");
Insert into formadepago(afectaCaja, eliminada, nombre, predeterminado, paymentMethodId) values (false, false, "Rapipago", false, "rapipago");
Insert into formadepago(afectaCaja, eliminada, nombre, predeterminado, paymentMethodId) values (false, false, "CargaVirtual", false, "cargavirtual");
Insert into formadepago(afectaCaja, eliminada, nombre, predeterminado, paymentMethodId) values (false, false, "CobroExpress", false, "cobroexpress");
Insert into formadepago(afectaCaja, eliminada, nombre, predeterminado, paymentMethodId) values (false, false, "Redlink", false, "redlink");
Insert into formadepago(afectaCaja, eliminada, nombre, predeterminado, paymentMethodId) values (false, false, "BaproPagos", false, "bapropagos"); 


UPDATE formadepago SET paymentMethodId='naranja' WHERE id_FormaDePago='38';
UPDATE formadepago SET paymentMethodId='visa' WHERE id_FormaDePago='39';
UPDATE formadepago SET paymentMethodId='master' WHERE id_FormaDePago='40';
UPDATE formadepago SET paymentMethodId='maestro' WHERE id_FormaDePago='41';
UPDATE formadepago SET paymentMethodId='cabal' WHERE id_FormaDePago='43';
UPDATE formadepago SET paymentMethodId='debvisa' WHERE id_FormaDePago='45';
UPDATE formadepago SET paymentMethodId='nativa' WHERE id_FormaDePago='54';
UPDATE formadepago SET paymentMethodId='debmaster' WHERE id_FormaDePago='57';



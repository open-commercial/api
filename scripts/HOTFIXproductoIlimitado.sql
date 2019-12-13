 SET SQL_SAFE_UPDATES = 0;
update producto set ilimitado = 0 where ilimitado is true;
 SET SQL_SAFE_UPDATES = 1;
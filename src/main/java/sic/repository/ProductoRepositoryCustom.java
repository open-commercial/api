package sic.repository;

import sic.modelo.BusquedaProductoCriteria;

public interface ProductoRepositoryCustom {

    double calcularValorStock(BusquedaProductoCriteria criteria);

}

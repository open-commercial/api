package sic.repository;

import java.math.BigDecimal;
import sic.modelo.BusquedaProductoCriteria;

public interface ProductoRepositoryCustom {

    BigDecimal calcularValorStock(BusquedaProductoCriteria criteria);

}

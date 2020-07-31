package sic.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import sic.modelo.Factura;

public interface FacturaRepository<T extends Factura> extends PagingAndSortingRepository<T, Long> {}

package sic.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sic.modelo.Factura;

public interface FacturaRepository<T extends Factura> extends JpaRepository<T, Long> {}

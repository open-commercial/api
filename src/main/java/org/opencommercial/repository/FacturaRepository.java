package org.opencommercial.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.opencommercial.model.Factura;

public interface FacturaRepository<T extends Factura> extends JpaRepository<T, Long> {}

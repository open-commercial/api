package org.opencommercial.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.opencommercial.model.CuentaCorriente;

public interface CuentaCorrienteRepository<T extends CuentaCorriente> extends JpaRepository<T, Long> {

      @Query("SELECT c FROM CuentaCorriente c WHERE c.idCuentaCorriente = :idCuentaCorriente AND c.eliminada = false")
      CuentaCorriente findById(@Param("idCuentaCorriente") long idCuentaCorriente);
}

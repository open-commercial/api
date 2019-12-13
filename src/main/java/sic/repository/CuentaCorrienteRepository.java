package sic.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import sic.modelo.CuentaCorriente;

import java.math.BigDecimal;

public interface CuentaCorrienteRepository<T extends CuentaCorriente> extends PagingAndSortingRepository<T, Long> {

      @Query("SELECT c FROM CuentaCorriente c WHERE c.idCuentaCorriente = :idCuentaCorriente AND c.eliminada = false")
      CuentaCorriente findById(@Param("idCuentaCorriente") long idCuentaCorriente);

      @Query("SELECT c.saldo FROM CuentaCorriente c WHERE c.idCuentaCorriente = :idCuentaCorriente AND c.eliminada = false")
      BigDecimal getSaldoCuentaCorriente(@Param("idCuentaCorriente") long idCuentaCorriente);
}

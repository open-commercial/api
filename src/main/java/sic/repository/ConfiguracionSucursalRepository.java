package sic.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sic.modelo.ConfiguracionSucursal;

public interface ConfiguracionSucursalRepository extends JpaRepository<ConfiguracionSucursal, Long> {

  @Query("SELECT configuracionSucursal.cantidadMaximaDeRenglonesEnFactura "
          + "FROM Sucursal sucursal INNER JOIN sucursal.configuracionSucursal configuracionSucursal "
          + "WHERE sucursal.idSucursal = :idSucursal")
  int getCantidadMaximaDeRenglones(@Param("idSucursal") long idSucursal);

  @Query("SELECT configuracionSucursal.facturaElectronicaHabilitada "
          + "FROM Sucursal sucursal INNER JOIN sucursal.configuracionSucursal configuracionSucursal "
          + "WHERE sucursal.idSucursal = :idSucursal")
  boolean isFacturaElectronicaHabilitada(@Param("idSucursal") long idSucursal);

  @Modifying
  @Query("UPDATE ConfiguracionSucursal cs SET cs.predeterminada = false WHERE cs.predeterminada = true")
  void desmarcarSucursalPredeterminada();
}

package sic.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import sic.entity.ConfiguracionSucursal;

public interface ConfiguracionSucursalRepository
    extends PagingAndSortingRepository<ConfiguracionSucursal, Long> {

  @Query(
      "SELECT configuracionSucursal.cantidadMaximaDeRenglonesEnFactura "
          + "FROM Sucursal sucursal INNER JOIN sucursal.configuracionSucursal configuracionSucursal "
          + "WHERE sucursal.idSucursal = :idSucursal")
  int getCantidadMaximaDeRenglones(@Param("idSucursal") long idSucursal);

  @Query(
      "SELECT configuracionSucursal.facturaElectronicaHabilitada "
          + "FROM Sucursal sucursal INNER JOIN sucursal.configuracionSucursal configuracionSucursal "
          + "WHERE sucursal.idSucursal = :idSucursal")
  boolean isFacturaElectronicaHabilitada(@Param("idSucursal") long idSucursal);

  @Modifying
  @Query(
      "UPDATE ConfiguracionSucursal cs SET cs.predeterminada = false WHERE cs.predeterminada = true")
  void desmarcarSucursalPredeterminada();
}

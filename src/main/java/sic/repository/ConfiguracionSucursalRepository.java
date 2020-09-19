package sic.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import sic.modelo.ConfiguracionSucursal;
import sic.modelo.Sucursal;

public interface ConfiguracionSucursalRepository
    extends PagingAndSortingRepository<ConfiguracionSucursal, Long> {

//  ConfiguracionSucursal findBySucursal(Sucursal sucursal);

//  @Query(
//      "SELECT configuracionSucursal.cantidadMaximaDeRenglonesEnFactura "
//          + "FROM ConfiguracionSucursal configuracionSucursal WHERE configuracionSucursal.sucursal.idSucursal = :idSucursal")
//  int getCantidadMaximaDeRenglones(@Param("idSucursal") long idSucursal);

//  @Query(
//      "SELECT configuracionSucursal.facturaElectronicaHabilitada "
//          + "FROM ConfiguracionSucursal configuracionSucursal WHERE configuracionSucursal.sucursal.idSucursal = :idSucursal")
//  boolean isFacturaElectronicaHabilitada(@Param("idSucursal") long idSucursal);

  @Modifying
  @Query("UPDATE ConfiguracionSucursal cs SET cs.predeterminada = false WHERE cs.predeterminada = true")
  void desmarcarSucursalPredeterminada();
}

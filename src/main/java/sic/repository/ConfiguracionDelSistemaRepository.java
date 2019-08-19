package sic.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import sic.modelo.ConfiguracionDelSistema;
import sic.modelo.Sucursal;

public interface ConfiguracionDelSistemaRepository
    extends PagingAndSortingRepository<ConfiguracionDelSistema, Long> {

  ConfiguracionDelSistema findBySucursal(Sucursal sucursal);

  @Query(
      "SELECT cds.cantidadMaximaDeRenglonesEnFactura FROM ConfiguracionDelSistema cds WHERE cds.sucursal.idSucursal = :idSucursal")
  int getCantidadMaximaDeRenglones(@Param("idSucursal") long idSucursal);

  @Query(
      "SELECT cds.facturaElectronicaHabilitada FROM ConfiguracionDelSistema cds WHERE cds.sucursal.idSucursal = :idSucursal")
  boolean isFacturaElectronicaHabilitada(@Param("idSucursal") long idSucursal);
}

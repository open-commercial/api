package sic.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import sic.modelo.ConfiguracionDelSistema;
import sic.modelo.Empresa;

public interface ConfiguracionDelSistemaRepository extends PagingAndSortingRepository<ConfiguracionDelSistema, Long> {

      ConfiguracionDelSistema findByEmpresa(Empresa empresa);

      @Query("SELECT cds.cantidadMaximaDeRenglonesEnFactura FROM ConfiguracionDelSistema cds WHERE cds.empresa.id_Empresa = :idEmpresa")
      int getCantidadMaximaDeRenglones(@Param("idEmpresa") long idEmpresa);
    
}

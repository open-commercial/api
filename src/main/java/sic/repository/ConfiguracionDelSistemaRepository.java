package sic.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import sic.modelo.ConfiguracionDelSistema;
import sic.modelo.Empresa;

public interface ConfiguracionDelSistemaRepository extends PagingAndSortingRepository<ConfiguracionDelSistema, Long> {

      ConfiguracionDelSistema findByEmpresa(Empresa empresa);
    
}

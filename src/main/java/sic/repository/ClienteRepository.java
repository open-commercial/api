package sic.repository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import sic.modelo.Cliente;
import sic.modelo.Empresa;

public interface ClienteRepository extends PagingAndSortingRepository<Cliente, Long> , QueryDslPredicateExecutor<Cliente> {
    
      @Query("SELECT c FROM Cliente c WHERE c.id_Cliente = :idCliente AND c.eliminado = false")
      Cliente findById(@Param("idCliente") long idCliente);

      Cliente findByIdFiscalAndEmpresaAndEliminado(String idFiscal, Empresa empresa, boolean eliminado);

      Cliente findByRazonSocialAndEmpresaAndEliminado(String razonSocial, Empresa empresa, boolean eliminado);

      Cliente findByAndEmpresaAndPredeterminadoAndEliminado(Empresa empresa, boolean predeterminado, boolean eliminado);

      List<Cliente> findAllByAndEmpresaAndEliminadoOrderByRazonSocialAsc(Empresa empresa, boolean eliminado);

      List<Cliente> findByRazonSocialContainingIgnoreCaseAndNombreFantasiaContainingIgnoreCaseAndIdFiscalContainingIgnoreCaseAndEmpresaAndEliminado
                    (String razonSocial, String nombreFantasia, String idFiscal, Empresa empresa, boolean eliminado);
    
}

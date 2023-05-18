package sic.repository;

import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import sic.modelo.EnvioDeCorreoGrupal;

public interface EnvioDeCorreoGrupalRepository extends PagingAndSortingRepository<EnvioDeCorreoGrupal, Long>, QuerydslPredicateExecutor<EnvioDeCorreoGrupal> {

}

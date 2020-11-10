package sic.repository;

import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import sic.modelo.TokenAccesoExcluido;

public interface TokenAccesoExcluidoRepository
    extends PagingAndSortingRepository<TokenAccesoExcluido, Long>,
        QuerydslPredicateExecutor<TokenAccesoExcluido> {

  TokenAccesoExcluido findByToken(String token);
}

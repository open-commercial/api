package sic.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import sic.modelo.TokenAccesoExcluido;

public interface TokenAccesoExcluidoRepository extends
        JpaRepository<TokenAccesoExcluido, Long>,
        QuerydslPredicateExecutor<TokenAccesoExcluido> {

  TokenAccesoExcluido findByToken(String token);
}

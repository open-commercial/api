package org.opencommercial.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.opencommercial.model.TokenAccesoExcluido;

public interface TokenAccesoExcluidoRepository extends
        JpaRepository<TokenAccesoExcluido, Long>,
        QuerydslPredicateExecutor<TokenAccesoExcluido> {

  TokenAccesoExcluido findByToken(String token);
}

package org.opencommercial.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.opencommercial.model.Nota;

public interface NotaRepository<T extends Nota> extends JpaRepository<T, Long> {}

package sic.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sic.modelo.Nota;

public interface NotaRepository<T extends Nota> extends JpaRepository<T, Long> {}

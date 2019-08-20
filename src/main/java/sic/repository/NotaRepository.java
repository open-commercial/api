package sic.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import sic.modelo.Nota;

public interface NotaRepository<T extends Nota>
    extends PagingAndSortingRepository<T, Long> {}

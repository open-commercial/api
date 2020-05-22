package sic.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import sic.modelo.RenglonTraspaso;

public interface RenglonTraspasoRepository
    extends PagingAndSortingRepository<RenglonTraspaso, Long> {}

package sic.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import sic.modelo.RenglonFactura;

public interface RenglonFacturaRepository extends PagingAndSortingRepository<RenglonFactura, Long> {
    
}

package sic.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sic.modelo.RenglonFactura;

public interface RenglonFacturaRepository extends JpaRepository<RenglonFactura, Long> {
    
}

package org.opencommercial.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.opencommercial.model.RenglonRemito;

import java.util.List;

public interface RenglonRemitoRepository extends PagingAndSortingRepository<RenglonRemito, Long> {

  @Query("SELECT rr FROM Remito r INNER JOIN r.renglones rr"
          + " WHERE r.idRemito = :idRemito order by rr.idRenglonRemito asc")
  List<RenglonRemito> findByIdRemitoOrderByIdRenglonRemito(@Param("idRemito") long idRemito);
}

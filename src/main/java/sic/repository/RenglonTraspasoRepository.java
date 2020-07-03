package sic.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import sic.modelo.RenglonTraspaso;

import java.util.List;

public interface RenglonTraspasoRepository
    extends PagingAndSortingRepository<RenglonTraspaso, Long> {

  @Query(
      "SELECT rt FROM Traspaso t INNER JOIN t.renglones rt"
          + " WHERE t.idTraspaso = :idTraspaso order by rt.idRenglonTraspaso asc")
  List<RenglonTraspaso> findByIdTraspasoOrderByIdRenglonTraspaso(
      @Param("idTraspaso") long idTraspaso);
}

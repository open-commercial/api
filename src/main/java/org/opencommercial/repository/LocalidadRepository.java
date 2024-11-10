package org.opencommercial.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.opencommercial.model.Localidad;
import org.opencommercial.model.Provincia;

public interface LocalidadRepository extends
        JpaRepository<Localidad, Long>,
        QuerydslPredicateExecutor<Localidad> {

  @Query("SELECT l FROM Localidad l WHERE l.idLocalidad = :idLocalidad")
  Localidad findById(@Param("idLocalidad") long idLocalidad);

  Localidad findByNombreAndProvinciaOrderByNombreAsc(String nombre, Provincia provincia);

  Localidad findByCodigoPostal(String codigoPostal);

  List<Localidad> findAllByAndProvinciaOrderByNombreAsc(Provincia provincia);
}

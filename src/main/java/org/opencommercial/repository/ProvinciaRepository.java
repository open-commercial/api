package org.opencommercial.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.opencommercial.model.Provincia;

public interface ProvinciaRepository extends JpaRepository<Provincia, Long> {
    
      @Query("SELECT p FROM Provincia p WHERE p.idProvincia = :idProvincia")
      Provincia findById(@Param("idProvincia") long idProvincia);
    
      Provincia findByNombreOrderByNombreAsc(String nombre);
      
      List<Provincia> findAllByOrderByNombreAsc();
}

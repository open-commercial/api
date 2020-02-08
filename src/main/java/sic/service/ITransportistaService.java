package sic.service;

import java.util.List;
import sic.modelo.criteria.BusquedaTransportistaCriteria;
import sic.modelo.Sucursal;
import org.springframework.data.domain.Page;
import sic.modelo.Transportista;

import javax.validation.Valid;

public interface ITransportistaService {

    Transportista getTransportistaNoEliminadoPorId(long idTransportista);
            
    void actualizar(@Valid Transportista transportista);

    Page<Transportista> buscarTransportistas(BusquedaTransportistaCriteria criteria);

    void eliminar(long idTransportista);

    Transportista getTransportistaPorNombre(String nombre);

    List<Transportista> getTransportistas();

    Transportista guardar(@Valid Transportista transportista);
    
}

package sic.service;

import java.util.List;
import sic.modelo.BusquedaTransportistaCriteria;
import sic.modelo.Sucursal;
import sic.modelo.Transportista;

import javax.validation.Valid;

public interface ITransportistaService {

    Transportista getTransportistaNoEliminadoPorId(long id_Transportista);
            
    void actualizar(@Valid Transportista transportista);

    List<Transportista> buscarTransportistas(BusquedaTransportistaCriteria criteria);

    void eliminar(long idTransportista);

    Transportista getTransportistaPorNombre(String nombre, Sucursal sucursal);

    List<Transportista> getTransportistas(Sucursal sucursal);

    Transportista guardar(@Valid Transportista transportista);
    
}

package sic.service.impl;

import java.util.List;
import java.util.ResourceBundle;
import javax.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sic.modelo.Localidad;
import sic.modelo.Provincia;
import sic.service.ILocalidadService;
import sic.service.BusinessServiceException;
import sic.modelo.TipoDeOperacion;
import sic.util.Validator;
import sic.repository.LocalidadRepository;

@Service
public class LocalidadServiceImpl implements ILocalidadService {

    private final LocalidadRepository localidadRepository;
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public LocalidadServiceImpl(LocalidadRepository localidadRepository) {
        this.localidadRepository = localidadRepository;
    }

    @Override
    public Localidad getLocalidadPorId(Long idLocalidad) {
        Localidad localidad = localidadRepository.findOne(idLocalidad);
        if (localidad == null) {
            throw new EntityNotFoundException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_localidad_no_existente"));
        }
        return localidad;
    }
    
    @Override
    public List<Localidad> getLocalidadesDeLaProvincia(Provincia provincia) {
        return localidadRepository.findAllByAndProvinciaAndEliminadaOrderByNombreAsc(provincia, false);
    }

    @Override
    @Transactional
    public void eliminar(Long  idLocalidad) {
        Localidad localidad = this.getLocalidadPorId(idLocalidad);
        localidad.setEliminada(true);
        localidadRepository.save(localidad);
    }

    @Override
    public Localidad getLocalidadPorNombre(String nombre, Provincia provincia) {
        return localidadRepository.findByNombreAndProvinciaAndEliminadaOrderByNombreAsc(nombre, provincia, false);
    }

    @Override
    public void validarOperacion(TipoDeOperacion operacion, Localidad localidad) {
        //Requeridos
        if (Validator.esVacio(localidad.getNombre())) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_localidad_vacio_nombre"));
        }
        if (localidad.getProvincia() == null) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_localidad_provincia_vacio"));
        }
        //Duplicados
        //Nombre
        Localidad localidadDuplicada = this.getLocalidadPorNombre(localidad.getNombre(), localidad.getProvincia());
        if (operacion.equals(TipoDeOperacion.ALTA) && localidadDuplicada != null) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_localidad_duplicado_nombre"));
        }
        if (operacion.equals(TipoDeOperacion.ACTUALIZACION)) {
            if (localidadDuplicada != null && localidadDuplicada.getId_Localidad() != localidad.getId_Localidad()) {
                throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                        .getString("mensaje_localidad_duplicado_nombre"));
            }
        }
    }

    @Override
    @Transactional
    public void actualizar(Localidad localidad) {
        this.validarOperacion(TipoDeOperacion.ACTUALIZACION, localidad);
        localidadRepository.save(localidad);
    }

    @Override
    @Transactional
    public Localidad guardar(Localidad localidad) {
        this.validarOperacion(TipoDeOperacion.ALTA, localidad);
        localidad = localidadRepository.save(localidad);
        LOGGER.warn("La Localidad " + localidad + " se guardó correctamente." );
        return localidad;
    }
}

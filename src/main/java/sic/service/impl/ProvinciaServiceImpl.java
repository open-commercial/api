package sic.service.impl;

import java.util.List;
import java.util.ResourceBundle;
import javax.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sic.modelo.Provincia;
import sic.service.IProvinciaService;
import sic.service.BusinessServiceException;
import sic.modelo.TipoDeOperacion;
import sic.util.Validator;
import sic.repository.ProvinciaRepository;

@Service
public class ProvinciaServiceImpl implements IProvinciaService {

    private final ProvinciaRepository provinciaRepository;
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public ProvinciaServiceImpl(ProvinciaRepository provinciaRepository) {
        this.provinciaRepository = provinciaRepository;
    }
    
    @Override
    public Provincia getProvinciaPorId(Long idProvincia) {
        Provincia provincia = provinciaRepository.findOne(idProvincia);
        if (provincia == null) {
            throw new EntityNotFoundException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_provincia_no_existente"));
        }
        return provincia;
    }

    @Override
    public Provincia getProvinciaPorNombre(String nombre) {
        return provinciaRepository.findByNombreAndEliminadaOrderByNombreAsc(nombre, false);
    }

    private void validarOperacion(TipoDeOperacion operacion, Provincia provincia) {
        //Requeridos
        if (Validator.esVacio(provincia.getNombre())) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_provincia_vacio_nombre"));
        }
        //Duplicados
        //Nombre
        Provincia provinciaDuplicada = this.getProvinciaPorNombre(provincia.getNombre());
        if (operacion.equals(TipoDeOperacion.ALTA) && provinciaDuplicada != null) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_provincia_duplicado_nombre"));
        }
        if (operacion.equals(TipoDeOperacion.ACTUALIZACION)) {
            if (provinciaDuplicada != null && provinciaDuplicada.getId_Provincia() != provincia.getId_Provincia()) {
                throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                        .getString("mensaje_provincia_duplicado_nombre"));
            }
        }
    }

    @Override
    @Transactional
    public void eliminar(long idProvincia) {
        Provincia provincia = this.getProvinciaPorId(idProvincia);
        if (provincia == null) {
            throw new EntityNotFoundException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_provincia_no_existente"));
        }
        provincia.setEliminada(true);
        provinciaRepository.save(provincia);
    }

    @Override
    @Transactional
    public void actualizar(Provincia provincia) {
        this.validarOperacion(TipoDeOperacion.ACTUALIZACION, provincia);
        provinciaRepository.save(provincia);
    }

    @Override
    @Transactional
    public Provincia guardar(Provincia provincia) {
        this.validarOperacion(TipoDeOperacion.ALTA, provincia);
        provincia = provinciaRepository.save(provincia);
        LOGGER.warn("La Provincia {} se guardó correctamente.", provincia);
        return provincia;
    }

    @Override
    public List<Provincia> getProvincias() {
        return provinciaRepository.findAllByAndEliminadaOrderByNombreAsc(false);
    }
}

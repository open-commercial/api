package sic.service.impl;

import java.util.List;
import java.util.ResourceBundle;
import javax.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sic.modelo.Pais;
import sic.service.IPaisService;
import sic.service.BusinessServiceException;
import sic.modelo.TipoDeOperacion;
import sic.util.Validator;
import sic.repository.PaisRepository;

@Service
public class PaisServiceImpl implements IPaisService {

    private final PaisRepository paisRepository;
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public PaisServiceImpl(PaisRepository paisRepository) {
        this.paisRepository = paisRepository;
    }
    
    @Override
    public Pais getPaisPorId(Long idPais) {
        Pais pais = paisRepository.findOne(idPais);
        if (pais == null) {
            throw new EntityNotFoundException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaja_pais_no_existente"));
        }
        return pais;
    }

    @Override
    public List<Pais> getPaises() {
        return paisRepository.findAllByAndEliminadoOrderByNombreAsc(false);
    }

    @Override
    public Pais getPaisPorNombre(String nombre) {
        return paisRepository.findByNombreIsAndEliminadoOrderByNombreAsc(nombre, false);
    }

    private void validarOperacion(TipoDeOperacion operacion, Pais pais) {
        //Obligatorios
        if (Validator.esVacio(pais.getNombre())) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_pais_vacio_nombre"));
        }
        //Duplicados
        //Nombre
        Pais paisDuplicado = this.getPaisPorNombre(pais.getNombre());
        if (operacion.equals(TipoDeOperacion.ALTA) && paisDuplicado != null) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_pais_duplicado_nombre"));
        }
        if (operacion.equals(TipoDeOperacion.ACTUALIZACION)) {
            if (paisDuplicado != null && paisDuplicado.getId_Pais() != pais.getId_Pais()) {
                throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                        .getString("mensaje_pais_duplicado_nombre"));
            }
        }
    }

    @Override
    @Transactional
    public void eliminar(Long idPais) {
        Pais pais = this.getPaisPorId(idPais);
        if (pais == null) {
            throw new EntityNotFoundException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaja_pais_no_existente"));
        }
        pais.setEliminado(true);
        paisRepository.save(pais);
    }

    @Override
    @Transactional
    public void actualizar(Pais pais) {
        this.validarOperacion(TipoDeOperacion.ACTUALIZACION, pais);
        paisRepository.save(pais);
    }

    @Override
    @Transactional
    public Pais guardar(Pais pais) {
        this.validarOperacion(TipoDeOperacion.ALTA, pais);
        pais = paisRepository.save(pais);
        LOGGER.warn("El Pais " + pais + " se guardó correctamente.");
        return pais;
    }
}

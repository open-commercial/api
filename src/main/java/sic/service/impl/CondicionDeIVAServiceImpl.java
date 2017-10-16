package sic.service.impl;

import java.util.List;
import java.util.ResourceBundle;
import javax.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sic.modelo.CondicionIVA;
import sic.service.ICondicionIVAService;
import sic.service.BusinessServiceException;
import sic.modelo.TipoDeOperacion;
import sic.util.Validator;
import sic.repository.CondicionIVARepository;

@Service
public class CondicionDeIVAServiceImpl implements ICondicionIVAService {

    private final CondicionIVARepository condicionIVARepository;    
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public CondicionDeIVAServiceImpl(CondicionIVARepository condicionIVARepository) {        
        this.condicionIVARepository = condicionIVARepository;        
    }
    
    @Override
    public CondicionIVA getCondicionIVAPorId(long idCondicionIVA) { 
        CondicionIVA condicionIVA = condicionIVARepository.findById(idCondicionIVA);
        if (condicionIVA == null) {
            throw new EntityNotFoundException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_CondicionIVA_no_existente"));
        }
        return condicionIVA;
    }

    @Override
    public List<CondicionIVA> getCondicionesIVA() {        
        return condicionIVARepository.findAllByAndEliminadaOrderByNombreAsc(false);        
    }

    @Override
    @Transactional
    public void actualizar(CondicionIVA condicionIVA) {
        this.validarOperacion(TipoDeOperacion.ACTUALIZACION, condicionIVA);        
        condicionIVARepository.save(condicionIVA);       
    }

    @Override
    @Transactional
    public CondicionIVA guardar(CondicionIVA condicionIVA) {
        this.validarOperacion(TipoDeOperacion.ALTA, condicionIVA);        
        condicionIVA = condicionIVARepository.save(condicionIVA);       
        LOGGER.warn("La Condicion IVA " + condicionIVA + " se guardó correctamente." );
        return condicionIVA;
    }

    @Override
    @Transactional
    public void eliminar(Long idCondicionIVA) {
        CondicionIVA condicionIVA = this.getCondicionIVAPorId(idCondicionIVA);
        if (condicionIVA == null) {
            throw new EntityNotFoundException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_CondicionIVA_no_existente"));
        }
        condicionIVA.setEliminada(true);        
        condicionIVARepository.save(condicionIVA);
    }

    @Override
    public CondicionIVA getCondicionIVAPorNombre(String nombre) {        
        return condicionIVARepository.findByNombreIsAndEliminada(nombre, false);        
    }

    @Override
    public void validarOperacion(TipoDeOperacion operacion, CondicionIVA condicionIVA) {
        //Requeridos
        if (Validator.esVacio(condicionIVA.getNombre())) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_condicionIVA_nombre_requerido"));
        }
        //Duplicados
        CondicionIVA condicionIVADuplicada = this.getCondicionIVAPorNombre(condicionIVA.getNombre());
        if (operacion.equals(TipoDeOperacion.ALTA) && condicionIVADuplicada != null) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_condicionIVA_nombre_duplicado"));
        }
        if (operacion.equals(TipoDeOperacion.ACTUALIZACION)) {
            if (condicionIVADuplicada != null && condicionIVADuplicada.getId_CondicionIVA() != condicionIVA.getId_CondicionIVA()) {
                throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                        .getString("mensaje_condicionIVA_nombre_duplicado"));
            }
        }
    }
}

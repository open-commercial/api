package sic.service.impl;

import java.util.List;
import java.util.ResourceBundle;
import javax.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sic.modelo.Empresa;
import sic.modelo.FormaDePago;
import sic.service.IFormaDePagoService;
import sic.service.BusinessServiceException;
import sic.util.Validator;
import sic.repository.FormaDePagoRepository;

@Service
public class FormaDePagoServiceImpl implements IFormaDePagoService {

    private final FormaDePagoRepository formaDePagoRepository;
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public FormaDePagoServiceImpl(FormaDePagoRepository formaDePagoRepository) {
        this.formaDePagoRepository = formaDePagoRepository;
    }

    @Override
    public List<FormaDePago> getFormasDePago(Empresa empresa) {
        return formaDePagoRepository.findAllByAndEmpresaAndEliminadaOrderByNombreAsc(empresa, false);
    }

    @Override
    public FormaDePago getFormasDePagoPorId(long idFormaDePago) {
        FormaDePago formaDePago = formaDePagoRepository.findOne(idFormaDePago);
        if (formaDePago == null) {
            throw new EntityNotFoundException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_formaDePago_no_existente"));
        }
        return formaDePago;
    }

    @Override
    public FormaDePago getFormaDePagoPredeterminada(Empresa empresa) {
        FormaDePago formaDePago = formaDePagoRepository.findByAndEmpresaAndPredeterminadoAndEliminada(empresa, true, false);
        if (formaDePago == null) {
            throw new EntityNotFoundException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_formaDePago_sin_predeterminada"));
        }
        return formaDePago;
    }

    @Override
    @Transactional
    public void setFormaDePagoPredeterminada(FormaDePago formaDePago) {
        //antes de setear como predeterminado, busca si ya existe
        //otro como predeterminado y cambia su estado.
        FormaDePago formaPredeterminadaAnterior = formaDePagoRepository.findByAndEmpresaAndPredeterminadoAndEliminada(formaDePago.getEmpresa(), true, false);
        if (formaPredeterminadaAnterior != null) {
            formaPredeterminadaAnterior.setPredeterminado(false);
            formaDePagoRepository.save(formaPredeterminadaAnterior);
        }
        formaDePago.setPredeterminado(true);
        formaDePagoRepository.save(formaDePago);
    }

    private void validarOperacion(FormaDePago formaDePago) {
        //Requeridos
        if (Validator.esVacio(formaDePago.getNombre())) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_formaDePago_vacio_nombre"));
        }
        if (formaDePago.getEmpresa() == null) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_formaDePago_empresa_vacio"));
        }
        //Duplicados
        //Nombre
        if (formaDePagoRepository.findByNombreAndEmpresaAndEliminada(formaDePago.getNombre(), formaDePago.getEmpresa(), false) != null) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_formaDePago_duplicado_nombre"));
        }
        //Predeterminado
        if (formaDePago.isPredeterminado() && (formaDePagoRepository.findByAndEmpresaAndPredeterminadoAndEliminada(formaDePago.getEmpresa(), true, false) != null)) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_formaDePago_predeterminada_existente"));
        }
    }

    @Override
    @Transactional
    public FormaDePago guardar(FormaDePago formaDePago) {
        this.validarOperacion(formaDePago);
        formaDePago = formaDePagoRepository.save(formaDePago);
        LOGGER.warn("La Forma de Pago " + formaDePago + " se guardó correctamente." );
        return formaDePago;
    }

    @Override
    @Transactional
    public void eliminar(long idFormaDePago) {
        FormaDePago formaDePago = this.getFormasDePagoPorId(idFormaDePago);
        if (formaDePago == null) {
            throw new EntityNotFoundException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_formaDePago_no_existente"));
        }
        formaDePago.setEliminada(true);
        formaDePagoRepository.save(formaDePago);
    }
    
}

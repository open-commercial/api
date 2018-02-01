package sic.service.impl;

import java.math.BigDecimal;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import sic.modelo.AjusteCuentaCorriente;
import sic.modelo.Cliente;
import sic.modelo.Empresa;
import sic.modelo.Nota;
import sic.repository.AjusteCuentaCorrienteRepository;
import sic.service.IAjusteCuentaCorrienteService;
import sic.service.IEmpresaService;

@Service
public class AjusteCuentaCorrienteServiceImpl implements IAjusteCuentaCorrienteService {
    
    private final AjusteCuentaCorrienteRepository ajusteCuentaCorrienteRepository;
    private final IEmpresaService empresaService;
    
    @Autowired
    public AjusteCuentaCorrienteServiceImpl(AjusteCuentaCorrienteRepository ajusteCuentaCorrienteRepository, IEmpresaService empresaService) {
        this.ajusteCuentaCorrienteRepository = ajusteCuentaCorrienteRepository;
        this.empresaService = empresaService;
    } 

    @Override
    public AjusteCuentaCorriente getById(long idAjusteCuentaCorriente) {
        return ajusteCuentaCorrienteRepository.findById(idAjusteCuentaCorriente);
    }

    @Override
    public AjusteCuentaCorriente getByNroAjuste(long idAjusteCuentaCorriente) {
        return ajusteCuentaCorrienteRepository.findByNumAjusteAndEliminado(idAjusteCuentaCorriente, false);
    }

    @Override
    public Page<AjusteCuentaCorriente> getAllByFechaBetweenAndClienteAndEmpresaAndEliminado(Date desde, Date hasta, Cliente cliente, Empresa empresa, Pageable page) {
        return ajusteCuentaCorrienteRepository.findAllByFechaBetweenAndClienteAndEmpresaAndEliminado(desde, hasta, cliente, empresa, false, page);
    }

    @Override
    public AjusteCuentaCorriente findByNotaDebito(Nota notaDebito) {
        return ajusteCuentaCorrienteRepository.findByNotaDebitoAndEliminado(notaDebito, false);
    }

    @Override
    public BigDecimal getMontoById(long idAjusteCuentaCorriente) {
        return ajusteCuentaCorrienteRepository.getMontoById(idAjusteCuentaCorriente);
    }

    @Override
    public AjusteCuentaCorriente guardar(AjusteCuentaCorriente ajusteCuentaCorriente) {
        return ajusteCuentaCorrienteRepository.save(ajusteCuentaCorriente);
    }

    @Override
    public long getSiguienteNumeroAjuste(long idEmpresa, long numSerie) {
        AjusteCuentaCorriente ajusteCuentaCorriente = ajusteCuentaCorrienteRepository.findTopByEmpresaAndNumSerieOrderByNumAjusteDesc(empresaService.getEmpresaPorId(idEmpresa), numSerie);
        if (ajusteCuentaCorriente == null) {
            return 1; // No existe ningun ajuste anterior
        } else {
            return 1 + ajusteCuentaCorriente.getNumAjuste();
        }
    }

    @Override
    public void eliminar(long idAjusteCuentaCorriente) {
        AjusteCuentaCorriente ajusteCuentaCorriente = ajusteCuentaCorrienteRepository.findById(idAjusteCuentaCorriente);
        ajusteCuentaCorriente.setEliminado(true);
        ajusteCuentaCorrienteRepository.save(ajusteCuentaCorriente);
    }
    
}

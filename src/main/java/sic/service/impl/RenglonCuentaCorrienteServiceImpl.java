package sic.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import sic.modelo.FacturaVenta;
import sic.modelo.Nota;
import sic.modelo.Pago;
import sic.modelo.RenglonCuentaCorriente;
import sic.repository.RenglonCuentaCorrienteRepository;
import sic.service.IRenglonCuentaCorrienteService;

@Service
public class RenglonCuentaCorrienteServiceImpl implements IRenglonCuentaCorrienteService {
    
    private final RenglonCuentaCorrienteRepository renglonCuentaCorrienteRepository;
    
    @Autowired
    @Lazy
    public RenglonCuentaCorrienteServiceImpl(RenglonCuentaCorrienteRepository renglonCuentaCorrienteRepository) {
        this.renglonCuentaCorrienteRepository = renglonCuentaCorrienteRepository;
    }
    
    @Override
    public RenglonCuentaCorriente asentarRenglonCuentaCorriente(RenglonCuentaCorriente renglonCuentaCorriente) {
        return renglonCuentaCorrienteRepository.save(renglonCuentaCorriente);
    }
    
    @Override
    public RenglonCuentaCorriente getRenglonCuentaCorrienteDeFactura(FacturaVenta fv, boolean eliminado) {
        return renglonCuentaCorrienteRepository.findByFacturaAndEliminado(fv, eliminado);
    }
    
    @Override
    public RenglonCuentaCorriente getRenglonCuentaCorrienteDeNota(Nota n, boolean eliminado) {
        return renglonCuentaCorrienteRepository.findByNotaAndEliminado(n, eliminado);
    }
    
    @Override
    public RenglonCuentaCorriente getRenglonCuentaCorrienteDePago(Pago p, boolean eliminado) {
        return renglonCuentaCorrienteRepository.findByPagoAndEliminado(p, eliminado);
    }
    
    @Override
    public Page<RenglonCuentaCorriente> getRenglonesCuentaCorriente(long idCuentaCorriente, Pageable page) {
        return renglonCuentaCorrienteRepository.getRenglonesCuentaCorriente(idCuentaCorriente, page);
    }
    
}

package sic.service.impl;

import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import sic.modelo.CuentaCorriente;
import sic.modelo.FacturaVenta;
import sic.modelo.Nota;
import sic.modelo.Pago;
import sic.modelo.Recibo;
import sic.modelo.RenglonCuentaCorriente;
import sic.repository.RenglonCuentaCorrienteRepository;
import sic.service.IRenglonCuentaCorrienteService;

@Service
public class RenglonCuentaCorrienteServiceImpl implements IRenglonCuentaCorrienteService {
    
    private final RenglonCuentaCorrienteRepository renglonCuentaCorrienteRepository;
    
    @Autowired
    public RenglonCuentaCorrienteServiceImpl(RenglonCuentaCorrienteRepository renglonCuentaCorrienteRepository) {
        this.renglonCuentaCorrienteRepository = renglonCuentaCorrienteRepository;
    }
    
    @Override
    public RenglonCuentaCorriente guardar(RenglonCuentaCorriente renglonCuentaCorriente) {
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
    public RenglonCuentaCorriente getRenglonCuentaCorrienteDeRecibo(Recibo r, boolean eliminado) {
        return renglonCuentaCorrienteRepository.findByReciboAndEliminado(r, eliminado);
    }
    
    @Override
    public Page<RenglonCuentaCorriente> getRenglonesCuentaCorriente(CuentaCorriente cuentaCorriente, boolean eliminado, Pageable page) {
        return renglonCuentaCorrienteRepository.findAllByCuentaCorrienteAndEliminado(cuentaCorriente, eliminado, page);
    }
    
    @Override
    public Double getSaldoCuentaCorriente(long idCuentaCorriente) {
        return renglonCuentaCorrienteRepository.getSaldoCuentaCorriente(idCuentaCorriente);
    }
    
    @Override
    public Date getFechaUltimoMovimiento(long idCuentaCorriente) {
        return renglonCuentaCorrienteRepository.getFechaUltimoMovimiento(idCuentaCorriente);
    }
}

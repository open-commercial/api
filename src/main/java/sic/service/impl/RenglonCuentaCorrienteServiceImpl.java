package sic.service.impl;

import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import sic.modelo.AjusteCuentaCorriente;
import sic.modelo.CuentaCorriente;
import sic.modelo.Factura;
import sic.modelo.Nota;
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
    public RenglonCuentaCorriente getRenglonCuentaCorrienteDeFactura(Factura f, boolean eliminado) {
        return renglonCuentaCorrienteRepository.findByFacturaAndEliminado(f, eliminado);
    }

    @Override
    public RenglonCuentaCorriente getRenglonCuentaCorrienteDeNota(Nota n, boolean eliminado) {
        return renglonCuentaCorrienteRepository.findByNotaAndEliminado(n, eliminado);
    }

    @Override
    public RenglonCuentaCorriente getRenglonCuentaCorrienteDeRecibo(Recibo r, boolean eliminado) {
        return renglonCuentaCorrienteRepository.findByReciboAndEliminado(r, eliminado);
    }
    
    @Override
    public RenglonCuentaCorriente getRenglonCuentaCorrienteDeAjusteCuentaCorriente(AjusteCuentaCorriente ajusteCC, boolean eliminado) {
        return renglonCuentaCorrienteRepository.findByAjusteCuentaCorrienteAndEliminado(ajusteCC, eliminado);
    }

    @Override
    public Page<RenglonCuentaCorriente> getRenglonesCuentaCorriente(CuentaCorriente cuentaCorriente, boolean eliminado, Pageable page) {
        return renglonCuentaCorrienteRepository.findAllByCuentaCorrienteAndEliminado(cuentaCorriente, eliminado, page);
    }

    @Override
    public Slice<RenglonCuentaCorriente> getRenglonesFacturasYNotaDebitoCuentaCorriente(long idCuentaCorriente, Pageable page) {
        return renglonCuentaCorrienteRepository.getRenglonesFacturasYNotaDebitoCuentaCorriente(idCuentaCorriente, page);
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

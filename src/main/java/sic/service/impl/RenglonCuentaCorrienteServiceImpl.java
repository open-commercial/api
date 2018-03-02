package sic.service.impl;

import java.math.BigDecimal;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
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
    public RenglonCuentaCorriente getRenglonCuentaCorrienteDeFactura(Factura factura, boolean eliminado) {
        return renglonCuentaCorrienteRepository.findByFacturaAndEliminado(factura, eliminado);
    }

    @Override
    public RenglonCuentaCorriente getRenglonCuentaCorrienteDeNota(Nota nota, boolean eliminado) {
        return renglonCuentaCorrienteRepository.findByNotaAndEliminado(nota, eliminado);
    }

    @Override
    public RenglonCuentaCorriente getRenglonCuentaCorrienteDeRecibo(Recibo recibo, boolean eliminado) {
        return renglonCuentaCorrienteRepository.findByReciboAndEliminado(recibo, eliminado);
    }

    @Override
    public Page<RenglonCuentaCorriente> getRenglonesCuentaCorriente(long idCuentaCorriente, Pageable page) {
        return renglonCuentaCorrienteRepository.findAllByCuentaCorrienteAndEliminado(idCuentaCorriente, page);
    }

    @Override
    public BigDecimal getSaldoCuentaCorriente(long idCuentaCorriente) {
        return renglonCuentaCorrienteRepository.getSaldoCuentaCorriente(idCuentaCorriente);
    }

    @Override
    public Date getFechaUltimoMovimiento(long idCuentaCorriente) {
        return renglonCuentaCorrienteRepository.getFechaUltimoMovimiento(idCuentaCorriente);
    }

}

package sic.service.impl;

import java.util.ResourceBundle;
import javax.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sic.modelo.CuentaCorriente;
import sic.modelo.FacturaVenta;
import sic.modelo.Nota;
import sic.modelo.NotaCredito;
import sic.modelo.NotaDebito;
import sic.modelo.Pago;
import sic.modelo.RenglonCuentaCorriente;
import sic.modelo.TipoDeComprobante;
import sic.modelo.TipoDeOperacion;
import sic.modelo.TipoMovimiento;
import sic.repository.CuentaCorrienteRepository;
import sic.service.BusinessServiceException;
import sic.service.IClienteService;
import sic.service.ICuentaCorrienteService;
import sic.service.IFacturaService;
import sic.service.INotaService;
import sic.service.IPagoService;
import sic.service.IRenglonCuentaCorrienteService;
import sic.util.FormatterFechaHora;

@Service
public class CuentaCorrienteServiceImpl implements ICuentaCorrienteService {
    
    private final CuentaCorrienteRepository cuentaCorrienteRepository;
    private final IClienteService clienteService;
    private final IRenglonCuentaCorrienteService renglonCuentaCorrienteService;
    private final IFacturaService facturaService;
    private final INotaService notaService;
    private final IPagoService pagoService;
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    
    @Autowired
    @Lazy
    public CuentaCorrienteServiceImpl(CuentaCorrienteRepository cuentaCorrienteRepository, IClienteService clienteService,
                IRenglonCuentaCorrienteService renglonCuentaCorrienteService, IFacturaService facturaService,
                INotaService notaService, IPagoService pagoService) {
                this.cuentaCorrienteRepository = cuentaCorrienteRepository;
                this.clienteService = clienteService;
                this.renglonCuentaCorrienteService = renglonCuentaCorrienteService;
                this.facturaService = facturaService;
                this.notaService = notaService;
                this.pagoService = pagoService;
    }

    @Override
    public void eliminar(Long idCuentaCorriente) {
        CuentaCorriente cuentaCorriente = this.getCuentaCorrientePorID(idCuentaCorriente);
        if (cuentaCorriente == null) {
            throw new EntityNotFoundException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_cuenta_corriente_no_existente"));
        }
        cuentaCorriente.setEliminada(true);
        this.cuentaCorrienteRepository.save(cuentaCorriente);
    }

    @Override
    public CuentaCorriente getCuentaCorrientePorID(Long idCuentaCorriente) {
        CuentaCorriente cuentaCorriente = cuentaCorrienteRepository.findOne(idCuentaCorriente);
        if (cuentaCorriente == null) {
            throw new EntityNotFoundException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_cuenta_corriente_no_existente"));
        }
        return cuentaCorriente;
    }

    @Override
    public CuentaCorriente guardar(CuentaCorriente cuentaCorriente) {
        cuentaCorriente.setFechaApertura(cuentaCorriente.getCliente().getFechaAlta());
        this.validarCuentaCorriente(cuentaCorriente);
        cuentaCorriente = cuentaCorrienteRepository.save(cuentaCorriente);
        LOGGER.warn("La Cuenta Corriente " + cuentaCorriente + " se guardó correctamente." );
        return cuentaCorriente;
    }

    @Override
    public void validarCuentaCorriente(CuentaCorriente cuentaCorriente) {
        //Entrada de Datos
        //Requeridos
        if (cuentaCorriente.getFechaApertura() == null) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_cuenta_corriente_fecha_vacia"));
        }
        if (cuentaCorriente.getEmpresa() == null) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_caja_empresa_vacia"));
        }
        if (cuentaCorriente.getCliente() == null) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_cliente_vacio"));
        }
        //Duplicados        
        if (cuentaCorriente.getIdCuentaCorriente() != null) {
            if (cuentaCorrienteRepository.findById(cuentaCorriente.getIdCuentaCorriente()) != null) {
                throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                        .getString("mensaje_cuenta_corriente_duplicada"));
            }
        }
    }

    @Override
    public double getSaldoCuentaCorriente(long idCuentaCorriente) {
        Double saldo = renglonCuentaCorrienteService.getSaldoCuentaCorriente(idCuentaCorriente);
        return (saldo != null) ? saldo : 0.0;
    }
    
    @Override
    public CuentaCorriente getCuentaCorrientePorCliente(long idCliente) {
        CuentaCorriente cc = cuentaCorrienteRepository.findByClienteAndEliminada(clienteService.getClientePorId(idCliente), false);
        cc.setSaldo(this.getSaldoCuentaCorriente(idCliente));
        return cc;
    }
    
    @Override
    public Page<RenglonCuentaCorriente> getRenglonesCuentaCorriente(long idCuentaCorriente, Pageable pageable) {
        CuentaCorriente cc = this.getCuentaCorrientePorID(idCuentaCorriente);
        Page<RenglonCuentaCorriente> renglonesCuentaCorriente = renglonCuentaCorrienteService.getRenglonesCuentaCorriente(cc, false, pageable);
        if (!renglonesCuentaCorriente.getContent().isEmpty()) {
            Double saldoCC = this.getSaldoCuentaCorriente(cc.getIdCuentaCorriente());
            int tamanioDePaginaAuxiliar = pageable.getPageNumber() * pageable.getPageSize();
            if (tamanioDePaginaAuxiliar != 0) {
                Pageable pageableAuxiliar = new PageRequest(0, tamanioDePaginaAuxiliar, pageable.getSort());
                Page<RenglonCuentaCorriente> renglonesCuentaCorrienteAuxiliar = renglonCuentaCorrienteService.getRenglonesCuentaCorriente(cc, false, pageableAuxiliar);
                double saldoPaginaSuperiores = 0;
                for (RenglonCuentaCorriente rcc : renglonesCuentaCorrienteAuxiliar) {
                    saldoPaginaSuperiores += rcc.getMonto();
                }
                saldoCC = saldoCC - saldoPaginaSuperiores;
            }
            for (RenglonCuentaCorriente rcc : renglonesCuentaCorriente) {
                rcc.setSaldo(saldoCC);
                saldoCC -= rcc.getMonto();
                if (rcc.getTipoMovimiento() == TipoMovimiento.VENTA) {
                    rcc.setCAE(facturaService.getCAEById(rcc.getIdMovimiento()));
                }
                if (rcc.getTipoMovimiento() == TipoMovimiento.CREDITO || rcc.getTipoMovimiento() == TipoMovimiento.DEBITO) {
                    rcc.setCAE(notaService.getCAEById(rcc.getIdMovimiento()));
                }
            }
        }
        return renglonesCuentaCorriente;
    }
    
    private TipoMovimiento getTipoMovimiento(Nota nota) {
        if (nota.getTipoComprobante().equals(TipoDeComprobante.NOTA_CREDITO_A) || nota.getTipoComprobante().equals(TipoDeComprobante.NOTA_CREDITO_B)
                || nota.getTipoComprobante().equals(TipoDeComprobante.NOTA_CREDITO_Y) || nota.getTipoComprobante().equals(TipoDeComprobante.NOTA_CREDITO_X)) {
            return TipoMovimiento.CREDITO;
        } else {
            return TipoMovimiento.DEBITO;
        }
    }
    
    @Override
    @Transactional
    public void asentarEnCuentaCorriente(FacturaVenta fv, TipoDeOperacion operacion) {
        if (operacion == TipoDeOperacion.ALTA) {
            RenglonCuentaCorriente rcc = new RenglonCuentaCorriente();
            rcc.setComprobante((fv.getTipoComprobante().equals(TipoDeComprobante.PRESUPUESTO) ? "PRESUPUESTO " : "FACTURA ")
                    + (fv.getTipoComprobante().equals(TipoDeComprobante.FACTURA_A) ? "\"A\""
                    : fv.getTipoComprobante().equals(TipoDeComprobante.FACTURA_B) ? "\"B\""
                    : fv.getTipoComprobante().equals(TipoDeComprobante.FACTURA_C) ? "\"C\""
                    : fv.getTipoComprobante().equals(TipoDeComprobante.FACTURA_X) ? "\"X\""
                    : fv.getTipoComprobante().equals(TipoDeComprobante.FACTURA_Y) ? "\"Y\""
                    : "") + " " + fv.getNumSerie() + " - " + fv.getNumFactura());
            rcc.setFactura(fv);
            rcc.setFecha(fv.getFecha());
            rcc.setFechaVencimiento(fv.getFechaVencimiento());
            rcc.setIdMovimiento(fv.getId_Factura());
            rcc.setMonto(-fv.getTotal());
            rcc.setTipoMovimiento(TipoMovimiento.VENTA);
            CuentaCorriente cc = this.getCuentaCorrientePorCliente(fv.getCliente().getId_Cliente());
            cc.getRenglones().add(rcc);
            rcc.setCuentaCorriente(cc);
            this.renglonCuentaCorrienteService.guardar(rcc);
            LOGGER.warn("El renglon " + rcc + " se guardó correctamente." );
        }
        if (operacion == TipoDeOperacion.ELIMINACION) {
            RenglonCuentaCorriente rcc = this.renglonCuentaCorrienteService.getRenglonCuentaCorrienteDeFactura(fv, false);
            rcc.setEliminado(true);
            LOGGER.warn("El renglon " + rcc + " se eliminó correctamente." );
        }
    }

    @Override
    @Transactional
    public void asentarEnCuentaCorriente(Nota n, TipoDeOperacion operacion) {
        if (operacion == TipoDeOperacion.ALTA) {
            RenglonCuentaCorriente rcc = new RenglonCuentaCorriente();
            if (n instanceof NotaCredito) {
                rcc.setComprobante("NOTA CREDITO " + (n.getTipoComprobante().equals(TipoDeComprobante.NOTA_CREDITO_A) ? "\"A\""
                        : n.getTipoComprobante().equals(TipoDeComprobante.NOTA_CREDITO_B) ? "\"B\""
                        : n.getTipoComprobante().equals(TipoDeComprobante.NOTA_CREDITO_PRESUPUESTO) ? "\"P\""
                        : n.getTipoComprobante().equals(TipoDeComprobante.NOTA_CREDITO_Y) ? "\"Y\""
                        : n.getTipoComprobante().equals(TipoDeComprobante.NOTA_CREDITO_X) ? "\"X\"" : "")
                        + " " + n.getSerie() + " - " + n.getNroNota());
                rcc.setMonto(n.getTotal());
                rcc.setDescripcion(n.getMotivo()); // Descripción de los productos
            }
            if (n instanceof NotaDebito) {
                rcc.setComprobante("NOTA DEBITO " + (n.getTipoComprobante().equals(TipoDeComprobante.NOTA_DEBITO_A) ? "\"A\""
                        : n.getTipoComprobante().equals(TipoDeComprobante.NOTA_DEBITO_B) ? "\"B\""
                        : n.getTipoComprobante().equals(TipoDeComprobante.NOTA_DEBITO_PRESUPUESTO) ? "\"P\""
                        : n.getTipoComprobante().equals(TipoDeComprobante.NOTA_DEBITO_Y) ? "\"Y\""
                        : n.getTipoComprobante().equals(TipoDeComprobante.NOTA_DEBITO_X) ? "\"X\"" : "")
                        + " " + n.getSerie() + " - " + n.getNroNota());
                rcc.setMonto(-n.getTotal());
                Pago p = pagoService.getPagoPorId(((NotaDebito) n).getPagoId());
                String descripcion = "Pago Nº " + p.getNroPago() + " " + (new FormatterFechaHora(FormatterFechaHora.FORMATO_FECHA_HISPANO)).format(p.getFecha());
                if (p.getNota() != null && p.getNota().length() > 0) {
                    descripcion += " " + p.getNota();
                }
                rcc.setDescripcion(descripcion);
            }
            rcc.setNota(n); 
            rcc.setFecha(n.getFecha());
            rcc.setIdMovimiento(n.getIdNota());
            rcc.setTipoMovimiento(this.getTipoMovimiento(n));
            CuentaCorriente cc = this.getCuentaCorrientePorCliente(n.getCliente().getId_Cliente());
            cc.getRenglones().add(rcc);
            rcc.setCuentaCorriente(cc);
            this.renglonCuentaCorrienteService.guardar(rcc);
            LOGGER.warn("El renglon " + rcc + " se guardó correctamente." );
        }
        if (operacion == TipoDeOperacion.ELIMINACION) {
            RenglonCuentaCorriente rcc = this.renglonCuentaCorrienteService.getRenglonCuentaCorrienteDeNota(n, false);
            rcc.setEliminado(true);
            LOGGER.warn("El renglon " + rcc + " se eliminó correctamente." );
        }
    }

    @Override
    @Transactional
    public void asentarEnCuentaCorriente(Pago p, TipoDeOperacion operacion, Long idCliente) {
        if (operacion == TipoDeOperacion.ALTA) {
            RenglonCuentaCorriente rcc = new RenglonCuentaCorriente();
            rcc.setPago(p);
            rcc.setComprobante("PAGO Nº " + p.getNroPago());
            rcc.setDescripcion(p.getNota());
            rcc.setFecha(p.getFecha());
            rcc.setIdMovimiento(p.getId_Pago());
            rcc.setMonto(p.getMonto());
            rcc.setTipoMovimiento(TipoMovimiento.PAGO);
            CuentaCorriente cc;
            if (p.getFactura() instanceof FacturaVenta) {
                cc = this.getCuentaCorrientePorCliente(((FacturaVenta) p.getFactura()).getCliente().getId_Cliente());
                cc.getRenglones().add(rcc);
                rcc.setCuentaCorriente(cc);
            } else if (idCliente != null) {
                cc = this.getCuentaCorrientePorCliente(idCliente);
                cc.getRenglones().add(rcc);
                rcc.setCuentaCorriente(cc);
            }
            this.renglonCuentaCorrienteService.guardar(rcc);
            LOGGER.warn("El renglon " + rcc + " se guardó correctamente." );
        }
        if (operacion == TipoDeOperacion.ELIMINACION) {
            RenglonCuentaCorriente rcc = this.renglonCuentaCorrienteService.getRenglonCuentaCorrienteDePago(p, false);
            rcc.setEliminado(true);
            LOGGER.warn("El renglon " + rcc + " se eliminó correctamente." );
        }
    }

}

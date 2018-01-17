package sic.service.impl;

import java.util.Date;
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
import sic.modelo.AjusteCuentaCorriente;
import sic.modelo.Cliente;
import sic.modelo.CuentaCorriente;
import sic.modelo.CuentaCorrienteCliente;
import sic.modelo.CuentaCorrienteProveedor;
import sic.modelo.FacturaCompra;
import sic.modelo.FacturaVenta;
import sic.modelo.Nota;
import sic.modelo.NotaCredito;
import sic.modelo.NotaDebito;
import sic.modelo.Proveedor;
import sic.modelo.Recibo;
import sic.modelo.RenglonCuentaCorriente;
import sic.modelo.TipoDeComprobante;
import sic.modelo.TipoDeOperacion;
import sic.repository.CuentaCorrienteClienteRepository;
import sic.repository.CuentaCorrienteProveedorRepository;
import sic.repository.CuentaCorrienteRepository;
import sic.service.BusinessServiceException;
import sic.service.IClienteService;
import sic.service.ICuentaCorrienteService;
import sic.service.IFacturaService;
import sic.service.INotaService;
import sic.service.IProveedorService;
import sic.service.IRenglonCuentaCorrienteService;

@Service
public class CuentaCorrienteServiceImpl implements ICuentaCorrienteService {
    
    private final CuentaCorrienteRepository cuentaCorrienteRepository;
    private final CuentaCorrienteClienteRepository cuentaCorrienteClienteRepository;
    private final CuentaCorrienteProveedorRepository cuentaCorrienteProveedorRepository;
    private final IClienteService clienteService;
    private final IProveedorService proveedorService;
    private final IRenglonCuentaCorrienteService renglonCuentaCorrienteService;
    private final IFacturaService facturaService;
    private final INotaService notaService;
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    
    @Autowired
    @Lazy
    public CuentaCorrienteServiceImpl(CuentaCorrienteRepository cuentaCorrienteRepository, CuentaCorrienteClienteRepository cuentaCorrienteClienteRepository,
                CuentaCorrienteProveedorRepository cuentaCorrienteProveedorRepository, IClienteService clienteService, IProveedorService proveedorService,
                IRenglonCuentaCorrienteService renglonCuentaCorrienteService, IFacturaService facturaService,
                INotaService notaService) {
                this.cuentaCorrienteRepository = cuentaCorrienteRepository;
                this.cuentaCorrienteClienteRepository = cuentaCorrienteClienteRepository;
                this.cuentaCorrienteProveedorRepository = cuentaCorrienteProveedorRepository;
                this.clienteService = clienteService;
                this.proveedorService = proveedorService;
                this.renglonCuentaCorrienteService = renglonCuentaCorrienteService;
                this.facturaService = facturaService;
                this.notaService = notaService;
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
        CuentaCorriente cuentaCorriente = cuentaCorrienteRepository.findById(idCuentaCorriente);
        if (cuentaCorriente == null) {
            throw new EntityNotFoundException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_cuenta_corriente_no_existente"));
        }
        return cuentaCorriente;
    }

    @Override
    public CuentaCorrienteCliente guardarCuentaCorrienteCliente(CuentaCorrienteCliente cuentaCorrienteCliente) {
        cuentaCorrienteCliente.setFechaApertura(cuentaCorrienteCliente.getCliente().getFechaAlta());
        this.validarCuentaCorriente(cuentaCorrienteCliente);
        cuentaCorrienteCliente = cuentaCorrienteClienteRepository.save(cuentaCorrienteCliente);
        LOGGER.warn("La Cuenta Corriente Cliente " + cuentaCorrienteCliente + " se guardó correctamente." );
        return cuentaCorrienteCliente;
    }
    
    @Override
    public CuentaCorrienteProveedor guardarCuentaCorrienteProveedor(CuentaCorrienteProveedor cuentaCorrienteProveedor) {
        cuentaCorrienteProveedor.setFechaApertura(new Date());
        this.validarCuentaCorriente(cuentaCorrienteProveedor);
        cuentaCorrienteProveedor = cuentaCorrienteProveedorRepository.save(cuentaCorrienteProveedor);
        LOGGER.warn("La Cuenta Corriente Proveedor " + cuentaCorrienteProveedor + " se guardó correctamente." );
        return cuentaCorrienteProveedor;
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
        if (cuentaCorriente instanceof CuentaCorrienteCliente) {
            if (((CuentaCorrienteCliente) cuentaCorriente).getCliente() == null) {
                throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                        .getString("mensaje_cliente_vacio"));
            }
        } else if (cuentaCorriente instanceof CuentaCorrienteProveedor) {
            if (((CuentaCorrienteProveedor) cuentaCorriente).getProveedor() == null) {
                throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                        .getString("mensaje_proveedor_vacio"));
            }
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
    public CuentaCorrienteCliente getCuentaCorrientePorCliente(long idCliente) {
        Cliente cliente = clienteService.getClientePorId(idCliente);
        CuentaCorrienteCliente cc = cuentaCorrienteClienteRepository.findByClienteAndEmpresaAndEliminada(cliente, cliente.getEmpresa(), false);
        cc.setSaldo(this.getSaldoCuentaCorriente(cc.getIdCuentaCorriente()));
        cc.setFechaUltimoMovimiento(this.getFechaUltimoMovimiento(cc.getIdCuentaCorriente()));
        return cc;
    }
    
    @Override
    public CuentaCorrienteProveedor getCuentaCorrientePorProveedor(long idProveedor) {
        Proveedor proveedor = proveedorService.getProveedorPorId(idProveedor);
        CuentaCorrienteProveedor cc = cuentaCorrienteProveedorRepository.findByProveedorAndEmpresaAndEliminada(proveedor, proveedor.getEmpresa(), false);
        cc.setSaldo(this.getSaldoCuentaCorriente(cc.getIdCuentaCorriente()));
        cc.setFechaUltimoMovimiento(this.getFechaUltimoMovimiento(cc.getIdCuentaCorriente()));
        return cc;
    }
    
    @Override
    public Page<RenglonCuentaCorriente> getRenglonesCuentaCorriente(long idCuentaCorriente, Pageable pageable) {
        CuentaCorriente cc = this.getCuentaCorrientePorID(idCuentaCorriente);
        Page<RenglonCuentaCorriente> renglonesCuentaCorriente = renglonCuentaCorrienteService.getRenglonesCuentaCorriente(cc, false, pageable);
        if (!renglonesCuentaCorriente.getContent().isEmpty()) {
            double saldoCC = this.getSaldoCuentaCorriente(cc.getIdCuentaCorriente());
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
                if (rcc.getTipo_comprobante() == TipoDeComprobante.FACTURA_A || rcc.getTipo_comprobante() == TipoDeComprobante.FACTURA_B
                        || rcc.getTipo_comprobante() == TipoDeComprobante.FACTURA_C || rcc.getTipo_comprobante() == TipoDeComprobante.FACTURA_X
                        || rcc.getTipo_comprobante() == TipoDeComprobante.FACTURA_Y || rcc.getTipo_comprobante() == TipoDeComprobante.PRESUPUESTO) {
                    rcc.setCAE(facturaService.getCAEById(rcc.getIdMovimiento()));
                }
                if (rcc.getTipo_comprobante() == TipoDeComprobante.NOTA_CREDITO_A || rcc.getTipo_comprobante() == TipoDeComprobante.NOTA_CREDITO_B
                        || rcc.getTipo_comprobante() == TipoDeComprobante.NOTA_CREDITO_X || rcc.getTipo_comprobante() == TipoDeComprobante.NOTA_CREDITO_Y
                        || rcc.getTipo_comprobante() == TipoDeComprobante.NOTA_CREDITO_PRESUPUESTO || rcc.getTipo_comprobante() == TipoDeComprobante.NOTA_DEBITO_A
                        || rcc.getTipo_comprobante() == TipoDeComprobante.NOTA_DEBITO_B || rcc.getTipo_comprobante() == TipoDeComprobante.NOTA_DEBITO_X
                        || rcc.getTipo_comprobante() == TipoDeComprobante.NOTA_DEBITO_Y || rcc.getTipo_comprobante() == TipoDeComprobante.NOTA_DEBITO_PRESUPUESTO) {
                    rcc.setCAE(notaService.getCAEById(rcc.getIdMovimiento()));
                }
            }
        }
        return renglonesCuentaCorriente;
    }
    
    @Override
    @Transactional
    public void asentarEnCuentaCorriente(FacturaVenta fv, TipoDeOperacion operacion) {
        if (operacion == TipoDeOperacion.ALTA) {
            RenglonCuentaCorriente rcc = new RenglonCuentaCorriente();
            rcc.setTipo_comprobante(fv.getTipoComprobante());
            rcc.setSerie(fv.getNumSerie());
            rcc.setNumero(fv.getNumFactura());
            rcc.setFactura(fv);
            rcc.setFecha(fv.getFecha());
            rcc.setFechaVencimiento(fv.getFechaVencimiento());
            rcc.setIdMovimiento(fv.getId_Factura());
            rcc.setMonto(-fv.getTotal());
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
    public void asentarEnCuentaCorriente(FacturaCompra fc, TipoDeOperacion operacion) {
        if (operacion == TipoDeOperacion.ALTA) {
            RenglonCuentaCorriente rcc = new RenglonCuentaCorriente();
            rcc.setTipo_comprobante(fc.getTipoComprobante());
            rcc.setSerie(fc.getNumSerie());
            rcc.setNumero(fc.getNumFactura());
            rcc.setFactura(fc);
            rcc.setFecha(fc.getFecha());
            rcc.setFechaVencimiento(fc.getFechaVencimiento());
            rcc.setIdMovimiento(fc.getId_Factura());
            rcc.setMonto(-fc.getTotal());
            CuentaCorriente cc = this.getCuentaCorrientePorProveedor(fc.getProveedor().getId_Proveedor());
            cc.getRenglones().add(rcc);
            rcc.setCuentaCorriente(cc);
            this.renglonCuentaCorrienteService.guardar(rcc);
            LOGGER.warn("El renglon " + rcc + " se guardó correctamente." );
        }
        if (operacion == TipoDeOperacion.ELIMINACION) {
            RenglonCuentaCorriente rcc = this.renglonCuentaCorrienteService.getRenglonCuentaCorrienteDeFactura(fc, false);
            rcc.setEliminado(true);
            LOGGER.warn("El renglon " + rcc + " se eliminó correctamente." );
        }
    }

    @Override
    @Transactional
    public void asentarEnCuentaCorriente(Nota n, TipoDeOperacion operacion) {
        if (operacion == TipoDeOperacion.ALTA) {
            RenglonCuentaCorriente rcc = new RenglonCuentaCorriente();
            rcc.setTipo_comprobante(n.getTipoComprobante());
            rcc.setSerie(n.getSerie());
            rcc.setNumero(n.getNroNota());
            if (n instanceof NotaCredito) {
                rcc.setMonto(n.getTotal());
                rcc.setDescripcion(n.getMotivo()); 
            }
            if (n instanceof NotaDebito) {
                rcc.setMonto(-n.getTotal());
                String descripcion = "";
                if (((NotaDebito) n).getRecibo() != null) {
                    descripcion = ((NotaDebito) n).getRecibo().getConcepto();
                }           
                rcc.setDescripcion(descripcion);
            }
            rcc.setNota(n); 
            rcc.setFecha(n.getFecha());
            rcc.setIdMovimiento(n.getIdNota());
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
    public void asentarEnCuentaCorriente(AjusteCuentaCorriente ajusteCC, TipoDeOperacion operacion) {
        if (operacion == TipoDeOperacion.ALTA) {
            RenglonCuentaCorriente rcc = new RenglonCuentaCorriente();
            rcc.setTipo_comprobante(ajusteCC.getTipoComprobante());
            rcc.setSerie(ajusteCC.getNumSerie());
            rcc.setNumero(ajusteCC.getNumAjuste());
            rcc.setMonto(ajusteCC.getMonto());
            rcc.setDescripcion(ajusteCC.getConcepto()); 
            rcc.setAjusteCuentaCorriente(ajusteCC); 
            rcc.setFecha(ajusteCC.getFecha());
            rcc.setIdMovimiento(ajusteCC.getIdAjusteCuentaCorriente());
            CuentaCorriente cc = this.getCuentaCorrientePorCliente(ajusteCC.getCliente().getId_Cliente());
            cc.getRenglones().add(rcc);
            rcc.setCuentaCorriente(cc);
            this.renglonCuentaCorrienteService.guardar(rcc);
            LOGGER.warn("El renglon " + rcc + " se guardó correctamente." );
        }
        if (operacion == TipoDeOperacion.ELIMINACION) {
            RenglonCuentaCorriente rcc = this.renglonCuentaCorrienteService.getRenglonCuentaCorrienteDeAjusteCuentaCorriente(ajusteCC, false);
            rcc.setEliminado(true);
            LOGGER.warn("El renglon " + rcc + " se eliminó correctamente." );
        }
    }
    
    @Override
    @Transactional
    public void asentarEnCuentaCorriente(Recibo r, TipoDeOperacion operacion) {
        RenglonCuentaCorriente rcc;
        if (operacion == TipoDeOperacion.ALTA) {
            rcc = new RenglonCuentaCorriente();
            rcc.setRecibo(r);
            rcc.setTipo_comprobante(TipoDeComprobante.RECIBO);
            rcc.setSerie(r.getNumSerie());
            rcc.setNumero(r.getNumRecibo());
            rcc.setDescripcion(r.getConcepto());
            rcc.setFecha(r.getFecha());
            rcc.setIdMovimiento(r.getIdRecibo());
            rcc.setMonto(r.getMonto());
            CuentaCorriente cc = null;
            if (r.getCliente() != null) {
                cc = this.getCuentaCorrientePorCliente(r.getCliente().getId_Cliente());
            } else if (r.getProveedor() != null) {
                cc = this.getCuentaCorrientePorProveedor(r.getProveedor().getId_Proveedor());
            }
            if (null == cc) {
                throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                        .getString("mensaje_cuenta_corriente_no_existente"));

            }
            cc.getRenglones().add(rcc);
            rcc.setCuentaCorriente(cc);
            this.renglonCuentaCorrienteService.guardar(rcc);
            LOGGER.warn("El renglon " + rcc + " se guardó correctamente.");
        }
        if (operacion == TipoDeOperacion.ELIMINACION) {
            rcc = this.renglonCuentaCorrienteService.getRenglonCuentaCorrienteDeRecibo(r, false);
            rcc.setEliminado(true);
            LOGGER.warn("El renglon " + rcc + " se eliminó correctamente.");
        }
    }

    @Override
    public Date getFechaUltimoMovimiento(long idCuentaCorriente) {
        return renglonCuentaCorrienteService.getFechaUltimoMovimiento(idCuentaCorriente);
    }

}

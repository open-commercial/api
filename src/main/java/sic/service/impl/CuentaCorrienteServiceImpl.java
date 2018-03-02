package sic.service.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.ResourceBundle;
import javax.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    
    @Autowired
    @Lazy
    public CuentaCorrienteServiceImpl(CuentaCorrienteRepository cuentaCorrienteRepository, CuentaCorrienteClienteRepository cuentaCorrienteClienteRepository,
                CuentaCorrienteProveedorRepository cuentaCorrienteProveedorRepository, IClienteService clienteService, IProveedorService proveedorService,
                IRenglonCuentaCorrienteService renglonCuentaCorrienteService) {
                this.cuentaCorrienteRepository = cuentaCorrienteRepository;
                this.cuentaCorrienteClienteRepository = cuentaCorrienteClienteRepository;
                this.cuentaCorrienteProveedorRepository = cuentaCorrienteProveedorRepository;
                this.clienteService = clienteService;
                this.proveedorService = proveedorService;
                this.renglonCuentaCorrienteService = renglonCuentaCorrienteService;
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
    public BigDecimal getSaldoCuentaCorriente(long idCuentaCorriente) {
        BigDecimal saldo = renglonCuentaCorrienteService.getSaldoCuentaCorriente(idCuentaCorriente);
        return (saldo != null) ? saldo : BigDecimal.ZERO;
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
        return renglonCuentaCorrienteService.getRenglonesCuentaCorriente(idCuentaCorriente, pageable);
    }
    
    @Override
    @Transactional
    public void asentarEnCuentaCorriente(FacturaVenta facturaVenta, TipoDeOperacion tipo) {
        if (tipo == TipoDeOperacion.ALTA) {
            RenglonCuentaCorriente rcc = new RenglonCuentaCorriente();
            rcc.setTipoComprobante(facturaVenta.getTipoComprobante());
            rcc.setSerie(facturaVenta.getNumSerie());
            rcc.setNumero(facturaVenta.getNumFactura());
            rcc.setFactura(facturaVenta);
            rcc.setFecha(facturaVenta.getFecha());
            rcc.setFechaVencimiento(facturaVenta.getFechaVencimiento());
            rcc.setIdMovimiento(facturaVenta.getId_Factura());
            rcc.setMonto(facturaVenta.getTotal().negate());
            CuentaCorriente cc = this.getCuentaCorrientePorCliente(facturaVenta.getCliente().getId_Cliente());
            cc.getRenglones().add(rcc);
            rcc.setCuentaCorriente(cc);
            this.renglonCuentaCorrienteService.guardar(rcc);
            LOGGER.warn("El renglon " + rcc + " se guardó correctamente." );
        }
        if (tipo == TipoDeOperacion.ELIMINACION) {
            RenglonCuentaCorriente rcc = this.renglonCuentaCorrienteService.getRenglonCuentaCorrienteDeFactura(facturaVenta, false);
            rcc.setEliminado(true);
            LOGGER.warn("El renglon " + rcc + " se eliminó correctamente." );
        }
    }
    
    @Override
    @Transactional
    public void asentarEnCuentaCorriente(FacturaCompra facturaCompra, TipoDeOperacion tipo) {
        if (tipo == TipoDeOperacion.ALTA) {
            RenglonCuentaCorriente rcc = new RenglonCuentaCorriente();
            rcc.setTipoComprobante(facturaCompra.getTipoComprobante());
            rcc.setSerie(facturaCompra.getNumSerie());
            rcc.setNumero(facturaCompra.getNumFactura());
            rcc.setFactura(facturaCompra);
            rcc.setFecha(facturaCompra.getFecha());
            rcc.setFechaVencimiento(facturaCompra.getFechaVencimiento());
            rcc.setIdMovimiento(facturaCompra.getId_Factura());
            rcc.setMonto(facturaCompra.getTotal().negate());
            CuentaCorriente cc = this.getCuentaCorrientePorProveedor(facturaCompra.getProveedor().getId_Proveedor());
            cc.getRenglones().add(rcc);
            rcc.setCuentaCorriente(cc);
            this.renglonCuentaCorrienteService.guardar(rcc);
            LOGGER.warn("El renglon " + rcc + " se guardó correctamente." );
        }
        if (tipo == TipoDeOperacion.ELIMINACION) {
            RenglonCuentaCorriente rcc = this.renglonCuentaCorrienteService.getRenglonCuentaCorrienteDeFactura(facturaCompra, false);
            rcc.setEliminado(true);
            LOGGER.warn("El renglon " + rcc + " se eliminó correctamente." );
        }
    }

    @Override
    @Transactional
    public void asentarEnCuentaCorriente(Nota nota, TipoDeOperacion tipo) {
        if (tipo == TipoDeOperacion.ALTA) {
            RenglonCuentaCorriente rcc = new RenglonCuentaCorriente();
            rcc.setTipoComprobante(nota.getTipoComprobante());
            rcc.setSerie(nota.getSerie());
            rcc.setNumero(nota.getNroNota());
            if (nota instanceof NotaCredito) {
                rcc.setMonto(nota.getTotal());
                rcc.setDescripcion(nota.getMotivo());
            }
            if (nota instanceof NotaDebito) {
                rcc.setMonto(nota.getTotal().negate());
                String descripcion = "";
                if (((NotaDebito) nota).getRecibo() != null) {
                    descripcion = ((NotaDebito) nota).getRecibo().getConcepto();
                }           
                rcc.setDescripcion(descripcion);
            }
            rcc.setNota(nota);
            rcc.setFecha(nota.getFecha());
            rcc.setIdMovimiento(nota.getIdNota());
            CuentaCorriente cc = this.getCuentaCorrientePorCliente(nota.getCliente().getId_Cliente());
            cc.getRenglones().add(rcc);
            rcc.setCuentaCorriente(cc);
            this.renglonCuentaCorrienteService.guardar(rcc);
            LOGGER.warn("El renglon " + rcc + " se guardó correctamente." );
        }
        if (tipo == TipoDeOperacion.ELIMINACION) {
            RenglonCuentaCorriente rcc = this.renglonCuentaCorrienteService.getRenglonCuentaCorrienteDeNota(nota, false);
            rcc.setEliminado(true);
            LOGGER.warn("El renglon " + rcc + " se eliminó correctamente." );
        }
    }
    
    @Override
    @Transactional
    public void asentarEnCuentaCorriente(Recibo recibo, TipoDeOperacion tipo) {
        RenglonCuentaCorriente rcc;
        if (tipo == TipoDeOperacion.ALTA) {
            rcc = new RenglonCuentaCorriente();
            rcc.setRecibo(recibo);
            rcc.setTipoComprobante(TipoDeComprobante.RECIBO);
            rcc.setSerie(recibo.getNumSerie());
            rcc.setNumero(recibo.getNumRecibo());
            rcc.setDescripcion(recibo.getConcepto());
            rcc.setFecha(recibo.getFecha());
            rcc.setIdMovimiento(recibo.getIdRecibo());
            rcc.setMonto(recibo.getMonto());
            CuentaCorriente cc = null;
            if (recibo.getCliente() != null) {
                cc = this.getCuentaCorrientePorCliente(recibo.getCliente().getId_Cliente());
            } else if (recibo.getProveedor() != null) {
                cc = this.getCuentaCorrientePorProveedor(recibo.getProveedor().getId_Proveedor());
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
        if (tipo == TipoDeOperacion.ELIMINACION) {
            rcc = this.renglonCuentaCorrienteService.getRenglonCuentaCorrienteDeRecibo(recibo, false);
            rcc.setEliminado(true);
            LOGGER.warn("El renglon " + rcc + " se eliminó correctamente.");
        }
    }

    @Override
    public Date getFechaUltimoMovimiento(long idCuentaCorriente) {
        return renglonCuentaCorrienteService.getFechaUltimoMovimiento(idCuentaCorriente);
    }

}

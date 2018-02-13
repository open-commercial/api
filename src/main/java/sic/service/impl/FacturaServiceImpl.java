package sic.service.impl;

import java.io.IOException;
import sic.modelo.BusquedaFacturaCompraCriteria;
import sic.modelo.BusquedaFacturaVentaCriteria;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import javax.imageio.ImageIO;
import javax.persistence.EntityNotFoundException;
import javax.swing.ImageIcon;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sic.modelo.Cliente;
import sic.modelo.ComprobanteAFIP;
import sic.modelo.ConfiguracionDelSistema;
import sic.modelo.Empresa;
import sic.modelo.Factura;
import sic.modelo.FacturaCompra;
import sic.modelo.FacturaVenta;
import sic.modelo.Pago;
import sic.modelo.Pedido;
import sic.modelo.Producto;
import sic.modelo.Proveedor;
import sic.modelo.RenglonFactura;
import sic.service.IConfiguracionDelSistemaService;
import sic.service.IFacturaService;
import sic.service.IPagoService;
import sic.service.IPedidoService;
import sic.service.IProductoService;
import sic.modelo.Movimiento;
import sic.modelo.Recibo;
import sic.modelo.RenglonPedido;
import sic.modelo.TipoDeComprobante;
import sic.service.BusinessServiceException;
import sic.service.ServiceException;
import sic.modelo.TipoDeOperacion;
import sic.util.Validator;
import sic.repository.FacturaVentaRepository;
import sic.repository.FacturaCompraRepository;
import sic.repository.FacturaRepository;
import sic.repository.RenglonFacturaRepository;
import sic.service.ICuentaCorrienteService;
import sic.service.IAfipService;
import sic.service.INotaService;
import sic.service.IReciboService;

@Service
public class FacturaServiceImpl implements IFacturaService {

    private final FacturaRepository facturaRepository;
    private final FacturaVentaRepository facturaVentaRepository;
    private final FacturaCompraRepository facturaCompraRepository;
    private final RenglonFacturaRepository renglonFacturaRepository;
    private final IProductoService productoService;
    private final IConfiguracionDelSistemaService configuracionDelSistemaService;
    private final IPedidoService pedidoService;
    private final IPagoService pagoService;
    private final INotaService notaService;
    private final ICuentaCorrienteService cuentaCorrienteService;
    private final IAfipService afipService;
    private final IReciboService reciboService;
    private final static BigDecimal IVA_21 = new BigDecimal("21");
    private final static BigDecimal IVA_105 = new BigDecimal("10.5");
    private final static BigDecimal CIEN = new BigDecimal("100");
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    
    @Autowired
    @Lazy
    public FacturaServiceImpl(FacturaRepository facturaRepository,
            FacturaVentaRepository facturaVentaRepository,
            FacturaCompraRepository facturaCompraRepository,
            RenglonFacturaRepository renglonFacturaRepository,
            IProductoService productoService,
            IConfiguracionDelSistemaService configuracionDelSistemaService,
            IPedidoService pedidoService, IPagoService pagoService, 
            INotaService notaService,
            ICuentaCorrienteService cuentaCorrienteService,
            IAfipService afipService,
            IReciboService reciboService) {
        this.facturaRepository = facturaRepository;
        this.facturaVentaRepository = facturaVentaRepository;
        this.facturaCompraRepository = facturaCompraRepository;
        this.renglonFacturaRepository = renglonFacturaRepository;
        this.productoService = productoService;
        this.configuracionDelSistemaService = configuracionDelSistemaService;
        this.pedidoService = pedidoService;
        this.pagoService = pagoService;
        this.notaService = notaService;
        this.cuentaCorrienteService = cuentaCorrienteService;
        this.afipService = afipService;
        this.reciboService = reciboService;
    }
    
    @Override
    public Factura getFacturaPorId(Long idFactura) {
        Factura factura = facturaRepository.findById(idFactura);
        if (factura == null) {
            throw new EntityNotFoundException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_factura_eliminada"));
        }
        return factura;
    }
    
    @Override
    public Long getCAEById(long idFactura) {    
         return facturaRepository.getCAEById(idFactura);
    }
    
    @Override
    public BigDecimal getTotalById(long idFactura) {
        BigDecimal total = facturaRepository.getTotalById(idFactura);
        return (total != null) ? total : BigDecimal.ZERO;
    }
    
    @Override
    public List<Factura> getFacturasDelPedido(Long idPedido) {
        return facturaRepository.findAllByPedidoAndEliminada(pedidoService.getPedidoPorId(idPedido), false);
    }
    
    @Override
    public TipoDeComprobante[] getTipoFacturaCompra(Empresa empresa, Proveedor proveedor) {
        //cuando la Empresa discrimina IVA
        if (empresa.getCondicionIVA().isDiscriminaIVA()) {
            if (proveedor.getCondicionIVA().isDiscriminaIVA()) {
                //cuando la Empresa discrimina IVA y el Proveedor tambien
                TipoDeComprobante[] tiposPermitidos = new TipoDeComprobante[4];
                tiposPermitidos[0] = TipoDeComprobante.FACTURA_A;
                tiposPermitidos[1] = TipoDeComprobante.FACTURA_B;
                tiposPermitidos[2] = TipoDeComprobante.FACTURA_X;
                tiposPermitidos[3] = TipoDeComprobante.PRESUPUESTO;
                return tiposPermitidos;
            } else {
                //cuando la Empresa discrminina IVA y el Proveedor NO
                TipoDeComprobante[] tiposPermitidos = new TipoDeComprobante[3];
                tiposPermitidos[0] = TipoDeComprobante.FACTURA_C;
                tiposPermitidos[1] = TipoDeComprobante.FACTURA_X;
                tiposPermitidos[2] = TipoDeComprobante.PRESUPUESTO;
                return tiposPermitidos;
            }
        } else {
            //cuando la Empresa NO discrimina IVA                
            if (proveedor.getCondicionIVA().isDiscriminaIVA()) {
                //cuando Empresa NO discrimina IVA y el Proveedor SI
                TipoDeComprobante[] tiposPermitidos = new TipoDeComprobante[3];
                tiposPermitidos[0] = TipoDeComprobante.FACTURA_B;
                tiposPermitidos[1] = TipoDeComprobante.FACTURA_X;
                tiposPermitidos[2] = TipoDeComprobante.PRESUPUESTO;
                return tiposPermitidos;
            } else {
                //cuando la Empresa NO discrminina IVA y el Proveedor tampoco
                TipoDeComprobante[] tiposPermitidos = new TipoDeComprobante[3];
                tiposPermitidos[0] = TipoDeComprobante.FACTURA_C;
                tiposPermitidos[1] = TipoDeComprobante.FACTURA_X;
                tiposPermitidos[2] = TipoDeComprobante.PRESUPUESTO;
                return tiposPermitidos;
            }
        }
    }

    @Override
    public TipoDeComprobante[] getTipoFacturaVenta(Empresa empresa, Cliente cliente) {
        //cuando la Empresa discrimina IVA
        if (empresa.getCondicionIVA().isDiscriminaIVA()) {
            if (cliente.getCondicionIVA().isDiscriminaIVA()) {
                //cuando la Empresa discrimina IVA y el Cliente tambien
                TipoDeComprobante[] tiposPermitidos = new TipoDeComprobante[4];
                tiposPermitidos[0] = TipoDeComprobante.FACTURA_A;                
                tiposPermitidos[1] = TipoDeComprobante.FACTURA_X;
                tiposPermitidos[2] = TipoDeComprobante.FACTURA_Y;                
                tiposPermitidos[3] = TipoDeComprobante.PRESUPUESTO;
                return tiposPermitidos;
            } else {
                //cuando la Empresa discrminina IVA y el Cliente NO
                TipoDeComprobante[] tiposPermitidos = new TipoDeComprobante[4];
                tiposPermitidos[0] = TipoDeComprobante.FACTURA_B;
                tiposPermitidos[1] = TipoDeComprobante.FACTURA_X;
                tiposPermitidos[2] = TipoDeComprobante.FACTURA_Y;                
                tiposPermitidos[3] = TipoDeComprobante.PRESUPUESTO;
                return tiposPermitidos;
            }
        } else {
            //cuando la Empresa NO discrimina IVA
            if (cliente.getCondicionIVA().isDiscriminaIVA()) {
                //cuando Empresa NO discrimina IVA y el Cliente SI
                TipoDeComprobante[] tiposPermitidos = new TipoDeComprobante[4];
                tiposPermitidos[0] = TipoDeComprobante.FACTURA_C;
                tiposPermitidos[1] = TipoDeComprobante.FACTURA_X;
                tiposPermitidos[2] = TipoDeComprobante.FACTURA_Y;                
                tiposPermitidos[3] = TipoDeComprobante.PRESUPUESTO;
                return tiposPermitidos;
            } else {
                //cuando la Empresa NO discrminina IVA y el Cliente tampoco
                TipoDeComprobante[] tiposPermitidos = new  TipoDeComprobante[4];
                tiposPermitidos[0] = TipoDeComprobante.FACTURA_C;
                tiposPermitidos[1] = TipoDeComprobante.FACTURA_X;
                tiposPermitidos[2] = TipoDeComprobante.FACTURA_Y;                
                tiposPermitidos[3] = TipoDeComprobante.PRESUPUESTO;
                return tiposPermitidos;
            }
        }
    }

    @Override
    public TipoDeComprobante[] getTiposFacturaSegunEmpresa(Empresa empresa) {
        if (empresa.getCondicionIVA().isDiscriminaIVA()) {
            TipoDeComprobante[] tiposPermitidos = new TipoDeComprobante[5];
            tiposPermitidos[0] = TipoDeComprobante.FACTURA_A;
            tiposPermitidos[1] = TipoDeComprobante.FACTURA_B;
            tiposPermitidos[2] = TipoDeComprobante.FACTURA_X;
            tiposPermitidos[3] = TipoDeComprobante.FACTURA_Y;
            tiposPermitidos[4] = TipoDeComprobante.PRESUPUESTO;
            return tiposPermitidos;
        } else {
            TipoDeComprobante[] tiposPermitidos = new TipoDeComprobante[4];
            tiposPermitidos[0] = TipoDeComprobante.FACTURA_C;
            tiposPermitidos[1] = TipoDeComprobante.FACTURA_X;
            tiposPermitidos[2] = TipoDeComprobante.FACTURA_Y;
            tiposPermitidos[3] = TipoDeComprobante.PRESUPUESTO;
            return tiposPermitidos;
        }
    }

    @Override
    public List<RenglonFactura> getRenglonesDeLaFactura(Long idFactura) {
        return this.getFacturaPorId(idFactura).getRenglones();
    }  
    
    @Override
    public Factura getFacturaDelPago(long idPago) {
        return facturaRepository.getFacturaDelPago(idPago);
    }
    
    @Override
    public List<RenglonFactura> getRenglonesDeLaFacturaModificadosParaCredito(Long id_Factura) {
        return notaService.getRenglonesFacturaModificadosParaNotaCredito(id_Factura);
    }
    
    @Override
    public RenglonFactura getRenglonFactura(Long idRenglonFactura) {
        return renglonFacturaRepository.findOne(idRenglonFactura);
    }

    @Override
    public Page<FacturaCompra> buscarFacturaCompra(BusquedaFacturaCompraCriteria criteria) {
        //Empresa
        if (criteria.getEmpresa() == null) {
            throw new EntityNotFoundException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_empresa_no_existente"));
        }
        //Fecha de Factura        
        if (criteria.isBuscaPorFecha() == true & (criteria.getFechaDesde() == null | criteria.getFechaHasta() == null)) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_factura_fechas_busqueda_invalidas"));
        }
        if (criteria.isBuscaPorFecha() == true) {
            Calendar cal = new GregorianCalendar();
            cal.setTime(criteria.getFechaDesde());
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            criteria.setFechaDesde(cal.getTime());
            cal.setTime(criteria.getFechaHasta());
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            criteria.setFechaHasta(cal.getTime());
        }
        //Proveedor
        if (criteria.isBuscaPorProveedor() == true && criteria.getProveedor() == null) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_factura_proveedor_vacio"));
        }
        return facturaCompraRepository.buscarFacturasCompra(criteria);
    }

    @Override
    public Page<FacturaVenta> buscarFacturaVenta(BusquedaFacturaVentaCriteria criteria) {
        //Empresa
        if(criteria.getEmpresa() == null ) {
            throw new EntityNotFoundException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_empresa_no_existente"));
        }
        //Fecha de Factura        
        if (criteria.isBuscaPorFecha() == true && (criteria.getFechaDesde() == null || criteria.getFechaHasta() == null)) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_factura_fechas_busqueda_invalidas"));
        }
        if (criteria.isBuscaPorFecha() == true) {
            Calendar cal = new GregorianCalendar();
            cal.setTime(criteria.getFechaDesde());
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            criteria.setFechaDesde(cal.getTime());
            cal.setTime(criteria.getFechaHasta());
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            criteria.setFechaHasta(cal.getTime());
        }
        //Cliente
        if (criteria.isBuscaCliente() == true && criteria.getCliente() == null) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_factura_cliente_vacio"));
        }
        //Usuario
        if (criteria.isBuscaUsuario() == true && criteria.getUsuario() == null) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_factura_usuario_vacio"));
        }
        if (criteria.isBuscaViajante() == true && criteria.getViajante() == null) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_factura_viajante_vacio"));
        }
        return facturaVentaRepository.buscarFacturasVenta(criteria);
    }

    private Factura procesarFactura(Factura factura) {
        factura.setEliminada(false);
        if (factura instanceof FacturaVenta) {
            factura.setFecha(new Date());
            factura.setNumSerie(configuracionDelSistemaService
                    .getConfiguracionDelSistemaPorEmpresa(factura.getEmpresa()).getNroPuntoDeVentaAfip());
            factura.setNumFactura(this.calcularNumeroFacturaVenta(factura.getTipoComprobante(),
                    factura.getNumSerie(), factura.getEmpresa().getId_Empresa()));
        }
        this.validarFactura(factura);
        return factura;
    }

    @Override
    @Transactional
    public List<Factura> guardar(List<Factura> facturas, Long idPedido, List<Recibo> recibos) {
        List<Factura> facturasProcesadas = new ArrayList<>();
        facturas.forEach(f -> {
            if (f instanceof FacturaVenta) {
                productoService.actualizarStock(this.getIdsProductosYCantidades(f), TipoDeOperacion.ALTA, Movimiento.VENTA);
            } else if (f instanceof FacturaCompra) {
                productoService.actualizarStock(this.getIdsProductosYCantidades(f), TipoDeOperacion.ALTA, Movimiento.COMPRA);
            }
        });
        if (idPedido != null) {
            Pedido pedido = pedidoService.getPedidoPorId(idPedido);
            facturas.forEach(f -> {
                f.setPedido(pedido);
            });
            for (Factura f : facturas) {
                Factura facturaGuardada = facturaVentaRepository.save((FacturaVenta) this.procesarFactura(f));
                this.cuentaCorrienteService.asentarEnCuentaCorriente((FacturaVenta) facturaGuardada, TipoDeOperacion.ALTA);
                facturasProcesadas.add(facturaGuardada);
                this.procesarRecibosYPagarFacturas(recibos, facturaGuardada);
            }
            pedido.setFacturas(facturasProcesadas);
            pedidoService.actualizar(pedido);
            facturasProcesadas.stream().forEach(f -> {
                LOGGER.warn("La Factura " + f + " se guardó correctamente.");
            });
            pedidoService.actualizarEstadoPedido(pedido);
        } else {
            facturasProcesadas = new ArrayList<>();
            for (Factura f : facturas) {
                Factura facturaGuardada = null;
                if (f instanceof FacturaVenta) {
                    facturaGuardada = facturaVentaRepository.save((FacturaVenta) this.procesarFactura(f));
                    this.cuentaCorrienteService.asentarEnCuentaCorriente((FacturaVenta) facturaGuardada, TipoDeOperacion.ALTA);
                } else if (f instanceof FacturaCompra) {
                    facturaGuardada = facturaCompraRepository.save((FacturaCompra) this.procesarFactura(f));
                    this.cuentaCorrienteService.asentarEnCuentaCorriente((FacturaCompra) facturaGuardada, TipoDeOperacion.ALTA);
                }
                facturasProcesadas.add(facturaGuardada);
                LOGGER.warn("La Factura " + facturaGuardada + " se guardó correctamente.");
                this.procesarRecibosYPagarFacturas(recibos, facturaGuardada);
            }
        }
        return facturasProcesadas;
    }

    private void procesarRecibosYPagarFacturas(List<Recibo> recibos, Factura facturaGuardada) {
        if (recibos != null) {
            recibos.forEach(r -> {
                reciboService.guardar(r);
            });
        } else {
            this.pagarFacturaConRecibosSobrantes(facturaGuardada);
        }
    }

    private void pagarFacturaConRecibosSobrantes(Factura facturaGuardada) {
        List<Recibo> recibos = new ArrayList<>();
        if (facturaGuardada instanceof FacturaVenta) {
            recibos = reciboService.getRecibosConSaldoSobranteCliente(facturaGuardada.getEmpresa().getId_Empresa(),
                    ((FacturaVenta) facturaGuardada).getCliente().getId_Cliente());
        } else if (facturaGuardada instanceof FacturaCompra) {
            recibos = reciboService.getRecibosConSaldoSobranteProveedor(facturaGuardada.getEmpresa().getId_Empresa(),
                    ((FacturaCompra) facturaGuardada).getProveedor().getId_Proveedor());
        }
        List<Pago> pagos = new ArrayList<>();
        BigDecimal saldoFactura = pagoService.getSaldoAPagarFactura(facturaGuardada.getId_Factura());
        for (Recibo r : recibos) {
            BigDecimal saldoSobrante = r.getSaldoSobrante();
            while (r.getSaldoSobrante().compareTo(BigDecimal.ZERO) > 0) {
                if (saldoFactura.compareTo(saldoSobrante) == -1) {
                    Pago nuevoPago = new Pago();
                    nuevoPago.setMonto(saldoFactura);
                    nuevoPago.setRecibo(r);
                    nuevoPago.setFactura(facturaGuardada);
                    nuevoPago.setEmpresa(facturaGuardada.getEmpresa());
                    nuevoPago.setFecha(new Date());
                    nuevoPago.setFormaDePago(r.getFormaDePago());
                    nuevoPago.setNota("");
                    pagoService.guardar(nuevoPago);
                    pagos.add(nuevoPago);
                    reciboService.actualizarSaldoSobrante(r.getIdRecibo(), (r.getSaldoSobrante().subtract(saldoFactura)));
                } else if (saldoFactura.compareTo(saldoSobrante) > -1) {
                    Pago nuevoPago = new Pago();
                    nuevoPago.setMonto(r.getSaldoSobrante());
                    nuevoPago.setRecibo(r);
                    nuevoPago.setFactura(facturaGuardada);
                    nuevoPago.setEmpresa(facturaGuardada.getEmpresa());
                    nuevoPago.setFecha(new Date());
                    nuevoPago.setFormaDePago(r.getFormaDePago());
                    nuevoPago.setNota("");
                    pagoService.guardar(nuevoPago);
                    pagos.add(nuevoPago);
                    reciboService.actualizarSaldoSobrante(r.getIdRecibo(), BigDecimal.ZERO);
                }
                if (facturaGuardada.isPagada()) {
                    break;
                }
                saldoFactura = pagoService.getSaldoAPagarFactura(facturaGuardada.getId_Factura());
            }
            if (facturaGuardada.isPagada()) {
                break;
            }
        }
        facturaGuardada.setPagos(pagos);
    }
    
    @Override
    @Transactional
    public void eliminar(long[] idsFactura) {
        for (long idFactura : idsFactura) {
            Factura factura = this.getFacturaPorId(idFactura);
            if (factura.getCAE() == 0L) {
                if (factura instanceof FacturaVenta) {
                    if (notaService.existsByFacturaVentaAndEliminada((FacturaVenta) factura)) {
                        throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                                .getString("mensaje_no_se_puede_eliminar"));
                    }
                    this.cuentaCorrienteService.asentarEnCuentaCorriente((FacturaVenta) factura, TipoDeOperacion.ELIMINACION);
                    productoService.actualizarStock(this.getIdsProductosYCantidades(factura), TipoDeOperacion.ELIMINACION, Movimiento.VENTA);
                } else if (factura instanceof FacturaCompra) {
                    this.cuentaCorrienteService.asentarEnCuentaCorriente((FacturaCompra) factura, TipoDeOperacion.ELIMINACION);
                    productoService.actualizarStock(this.getIdsProductosYCantidades(factura), TipoDeOperacion.ELIMINACION, Movimiento.COMPRA);
                }
                List<Pago> pagos = pagoService.getPagosDeLaFactura(idFactura);
                if (!pagos.isEmpty()) {
                    pagos.forEach(pago -> {
                        pagoService.eliminar(pago.getId_Pago());
                    });
                }
                factura.setEliminada(true);
                if (factura.getPedido() != null) {
                    pedidoService.actualizarEstadoPedido(factura.getPedido());
                }
            } else {
                throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                        .getString("mensaje_eliminar_factura_aprobada"));
            }
        }
    }

    private HashMap<Long, BigDecimal> getIdsProductosYCantidades(Factura factura) {
        HashMap<Long, BigDecimal> idsYCantidades = new HashMap<>();
        factura.getRenglones().forEach(r -> {
            idsYCantidades.put(r.getId_ProductoItem(), r.getCantidad());
        });
        return idsYCantidades;
    }

    private void validarFactura(Factura factura) {
        //Entrada de Datos
        if (factura.getFechaVencimiento() != null) {
            Calendar calFechaVencimiento = new GregorianCalendar();
            calFechaVencimiento.setTime(factura.getFechaVencimiento());
            calFechaVencimiento.set(Calendar.HOUR, 0);
            calFechaVencimiento.set(Calendar.MINUTE, 0);
            calFechaVencimiento.set(Calendar.SECOND, 0);
            calFechaVencimiento.set(Calendar.MILLISECOND, 0);          
            Calendar calFechaFactura = new GregorianCalendar();
            calFechaFactura.setTime(factura.getFecha());
            calFechaFactura.set(Calendar.HOUR, 0);
            calFechaFactura.set(Calendar.MINUTE, 0);
            calFechaFactura.set(Calendar.SECOND, 0);
            calFechaFactura.set(Calendar.MILLISECOND, 0);
            if (Validator.compararFechas(calFechaVencimiento.getTime(), calFechaFactura.getTime()) > 0) {
                throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                        .getString("mensaje_factura_fecha_invalida"));
            }
        }
        //Requeridos
        if (factura.getFecha() == null) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_factura_fecha_vacia"));
        }
        if (factura.getTipoComprobante() == null) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_factura_tipo_factura_vacia"));
        }
        if (factura.getTransportista() == null) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_factura_transportista_vacio"));
        }
        if (factura.getRenglones() == null || factura.getRenglones().isEmpty()) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_factura_renglones_vacio"));
        }
        if (factura.getEmpresa() == null) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_factura_empresa_vacia"));
        }
        if (factura instanceof FacturaCompra) {
            FacturaCompra facturaCompra = (FacturaCompra) factura;
            if (facturaCompra.getProveedor() == null) {
                throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                        .getString("mensaje_factura_proveedor_vacio"));
            }
        }
        if (factura instanceof FacturaVenta) {
            FacturaVenta facturaVenta = (FacturaVenta) factura;
            if (facturaVenta.getCliente() == null) {
                throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                        .getString("mensaje_factura_cliente_vacio"));
            }
            if (facturaVenta.getUsuario() == null) {
                throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                        .getString("mensaje_factura_usuario_vacio"));
            }
        }
        //Calculos
        //SubTotal
        BigDecimal[] importes = new BigDecimal[factura.getRenglones().size()];
        int i = 0;
        for (RenglonFactura renglon : factura.getRenglones()) {
            importes[i] = renglon.getImporte();
            i++;
        }        
        if (factura.getSubTotal().compareTo(this.calcularSubTotal(importes)) != 0) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_factura_sub_total_no_valido"));
        }
        //SubTotalBruto
        if (factura.getSubTotal_bruto().compareTo(this.calcularSubTotalBruto(factura.getTipoComprobante(),
                factura.getSubTotal(), factura.getRecargo_neto(), factura.getDescuento_neto(),
                factura.getIva_105_neto(), factura.getIva_21_neto())) != 0) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_factura_sub_total_bruto_no_valido"));
        }
        //IVA
        i = 0;
        if (factura.getTipoComprobante() == TipoDeComprobante.FACTURA_A || factura.getTipoComprobante() == TipoDeComprobante.FACTURA_B
                || factura.getTipoComprobante() == TipoDeComprobante.PRESUPUESTO) {
            BigDecimal[] ivaPorcentajes = new BigDecimal[factura.getRenglones().size()];
            BigDecimal[] ivaNetos = new BigDecimal[factura.getRenglones().size()];
            BigDecimal[] cantidades = new BigDecimal[factura.getRenglones().size()];
            for (RenglonFactura renglon : factura.getRenglones()) {
                ivaPorcentajes[i] = renglon.getIva_porcentaje();
                ivaNetos[i] = renglon.getIva_neto();
                cantidades[i] = renglon.getCantidad();
                i++;
            }
            BigDecimal ivaNeto21 = this.calcularIvaNetoFactura(factura.getTipoComprobante(), cantidades, ivaPorcentajes, ivaNetos,
                    IVA_21, factura.getDescuento_porcentaje(), factura.getRecargo_porcentaje());
            BigDecimal ivaNeto105 = this.calcularIvaNetoFactura(factura.getTipoComprobante(), cantidades, ivaPorcentajes, ivaNetos,
                    IVA_105, factura.getDescuento_porcentaje(), factura.getRecargo_porcentaje());            
            if (factura.getIva_21_neto().compareTo(ivaNeto21) != 0) {
                throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                        .getString("mensaje_factura_iva21_no_valido"));
            }            
            if (factura.getIva_105_neto().compareTo(ivaNeto105) != 0) {
                throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                        .getString("mensaje_factura_iva105_no_valido"));
            }
        }
        //Total
        BigDecimal total = this.calcularTotal(factura.getSubTotal_bruto(), factura.getIva_105_neto(), factura.getIva_21_neto());
        if (factura.getTotal().compareTo(total) != 0 || factura.getTotal().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_factura_total_no_valido"));
        }
    }

    @Override
    @Transactional
    public FacturaVenta autorizarFacturaVenta(FacturaVenta fv) {
        ComprobanteAFIP comprobante = ComprobanteAFIP.builder()
                .fecha(fv.getFecha())
                .tipoComprobante(fv.getTipoComprobante())
                .CAE(fv.getCAE())
                .vencimientoCAE(fv.getVencimientoCAE())
                .numSerieAfip(fv.getNumSerieAfip())
                .numFacturaAfip(fv.getNumFacturaAfip())
                .empresa(fv.getEmpresa())
                .cliente(fv.getCliente())
                .subtotalBruto(fv.getSubTotal_bruto())
                .iva105neto(fv.getIva_105_neto())
                .iva21neto(fv.getIva_21_neto())
                .total(fv.getTotal())
                .build();
        afipService.autorizar(comprobante);
        fv.setCAE(comprobante.getCAE());
        fv.setVencimientoCAE(comprobante.getVencimientoCAE());
        fv.setNumSerieAfip(comprobante.getNumSerieAfip());
        fv.setNumFacturaAfip(comprobante.getNumFacturaAfip());
        return fv;
    }
    
    @Override
    @Transactional
    public Factura actualizarFacturaEstadoPago(Factura factura) {
        if (this.getTotalPagado(factura.getId_Factura()).setScale(2, RoundingMode.HALF_UP) 
                .compareTo(factura.getTotal().setScale(2, RoundingMode.HALF_UP)) >= 0) {                
            factura.setPagada(true);
        } else {
            factura.setPagada(false);
        }
        return factura;
    }
    
    @Override
    public BigDecimal getTotalPagado(long idFactura) {
        BigDecimal total = pagoService.getTotalPagosDeLaFactura(idFactura);
        return (total != null) ? total : BigDecimal.ZERO;      
    }
    
    @Override
    public List<Factura> ordenarFacturasPorFechaAsc(List<Factura> facturas) {
        Comparator comparador = (Comparator<Factura>) (Factura f1, Factura f2) -> f1.getFecha().compareTo(f2.getFecha());
        facturas.sort(comparador);
        return facturas;
    }

    @Override
    public boolean validarFacturasParaPagoMultiple(List<Factura> facturas, Movimiento movimiento) {
        return (this.validarClienteProveedorParaPagosMultiples(facturas, movimiento)
                && this.validarFacturasImpagasParaPagoMultiple(facturas));
    }

    @Override
    public boolean validarClienteProveedorParaPagosMultiples(List<Factura> facturas, Movimiento movimiento) {
        boolean resultado = true;
        if (movimiento == Movimiento.VENTA) {
            if (facturas != null) {
                if (facturas.isEmpty()) {
                    resultado = false;
                } else {
                    Cliente cliente = ((FacturaVenta) facturas.get(0)).getCliente();
                    for (Factura factura : facturas) {
                        if (!cliente.equals(((FacturaVenta) factura).getCliente())) {
                            resultado = false;
                            break;
                        }
                    }
                }
            } else {
                resultado = false;
            }
        }
        if (movimiento == Movimiento.COMPRA) {
            if (facturas != null) {
                if (facturas.isEmpty()) {
                    resultado = false;
                } else {
                    Proveedor proveedor = ((FacturaCompra) facturas.get(0)).getProveedor();
                    for (Factura factura : facturas) {
                        if (!proveedor.equals(((FacturaCompra) factura).getProveedor())) {
                            resultado = false;
                            break;
                        }
                    }
                }
            } else {
                resultado = false;
            }
        }
        return resultado;
    }

    @Override
    public boolean validarFacturasImpagasParaPagoMultiple(List<Factura> facturas) {
        boolean resultado = true;
        if (facturas != null) {
            if (facturas.isEmpty()) {
                resultado = false;
            } else {
                for (Factura factura : facturas) {
                    if (factura.isPagada()) {
                        resultado = false;
                        break;
                    }
                }
            }
        } else {
            resultado = false;
        }
        return resultado;
    }

    @Override
    public boolean validarCantidadMaximaDeRenglones(int cantidad, Empresa empresa) {
        ConfiguracionDelSistema cds = configuracionDelSistemaService.getConfiguracionDelSistemaPorEmpresa(empresa);
        int max = cds.getCantidadMaximaDeRenglonesEnFactura();
        return cantidad < max;
    }

    @Override
    public BigDecimal calcularSubTotal(BigDecimal[] importes) {
        BigDecimal resultado = BigDecimal.ZERO;
        for (BigDecimal importe : importes) {
            resultado = resultado.add(importe);
        }
        return resultado;
    }

    @Override
    public BigDecimal calcularDescuentoNeto(BigDecimal importe, BigDecimal descuento_porcentaje) {
        BigDecimal resultado = BigDecimal.ZERO;
        if (descuento_porcentaje != BigDecimal.ZERO) {
            resultado = importe.multiply(descuento_porcentaje).divide(CIEN, 16, RoundingMode.HALF_UP);
        }
        return resultado;
    }

    @Override
    public BigDecimal calcularRecargoNeto(BigDecimal subtotal, BigDecimal recargo_porcentaje) {
        BigDecimal resultado = BigDecimal.ZERO;
        if (recargo_porcentaje != BigDecimal.ZERO) {
            resultado = subtotal.multiply(recargo_porcentaje).divide(CIEN, 16, RoundingMode.HALF_UP);
        }
        return resultado;
    }

    @Override
    public BigDecimal calcularSubTotalBruto(TipoDeComprobante tipo, BigDecimal subTotal,
            BigDecimal recargoNeto, BigDecimal descuentoNeto, BigDecimal iva105Neto, BigDecimal iva21Neto) {
        
        BigDecimal resultado = subTotal.add(recargoNeto).subtract(descuentoNeto);
        if (tipo == TipoDeComprobante.FACTURA_B || tipo == TipoDeComprobante.PRESUPUESTO) {
            resultado = resultado.subtract(iva105Neto.add(iva21Neto));
        }
        return resultado;
    }

    @Override
    public BigDecimal calcularImpInternoNeto(TipoDeComprobante tipoDeComprobante, BigDecimal descuento_porcentaje,
            BigDecimal recargo_porcentaje, BigDecimal[] importes, BigDecimal [] impuestoPorcentajes) {

        BigDecimal resultado = BigDecimal.ZERO;
        if (tipoDeComprobante == TipoDeComprobante.FACTURA_A || tipoDeComprobante == TipoDeComprobante.FACTURA_B || tipoDeComprobante == TipoDeComprobante.PRESUPUESTO) {
            int longitudImportes = importes.length;
            int longitudImpuestos = impuestoPorcentajes.length;
            if (longitudImportes == longitudImpuestos) {
                for (int i = 0; i < longitudImportes; i++) {                
                BigDecimal descuento = BigDecimal.ZERO;
                if (descuento_porcentaje != BigDecimal.ZERO) {
                    descuento = importes[i].multiply(descuento_porcentaje).divide(CIEN, 16, RoundingMode.HALF_UP);
                }
                BigDecimal recargo = BigDecimal.ZERO;
                if (recargo_porcentaje != BigDecimal.ZERO) {
                    recargo = importes[i].multiply(recargo_porcentaje).divide(CIEN, 16, RoundingMode.HALF_UP);
                }
                BigDecimal impInterno_neto = BigDecimal.ZERO;
                impInterno_neto = impInterno_neto.add(importes[i]).add(recargo).subtract(descuento).multiply(impuestoPorcentajes[i]).divide(CIEN, 16, RoundingMode.HALF_UP);
                resultado = resultado.add(impInterno_neto);
                }
            }
        }
        return resultado;
    }

    @Override
    public BigDecimal calcularTotal(BigDecimal subTotalBruto, BigDecimal iva105Neto, BigDecimal iva21Neto) {
        return subTotalBruto.add(iva105Neto).add(iva21Neto);        
    }

    @Override
    public BigDecimal calcularTotalFacturadoVenta(BusquedaFacturaVentaCriteria criteria) {
        //Empresa
        if(criteria.getEmpresa() == null ) {
            throw new EntityNotFoundException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_empresa_no_existente"));
        }
        //Fecha de Factura        
        if (criteria.isBuscaPorFecha() == true && (criteria.getFechaDesde() == null || criteria.getFechaHasta() == null)) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_factura_fechas_busqueda_invalidas"));
        }
        if (criteria.isBuscaPorFecha() == true) {
            Calendar cal = new GregorianCalendar();
            cal.setTime(criteria.getFechaDesde());
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            criteria.setFechaDesde(cal.getTime());
            cal.setTime(criteria.getFechaHasta());
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            criteria.setFechaHasta(cal.getTime());
        }
        //Cliente
        if (criteria.isBuscaCliente() == true && criteria.getCliente() == null) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_factura_cliente_vacio"));
        }
        //Usuario
        if (criteria.isBuscaUsuario() == true && criteria.getUsuario() == null) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_factura_usuario_vacio"));
        }
        if (criteria.isBuscaViajante() == true && criteria.getViajante() == null) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_factura_viajante_vacio"));
        }
        return facturaVentaRepository.calcularTotalFacturadoVenta(criteria);
    }
    
    @Override
    public BigDecimal getSaldoFacturasVentaSegunClienteYEmpresa(long empresa, long cliente, Date hasta) {
        BigDecimal saldo = facturaVentaRepository.getSaldoFacturasVentaSegunClienteYEmpresa(empresa, cliente, hasta);
        return (saldo == null) ? BigDecimal.ZERO : saldo;
    }

    @Override
    public BigDecimal  calcularTotalFacturadoCompra(BusquedaFacturaCompraCriteria criteria) {
        //Empresa
        if (criteria.getEmpresa() == null) {
            throw new EntityNotFoundException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_empresa_no_existente"));
        }
        //Fecha de Factura        
        if (criteria.isBuscaPorFecha() == true & (criteria.getFechaDesde() == null | criteria.getFechaHasta() == null)) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_factura_fechas_busqueda_invalidas"));
        }
        if (criteria.isBuscaPorFecha() == true) {
            Calendar cal = new GregorianCalendar();
            cal.setTime(criteria.getFechaDesde());
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            criteria.setFechaDesde(cal.getTime());
            cal.setTime(criteria.getFechaHasta());
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            criteria.setFechaHasta(cal.getTime());
        }
        //Proveedor
        if (criteria.isBuscaPorProveedor() == true && criteria.getProveedor() == null) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_factura_proveedor_vacio"));
        }
        return facturaCompraRepository.calcularTotalFacturadoCompra(criteria);
    }

    @Override
    public BigDecimal calcularIvaVenta(BusquedaFacturaVentaCriteria criteria) {
        //Empresa
        if(criteria.getEmpresa() == null ) {
            throw new EntityNotFoundException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_empresa_no_existente"));
        }
        //Fecha de Factura        
        if (criteria.isBuscaPorFecha() == true && (criteria.getFechaDesde() == null || criteria.getFechaHasta() == null)) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_factura_fechas_busqueda_invalidas"));
        }
        if (criteria.isBuscaPorFecha() == true) {
            Calendar cal = new GregorianCalendar();
            cal.setTime(criteria.getFechaDesde());
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            criteria.setFechaDesde(cal.getTime());
            cal.setTime(criteria.getFechaHasta());
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            criteria.setFechaHasta(cal.getTime());
        }
        //Cliente
        if (criteria.isBuscaCliente() == true && criteria.getCliente() == null) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_factura_cliente_vacio"));
        }
        //Usuario
        if (criteria.isBuscaUsuario() == true && criteria.getUsuario() == null) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_factura_usuario_vacio"));
        }
        if (criteria.isBuscaViajante() == true && criteria.getViajante() == null) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_factura_viajante_vacio"));
        }
        TipoDeComprobante[] tipoFactura = {TipoDeComprobante.FACTURA_A, TipoDeComprobante.FACTURA_B};
        return facturaVentaRepository.calcularIVA_Venta(criteria, tipoFactura);
    }

    @Override
    public BigDecimal calcularIvaCompra(BusquedaFacturaCompraCriteria criteria) {
        //Empresa
        if (criteria.getEmpresa() == null) {
            throw new EntityNotFoundException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_empresa_no_existente"));
        }
        //Fecha de Factura        
        if (criteria.isBuscaPorFecha() == true & (criteria.getFechaDesde() == null | criteria.getFechaHasta() == null)) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_factura_fechas_busqueda_invalidas"));
        }
        if (criteria.isBuscaPorFecha() == true) {
            Calendar cal = new GregorianCalendar();
            cal.setTime(criteria.getFechaDesde());
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            criteria.setFechaDesde(cal.getTime());
            cal.setTime(criteria.getFechaHasta());
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            criteria.setFechaHasta(cal.getTime());
        }
        //Proveedor
        if (criteria.isBuscaPorProveedor() == true && criteria.getProveedor() == null) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_factura_proveedor_vacio"));
        }
        TipoDeComprobante[] tipoFactura = {TipoDeComprobante.FACTURA_A};
        return facturaCompraRepository.calcularIVA_Compra(criteria, tipoFactura);
    }

    @Override
    public BigDecimal calcularGananciaTotal(BusquedaFacturaVentaCriteria criteria) {
        //Empresa
        if (criteria.getEmpresa() == null) {
            throw new EntityNotFoundException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_empresa_no_existente"));
        }
        //Fecha de Factura        
        if (criteria.isBuscaPorFecha() == true && (criteria.getFechaDesde() == null || criteria.getFechaHasta() == null)) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_factura_fechas_busqueda_invalidas"));
        }
        if (criteria.isBuscaPorFecha() == true) {
            Calendar cal = new GregorianCalendar();
            cal.setTime(criteria.getFechaDesde());
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            criteria.setFechaDesde(cal.getTime());
            cal.setTime(criteria.getFechaHasta());
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            criteria.setFechaHasta(cal.getTime());
        }
        //Cliente
        if (criteria.isBuscaCliente() == true && criteria.getCliente() == null) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_factura_cliente_vacio"));
        }
        //Usuario
        if (criteria.isBuscaUsuario() == true && criteria.getUsuario() == null) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_factura_usuario_vacio"));
        }
        if (criteria.isBuscaViajante() == true && criteria.getViajante() == null) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_factura_viajante_vacio"));
        }
        return facturaVentaRepository.calcularGananciaTotal(criteria);
    }

    @Override    
    public BigDecimal calcularIVANetoRenglon(Movimiento movimiento, TipoDeComprobante tipo, Producto producto, BigDecimal descuento_porcentaje) {
        BigDecimal resultado = BigDecimal.ZERO;
        if (movimiento == Movimiento.COMPRA) {
            if (tipo == TipoDeComprobante.FACTURA_A || tipo == TipoDeComprobante.FACTURA_B) {
                resultado = producto.getPrecioCosto().multiply(BigDecimal.ONE.subtract(descuento_porcentaje.divide(CIEN)).multiply(producto.getIva_porcentaje().divide(CIEN)));
            }
        } else if (movimiento == Movimiento.VENTA) {
            if (tipo == TipoDeComprobante.FACTURA_A || tipo == TipoDeComprobante.FACTURA_B || tipo == TipoDeComprobante.PRESUPUESTO) {
                resultado = producto.getPrecioVentaPublico().multiply(BigDecimal.ONE.subtract(descuento_porcentaje.divide(CIEN)).multiply(producto.getIva_porcentaje().divide(CIEN)));
            }
        }                               
        return resultado;
    }
    
    @Override
    public BigDecimal calcularIvaNetoFactura(TipoDeComprobante tipo, BigDecimal[] cantidades, BigDecimal[] ivaPorcentajeRenglones,
            BigDecimal[] ivaNetoRenglones, BigDecimal ivaPorcentaje, BigDecimal descuentoPorcentaje, BigDecimal recargoPorcentaje) {
        BigDecimal resultado = BigDecimal.ZERO;
        int indice = cantidades.length;
        for (int i = 0; i < indice; i++) {
            if (ivaPorcentajeRenglones[i].compareTo(ivaPorcentaje) == 0) {
                if (tipo == TipoDeComprobante.FACTURA_A || tipo == TipoDeComprobante.FACTURA_B
                        || tipo == TipoDeComprobante.FACTURA_C || tipo == TipoDeComprobante.PRESUPUESTO) {
                    resultado = resultado.add(cantidades[i].multiply(ivaNetoRenglones[i]
                            .subtract(ivaNetoRenglones[i].multiply(descuentoPorcentaje.divide(CIEN)))
                            .add(ivaNetoRenglones[i].multiply(recargoPorcentaje.divide(CIEN)))));
                } else {
                    resultado = resultado.add(cantidades[i].multiply(ivaNetoRenglones[i]));
                }
            }
        }
        return resultado;
    }

    @Override
    public BigDecimal calcularImpInternoNeto(Movimiento movimiento, Producto producto, BigDecimal descuento_neto) {
        BigDecimal resultado = BigDecimal.ZERO;
        if (movimiento == Movimiento.COMPRA) {
            resultado = producto.getPrecioCosto().subtract(descuento_neto).multiply(producto.getImpuestoInterno_porcentaje()).divide(CIEN);
        } 
        if (movimiento == Movimiento.VENTA) {
            resultado = producto.getPrecioVentaPublico().subtract(descuento_neto).multiply(producto.getImpuestoInterno_porcentaje()).divide(CIEN);
        }
        return resultado;
    }

    @Override
    public BigDecimal calcularPrecioUnitario(Movimiento movimiento, TipoDeComprobante tipoDeComprobante, Producto producto) {
        BigDecimal iva_resultado;
        BigDecimal impInterno_resultado;
        BigDecimal resultado = BigDecimal.ZERO;
        if (movimiento == Movimiento.COMPRA) {
            if (tipoDeComprobante.equals(TipoDeComprobante.FACTURA_A) || tipoDeComprobante.equals(TipoDeComprobante.FACTURA_X)) {
                resultado = producto.getPrecioCosto();
            } else {
                iva_resultado = producto.getPrecioCosto().multiply(producto.getIva_porcentaje()).divide(CIEN);
                impInterno_resultado = producto.getPrecioCosto().multiply(producto.getImpuestoInterno_porcentaje()).divide(CIEN);
                resultado = producto.getPrecioCosto().add(iva_resultado).add(impInterno_resultado);
            }
        }
        if (movimiento == Movimiento.VENTA) {
            switch (tipoDeComprobante) {
                case FACTURA_A:
                case FACTURA_X:
                    resultado = producto.getPrecioVentaPublico();
                    break;
                case FACTURA_Y:
                    iva_resultado = producto.getPrecioVentaPublico().multiply(producto.getIva_porcentaje()).divide(new BigDecimal(2)).divide(CIEN);
                    impInterno_resultado = producto.getPrecioVentaPublico().multiply(producto.getImpuestoInterno_porcentaje()).divide(CIEN);
                    resultado = producto.getPrecioVentaPublico().add(iva_resultado).add(impInterno_resultado);
                    break;
                default:
                    resultado = producto.getPrecioLista();
                    break;
            }
        }
        if (movimiento == Movimiento.PEDIDO) {
            resultado = producto.getPrecioLista();
        }
        return resultado;
    }

    @Override
    public long calcularNumeroFacturaVenta(TipoDeComprobante tipoDeComprobante, long serie, long idEmpresa) {
        Long numeroFactura = facturaVentaRepository.buscarMayorNumFacturaSegunTipo(tipoDeComprobante, serie, idEmpresa);
        if (numeroFactura == null) {
            return 1; // No existe ninguna Factura anterior
        } else {
            return 1 + numeroFactura;
        }
    }

    @Override
    public BigDecimal calcularImporte(BigDecimal cantidad, BigDecimal precioUnitario, BigDecimal descuento_neto) {
        return (precioUnitario.subtract(descuento_neto)).multiply(cantidad);
    }

    @Override
    public byte[] getReporteFacturaVenta(Factura factura) {
        ClassLoader classLoader = FacturaServiceImpl.class.getClassLoader();
        InputStream isFileReport = classLoader.getResourceAsStream("sic/vista/reportes/FacturaVenta.jasper");
        Map params = new HashMap();
        ConfiguracionDelSistema cds = configuracionDelSistemaService.getConfiguracionDelSistemaPorEmpresa(factura.getEmpresa());
        params.put("preImpresa", cds.isUsarFacturaVentaPreImpresa());
        String formasDePago = "";
        formasDePago = pagoService.getPagosDeLaFactura(factura.getId_Factura())
                                  .stream()
                                  .map((pago) -> pago.getFormaDePago().getNombre() + " -")
                                  .reduce(formasDePago, String::concat);
        params.put("formasDePago", formasDePago);
        if (factura.getTipoComprobante().equals(TipoDeComprobante.FACTURA_B) || factura.getTipoComprobante().equals(TipoDeComprobante.PRESUPUESTO)) {
            factura.setSubTotal_bruto(factura.getTotal());
            factura.setIva_105_neto(BigDecimal.ZERO);
            factura.setIva_21_neto(BigDecimal.ZERO);
        }
        params.put("facturaVenta", factura);
        if (factura.getTipoComprobante().equals(TipoDeComprobante.FACTURA_A) || factura.getTipoComprobante().equals(TipoDeComprobante.FACTURA_B)
                || factura.getTipoComprobante().equals(TipoDeComprobante.FACTURA_C)) {
            if (factura.getNumSerieAfip() != 0 && factura.getNumFacturaAfip() != 0) {
                params.put("nroSerie", factura.getNumSerieAfip());
                params.put("nroFactura", factura.getNumFacturaAfip());
            } else {
                params.put("nroSerie", null);
                params.put("nroFactura", null);
            }
        } else {
            params.put("nroSerie", factura.getNumSerie());
            params.put("nroFactura", factura.getNumFactura());
        }
        if (!factura.getEmpresa().getLogo().isEmpty()) {
            try {
                params.put("logo", new ImageIcon(ImageIO.read(new URL(factura.getEmpresa().getLogo()))).getImage());
            } catch (IOException ex) {
                LOGGER.error(ex.getMessage());
                throw new ServiceException(ResourceBundle.getBundle("Mensajes")
                        .getString("mensaje_empresa_404_logo"), ex);
            }
        }
        List<RenglonFactura> renglones = this.getRenglonesDeLaFactura(factura.getId_Factura());
        JRBeanCollectionDataSource ds = new JRBeanCollectionDataSource(renglones);
         try {
            return JasperExportManager.exportReportToPdf(JasperFillManager.fillReport(isFileReport, params, ds));
        } catch (JRException ex) {
            LOGGER.error(ex.getMessage());
            throw new ServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_error_reporte"), ex);
        }
    }

    @Override
    public List<RenglonFactura> convertirRenglonesPedidoEnRenglonesFactura(Pedido pedido, TipoDeComprobante tipoDeComprobante) {
        List<RenglonFactura> renglonesRestantes = new ArrayList<>();
        HashMap<Long, RenglonFactura> renglonesDeFacturas = pedidoService.getRenglonesFacturadosDelPedido(pedido.getId_Pedido());
        if (renglonesDeFacturas != null) {
            pedido.getRenglones().stream().forEach(r -> {
                if (renglonesDeFacturas.containsKey(r.getProducto().getId_Producto())) {
                    if (r.getCantidad().compareTo(renglonesDeFacturas.get(r.getProducto().getId_Producto()).getCantidad()) > 0) {
                        renglonesRestantes.add(this.calcularRenglon(tipoDeComprobante,
                                Movimiento.VENTA, r.getCantidad().subtract(renglonesDeFacturas.get(r.getProducto().getId_Producto()).getCantidad()),
                                r.getProducto().getId_Producto(), r.getDescuento_porcentaje(), false));
                    }
                } else {
                    renglonesRestantes.add(this.calcularRenglon(tipoDeComprobante, Movimiento.VENTA,
                            r.getCantidad(), r.getProducto().getId_Producto(), r.getDescuento_porcentaje(), false));
                }
            });
        } else {
            pedido.getRenglones().stream().forEach(r -> {
                renglonesRestantes.add(this.calcularRenglon(tipoDeComprobante, Movimiento.VENTA,
                        r.getCantidad(), r.getProducto().getId_Producto(), r.getDescuento_porcentaje(), false));
            });
        }
        return renglonesRestantes;
    }

    @Override
    public boolean pedidoTotalmenteFacturado(Pedido pedido) {
        boolean facturado = false;
        HashMap<Long, RenglonFactura> renglonesDeFacturas = pedidoService.getRenglonesFacturadosDelPedido(pedido.getId_Pedido());
        if (!renglonesDeFacturas.isEmpty()) {
            for (RenglonPedido r : pedido.getRenglones()) {
                if (renglonesDeFacturas.containsKey(r.getProducto().getId_Producto())) {
                    facturado = (r.getCantidad().compareTo(renglonesDeFacturas.get(r.getProducto().getId_Producto()).getCantidad()) < 1);                   
                } else {
                    return false;
                }
            }
        }
        return facturado;
    }
    
    @Override
    public RenglonFactura calcularRenglon(TipoDeComprobante tipoDeComprobante, Movimiento movimiento,
            BigDecimal cantidad, long idProducto, BigDecimal descuentoPorcentaje, boolean dividiendoRenglonFactura) {
        Producto producto = productoService.getProductoPorId(idProducto);
        /*if (dividiendoRenglonFactura == false && cantidad < producto.getVentaMinima()
                && (movimiento == Movimiento.VENTA || movimiento == Movimiento.PEDIDO)) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_producto_cantidad_menor_a_minima"));
        }*/
        RenglonFactura nuevoRenglon = new RenglonFactura();
        nuevoRenglon.setId_ProductoItem(producto.getId_Producto());
        nuevoRenglon.setCodigoItem(producto.getCodigo());
        nuevoRenglon.setDescripcionItem(producto.getDescripcion());
        nuevoRenglon.setMedidaItem(producto.getMedida().getNombre());
        nuevoRenglon.setCantidad(cantidad);
        nuevoRenglon.setPrecioUnitario(this.calcularPrecioUnitario(movimiento, tipoDeComprobante, producto));
        if (descuentoPorcentaje.compareTo(CIEN) > 0) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_descuento_mayor_cien"));
        }
        nuevoRenglon.setDescuento_porcentaje(descuentoPorcentaje);
        nuevoRenglon.setDescuento_neto(this.calcularDescuentoNeto(nuevoRenglon.getPrecioUnitario(), descuentoPorcentaje));
        nuevoRenglon.setIva_porcentaje(producto.getIva_porcentaje());
        if (tipoDeComprobante.equals(TipoDeComprobante.FACTURA_Y)) {
            nuevoRenglon.setIva_porcentaje(producto.getIva_porcentaje().divide(new BigDecimal(2)));
        }
        nuevoRenglon.setIva_neto(this.calcularIVANetoRenglon(movimiento, tipoDeComprobante, producto, nuevoRenglon.getDescuento_porcentaje()));
        nuevoRenglon.setImpuesto_porcentaje(producto.getImpuestoInterno_porcentaje());
        nuevoRenglon.setImpuesto_neto(this.calcularImpInternoNeto(movimiento, producto, nuevoRenglon.getDescuento_neto()));
        nuevoRenglon.setGanancia_porcentaje(producto.getGanancia_porcentaje());
        nuevoRenglon.setGanancia_neto(producto.getGanancia_neto());
        nuevoRenglon.setImporte(this.calcularImporte(cantidad, nuevoRenglon.getPrecioUnitario(), nuevoRenglon.getDescuento_neto()));
        return nuevoRenglon;
    }

    @Override
    public List<Factura> dividirFactura(FacturaVenta facturaADividir, int[] indices) {
        FacturaVenta facturaSinIVA = new FacturaVenta();
        facturaSinIVA.setCliente(facturaADividir.getCliente());
        facturaSinIVA.setUsuario(facturaADividir.getUsuario());
        facturaSinIVA.setPedido(facturaADividir.getPedido());
        facturaSinIVA.setDescuento_porcentaje(facturaADividir.getDescuento_porcentaje());
        facturaSinIVA.setRecargo_porcentaje(facturaADividir.getRecargo_porcentaje());
        FacturaVenta facturaConIVA = new FacturaVenta();
        facturaConIVA.setCliente(facturaADividir.getCliente());
        facturaConIVA.setUsuario(facturaADividir.getUsuario());
        facturaConIVA.setPedido(facturaADividir.getPedido());
        facturaConIVA.setTipoComprobante(facturaADividir.getTipoComprobante());
        facturaConIVA.setDescuento_porcentaje(facturaADividir.getDescuento_porcentaje());
        facturaConIVA.setRecargo_porcentaje(facturaADividir.getRecargo_porcentaje());
        List<Factura> facturas = new ArrayList<>();
        facturaSinIVA = this.agregarRenglonesAFacturaSinIVA(facturaSinIVA, indices, facturaADividir.getRenglones());
        facturaConIVA = this.agregarRenglonesAFacturaConIVA(facturaConIVA, indices,facturaADividir.getRenglones());
        if (facturaSinIVA.getRenglones().size() > 0) {
            facturaSinIVA = this.procesarFacturaSinIVA(facturaADividir, facturaSinIVA);
            facturas.add(facturaSinIVA);
        }
        facturaConIVA = this.procesarFacturaConIVA(facturaADividir, facturaConIVA);
        facturas.add(facturaConIVA);
        return facturas;
    }
    
    private FacturaVenta procesarFacturaSinIVA(FacturaVenta facturaADividir, FacturaVenta facturaSinIVA) {
        int size = facturaSinIVA.getRenglones().size();
        BigDecimal[] importes = new BigDecimal[size];
        BigDecimal[] cantidades = new BigDecimal[size];
        BigDecimal[] ivaPorcentajeRenglones = new BigDecimal[size];
        BigDecimal[] ivaNetoRenglones = new BigDecimal[size];
        int indice = 0;
        List<RenglonFactura> listRenglonesSinIVA = new ArrayList<>(facturaSinIVA.getRenglones());
        facturaSinIVA.setFecha(facturaADividir.getFecha());
        facturaSinIVA.setTipoComprobante(TipoDeComprobante.FACTURA_X);
        facturaSinIVA.setFechaVencimiento(facturaADividir.getFechaVencimiento());
        facturaSinIVA.setTransportista(facturaADividir.getTransportista());
        facturaSinIVA.setRenglones(listRenglonesSinIVA);
        for (RenglonFactura renglon : facturaSinIVA.getRenglones()) {
            importes[indice] = renglon.getImporte();
            cantidades[indice] = renglon.getCantidad();
            ivaPorcentajeRenglones[indice] = renglon.getIva_porcentaje();
            ivaNetoRenglones[indice] = renglon.getIva_neto();
            indice++;
        }
        facturaSinIVA.setSubTotal(this.calcularSubTotal(importes));
        facturaSinIVA.setDescuento_neto(this.calcularDescuentoNeto(facturaSinIVA.getSubTotal(), facturaSinIVA.getDescuento_porcentaje()));
        facturaSinIVA.setRecargo_neto(this.calcularRecargoNeto(facturaSinIVA.getSubTotal(), facturaSinIVA.getRecargo_porcentaje()));
        facturaSinIVA.setIva_105_neto(this.calcularIvaNetoFactura(facturaSinIVA.getTipoComprobante(), cantidades,
                ivaPorcentajeRenglones, ivaNetoRenglones, IVA_105, facturaADividir.getDescuento_porcentaje(), facturaADividir.getRecargo_porcentaje()));
        facturaSinIVA.setIva_21_neto(this.calcularIvaNetoFactura(facturaSinIVA.getTipoComprobante(), cantidades,
                ivaPorcentajeRenglones, ivaNetoRenglones, IVA_21, facturaADividir.getDescuento_porcentaje(), facturaADividir.getRecargo_porcentaje()));
        facturaSinIVA.setSubTotal_bruto(this.calcularSubTotalBruto(facturaSinIVA.getTipoComprobante(), facturaSinIVA.getSubTotal(),
                facturaSinIVA.getRecargo_neto(), facturaSinIVA.getDescuento_neto(), facturaSinIVA.getIva_105_neto(), facturaSinIVA.getIva_21_neto()));
        facturaSinIVA.setTotal(this.calcularTotal(facturaSinIVA.getSubTotal_bruto(), facturaSinIVA.getIva_105_neto(), facturaSinIVA.getIva_21_neto()));
        facturaSinIVA.setObservaciones(facturaADividir.getObservaciones());
        facturaSinIVA.setPagada(facturaADividir.isPagada());
        facturaSinIVA.setEmpresa(facturaADividir.getEmpresa());
        facturaSinIVA.setEliminada(facturaADividir.isEliminada());
        return facturaSinIVA;
    }

    private FacturaVenta procesarFacturaConIVA(FacturaVenta facturaADividir, FacturaVenta facturaConIVA) {
        int size = facturaConIVA.getRenglones().size();
        BigDecimal[] importes = new BigDecimal[size];
        BigDecimal[] cantidades = new BigDecimal[size];
        BigDecimal[] ivaPorcentajeRenglones = new BigDecimal[size];
        BigDecimal[] ivaNetoRenglones = new BigDecimal[size];
        int indice = 0;
        List<RenglonFactura> listRenglonesConIVA = new ArrayList<>(facturaConIVA.getRenglones());
        facturaConIVA.setFecha(facturaADividir.getFecha());
        facturaConIVA.setTipoComprobante(facturaADividir.getTipoComprobante());
        facturaConIVA.setFechaVencimiento(facturaADividir.getFechaVencimiento());
        facturaConIVA.setTransportista(facturaADividir.getTransportista());
        facturaConIVA.setRenglones(listRenglonesConIVA);
        for (RenglonFactura renglon : facturaConIVA.getRenglones()) {
            importes[indice] = renglon.getImporte();
            cantidades[indice] = renglon.getCantidad();
            ivaPorcentajeRenglones[indice] = renglon.getIva_porcentaje();
            ivaNetoRenglones[indice] = renglon.getIva_neto();
            indice++;
        }
        facturaConIVA.setSubTotal(this.calcularSubTotal(importes));
        facturaConIVA.setDescuento_neto(this.calcularDescuentoNeto(facturaConIVA.getSubTotal(), facturaConIVA.getDescuento_porcentaje()));
        facturaConIVA.setRecargo_neto(this.calcularRecargoNeto(facturaConIVA.getSubTotal(), facturaConIVA.getRecargo_porcentaje()));
        facturaConIVA.setIva_105_neto(this.calcularIvaNetoFactura(facturaConIVA.getTipoComprobante(), cantidades,
                ivaPorcentajeRenglones, ivaNetoRenglones, IVA_105, facturaADividir.getDescuento_porcentaje(), facturaADividir.getRecargo_porcentaje()));
        facturaConIVA.setIva_21_neto(this.calcularIvaNetoFactura(facturaConIVA.getTipoComprobante(), cantidades,
                ivaPorcentajeRenglones, ivaNetoRenglones, IVA_21, facturaADividir.getDescuento_porcentaje(), facturaADividir.getRecargo_porcentaje()));
        facturaConIVA.setSubTotal_bruto(this.calcularSubTotalBruto(facturaConIVA.getTipoComprobante(), facturaConIVA.getSubTotal(),
                facturaConIVA.getRecargo_neto(), facturaConIVA.getDescuento_neto(), facturaConIVA.getIva_105_neto(), facturaConIVA.getIva_21_neto()));
        facturaConIVA.setTotal(this.calcularTotal(facturaConIVA.getSubTotal_bruto(), facturaConIVA.getIva_105_neto(), facturaConIVA.getIva_21_neto()));
        facturaConIVA.setObservaciones(facturaADividir.getObservaciones());
        facturaConIVA.setPagada(facturaADividir.isPagada());
        facturaConIVA.setEmpresa(facturaADividir.getEmpresa());
        facturaConIVA.setEliminada(facturaADividir.isEliminada());
        return facturaConIVA;
    }

    private FacturaVenta agregarRenglonesAFacturaSinIVA(FacturaVenta facturaSinIVA, int[] indices, List<RenglonFactura> renglones) {
        List<RenglonFactura> renglonesSinIVA = new ArrayList<>();
        BigDecimal cantidadProductosRenglonFacturaSinIVA = BigDecimal.ZERO;
        int renglonMarcado = 0;
        int numeroDeRenglon = 0;
        for (RenglonFactura renglon : renglones) {
            if (numeroDeRenglon == indices[renglonMarcado]) {
                BigDecimal cantidad = renglon.getCantidad();
                if (cantidad.compareTo(BigDecimal.ONE) >= 0) {
                    if ((cantidad.remainder(BigDecimal.ONE) != BigDecimal.ZERO) || cantidad.remainder(new BigDecimal(2)) == BigDecimal.ZERO) {
                        cantidadProductosRenglonFacturaSinIVA = cantidad.divide(new BigDecimal(2));
                    } else if (cantidad.remainder(new BigDecimal(2)).compareTo(BigDecimal.ZERO) != 0) {
                        cantidadProductosRenglonFacturaSinIVA = cantidad.subtract(cantidad.divide(new BigDecimal(2)).setScale(0, RoundingMode.CEILING));
                    }
                } else {
                    cantidadProductosRenglonFacturaSinIVA = BigDecimal.ZERO;
                }
                RenglonFactura nuevoRenglonSinIVA = this.calcularRenglon(TipoDeComprobante.FACTURA_X, Movimiento.VENTA, 
                            cantidadProductosRenglonFacturaSinIVA, renglon.getId_ProductoItem(),
                            renglon.getDescuento_porcentaje(), true);
                if (nuevoRenglonSinIVA.getCantidad().compareTo(BigDecimal.ZERO) != 0) {
                    renglonesSinIVA.add(nuevoRenglonSinIVA);
                }
                numeroDeRenglon++;
                renglonMarcado++;
            } else {
                numeroDeRenglon++;
            }
        }
        facturaSinIVA.setRenglones(renglonesSinIVA);
        return facturaSinIVA;
    }

    private FacturaVenta agregarRenglonesAFacturaConIVA(FacturaVenta facturaConIVA, int[] indices,  List<RenglonFactura> renglones) {
        List<RenglonFactura> renglonesConIVA = new ArrayList<>();
        BigDecimal cantidadProductosRenglonFacturaConIVA = BigDecimal.ZERO;
        int renglonMarcado = 0;
        int numeroDeRenglon = 0;
        for (RenglonFactura renglon : renglones) {
            if (numeroDeRenglon == indices[renglonMarcado]) {
                BigDecimal cantidad = renglon.getCantidad();
                if (cantidad.compareTo(BigDecimal.ONE) == -1 || cantidad.compareTo(BigDecimal.ONE) == 0) { //if (cantidad < 1 || cantidad == 1) {
                    cantidadProductosRenglonFacturaConIVA = cantidad;
                } else if ((cantidad.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) != 0) || renglon.getCantidad().remainder(new BigDecimal(2)).compareTo(BigDecimal.ZERO) == 0) {
                    cantidadProductosRenglonFacturaConIVA = renglon.getCantidad().subtract(new BigDecimal(2));
                } else if (renglon.getCantidad().remainder(new BigDecimal(2)).compareTo(BigDecimal.ZERO) != 0) {
                    cantidadProductosRenglonFacturaConIVA = renglon.getCantidad().divide(new BigDecimal(2)).setScale(0, RoundingMode.CEILING);
                }
                RenglonFactura nuevoRenglonConIVA = this.calcularRenglon(facturaConIVA.getTipoComprobante(), Movimiento.VENTA,
                        cantidadProductosRenglonFacturaConIVA, renglon.getId_ProductoItem(), renglon.getDescuento_porcentaje(), true);
                renglonesConIVA.add(nuevoRenglonConIVA);
                renglonMarcado++;
                numeroDeRenglon++;
            } else {
                numeroDeRenglon++;
                RenglonFactura nuevoRenglonConIVA = this.calcularRenglon(facturaConIVA.getTipoComprobante(), Movimiento.VENTA,
                        renglon.getCantidad(), renglon.getId_ProductoItem(), renglon.getDescuento_porcentaje(), false);
                renglonesConIVA.add(nuevoRenglonConIVA);
            }
        }
        facturaConIVA.setRenglones(renglonesConIVA);
        return facturaConIVA;
    }

}

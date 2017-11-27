package sic.service.impl;

import java.io.IOException;
import sic.modelo.BusquedaFacturaCompraCriteria;
import sic.modelo.BusquedaFacturaVentaCriteria;
import java.io.InputStream;
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
import sic.modelo.TipoDeComprobante;
import sic.service.BusinessServiceException;
import sic.service.ServiceException;
import sic.modelo.TipoDeOperacion;
import sic.util.Utilidades;
import sic.util.Validator;
import sic.repository.FacturaVentaRepository;
import sic.repository.FacturaCompraRepository;
import sic.repository.FacturaRepository;
import sic.repository.RenglonFacturaRepository;
import sic.service.ICuentaCorrienteService;
import sic.service.IAfipService;
import sic.service.INotaService;

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
            IAfipService afipService) {
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
    public Double getTotalById(long idFactura) {
        Double total = facturaRepository.getTotalById(idFactura);
        return (total != null) ? total : 0.0;
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
    public List<Factura> guardar(List<Factura> facturas, Long idPedido) {
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
                List<Pago> pagosFactura = f.getPagos();
                f.setPagos(null);
                Factura facturaGuardada = facturaVentaRepository.save((FacturaVenta) this.procesarFactura(f));
                this.cuentaCorrienteService.asentarEnCuentaCorriente((FacturaVenta) facturaGuardada, TipoDeOperacion.ALTA);
                facturasProcesadas.add(facturaGuardada);
                if (pagosFactura != null) {
                    pagosFactura.forEach(p -> {
                        pagoService.guardar(p);
                    });
                    f.setPagos(pagosFactura);
                }
            }
            pedido.setFacturas(facturasProcesadas);
            pedidoService.actualizar(pedido);
            facturasProcesadas.stream().forEach(f -> {
                this.actualizarFacturaEstadoPago(f);
                LOGGER.warn("La Factura " + f + " se guardó correctamente.");
            });
            pedidoService.actualizarEstadoPedido(pedido, facturasProcesadas);
        } else {
            facturasProcesadas = new ArrayList<>();
            for (Factura f : facturas) {
                List<Pago> pagosFactura = f.getPagos();
                f.setPagos(null);
                Factura facturaGuardada = null;
                if (f instanceof FacturaVenta) {
                    facturaGuardada = facturaVentaRepository.save((FacturaVenta) this.procesarFactura(f));
                    this.cuentaCorrienteService.asentarEnCuentaCorriente((FacturaVenta) facturaGuardada, TipoDeOperacion.ALTA);
                } else if (f instanceof FacturaCompra) {
                    facturaGuardada = facturaCompraRepository.save((FacturaCompra) this.procesarFactura(f));
                }
                facturasProcesadas.add(facturaGuardada);
                LOGGER.warn("La Factura " + facturaGuardada + " se guardó correctamente.");
                if (pagosFactura != null) {
                    pagosFactura.forEach((p) -> {
                        pagoService.guardar(p);
                    });
                    f.setPagos(pagosFactura);
                }
                this.actualizarFacturaEstadoPago(facturaGuardada);
            }
        }
        return facturasProcesadas;
    }
    
    @Override
    @Transactional
    public void eliminar(long[] idsFactura) {
        for (long idFactura : idsFactura) {
            if (!pagoService.getPagosDeLaFactura(idFactura).isEmpty()) {
                throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                        .getString("mensaje_no_se_puede_eliminar"));
            }
            Factura factura = this.getFacturaPorId(idFactura);
            if (factura.getCAE() == 0L) {
                factura.setEliminada(true);
                if (factura instanceof FacturaVenta) {
                    this.cuentaCorrienteService.asentarEnCuentaCorriente((FacturaVenta) factura, TipoDeOperacion.ELIMINACION);
                    productoService.actualizarStock(this.getIdsProductosYCantidades(factura), TipoDeOperacion.ELIMINACION, Movimiento.VENTA);
                } else if (factura instanceof FacturaCompra) {
                    productoService.actualizarStock(this.getIdsProductosYCantidades(factura), TipoDeOperacion.ELIMINACION, Movimiento.COMPRA);
                }
                if (factura.getPedido() != null) {
                    List<Factura> facturas = new ArrayList<>();
                    facturas.add(factura);
                    pedidoService.actualizarEstadoPedido(factura.getPedido(), facturas);
                }
            } else {
                throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                        .getString("mensaje_eliminar_factura_aprobada"));
            }
        }  
    }
    
    private HashMap<Long, Double> getIdsProductosYCantidades(Factura factura) {
        HashMap<Long, Double> idsYCantidades = new HashMap<>();
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
        double[] importes = new double[factura.getRenglones().size()];
        int i = 0;
        for (RenglonFactura renglon : factura.getRenglones()) {
            importes[i] = renglon.getImporte();
            i++;
        }        
        if (factura.getSubTotal() != this.calcularSubTotal(importes)) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_factura_sub_total_no_valido"));
        }
        //SubTotalBruto
        double subTotalBruto = this.calcularSubTotalBruto(factura.getTipoComprobante(),
                factura.getSubTotal(), factura.getRecargo_neto(), factura.getDescuento_neto(),
                factura.getIva_105_neto(), factura.getIva_21_neto());
        if (factura.getSubTotal_bruto() != subTotalBruto) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_factura_sub_total_bruto_no_valido"));
        }
        //IVA
        i = 0;
        if (factura.getTipoComprobante() == TipoDeComprobante.FACTURA_A || factura.getTipoComprobante() == TipoDeComprobante.FACTURA_B
                || factura.getTipoComprobante() == TipoDeComprobante.PRESUPUESTO) {
            double[] ivaPorcentajes = new double[factura.getRenglones().size()];
            double[] ivaNetos = new double[factura.getRenglones().size()];
            double[] cantidades = new double[factura.getRenglones().size()];
            for (RenglonFactura renglon : factura.getRenglones()) {
                ivaPorcentajes[i] = renglon.getIva_porcentaje();
                ivaNetos[i] = renglon.getIva_neto();
                cantidades[i] = renglon.getCantidad();
                i++;
            }
            double ivaNeto21 = this.calcularIvaNetoFactura(factura.getTipoComprobante(), cantidades, ivaPorcentajes, ivaNetos,
                    21, factura.getDescuento_porcentaje(), factura.getRecargo_porcentaje());
            double ivaNeto105 = this.calcularIvaNetoFactura(factura.getTipoComprobante(), cantidades, ivaPorcentajes, ivaNetos,
                    10.5, factura.getDescuento_porcentaje(), factura.getRecargo_porcentaje());            
            if (factura.getIva_21_neto() != ivaNeto21) {
                throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                        .getString("mensaje_factura_iva21_no_valido"));
            }            
            if (factura.getIva_105_neto() != ivaNeto105) {
                throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                        .getString("mensaje_factura_iva105_no_valido"));
            }
        }
        //Total
        double total = this.calcularTotal(factura.getSubTotal_bruto(), factura.getIva_105_neto(), factura.getIva_21_neto());
        if (factura.getTotal() != total || factura.getTotal() < 0) {
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
        double totalFactura = Utilidades.round(factura.getTotal(), 2);
        double totalPagado = Utilidades.round(this.getTotalPagado(factura.getId_Factura()), 2);
        if (totalPagado >= totalFactura) {               
            factura.setPagada(true);
        } else {
            factura.setPagada(false);
        }
        return factura;
    }
    
    @Override
    public Double getTotalPagado(long idFactura) {
        Double total = pagoService.getTotalPagosDeLaFactura(idFactura);
        return (total != null) ? total : 0.0;      
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
    public double calcularSubTotal(double[] importes) {
        double resultado = 0;
        for (double importe : importes) {
            resultado += importe;
        }
        return resultado;
    }

    @Override
    public double calcularDescuentoNeto(double importe, double descuento_porcentaje) {
        double resultado = 0;
        if (descuento_porcentaje != 0) {
            resultado = (importe * descuento_porcentaje) / 100;
        }
        return resultado;
    }

    @Override
    public double calcularRecargoNeto(double subtotal, double recargo_porcentaje) {
        double resultado = 0;
        if (recargo_porcentaje != 0) {
            resultado = (subtotal * recargo_porcentaje) / 100;
        }
        return resultado;
    }

    @Override
    public double calcularSubTotalBruto(TipoDeComprobante tipo, double subTotal,
            double recargoNeto, double descuentoNeto, double iva105Neto, double iva21Neto) {
        
        double resultado = subTotal + recargoNeto - descuentoNeto;
        if (tipo == TipoDeComprobante.FACTURA_B || tipo == TipoDeComprobante.PRESUPUESTO) {
            resultado = resultado - (iva105Neto + iva21Neto);
        }
        return resultado;
    }

    @Override
    public double calcularImpInternoNeto(TipoDeComprobante tipoDeComprobante, double descuento_porcentaje,
            double recargo_porcentaje, double[] importes, double [] impuestoPorcentajes) {

        double resultado = 0;
        if (tipoDeComprobante == TipoDeComprobante.FACTURA_A || tipoDeComprobante == TipoDeComprobante.FACTURA_B || tipoDeComprobante == TipoDeComprobante.PRESUPUESTO) {
            int longitudImportes = importes.length;
            int longitudImpuestos = impuestoPorcentajes.length;
            if (longitudImportes == longitudImpuestos) {
                for (int i = 0; i < longitudImportes; i++) {                
                double descuento = 0;
                if (descuento_porcentaje != 0) {
                    descuento = (importes[i] * descuento_porcentaje) / 100;
                }
                double recargo = 0;
                if (recargo_porcentaje != 0) {
                    recargo = (importes[i]  * recargo_porcentaje) / 100;
                }
                double impInterno_neto = ((importes[i]  + recargo - descuento) * impuestoPorcentajes[i]) / 100;
                resultado += impInterno_neto;
                }
            }
        }
        return resultado;
    }

    @Override
    public double calcularTotal(double subTotalBruto, double iva105Neto, double iva21Neto) {
        return (subTotalBruto + iva105Neto + iva21Neto);        
    }

    @Override
    public double calcularTotalFacturadoVenta(BusquedaFacturaVentaCriteria criteria) {
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
    public double getSaldoFacturasVentaSegunClienteYEmpresa(long empresa, long cliente, Date hasta) {
        Double saldo = facturaVentaRepository.getSaldoFacturasVentaSegunClienteYEmpresa(empresa, cliente, hasta);
        return (saldo == null) ? 0 : saldo;
    }

    @Override
    public double calcularTotalFacturadoCompra(BusquedaFacturaCompraCriteria criteria) {
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
    public double calcularIvaVenta(BusquedaFacturaVentaCriteria criteria) {
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
    public double calcularIvaCompra(BusquedaFacturaCompraCriteria criteria) {
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
    public double calcularGananciaTotal(BusquedaFacturaVentaCriteria criteria) {
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
    public double calcularIVANetoRenglon(Movimiento movimiento, TipoDeComprobante tipo, Producto producto, double descuento_porcentaje) {
        double resultado = 0;
        if (movimiento == Movimiento.COMPRA) {
            if (tipo == TipoDeComprobante.FACTURA_A || tipo == TipoDeComprobante.FACTURA_B) {
                resultado = producto.getPrecioCosto() * (1 - (descuento_porcentaje / 100)) * (producto.getIva_porcentaje() / 100);
            }
        } else if (movimiento == Movimiento.VENTA) {
            if (tipo == TipoDeComprobante.FACTURA_A || tipo == TipoDeComprobante.FACTURA_B || tipo == TipoDeComprobante.PRESUPUESTO) {
                resultado = producto.getPrecioVentaPublico() * (1 - (descuento_porcentaje / 100)) * (producto.getIva_porcentaje() / 100);
            }
        }                               
        return resultado;
    }
    
    @Override
    public double calcularIvaNetoFactura(TipoDeComprobante tipo, double[] cantidades, double[] ivaPorcentajeRenglones,
            double[] ivaNetoRenglones, double ivaPorcentaje, double descuentoPorcentaje, double recargoPorcentaje) {
        double resultado = 0;
        int indice = cantidades.length;
        for (int i = 0; i < indice; i++) {
            if (ivaPorcentajeRenglones[i] == ivaPorcentaje) {                
                if (tipo == TipoDeComprobante.FACTURA_A || tipo == TipoDeComprobante.FACTURA_B
                        || tipo == TipoDeComprobante.FACTURA_C || tipo == TipoDeComprobante.PRESUPUESTO) {
                    resultado += cantidades[i] * (ivaNetoRenglones[i] 
                                - (ivaNetoRenglones[i]  * (descuentoPorcentaje/100)) 
                                + (ivaNetoRenglones[i] * (recargoPorcentaje/100)));
                } else {
                    resultado += cantidades[i] * ivaNetoRenglones[i];
                }
            }
        }
        return resultado;
    }

    @Override
    public double calcularImpInternoNeto(Movimiento movimiento, Producto producto, double descuento_neto) {
        double resultado = 0;
        if (movimiento == Movimiento.COMPRA) {
            resultado = ((producto.getPrecioCosto() - descuento_neto) * producto.getImpuestoInterno_porcentaje()) / 100;
        }
        if (movimiento == Movimiento.VENTA) {
            resultado = ((producto.getPrecioVentaPublico() - descuento_neto) * producto.getImpuestoInterno_porcentaje()) / 100;
        }
        return resultado;
    }

    @Override
    public double calcularPrecioUnitario(Movimiento movimiento, TipoDeComprobante tipoDeComprobante, Producto producto) {
        double iva_resultado;
        double impInterno_resultado;
        double resultado = 0;
        if (movimiento == Movimiento.COMPRA) {
            if (tipoDeComprobante.equals(TipoDeComprobante.FACTURA_A) || tipoDeComprobante.equals(TipoDeComprobante.FACTURA_X)) {
                resultado = producto.getPrecioCosto();
            } else {
                iva_resultado = (producto.getPrecioCosto() * producto.getIva_porcentaje()) / 100;
                impInterno_resultado = (producto.getPrecioCosto() * producto.getImpuestoInterno_porcentaje()) / 100;
                resultado = producto.getPrecioCosto() + iva_resultado + impInterno_resultado;
            }
        }
        if (movimiento == Movimiento.VENTA) {
            if (tipoDeComprobante.equals(TipoDeComprobante.FACTURA_A) || tipoDeComprobante.equals(TipoDeComprobante.FACTURA_X)) {
                resultado = producto.getPrecioVentaPublico();
            } else if (tipoDeComprobante.equals(TipoDeComprobante.FACTURA_Y)) {
                iva_resultado = (producto.getPrecioVentaPublico() * producto.getIva_porcentaje() / 2) / 100;
                impInterno_resultado = (producto.getPrecioVentaPublico() * producto.getImpuestoInterno_porcentaje()) / 100;
                resultado = producto.getPrecioVentaPublico() + iva_resultado + impInterno_resultado;
            } else {
                resultado = producto.getPrecioLista();
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
    public double calcularVuelto(double importeAPagar, double importeAbonado) {
        if (importeAbonado <= importeAPagar) {
            return 0;
        } else {
            return importeAbonado - importeAPagar;
        }
    }

    @Override
    public double calcularImporte(double cantidad, double precioUnitario, double descuento_neto) {
        return (precioUnitario - descuento_neto) * cantidad;
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
            factura.setIva_105_neto(0);
            factura.setIva_21_neto(0);
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
    public List<RenglonFactura> convertirRenglonesPedidoARenglonesFactura(Pedido pedido, TipoDeComprobante tipoDeComprobante) {
        List<RenglonFactura> renglonesRestantes = new ArrayList<>();
        HashMap<Long, RenglonFactura> renglonesDeFacturas = pedidoService.getRenglonesFacturadosDelPedido(pedido.getId_Pedido());
        List<Factura> facturasDePedido = this.getFacturasDelPedido(pedido.getId_Pedido());
        if (facturasDePedido != null) {
            pedido.getRenglones().stream().forEach((renglon) -> {
                if (renglonesDeFacturas.containsKey(renglon.getProducto().getId_Producto())) {
                    if (renglon.getCantidad() > renglonesDeFacturas.get(renglon.getProducto().getId_Producto()).getCantidad()) {
                        renglonesRestantes.add(this.calcularRenglon(tipoDeComprobante,
                                Movimiento.VENTA, renglon.getCantidad() - renglonesDeFacturas.get(renglon.getProducto().getId_Producto()).getCantidad(),
                                renglon.getProducto().getId_Producto(), renglon.getDescuento_porcentaje()));
                    }
                } else {
                    renglonesRestantes.add(this.calcularRenglon(tipoDeComprobante, Movimiento.VENTA,
                            renglon.getCantidad(), renglon.getProducto().getId_Producto(), renglon.getDescuento_porcentaje()));
                }
            });
        } else {
            pedido.getRenglones().stream().forEach((renglon) -> {
                renglonesRestantes.add(this.calcularRenglon(tipoDeComprobante, Movimiento.VENTA,
                        renglon.getCantidad(), renglon.getProducto().getId_Producto(), renglon.getDescuento_porcentaje()));
            });
        }
        return renglonesRestantes;
    }

    @Override
    public RenglonFactura calcularRenglon(TipoDeComprobante tipo, Movimiento movimiento,
            double cantidad, Long idProducto, double descuento_porcentaje) {

        Producto producto = productoService.getProductoPorId(idProducto);
        RenglonFactura nuevoRenglon = new RenglonFactura();
        nuevoRenglon.setId_ProductoItem(producto.getId_Producto());
        nuevoRenglon.setCodigoItem(producto.getCodigo());
        nuevoRenglon.setDescripcionItem(producto.getDescripcion());
        nuevoRenglon.setMedidaItem(producto.getMedida().getNombre());
        nuevoRenglon.setCantidad(cantidad);
        nuevoRenglon.setPrecioUnitario(this.calcularPrecioUnitario(movimiento, tipo, producto));
        if(descuento_porcentaje > 100) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_descuento_mayor_cien"));
        }
        nuevoRenglon.setDescuento_porcentaje(descuento_porcentaje);
        nuevoRenglon.setDescuento_neto(this.calcularDescuentoNeto(nuevoRenglon.getPrecioUnitario(), descuento_porcentaje));
        nuevoRenglon.setIva_porcentaje(producto.getIva_porcentaje());
        if (tipo.equals(TipoDeComprobante.FACTURA_Y)) {
            nuevoRenglon.setIva_porcentaje(producto.getIva_porcentaje() / 2);
        }
        nuevoRenglon.setIva_neto(this.calcularIVANetoRenglon(movimiento, tipo, producto, nuevoRenglon.getDescuento_porcentaje()));
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
        double[] importes = new double[size];
        double[] cantidades = new double[size];
        double[] ivaPorcentajeRenglones = new double[size];
        double[] ivaNetoRenglones = new double[size];
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
                ivaPorcentajeRenglones, ivaNetoRenglones, 10.5, facturaADividir.getDescuento_porcentaje(), facturaADividir.getRecargo_porcentaje()));
        facturaSinIVA.setIva_21_neto(this.calcularIvaNetoFactura(facturaSinIVA.getTipoComprobante(), cantidades,
                ivaPorcentajeRenglones, ivaNetoRenglones, 21, facturaADividir.getDescuento_porcentaje(), facturaADividir.getRecargo_porcentaje()));
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
        double[] importes = new double[size];
        double[] cantidades = new double[size];
        double[] ivaPorcentajeRenglones = new double[size];
        double[] ivaNetoRenglones = new double[size];
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
                ivaPorcentajeRenglones, ivaNetoRenglones, 10.5, facturaADividir.getDescuento_porcentaje(), facturaADividir.getRecargo_porcentaje()));
        facturaConIVA.setIva_21_neto(this.calcularIvaNetoFactura(facturaConIVA.getTipoComprobante(), cantidades,
                ivaPorcentajeRenglones, ivaNetoRenglones, 21, facturaADividir.getDescuento_porcentaje(), facturaADividir.getRecargo_porcentaje()));
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
        double cantidadProductosRenglonFacturaSinIVA = 0;
        int renglonMarcado = 0;
        int numeroDeRenglon = 0;
        for (RenglonFactura renglon : renglones) {
            if (numeroDeRenglon == indices[renglonMarcado]) {
                double cantidad = renglon.getCantidad();
                if (cantidad >= 1) {
                    if ((cantidad % 1 != 0) || (cantidad % 2) == 0) {
                        cantidadProductosRenglonFacturaSinIVA = cantidad / 2;
                    } else if ((cantidad % 2) != 0) {
                        cantidadProductosRenglonFacturaSinIVA = cantidad - (Math.ceil(cantidad / 2));
                    }
                } else {
                    cantidadProductosRenglonFacturaSinIVA = 0;
                }
                RenglonFactura nuevoRenglonSinIVA = this.calcularRenglon(TipoDeComprobante.FACTURA_X, Movimiento.VENTA, 
                            cantidadProductosRenglonFacturaSinIVA, renglon.getId_ProductoItem(), renglon.getDescuento_porcentaje());
                if (nuevoRenglonSinIVA.getCantidad() != 0) {
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
        double cantidadProductosRenglonFacturaConIVA = 0;
        int renglonMarcado = 0;
        int numeroDeRenglon = 0;
        for (RenglonFactura renglon : renglones) {
            if (numeroDeRenglon == indices[renglonMarcado]) {
                double cantidad = renglon.getCantidad();
                if (cantidad < 1 || cantidad == 1) {
                    cantidadProductosRenglonFacturaConIVA = cantidad;
                } else if ((cantidad % 1 != 0) || (renglon.getCantidad() % 2) == 0) {
                    cantidadProductosRenglonFacturaConIVA = renglon.getCantidad() / 2;
                } else if ((renglon.getCantidad() % 2) != 0) {
                    cantidadProductosRenglonFacturaConIVA = Math.ceil(renglon.getCantidad() / 2);
                }
                RenglonFactura nuevoRenglonConIVA = this.calcularRenglon(facturaConIVA.getTipoComprobante(), Movimiento.VENTA,
                        cantidadProductosRenglonFacturaConIVA, renglon.getId_ProductoItem(), renglon.getDescuento_porcentaje());
                renglonesConIVA.add(nuevoRenglonConIVA);
                renglonMarcado++;
                numeroDeRenglon++;
            } else {
                numeroDeRenglon++;
                RenglonFactura nuevoRenglonConIVA = this.calcularRenglon(facturaConIVA.getTipoComprobante(), Movimiento.VENTA,
                        renglon.getCantidad(), renglon.getId_ProductoItem(), renglon.getDescuento_porcentaje());
                renglonesConIVA.add(nuevoRenglonConIVA);
            }
        }
        facturaConIVA.setRenglones(renglonesConIVA);
        return facturaConIVA;
    }

}

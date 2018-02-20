package sic.service.impl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.DateExpression;
import com.querydsl.core.types.dsl.Expressions;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
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
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sic.modelo.BusquedaPedidoCriteria;
import sic.modelo.Empresa;
import sic.modelo.Factura;
import sic.modelo.Pedido;
import sic.modelo.RenglonFactura;
import sic.modelo.RenglonPedido;
import sic.modelo.EstadoPedido;
import sic.modelo.Movimiento;
import sic.modelo.QPedido;
import sic.service.IFacturaService;
import sic.service.IPedidoService;
import sic.service.BusinessServiceException;
import sic.service.ServiceException;
import sic.modelo.TipoDeOperacion;
import sic.repository.PedidoRepository;
import sic.util.FormatterFechaHora;

@Service
public class PedidoServiceImpl implements IPedidoService {

    private final PedidoRepository pedidoRepository;
    private final IFacturaService facturaService;    
    private final static BigDecimal CIEN = new BigDecimal("100");
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    
    @Autowired
    public PedidoServiceImpl(IFacturaService facturaService, PedidoRepository pedidoRepository) {
        this.facturaService = facturaService;
        this.pedidoRepository = pedidoRepository;        
    }

    @Override
    public Pedido getPedidoPorId(Long idPedido) {
        Pedido pedido = pedidoRepository.findById(idPedido);
        if (pedido == null) {
            throw new EntityNotFoundException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_pedido_no_existente"));
        }
        return pedido;
    }
    
    @Override
    public Pedido getPedidoPorNumeroYEmpresa(Long nroPedido, Empresa empresa) {
        Pedido pedido = this.pedidoRepository.findByNroPedidoAndEmpresaAndEliminado(nroPedido, empresa, false);
        if (pedido == null) {
            throw new EntityNotFoundException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_pedido_no_existente"));
        }
        return pedido;
    }
    
    private void validarPedido(TipoDeOperacion operacion, Pedido pedido) {
        //Entrada de Datos
        //Requeridos
        if (pedido.getFecha() == null) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_pedido_fecha_vacia"));
        }        
        if (pedido.getRenglones() == null || pedido.getRenglones().isEmpty()) {  
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_pedido_renglones_vacio"));
        }
        if (pedido.getEmpresa() == null) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_pedido_empresa_vacia"));
        }
        if (pedido.getUsuario() == null) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_pedido_usuario_vacio"));
        }
        if (pedido.getCliente() == null) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_pedido_cliente_vacio"));
        }
        //Validar Estado
        EstadoPedido estado = pedido.getEstado();
        if ((estado != EstadoPedido.ABIERTO) && (estado != EstadoPedido.ACTIVO) && (estado != EstadoPedido.CERRADO)) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaja_estado_no_valido"));
        }
        if (operacion == TipoDeOperacion.ALTA) {
            //Duplicados       
            if (pedidoRepository.findByNroPedidoAndEmpresaAndEliminado(pedido.getNroPedido(), pedido.getEmpresa(), false) != null) {
                throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                        .getString("mensaje_pedido_duplicado"));
            }
        }
        if (operacion == TipoDeOperacion.ACTUALIZACION) {
            //Duplicados       
            if (pedidoRepository.findByNroPedidoAndEmpresaAndEliminado(pedido.getNroPedido(), pedido.getEmpresa(), false) == null) {
                throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                        .getString("mensaje_pedido_no_existente"));
            }
        }        
    }

    private List<Pedido> calcularTotalActualDePedidos(List<Pedido> pedidos) {
        pedidos.stream().forEach(p -> {
            this.calcularTotalActualDePedido(p);
        });
        return pedidos;
    }

    @Override
    public Pedido actualizarEstadoPedido(Pedido pedido) {
        pedido.setEstado(EstadoPedido.ACTIVO);
        if (this.getFacturasDelPedido(pedido.getId_Pedido()).isEmpty()) {
            pedido.setEstado(EstadoPedido.ABIERTO);
        }
        if (facturaService.pedidoTotalmenteFacturado(pedido)) {
            pedido.setEstado(EstadoPedido.CERRADO);
        }
        return pedido;
    }

    @Override
    public Pedido calcularTotalActualDePedido(Pedido pedido) {
        BigDecimal porcentajeDescuento;
        BigDecimal totalActual = BigDecimal.ZERO;
        for (RenglonPedido renglonPedido : this.getRenglonesDelPedido(pedido.getId_Pedido())) {
            porcentajeDescuento = BigDecimal.ONE.subtract(renglonPedido.getDescuento_porcentaje().divide(CIEN, 15, RoundingMode.HALF_UP));
            renglonPedido.setSubTotal(renglonPedido.getProducto().getPrecioLista().multiply(renglonPedido.getCantidad()).multiply(porcentajeDescuento));
            totalActual = totalActual.add(renglonPedido.getSubTotal());
        }
        pedido.setTotalActual(totalActual);
        return pedido;
    }

    @Override
    public long calcularNumeroPedido(Empresa empresa) {
        Pedido pedido = pedidoRepository.findTopByEmpresaAndEliminadoOrderByNroPedidoDesc(empresa, false);
        if (pedido == null) {
            return 1; // No existe ningun Pedido anterior
        } else {
            return 1 + pedido.getNroPedido();
        }
    }

    @Override
    public List<Factura> getFacturasDelPedido(long idPedido) {
        return facturaService.getFacturasDelPedido(idPedido);
    }

    @Override
    @Transactional
    public Pedido guardar(Pedido pedido) {
        pedido.setFecha(new Date());
        pedido.setNroPedido(this.calcularNumeroPedido(pedido.getEmpresa()));
        pedido.setEstado(EstadoPedido.ABIERTO);
        this.validarPedido(TipoDeOperacion.ALTA , pedido);
        pedido = pedidoRepository.save(pedido);
        LOGGER.warn("El Pedido " + pedido + " se guardó correctamente.");
        return pedido;
    }

    @Override
    public Page<Pedido> buscarConCriteria(BusquedaPedidoCriteria criteria) {
        //Fecha
        if (criteria.isBuscaPorFecha() == true & (criteria.getFechaDesde() == null | criteria.getFechaHasta() == null)) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_pedido_fechas_busqueda_invalidas"));
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
        //Empresa
        if (criteria.getEmpresa() == null) {
            throw new EntityNotFoundException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_empresa_no_existente"));
        }
        //Cliente
        if (criteria.isBuscaCliente() == true && criteria.getCliente() == null) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_cliente_vacio_razonSocial"));
        }
        //Usuario
        if (criteria.isBuscaUsuario() == true && criteria.getUsuario() == null) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_usuario_vacio_nombre"));
        }
        QPedido qpedido = QPedido.pedido;
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(qpedido.empresa.eq(criteria.getEmpresa()).and(qpedido.eliminado.eq(false)));       
        if (criteria.isBuscaPorFecha() == true) {
            FormatterFechaHora formateadorFecha = new FormatterFechaHora(FormatterFechaHora.FORMATO_FECHAHORA_INTERNACIONAL);
            DateExpression<Date> fDesde = Expressions.dateTemplate(Date.class, "convert({0}, datetime)", formateadorFecha.format(criteria.getFechaDesde()));
            DateExpression<Date> fHasta = Expressions.dateTemplate(Date.class, "convert({0}, datetime)", formateadorFecha.format(criteria.getFechaHasta()));            
            builder.and(qpedido.fecha.between(fDesde, fHasta));
        }
        if (criteria.isBuscaCliente() == true) {
            builder.and(qpedido.cliente.eq(criteria.getCliente()));
        }
        if (criteria.isBuscaUsuario() == true) {
            builder.and(qpedido.usuario.eq(criteria.getUsuario()));
        }
        if (criteria.isBuscaPorNroPedido() == true) {
            builder.and(qpedido.nroPedido.eq(criteria.getNroPedido()));
        }        
        Page<Pedido> pedidos = pedidoRepository.findAll(builder, criteria.getPageable());
        this.calcularTotalActualDePedidos(pedidos.getContent());
        return pedidos;
    }

    @Override
    @Transactional
    public void actualizar(Pedido pedido) {
        this.validarPedido(TipoDeOperacion.ACTUALIZACION , pedido);
        pedidoRepository.save(pedido);
    }

    @Override
    @Transactional
    public boolean eliminar(long idPedido) {
        Pedido pedido = this.getPedidoPorId(idPedido);
        if (pedido.getEstado() == EstadoPedido.ABIERTO) {
            pedido.setEliminado(true);
            pedidoRepository.save(pedido);
        }
        return pedido.isEliminado();
    }

    @Override
    public List<RenglonPedido> getRenglonesDelPedido(Long idPedido) {
        return this.getPedidoPorId(idPedido).getRenglones();
    }

    @Override
    public HashMap<Long, RenglonFactura> getRenglonesFacturadosDelPedido(long nroPedido) {
        List<RenglonFactura> renglonesDeFacturas = new ArrayList<>();
        this.getFacturasDelPedido(nroPedido).stream().forEach(f -> {
            f.getRenglones().stream().forEach(r -> {
                renglonesDeFacturas.add(facturaService.calcularRenglon(f.getTipoComprobante(),
                        Movimiento.VENTA, r.getCantidad(), r.getId_ProductoItem(), r.getDescuento_porcentaje(), false));
            });
        });
        HashMap<Long, RenglonFactura> listaRenglonesUnificados = new HashMap<>();
        if (!renglonesDeFacturas.isEmpty()) {
            renglonesDeFacturas.stream().forEach(r -> {
                if (listaRenglonesUnificados.containsKey(r.getId_ProductoItem())) {
                    listaRenglonesUnificados.get(r.getId_ProductoItem())
                            .setCantidad(listaRenglonesUnificados
                                    .get(r.getId_ProductoItem()).getCantidad().add(r.getCantidad()));
                } else {
                    listaRenglonesUnificados.put(r.getId_ProductoItem(), r);
                }
            });
        }
        return listaRenglonesUnificados;
    }

    @Override
    public byte[] getReportePedido(Pedido pedido) {
        ClassLoader classLoader = PedidoServiceImpl.class.getClassLoader();
        InputStream isFileReport = classLoader.getResourceAsStream("sic/vista/reportes/Pedido.jasper");
        Map params = new HashMap();
        params.put("pedido", pedido);
        if (!pedido.getEmpresa().getLogo().isEmpty()) {
            try {
                params.put("logo", new ImageIcon(ImageIO.read(new URL(pedido.getEmpresa().getLogo()))).getImage());
            } catch (IOException ex) {
                LOGGER.error(ex.getMessage());
                throw new ServiceException(ResourceBundle.getBundle("Mensajes")
                        .getString("mensaje_empresa_404_logo"), ex);
            }
        }
        List<RenglonPedido> renglones = this.getRenglonesDelPedido(pedido.getId_Pedido());
        JRBeanCollectionDataSource ds = new JRBeanCollectionDataSource(renglones);
        try {
            return JasperExportManager.exportReportToPdf(JasperFillManager.fillReport(isFileReport, params, ds));
        } catch (JRException ex) {
            LOGGER.error(ex.getMessage());
            throw new ServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_error_reporte"), ex);
        }
    }
  
}

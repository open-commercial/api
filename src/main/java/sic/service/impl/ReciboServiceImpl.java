package sic.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.util.*;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.DateExpression;
import com.querydsl.core.types.dsl.Expressions;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sic.modelo.*;
import sic.repository.ReciboRepository;
import sic.service.*;
import sic.util.FormatterFechaHora;

@Service
public class ReciboServiceImpl implements IReciboService {

  private final ReciboRepository reciboRepository;
  private final ICuentaCorrienteService cuentaCorrienteService;
  private final IEmpresaService empresaService;
  private final IConfiguracionDelSistemaService configuracionDelSistemaService;
  private final INotaService notaService;
  private final IFormaDePagoService formaDePagoService;
  private final ICajaService cajaService;
  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("Mensajes");

  @Autowired
  @Lazy
  public ReciboServiceImpl(
      ReciboRepository reciboRepository,
      ICuentaCorrienteService cuentaCorrienteService,
      IEmpresaService empresaService,
      IConfiguracionDelSistemaService cds,
      INotaService notaService,
      IFormaDePagoService formaDePagoService,
      ICajaService cajaService) {
    this.reciboRepository = reciboRepository;
    this.cuentaCorrienteService = cuentaCorrienteService;
    this.empresaService = empresaService;
    this.configuracionDelSistemaService = cds;
    this.notaService = notaService;
    this.formaDePagoService = formaDePagoService;
    this.cajaService = cajaService;
  }

  @Override
  public Recibo getById(long idRecibo) {
    return reciboRepository.findById(idRecibo);
  }

  private BooleanBuilder getBuilder(BusquedaReciboCriteria criteria) {
    QRecibo qRecibo = QRecibo.recibo;
    BooleanBuilder builder = new BooleanBuilder();
    if (criteria.isBuscaPorFecha()) {
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
      FormatterFechaHora formateadorFecha =
        new FormatterFechaHora(FormatterFechaHora.FORMATO_FECHAHORA_INTERNACIONAL);
      DateExpression<Date> fDesde =
        Expressions.dateTemplate(
          Date.class,
          "convert({0}, datetime)",
          formateadorFecha.format(criteria.getFechaDesde()));
      DateExpression<Date> fHasta =
        Expressions.dateTemplate(
          Date.class,
          "convert({0}, datetime)",
          formateadorFecha.format(criteria.getFechaHasta()));
      builder.and(qRecibo.fecha.between(fDesde, fHasta));
    }
    if (criteria.isBuscaPorNumeroRecibo())
      builder
        .and(qRecibo.numSerie.eq(criteria.getNumSerie()))
        .and(qRecibo.numRecibo.eq(criteria.getNumRecibo()));
    if (criteria.isBuscaPorConcepto()) {
      String[] terminos = criteria.getConcepto().split(" ");
      BooleanBuilder rsPredicate = new BooleanBuilder();
      for (String termino : terminos) {
        rsPredicate.and(qRecibo.concepto.containsIgnoreCase(termino));
      }
      builder.or(rsPredicate);
    }
    if (criteria.isBuscaPorCliente())
      builder.and(qRecibo.cliente.id_Cliente.eq(criteria.getIdCliente()));
    if (criteria.isBuscaPorProveedor())
      builder.and(qRecibo.proveedor.id_Proveedor.eq(criteria.getIdProveedor()));
    if (criteria.isBuscaPorUsuario())
      builder.and(qRecibo.usuario.id_Usuario.eq(criteria.getIdUsuario()));
    if (criteria.isBuscaPorViajante())
      builder.and(qRecibo.cliente.viajante.id_Usuario.eq(criteria.getIdViajante()));
    if (criteria.getMovimiento() == Movimiento.VENTA) builder.and(qRecibo.proveedor.isNull());
    else if (criteria.getMovimiento() == Movimiento.COMPRA) builder.and(qRecibo.cliente.isNull());
    builder.and(
      qRecibo.empresa.id_Empresa.eq(criteria.getIdEmpresa()).and(qRecibo.eliminado.eq(false)));
    return builder;
  }

  @Override
  public Page<Recibo> buscarRecibos(BusquedaReciboCriteria criteria) {
    return reciboRepository.findAll(this.getBuilder(criteria), criteria.getPageable());
  }

  @Override
  public BigDecimal getTotalRecibos(BusquedaReciboCriteria criteria) {
    return reciboRepository.getTotalRecibos(this.getBuilder(criteria));
  }

  @Override
  @Transactional
  public Recibo guardar(Recibo recibo) {
    recibo.setNumSerie(
        configuracionDelSistemaService
            .getConfiguracionDelSistemaPorEmpresa(recibo.getEmpresa())
            .getNroPuntoDeVentaAfip());
    recibo.setNumRecibo(
        this.getSiguienteNumeroRecibo(
            recibo.getEmpresa().getId_Empresa(),
            configuracionDelSistemaService
                .getConfiguracionDelSistemaPorEmpresa(recibo.getEmpresa())
                .getNroPuntoDeVentaAfip()));
    recibo.setFecha(new Date());
    this.validarRecibo(recibo);
    recibo = reciboRepository.save(recibo);
    this.cuentaCorrienteService.asentarEnCuentaCorriente(recibo, TipoDeOperacion.ALTA);
    logger.warn("El Recibo {} se guardó correctamente.", recibo);
    return recibo;
  }

  private void validarRecibo(Recibo recibo) {
    // Requeridos
    if (recibo.getMonto().compareTo(BigDecimal.ZERO) <= 0) {
      throw new BusinessServiceException(
          RESOURCE_BUNDLE.getString("mensaje_recibo_monto_igual_menor_cero"));
    }
    if (recibo.getEmpresa() == null) {
      throw new BusinessServiceException(RESOURCE_BUNDLE.getString("mensaje_recibo_empresa_vacia"));
    }
    this.cajaService.validarMovimiento(recibo.getFecha(), recibo.getEmpresa().getId_Empresa());
    if (recibo.getCliente() == null && recibo.getProveedor() == null) {
      throw new BusinessServiceException(
          RESOURCE_BUNDLE.getString("mensaje_recibo_cliente_proveedor_vacio"));
    }
    if (recibo.getCliente() != null && recibo.getProveedor() != null) {
      throw new BusinessServiceException(
          RESOURCE_BUNDLE.getString("mensaje_recibo_cliente_proveedor_simultaneos"));
    }
    if (recibo.getUsuario() == null) {
      throw new BusinessServiceException(RESOURCE_BUNDLE.getString("mensaje_recibo_usuario_vacio"));
    }
    if (recibo.getFormaDePago() == null) {
      throw new BusinessServiceException(
          RESOURCE_BUNDLE.getString("mensaje_recibo_forma_de_pago_vacia"));
    }
    if (recibo.getConcepto() == null || recibo.getConcepto().equals("")) {
      throw new BusinessServiceException(
          RESOURCE_BUNDLE.getString("mensaje_recibo_concepto_vacio"));
    }
  }

  @Override
  public long getSiguienteNumeroRecibo(long idEmpresa, long serie) {
    Recibo recibo =
      reciboRepository.findTopByEmpresaAndNumSerieOrderByNumReciboDesc(
        empresaService.getEmpresaPorId(idEmpresa), serie);
    if (recibo == null) {
      return 1; // No existe ningun Recibo anterior
    } else {
      return 1 + recibo.getNumRecibo();
    }
  }

  @Override
  public List<Recibo> construirRecibos(
      long[] idsFormaDePago,
      Empresa empresa,
      Cliente cliente,
      Usuario usuario,
      BigDecimal[] montos,
      BigDecimal totalFactura,
      Date fecha) {
    List<Recibo> recibos = new ArrayList<>();
    if (idsFormaDePago != null && montos != null && idsFormaDePago.length == montos.length) {
      BigDecimal totalMontos = BigDecimal.ZERO;
      for (BigDecimal monto : montos) {
        totalMontos = totalMontos.add(monto);
      }
      if (totalMontos.compareTo(totalFactura) > 0 || totalMontos.compareTo(BigDecimal.ZERO) < 0) {
        throw new BusinessServiceException(
            RESOURCE_BUNDLE.getString("mensaje_recibo_superan_total_factura"));
      }
      int i = 0;
      for (long idFormaDePago : idsFormaDePago) {
        Recibo recibo = new Recibo();
        recibo.setCliente(cliente);
        recibo.setUsuario(usuario);
        recibo.setEmpresa(empresa);
        recibo.setFecha(fecha);
        FormaDePago fdp = formaDePagoService.getFormasDePagoPorId(idFormaDePago);
        recibo.setFormaDePago(fdp);
        recibo.setMonto(montos[i]);
        recibo.setNumSerie(
            configuracionDelSistemaService
                .getConfiguracionDelSistemaPorEmpresa(recibo.getEmpresa())
                .getNroPuntoDeVentaAfip());
        recibo.setNumRecibo(
            this.getSiguienteNumeroRecibo(empresa.getId_Empresa(), recibo.getNumSerie()));
        recibo.setConcepto("SALDO.");
        recibos.add(recibo);
        i++;
      }
    }
    return recibos;
  }

  @Override
  @Transactional
  public void eliminar(long idRecibo) {
    Recibo r = reciboRepository.findById(idRecibo);
    if (!notaService.existsNotaDebitoPorRecibo(r)) {
      r.setEliminado(true);
      this.cuentaCorrienteService.asentarEnCuentaCorriente(r, TipoDeOperacion.ELIMINACION);
      this.actualizarCajaPorEliminacionDeRecibo(r);
      reciboRepository.save(r);
      logger.warn("El Recibo {} se eliminó correctamente.", r);
    } else {
      throw new BusinessServiceException(RESOURCE_BUNDLE.getString("mensaje_no_se_puede_eliminar"));
    }
  }

  private void actualizarCajaPorEliminacionDeRecibo(Recibo recibo) {
    Caja caja =
        this.cajaService.encontrarCajaCerradaQueContengaFechaEntreFechaAperturaYFechaCierre(
            recibo.getEmpresa().getId_Empresa(), recibo.getFecha());
    BigDecimal monto = BigDecimal.ZERO;
    if (caja != null && caja.getEstado().equals(EstadoCaja.CERRADA)) {
      if (recibo.getCliente() != null) {
        monto = recibo.getMonto().negate();
      } else if (recibo.getProveedor() != null) {
        monto = recibo.getMonto();
      }
      cajaService.actualizarSaldoSistema(caja, monto);
      logger.warn("El Recibo {} modificó la caja {} debido a una eliminación.", recibo, caja);
    }
  }

  @Override
  public List<Recibo> getRecibosEntreFechasPorFormaDePago(
      Date desde, Date hasta, FormaDePago formaDePago, Empresa empresa) {
    return reciboRepository.getRecibosEntreFechasPorFormaDePago(
        empresa.getId_Empresa(), formaDePago.getId_FormaDePago(), desde, hasta);
  }

  @Override
  public byte[] getReporteRecibo(Recibo recibo) {
    if (recibo.getProveedor() != null) {
      throw new BusinessServiceException(
          RESOURCE_BUNDLE.getString("mensaje_recibo_reporte_proveedor"));
    }
    ClassLoader classLoader = FacturaServiceImpl.class.getClassLoader();
    InputStream isFileReport = classLoader.getResourceAsStream("sic/vista/reportes/Recibo.jasper");
    Map<String, Object> params = new HashMap<>();
    params.put("recibo", recibo);
    if (recibo.getEmpresa().getLogo() != null && !recibo.getEmpresa().getLogo().isEmpty()) {
      try {
        params.put(
            "logo", new ImageIcon(ImageIO.read(new URL(recibo.getEmpresa().getLogo()))).getImage());
      } catch (IOException ex) {
        logger.error(ex.getMessage());
        throw new ServiceException(RESOURCE_BUNDLE.getString("mensaje_empresa_404_logo"), ex);
      }
    }
    try {
      return JasperExportManager.exportReportToPdf(
          JasperFillManager.fillReport(isFileReport, params));
    } catch (JRException ex) {
      logger.error(ex.getMessage());
      throw new ServiceException(RESOURCE_BUNDLE.getString("mensaje_error_reporte"), ex);
    }
  }

  @Override
  public BigDecimal getTotalRecibosClientesEntreFechasPorFormaDePago(
      long idEmpresa, long idFormaDePago, Date desde, Date hasta) {
    BigDecimal total =
        reciboRepository.getTotalRecibosClientesEntreFechasPorFormaDePago(
            idEmpresa, idFormaDePago, desde, hasta);
    return (total == null) ? BigDecimal.ZERO : total;
  }

  @Override
  public BigDecimal getTotalRecibosProveedoresEntreFechasPorFormaDePago(
      long idEmpresa, long idFormaDePago, Date desde, Date hasta) {
    BigDecimal total =
        reciboRepository.getTotalRecibosProveedoresEntreFechasPorFormaDePago(
            idEmpresa, idFormaDePago, desde, hasta);
    return (total == null) ? BigDecimal.ZERO : total;
  }

  @Override
  public BigDecimal getTotalRecibosClientesQueAfectanCajaEntreFechas(
      long idEmpresa, Date desde, Date hasta) {
    BigDecimal total =
        reciboRepository.getTotalRecibosClientesQueAfectanCajaEntreFechas(idEmpresa, desde, hasta);
    return (total == null) ? BigDecimal.ZERO : total;
  }

  @Override
  public BigDecimal getTotalRecibosProveedoresQueAfectanCajaEntreFechas(
      long idEmpresa, Date desde, Date hasta) {
    BigDecimal total =
        reciboRepository.getTotalRecibosProveedoresQueAfectanCajaEntreFechas(
            idEmpresa, desde, hasta);
    return (total == null) ? BigDecimal.ZERO : total;
  }

  @Override
  public BigDecimal getTotalRecibosClientesEntreFechas(long idEmpresa, Date desde, Date hasta) {
    BigDecimal total = reciboRepository.getTotalRecibosClientesEntreFechas(idEmpresa, desde, hasta);
    return (total == null) ? BigDecimal.ZERO : total;
  }

  @Override
  public BigDecimal getTotalRecibosProveedoresEntreFechas(long idEmpresa, Date desde, Date hasta) {
    BigDecimal total =
        reciboRepository.getTotalRecibosProveedoresEntreFechas(idEmpresa, desde, hasta);
    return (total == null) ? BigDecimal.ZERO : total;
  }
}

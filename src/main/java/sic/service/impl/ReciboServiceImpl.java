package sic.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;
import javax.imageio.ImageIO;
import javax.persistence.EntityNotFoundException;
import javax.swing.ImageIcon;

import com.mercadopago.resources.Payment;
import com.querydsl.core.BooleanBuilder;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sic.modelo.*;
import sic.modelo.criteria.BusquedaReciboCriteria;
import sic.repository.ReciboRepository;
import sic.service.*;
import sic.exception.BusinessServiceException;
import sic.exception.ServiceException;
import sic.util.CustomValidator;

@Service
public class ReciboServiceImpl implements IReciboService {

  private final ReciboRepository reciboRepository;
  private final ICuentaCorrienteService cuentaCorrienteService;
  private final ISucursalService sucursalService;
  private final IConfiguracionSucursalService configuracionSucursalService;
  private final INotaService notaService;
  private final IFormaDePagoService formaDePagoService;
  private final ICajaService cajaService;
  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private static final int TAMANIO_PAGINA_DEFAULT = 25;
  private final MessageSource messageSource;
  private final CustomValidator customValidator;

  @Autowired
  @Lazy
  public ReciboServiceImpl(
    ReciboRepository reciboRepository,
    ICuentaCorrienteService cuentaCorrienteService,
    ISucursalService sucursalService,
    IConfiguracionSucursalService configuracionSucursalService,
    INotaService notaService,
    IFormaDePagoService formaDePagoService,
    ICajaService cajaService,
    MessageSource messageSource,
    CustomValidator customValidator) {
    this.reciboRepository = reciboRepository;
    this.cuentaCorrienteService = cuentaCorrienteService;
    this.sucursalService = sucursalService;
    this.configuracionSucursalService = configuracionSucursalService;
    this.notaService = notaService;
    this.formaDePagoService = formaDePagoService;
    this.cajaService = cajaService;
    this.messageSource = messageSource;
    this.customValidator = customValidator;
  }

  @Override
  public Recibo getReciboNoEliminadoPorId(long idRecibo) {
    Optional<Recibo> recibo = reciboRepository.findById(idRecibo);
    if (recibo.isPresent() && !recibo.get().isEliminado()) {
      return recibo.get();
    } else {
      throw new EntityNotFoundException(messageSource.getMessage(
        "mensaje_recibo_no_existente", null, Locale.getDefault()));
    }
  }

  @Override
  public Optional<Recibo> getReciboPorIdMercadoPago(String idPagoMercadoPago) {
    return reciboRepository.findReciboByIdPagoMercadoPagoAndEliminado(idPagoMercadoPago, false);
  }

  @Override
  public BooleanBuilder getBuilder(BusquedaReciboCriteria criteria) {
    QRecibo qRecibo = QRecibo.recibo;
    BooleanBuilder builder = new BooleanBuilder();
    if (criteria.getConcepto() != null) {
      String[] terminos = criteria.getConcepto().split(" ");
      BooleanBuilder rsPredicate = new BooleanBuilder();
      for (String termino : terminos) {
        rsPredicate.and(qRecibo.concepto.containsIgnoreCase(termino));
      }
      builder.or(rsPredicate);
    }
    if (criteria.getNumSerie() != null && criteria.getNumRecibo() != null)
      builder
          .and(qRecibo.numSerie.eq(criteria.getNumSerie()))
          .and(qRecibo.numRecibo.eq(criteria.getNumRecibo()));
    if (criteria.getIdCliente() != null)
      builder.and(qRecibo.cliente.idCliente.eq(criteria.getIdCliente()));
    if (criteria.getIdProveedor() != null)
      builder.and(qRecibo.proveedor.idProveedor.eq(criteria.getIdProveedor()));
    if (criteria.getIdUsuario() != null)
      builder.and(qRecibo.usuario.idUsuario.eq(criteria.getIdUsuario()));
    if (criteria.getIdViajante() != null)
      builder.and(qRecibo.cliente.viajante.idUsuario.eq(criteria.getIdViajante()));
    if (criteria.getIdFormaDePago() != null)
      builder.and(qRecibo.formaDePago.idFormaDePago.eq(criteria.getIdFormaDePago()));
    if (criteria.getMovimiento() == Movimiento.VENTA) builder.and(qRecibo.proveedor.isNull());
    else if (criteria.getMovimiento() == Movimiento.COMPRA) builder.and(qRecibo.cliente.isNull());
    if (criteria.getIdSucursal() != null)
      builder.and(qRecibo.sucursal.idSucursal.eq(criteria.getIdSucursal()));
    builder.and(qRecibo.eliminado.eq(false));
    if (criteria.getFechaDesde() != null || criteria.getFechaHasta() != null) {
      if (criteria.getFechaDesde() != null && criteria.getFechaHasta() != null) {
        criteria.setFechaDesde(criteria.getFechaDesde().withHour(0).withMinute(0).withSecond(0).withNano(0));
        criteria.setFechaHasta(criteria.getFechaHasta().withHour(23).withMinute(59).withSecond(59).withNano(999999999));
        builder.and(qRecibo.fecha.between(criteria.getFechaDesde(), criteria.getFechaHasta()));
      } else if (criteria.getFechaDesde() != null) {
        criteria.setFechaDesde(criteria.getFechaDesde().withHour(0).withMinute(0).withSecond(0).withNano(0));
        builder.and(qRecibo.fecha.after(criteria.getFechaDesde()));
      } else if (criteria.getFechaHasta() != null) {
        criteria.setFechaHasta(criteria.getFechaHasta().withHour(23).withMinute(59).withSecond(59).withNano(999999999));
        builder.and(qRecibo.fecha.before(criteria.getFechaHasta()));
      }
    }
    return builder;
  }

  @Override
  public Page<Recibo> buscarRecibos(BusquedaReciboCriteria criteria) {
    return reciboRepository.findAll(
        this.getBuilder(criteria),
        this.getPageable(criteria.getPagina(), criteria.getOrdenarPor(), criteria.getSentido()));
  }

  private Pageable getPageable(Integer pagina, String ordenarPor, String sentido) {
    if (pagina == null) pagina = 0;
    String ordenDefault = "fecha";
    if (ordenarPor == null || sentido == null) {
      return PageRequest.of(
          pagina, TAMANIO_PAGINA_DEFAULT, Sort.by(Sort.Direction.DESC, ordenDefault));
    } else {
      switch (sentido) {
        case "ASC":
          return PageRequest.of(
              pagina, TAMANIO_PAGINA_DEFAULT, Sort.by(Sort.Direction.ASC, ordenarPor));
        case "DESC":
          return PageRequest.of(
              pagina, TAMANIO_PAGINA_DEFAULT, Sort.by(Sort.Direction.DESC, ordenarPor));
        default:
          return PageRequest.of(
              pagina, TAMANIO_PAGINA_DEFAULT, Sort.by(Sort.Direction.DESC, ordenDefault));
      }
    }
  }

  @Override
  public BigDecimal getTotalRecibos(BusquedaReciboCriteria criteria) {
    return reciboRepository.getTotalRecibos(this.getBuilder(criteria));
  }

  @Override
  @Transactional
  public Recibo guardar(Recibo recibo) {
    customValidator.validar(recibo);
    recibo.setNumSerie(
        configuracionSucursalService
            .getConfiguracionSucursal(recibo.getSucursal())
            .getNroPuntoDeVentaAfip());
    recibo.setNumRecibo(
        this.getSiguienteNumeroRecibo(
            recibo.getSucursal().getIdSucursal(),
            configuracionSucursalService
                .getConfiguracionSucursal(recibo.getSucursal())
                .getNroPuntoDeVentaAfip()));
    this.validarReglasDeNegocio(recibo);
    recibo = reciboRepository.save(recibo);
    this.cuentaCorrienteService.asentarEnCuentaCorriente(recibo, TipoDeOperacion.ALTA);
    logger.warn("El Recibo {} se guardó correctamente.", recibo);
    return recibo;
  }

  @Override
  public void validarReglasDeNegocio(Recibo recibo) {
    // Muteado momentaneamente por el problema del alta de recibo generado por sic-com cuando la caja esta cerrada
    // this.cajaService.validarMovimiento(recibo.getFecha(), recibo.getSucursal().getIdSucursal());
    if (recibo.getCliente() == null && recibo.getProveedor() == null) {
      throw new BusinessServiceException(messageSource.getMessage(
        "mensaje_recibo_cliente_proveedor_vacio", null, Locale.getDefault()));
    }
    if (recibo.getCliente() != null && recibo.getProveedor() != null) {
      throw new BusinessServiceException(messageSource.getMessage(
        "mensaje_recibo_cliente_proveedor_simultaneos", null, Locale.getDefault()));
    }
  }

  @Override
  public long getSiguienteNumeroRecibo(long idSucursal, long serie) {
    Recibo recibo =
      reciboRepository.findTopBySucursalAndNumSerieOrderByNumReciboDesc(
        sucursalService.getSucursalPorId(idSucursal), serie);
    if (recibo == null) {
      return 1; // No existe ningun Recibo anterior
    } else {
      return 1 + recibo.getNumRecibo();
    }
  }

  @Override
  public List<Recibo> construirRecibos(
      Long[] idsFormaDePago,
      Long idSucursal,
      Cliente cliente,
      Usuario usuario,
      BigDecimal[] montos,
      LocalDateTime fecha) {
    List<Recibo> recibos = new ArrayList<>();
    if (idsFormaDePago != null && montos != null && idsFormaDePago.length == montos.length) {
      HashMap<Long, BigDecimal> mapIdsFormaDePago = new HashMap<>();
      for (int i = 0; i < idsFormaDePago.length; i++) {
        if (mapIdsFormaDePago.containsKey(idsFormaDePago[i])) {
          mapIdsFormaDePago.put(
              idsFormaDePago[i], mapIdsFormaDePago.get(idsFormaDePago[i]).add(montos[i]));
        } else {
          mapIdsFormaDePago.put(idsFormaDePago[i], montos[i]);
        }
      }
      mapIdsFormaDePago.forEach(
          (k, v) -> {
            Recibo recibo = new Recibo();
            recibo.setCliente(cliente);
            recibo.setUsuario(usuario);
            Sucursal sucursal = sucursalService.getSucursalPorId(idSucursal);
            recibo.setSucursal(sucursal);
            recibo.setFecha(fecha);
            FormaDePago fdp = formaDePagoService.getFormasDePagoNoEliminadoPorId(k);
            recibo.setFormaDePago(fdp);
            recibo.setMonto(v);
            recibo.setNumSerie(
                configuracionSucursalService
                    .getConfiguracionSucursal(recibo.getSucursal())
                    .getNroPuntoDeVentaAfip());
            recibo.setNumRecibo(
                this.getSiguienteNumeroRecibo(sucursal.getIdSucursal(), recibo.getNumSerie()));
            recibo.setConcepto("SALDO.");
            recibos.add(recibo);
          });
    }
    return recibos;
  }

  @Override
  public Recibo construirReciboPorPayment(
      Sucursal sucursal, Usuario usuario, Cliente cliente, Payment payment) {
    Recibo nuevoRecibo = new Recibo();
    nuevoRecibo.setSucursal(sucursal);
    nuevoRecibo.setFormaDePago(
        formaDePagoService.getFormaDePagoPorNombre(FormaDePagoEnum.MERCADO_PAGO));
    nuevoRecibo.setUsuario(usuario);
    nuevoRecibo.setCliente(cliente);
    nuevoRecibo.setFecha(LocalDateTime.now());
    nuevoRecibo.setConcepto("Pago en MercadoPago (" + payment.getPaymentMethodId() + ")");
    nuevoRecibo.setMonto(new BigDecimal(Float.toString(payment.getTransactionAmount())));
    nuevoRecibo.setIdPagoMercadoPago(payment.getId());
    return nuevoRecibo;
  }

  @Override
  @Transactional
  public void eliminar(long idRecibo) {
    Recibo r = this.getReciboNoEliminadoPorId(idRecibo);
    if (!notaService.existsNotaDebitoPorRecibo(r)) {
      r.setEliminado(true);
      this.cuentaCorrienteService.asentarEnCuentaCorriente(r, TipoDeOperacion.ELIMINACION);
      this.actualizarCajaPorEliminacionDeRecibo(r);
      reciboRepository.save(r);
      logger.warn("El Recibo {} se eliminó correctamente.", r);
    } else {
      throw new BusinessServiceException(messageSource.getMessage(
        "mensaje_no_se_puede_eliminar", null, Locale.getDefault()));
    }
  }

  private void actualizarCajaPorEliminacionDeRecibo(Recibo recibo) {
    Caja caja =
        this.cajaService.encontrarCajaCerradaQueContengaFechaEntreFechaAperturaYFechaCierre(
            recibo.getSucursal().getIdSucursal(), recibo.getFecha());
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
    LocalDateTime desde, LocalDateTime hasta, FormaDePago formaDePago, Sucursal sucursal) {
    return reciboRepository.getRecibosEntreFechasPorFormaDePago(
        sucursal.getIdSucursal(), formaDePago.getIdFormaDePago(), desde, hasta);

  }

  @Override
  public byte[] getReporteRecibo(Recibo recibo) {
    if (recibo.getProveedor() != null) {
      throw new BusinessServiceException(messageSource.getMessage(
        "mensaje_recibo_reporte_proveedor", null, Locale.getDefault()));
    }
    ClassLoader classLoader = FacturaServiceImpl.class.getClassLoader();
    InputStream isFileReport = classLoader.getResourceAsStream("sic/vista/reportes/Recibo.jasper");
    Map<String, Object> params = new HashMap<>();
    params.put("recibo", recibo);
    if (recibo.getSucursal().getLogo() != null && !recibo.getSucursal().getLogo().isEmpty()) {
      try {
        params.put(
            "logo", new ImageIcon(ImageIO.read(new URL(recibo.getSucursal().getLogo()))).getImage());
      } catch (IOException ex) {
        throw new ServiceException(messageSource.getMessage(
          "mensaje_sucursal_404_logo", null, Locale.getDefault()), ex);
      }
    }
    try {
      return JasperExportManager.exportReportToPdf(
          JasperFillManager.fillReport(isFileReport, params));
    } catch (JRException ex) {
      throw new ServiceException(messageSource.getMessage(
        "mensaje_error_reporte", null, Locale.getDefault()), ex);
    }
  }

  @Override
  public BigDecimal getTotalRecibosClientesEntreFechasPorFormaDePago(
      long idSucursal, long idFormaDePago, LocalDateTime desde, LocalDateTime hasta) {
    BigDecimal total =
        reciboRepository.getTotalRecibosClientesEntreFechasPorFormaDePago(
            idSucursal, idFormaDePago, desde, hasta);
    return (total == null) ? BigDecimal.ZERO : total;
  }

  @Override
  public BigDecimal getTotalRecibosProveedoresEntreFechasPorFormaDePago(
      long idSucursal, long idFormaDePago, LocalDateTime desde, LocalDateTime hasta) {
    BigDecimal total =
        reciboRepository.getTotalRecibosProveedoresEntreFechasPorFormaDePago(
            idSucursal, idFormaDePago, desde, hasta);
    return (total == null) ? BigDecimal.ZERO : total;
  }

  @Override
  public BigDecimal getTotalRecibosClientesQueAfectanCajaEntreFechas(
      long idSucursal, LocalDateTime desde, LocalDateTime hasta) {
    BigDecimal total =
        reciboRepository.getTotalRecibosClientesQueAfectanCajaEntreFechas(idSucursal, desde, hasta);
    return (total == null) ? BigDecimal.ZERO : total;
  }

  @Override
  public BigDecimal getTotalRecibosProveedoresQueAfectanCajaEntreFechas(
      long idSucursal, LocalDateTime desde, LocalDateTime hasta) {
    BigDecimal total =
        reciboRepository.getTotalRecibosProveedoresQueAfectanCajaEntreFechas(
            idSucursal, desde, hasta);
    return (total == null) ? BigDecimal.ZERO : total;
  }

  @Override
  public BigDecimal getTotalRecibosClientesEntreFechas(long idSucursal, LocalDateTime desde, LocalDateTime hasta) {
    BigDecimal total = reciboRepository.getTotalRecibosClientesEntreFechas(idSucursal, desde, hasta);
    return (total == null) ? BigDecimal.ZERO : total;
  }

  @Override
  public BigDecimal getTotalRecibosProveedoresEntreFechas(long idSucursal, LocalDateTime desde, LocalDateTime hasta) {
    BigDecimal total =
        reciboRepository.getTotalRecibosProveedoresEntreFechas(idSucursal, desde, hasta);
    return (total == null) ? BigDecimal.ZERO : total;
  }
}

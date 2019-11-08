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
import javax.validation.Valid;

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
import org.springframework.validation.annotation.Validated;
import sic.modelo.*;
import sic.modelo.criteria.BusquedaReciboCriteria;
import sic.repository.ReciboRepository;
import sic.service.*;
import sic.exception.BusinessServiceException;
import sic.exception.ServiceException;

@Service
@Validated
public class ReciboServiceImpl implements IReciboService {

  private final ReciboRepository reciboRepository;
  private final ICuentaCorrienteService cuentaCorrienteService;
  private final IEmpresaService empresaService;
  private final IConfiguracionDelSistemaService configuracionDelSistemaService;
  private final INotaService notaService;
  private final IFormaDePagoService formaDePagoService;
  private final ICajaService cajaService;
  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private static final int TAMANIO_PAGINA_DEFAULT = 25;
  private final MessageSource messageSource;

  @Autowired
  @Lazy
  public ReciboServiceImpl(
      ReciboRepository reciboRepository,
      ICuentaCorrienteService cuentaCorrienteService,
      IEmpresaService empresaService,
      IConfiguracionDelSistemaService cds,
      INotaService notaService,
      IFormaDePagoService formaDePagoService,
      ICajaService cajaService,
      MessageSource messageSource) {
    this.reciboRepository = reciboRepository;
    this.cuentaCorrienteService = cuentaCorrienteService;
    this.empresaService = empresaService;
    this.configuracionDelSistemaService = cds;
    this.notaService = notaService;
    this.formaDePagoService = formaDePagoService;
    this.cajaService = cajaService;
    this.messageSource = messageSource;
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

  private BooleanBuilder getBuilder(BusquedaReciboCriteria criteria) {
    QRecibo qRecibo = QRecibo.recibo;
    BooleanBuilder builder = new BooleanBuilder();
    if (criteria.getFechaDesde() != null && criteria.getFechaHasta() != null) {
      criteria.setFechaDesde(criteria.getFechaDesde().withHour(0).withMinute(0).withSecond(0));
      criteria.setFechaHasta(criteria.getFechaHasta().withHour(23).withMinute(59).withSecond(59).withNano(999999999));
      builder.and(qRecibo.fecha.between(criteria.getFechaDesde(), criteria.getFechaHasta()));
    }
    if (criteria.getNumSerie() != null && criteria.getNumRecibo() != null)
      builder
          .and(qRecibo.numSerie.eq(criteria.getNumSerie()))
          .and(qRecibo.numRecibo.eq(criteria.getNumRecibo()));
    if (criteria.getConcepto() != null) {
      String[] terminos = criteria.getConcepto().split(" ");
      BooleanBuilder rsPredicate = new BooleanBuilder();
      for (String termino : terminos) {
        rsPredicate.and(qRecibo.concepto.containsIgnoreCase(termino));
      }
      builder.or(rsPredicate);
    }
    if (criteria.getIdCliente() != null)
      builder.and(qRecibo.cliente.id_Cliente.eq(criteria.getIdCliente()));
    if (criteria.getIdProveedor() != null)
      builder.and(qRecibo.proveedor.id_Proveedor.eq(criteria.getIdProveedor()));
    if (criteria.getIdUsuario() != null)
      builder.and(qRecibo.usuario.id_Usuario.eq(criteria.getIdUsuario()));
    if (criteria.getIdViajante() != null)
      builder.and(qRecibo.cliente.viajante.id_Usuario.eq(criteria.getIdViajante()));
    if (criteria.getMovimiento() == Movimiento.VENTA) builder.and(qRecibo.proveedor.isNull());
    else if (criteria.getMovimiento() == Movimiento.COMPRA) builder.and(qRecibo.cliente.isNull());
    builder.and(
      qRecibo.empresa.idEmpresa.eq(criteria.getIdEmpresa()).and(qRecibo.eliminado.eq(false)));
    return builder;
  }

  @Override
  public Page<Recibo> buscarRecibos(BusquedaReciboCriteria criteria) {
    return reciboRepository.findAll(
        this.getBuilder(criteria),
        this.getPageable(criteria.getPagina(), criteria.getOrdenarPor(), criteria.getSentido()));
  }

  private Pageable getPageable(int pagina, String ordenarPor, String sentido) {
    String ordenDefault = "fecha";
    if (ordenarPor == null || sentido == null) {
      return PageRequest.of(
          pagina, TAMANIO_PAGINA_DEFAULT, new Sort(Sort.Direction.DESC, ordenDefault));
    } else {
      switch (sentido) {
        case "ASC":
          return PageRequest.of(
              pagina, TAMANIO_PAGINA_DEFAULT, new Sort(Sort.Direction.ASC, ordenarPor));
        case "DESC":
          return PageRequest.of(
              pagina, TAMANIO_PAGINA_DEFAULT, new Sort(Sort.Direction.DESC, ordenarPor));
        default:
          return PageRequest.of(
              pagina, TAMANIO_PAGINA_DEFAULT, new Sort(Sort.Direction.DESC, ordenDefault));
      }
    }
  }

  @Override
  public BigDecimal getTotalRecibos(BusquedaReciboCriteria criteria) {
    return reciboRepository.getTotalRecibos(this.getBuilder(criteria));
  }

  @Override
  @Transactional
  public Recibo guardar(@Valid Recibo recibo) {
    recibo.setNumSerie(
        configuracionDelSistemaService
            .getConfiguracionDelSistemaPorEmpresa(recibo.getEmpresa())
            .getNroPuntoDeVentaAfip());
    recibo.setNumRecibo(
        this.getSiguienteNumeroRecibo(
            recibo.getEmpresa().getIdEmpresa(),
            configuracionDelSistemaService
                .getConfiguracionDelSistemaPorEmpresa(recibo.getEmpresa())
                .getNroPuntoDeVentaAfip()));
    this.validarOperacion(recibo);
    recibo = reciboRepository.save(recibo);
    this.cuentaCorrienteService.asentarEnCuentaCorriente(recibo, TipoDeOperacion.ALTA);
    logger.warn("El Recibo {} se guardó correctamente.", recibo);
    return recibo;
  }

  @Override
  public void validarOperacion(Recibo recibo) {
    // Muteado momentaneamente por el problema del alta de recibo generado por sic-com cuando la caja esta cerrada
    // this.cajaService.validarMovimiento(recibo.getFecha(), recibo.getEmpresa().getId_Empresa());
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
      LocalDateTime fecha) {
    List<Recibo> recibos = new ArrayList<>();
    if (idsFormaDePago != null && montos != null && idsFormaDePago.length == montos.length) {
      BigDecimal totalMontos = BigDecimal.ZERO;
      for (BigDecimal monto : montos) {
        totalMontos = totalMontos.add(monto);
      }
//      if (totalMontos.compareTo(totalFactura) > 0 || totalMontos.compareTo(BigDecimal.ZERO) < 0) {
//        throw new BusinessServiceException(messageSource.getMessage(
//          "mensaje_recibo_superan_total_factura", null, Locale.getDefault()));
//      }
      int i = 0;
      for (long idFormaDePago : idsFormaDePago) {
        Recibo recibo = new Recibo();
        recibo.setCliente(cliente);
        recibo.setUsuario(usuario);
        recibo.setEmpresa(empresa);
        recibo.setFecha(fecha);
        FormaDePago fdp = formaDePagoService.getFormasDePagoNoEliminadoPorId(idFormaDePago);
        recibo.setFormaDePago(fdp);
        recibo.setMonto(montos[i]);
        recibo.setNumSerie(
            configuracionDelSistemaService
                .getConfiguracionDelSistemaPorEmpresa(recibo.getEmpresa())
                .getNroPuntoDeVentaAfip());
        recibo.setNumRecibo(
            this.getSiguienteNumeroRecibo(empresa.getIdEmpresa(), recibo.getNumSerie()));
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
            recibo.getEmpresa().getIdEmpresa(), recibo.getFecha());
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
    LocalDateTime desde, LocalDateTime hasta, FormaDePago formaDePago, Empresa empresa) {
    return reciboRepository.getRecibosEntreFechasPorFormaDePago(
        empresa.getIdEmpresa(), formaDePago.getId_FormaDePago(), desde, hasta);
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
    if (recibo.getEmpresa().getLogo() != null && !recibo.getEmpresa().getLogo().isEmpty()) {
      try {
        params.put(
            "logo", new ImageIcon(ImageIO.read(new URL(recibo.getEmpresa().getLogo()))).getImage());
      } catch (IOException ex) {
        logger.error(ex.getMessage());
        throw new ServiceException(messageSource.getMessage(
          "mensaje_empresa_404_logo", null, Locale.getDefault()), ex);
      }
    }
    try {
      return JasperExportManager.exportReportToPdf(
          JasperFillManager.fillReport(isFileReport, params));
    } catch (JRException ex) {
      logger.error(ex.getMessage());
      throw new ServiceException(messageSource.getMessage(
        "mensaje_error_reporte", null, Locale.getDefault()), ex);
    }
  }

  @Override
  public BigDecimal getTotalRecibosClientesEntreFechasPorFormaDePago(
      long idEmpresa, long idFormaDePago, LocalDateTime desde, LocalDateTime hasta) {
    BigDecimal total =
        reciboRepository.getTotalRecibosClientesEntreFechasPorFormaDePago(
            idEmpresa, idFormaDePago, desde, hasta);
    return (total == null) ? BigDecimal.ZERO : total;
  }

  @Override
  public BigDecimal getTotalRecibosProveedoresEntreFechasPorFormaDePago(
      long idEmpresa, long idFormaDePago, LocalDateTime desde, LocalDateTime hasta) {
    BigDecimal total =
        reciboRepository.getTotalRecibosProveedoresEntreFechasPorFormaDePago(
            idEmpresa, idFormaDePago, desde, hasta);
    return (total == null) ? BigDecimal.ZERO : total;
  }

  @Override
  public BigDecimal getTotalRecibosClientesQueAfectanCajaEntreFechas(
      long idEmpresa, LocalDateTime desde, LocalDateTime hasta) {
    BigDecimal total =
        reciboRepository.getTotalRecibosClientesQueAfectanCajaEntreFechas(idEmpresa, desde, hasta);
    return (total == null) ? BigDecimal.ZERO : total;
  }

  @Override
  public BigDecimal getTotalRecibosProveedoresQueAfectanCajaEntreFechas(
      long idEmpresa, LocalDateTime desde, LocalDateTime hasta) {
    BigDecimal total =
        reciboRepository.getTotalRecibosProveedoresQueAfectanCajaEntreFechas(
            idEmpresa, desde, hasta);
    return (total == null) ? BigDecimal.ZERO : total;
  }

  @Override
  public BigDecimal getTotalRecibosClientesEntreFechas(long idEmpresa, LocalDateTime desde, LocalDateTime hasta) {
    BigDecimal total = reciboRepository.getTotalRecibosClientesEntreFechas(idEmpresa, desde, hasta);
    return (total == null) ? BigDecimal.ZERO : total;
  }

  @Override
  public BigDecimal getTotalRecibosProveedoresEntreFechas(long idEmpresa, LocalDateTime desde, LocalDateTime hasta) {
    BigDecimal total =
        reciboRepository.getTotalRecibosProveedoresEntreFechas(idEmpresa, desde, hasta);
    return (total == null) ? BigDecimal.ZERO : total;
  }
}

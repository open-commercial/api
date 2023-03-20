package sic.service.impl;

import com.querydsl.core.BooleanBuilder;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sic.domain.TipoDeOperacion;
import sic.entity.*;
import sic.exception.BusinessServiceException;
import sic.exception.ServiceException;
import sic.entity.criteria.BusquedaRemitoCriteria;
import sic.dto.NuevoRemitoDTO;
import sic.repository.RemitoRepository;
import sic.repository.RenglonRemitoRepository;
import sic.service.*;
import sic.util.CustomValidator;

import javax.imageio.ImageIO;
import javax.persistence.EntityNotFoundException;
import javax.swing.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
public class RemitoServiceImpl implements IRemitoService {

    private final IFacturaService facturaService;
    private final IFacturaVentaService facturaVentaService;
    private final RemitoRepository remitoRepository;
    private final RenglonRemitoRepository renglonRemitoRepository;
    private final IClienteService clienteService;
    private final IUsuarioService usuarioService;
    private final ITransportistaService transportistaService;
    private final ICuentaCorrienteService cuentaCorrienteService;
    private final MessageSource messageSource;
    private static final int TAMANIO_PAGINA_DEFAULT = 25;
    private final CustomValidator customValidator;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public RemitoServiceImpl (IFacturaService facturaService,
                              IFacturaVentaService facturaVentaService,
                              RemitoRepository remitoRepository,
                              RenglonRemitoRepository renglonRemitoRepository,
                              IClienteService clienteService,
                              IUsuarioService usuarioService,
                              ITransportistaService transportistaService,
                              ICuentaCorrienteService cuentaCorrienteService,
                              MessageSource messageSource,
                              CustomValidator customValidator) {
        this.facturaService = facturaService;
        this.facturaVentaService = facturaVentaService;
        this.remitoRepository = remitoRepository;
        this.renglonRemitoRepository = renglonRemitoRepository;
        this.clienteService = clienteService;
        this.usuarioService = usuarioService;
        this.transportistaService = transportistaService;
        this.cuentaCorrienteService = cuentaCorrienteService;
        this.messageSource = messageSource;
        this.customValidator = customValidator;
    }

    @Override
    public Remito getRemitoPorId(long idRemito) {
        Optional<Remito> remito = remitoRepository.findById(idRemito);
        if (remito.isPresent() && !remito.get().isEliminado()) {
            return remito.get();
        } else {
            throw new EntityNotFoundException(messageSource.getMessage(
                    "mensaje_remito_no_existente", null, Locale.getDefault()));
        }
    }

    @Override
    public Remito crearRemitoDeFacturasVenta(NuevoRemitoDTO nuevoRemitoDTO, long idUsuario) {
       if (nuevoRemitoDTO.getCostoDeEnvio() == null) {
           throw new BusinessServiceException(
                   messageSource.getMessage(
                           "mensaje_remito_sin_costo_de_envio", null, Locale.getDefault()));
       }
       List<FacturaVenta> facturas = new ArrayList<>();
       this.validarReglasDeNegocio(facturas, nuevoRemitoDTO.getIdFacturaVenta());
       Pedido pedido = facturas.get(0).getPedido();
       Remito remito = new Remito();
       remito.setFecha(LocalDateTime.now());
       remito.setDetalleEnvio(pedido.getDetalleEnvio());
       remito.setCliente(pedido.getCliente());
       remito.setClienteEmbedded(clienteService.crearClienteEmbedded(pedido.getCliente()));
       BigDecimal totalFacturas = facturas.stream().map(FacturaVenta::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
       remito.setTotalFacturas(totalFacturas);
       remito.setCostoDeEnvio(nuevoRemitoDTO.getCostoDeEnvio());
       remito.setTotal(totalFacturas.add(remito.getCostoDeEnvio()));
       remito.setRenglones(this.construirRenglonesDeRemito(nuevoRemitoDTO));
       remito.setCantidadDeBultos(remito.getRenglones().stream()
                   .map(RenglonRemito::getCantidad)
                   .reduce(BigDecimal.ZERO, BigDecimal::add));
       remito.setPesoTotalEnKg(nuevoRemitoDTO.getPesoTotalEnKg());
       remito.setVolumenTotalEnM3(nuevoRemitoDTO.getVolumenTotalEnM3());
       remito.setObservaciones(nuevoRemitoDTO.getObservaciones());
       remito.setSucursal(pedido.getSucursal());
       remito.setSerie(remito.getSucursal().getConfiguracionSucursal().getNroPuntoDeVentaAfip());
       remito.setNroRemito(this.getSiguienteNumeroRemito(remito.getSerie()));
       remito.setUsuario(usuarioService.getUsuarioNoEliminadoPorId(idUsuario));
       remito.setTransportista(transportistaService.getTransportistaNoEliminadoPorId(nuevoRemitoDTO.getIdTransportista()));
       customValidator.validar(remito);
       remitoRepository.save(remito);
       logger.warn(
               messageSource.getMessage(
                       "mensaje_remito_guardado_correctamente",
                       new Object[] {remito},
                       Locale.getDefault()));
       facturas.forEach(facturaVenta -> facturaVentaService.asignarRemitoConFactura(remito, facturaVenta.getIdFactura()));
       cuentaCorrienteService.asentarEnCuentaCorriente(remito, TipoDeOperacion.ALTA);
       return remito;
    }

    @Override
    public void validarReglasDeNegocio(List<FacturaVenta> facturas, long[] idFacturaVenta) {
        Arrays.stream(idFacturaVenta).forEach(idFactura -> {
            Factura factura = facturaService.getFacturaNoEliminadaPorId(idFactura);
            if (!(factura instanceof FacturaVenta)) {
                throw new BusinessServiceException(
                        messageSource.getMessage(
                                "mensaje_tipo_de_comprobante_no_valido", null, Locale.getDefault()));
            }
            FacturaVenta facturaVenta = (FacturaVenta) factura;
            if (facturaVenta.getRemito() != null) {
                throw new BusinessServiceException(
                        messageSource.getMessage(
                                "mensaje_factura_con_remito", null, Locale.getDefault()));
            }
            facturas.add(facturaVenta);
        });
        facturas.forEach(facturaVenta -> {
            if (Collections.frequency(facturas, facturaVenta) > 1)
                throw new BusinessServiceException(
                        messageSource.getMessage(
                                "mensaje_remito_facturas_iguales", null, Locale.getDefault()));
        });
        Pedido pedido = facturas.get(0).getPedido();
        facturas.stream()
                .filter(facturaVenta -> !facturaVenta.getPedido().equals(pedido))
                .forEach(facturaVenta -> {
                    throw new BusinessServiceException(
                            messageSource.getMessage(
                                    "mensaje_remito_facturas_diferentes_pedidos", null, Locale.getDefault()));
                });
    }

    @Override
    public List<RenglonRemito> construirRenglonesDeRemito(NuevoRemitoDTO nuevoRemitoDTO) {
      List<RenglonRemito> renglonesParaRemito = new ArrayList<>();
      if (nuevoRemitoDTO.getCantidadPorBulto().length != nuevoRemitoDTO.getTiposDeBulto().length) {
          throw new BusinessServiceException(
                  messageSource.getMessage(
                          "mensaje_remito_error_renglones", null, Locale.getDefault()));
      }
      for (int i = 0; i < nuevoRemitoDTO.getTiposDeBulto().length; i++) {
          RenglonRemito renglonRemito = new RenglonRemito();
          renglonRemito.setCantidad(nuevoRemitoDTO.getCantidadPorBulto()[i]);
          renglonRemito.setTipoBulto(nuevoRemitoDTO.getTiposDeBulto()[i].toString());
          renglonesParaRemito.add(renglonRemito);
      }
      return renglonesParaRemito;
    }

    @Override
    public void eliminar(long idRemito) {
        Remito remito = this.getRemitoPorId(idRemito);
        cuentaCorrienteService.asentarEnCuentaCorriente(remito, TipoDeOperacion.ELIMINACION);
        facturaVentaService.getFacturaVentaDelRemito(remito).forEach(
                facturaVenta -> facturaVentaService.asignarRemitoConFactura(null, facturaVenta.getIdFactura())
        );
        remito.setEliminado(true);
        remito = remitoRepository.save(remito);
        logger.warn(
                messageSource.getMessage(
                        "mensaje_remito_eliminado_correctamente",
                        new Object[] {remito},
                        Locale.getDefault()));
    }

    @Override
    public long getSiguienteNumeroRemito(Long nroSerie) {
        Long numeroNota =
                remitoRepository.buscarMayorNumRemitoSegunSerie(nroSerie);
        return (numeroNota == null) ? 1 : numeroNota + 1;
    }

    @Override
    public List<RenglonRemito> getRenglonesDelRemito(long idRemito) {
        return renglonRemitoRepository.findByIdRemitoOrderByIdRenglonRemito(idRemito);
    }

    @Override
    public Page<Remito> buscarRemito(BusquedaRemitoCriteria criteria) {
        return remitoRepository.findAll(this.getBuilder(criteria), this.getPageable(criteria.getPagina(), criteria.getOrdenarPor(), criteria.getSentido()));
    }

    @Override
    public Pageable getPageable(Integer pagina, String ordenarPor, String sentido) {
        if (pagina == null) pagina = 0;
        String ordenDefault = "fecha";
        if (ordenarPor == null || sentido == null) {
            return PageRequest.of(
                    pagina, TAMANIO_PAGINA_DEFAULT, Sort.by(Sort.Direction.DESC, ordenDefault));
        } else {
            return switch (sentido) {
                case "ASC" -> PageRequest.of(
                        pagina, TAMANIO_PAGINA_DEFAULT, Sort.by(Sort.Direction.ASC, ordenarPor));
                case "DESC" -> PageRequest.of(
                        pagina, TAMANIO_PAGINA_DEFAULT, Sort.by(Sort.Direction.DESC, ordenarPor));
                default -> PageRequest.of(
                        pagina, TAMANIO_PAGINA_DEFAULT, Sort.by(Sort.Direction.DESC, ordenDefault));
            };
        }
    }

    @Override
    public BooleanBuilder getBuilder(BusquedaRemitoCriteria criteria) {
        QRemito qRemito = QRemito.remito;
        BooleanBuilder builder = new BooleanBuilder();
        if (criteria.getFechaDesde() != null || criteria.getFechaHasta() != null) {
            if (criteria.getFechaDesde() != null && criteria.getFechaHasta() != null) {
                criteria.setFechaDesde(criteria.getFechaDesde().withHour(0).withMinute(0).withSecond(0).withNano(0));
                criteria.setFechaHasta(
                        criteria
                                .getFechaHasta()
                                .withHour(23)
                                .withMinute(59)
                                .withSecond(59)
                                .withNano(999999999));
                builder.and(
                        qRemito.fecha.between(criteria.getFechaDesde(), criteria.getFechaHasta()));
            } else if (criteria.getFechaDesde() != null) {
                criteria.setFechaDesde(criteria.getFechaDesde().withHour(0).withMinute(0).withSecond(0).withNano(0));
                builder.and(qRemito.fecha.after(criteria.getFechaDesde()));
            } else if (criteria.getFechaHasta() != null) {
                criteria.setFechaHasta(
                        criteria
                                .getFechaHasta()
                                .withHour(23)
                                .withMinute(59)
                                .withSecond(59)
                                .withNano(999999999));
                builder.and(qRemito.fecha.before(criteria.getFechaHasta()));
            }
        }
        if (criteria.getSerieRemito() != null && criteria.getNroRemito() != null) {
            builder.and(qRemito.serie.eq(criteria.getSerieRemito()).and(qRemito.nroRemito.eq(criteria.getNroRemito())));
        }
        if (criteria.getIdCliente() != null) {
            builder.and(qRemito.cliente.idCliente.eq(criteria.getIdCliente()));
        }
        if (criteria.getIdSucursal() != null) {
            builder.and(qRemito.sucursal.idSucursal.eq(criteria.getIdSucursal()));
        }
        if (criteria.getIdUsuario() != null) {
            builder.and(qRemito.usuario.idUsuario.eq(criteria.getIdUsuario()));
        }
        if (criteria.getIdTransportista() != null) {
            builder.and(qRemito.transportista.idTransportista.eq(criteria.getIdTransportista()));
        }
        builder.and(qRemito.eliminado.eq(false));
        return builder;
    }

    @Override
    public byte[] getReporteRemito(long idRemito) {
        Remito remitoParaReporte = this.getRemitoPorId(idRemito);
        Map<String, Object> params = new HashMap<>();
        params.put("remito", remitoParaReporte);
        if (remitoParaReporte.getSucursal().getLogo() != null && !remitoParaReporte.getSucursal().getLogo().isEmpty()) {
            try {
                params.put(
                        "logo",
                        new ImageIcon(ImageIO.read(new URL(remitoParaReporte.getSucursal().getLogo()))).getImage());
            } catch (IOException ex) {
                throw new ServiceException(
                        messageSource.getMessage("mensaje_sucursal_404_logo", null, Locale.getDefault()), ex);
            }
        }
        List<RenglonRemito> renglones = this.getRenglonesDelRemito(idRemito);
        JRBeanCollectionDataSource ds = new JRBeanCollectionDataSource(renglones);
        JasperReport jasperDesign;
        try {
            var classLoader = this.getClass().getClassLoader();
            var isFileReport = classLoader.getResourceAsStream("report/Remito.jrxml");
            jasperDesign = JasperCompileManager.compileReport(isFileReport);
        } catch (JRException ex) {
            throw new ServiceException(messageSource.getMessage(
                    "mensaje_error_reporte", null, Locale.getDefault()), ex);
        }
        try {
            return JasperExportManager.exportReportToPdf(
                    JasperFillManager.fillReport(jasperDesign, params, ds));
        } catch (JRException ex) {
            throw new ServiceException(
                    messageSource.getMessage("mensaje_error_reporte", null, Locale.getDefault()), ex);
        }
    }
}

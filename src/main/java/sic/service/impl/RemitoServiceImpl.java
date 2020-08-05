package sic.service.impl;

import com.querydsl.core.BooleanBuilder;
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
import sic.exception.BusinessServiceException;
import sic.modelo.*;
import sic.modelo.criteria.BusquedaRemitoCriteria;
import sic.modelo.dto.NuevoRemitoDTO;
import sic.repository.RemitoRepository;
import sic.repository.RenglonRemitoRepository;
import sic.service.*;
import sic.util.CustomValidator;

import javax.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@Transactional
public class RemitoServiceImpl implements IRemitoService {

    private final IFacturaService facturaService;
    private final IFacturaVentaService facturaVentaService;
    private final RemitoRepository remitoRepository;
    private final RenglonRemitoRepository renglonRemitoRepository;
    private final IClienteService clienteService;
    private final IUsuarioService usuarioService;
    private final IConfiguracionSucursalService configuracionSucursalService;
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
                              IConfiguracionSucursalService configuracionSucursalService,
                              ICuentaCorrienteService cuentaCorrienteService,
                              MessageSource messageSource,
                              CustomValidator customValidator) {
        this.facturaService = facturaService;
        this.facturaVentaService = facturaVentaService;
        this.remitoRepository = remitoRepository;
        this.renglonRemitoRepository = renglonRemitoRepository;
        this.clienteService = clienteService;
        this.usuarioService = usuarioService;
        this.configuracionSucursalService = configuracionSucursalService;
        this.cuentaCorrienteService = cuentaCorrienteService;
        this.messageSource = messageSource;
        this.customValidator = customValidator;
    }

    @Override
    public Remito getRemitoPorId(long idRemito) {
        Optional<Remito> remito = remitoRepository.findById(idRemito);
        if (remito.isPresent()) {
            return remito.get();
        } else {
            throw new EntityNotFoundException(messageSource.getMessage(
                    "mensaje_remito_no_existente", null, Locale.getDefault()));
        }
    }

    @Override
    public Remito crearRemitoDeFacturaVenta(NuevoRemitoDTO nuevoRemitoDTO, long idUsuario) {
       Factura factura = facturaService.getFacturaNoEliminadaPorId(nuevoRemitoDTO.getIdFacturaVenta());
       if (!(factura instanceof FacturaVenta)) {
           throw new BusinessServiceException(
                   messageSource.getMessage(
                           "mensaje_tipo_de_comprobante_no_valido", null, Locale.getDefault()));
       } else {
       Remito remito = new Remito();
       FacturaVenta facturaVenta = (FacturaVenta) factura;
       remito.setFecha(LocalDateTime.now());
       remito.setDetalleEnvio(facturaVenta.getPedido().getDetalleEnvio());
       remito.setCliente(facturaVenta.getCliente());
       remito.setClienteEmbedded(clienteService.crearClienteEmbedded(facturaVenta.getCliente()));
       switch (facturaVenta.getTipoComprobante()) {
           case FACTURA_A -> remito.setTipoComprobante(TipoDeComprobante.REMITO_A);
           case FACTURA_B -> remito.setTipoComprobante(TipoDeComprobante.REMITO_B);
           case FACTURA_C -> remito.setTipoComprobante(TipoDeComprobante.REMITO_C);
           case FACTURA_X -> remito.setTipoComprobante(TipoDeComprobante.REMITO_X);
           case PRESUPUESTO -> remito.setTipoComprobante(TipoDeComprobante.PRESUPUESTO);
           default ->
                   throw new BusinessServiceException(
                   messageSource.getMessage(
                           "mensaje_remito_tipo_no_valido", null, Locale.getDefault()));
       }
       if (nuevoRemitoDTO.isDividir()) {
         remito.setTotal(
             facturaVenta
                 .getPedido()
                 .getDetalleEnvio()
                 .getCostoDeEnvio()
                 .divide(new BigDecimal("2"), RoundingMode.HALF_UP));
        } else {
         remito.setTotal(facturaVenta.getPedido().getDetalleEnvio().getCostoDeEnvio());
       }
       remito.setRenglones(this.construirRenglonesDeRemito(nuevoRemitoDTO));
       remito.setContraEntrega(nuevoRemitoDTO.isContraEntrega());
       remito.setSucursal(facturaVenta.getSucursal());
       remito.setSerie(configuracionSucursalService
            .getConfiguracionSucursal(remito.getSucursal())
            .getNroPuntoDeVentaAfip());
       remito.setNroRemito(this.getSiguienteNumeroRemito(remito.getTipoComprobante(), remito.getSerie()));
       remito.setUsuario(usuarioService.getUsuarioNoEliminadoPorId(idUsuario));
       customValidator.validar(remito);
       remitoRepository.save(remito);
       logger.warn(
               messageSource.getMessage(
                       "mensaje_remito_guardado_correctamente",
                       new Object[] {remito},
                       Locale.getDefault()));
       facturaVentaService.asignarRemitoConFactura(remito, facturaVenta.getIdFactura());
       cuentaCorrienteService.asentarEnCuentaCorriente(remito, TipoDeOperacion.ALTA);
       return remito;
       }
    }

    @Override
    public List<RenglonRemito> construirRenglonesDeRemito(NuevoRemitoDTO nuevoRemitoDTO) {
      List<RenglonRemito> renglonesParaRemito = new ArrayList<>();
      if (nuevoRemitoDTO.getCantidadDeBultos().length != nuevoRemitoDTO.getTiposDeBulto().length) {
          throw new BusinessServiceException(
                  messageSource.getMessage(
                          "mensaje_remito_error_renglones", null, Locale.getDefault()));
      }
      for (int i = 0; i < nuevoRemitoDTO.getTiposDeBulto().length; i++) {
          RenglonRemito renglonRemito = new RenglonRemito();
          renglonRemito.setCantidad(nuevoRemitoDTO.getCantidadDeBultos()[i]);
          renglonRemito.setTipoBulto(nuevoRemitoDTO.getTiposDeBulto()[i]);
          renglonesParaRemito.add(renglonRemito);
      }
      return renglonesParaRemito;
    }

    @Override
    public void eliminar(long idRemito) {
        Remito remito = this.getRemitoPorId(idRemito);
        cuentaCorrienteService.asentarEnCuentaCorriente(remito, TipoDeOperacion.ELIMINACION);
        facturaVentaService.asignarRemitoConFactura(null, facturaVentaService.getFacturaVentaDelRemito(remito).getIdFactura());
        remitoRepository.delete(remito);
        logger.warn(
                messageSource.getMessage(
                        "mensaje_remito_eliminado_correctamente",
                        new Object[] {remito},
                        Locale.getDefault()));
    }

    @Override
    public  long getSiguienteNumeroRemito(TipoDeComprobante tipoDeComprobante, Long nroSerie) {
        Long numeroNota =
                remitoRepository.buscarMayorNumRemitoSegunTipo(tipoDeComprobante, nroSerie);
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
            criteria.setFechaDesde(criteria.getFechaDesde().withHour(0).withMinute(0).withSecond(0));
            criteria.setFechaHasta(criteria.getFechaHasta().withHour(23).withMinute(59).withSecond(59).withNano(999999999));
            if (criteria.getFechaDesde() != null && criteria.getFechaHasta() != null) {
                builder.and(
                        qRemito.fecha.between(criteria.getFechaDesde(), criteria.getFechaHasta()));
            } else if (criteria.getFechaDesde() != null) {
                builder.and(qRemito.fecha.after(criteria.getFechaDesde()));
            } else if (criteria.getFechaHasta() != null) {
                builder.and(qRemito.fecha.before(criteria.getFechaHasta()));
            }
        }
        if (criteria.getSerie() != null && criteria.getNroRemito() != null) {
            builder.and(qRemito.serie.eq(criteria.getSerie()).and(qRemito.nroRemito.eq(criteria.getNroRemito())));
        }
        if (criteria.getTipoDeRemito() != null) {
            builder.and(qRemito.tipoComprobante.eq(criteria.getTipoDeRemito()));
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
        if (criteria.getContraEntrega() != null) {
            builder.and(qRemito.contraEntrega.eq(criteria.getContraEntrega()));
        }
        return builder;
    }
}

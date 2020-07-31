package sic.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sic.exception.BusinessServiceException;
import sic.modelo.*;
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
       remito.setRenglones(this.construirRenglonesDeRemito(facturaVenta));
       remito.setContraEntrega(nuevoRemitoDTO.isContraEntrega());
       remito.setSucursal(facturaVenta.getSucursal());
       remito.setSerie(configuracionSucursalService
            .getConfiguracionSucursal(remito.getSucursal())
            .getNroPuntoDeVentaAfip());
       remito.setNroRemito(this.getSiguienteNumeroRemito(remito.getTipoComprobante(), remito.getSerie()));
       remito.setUsuario(usuarioService.getUsuarioNoEliminadoPorId(idUsuario));
       remitoRepository.save(remito);
       facturaVentaService.asignarRemitoConFactura(remito, facturaVenta.getIdFactura());
       cuentaCorrienteService.asentarEnCuentaCorriente(remito, TipoDeOperacion.ALTA);
       return remito;
       }
    }

    @Override
    public List<RenglonRemito> construirRenglonesDeRemito(FacturaVenta facturaVenta) {
      List<RenglonRemito> renglonesParaRemito = new ArrayList<>();
      List<RenglonFactura> renglonesDeFactura =
          facturaService.getRenglonesDeLaFactura(facturaVenta.getIdFactura());
      renglonesDeFactura.forEach(renglonFactura -> {
        RenglonRemito renglonRemito = new RenglonRemito();
        renglonRemito.setCantidad(renglonFactura.getCantidad());
        renglonRemito.setCodigoItem(renglonFactura.getCodigoItem());
        renglonRemito.setDescripcionItem(renglonFactura.getDescripcionItem());
        renglonRemito.setMedidaItem(renglonFactura.getMedidaItem());
        renglonesParaRemito.add(renglonRemito);
    });
      return renglonesParaRemito;
    }

    @Override
    public void eliminar(long idRemito) {
        Remito remito = this.getRemitoPorId(idRemito);
        cuentaCorrienteService.asentarEnCuentaCorriente(remito, TipoDeOperacion.ELIMINACION);
        facturaVentaService.asignarRemitoConFactura(null, facturaVentaService.getFacturaVentaDelRemito(remito).getIdFactura());
        remitoRepository.delete(remito);
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
}

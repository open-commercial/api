package sic.controller;

import java.math.BigDecimal;
import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import sic.aspect.AccesoRolesPermitidos;
import sic.modelo.*;
import sic.service.ICajaService;
import sic.service.IEmpresaService;
import sic.service.IFormaDePagoService;
import sic.service.IUsuarioService;

@RestController
@RequestMapping("/api/v1")
public class CajaController {
    
    private final ICajaService cajaService;    
    private final IEmpresaService empresaService;
    private final IUsuarioService usuarioService;
    private final IFormaDePagoService formaDePagoService;
    private static final int TAMANIO_PAGINA_DEFAULT = 50;

    @Value("${SIC_JWT_KEY}")
    private String secretkey;

  @Autowired
  public CajaController(
      ICajaService cajaService,
      IEmpresaService empresaService,
      IFormaDePagoService formaDePagoService,
      IUsuarioService usuarioService) {
    this.cajaService = cajaService;
    this.empresaService = empresaService;
    this.formaDePagoService = formaDePagoService;
    this.usuarioService = usuarioService;
  }

    @GetMapping("/cajas/{idCaja}")
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
    public Caja getCajaPorId(@PathVariable long idCaja) {
        return cajaService.getCajaPorId(idCaja);
    }

  @PostMapping("/cajas/apertura/empresas/{idEmpresa}/usuarios/{idUsuario}")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public Caja abrirCaja(
      @PathVariable long idEmpresa,
      @PathVariable long idUsuario,
      @RequestParam BigDecimal saldoApertura) {
    return cajaService.abrirCaja(
        empresaService.getEmpresaPorId(idEmpresa),
        usuarioService.getUsuarioPorId(idUsuario),
        saldoApertura);
  }

    @DeleteMapping("/cajas/{idCaja}")
    @AccesoRolesPermitidos(Rol.ADMINISTRADOR)
    public void eliminar(@PathVariable long idCaja) {
        cajaService.eliminar(idCaja);
    }
    
    @PutMapping("/cajas/{idCaja}/cierre")
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
    public Caja cerrarCaja(@PathVariable long idCaja,
                           @RequestParam BigDecimal monto,
                           @RequestParam long idUsuarioCierre) {
        return cajaService.cerrarCaja(idCaja, monto, idUsuarioCierre, false);
    }
    
    @GetMapping("/cajas/busqueda/criteria")
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
    public Page<Caja> getCajasCriteria(@RequestParam long idEmpresa,
                                       @RequestParam(required = false) Long desde,
                                       @RequestParam(required = false) Long hasta,
                                       @RequestParam(required = false) Long idUsuarioApertura,
                                       @RequestParam(required = false) Long idUsuarioCierre,
                                       @RequestParam(required = false) Integer pagina,
                                       @RequestParam(required = false) Integer tamanio) {
        Calendar fechaDesde = Calendar.getInstance();            
        Calendar fechaHasta = Calendar.getInstance();
        if (desde != null && hasta != null) {           
            fechaDesde.setTimeInMillis(desde);
            fechaHasta.setTimeInMillis(hasta);
        }
        if (tamanio == null || tamanio <= 0) tamanio = TAMANIO_PAGINA_DEFAULT;
        if (pagina == null || pagina < 0) pagina = 0;
        Pageable pageable = new PageRequest(pagina, tamanio, new Sort(Sort.Direction.DESC, "fechaApertura"));
        BusquedaCajaCriteria criteria = BusquedaCajaCriteria.builder()
                                        .buscaPorFecha((desde != null) && (hasta != null))
                                        .fechaDesde(fechaDesde.getTime())
                                        .fechaHasta(fechaHasta.getTime())
                                        .idEmpresa(idEmpresa)
                                        .cantidadDeRegistros(0)
                                        .buscaPorUsuarioApertura(idUsuarioApertura != null)
                                        .idUsuarioApertura(idUsuarioApertura)
                                        .buscaPorUsuarioCierre(idUsuarioCierre != null)
                                        .idUsuarioCierre(idUsuarioCierre)
                                        .pageable(pageable)
                                        .build();
        return cajaService.getCajasCriteria(criteria);
    }

  @GetMapping("/cajas/{idCaja}/movimientos")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public List<MovimientoCaja> getMovimientosDeCaja(
      @PathVariable long idCaja, @RequestParam long idFormaDePago) {
    Caja caja = cajaService.getCajaPorId(idCaja);
    Date fechaHasta = new Date();
    if (caja.getFechaCierre() != null) fechaHasta = caja.getFechaCierre();
    return cajaService.getMovimientosPorFormaDePagoEntreFechas(
        caja.getEmpresa(),
        formaDePagoService.getFormasDePagoPorId(idFormaDePago),
        caja.getFechaApertura(),
        fechaHasta);
  }

    @GetMapping("/cajas/{idCaja}/saldo-afecta-caja")
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
    public BigDecimal getSaldoQueAfectaCaja(@PathVariable long idCaja) {
        return cajaService.getSaldoQueAfectaCaja(cajaService.getCajaPorId(idCaja));
    }

    @GetMapping("/cajas/{idCaja}/saldo-sistema")
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
    public BigDecimal getSaldoSistema(@PathVariable long idCaja) {
        return cajaService.getSaldoSistema(cajaService.getCajaPorId(idCaja));
    }

    @GetMapping("/cajas/empresas/{idEmpresa}/estado-ultima-caja")
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
    public boolean getEstadoUltimaCaja(@PathVariable long idEmpresa) {
        return cajaService.isUltimaCajaAbierta(idEmpresa);
    }

  @GetMapping("/cajas/saldo-sistema")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public BigDecimal getSaldoSistemaCajas(
      @RequestParam long idEmpresa,
      @RequestParam(required = false) Long desde,
      @RequestParam(required = false) Long hasta,
      @RequestParam(required = false) Long idUsuarioApertura,
      @RequestParam(required = false) Long idUsuarioCierre) {
    Calendar fechaDesde = Calendar.getInstance();
    Calendar fechaHasta = Calendar.getInstance();
    if (desde != null && hasta != null) {
      fechaDesde.setTimeInMillis(desde);
      fechaHasta.setTimeInMillis(hasta);
    }
    BusquedaCajaCriteria criteria =
        BusquedaCajaCriteria.builder()
            .buscaPorFecha((desde != null) && (hasta != null))
            .fechaDesde(fechaDesde.getTime())
            .fechaHasta(fechaHasta.getTime())
            .idEmpresa(idEmpresa)
            .cantidadDeRegistros(0)
            .buscaPorUsuarioApertura(idUsuarioApertura != null)
            .idUsuarioApertura(idUsuarioApertura)
            .buscaPorUsuarioCierre(idUsuarioCierre != null)
            .idUsuarioCierre(idUsuarioCierre)
            .build();
    return cajaService.getSaldoSistemaCajas(criteria);
  }

  @GetMapping("/cajas/saldo-real")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public BigDecimal getSaldoRealCajas(
      @RequestParam long idEmpresa,
      @RequestParam(required = false) Long desde,
      @RequestParam(required = false) Long hasta,
      @RequestParam(required = false) Long idUsuarioApertura,
      @RequestParam(required = false) Long idUsuarioCierre) {
    Calendar fechaDesde = Calendar.getInstance();
    Calendar fechaHasta = Calendar.getInstance();
    if (desde != null && hasta != null) {
      fechaDesde.setTimeInMillis(desde);
      fechaHasta.setTimeInMillis(hasta);
    }
    BusquedaCajaCriteria criteria =
        BusquedaCajaCriteria.builder()
            .buscaPorFecha((desde != null) && (hasta != null))
            .fechaDesde(fechaDesde.getTime())
            .fechaHasta(fechaHasta.getTime())
            .idEmpresa(idEmpresa)
            .cantidadDeRegistros(0)
            .buscaPorUsuarioApertura(idUsuarioApertura != null)
            .idUsuarioApertura(idUsuarioApertura)
            .buscaPorUsuarioCierre(idUsuarioCierre != null)
            .idUsuarioCierre(idUsuarioCierre)
            .build();
    return cajaService.getSaldoRealCajas(criteria);
  }

    @GetMapping("/cajas/{idCaja}/totales-formas-de-pago")
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
    public Map<Long, BigDecimal> getTotalesPorFormaDePago(@PathVariable long idCaja) {
        return cajaService.getTotalesDeFormaDePago(idCaja);
    }

  @PutMapping("/cajas/{idCaja}/reapertura")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public void reabrirCaja(
      @PathVariable long idCaja,
      @RequestParam BigDecimal monto) {
    cajaService.reabrirCaja(idCaja, monto);
  }
}

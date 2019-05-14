package sic.controller;

import io.jsonwebtoken.Claims;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import sic.aspect.AccesoRolesPermitidos;
import sic.modelo.BusquedaGastoCriteria;
import sic.modelo.Gasto;
import sic.modelo.Rol;
import sic.modelo.dto.GastoDTO;
import sic.service.*;

import java.math.BigDecimal;
import java.util.Calendar;

@RestController
@RequestMapping("/api/v1")
public class GastoController {
    
    private final IGastoService gastoService;
    private final IEmpresaService empresaService;
    private final IFormaDePagoService formaDePagoService;
    private final IUsuarioService usuarioService;
    private final IAuthService authService;
    private final ModelMapper modelMapper;
    private static final int TAMANIO_PAGINA_DEFAULT = 25;
    
    @Autowired
    public GastoController(IGastoService gastoService, ModelMapper modelMapper,
                           IEmpresaService empresaService, IFormaDePagoService formaDePagoService,
                           IUsuarioService usuarioService, IAuthService authService) {
        this.gastoService = gastoService;
        this.empresaService = empresaService;
        this.formaDePagoService = formaDePagoService;
        this.usuarioService = usuarioService;
        this.authService = authService;
        this.modelMapper = modelMapper;
    }
    
    @GetMapping("/gastos/{idGasto}")
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
    public Gasto getGastoPorId(@PathVariable long idGasto) {
        return gastoService.getGastoPorId(idGasto);
    }

    @GetMapping("/gastos/busqueda/criteria")
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
    public Page<Gasto> buscarConCriteria(
      @RequestParam Long idEmpresa,
      @RequestParam(required = false) Long desde,
      @RequestParam(required = false) Long hasta,
      @RequestParam(required = false) String concepto,
      @RequestParam(required = false) Long idUsuario,
      @RequestParam(required = false) Long idFormaDePago,
      @RequestParam(required = false) Long nroGasto,
      @RequestParam(required = false) Integer pagina,
      @RequestParam(required = false) String ordenarPor,
      @RequestParam(required = false) String sentido) {
        if (pagina == null || pagina < 0) pagina = 0;
        Pageable pageable;
        if (ordenarPor == null || sentido == null) {
            pageable =
              new PageRequest(
                pagina, TAMANIO_PAGINA_DEFAULT, new Sort(Sort.Direction.DESC, "fecha"));
        } else {
            switch (sentido) {
                case "ASC":
                    pageable =
                      new PageRequest(
                        pagina, TAMANIO_PAGINA_DEFAULT, new Sort(Sort.Direction.ASC, ordenarPor));
                    break;
                case "DESC":
                    pageable =
                      new PageRequest(
                        pagina, TAMANIO_PAGINA_DEFAULT, new Sort(Sort.Direction.DESC, ordenarPor));
                    break;
                default:
                    pageable =
                      new PageRequest(
                        pagina, TAMANIO_PAGINA_DEFAULT, new Sort(Sort.Direction.DESC, "fecha"));
                    break;
            }
        }
        Calendar fechaDesde = Calendar.getInstance();
        Calendar fechaHasta = Calendar.getInstance();
        if ((desde != null) && (hasta != null)) {
            fechaDesde.setTimeInMillis(desde);
            fechaHasta.setTimeInMillis(hasta);
        }
        BusquedaGastoCriteria criteria =
          BusquedaGastoCriteria.builder()
            .buscaPorFecha((desde != null) && (hasta != null))
            .fechaDesde(fechaDesde.getTime())
            .fechaHasta(fechaHasta.getTime())
            .buscaPorConcepto(concepto != null)
            .concepto(concepto)
            .buscaPorUsuario(idUsuario != null)
            .idUsuario(idUsuario)
            .buscarPorFormaDePago(idFormaDePago != null)
            .idFormaDePago(idFormaDePago)
            .buscaPorNro(nroGasto != null)
            .nroGasto(nroGasto)
            .idEmpresa(idEmpresa)
            .pageable(pageable)
            .build();
        return gastoService.buscarGastos(criteria);
    }

    @DeleteMapping("/gastos/{idGasto}")
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
    public void eliminar(@PathVariable long idGasto) {
        gastoService.eliminar(idGasto);
    }

    @PostMapping("/gastos")
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
    public Gasto guardar(@RequestBody GastoDTO gastoDTO,
                         @RequestParam Long idEmpresa,
                         @RequestParam Long idFormaDePago,
                         @RequestHeader(name = "Authorization") String authorizationHeader) {
        Gasto gasto = modelMapper.map(gastoDTO, Gasto.class);
        gasto.setEmpresa(empresaService.getEmpresaPorId(idEmpresa));
        gasto.setFormaDePago(formaDePagoService.getFormasDePagoPorId(idFormaDePago));
        Claims claims = authService.getClaimsDelToken(authorizationHeader);
        long idUsuarioLoggedIn = (int) claims.get("idUsuario");
        gasto.setUsuario(usuarioService.getUsuarioPorId(idUsuarioLoggedIn));
        return gastoService.guardar(gasto);
    }

  @GetMapping("/gastos/total/criteria")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public BigDecimal getTotalGastos(
      @RequestParam Long idEmpresa,
      @RequestParam(required = false) Long desde,
      @RequestParam(required = false) Long hasta,
      @RequestParam(required = false) String concepto,
      @RequestParam(required = false) Long idUsuario,
      @RequestParam(required = false) Long idFormaDePago,
      @RequestParam(required = false) Long nroGasto,
      @RequestParam(required = false) Integer pagina,
      @RequestParam(required = false) String ordenarPor,
      @RequestParam(required = false) String sentido) {
    if (pagina == null || pagina < 0) pagina = 0;
    Pageable pageable;
    if (ordenarPor == null || sentido == null) {
      pageable =
          new PageRequest(pagina, TAMANIO_PAGINA_DEFAULT, new Sort(Sort.Direction.DESC, "fecha"));
    } else {
      switch (sentido) {
        case "ASC":
          pageable =
              new PageRequest(
                  pagina, TAMANIO_PAGINA_DEFAULT, new Sort(Sort.Direction.ASC, ordenarPor));
          break;
        case "DESC":
          pageable =
              new PageRequest(
                  pagina, TAMANIO_PAGINA_DEFAULT, new Sort(Sort.Direction.DESC, ordenarPor));
          break;
        default:
          pageable =
              new PageRequest(
                  pagina, TAMANIO_PAGINA_DEFAULT, new Sort(Sort.Direction.DESC, "fecha"));
          break;
      }
    }
    Calendar fechaDesde = Calendar.getInstance();
    Calendar fechaHasta = Calendar.getInstance();
    if ((desde != null) && (hasta != null)) {
      fechaDesde.setTimeInMillis(desde);
      fechaHasta.setTimeInMillis(hasta);
    }
    BusquedaGastoCriteria criteria =
        BusquedaGastoCriteria.builder()
            .buscaPorFecha((desde != null) && (hasta != null))
            .fechaDesde(fechaDesde.getTime())
            .fechaHasta(fechaHasta.getTime())
            .buscaPorConcepto(concepto != null)
            .concepto(concepto)
            .buscaPorUsuario(idUsuario != null)
            .idUsuario(idUsuario)
            .buscarPorFormaDePago(idFormaDePago != null)
            .idFormaDePago(idFormaDePago)
            .buscaPorNro(nroGasto != null)
            .nroGasto(nroGasto)
            .idEmpresa(idEmpresa)
            .pageable(pageable)
            .build();
    return gastoService.getTotalGastos(criteria);
  }
}

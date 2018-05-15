package sic.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import sic.modelo.BusquedaCajaCriteria;
import sic.modelo.Caja;
import sic.modelo.MovimientoCaja;
import sic.modelo.Usuario;
import sic.service.ICajaService;
import sic.service.IEmpresaService;
import sic.service.IFormaDePagoService;
import sic.service.IUsuarioService;
import io.jsonwebtoken.Jwts;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1")
public class CajaController {
    
    private final ICajaService cajaService;    
    private final IEmpresaService empresaService;
    private final IUsuarioService usuarioService;
    private final IFormaDePagoService formaDePagoService;
    private final int TAMANIO_PAGINA_DEFAULT = 50;

    @Value("${SIC_JWT_KEY}")
    private String secretkey;
    
    @Autowired
    public CajaController(ICajaService cajaService, IEmpresaService empresaService,
                          IFormaDePagoService formaDePagoService, IUsuarioService usuarioService) {
        this.cajaService = cajaService;        
        this.empresaService = empresaService;
        this.formaDePagoService = formaDePagoService;
        this.usuarioService = usuarioService;
    }
    
    @GetMapping("/cajas/{idCaja}")
    @ResponseStatus(HttpStatus.OK)
    public Caja getCajaPorId(@PathVariable long idCaja) {
        return cajaService.getCajaPorId(idCaja);
    }

    @PostMapping("/cajas/empresas/{idEmpresa}/usuarios/{idUsuario}/abrir")
    @ResponseStatus(HttpStatus.CREATED)
    public Caja abrirCaja(@PathVariable long idEmpresa ,
                          @PathVariable long idUsuario,
                          @RequestParam String observacion,
                          @RequestParam BigDecimal saldoApertura) {
        return cajaService.abrirCaja(empresaService.getEmpresaPorId(idEmpresa), usuarioService.getUsuarioPorId(idUsuario), observacion, saldoApertura);
    }

    @PutMapping("/cajas")
    @ResponseStatus(HttpStatus.OK)
    public void actualizar(@RequestBody Caja caja) {
        cajaService.actualizar(caja);        
    }
    
    @DeleteMapping("/cajas/{idCaja}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable long idCaja) {
        cajaService.eliminar(idCaja);
    }
    
    @PutMapping("/cajas/{idCaja}/cierre")
    @ResponseStatus(HttpStatus.OK)
    public Caja cerrarCaja(@PathVariable long idCaja,
                           @RequestParam BigDecimal monto,
                           @RequestParam long idUsuarioCierre) {
        return cajaService.cerrarCaja(idCaja, monto, idUsuarioCierre, false);
    }
    
    @GetMapping("/cajas/busqueda/criteria")
    @ResponseStatus(HttpStatus.OK)
    public Page<Caja> getCajasCriteria(@RequestParam(value = "idEmpresa") long idEmpresa,
                                       @RequestParam(value = "desde", required = false) Long desde,
                                       @RequestParam(value = "hasta", required = false) Long hasta,
                                       @RequestParam(value = "idUsuarioApertura", required = false) Long idUsuarioApertura,
                                       @RequestParam(value = "idUsuarioCierre", required = false) Long idUsuarioCierre,
                                       @RequestParam(required = false) Integer pagina,
                                       @RequestParam(required = false) Integer tamanio) {
        Calendar fechaDesde = Calendar.getInstance();            
        Calendar fechaHasta = Calendar.getInstance();
        if (desde != null && hasta != null) {           
            fechaDesde.setTimeInMillis(desde);
            fechaHasta.setTimeInMillis(hasta);
        }
        Usuario usuarioApertura = new Usuario();
        if(idUsuarioApertura != null) {
            usuarioApertura = usuarioService.getUsuarioPorId(idUsuarioApertura);
        }
        Usuario usuarioCierre = new Usuario();
        if(idUsuarioCierre != null) {
            usuarioCierre = usuarioService.getUsuarioPorId(idUsuarioCierre);
        }
        if (tamanio == null || tamanio <= 0) {
            tamanio = TAMANIO_PAGINA_DEFAULT;
        }
        if (pagina == null || pagina < 0) {
            pagina = 0;
        }
        Pageable pageable = new PageRequest(pagina, tamanio, new Sort(Sort.Direction.DESC, "fechaApertura"));
        BusquedaCajaCriteria criteria = BusquedaCajaCriteria.builder()
                                        .buscaPorFecha((desde != null) && (hasta != null))
                                        .fechaDesde(fechaDesde.getTime())
                                        .fechaHasta(fechaHasta.getTime())
                                        .empresa(empresaService.getEmpresaPorId(idEmpresa))
                                        .cantidadDeRegistros(0)
                                        .buscaPorUsuarioApertura(idUsuarioApertura != null)
                                        .usuarioApertura(usuarioApertura)
                                        .buscaPorUsuarioCierre(idUsuarioCierre != null)
                                        .usuarioCierre(usuarioCierre)
                                        .pageable(pageable)
                                        .build();
        return cajaService.getCajasCriteria(criteria);
    }
    
    @GetMapping("/cajas/{idCaja}/movimientos")
    @ResponseStatus(HttpStatus.OK)
    public List<MovimientoCaja> getMovimientosDeCaja(@PathVariable long idCaja,
                                                     @RequestParam(value = "idFormaDePago") long idFormaDePago) {
        Caja caja = cajaService.getCajaPorId(idCaja);
        LocalDateTime desde = caja.getFechaApertura().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        LocalDateTime hasta = caja.getFechaApertura().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        if (caja.getFechaCierre() == null) {
            hasta = hasta.withHour(23);
            hasta = hasta.withMinute(59);
            hasta = hasta.withSecond(59);
        } else {
            hasta = caja.getFechaCierre().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        }
        return cajaService.getMovimientosPorFormaDePagoEntreFechas(caja.getEmpresa(), formaDePagoService.getFormasDePagoPorId(idFormaDePago),
                Date.from(desde.atZone(ZoneId.systemDefault()).toInstant()), Date.from(hasta.atZone(ZoneId.systemDefault()).toInstant()));
    }

    @GetMapping("/cajas/{idCaja}/saldo-afecta-caja")
    @ResponseStatus(HttpStatus.OK)
    public BigDecimal getSaldoQueAfectaCaja(@PathVariable long idCaja) {
        return cajaService.getSaldoQueAfectaCaja(cajaService.getCajaPorId(idCaja));
    }

    @GetMapping("/cajas/{idCaja}/saldo-sistema")
    @ResponseStatus(HttpStatus.OK)
    public BigDecimal getSaldoSistema(@PathVariable long idCaja) {
        return cajaService.getSaldoSistema(cajaService.getCajaPorId(idCaja));
    }

    @GetMapping("/cajas/empresas/{idEmpresa}/estado-ultima-caja")
    @ResponseStatus(HttpStatus.OK)
    public boolean getEstadoUltimaCaja(@PathVariable long idEmpresa) {
        return cajaService.isUltimaCajaAbierta(idEmpresa);
    }

    @GetMapping("/cajas/saldo-sistema")
    @ResponseStatus(HttpStatus.OK)
    public BigDecimal getSaldoSistemaCajas(@RequestParam long idEmpresa,
                                           @RequestParam(value = "desde", required = false) Long desde,
                                           @RequestParam(value = "hasta", required = false) Long hasta,
                                           @RequestParam(value = "idUsuarioApertura", required = false) Long idUsuarioApertura,
                                           @RequestParam(value = "idUsuarioCierre", required = false) Long idUsuarioCierre) {
        Calendar fechaDesde = Calendar.getInstance();
        Calendar fechaHasta = Calendar.getInstance();
        if (desde != null && hasta != null) {
            fechaDesde.setTimeInMillis(desde);
            fechaHasta.setTimeInMillis(hasta);
        }
        Usuario usuarioApertura = new Usuario();
        if(idUsuarioApertura != null) {
            usuarioApertura = usuarioService.getUsuarioPorId(idUsuarioApertura);
        }
        Usuario usuarioCierre = new Usuario();
        if(idUsuarioCierre != null) {
            usuarioCierre = usuarioService.getUsuarioPorId(idUsuarioCierre);
        }
        BusquedaCajaCriteria criteria = BusquedaCajaCriteria.builder()
                .buscaPorFecha((desde != null) && (hasta != null))
                .fechaDesde(fechaDesde.getTime())
                .fechaHasta(fechaHasta.getTime())
                .empresa(empresaService.getEmpresaPorId(idEmpresa))
                .cantidadDeRegistros(0)
                .buscaPorUsuarioApertura(idUsuarioApertura != null)
                .usuarioApertura(usuarioApertura)
                .buscaPorUsuarioCierre(idUsuarioCierre != null)
                .usuarioCierre(usuarioCierre)
                .build();
        return cajaService.getSaldoSistemaCajas(criteria);
    }

    @GetMapping("/cajas/saldo-real")
    @ResponseStatus(HttpStatus.OK)
    public BigDecimal getSaldoRealCajas(@RequestParam long idEmpresa,
                                        @RequestParam(value = "desde", required = false) Long desde,
                                        @RequestParam(value = "hasta", required = false) Long hasta,
                                        @RequestParam(value = "idUsuarioApertura", required = false) Long idUsuarioApertura,
                                        @RequestParam(value = "idUsuarioCierre", required = false) Long idUsuarioCierre) {
        Calendar fechaDesde = Calendar.getInstance();
        Calendar fechaHasta = Calendar.getInstance();
        if (desde != null && hasta != null) {
            fechaDesde.setTimeInMillis(desde);
            fechaHasta.setTimeInMillis(hasta);
        }
        Usuario usuarioApertura = new Usuario();
        if (idUsuarioApertura != null) {
            usuarioApertura = usuarioService.getUsuarioPorId(idUsuarioApertura);
        }
        Usuario usuarioCierre = new Usuario();
        if (idUsuarioCierre != null) {
            usuarioCierre = usuarioService.getUsuarioPorId(idUsuarioCierre);
        }
        BusquedaCajaCriteria criteria = BusquedaCajaCriteria.builder()
                .buscaPorFecha((desde != null) && (hasta != null))
                .fechaDesde(fechaDesde.getTime())
                .fechaHasta(fechaHasta.getTime())
                .empresa(empresaService.getEmpresaPorId(idEmpresa))
                .cantidadDeRegistros(0)
                .buscaPorUsuarioApertura(idUsuarioApertura != null)
                .usuarioApertura(usuarioApertura)
                .buscaPorUsuarioCierre(idUsuarioCierre != null)
                .usuarioCierre(usuarioCierre)
                .build();
        return cajaService.getSaldoRealCajas(criteria);
    }

    @GetMapping("/cajas/{idCaja}/totales-formas-de-pago")
    @ResponseStatus(HttpStatus.OK)
    public Map<Long, BigDecimal> getTotalesPorFormaDePago(@PathVariable long idCaja) {
        return cajaService.getTotalesDeFormaDePago(idCaja);
    }

    @PutMapping("/cajas/{idCaja}/reabrir")
    @ResponseStatus(HttpStatus.OK)
    public void abrirCaja(@PathVariable long idCaja, @RequestParam BigDecimal monto, HttpServletRequest request) {
        Claims claims = Jwts.parser()
                .setSigningKey(secretkey)
                .parseClaimsJws(request.getHeader("Authorization").substring(7)) //token
                .getBody();
        cajaService.reabrirCaja(idCaja, monto, ((int) claims.get("idUsuario")));
    }

}

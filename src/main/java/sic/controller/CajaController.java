package sic.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import sic.modelo.BusquedaCajaCriteria;
import sic.modelo.Caja;
import sic.modelo.MovimientoCaja;
import sic.modelo.Usuario;
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
    private final int TAMANIO_PAGINA_DEFAULT = 50;
    
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
    
    @PutMapping("/cajas")
    @ResponseStatus(HttpStatus.OK)
    public void actualizar(@RequestBody Caja caja) {
        cajaService.actualizar(caja);        
    }
    
    @PostMapping("/cajas")
    @ResponseStatus(HttpStatus.CREATED)
    public Caja guardar(@RequestBody Caja caja) {
        return cajaService.guardar(caja);
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
                                       @RequestParam(value = "idUsuario", required = false) Long idUsuario,
                                       @RequestParam(required = false) Integer pagina,
                                       @RequestParam(required = false) Integer tamanio) {
        Calendar fechaDesde = Calendar.getInstance();            
        Calendar fechaHasta = Calendar.getInstance();
        if (desde != null && hasta != null) {           
            fechaDesde.setTimeInMillis(desde);
            fechaHasta.setTimeInMillis(hasta);
        }
        Usuario usuario = new Usuario();
        if(idUsuario != null) {
            usuario = usuarioService.getUsuarioPorId(idUsuario);
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
                                        .buscaPorUsuario(idUsuario != null)
                                        .usuario(usuario)
                                        .pageable(pageable)
                                        .build();
        return cajaService.getCajasCriteria(criteria);        
    }
    
    @GetMapping("/cajas/{idCaja}/movimientos")
    @ResponseStatus(HttpStatus.OK)
    public List<MovimientoCaja> getCajaPorId(@PathVariable long idCaja,
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

    @GetMapping("/cajas/{idCaja}/total-afecta-caja")
    @ResponseStatus(HttpStatus.OK)
    public BigDecimal getTotalQueAfectaCaja(@PathVariable long idCaja) {
        return cajaService.getTotalQueAfectaCaja(cajaService.getCajaPorId(idCaja));
    }

    @GetMapping("/cajas/empresas/{idEmpresa}/estado-ultima-caja")
    @ResponseStatus(HttpStatus.OK)
    public boolean getEstadoUltimaCaja(@PathVariable long idEmpresa) {
        return cajaService.isUltimaCajaAbierta(idEmpresa);
    }

    @GetMapping("/cajas/empresas/{idEmpresa}/saldo-sistema")
    @ResponseStatus(HttpStatus.OK)
    public BigDecimal getSaldoSistemaCajas(@PathVariable long idEmpresa,
                                           @RequestParam(value = "idUsuario", required = false) Long idUsuario,
                                           @RequestParam(value = "desde", required = false) Long desde,
                                           @RequestParam(value = "hasta", required = false) Long hasta) {
        Calendar fechaDesde = Calendar.getInstance();
        fechaDesde.add(Calendar.YEAR, -17); // Rango temporal hasta la implementacion de criteria builder
        Calendar fechaHasta = Calendar.getInstance();
        if (desde != null && hasta != null) {
            fechaDesde.setTimeInMillis(desde);
            fechaDesde.set(Calendar.HOUR_OF_DAY, 0);
            fechaDesde.set(Calendar.MINUTE, 0);
            fechaDesde.set(Calendar.SECOND, 0);
            fechaDesde.set(Calendar.MILLISECOND, 0);
            fechaHasta.setTimeInMillis(hasta);
            fechaHasta.set(Calendar.HOUR_OF_DAY, 23);
            fechaHasta.set(Calendar.MINUTE, 59);
            fechaHasta.set(Calendar.SECOND, 59);
            fechaHasta.set(Calendar.MILLISECOND, 0);
        }
        return cajaService.getSaldoSistemaCajas(idEmpresa, idUsuario, fechaDesde.getTime(), fechaHasta.getTime());
    }

    @GetMapping("/cajas/empresas/{idEmpresa}/saldo-real")
    @ResponseStatus(HttpStatus.OK)
    public BigDecimal getSaldoRealCajas(@PathVariable long idEmpresa,
                                        @RequestParam(value = "idUsuario", required = false) Long idUsuario,
                                        @RequestParam(value = "desde", required = false) Long desde,
                                        @RequestParam(value = "hasta", required = false) Long hasta) {
        Calendar fechaDesde = Calendar.getInstance();
        fechaDesde.add(Calendar.YEAR, -17); // Rango temporal hasta la implementacion de criteria builder
        Calendar fechaHasta = Calendar.getInstance();
        if (desde != null && hasta != null) {
            fechaDesde.setTimeInMillis(desde);
            fechaDesde.set(Calendar.HOUR_OF_DAY, 0);
            fechaDesde.set(Calendar.MINUTE, 0);
            fechaDesde.set(Calendar.SECOND, 0);
            fechaDesde.set(Calendar.MILLISECOND, 0);
            fechaHasta.setTimeInMillis(hasta);
            fechaHasta.set(Calendar.HOUR_OF_DAY, 23);
            fechaHasta.set(Calendar.MINUTE, 59);
            fechaHasta.set(Calendar.SECOND, 59);
            fechaHasta.set(Calendar.MILLISECOND, 0);
        }
        return cajaService.getSaldoRealCajas(idEmpresa, idUsuario, fechaDesde.getTime(), fechaHasta.getTime());
    }

    @GetMapping("/cajas/{idCaja}/totales-formas-de-pago")
    @ResponseStatus(HttpStatus.OK)
    public Map<Long, BigDecimal> getTotalesPorFormaDePago(@PathVariable long idCaja) {
        return cajaService.getTotalesDeFormaDePago(idCaja);
    }

}

package sic.controller;

import java.util.Calendar;
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
import sic.modelo.Usuario;
import sic.service.ICajaService;
import sic.service.IEmpresaService;
import sic.service.IUsuarioService;

@RestController
@RequestMapping("/api/v1")
public class CajaController {
    
    private final ICajaService cajaService;    
    private final IEmpresaService empresaService;
    private final IUsuarioService usuarioService;
    private final int TAMANIO_PAGINA_DEFAULT = 100;
    
    @Autowired
    public CajaController(ICajaService cajaService, IEmpresaService empresaService,
                          IUsuarioService usuarioService) {
        this.cajaService = cajaService;        
        this.empresaService = empresaService;
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
                           @RequestParam double monto,
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
    
    
    @GetMapping("/cajas/{idCaja}/total")
    public double getTotalCaja(@PathVariable long idCaja, 
                               @RequestParam(required = false) boolean soloAfectaCaja) {
        return cajaService.getTotalCaja(cajaService.getCajaPorId(idCaja), soloAfectaCaja);
    }
    
    @GetMapping("/cajas/empresas/{idEmpresa}/saldo-final")
    public double getSaldoFinalCajas(@PathVariable long idEmpresa,
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
        return cajaService.getSaldoFinalCajas(idEmpresa, idUsuario, fechaDesde.getTime(), fechaHasta.getTime());
    }

    @GetMapping("/cajas/empresas/{idEmpresa}/saldo-real")
    public double getSaldoRealCajas(@PathVariable long idEmpresa,
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
    
}

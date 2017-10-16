package sic.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
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
import sic.modelo.Gasto;
import sic.service.IGastoService;

@RestController
@RequestMapping("/api/v1")
public class GastoController {
    
    private final IGastoService gastoService;    
    
    @Autowired
    public GastoController(IGastoService gastoService) {
        this.gastoService = gastoService;        
    }
    
    @GetMapping("/gastos/{idGasto}")
    @ResponseStatus(HttpStatus.OK)
    public Gasto getGastoPorId(@PathVariable long idGasto) {
        return gastoService.getGastoPorId(idGasto);
    }
    
    @PutMapping("/gastos")
    @ResponseStatus(HttpStatus.OK)
    public void actualizar(@RequestBody Gasto gasto) {
        if (gastoService.getGastoPorId(gasto.getId_Gasto()) != null) {
            gastoService.actualizar(gasto);
        }
    }
    
    @DeleteMapping("/gastos/{idGasto}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable long idGasto) {
        gastoService.eliminar(idGasto);
    }
    
    @GetMapping("/gastos/busqueda")
    @ResponseStatus(HttpStatus.OK)
    public List<Gasto> getGastosPorCajaYFormaDePago(@RequestParam long idEmpresa,
                                                    @RequestParam long idFormaDePago,
                                                    @RequestParam long desde,
                                                    @RequestParam long hasta) {
        Date fechaDesde = new Date(desde);
        Date fechaHasta = new Date(hasta);
        return gastoService.getGastosEntreFechasYFormaDePago(idEmpresa, idFormaDePago, fechaDesde, fechaHasta);
    }
    
    @GetMapping("/gastos/total")
    @ResponseStatus(HttpStatus.OK)
    public double calcularTotalGastos(@RequestParam long[] idGasto) {
        List<Gasto> gastos = new ArrayList<>();
        for (long id : idGasto) {
            gastos.add(gastoService.getGastoPorId(id));
        }
        return gastoService.calcularTotalGastos(gastos);
    }
    
    @PostMapping("/gastos")
    @ResponseStatus(HttpStatus.CREATED)
    public Gasto guardar(@RequestBody Gasto gasto) {
        gastoService.validarGasto(gasto);
        return gastoService.guardar(gasto);
    }

}

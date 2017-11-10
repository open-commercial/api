package sic.controller;

import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import sic.modelo.CuentaCorriente;
import sic.modelo.RenglonCuentaCorriente;
import sic.service.ICuentaCorrienteService;

@RestController
@RequestMapping("/api/v1")
public class CuentaCorrienteController {
    
    private final ICuentaCorrienteService cuentaCorrienteService;
    private final int TAMANIO_PAGINA_DEFAULT = 100;
    
    @Autowired
    public CuentaCorrienteController(ICuentaCorrienteService cuentaCorrienteService) {
        this.cuentaCorrienteService = cuentaCorrienteService;
    }
    
    @DeleteMapping("/cuentas-corrientes/{idCuentaCorriente}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long idCuentaCorriente) {
        cuentaCorrienteService.eliminar(idCuentaCorriente);
    }
    
    @GetMapping("/cuentas-corrientes/{idCuentaCorriente}")
    @ResponseStatus(HttpStatus.OK)
    public CuentaCorriente getCuentaCorrientePorID(@PathVariable Long idCuentaCorriente) {
        return cuentaCorrienteService.getCuentaCorrientePorID(idCuentaCorriente);
    }
    
    @GetMapping("/cuentas-corrientes/clientes/{idCliente}")
    @ResponseStatus(HttpStatus.OK)
    public CuentaCorriente getCuentaCorrientePorCliente(@PathVariable Long idCliente) {
        return cuentaCorrienteService.getCuentaCorrientePorCliente(idCliente);
    }
    
    @GetMapping("/cuentas-corrientes/clientes/{idCliente}/saldo")
    @ResponseStatus(HttpStatus.OK)
    public double getSaldoCuentaCorriente(@PathVariable long idCliente) {
        return cuentaCorrienteService.getSaldoCuentaCorriente(idCliente, new Date());
    }
    
    @GetMapping("/cuentas-corrientes/{idCuentaCorriente}/renglones")
    @ResponseStatus(HttpStatus.OK)
    public Page<RenglonCuentaCorriente> getRenglonesCuentaCorriente(@PathVariable long idCuentaCorriente,
                                                                    @RequestParam(required = false) Integer pagina,
                                                                    @RequestParam(required = false) Integer tamanio) {  
        if (tamanio == null || tamanio <= 0) {
            tamanio = TAMANIO_PAGINA_DEFAULT;
        }
        if (pagina == null || pagina < 0) {
            pagina = 0;
        }
        Pageable pageable = new PageRequest(pagina, tamanio, new Sort(Sort.Direction.DESC, "fecha"));
        return cuentaCorrienteService.getRenglonesCuentaCorriente(idCuentaCorriente, pageable);
    }
    
}

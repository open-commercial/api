package sic.controller;

import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import sic.modelo.CuentaCorriente;
import sic.modelo.CuentaCorrienteCliente;
import sic.modelo.CuentaCorrienteProveedor;
import sic.modelo.RenglonCuentaCorriente;
import sic.service.IClienteService;
import sic.service.ICuentaCorrienteService;
import sic.service.IProveedorService;

@RestController
@RequestMapping("/api/v1")
public class CuentaCorrienteController {
    
    private final ICuentaCorrienteService cuentaCorrienteService;
    private final IProveedorService proveedorService;
    private final IClienteService clienteService;
    private final int TAMANIO_PAGINA_DEFAULT = 100;
    
    @Autowired
    public CuentaCorrienteController(ICuentaCorrienteService cuentaCorrienteService, IProveedorService proveedorService, IClienteService clienteService) {
        this.cuentaCorrienteService = cuentaCorrienteService;
        this.clienteService = clienteService;
        this.proveedorService = proveedorService;
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
    public CuentaCorrienteCliente getCuentaCorrientePorCliente(@PathVariable Long idCliente) {
        return cuentaCorrienteService.getCuentaCorrientePorCliente(clienteService.getClientePorId(idCliente));
    }
    
    @GetMapping("/cuentas-corrientes/proveedores/{idProveedor}")
    @ResponseStatus(HttpStatus.OK)
    public CuentaCorrienteProveedor getCuentaCorrientePorProveedor(@PathVariable Long idProveedor) {
        return cuentaCorrienteService.getCuentaCorrientePorProveedor(proveedorService.getProveedorPorId(idProveedor));
    }
    
    @GetMapping("/cuentas-corrientes/clientes/{idCliente}/saldo")
    @ResponseStatus(HttpStatus.OK)
    public BigDecimal getSaldoCuentaCorrienteCliente(@PathVariable long idCliente) {       
        return cuentaCorrienteService.getCuentaCorrientePorCliente(clienteService.getClientePorId(idCliente)).getSaldo();
    }
    
    @GetMapping("/cuentas-corrientes/proveedores/{idProveedor}/saldo")
    @ResponseStatus(HttpStatus.OK)
    public BigDecimal getSaldoCuentaCorrienteProveedor(@PathVariable long idProveedor) {       
        return cuentaCorrienteService.getCuentaCorrientePorProveedor(proveedorService.getProveedorPorId(idProveedor)).getSaldo();
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
        } // filtrar los resultados por roles
        Pageable pageable = new PageRequest(pagina, tamanio);
        return cuentaCorrienteService.getRenglonesCuentaCorriente(idCuentaCorriente, pageable);
    }
    
}

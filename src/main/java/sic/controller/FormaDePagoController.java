package sic.controller;

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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import sic.modelo.FormaDePago;
import sic.service.IEmpresaService;
import sic.service.IFormaDePagoService;

@RestController
@RequestMapping("/api/v1")
public class FormaDePagoController {
    
    private final IFormaDePagoService formaDePagoService;
    private final IEmpresaService empresaService;
    
    @Autowired
    public FormaDePagoController(IFormaDePagoService formaDePagoService, 
                                 IEmpresaService empresaService) {
        this.formaDePagoService = formaDePagoService;
        this.empresaService = empresaService;
    }
    
    @GetMapping("/formas-de-pago/{idFormaDePago}")
    @ResponseStatus(HttpStatus.OK)
    public FormaDePago getFormaDePagoPorId(@PathVariable long idFormaDePago) {
        return formaDePagoService.getFormasDePagoPorId(idFormaDePago);
    }
    
    @DeleteMapping("/formas-de-pago/{idFormaDePago}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable long idFormaDePago) {
        formaDePagoService.eliminar(idFormaDePago);
    }
    
    @GetMapping("/formas-de-pago/predeterminada/empresas/{idEmpresa}")
    @ResponseStatus(HttpStatus.OK)
    public FormaDePago getFormaDePagoPredeterminada(@PathVariable long idEmpresa) {
        return formaDePagoService.getFormaDePagoPredeterminada(empresaService.getEmpresaPorId(idEmpresa));
    }
    
    @GetMapping("/formas-de-pago/empresas/{idEmpresa}")
    @ResponseStatus(HttpStatus.OK)
    public List<FormaDePago> getFormasDePago(@PathVariable long idEmpresa) {
        return formaDePagoService.getFormasDePago(empresaService.getEmpresaPorId(idEmpresa));
    }
    
    @PostMapping("/formas-de-pago")
    @ResponseStatus(HttpStatus.CREATED)
    public FormaDePago guardar(@RequestBody FormaDePago formaDePago) {
        return formaDePagoService.guardar(formaDePago);
    }
    
    @PutMapping("/formas-de-pago/predeterminada/{idFormaDePago}")
    @ResponseStatus(HttpStatus.OK)
    public void setFormaDePagoPredeterminada(@PathVariable long idFormaDePago) {
        formaDePagoService.setFormaDePagoPredeterminada(formaDePagoService.getFormasDePagoPorId(idFormaDePago));
    }    
}

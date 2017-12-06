package sic.controller;

import java.util.List;
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
import sic.modelo.BusquedaClienteCriteria;
import sic.modelo.Cliente;
import sic.modelo.Localidad;
import sic.modelo.Pais;
import sic.modelo.Provincia;
import sic.service.IClienteService;
import sic.service.IEmpresaService;
import sic.service.ILocalidadService;
import sic.service.IPaisService;
import sic.service.IProvinciaService;

@RestController
@RequestMapping("/api/v1")
public class ClienteController {
    
    private final IClienteService clienteService;
    private final IEmpresaService empresaService;
    private final IPaisService paisService;
    private final IProvinciaService provinciaService;
    private final ILocalidadService localidadService;
    private final int TAMANIO_PAGINA_DEFAULT = 50;
    
    @Autowired
    public ClienteController(IClienteService clienteService, IEmpresaService empresaService,
            IPaisService paisService, IProvinciaService provinciaService,
            ILocalidadService localidadService) {
        this.clienteService = clienteService;
        this.empresaService = empresaService;
        this.paisService = paisService;
        this.provinciaService = provinciaService;
        this.localidadService = localidadService;
    }
  
    @GetMapping("/clientes/{idCliente}")
    @ResponseStatus(HttpStatus.OK)
    public Cliente getCliente(@PathVariable long idCliente) {
        return clienteService.getClientePorId(idCliente);
    }
    
    @GetMapping("/clientes/busqueda/criteria")
    @ResponseStatus(HttpStatus.OK)
    public Page<Cliente> buscarConCriteria(@RequestParam Long idEmpresa,
                                           @RequestParam(required = false) String razonSocial,
                                           @RequestParam(required = false) String nombreFantasia,
                                           @RequestParam(required = false) String idFiscal,
                                           @RequestParam(required = false) Long idPais,
                                           @RequestParam(required = false) Long idProvincia, 
                                           @RequestParam(required = false) Long idLocalidad,
                                           @RequestParam(required = false) Integer pagina,
                                           @RequestParam(required = false) Integer tamanio) {
        Pais pais = null;
        if (idPais != null) {
            pais = paisService.getPaisPorId(idPais);
        }
        Provincia provincia = null;
        if (idProvincia != null) {
            provincia = provinciaService.getProvinciaPorId(idProvincia);
        }
        Localidad localidad = null;
        if (idLocalidad != null) {
            localidad = localidadService.getLocalidadPorId(idLocalidad);
        }
        if (tamanio == null || tamanio <= 0) {
            tamanio = TAMANIO_PAGINA_DEFAULT;
        }
        if (pagina == null || pagina < 0) {
            pagina = 0;
        }
        Pageable pageable = new PageRequest(pagina, tamanio, new Sort(Sort.Direction.ASC, "razonSocial"));
        BusquedaClienteCriteria criteria = BusquedaClienteCriteria.builder()
                .buscaPorRazonSocial(razonSocial != null)
                .razonSocial(razonSocial)
                .buscaPorNombreFantasia(nombreFantasia != null)
                .nombreFantasia(nombreFantasia)
                .buscaPorId_Fiscal(idFiscal != null)
                .idFiscal(idFiscal)
                .buscaPorPais(idPais != null)
                .pais(pais)
                .buscaPorProvincia(idProvincia != null)
                .provincia(provincia)
                .buscaPorLocalidad(idLocalidad != null)
                .localidad(localidad)
                .empresa(empresaService.getEmpresaPorId(idEmpresa))
                .pageable(pageable)
                .build();
        return clienteService.buscarClientes(criteria);
    }
       
    @GetMapping("/clientes/empresas/{idEmpresa}")
    @ResponseStatus(HttpStatus.OK)
    public List<Cliente> getClientes(@PathVariable long idEmpresa) {
        return clienteService.getClientes(empresaService.getEmpresaPorId(idEmpresa));
    }
    
    @GetMapping("/clientes/predeterminado/empresas/{idEmpresa}")
    @ResponseStatus(HttpStatus.OK)
    public Cliente getClientePredeterminado(@PathVariable long idEmpresa) {
     return clienteService.getClientePredeterminado(empresaService.getEmpresaPorId(idEmpresa));
    }
    
    @GetMapping("/clientes/existe-predeterminado/empresas/{idEmpresa}")
    @ResponseStatus(HttpStatus.OK)
    public boolean existeClientePredeterminado(@PathVariable long idEmpresa) {
        return clienteService.existeClientePredeterminado(empresaService.getEmpresaPorId(idEmpresa));
    }

    @DeleteMapping("/clientes/{idCliente}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable long idCliente) {
        clienteService.eliminar(idCliente);
    }
    
    @PostMapping("/clientes")
    @ResponseStatus(HttpStatus.CREATED)
    public Cliente guardar(@RequestBody Cliente cliente) {
        return clienteService.guardar(cliente);
    }
    
    @PutMapping("/clientes")
    @ResponseStatus(HttpStatus.OK)
    public void actualizar(@RequestBody Cliente cliente) {
       clienteService.actualizar(cliente);       
    }
    
    @PutMapping("/clientes/{idCliente}/predeterminado")
    @ResponseStatus(HttpStatus.OK)
    public void setClientePredeterminado(@PathVariable long idCliente) {
       clienteService.setClientePredeterminado(clienteService.getClientePorId(idCliente));       
    }
    
    @GetMapping("/clientes/pedidos/{idPedido}")
    @ResponseStatus(HttpStatus.OK)
    public Cliente getClientePorIdPedido(@PathVariable long idPedido) {
       return clienteService.getClientePorIdPedido(idPedido);
    }
}

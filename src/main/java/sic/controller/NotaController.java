package sic.controller;

import java.util.Calendar;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import sic.modelo.BusquedaNotaCriteria;
import sic.modelo.FacturaVenta;
import sic.modelo.Nota;
import sic.modelo.NotaCredito;
import sic.modelo.NotaDebito;
import sic.modelo.RenglonNotaCredito;
import sic.modelo.RenglonNotaDebito;
import sic.modelo.TipoDeComprobante;
import sic.service.IClienteService;
import sic.service.IEmpresaService;
import sic.service.INotaService;

@RestController
@RequestMapping("/api/v1")
public class NotaController {
    
    private final INotaService notaService;
    private final IClienteService clienteService;
    private final IEmpresaService empresaService;
    private final int TAMANIO_PAGINA_DEFAULT = 100;
    
    @Autowired
    public NotaController(INotaService notaService, IClienteService clienteService,
            IEmpresaService empresaService) {
        this.notaService = notaService;
        this.clienteService = clienteService;
        this.empresaService = empresaService;
    }
    
    @GetMapping("/notas/{idNota}")
    @ResponseStatus(HttpStatus.OK)
    public Nota getNota(@PathVariable long idNota) {
        return notaService.getNotaPorId(idNota);
    }
 
    @GetMapping("/notas/{idNota}/facturas")
    @ResponseStatus(HttpStatus.OK)
    public FacturaVenta getFacturaNota(@PathVariable long idNota) {
        return notaService.getFacturaNota(idNota);
    }
    
    @GetMapping("/notas/cliente/{idCliente}/empresa/{idEmpresa}")
    @ResponseStatus(HttpStatus.OK)
    public List<Nota> getNotasPorClienteYEmpresa(Long idEmpresa, Long idCliente) {
        return notaService.getNotasPorClienteYEmpresa(idEmpresa, idCliente);
    }
    
    @GetMapping("/notas/busqueda/criteria") 
    @ResponseStatus(HttpStatus.OK)
    public Page<Nota> buscarNotasPorClienteYEmpresa(@RequestParam(value = "desde", required = false) Long desde,
                                                    @RequestParam(value = "hasta", required = false) Long hasta,
                                                    @RequestParam Long idCliente, 
                                                    @RequestParam Long idEmpresa,
                                                    @RequestParam(required = false) Integer pagina,
                                                    @RequestParam(required = false) Integer tamanio) {
        Calendar fechaDesde = Calendar.getInstance();
        Calendar fechaHasta = Calendar.getInstance();
        if (desde != null && hasta != null) {
            fechaDesde.setTimeInMillis(desde);
            fechaHasta.setTimeInMillis(hasta);
        }
        if (tamanio == null || tamanio <= 0) {
            tamanio = TAMANIO_PAGINA_DEFAULT;
        }
        if (pagina == null || pagina < 0) {
            pagina = 0;
        }
        Pageable pageable = new PageRequest(pagina, tamanio, new Sort(Sort.Direction.ASC, "fecha"));
        BusquedaNotaCriteria criteria = BusquedaNotaCriteria.builder()
                .buscaPorFecha((desde != null) && (hasta != null))
                .fechaDesde(fechaDesde.getTime())
                .fechaHasta(fechaHasta.getTime())
                .empresa(empresaService.getEmpresaPorId(idEmpresa))
                .cantidadDeRegistros(0)
                .cliente(clienteService.getClientePorId(idCliente))
                .pageable(pageable)
                .build();
        return notaService.buscarNotasPorClienteYEmpresa(criteria);
    }
    
    @GetMapping("/notas/saldo")
    @ResponseStatus(HttpStatus.OK)
    public double getSaldoNotas(@RequestParam Long hasta,
                                @RequestParam long idCliente, 
                                @RequestParam long IdEmpresa) {
        Calendar fechaHasta = Calendar.getInstance();
        fechaHasta.setTimeInMillis(hasta);
        return notaService.getSaldoNotas(fechaHasta.getTime(), idCliente, IdEmpresa);
    }
    
    @GetMapping("/notas/tipos")
    @ResponseStatus(HttpStatus.OK)
    public TipoDeComprobante[] getTipoNota(@RequestParam long idCliente,
                                           @RequestParam long idEmpresa) {
        return notaService.getTipoNota(idCliente, idEmpresa);
    }
    
    @GetMapping("/notas/renglones/credito/{idNotaCredito}")
    @ResponseStatus(HttpStatus.OK)
    public List<RenglonNotaCredito> getRenglonesDeNotaCredito(@RequestParam long idNotaCredito) {
        return notaService.getRenglonesDeNotaCredito(idNotaCredito);
    }
    
    @GetMapping("/notas/renglones/debito/{idNotaDebito}")
    @ResponseStatus(HttpStatus.OK)
    public List<RenglonNotaDebito> getRenglonesDeNotaDebito(@RequestParam long idNotaDebito) {
        return notaService.getRenglonesDeNotaDebito(idNotaDebito);
    }
    
    @PostMapping("/notas/credito/empresa/{idEmpresa}/cliente/{idCliente}/usuario/{idUsuario}/factura/{idFactura}")
    @ResponseStatus(HttpStatus.CREATED)
    public Nota guardarNotaCredito(@RequestBody NotaCredito nota,
                                   @PathVariable long idEmpresa,
                                   @PathVariable long idCliente,
                                   @PathVariable long idUsuario,
                                   @PathVariable long idFactura, 
                                   @RequestParam boolean modificarStock) {
        return notaService.guardarNota(nota, idEmpresa, idCliente, idUsuario, null, idFactura, modificarStock);
    }
    
    @PostMapping("/notas/debito/empresa/{idEmpresa}/cliente/{idCliente}/usuario/{idUsuario}/recibo/{idRecibo}")
    @ResponseStatus(HttpStatus.CREATED)
    public Nota guardarNotaDebito(@RequestBody NotaDebito nota,
                                  @PathVariable long idEmpresa,
                                  @PathVariable long idCliente,
                                  @PathVariable long idUsuario,
                                  @PathVariable long idRecibo) {
        return notaService.guardarNota(nota, idEmpresa, idCliente, idUsuario, idRecibo, null, false);
    }

    @GetMapping("/notas/{idNota}/reporte")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<byte[]> getReporteNota(@PathVariable long idNota) {        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);   
        Nota nota = notaService.getNotaPorId(idNota);
        String fileName = (nota instanceof NotaCredito) ? "NotaCredito.pdf" : (nota instanceof NotaDebito) ? "NotaDebito.pdf" : "Nota.pdf";
        headers.add("content-disposition", "inline; filename=" + fileName);
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
        byte[] reportePDF = notaService.getReporteNota(nota);
        return new ResponseEntity<>(reportePDF, headers, HttpStatus.OK);
    }
    
    @PostMapping("/notas/{idNota}/autorizacion")
    @ResponseStatus(HttpStatus.CREATED)
    public Nota autorizarNota(@PathVariable long idNota) {
        return notaService.autorizarNota(notaService.getNotaPorId(idNota));
    }
    
    @DeleteMapping("/notas")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarNota(@RequestParam long[] idsNota) {
        notaService.eliminarNota(idsNota);
    }
    
    @GetMapping("/notas/{idNota}/iva-neto")
    @ResponseStatus(HttpStatus.OK)
    public double getIvaNetoNota(@PathVariable long idNota) {
        return notaService.getIvaNetoNota(idNota);
    }
    
    @GetMapping("/notas/renglon/credito/producto") 
    @ResponseStatus(HttpStatus.OK) 
    public List<RenglonNotaCredito> calcularRenglonNotaCreditoProducto(@RequestParam TipoDeComprobante tipoDeComprobante,
                                                                       @RequestParam double[] cantidad,
                                                                       @RequestParam long[] idRenglonFactura) {
        return notaService.calcularRenglonCredito(tipoDeComprobante, cantidad, idRenglonFactura);
    }
    
    @GetMapping("/notas/renglon/debito/recibo/{idRecibo}")
    @ResponseStatus(HttpStatus.OK) 
    public List<RenglonNotaDebito> calcularRenglonNotaDebito(@PathVariable long idRecibo, 
                                                             @RequestParam double monto,
                                                             @RequestParam double ivaPorcentaje) {
        return notaService.calcularRenglonDebito(idRecibo, monto, ivaPorcentaje);
    }
    
    @GetMapping("/notas/credito/sub-total")
    @ResponseStatus(HttpStatus.OK)
    public double calcularSubTotalCredito(@RequestParam double[] importe) {
        return notaService.calcularSubTotalCredito(importe);
    }
    
    @GetMapping("/notas/credito/descuento-neto")
    @ResponseStatus(HttpStatus.OK)
    public double calcularDescuentoNetoCredito(@RequestParam double subTotal,
                                         @RequestParam double descuentoPorcentaje) {
        return notaService.calcularDecuentoNetoCredito(subTotal, descuentoPorcentaje);
    }
    
    @GetMapping("/notas/credito/recargo-neto")
    @ResponseStatus(HttpStatus.OK)
    public double calcularRecargoNetoCredito(@RequestParam double subTotal,
                                      @RequestParam double recargoPorcentaje) {
        return notaService.calcularRecargoNetoCredito(subTotal, recargoPorcentaje);
    }
    
    @GetMapping("/notas/credito/iva-neto")
    @ResponseStatus(HttpStatus.OK)
    public double calcularIVANetoCredito(@RequestParam TipoDeComprobante tipoDeComprobante,
                                  @RequestParam double[] cantidades,
                                  @RequestParam double[] ivaPorcentajeRenglones,
                                  @RequestParam double[] ivaNetoRenglones,
                                  @RequestParam double ivaPorcentaje,
                                  @RequestParam double descuentoPorcentaje, 
                                  @RequestParam double recargoPorcentaje){
        return notaService.calcularIVANetoCredito(tipoDeComprobante, cantidades, ivaPorcentajeRenglones, ivaNetoRenglones, ivaPorcentaje, descuentoPorcentaje, recargoPorcentaje);
    }  
    
    @GetMapping("/notas/credito/sub-total-bruto")
    @ResponseStatus(HttpStatus.OK)
    public double calcularSubTotalBrutoCredito(TipoDeComprobante tipoDeComprobante, 
                                        double subTotal, 
                                        double recargoNeto, 
                                        double descuentoNeto,
                                        double iva105Neto,
                                        double iva21Neto) {
        return notaService.calcularSubTotalBrutoCredito(tipoDeComprobante, subTotal, recargoNeto, descuentoNeto, iva105Neto, iva21Neto);
    }
    
    @GetMapping("/notas/credito/total")
    @ResponseStatus(HttpStatus.OK)
    public double calcularTotalCredito(@RequestParam double subTotalBruto,                                
                                @RequestParam double iva105Neto,
                                @RequestParam double iva21Neto) {
        return notaService.calcularTotalCredito(subTotalBruto, iva105Neto, iva21Neto);
    }
    
    @GetMapping("/notas/debito/total")
    @ResponseStatus(HttpStatus.OK)
    public double calcularTotalDebito(double subTotalBruto,                                
                                      double iva21Neto,
                                      double montoNoGravado) {
        return notaService.calcularTotalDebito(subTotalBruto, iva21Neto, montoNoGravado);
    }
    
}

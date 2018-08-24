package sic.controller;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.List;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sic.aspect.AccesoRolesPermitidos;
import sic.modelo.*;
import sic.modelo.dto.NuevoRenglonPedidoDTO;
import sic.modelo.dto.PedidoDTO;
import sic.service.*;

@RestController
@RequestMapping("/api/v1")
public class PedidoController {
    
    private final IPedidoService pedidoService;
    private final IEmpresaService empresaService;
    private final IUsuarioService usuarioService;
    private final IClienteService clienteService;
    private final IProductoService productoService;
    private final ModelMapper modelMapper;

    @Value("${SIC_JWT_KEY}")
    private String secretkey;

    @Autowired
    public PedidoController(IPedidoService pedidoService, IEmpresaService empresaService,
                            IUsuarioService usuarioService, IClienteService clienteService,
                            IProductoService productoService, ModelMapper modelMapper) {
        this.pedidoService = pedidoService;
        this.empresaService = empresaService;
        this.usuarioService = usuarioService;
        this.clienteService = clienteService;
        this.productoService = productoService;
        this.modelMapper = modelMapper;
    }
    
    @GetMapping("/pedidos/{idPedido}")
    @ResponseStatus(HttpStatus.OK)
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR, Rol.VIAJANTE, Rol.COMPRADOR})
    public Pedido getPedidoPorId(@PathVariable long idPedido) {
        return pedidoService.getPedidoPorId(idPedido);
    }
    
    @GetMapping("/pedidos/{idPedido}/renglones")
    @ResponseStatus(HttpStatus.OK)
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR, Rol.VIAJANTE, Rol.COMPRADOR})
    public List<RenglonPedido> getRenglonesDelPedido(@PathVariable long idPedido) {
        return pedidoService.getRenglonesDelPedido(idPedido);
    }


    @PostMapping("/pedidos/renglones")
    @ResponseStatus(HttpStatus.OK)
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR, Rol.VIAJANTE})
    public List<RenglonPedido> calcularRenglonesPedido(@RequestBody List<NuevoRenglonPedidoDTO> nuevosRenglonesPedidoDTO) {
        return pedidoService.calcularRenglonesPedido(nuevosRenglonesPedidoDTO);
    }

    @PutMapping("/pedidos")
    @ResponseStatus(HttpStatus.OK)
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR, Rol.VIAJANTE, Rol.COMPRADOR})
    public void actualizar(@RequestParam Long idEmpresa,
                           @RequestParam Long idUsuario,
                           @RequestParam Long idCliente,
                           @RequestBody PedidoDTO pedidoDTO) {
        Pedido pedido = modelMapper.map(pedidoDTO, Pedido.class);
        pedido.setEmpresa(empresaService.getEmpresaPorId(idEmpresa));
        pedido.setUsuario(usuarioService.getUsuarioPorId(idUsuario));
        pedido.setCliente(clienteService.getClientePorId(idCliente));
        //Las facturas se recuperan para evitar cambios no deseados. 
        pedido.setFacturas(pedidoService.getFacturasDelPedido(pedido.getId_Pedido()));
        //Si los renglones vienen null, recupera los renglones del pedido para actualizar
        //caso contrario, ultiliza los renglones del pedido.
        if (pedido.getRenglones() == null) {
            pedido.setRenglones(pedidoService.getRenglonesDelPedido(pedido.getId_Pedido()));
        }
        pedidoService.actualizar(pedido);        
    }

  @PostMapping("/pedidos")
  @ResponseStatus(HttpStatus.CREATED)
  @AccesoRolesPermitidos({
    Rol.ADMINISTRADOR,
    Rol.ENCARGADO,
    Rol.VENDEDOR,
    Rol.VIAJANTE,
    Rol.COMPRADOR
  })
  public Pedido guardar(
      @RequestParam Long idEmpresa,
      @RequestParam Long idUsuario,
      @RequestParam Long idCliente,
      @RequestBody PedidoDTO pedidoDTO) {
    Pedido pedido = modelMapper.map(pedidoDTO, Pedido.class);
    pedido.setEmpresa(empresaService.getEmpresaPorId(idEmpresa));
    pedido.setUsuario(usuarioService.getUsuarioPorId(idUsuario));
    pedido.setCliente(clienteService.getClientePorId(idCliente));
    pedido
        .getRenglones()
        .forEach(
            renglonPedido ->
                renglonPedido.setProducto(
                    productoService.getProductoPorId(
                        renglonPedido.getProducto().getId_Producto())));
    return pedidoService.guardar(pedido);
  }

    @GetMapping("/pedidos/busqueda/criteria")
    @ResponseStatus(HttpStatus.OK)
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR, Rol.VIAJANTE, Rol.COMPRADOR})
    public Page<Pedido> buscarConCriteria(@RequestParam Long idEmpresa,
                                          @RequestParam(required = false) Long desde,
                                          @RequestParam(required = false) Long hasta,
                                          @RequestParam(required = false) Long idCliente,
                                          @RequestParam(required = false) Long idUsuario,
                                          @RequestParam(required = false) Long nroPedido,
                                          @RequestParam(required = false) EstadoPedido estadoPedido,
                                          @RequestParam(required = false) Integer pagina,
                                          @RequestParam(required = false) Integer tamanio,
                                          @RequestHeader("Authorization") String token) {
        final int TAMANIO_PAGINA_DEFAULT = 50;
        Calendar fechaDesde = Calendar.getInstance();
        Calendar fechaHasta = Calendar.getInstance();
        if ((desde != null) && (hasta != null)) {
            fechaDesde.setTimeInMillis(desde);            
            fechaHasta.setTimeInMillis(hasta);
        }
        Cliente cliente = null;
        if (idCliente != null) cliente = clienteService.getClientePorId(idCliente);
        if (tamanio == null || tamanio <= 0) tamanio = TAMANIO_PAGINA_DEFAULT;
        if (pagina == null || pagina < 0) pagina = 0;
        Pageable pageable = new PageRequest(pagina, tamanio, new Sort(Sort.Direction.DESC, "fecha"));
        BusquedaPedidoCriteria criteria = BusquedaPedidoCriteria.builder()
                                                                .buscaPorFecha((desde != null) && (hasta != null))
                                                                .fechaDesde(fechaDesde.getTime())
                                                                .fechaHasta(fechaHasta.getTime())
                                                                .buscaCliente(cliente != null)
                                                                .idCliente(idCliente)
                                                                .buscaUsuario(idUsuario != null)
                                                                .idUsuario(idUsuario)
                                                                .buscaPorNroPedido(nroPedido != null)
                                                                .nroPedido((nroPedido != null) ? nroPedido : 0)
                                                                .buscaPorEstadoPedido(estadoPedido != null)
                                                                .estadoPedido(estadoPedido)
                                                                .idEmpresa(idEmpresa)
                                                                .pageable(pageable)
                                                                .build();
        Claims claims =
                Jwts.parser().setSigningKey(secretkey).parseClaimsJws(token.substring(7)).getBody();
        return pedidoService.buscarConCriteria(criteria, (int) claims.get("idUsuario"));
    }
    
    @DeleteMapping("/pedidos/{idPedido}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
    public void eliminar(@PathVariable long idPedido) {
        pedidoService.eliminar(idPedido);
    }       
        
    @GetMapping("/pedidos/{idPedido}/reporte")
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR, Rol.VIAJANTE, Rol.COMPRADOR})
    public ResponseEntity<byte[]> getReportePedido(@PathVariable long idPedido) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);        
        headers.add("content-disposition", "inline; filename=Pedido.pdf");
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
        byte[] reportePDF = pedidoService.getReportePedido(pedidoService.getPedidoPorId(idPedido));
        return new ResponseEntity<>(reportePDF, headers, HttpStatus.OK);
    }
    
}

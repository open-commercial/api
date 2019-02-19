package sic.controller;

import java.lang.reflect.Type;
import java.util.Calendar;
import java.util.List;
import java.util.ResourceBundle;

import io.jsonwebtoken.Claims;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
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
import sic.modelo.dto.NuevoPedidoDTO;
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
    private final IAuthService authService;
    private final ModelMapper modelMapper;
    private static final int TAMANIO_PAGINA_DEFAULT = 25;

    @Autowired
    public PedidoController(IPedidoService pedidoService, IEmpresaService empresaService,
                            IUsuarioService usuarioService, IClienteService clienteService,
                            IAuthService authService, ModelMapper modelMapper) {
        this.pedidoService = pedidoService;
        this.empresaService = empresaService;
        this.usuarioService = usuarioService;
        this.clienteService = clienteService;
        this.authService = authService;
        this.modelMapper = modelMapper;
    }
    
    @GetMapping("/pedidos/{idPedido}")
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR, Rol.VIAJANTE, Rol.COMPRADOR})
    public Pedido getPedidoPorId(@PathVariable long idPedido) {
        return pedidoService.getPedidoPorId(idPedido);
    }
    
    @GetMapping("/pedidos/{idPedido}/renglones")
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR, Rol.VIAJANTE, Rol.COMPRADOR})
    public List<RenglonPedido> getRenglonesDelPedido(@PathVariable long idPedido) {
        return pedidoService.getRenglonesDelPedido(idPedido);
    }


    @PostMapping("/pedidos/renglones")
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR, Rol.VIAJANTE})
    public List<RenglonPedido> calcularRenglonesPedido(@RequestBody List<NuevoRenglonPedidoDTO> nuevosRenglonesPedidoDTO) {
        return pedidoService.calcularRenglonesPedido(nuevosRenglonesPedidoDTO);
    }

    @PutMapping("/pedidos")
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
      @RequestParam boolean usarUbicacionDeFacturacion,
      @RequestBody NuevoPedidoDTO nuevoPedidoDTO) {
    Pedido pedido = new Pedido();
    pedido.setFechaVencimiento(nuevoPedidoDTO.getFechaVencimiento());
    pedido.setObservaciones(nuevoPedidoDTO.getObservaciones());
    Type listType = new TypeToken<List<RenglonPedido>>() {}.getType();
    pedido.setRenglones(modelMapper.map(nuevoPedidoDTO.getRenglones(), listType));
    pedido.setSubTotal(nuevoPedidoDTO.getSubTotal());
    pedido.setRecargoPorcentaje(nuevoPedidoDTO.getRecargoPorcentaje());
    pedido.setRecargoNeto(nuevoPedidoDTO.getRecargoNeto());
    pedido.setDescuentoPorcentaje(nuevoPedidoDTO.getDescuentoPorcentaje());
    pedido.setDescuentoNeto(nuevoPedidoDTO.getDescuentoNeto());
    pedido.setTotalEstimado(nuevoPedidoDTO.getTotal());
    pedido.setTotalActual(nuevoPedidoDTO.getTotal());
    pedido.setEmpresa(empresaService.getEmpresaPorId(idEmpresa));
    pedido.setUsuario(usuarioService.getUsuarioPorId(idUsuario));
    Cliente cliente = clienteService.getClientePorId(idCliente);
    if (cliente.getUbicacionFacturacion() == null) {
      throw new BusinessServiceException(
        ResourceBundle.getBundle("Mensajes").getString("mensaje_pedido_cliente_sin_ubicacion"));
    }
    DetalleEnvio detalleEnvio = new DetalleEnvio();
    if (usarUbicacionDeFacturacion) {
      detalleEnvio.setNombreLocalidad(cliente.getUbicacionFacturacion().getLocalidad().getNombre());
      detalleEnvio.setNombreProvincia(cliente.getUbicacionFacturacion().getLocalidad().getProvincia().getNombre());
      detalleEnvio.setDescripcion(cliente.getUbicacionFacturacion().getDescripcion());
      detalleEnvio.setLatitud(cliente.getUbicacionFacturacion().getLatitud());
      detalleEnvio.setLongitud(cliente.getUbicacionFacturacion().getLatitud());
      detalleEnvio.setCalle(cliente.getUbicacionFacturacion().getCalle());
      detalleEnvio.setNumero(cliente.getUbicacionFacturacion().getNumero());
      detalleEnvio.setPiso(cliente.getUbicacionFacturacion().getPiso());
      detalleEnvio.setDepartamento(cliente.getUbicacionFacturacion().getDepartamento());
      if (cliente.getUbicacionFacturacion().getLocalidad() != null) {
        detalleEnvio.setCodigoPostal(cliente.getUbicacionFacturacion().getLocalidad().getCodigoPostal());
        detalleEnvio.setDetalleUbicacion(cliente.getUbicacionFacturacion().getDetalleUbicacion());
      }
      pedido.setDetalleEnvio(detalleEnvio);
    }
    if (nuevoPedidoDTO.getDetalleEnvio() == null && cliente.getUbicacionEnvio() != null) {
      detalleEnvio.setNombreLocalidad(cliente.getUbicacionFacturacion().getLocalidad().getNombre());
      detalleEnvio.setNombreProvincia(cliente.getUbicacionFacturacion().getLocalidad().getProvincia().getNombre());
      detalleEnvio.setDescripcion(cliente.getUbicacionEnvio().getDescripcion());
      detalleEnvio.setLatitud(cliente.getUbicacionEnvio().getLatitud());
      detalleEnvio.setLongitud(cliente.getUbicacionEnvio().getLatitud());
      detalleEnvio.setCalle(cliente.getUbicacionEnvio().getCalle());
      detalleEnvio.setNumero(cliente.getUbicacionEnvio().getNumero());
      detalleEnvio.setPiso(cliente.getUbicacionEnvio().getPiso());
      detalleEnvio.setDepartamento(cliente.getUbicacionEnvio().getDepartamento());
      if (cliente.getUbicacionEnvio().getLocalidad() != null) {
        detalleEnvio.setCodigoPostal(cliente.getUbicacionEnvio().getLocalidad().getCodigoPostal());
        detalleEnvio.setDetalleUbicacion(cliente.getUbicacionEnvio().getDetalleUbicacion());
      }
      pedido.setDetalleEnvio(detalleEnvio);
    }
    pedido.setCliente(cliente);
    return pedidoService.guardar(pedido);
  }

  @GetMapping("/pedidos/busqueda/criteria")
  @AccesoRolesPermitidos({
    Rol.ADMINISTRADOR,
    Rol.ENCARGADO,
    Rol.VENDEDOR,
    Rol.VIAJANTE,
    Rol.COMPRADOR
  })
  public Page<Pedido> buscarConCriteria(
      @RequestParam Long idEmpresa,
      @RequestParam(required = false) Long desde,
      @RequestParam(required = false) Long hasta,
      @RequestParam(required = false) Long idCliente,
      @RequestParam(required = false) Long idUsuario,
      @RequestParam(required = false) Long nroPedido,
      @RequestParam(required = false) EstadoPedido estadoPedido,
      @RequestParam(required = false) Integer pagina,
      @RequestHeader("Authorization") String authorizationHeader) {
    Calendar fechaDesde = Calendar.getInstance();
    Calendar fechaHasta = Calendar.getInstance();
    if ((desde != null) && (hasta != null)) {
      fechaDesde.setTimeInMillis(desde);
      fechaHasta.setTimeInMillis(hasta);
    }
    Cliente cliente = null;
    if (idCliente != null) cliente = clienteService.getClientePorId(idCliente);
    if (pagina == null || pagina < 0) pagina = 0;
    Pageable pageable = new PageRequest(pagina, TAMANIO_PAGINA_DEFAULT, new Sort(Sort.Direction.DESC, "fecha"));
    BusquedaPedidoCriteria criteria =
        BusquedaPedidoCriteria.builder()
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
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    return pedidoService.buscarConCriteria(criteria, (int) claims.get("idUsuario"));
  }

    @DeleteMapping("/pedidos/{idPedido}")
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

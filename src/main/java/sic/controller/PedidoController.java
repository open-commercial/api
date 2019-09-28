package sic.controller;

import java.lang.reflect.Type;
import java.util.List;
import io.jsonwebtoken.Claims;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sic.aspect.AccesoRolesPermitidos;
import sic.modelo.*;
import sic.modelo.criteria.BusquedaPedidoCriteria;
import sic.modelo.calculos.NuevosResultadosPedido;
import sic.modelo.calculos.Resultados;
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
        return pedidoService.getPedidoNoEliminadoPorId(idPedido);
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
                         @RequestParam TipoDeEnvio tipoDeEnvio,
                         @RequestParam(required = false) Long idSucursal,
                         @RequestBody PedidoDTO pedidoDTO) {
    Pedido pedido = modelMapper.map(pedidoDTO, Pedido.class);
    pedido.setEmpresa(empresaService.getEmpresaPorId(idEmpresa));
    pedido.setUsuario(usuarioService.getUsuarioNoEliminadoPorId(idUsuario));
    pedido.setCliente(clienteService.getClienteNoEliminadoPorId(idCliente));
    pedido.setDetalleEnvio(pedidoService.getPedidoNoEliminadoPorId(pedidoDTO.getId_Pedido()).getDetalleEnvio());
    //Las facturas se recuperan para evitar cambios no deseados.
    pedido.setFacturas(pedidoService.getFacturasDelPedido(pedido.getId_Pedido()));
    //Si los renglones vienen null, recupera los renglones del pedido para actualizarLocalidad
    //caso contrario, ultiliza los renglones del pedido.
    pedidoService.actualizar(pedido, tipoDeEnvio, idSucursal);
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
    @RequestBody NuevoPedidoDTO nuevoPedidoDTO) {
    Pedido pedido = new Pedido();
    pedido.setFechaVencimiento(nuevoPedidoDTO.getFechaVencimiento());
    pedido.setObservaciones(nuevoPedidoDTO.getObservaciones());
    Type listType = new TypeToken<List<RenglonPedido>>() {
    }.getType();
    pedido.setRenglones(modelMapper.map(nuevoPedidoDTO.getRenglones(), listType));
    pedido.setSubTotal(nuevoPedidoDTO.getSubTotal());
    pedido.setRecargoPorcentaje(nuevoPedidoDTO.getRecargoPorcentaje());
    pedido.setRecargoNeto(nuevoPedidoDTO.getRecargoNeto());
    pedido.setDescuentoPorcentaje(nuevoPedidoDTO.getDescuentoPorcentaje());
    pedido.setDescuentoNeto(nuevoPedidoDTO.getDescuentoNeto());
    pedido.setTotalEstimado(nuevoPedidoDTO.getTotal());
    pedido.setTotalActual(nuevoPedidoDTO.getTotal());
    Empresa empresaParaPedido = empresaService.getEmpresaPorId(nuevoPedidoDTO.getIdEmpresa());
    pedido.setEmpresa(empresaParaPedido);
    pedido.setUsuario(usuarioService.getUsuarioNoEliminadoPorId(nuevoPedidoDTO.getIdUsuario()));
    Cliente cliente = clienteService.getClienteNoEliminadoPorId(nuevoPedidoDTO.getIdCliente());
    pedido.setCliente(cliente);
    return pedidoService.guardar(pedido, nuevoPedidoDTO.getTipoDeEnvio(), nuevoPedidoDTO.getIdSucursal());
  }

  @PostMapping("/pedidos/busqueda/criteria")
  @AccesoRolesPermitidos({
    Rol.ADMINISTRADOR,
    Rol.ENCARGADO,
    Rol.VENDEDOR,
    Rol.VIAJANTE,
    Rol.COMPRADOR
  })
  public Page<Pedido> buscarConCriteria(
      @RequestBody BusquedaPedidoCriteria criteria,
      @RequestHeader("Authorization") String authorizationHeader) {
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    return pedidoService.buscarPedidos(criteria, (int) claims.get("idUsuario"));
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
        byte[] reportePDF = pedidoService.getReportePedido(pedidoService.getPedidoNoEliminadoPorId(idPedido));
        return new ResponseEntity<>(reportePDF, headers, HttpStatus.OK);
    }

  @PostMapping("/pedidos/calculo-pedido")
  @AccesoRolesPermitidos({
    Rol.ADMINISTRADOR,
    Rol.ENCARGADO,
    Rol.VENDEDOR,
    Rol.VIAJANTE,
    Rol.COMPRADOR
  })
  public Resultados calcularResultadosPedido(@RequestBody NuevosResultadosPedido nuevosResultadosPedido) {
    return pedidoService.calcularResultadosPedido(nuevosResultadosPedido);
  }
}

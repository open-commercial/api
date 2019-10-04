package sic.controller;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Locale;
import io.jsonwebtoken.Claims;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sic.aspect.AccesoRolesPermitidos;
import sic.exception.BusinessServiceException;
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
    private final ISucursalService sucursalService;
    private final IUsuarioService usuarioService;
    private final IClienteService clienteService;
    private final IConfiguracionSucursalService configuracionSucursal;
    private final IAuthService authService;
    private final ModelMapper modelMapper;
    private final MessageSource messageSource;
    private static final int TAMANIO_PAGINA_DEFAULT = 25;

    @Autowired
    public PedidoController(IPedidoService pedidoService, ISucursalService sucursalService,
                            IUsuarioService usuarioService, IClienteService clienteService,
                            IConfiguracionSucursalService configuracionSucursal,
                            IAuthService authService, ModelMapper modelMapper, MessageSource messageSource) {
        this.pedidoService = pedidoService;
        this.sucursalService = sucursalService;
        this.usuarioService = usuarioService;
        this.clienteService = clienteService;
        this.configuracionSucursal = configuracionSucursal;
        this.authService = authService;
        this.modelMapper = modelMapper;
        this.messageSource = messageSource;
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
  public void actualizar(@RequestParam Long idSucursal,
                         @RequestParam Long idUsuario,
                         @RequestParam Long idCliente,
                         @RequestParam TipoDeEnvio tipoDeEnvio,
                         @RequestParam(required = false) Long idSucursalEnvio,
                         @RequestBody PedidoDTO pedidoDTO) {
    Pedido pedido = modelMapper.map(pedidoDTO, Pedido.class);
    pedido.setSucursal(sucursalService.getSucursalPorId(idSucursal));
    pedido.setUsuario(usuarioService.getUsuarioNoEliminadoPorId(idUsuario));
    pedido.setCliente(clienteService.getClienteNoEliminadoPorId(idCliente));
    pedido.setDetalleEnvio(pedidoService.getPedidoNoEliminadoPorId(pedidoDTO.getId_Pedido()).getDetalleEnvio());
    //Las facturas se recuperan para evitar cambios no deseados.
    pedido.setFacturas(pedidoService.getFacturasDelPedido(pedido.getId_Pedido()));
    //Si los renglones vienen null, recupera los renglones del pedido para actualizarLocalidad
    //caso contrario, ultiliza los renglones del pedido.
    pedidoService.actualizar(pedido, tipoDeEnvio, idSucursalEnvio);
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
    if (nuevoPedidoDTO.getTipoDeEnvio().equals(TipoDeEnvio.RETIRO_EN_SUCURSAL)) {
      Sucursal sucursal = sucursalService.getSucursalPorId(nuevoPedidoDTO.getIdSucursalEnvio());
      if (!configuracionSucursal
          .getConfiguracionSucursal(sucursal)
          .isPuntoDeRetiro()) {
        throw new BusinessServiceException(
            messageSource.getMessage(
                "mensaje_pedido_sucursal_entrega_no_valida", null, Locale.getDefault()));
      }
      pedido.setSucursal(sucursal);
    } else {
      pedido.setSucursal(sucursalService.getSucursalPorId(nuevoPedidoDTO.getIdSucursal()));
    }
    pedido.setUsuario(usuarioService.getUsuarioNoEliminadoPorId(nuevoPedidoDTO.getIdUsuario()));
    Cliente cliente = clienteService.getClienteNoEliminadoPorId(nuevoPedidoDTO.getIdCliente());
    pedido.setCliente(cliente);
    return pedidoService.guardar(pedido, nuevoPedidoDTO.getTipoDeEnvio(), nuevoPedidoDTO.getIdSucursalEnvio());
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
        byte[] reportePDF = pedidoService.getReportePedido(idPedido);
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

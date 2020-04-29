package sic.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import sic.exception.BusinessServiceException;
import sic.modelo.EstadoPedido;
import sic.modelo.Pedido;
import sic.repository.PedidoRepository;
import sic.repository.RenglonPedidoRepository;
import sic.util.CustomValidator;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(
    classes = {PedidoServiceImpl.class, CustomValidator.class, MessageSource.class})
class PedidoServiceImplTest {

  @MockBean PedidoRepository pedidoRepository;
  @MockBean RenglonPedidoRepository renglonPedidoRepository;
  @MockBean FacturaVentaServiceImpl facturaVentaService;
  @MockBean UsuarioServiceImpl usuarioService;
  @MockBean ClienteServiceImpl clienteService;
  @MockBean ProductoServiceImpl productoService;
  @MockBean CorreoElectronicoServiceImpl correoElectronicoService;
  @MockBean ConfiguracionSucursalServiceImpl configuracionSucursalService;
  @MockBean CuentaCorrienteServiceImpl cuentaCorrienteService;
  @MockBean MessageSource messageSource;
  @MockBean ModelMapper modelMapper;

  @Autowired PedidoServiceImpl pedidoService;

  @Test
  void shouldEliminarPedidoAbierto() {
    Pedido pedido = new Pedido();
    pedido.setEstado(EstadoPedido.ABIERTO);
    when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
    pedidoService.eliminar(1L);
    verify(pedidoRepository, times(1)).save(pedido);
  }

  @Test
  void shouldEliminarPedidoCerrado() {
    Pedido pedido = new Pedido();
    pedido.setEstado(EstadoPedido.CERRADO);
    when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
    assertThrows(BusinessServiceException.class, () -> pedidoService.eliminar(1L));
    verify(messageSource).getMessage(eq("mensaje_no_se_puede_eliminar_pedido"), any(), any());
  }
}

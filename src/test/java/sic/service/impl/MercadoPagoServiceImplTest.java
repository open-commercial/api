package sic.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import sic.modelo.Cliente;
import sic.modelo.Movimiento;
import sic.modelo.TipoDeEnvio;
import sic.modelo.dto.MercadoPagoPreferenceDTO;
import sic.modelo.dto.NuevaOrdenDePagoDTO;
import sic.service.*;
import sic.util.EncryptUtils;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {MercadoPagoServiceImpl.class})
@TestPropertySource(locations = "classpath:application.properties")
class MercadoPagoServiceImplTest {

  @MockBean IReciboService reciboService;
  @MockBean IFormaDePagoService formaDePagoService;
  @MockBean IClienteService clienteService;
  @MockBean INotaService notaService;
  @MockBean ISucursalService sucursalService;
  @MockBean ICarritoCompraService carritoCompraService;
  @MockBean IUsuarioService usuarioService;
  @MockBean IPedidoService pedidoService;
  @MockBean EncryptUtils encryptUtils;
  @MockBean MessageSource messageSource;
  @Autowired MercadoPagoServiceImpl mercadoPagoService;

  @BeforeEach
  void setup() {
    Cliente cliente = new Cliente();
    cliente.setEmail("test@test.com");
    cliente.setNroCliente("1234");
    cliente.setNombreFiscal("Jhon Test");
    when(clienteService.getClientePorIdUsuario(anyLong())).thenReturn(cliente);
  }

  @Test
  void crearNuevaPreference() {
    NuevaOrdenDePagoDTO nuevaOrdenDePagoDTO =
        NuevaOrdenDePagoDTO.builder()
            .movimiento(Movimiento.DEPOSITO)
            .idSucursal(1L)
            .tipoDeEnvio(TipoDeEnvio.RETIRO_EN_SUCURSAL)
            .monto(BigDecimal.TEN)
            .build();
    MercadoPagoPreferenceDTO mercadoPagoPreferenceDTO =
        mercadoPagoService.crearNuevaPreference(1L, nuevaOrdenDePagoDTO, "localhost");
    assertNotNull(mercadoPagoPreferenceDTO);
    assertNotNull(mercadoPagoPreferenceDTO.getId());
    assertNotEquals("", mercadoPagoPreferenceDTO.getId());
    assertNotNull(mercadoPagoPreferenceDTO.getInitPoint());
    assertNotEquals("", mercadoPagoPreferenceDTO.getInitPoint());
  }

}

package sic.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import sic.exception.ServiceException;
import sic.modelo.*;
import sic.modelo.criteria.BusquedaCuentaCorrienteClienteCriteria;
import sic.modelo.criteria.BusquedaProductoCriteria;
import sic.repository.CuentaCorrienteClienteRepository;
import sic.repository.CuentaCorrienteRepository;
import sic.repository.RenglonCuentaCorrienteRepository;
import sic.service.IClienteService;
import sic.service.ISucursalService;
import sic.service.IUsuarioService;
import sic.util.CustomValidator;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(
    classes = {CuentaCorrienteServiceImpl.class, CustomValidator.class, MessageSource.class})
class CuentaCorrienteServiceImplTest {

  @MockBean CuentaCorrienteRepository<CuentaCorriente> cuentaCorrienteRepository;
  @MockBean CuentaCorrienteClienteRepository cuentaCorrienteClienteRepository;
  @MockBean RenglonCuentaCorrienteRepository renglonCuentaCorrienteRepository;
  @MockBean IUsuarioService usuarioService;
  @MockBean IClienteService clienteService;
  @MockBean ISucursalService sucursalService;
  @MockBean MessageSource messageSource;

  @Autowired CuentaCorrienteServiceImpl cuentaCorrienteService;

  @Test
  void shouldTestReporteListaDeClientes() {
    List<CuentaCorrienteCliente> cuentasCorriente = new ArrayList<>();
    CuentaCorrienteCliente cuentaCorrienteCliente = new CuentaCorrienteCliente();
    cuentasCorriente.add(cuentaCorrienteCliente);
    Usuario usuario = new Usuario();
    List<Rol> rolesDeUsuario = new ArrayList<>();
    rolesDeUsuario.add(Rol.ADMINISTRADOR);
    usuario.setRoles(rolesDeUsuario);
    when(usuarioService.getUsuarioNoEliminadoPorId(1L)).thenReturn(usuario);
    when(cuentaCorrienteClienteRepository.findAll(
            cuentaCorrienteService.getBuilder(
                BusquedaCuentaCorrienteClienteCriteria.builder().build(), 1L),
            cuentaCorrienteService.getPageable(null, null, null, "cliente.nombreFiscal", Integer.MAX_VALUE)))
        .thenReturn(new PageImpl<>(cuentasCorriente));
    Sucursal sucursal = new Sucursal();
    sucursal.setLogo("noTieneImagen");
    when(sucursalService.getSucursalPredeterminada()).thenReturn(sucursal);
    assertThrows(
        ServiceException.class,
        () ->
            cuentaCorrienteService.getReporteListaDeCuentasCorrienteClientePorCriteria(
                BusquedaCuentaCorrienteClienteCriteria.builder().build(), 1L, "pdf"));
    assertThrows(
        ServiceException.class,
        () ->
            cuentaCorrienteService.getReporteListaDeCuentasCorrienteClientePorCriteria(
                BusquedaCuentaCorrienteClienteCriteria.builder().build(), 1L, "xlsx"));
    verify(messageSource, times(2)).getMessage(eq("mensaje_sucursal_404_logo"), any(), any());
    sucursal.setLogo(null);
    BusquedaProductoCriteria criteria = BusquedaProductoCriteria.builder().build();
    assertNotNull(
        cuentaCorrienteService.getReporteListaDeCuentasCorrienteClientePorCriteria(
            BusquedaCuentaCorrienteClienteCriteria.builder().build(), 1L, "pdf"));
    assertNotNull(
        cuentaCorrienteService.getReporteListaDeCuentasCorrienteClientePorCriteria(
            BusquedaCuentaCorrienteClienteCriteria.builder().build(), 1L, "pdf"));
  }
}

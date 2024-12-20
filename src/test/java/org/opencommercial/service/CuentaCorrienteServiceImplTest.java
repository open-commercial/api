package org.opencommercial.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.opencommercial.exception.ServiceException;
import org.opencommercial.model.*;
import org.opencommercial.model.criteria.BusquedaCuentaCorrienteClienteCriteria;
import org.opencommercial.repository.CuentaCorrienteClienteRepository;
import org.opencommercial.repository.CuentaCorrienteRepository;
import org.opencommercial.repository.RenglonCuentaCorrienteRepository;
import org.opencommercial.util.CustomValidator;
import org.opencommercial.util.FormatoReporte;
import org.opencommercial.util.JasperReportsHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(
        classes = {CuentaCorrienteServiceImpl.class, CustomValidator.class, MessageSource.class, JasperReportsHandler.class})
class CuentaCorrienteServiceImplTest {

  @MockBean CuentaCorrienteRepository<CuentaCorriente> cuentaCorrienteRepository;
  @MockBean CuentaCorrienteClienteRepository cuentaCorrienteClienteRepository;
  @MockBean RenglonCuentaCorrienteRepository renglonCuentaCorrienteRepository;
  @MockBean UsuarioService usuarioService;
  @MockBean ClienteService clienteService;
  @MockBean SucursalService sucursalService;
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
            cuentaCorrienteService.getBuilder(BusquedaCuentaCorrienteClienteCriteria.builder().build(), 1L),
            cuentaCorrienteService.getPageable(null, null, null, "cliente.nombreFiscal", Integer.MAX_VALUE)))
            .thenReturn(new PageImpl<>(cuentasCorriente));
    Sucursal sucursal = new Sucursal();
    sucursal.setLogo("noTieneImagen");
    when(sucursalService.getSucursalPredeterminada()).thenReturn(sucursal);
    assertThrows(
            ServiceException.class,
            () -> cuentaCorrienteService.getReporteListaDeCuentasCorrienteClientePorCriteria(
                    BusquedaCuentaCorrienteClienteCriteria.builder().build(), 1L, FormatoReporte.PDF));
    assertThrows(
            ServiceException.class,
            () -> cuentaCorrienteService.getReporteListaDeCuentasCorrienteClientePorCriteria(
                    BusquedaCuentaCorrienteClienteCriteria.builder().build(), 1L, FormatoReporte.XLSX));
    verify(messageSource, times(2)).getMessage(eq("mensaje_sucursal_404_logo"), any(), any());
    sucursal.setLogo(null);
    assertNotNull(
        cuentaCorrienteService.getReporteListaDeCuentasCorrienteClientePorCriteria(
            BusquedaCuentaCorrienteClienteCriteria.builder().build(), 1L, FormatoReporte.PDF));
    assertNotNull(
        cuentaCorrienteService.getReporteListaDeCuentasCorrienteClientePorCriteria(
            BusquedaCuentaCorrienteClienteCriteria.builder().build(), 1L, FormatoReporte.PDF));
  }
}

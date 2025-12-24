package org.opencommercial.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.opencommercial.model.CategoriaIVA;
import org.opencommercial.model.Proveedor;
import org.opencommercial.repository.ProveedorRepository;
import org.opencommercial.util.CustomValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(
    classes = {CustomValidator.class, ProveedorServiceImpl.class, MessageSource.class})
public class ProveedorServiceImplTest {

  @MockitoBean ProveedorRepository proveedorRepository;
  @MockitoBean CuentaCorrienteServiceImpl cuentaCorrienteService;
  @MockitoBean UbicacionServiceImpl ubicacionService;
  @MockitoBean MessageSource messageSource;

  @Autowired ProveedorServiceImpl proveedorService;

  @Test
  void shouldTestActualizarProveedor() {
    Proveedor proveedor = new Proveedor();
    proveedor.setRazonSocial("Razón social Proveedor");
    proveedor.setCategoriaIVA(CategoriaIVA.RESPONSABLE_INSCRIPTO);
    proveedor.setEmail("proveedor@delaempresa.com");
    proveedorService.actualizar(proveedor);
    verify(proveedorRepository).save(proveedor);
  }
}

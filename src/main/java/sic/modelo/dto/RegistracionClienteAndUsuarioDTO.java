package sic.modelo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sic.modelo.CategoriaIVA;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegistracionClienteAndUsuarioDTO implements Serializable {

  private String nombre;
  private String apellido;
  private String telefono;
  private String email;
  private CategoriaIVA categoriaIVA;
  private String nombreFiscal;
  private String password;
  private long idSucursal;
  private String recaptcha;
}
